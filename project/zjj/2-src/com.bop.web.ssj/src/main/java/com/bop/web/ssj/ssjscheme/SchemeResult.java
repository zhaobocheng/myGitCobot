package com.bop.web.ssj.ssjscheme;

import java.text.SimpleDateFormat;
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
	 * 获取方案浏览列表数据
	 * @param 年份
	 * @return
	 */
	@Action
	public String getFALBData(String zfnd){ 

		ExtObjectCollection eoc = new ExtObjectCollection();
		String sql = "select  t.plan00,tt.recordid,tt.parentid,t.plan0101 as nf,t.plan0107 as mc,aa.caption as qx,t.plan0101||t.plan0102 as zfyf,counorg.qys as cycczs , case when p6.plan0602 is null then round(counorg.qys*0.01)  else p6.plan0602  end as ccqys , "+
					 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs,fqs as fqfas,case when tt.plan0302 ='5' then '是' else '否' end as cz from plan01 t  "+
					 " inner join plan03 tt on tt.parentid = t.plan00 "+				   
					" left join dm_codetable_data aa on aa.cid=tt.plan0301 and aa.codetablename='DB064' " +
					 " left join (select count(*) qys,org.reg_district_dic from org01 org group by org.reg_district_dic) counorg on counorg.reg_district_dic = tt.plan0301"+
					" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
					" left join (select count(*) fqs , p4.plan2103,p4.parentid from plan21 p4 group by p4.plan2103 ,p4.parentid) countorg on countorg.parentid=t.plan00 and countorg.plan2103=tt.plan0301 " + 
					" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301 where ";
		
		String sql1="select plan00,plan00 as recordid,null as parentid,plan0101 as nf,plan0107 as mc,'' as qx,plan0101||plan0102 as zfyf,null as cycczs , null as ccqys , null as zfryzs,  null as cyzfrs,null as fqfas,'' as cz from plan01 where plan0105 = 1  ";
		
		String wheresql = " t.plan0105 = 1 ";
		String zone=this.userSession.getCurrentUserZone();
		if(zone!=null&&!"".equals(zone)){
			wheresql += "and tt.plan0301='"+zone+"' ";
		}
		if(zfnd!=null&&!"".equals(zfnd)){
			wheresql += " and t.plan0101 = "+zfnd;
			sql1+=" and plan0101="+zfnd;
		}
		
		//List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql+wheresql+" order by t.plan0102 desc");
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql+wheresql+" UNION ALL "+sql1+" order by zfyf,parentid desc");
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				UUID uid = UUID.randomUUID();
				eo.add("id", map.get("recordid"));
				eo.add("mc", map.get("mc"));
				eo.add("nf", map.get("nf"));
				eo.add("qx", map.get("qx"));
				eo.add("parentid", map.get("parentid"));
				eo.add("zfyf", map.get("zfyf"));
				if (map.get("zfryzs")==null){
					eo.add("zfryzs","");
				} else {
					eo.add("zfryzs",  "<a  id = \"zs\"   Style=\"color:black;\" onclick=\"showRy('zs')\">"+map.get("zfryzs")+"</a>");					
				}
				if (map.get("cyzfrs")==null){
					eo.add("cyzfrs","");
				}else {
					eo.add("cyzfrs", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showRy('zf')\">"+map.get("cyzfrs")+"</a>");					
				}
				if (map.get("cycczs")==null){
					eo.add("cycczs","");
				}else {				
					eo.add("cycczs",  "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showOrg('zs')\">"+map.get("cycczs")+"</a>");
				}
				if (map.get("ccqys")==null){
					eo.add("ccqys","");
				}else {	
					eo.add("ccqys", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showOrg('zf')\">"+map.get("ccqys")+"</a>");
				}
				eo.add("zffa", map.get("zffa"));
				eo.add("wtjfas", map.get("wtjfas"));
				eo.add("cz", map.get("cz"));//是否提交过
				if (map.get("fqfas")==null){
					eo.add("fqfas","");
				}else {				
					eo.add("fqfas", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showFQScheme('fq')\">"+map.get("fqfas")+"</a>");//废弃方案数
				}
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}

	/**
	 * 浏览方案时，点击数字下钻，显示废弃方案列表
	 * @param 
	 * @return
	 */
	@Action
	public String getFQScheme(String faid){
		ExtObjectCollection ero = new ExtObjectCollection();
		String flag=ActionContext.getActionContext().getHttpServletRequest().getParameter("flag");
		String zone = this.userSession.getCurrentUserZone();
		String sql = "select a.plan00,t.plan0101,plan0102,t.plan0107,a.plan2101 from plan03 b "+
					" inner join  plan21 a on a.parentid=b.parentid "+
					" inner join plan01 t on b.parentid=t.plan00 "+
					" where b.recordid='"+faid+"' ";
		
		if(null!=zone&&!"".equals(zone)){
			sql +=" and  a.plan2103='"+zone+"'";
		} 

		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("id", map.get("plan00"));
			eo.add("mc", map.get("plan0107"));
			eo.add("nd", map.get("plan0101"));
			eo.add("yf", map.get("plan0102"));
			eo.add("yy", map.get("plan2101"));
			ero.add(eo);
		}
		return ero.toString();		
		
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
