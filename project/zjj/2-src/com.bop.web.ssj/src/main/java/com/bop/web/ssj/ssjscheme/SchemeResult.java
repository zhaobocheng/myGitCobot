package com.bop.web.ssj.ssjscheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class SchemeResult {
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
	 * 获取代码表值
	 * @papam 代码表名
	 * 
	 */
	@Action
	public String getCode(String tableName){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String querySql = null;

		querySql = "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = '"+tableName+"'";

		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();	
				eo.add("id", map.get("cid"));				
				eo.add("text", map.get("caption"));
				eo.add("value", map.get("cid"));
				eoc.add(eo);
			}
		}
		return eoc.toString();				
	}
	/**
	 * 获取任务列表
	 * @param 任务名称
	 * @param 月份
	 * @return 返回没有提交和可以录入结果的任务
	 */
	@Action
	public String getSchemeDate(){
		ExtObjectCollection eoc = new ExtObjectCollection();
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String rwmc = request.getParameter("rwmc")==null?null:request.getParameter("rwmc").toString();
		String month = request.getParameter("month")==null?null:request.getParameter("month").toString();

		String zone = this.userSession.getCurrentUserZone();
		
		String querySql = "select * from plan01,plan12 where plan01.plan00=plan12.parentid and plan1210='保存'";

		if(null!=zone&&!"".equals(zone)){	
			querySql += " and plan1204='"+zone+"'";
		}
		
		if  (null!=rwmc &&!"".equals(rwmc)){
			querySql += " and plan0107 like '%"+rwmc+"%'";
		}
		if  (null!=month &&!"".equals(month)){
			querySql += " and plan0102='"+month+"'";
		}
		
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				eo.add("id", map.get("RECORDID"));
				eo.add("PLAN1202", map.get("PLAN1202"));
				eo.add("PLAN1203", map.get("PLAN1203"));
				eo.add("PLAN1221",  map.get("PLAN1221"));
				eo.add("PLAN1222", map.get("PLAN1222"));
				eo.add("PLAN1223", map.get("PLAN1223"));
				eo.add("PLAN1224",  map.get("PLAN1224"));
				eo.add("PLAN1225",  map.get("PLAN1225"));
				eo.add("PLAN1226",  map.get("PLAN1226"));
				eo.add("PLAN1227",  map.get("PLAN1227"));
				
				eoc.add(eo);
			}
		}
		return eoc.toString();	
		
	}
	
	/**
	 * 保存数据
	 */
	@Action
	public String saveGridData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			String faid = jsonObject.get("id").toString();
			String whereSql=" RECORDID='"+faid+"'";
			Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);
 
			//IRecord ire = this.recordDao.getRecord("PLAN12", UUID.fromString(faid));
			if (ires.size()>0){
				IRecord ire =ires.get(0);
				
				ire.put("PLAN1221", jsonObject.get("PLAN1221"));
				ire.put("PLAN1222", jsonObject.get("PLAN1222"));
				ire.put("PLAN1223", jsonObject.get("PLAN1223"));
				ire.put("PLAN1224", jsonObject.get("PLAN1224"));
				ire.put("PLAN1225", jsonObject.get("PLAN1225"));
				ire.put("PLAN1226", jsonObject.get("PLAN1226"));
				ire.put("PLAN1227", jsonObject.get("PLAN1227"));
	
				//ire.put("PLAN1210", "保存");
				
				this.recordDao.saveObject(ire);		
			}	
		}	
		return "success";
	}
	/**
	 * 提交数据，提交后不能修改
	 * 
	 */
	@Action
	public String commitGridData(){
		
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			String faid = jsonObject.get("id").toString();

			String whereSql=" RECORDID='"+faid+"'";
			Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);
 
			//IRecord ire = this.recordDao.getRecord("PLAN12", UUID.fromString(faid));
			if (ires.size()>0){
				IRecord ire =ires.get(0);
				ire.put("PLAN1210", "提交");
				this.recordDao.saveObject(ire);
			}
		}
		return "success";		

	}
}
