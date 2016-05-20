package com.bop.web.bopmain;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.HostUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bop.common.DateUnit;
import com.bop.common.DateUtility;
import com.bop.domain.IRecordDao;
import com.bop.hibernate.dbinit.ExecutableUpgrader;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridColumn;
import com.bop.json.ExtGridRow;
import com.bop.module.function.MenuItem;
import com.bop.module.function.service.FunctionTree;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.PathUtil;
import com.bop.web.bopmain.internal.FunctionTreeUtil;
import com.bop.web.bopmain.internal.NavigationService;
import com.bop.web.bopmain.internal.UpgradeExecutor;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.bop.web.rest.renderer.Renderer;
import com.bop.web.rest.renderer.TemplateRenderer;

@Controller
public class MainPage {
	private static final Logger log = LoggerFactory.getLogger(MainPage.class);
	private UpgradeExecutor upgraderExecutor;
	private IRecordDao recordDao;
	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public IRecordDao getRecordDao() {
		return recordDao;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	@Action
	public void logon() throws ServletException, IOException {
		HttpServletRequest req = ActionContext.getActionContext().getHttpServletRequest();
		HttpServletResponse resp = ActionContext.getActionContext().getHttpServletResponse();
		
		if(this.upgraderExecutor.isNeedUpgrader()) {
			ActionContext.getActionContext().getHttpServletResponse().sendRedirect("/bopmain/autoUpdate2.jsp?theme=none");
		}

		log.debug("logon system");

		String errorClassName = (String)req.getAttribute("shiroLoginFailure");
        if(UnknownAccountException.class.getName().equals(errorClassName)) {    			//没有这个账号
            req.setAttribute("error", "用户名/密码错误");
        } else if(IncorrectCredentialsException.class.getName().equals(errorClassName)) {   //密码错误
        	// 此处已设置好了error错误信息
        	String error = (String)req.getAttribute("error");
        	System.out.println(error);
        } else if(LockedAccountException.class.getName().equals(errorClassName)){			//用户锁定
        	req.setAttribute("error", "该账户已锁定");
        } else if(HostUnauthorizedException.class.getName().equals(errorClassName)){		//IP访问IP和绑定的IP不一致
        	req.setAttribute("error", "该用户非此IP绑定用户");
        } else if(errorClassName != null) {
            req.setAttribute("error", "未知错误：" + errorClassName);  
        }
        req.getRequestDispatcher("/theme/logon.jsp").forward(req, resp);
	}

	@Action
	public Renderer desktop() {
		Map<String, Object> vc = new HashMap<String, Object>();

		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String sid = request.getParameter("sid");
		FunctionTree tree = NavigationService.getMe().getSystemTreeMenu();
		MenuItem m = tree.getById(sid);

		//这个很重要呀，找了半天才找到
		Principal username = request.getUserPrincipal();

		String html = "";
		if(m != null) 
			html = FunctionTreeUtil.toNavHtml(tree, m, sid, "");

		vc.put("sys", m);
		vc.put("html", html);
		String ts = "";

		if(username!=null){
			int configPromptTime = Integer.parseInt(System.getProperty("bop.safety.exceedTimePromptDays","7"));   //系统配置距离密码到期提醒天数 如设置为7 ，则在密码小于7天到期的时间内一直提醒
			int sysPromptTime = this.outDate(username.toString());						 //系统记录密码到期的天数    从 密码上次修的有效时间段内到今天的天数
			if(sysPromptTime > 0){
				if(sysPromptTime<configPromptTime){
					ts += "密码还有" +sysPromptTime+ "天过期，为保护密码安全，请及时修改！";
				}
			}else if(sysPromptTime < 0){
				ts += "密码已经过期" + -sysPromptTime+ "天，为保护密码安全，请及时修改！";
			}
		}

		vc.put("ts", ts);
		return new TemplateRenderer(this.getClass(), "desktop", vc);
	}

	@Action
	public void logout() throws IOException {
		log.debug("logout system");
		
		ActionContext.getActionContext().getHttpServletRequest().getSession().invalidate();
		ActionContext.getActionContext().getHttpServletResponse().sendRedirect(PathUtil.getLogonUrl());
	}
	
	@Action
	public String upgradeModule(String name) {
		try
		{
			// 执行升级
			this.upgraderExecutor.ExecuteUpgraders(name);
			return "true";
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return ex.getMessage();
		}
	}
	
	@Action
	public String upgraders() throws IOException {
		if (!this.upgraderExecutor.isNeedUpgrader()) {
			ActionContext.getActionContext().getHttpServletResponse().sendRedirect(PathUtil.getLogonUrl());
		}

		Map<String, ExecutableUpgrader[]> map = this.upgraderExecutor.getModuleUpdaters();
		// 创建表格
		ExtGrid grid = new ExtGrid(); 
		grid.setWithColumn(true);
		// 添加表头
		grid.columns.addAll(createModuleColumnsHeader());
		for(Object moduleName:map.keySet())
		{			
			ExecutableUpgrader[] us = map.get(moduleName);
			// 升级描述
			String updateDesc ="";

			for(int i=0;i<us.length;i++)
			{
				updateDesc = updateDesc + "<br/>" + us[i].getDescription();
			}

			// 放入gridrow
			ExtGridRow egr = new ExtGridRow();
			egr.add("moduleName", moduleName);
			egr.add("description", updateDesc);
			egr.add("status", "");
			egr.add("isTrue", "false");

			// 加入grid
			grid.rows.add(egr);
		}
		return grid.toString();
	}
	
	private List<ExtGridColumn> createModuleColumnsHeader() {
		List<ExtGridColumn>  grid = new ArrayList<ExtGridColumn>();
		grid.add(new ExtGridColumn("模块名称","moduleName",200));
		grid.add(new ExtGridColumn("升级信息描述","description",300));
		grid.add(new ExtGridColumn("升级状态","status"));
		ExtGridColumn col1 = new ExtGridColumn("isTrue","isTrue");
		col1.setHidden(true);
		grid.add(col1);
		return grid;
	}

	public void setUpgradeExecutor(UpgradeExecutor ue) {
		this.upgraderExecutor = ue;
	}

	/**
	 * 得到一个离过期还有多少天的天数
	 * @param username
	 * @return
	 */
	private int outDate(String username) {
		User01 u = this.userService.getByLoginName(username);
		
		// 最后一次修改密码的时间
		Date d = u.getChangeDate();
		
		//密码有效的天数
		Integer i = u.getUserforTime();
		if(i == null) i = 0;
		d = DateUtility.dateAfter(d, i, DateUnit.day);
		
		int d2 = (int)DateUtility.dateDiff(new Date(), d, DateUnit.day);
		
		return d2;
	}
}
