package com.bop.web.bopmain.author;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.bop.module.user.dao.User01;
import com.bop.common.StringUtility;
import com.bop.hibernate.dbinit.DbUtil;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridColumn;
import com.bop.json.ExtGridRow;
import com.bop.json.ExtObject;
import com.bop.json.ExtResultObject;
import com.bop.module.author.AuthorService;
import com.bop.module.author.RoleService;
import com.bop.module.author.dao.Role;
import com.bop.module.function.MenuCommand;
import com.bop.module.function.MenuItem;
import com.bop.module.function.service.FunctionService;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.command.AutoNamedWebCommandImpl;
import com.bop.web.rest.Action;
import com.bop.web.rest.Controller;

@Controller
public class AuthorAjaxCommand extends AutoNamedWebCommandImpl {
	private AuthorService authorService;
	private FunctionService functionService;
	private RoleService roleService;
	private UserService userService;
//	private RoleUserService roleUserService;
	private JdbcOperations jdbcOperations;

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String op = request.getParameter("op");
		String id = request.getParameter("id");

		if ("get".equals(op)) {
			return this.getRoleAuthor(id);
		} else if ("getuserauthor".equals(op)) {
			String pid = request.getParameter("pid");
			if(StringUtility.isNullOrEmpty(pid))
				return this.getUserAuthor(id);
			else 
				return this.getUserAuthor(id, UUID.fromString(pid));
		} else if ("getuserrole".equals(op)) {
			return this.getUserRole(id);
		} else if ("set".equals(op)) {
			String fid = request.getParameter("fid");
			String chk = request.getParameter("check");
			return this.setRoleAuthor(id, fid, "true".equals(chk)).toString();
		} else if ("setcmd".equals(op)) {
			String cid = request.getParameter("cid");
			String chk = request.getParameter("check");
			return this.setRoleAuthorCmd(id, cid, "true".equals(chk)).toString();
		} else if ("sysset".equals(op)) {
			String arrayfid = request.getParameter("fid");
			String[] fid = arrayfid.split(",");
			String chk = request.getParameter("check");
			return this.setSysRoleAuthor(id, fid, "true".equals(chk))
					.toString();
		} else if ("setuser".equals(op)) {
			String uid = request.getParameter("uid");
			String chk = request.getParameter("check");
			return this.setRoleUser(id, uid, "true".equals(chk)).toString();
		} else if ("setusers".equals(op)) {
			String filter = request.getParameter("filter");
			String chk = request.getParameter("check");
			return this.setRoleUsers(id, filter, "true".equals(chk)).toString();
		}

		return "";
	}

	private Object setRoleAuthorCmd(String rid, String cid, boolean check) {
		if (check) {
			this.authorService.addCommandToRole(rid, cid);
		} else {
			this.authorService.removeCommandAtRole(rid, cid);
		}

		return new ExtResultObject(true);
	}

	private Object setSysRoleAuthor(String rid, String[] fid, boolean check) {
		if (check) {
			for (String f : fid) {
				this.authorService.addFunctionToRole(rid, f);
			}
		} else {
			for (String f : fid) {
				this.authorService.removeFunctionAtRole(rid, f);
			}
		}
		return new ExtResultObject(true);
	}

	
	
	
	
	//判断是否是三员之一，如果是三员之一  为ture的时候
	public void authorUserToRole(String rid, String uid) {
		
		User01 u = this.userService.getByLoginName(uid);
		if(u == null) return;
		String excludeRols;
		boolean isExcludeRols = false;
		try {
			excludeRols = new String(System.getProperty("bop.safety.excludeRoles", "").getBytes("ISO-8859-1"),"UTF-8");//配置中被拒绝设置的角色们
			if(StringUtility.isNullOrEmpty(excludeRols)) excludeRols="";
			isExcludeRols= excludeRols.contains(this.roleService.getRole(rid).getRoleName());//是否为被拒绝操作的角色
		} catch (UnsupportedEncodingException e) {
			isExcludeRols = false;
		}
		
		String excludeUsers = System.getProperty("bop.safety.excludeUsers", "");//配置中被拒绝设置的用户
		if(StringUtility.isNullOrEmpty(excludeUsers)) excludeUsers="";
		
		String[] excludeUsersList = excludeUsers.split(",");
		boolean isExcludeUsers= false;
		if(excludeUsersList.length>0) {
			for(int i=0;i<excludeUsersList.length;i++) {
				if(excludeUsersList[i].equals(this.userService.getByLoginName(uid).getLoginName())){
					isExcludeUsers=true;
					break;
				}
			}
		}
		if(isExcludeRols || isExcludeUsers) return;
		this.roleService.addUser2Role(rid, uid);
	}
	
	
	
	//为false的时候
	public void unAuthorUserToRole(String rid, String uid) {
		User01 u = this.userService.getByLoginName(uid);
		if(u == null) return;
		String excludeRols;
		boolean isExcludeRols = false;
		try {
			excludeRols = new String(System.getProperty("bop.safety.excludeRoles").getBytes("ISO-8859-1"),"UTF-8");//配置中被拒绝设置的角色们
			if(StringUtility.isNullOrEmpty(excludeRols)) excludeRols="";
			isExcludeRols= excludeRols.contains(this.roleService.getRole(rid).getRoleName());//是否为被拒绝操作的角色
		} catch (UnsupportedEncodingException e) {
			isExcludeRols = false;
		}

		String excludeUsers = System.getProperty("bop.safety.excludeUsers");//配置中被拒绝设置的用户
		if(StringUtility.isNullOrEmpty(excludeUsers)) excludeUsers="";
		
		String[] excludeUsersList = excludeUsers.split(",");
		boolean isExcludeUsers= false;
		if(excludeUsersList.length>0) {
			for(int i=0;i<excludeUsersList.length;i++) {
				if(excludeUsersList[1].equals(this.userService.getByLoginName(uid).getLoginName())){//是否为被拒绝操作的用户   ,这样有一个缺陷只能判断配置的第一个用户是否是不可配置的
					isExcludeUsers=true;
					break;
				}
			}
		}
		if(isExcludeRols || isExcludeUsers) return;
		this.roleService.removeUserFromRole(rid, uid);
	}
	
	
	
	private ExtResultObject setRoleUser(String id, String uid, boolean check) {
		uid = uid.substring(0, uid.length()-1);
		String[] uids = uid.split(",");
		if (check)
			for(String userId : uids) {
				this.authorUserToRole(id,userId);
				//this.roleService.addUser2Role(id, userId);
			}
		else {
			for(String userId : uids) {
				this.unAuthorUserToRole(id, userId);
				//this.roleService.removeUserFromRole(id, userId);
			}
		}
		return new ExtResultObject(true);
	}
	
	private ExtResultObject setRoleUsers(String id, String filter, boolean check) {
		return this.setRoleUser(id, filter, check);
	}

	private static final String SYSROWSPAN = "__sysrowspan__";
	private static final String CATROWSPAN = "__catrowspan__";
	private static final String SYSCID = "__syscid__";
	private static final String CATCID = "__catcid__";
	
	private String getRoleAuthor(String rid) throws Exception {
		String excludeRols = new String (System.getProperty("bop.safety.excludeRoles","").getBytes("ISO-8859-1"),"UTF-8");//配置中被拒绝设置的角色们
		if(StringUtility.isNullOrEmpty(excludeRols)) excludeRols="";
		String excludeFunctions = System.getProperty("bop.safety.excludeFunctions");//配置中被拒绝设置的功能们
		if(StringUtility.isNullOrEmpty(excludeFunctions)) excludeFunctions="";
		
		Role role = this.roleService.getRole(rid);
		boolean isExcludeRols= excludeRols.contains(role.getRoleName());//是否为被拒绝操作的角色
		boolean inProject = false;

		List<MenuItem> ms = this.functionService.getFunctionList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"input_form\"><table id=\"input_table_id\"><tr>");
		sb.append("<th width=\"10%\">系统</th width=\"10%\"><th width=\"10%\">分类</th><th width=\"15%\">功能</th><th>描述</th><th width=\"20%\">操作</th>");
		sb.append("</tr>");

		int sysrowspan = 1;
		int catrowspan = 1;

		String presys = "";
		String precat = "";
		List<String> currentSysCids = new ArrayList<String>();
		List<String> currentCatCids = new ArrayList<String>();

		/**
		 * 这里是之前做的逻辑，可能有点小问题。
		 * 这里的逻辑现在应该是这样的， 先判断如果这个角色是三员之一，那么权限分配这一块是不可改动
		 * 在判断如果，这个function模块是三员之一的，那么就是选中的状态
		 * 
		 * 
		 * 如果三员的权限在配置文件中是唯一可佩的，那么这里还需要改动，如果只是做一个限制，当前的功能就够用
		 */

		for(MenuItem m : ms) {
			if(inProject) {
				if(m.isNeedProject() != inProject) continue;
			}

			//是否为被拒绝操作的功能
			boolean isExcludeFunctions= excludeFunctions.contains(m.getId());//此处只对具体功能进行处理,不对父节点进行考虑
			String disabled= "";//是否允许修改,只针对具体功能和操作,父节点不进行考虑. 即不考虑fid相似
			if(isExcludeRols || isExcludeFunctions) disabled = "disabled";
			String temp = "";

			/**
			 * 一下未改之前 改动之后的变化是，只有配置在配置文件的function模块是可用的。其他的任何方式进来的模块都不可用。
			 * 
			 * if(this.authorService.isRoleCanAccess(rid, m.getId())) temp = "checked";
			 * if(this.authorService.isRoleCanAccess(rid, m.getId()) && isExcludeFunctions) temp = "checked";
			 */
			if(this.authorService.isRoleCanAccess(rid, m.getId())) temp = "checked";

			String sys = this.getFunctionSystem(m);
			String cat = this.getFunctionSubSystem(m);
			sb.append("<tr>");
			if(presys.equals(sys))
			{
				sysrowspan ++;
				currentSysCids.add(m.getId());
				
				if(precat.equals(cat)) {
					catrowspan ++;
					currentCatCids.add(m.getId());
				} else { //进入下一个分类
					int index2 = sb.indexOf(CATROWSPAN);
					if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
					int index3 = sb.indexOf(CATCID);
					if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
					sb.append(String.format("<td rowspan=\"%s\">" +
							"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" onclick=\"checkCids('%s',this);\" />" +
							"<label for=\"%s\" />%s</td>" +
							"</td>", CATROWSPAN, CATCID, cat, rid, cat, cat));
					catrowspan = 1;
					currentCatCids.clear();
					currentCatCids.add(m.getId());
				}
			} else { //进入下一个系统，肯定也进入了下一个分类
				int index1 = sb.indexOf(SYSROWSPAN);
				if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
				int index4 = sb.indexOf(SYSCID);
				if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" onclick=\"checkCids('%s',this);\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", SYSROWSPAN, SYSCID, sys, rid, sys, sys));
				
				int index2 = sb.indexOf(CATROWSPAN);
				if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
				int index3 = sb.indexOf(CATCID);
				if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
				
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\"  type=\"checkbox\" name=\"%s\" onclick=\"checkCids('%s',this);\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", CATROWSPAN, CATCID, cat, rid, cat, cat));
				
				sysrowspan = 1;
				currentSysCids.clear();
				currentSysCids.add(m.getId());
				catrowspan = 1;
				currentCatCids.clear();
				currentCatCids.add(m.getId());
			}
			
			presys = sys;
			precat = cat;
			
			sb.append(String.format("<td><input type=\"checkbox\" %s %s id=\"%s\" name=\"%s\" onclick=\"setFunction2Role('%s',this);\" />",
					temp, disabled, m.getId(), m.getName(), rid));
			sb.append(String.format("<label for=\"%s\" />%s</td>", m.getId(), m.getName()));
			sb.append(String.format("<td>%s</td>", m.getDescription()));
			
			sb.append("<td>");
			for(MenuCommand cm : m.getCommands()) {
				boolean canAccess = this.authorService.isRoleCanAccessCommand(rid, cm.getId());
				sb.append(String.format("<input pid=\"%s\" type=\"checkbox\" %s %s id=\"%s\" name=\"%s\" onclick=\"setCommand2Role('%s', this);\"/>",
						m.getId(), canAccess ? "checked" : "", disabled, cm.getId(), cm.getName(), rid));
				sb.append(String.format("<label for\"%s\">%s</label>", cm.getId(), cm.getName()));
				sb.append("<br>");
			}
			sb.append("</td>");
			
			sb.append("</tr>");
		}

		int index1 = sb.indexOf(SYSROWSPAN);
		if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
		int index4 = sb.indexOf(SYSCID);
		if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
		
		int index2 = sb.indexOf(CATROWSPAN);
		if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
		int index3 = sb.indexOf(CATCID);
		if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
		
		sb.append("</table></div>");
		
		
		return sb.toString();
	}
	
	private String getUserAuthor(String uid, UUID pid) throws Exception {
		List<MenuItem> ms = this.functionService.getFunctionList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"input_form\"><table><tr>");
		sb.append("<th width=\"10%\">系统</th width=\"10%\"><th width=\"10%\">分类</th><th width=\"15%\">功能</th><th>描述</th><th width=\"20%\">操作</th>");
		sb.append("</tr>");
		
		int sysrowspan = 1;
		int catrowspan = 1;
		
		String presys = "";
		String precat = "";
		
		List<String> currentSysCids = new ArrayList<String>();
		List<String> currentCatCids = new ArrayList<String>();
		
		for(MenuItem m : ms) {
			String temp = "";
			if(this.authorService.isUserCanAccess(uid, m.getId())) temp = "checked";
			
			String sys = this.getFunctionSystem(m);
			String cat = this.getFunctionSubSystem(m);
			
			sb.append("<tr>");
			
			if(presys.equals(sys))
			{
				sysrowspan ++;
				currentSysCids.add(m.getId());
				
				if(precat.equals(cat)) {
					catrowspan ++;
					currentCatCids.add(m.getId());
				} else { //进入下一个分类
					int index2 = sb.indexOf(CATROWSPAN);
					if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
					int index3 = sb.indexOf(CATCID);
					if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
					sb.append(String.format("<td rowspan=\"%s\">" +
							"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" />" +
							"<label for=\"%s\" />%s</td>" +
							"</td>", CATROWSPAN, CATCID, cat, cat, cat));
					catrowspan = 1;
					currentCatCids.clear();
					currentCatCids.add(m.getId());
				}
			} else { //进入下一个系统，肯定也进入了下一个分类
				int index1 = sb.indexOf(SYSROWSPAN);
				if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
				int index4 = sb.indexOf(SYSCID);
				if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", SYSROWSPAN, SYSCID, sys, sys, sys));
				
				int index2 = sb.indexOf(CATROWSPAN);
				if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
				int index3 = sb.indexOf(CATCID);
				if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
				
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\"  type=\"checkbox\" name=\"%s\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", CATROWSPAN, CATCID, cat, cat, cat));
				
				sysrowspan = 1;
				currentSysCids.clear();
				currentSysCids.add(m.getId());
				catrowspan = 1;
				currentCatCids.clear();
				currentCatCids.add(m.getId());
			}
			
			presys = sys;
			precat = cat;
			
			sb.append(String.format("<td><input type=\"checkbox\" %s id=\"%s\" name=\"%s\" />",
					temp, m.getId(), m.getName()));
			sb.append(String.format("<label for=\"%s\" />%s</td>", m.getId(), m.getName()));
			sb.append(String.format("<td>%s</td>", m.getDescription()));
			
			sb.append("<td>");
			for(MenuCommand cm : m.getCommands()) {
				boolean canAccess = this.authorService.isRoleCanAccessCommand(uid, cm.getId());
				sb.append(String.format("<input pid=\"%s\" type=\"checkbox\" %s id=\"%s\" name=\"%s\" />",
						m.getId(), canAccess ? "checked" : "", cm.getId(), cm.getName()));
				sb.append(String.format("<label for\"%s\">%s</label>", cm.getId(), cm.getName()));
				sb.append("<br>");
			}
			sb.append("</td>");
			
			sb.append("</tr>");
		}

		int index1 = sb.indexOf(SYSROWSPAN);
		if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
		int index4 = sb.indexOf(SYSCID);
		if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
		
		int index2 = sb.indexOf(CATROWSPAN);
		if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
		int index3 = sb.indexOf(CATCID);
		if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
		
		sb.append("</table></div>");
		return sb.toString();
	}
	
	private String getUserAuthor(String uid) throws Exception {
		List<MenuItem> ms = this.functionService.getFunctionList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"input_form\"><table><tr>");
		sb.append("<th width=\"10%\">系统</th width=\"10%\"><th width=\"10%\">分类</th><th width=\"15%\">功能</th><th>描述</th><th width=\"20%\">操作</th>");
		sb.append("</tr>");
		
		int sysrowspan = 1;
		int catrowspan = 1;
		
		String presys = "";
		String precat = "";
		
		List<String> currentSysCids = new ArrayList<String>();
		List<String> currentCatCids = new ArrayList<String>();
		
		for(MenuItem m : ms) {
			String temp = "";
			if(this.authorService.isUserCanAccess(uid, m.getId())) temp = "checked";
			
			String sys = this.getFunctionSystem(m);
			String cat = this.getFunctionSubSystem(m);
			
			sb.append("<tr>");
			
			if(presys.equals(sys))
			{
				sysrowspan ++;
				currentSysCids.add(m.getId());
				
				if(precat.equals(cat)) {
					catrowspan ++;
					currentCatCids.add(m.getId());
				} else { //进入下一个分类
					int index2 = sb.indexOf(CATROWSPAN);
					if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
					int index3 = sb.indexOf(CATCID);
					if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
					sb.append(String.format("<td rowspan=\"%s\">" +
							"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" />" +
							"<label for=\"%s\" />%s</td>" +
							"</td>", CATROWSPAN, CATCID, cat, cat, cat));
					catrowspan = 1;
					currentCatCids.clear();
					currentCatCids.add(m.getId());
				}
			} else { //进入下一个系统，肯定也进入了下一个分类
				int index1 = sb.indexOf(SYSROWSPAN);
				if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
				int index4 = sb.indexOf(SYSCID);
				if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\" type=\"checkbox\" name=\"%s\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", SYSROWSPAN, SYSCID, sys, sys, sys));
				
				int index2 = sb.indexOf(CATROWSPAN);
				if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
				int index3 = sb.indexOf(CATCID);
				if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
				
				sb.append(String.format("<td rowspan=\"%s\">" +
						"<input cid=\"%s\"  type=\"checkbox\" name=\"%s\" />" +
						"<label for=\"%s\" />%s</td>" +
						"</td>", CATROWSPAN, CATCID, cat, cat, cat));
				
				sysrowspan = 1;
				currentSysCids.clear();
				currentSysCids.add(m.getId());
				catrowspan = 1;
				currentCatCids.clear();
				currentCatCids.add(m.getId());
			}
			
			presys = sys;
			precat = cat;
			
			sb.append(String.format("<td><input type=\"checkbox\" %s id=\"%s\" name=\"%s\" />",
					temp, m.getId(), m.getName()));
			sb.append(String.format("<label for=\"%s\" />%s</td>", m.getId(), m.getName()));
			sb.append(String.format("<td>%s</td>", m.getDescription()));
			
			sb.append("<td>");
			for(MenuCommand cm : m.getCommands()) {
				boolean canAccess = this.authorService.isUserCanAccessCommand(uid, cm.getId());
				sb.append(String.format("<input pid=\"%s\" type=\"checkbox\" %s id=\"%s\" name=\"%s\" />",
						m.getId(), canAccess ? "checked" : "", cm.getId(), cm.getName()));
				sb.append(String.format("<label for\"%s\">%s</label>", cm.getId(), cm.getName()));
				sb.append("<br>");
			}
			sb.append("</td>");
			
			sb.append("</tr>");
		}

		int index1 = sb.indexOf(SYSROWSPAN);
		if(index1 > 0) sb.replace(index1, index1 + SYSROWSPAN.length(), String.valueOf(sysrowspan));
		int index4 = sb.indexOf(SYSCID);
		if(index4 > 0) sb.replace(index4, index4 + SYSCID.length(), StringUtility.arrayToString(currentSysCids));
		
		int index2 = sb.indexOf(CATROWSPAN);
		if(index2 > 0) sb.replace(index2, index2 + CATROWSPAN.length(), String.valueOf(catrowspan));
		int index3 = sb.indexOf(CATCID);
		if(index3 > 0) sb.replace(index3, index3 + CATCID.length(), StringUtility.arrayToString(currentCatCids));
		
		sb.append("</table></div>");
		return sb.toString();
	}
	
	private String getUserRole(final String uid) throws Exception {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"input_form\"><table>");
		
		sb.append(String.format("<thead><th colspan=2>用户 <a href=\"/module/author/author/userauthor.jsp?id=%s\">%s</a>" +
				" 的权限</th></thead>", uid, uid));
		
		sb.append("<tr><th width=\"200\">系统或项目</th><th width=\"300\">有权限的角色</th></tr>");
		
		//系统角色
		sb.append("<tr><th>系统角色</th>");
		List<Role> sysRoles = this.roleService.getUserRoles(uid);
		sb.append("<td>");
		for(Role r : sysRoles) {
			sb.append(String.format("<a href=\"/module/author/author/roleauthor.jsp?id=%s\">%s</a><br>",
					r.getAu00(), r.getRoleName()));
		}
		sb.append("</td></tr>");
		if (DbUtil.isObjectExist(jdbcOperations, "P08")) {
			// 项目下的角色
			String sql = "select distinct p01.p00, p01.p0101 "
					+ " from p07 inner join p08 on p07.recordid = p08.parentid"
					+ " inner join au01 on p08.p0801 = au01.au00"
					+ " inner join a01 on p07.p0702 = a01.recordid"
					+ " inner join p01 on p07.p00 = p01.p00"
					+ " where a0102 = '" + uid + "'" + " order by p01.p0101";
			jdbcOperations.query(sql, new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					sb.append(String.format("<tr><th>%s</th><td>",
							rs.getString("p0101")));

					String sql2 = "select au01.au0101, au01.au00 "
							+ " from p07 inner join p08 on p07.recordid = p08.parentid"
							+ " inner join au01 on p08.p0801 = au01.au00"
							+ " inner join a01 on p07.p0702 = a01.recordid"
							+ " inner join p01 on p07.p00 = p01.p00"
							+ " where a0102 = '" + uid + "' and p01.p00 = '"
							+ rs.getString("p00") + "'"
							+ " order by au01.au0104";
					jdbcOperations.query(sql2, new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs2)
								throws SQLException {
							sb.append(String
									.format("<a href=\"/module/author/author/roleauthor.jsp?id=%s\">%s</a><br>",
											rs2.getString("au00"),
											rs2.getString("au0101")));
						}
					});

					sb.append("</td></tr>");
				}
			});
		}else{
			String sql = "select distinct p01.p00, p01.p0101 "
					+ " from p07 "
					+ " inner join a01 on p07.p0702 = a01.recordid"
					+ " inner join p01 on p07.p00 = p01.p00"
					+ " where a0102 = '" + uid + "'" + " order by p01.p0101";
			jdbcOperations.query(sql, new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					sb.append(String.format("<tr><th>%s</th><td>",
							rs.getString("p0101")));

					String sql2 = "select p07.p0706 as roleNames,p07.p0713 as roleIds"
							+ " from p07 "
							+ " inner join a01 on p07.p0702 = a01.recordid"
							+ " inner join p01 on p07.p00 = p01.p00"
							+ " where a0102 = '" + uid + "' and p01.p00 = '"
							+ rs.getString("p00") + "'";
					jdbcOperations.query(sql2, new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs2)
								throws SQLException {
							String roleNames = rs2.getString("roleNames");
							String roleIDs = rs2.getString("roleIds");
							String[] roleNameList = roleNames.split(",");
							String[] roleIDList = roleIDs.split(",");
							if (roleNameList.length == roleIDList.length) {
								for (int j = 0; j < roleIDList.length; j++) {
									sb.append(String
											.format("<a href=\"/module/author/author/roleauthor.jsp?id=%s\">%s</a><br>",
													roleIDList[j],
													roleNameList[j]));
								}
							}

						}
					});

					sb.append("</td></tr>");
				}
			});
		}
		sb.append("</table></div>");
		return sb.toString();
	}
	
	private String getFunctionSystem(MenuItem item) {
		MenuItem parent = item.getParent();
		List<MenuItem> parents = new ArrayList<MenuItem>();

		while (parent != null) {
			parents.add(0, parent);
			parent = parent.getParent();
		}

		if (parents.size() > 0)
			return parents.get(0).getName();
		else
			return "";
	}

	private String getFunctionSubSystem(MenuItem item) {
		MenuItem parent = item.getParent();
		StringBuilder sb = new StringBuilder();
		List<MenuItem> parents = new ArrayList<MenuItem>();

		while (parent != null) {
			parents.add(0, parent);
			parent = parent.getParent();
		}

		if (parents.size() > 0)
			parents.remove(0);

		if (parents.size() == 0)
			return "未分类";

		for (MenuItem i : parents)
			sb.append(i.getName() + "/");

		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	private ExtResultObject setRoleAuthor(String rid, String fid, boolean check) {
		if (check) {
			this.authorService.addFunctionToRole(rid, fid);
		} else {
			this.authorService.removeFunctionAtRole(rid, fid);
		}

		return new ExtResultObject(true);
	}

	public void setAuthorService(AuthorService service) {
		this.authorService = service;
	}

	public void setFunctionService(FunctionService service) {
		this.functionService = service;
	}
	
	public void setUserService(UserService service) {
		this.userService = service;
	}

	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	@Action
	public String getRoleUsers(String rid) {
		List<String> uids = this.roleService.getRoleUsers(rid);
		List<User01> users = new ArrayList<User01>();

		for(int i = 0; i < uids.size(); i ++) {
			String id = uids.get(i);
			User01 user = this.userService.getByLoginName(id);
			users.add(user);
		}
		
		int total = this.userService.getUsers().size();
		
		return this.getUsersGrid(users, total).toString();
	}
//	
//	//授权在搜索
//	public ExtGrid getSearchRoleUsers(String rid, String filter, int start, int limit) {
//		List<User01> users = this.roleUserService.getRoleUsers(rid, filter, start, limit);
//		int total = this.roleUserService.getRoleUsers(rid, filter, 0, 0).size();
//		return this.getUsersGrid(users, total);
//	}
//	
//	//未授权的
//	public ExtGrid getSearchUser(String rid, String filter, int start, int limit) {
//		List<User01> users = this.roleUserService.getUsersForRole(rid, filter, start, limit);
//		int total = this.roleUserService.getUsersForRole(rid, filter, 0, 0).size();
//		return this.getUsersGrid(users, total);
//	}

	@Action
	public String getUsersForRole(String rid) {
		List<User01> users = this.userService.getUsers();
		List<String> uids = this.roleService.getRoleUsers(rid);
		List<User01> result = new ArrayList<User01>();
		
		for(User01 u : users) {
			if(!uids.contains(u.getUser00())) {
				result.add(u);
			}
		}
		
		int total = result.size();
		
		return this.getUsersGrid(result, total).toString();
	}
	
	private ExtGrid getUsersGrid(List<User01> users, int total) {
		ExtGrid grid = new ExtGrid();
		grid.setWithColumn(true);
		grid.columns.addAll(this.createHeaders());
		grid.rows.addAll(this.getRows(users));
		grid.setTotal(total);
		grid.setPageSize(30);
		return grid;
	}
	
	private Collection<? extends ExtObject> getRows(List<User01> users) {
		List<ExtGridRow> rows = new ArrayList<ExtGridRow>();
		for (User01 u : users) {
			if (!users.contains(u.getLoginName())){
				ExtGridRow row = new ExtGridRow();
				row.add("id", u.getLoginName());
				row.add("name", u.getLoginName() + "(" + u.getUserName() + ")");
				row.add("des", u.getDescription());
				rows.add(row);
			}
		}
		
		return rows;
	}

	private List<ExtGridColumn> createHeaders(){
		List<ExtGridColumn> cols = new ArrayList<ExtGridColumn>();
		
		ExtGridColumn col1 = new ExtGridColumn("id", "id");
		ExtGridColumn col2 = new ExtGridColumn("用户名", "name");
		ExtGridColumn col3 = new ExtGridColumn("描述", "des");
		col1.setHidden(true);
		cols.add(col1);
		cols.add(col2);
		cols.add(col3);
		
		return cols;
	}

	public void setJdbcOperations(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
	}
}
