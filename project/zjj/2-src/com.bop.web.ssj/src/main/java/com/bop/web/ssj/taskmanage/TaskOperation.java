package com.bop.web.ssj.taskmanage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcOperations;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;

public class TaskOperation {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private UserSession userSession;

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	
	
	
	/**
	 * 创建任务方法
	 * @author lh
	 * @param json 新增时传入的数据
	 * @return
	 */
	@Action
	public String addScheme(String json){
		ExtResultObject eor = new ExtResultObject();
		HttpServletRequest repquest = ActionContext.getActionContext().getHttpServletRequest();
		String addMonthcom = repquest.getParameter("addMonthcom").toString();
		String addYearcom = repquest.getParameter("addYearcom").toString();
		String faname = repquest.getParameter("faname").toString();
		String sql = "PLAN0102= "+addMonthcom+" and PLAN0101 = "+addYearcom;
		Records rds = this.recordDao.queryRecord("PLAN01", sql);

		if(rds.size()>0){
			//已经存在这种记录
			eor.add("inf", "false");
			eor.add("text", "已经存在相同时间段记录！");
			return eor.toString();
		}

		Records u1 = this.recordDao.queryRecord("USER01", "USER0101 = '"+this.userSession.getCurrentUserId()+"'");

		UUID uid = UUID.randomUUID();
		IRecord red =this.recordDao.createNew("PLAN01",uid, uid);
		red.put("PLAN0101", addYearcom);
		red.put("PLAN0102", addMonthcom);
		red.put("PLAN0103", new Date());
		red.put("PLAN0104",u1.get(0).get("user00"));
		red.put("PLAN0105", "0");
		red.put("PLAN0107", faname);
		this.recordDao.saveObject(red);

		eor.add("inf", "true");
		return eor.toString();
	}

	/**
	 * 删除任务方法
	 * @author bdsoft lh
	 * @return
	 */
	@Action
	public String deleteScheme(){
		ExtResultObject ero = new ExtResultObject();
		String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");

		IRecord p1 = this.recordDao.getRecord("PLAN01", UUID.fromString(id));
		if(p1.get("PLAN0105")!=null && "1".equals(p1.get("PLAN0105").toString())){
			return "false";
		}else{
			this.recordDao.deleteObject("PLAN01", UUID.fromString(id));
			ero.add("result", true);
			return "success";
		}
	}

	/**
	 * 加载任务列表的方法
	 * @return
	 * @author lh
	 */
	@Action
	public String getGridData(){
		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		Records rds = this.recordDao.queryRecord("PLAN01","1=1","plan0102");

		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		for(IRecord rd :rds){
			ExtObject eo = new ExtObject();

			eo.add("id", rd.getObjectId());
			eo.add("zftime",rd.get("PLAN0101").toString()+"0"+rd.get("PLAN0102").toString());
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
			eo.add("famc",rd.get("PLAN0107").toString());
			eo.add("zt", "1".equals(rd.get("PLAN0105").toString())?"已启用":"未启用");
			
			eoc.add(eo);
		}
		return eoc.toString();
	}

	/**
	 * 启用方案
	 * @return
	 */
	@Action
	public String goStart(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			String faid = jsonObject.get("id").toString();
			IRecord ird = this.recordDao.getRecord("PLAN01", UUID.fromString(faid));
			ird.put("PLAN0105", 1);
			this.recordDao.saveObject(ird);
			//方案启动生成一条记录状态的记录，复制人员信息到plan02,复制企业信息到plan04后两步用触发器实现
			this.insertPlan3(faid);
		}
		return "success";
	}
	/**
	 * 启动任务生出各区县状态表
	 * @author bdsoft lh
	 * @param faid
	 */
	private void insertPlan3(String faid){
		//需要区分是市局还是区县的用户
		String zone = this.userSession.getCurrentUserZone();
		if(zone!=null&&!"".equals(zone)){
			Object [] args = new Object[4];
			String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
			args[0]=faid;
			args[1]=faid;
			args[2]=this.userSession.getCurrentUserZone();
			args[3]=0;
			this.jdbcTemplate.update(sql, args);
		}else{
			Records irds = this.recordDao.queryRecord("dm_codetable_data", "codetablename='DB064' and cid<>'110000'");
			for(IRecord ird:irds){
				Object [] args = new Object[4];
				String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
				args[0]=faid;
				args[1]=faid;
				args[2]=ird.get("CID");
				args[3]=0;
				this.jdbcTemplate.update(sql, args);
			}
		}
	}

	
}
