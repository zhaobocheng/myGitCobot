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
import com.bop.json.ExtResultObject;
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
	 * 方案浏览时获取列表头部加载信息
	 * @param faid
	 * @return
	 */
	@Action
	public String getViewBaseInfo(String faid){
		ExtResultObject ero = new ExtResultObject();
		String zone = this.userSession.getCurrentUserZone();
		String sql = " select t.plan00,t.plan0107 as mc ,t.plan0102 as yf, count(p2.recordid) as rs ,p6.plan0602 qys"+
		" from plan03 tt left join plan01 t on tt.parentid=t.plan00 left join plan02 p2 on p2.parentid = t.plan00 and p2.plan0205 = '"+zone+"' and p2.plan0204 = 2 "+
		" left join plan06 p6 on p6.parentid = t.plan00 and p6.plan0601 = '"+zone+"' where tt.recordid = '"+faid+"' group by t.plan00,t.plan0107,t.plan0102,p6.plan0602";
		
		Map<String,Object> map = this.jdbcTemplate.queryForMap(sql);
		ero.add("id", map.get("plan00"));
		ero.add("mc", map.get("mc"));
		ero.add("yf", map.get("yf"));
		ero.add("rs", map.get("rs"));
		ero.add("qys", map.get("qys"));
		return ero.toString();
	}	
	/**
	 * 浏览方案和执法结果信息
	 * @param faid
	 * @return
	 */
	@Action
	public String getSchemeAndResult(String fzid){
		ExtObjectCollection eoc = new ExtObjectCollection();
		//HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();

		String zone = this.userSession.getCurrentUserZone();
		
	//	String querySql = "select * from plan01,plan12 where plan01.plan00=plan12.parentid ";
		String whereSql = null;	
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
		}		
		//List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);
		Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);

		if(ires.size()>0){
			for(IRecord ire:ires){
				ExtObject eo = new ExtObject();

				eo.add("id", ire.getRecordId());
				eo.add("PLAN1202", ire.get("PLAN1202"));
				eo.add("PLAN1203", ire.get("PLAN1203"));
				eo.add("PLAN1221",  ire.get("PLAN1221"));
				eo.add("PLAN1222", ire.get("PLAN1222"));
				eo.add("PLAN1223", ire.get("PLAN1223"));
				eo.add("PLAN1224",  ire.get("PLAN1224"));
				eo.add("PLAN1225",  ire.get("PLAN1225"));
				eo.add("PLAN1226",  ire.get("PLAN1226"));
				eo.add("PLAN1227",  ire.get("PLAN1227"));
				String personInf[] = this.getJCRData(ire.getRecordId());				
				eo.add("dq", ire.get("PLAN1204",DmCodetables.class).getCaption());
				eo.add("jgdm", ire.get("PLAN1202"));
				eo.add("dwmc", ire.get("PLAN1203"));
				eo.add("dz",  ire.get("PLAN1205"));
				eo.add("lxr", ire.get("PLAN1206"));
				eo.add("phone", ire.get("PLAN1207"));
				eo.add("jcnr",  ire.get("PLAN1208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);			
				eo.add("sjly", this.getJSLY(ire.get("PLAN1202").toString()));
				eo.add("ParentDqId", ire.get("PLAN1204",DmCodetables.class).getId());
				eo.add("dqid",  null);
				
				eoc.add(eo);
			}
		}
		return eoc.toString();		
	}
	
	/**
	 * 获取某次废弃执法方案包含的企业
	 * @param sccs
	 * @return
	 */
	@Action
	public String getFQCYQYData(String fzid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN2204 = '"+zone+"'";
		}

		Records ires  = this.recordDao.queryRecord("PLAN22", whereSql);

		if(ires.size()>0){
			for(IRecord ire:ires){
				ExtObject eo = new ExtObject();
				String personInf[] = this.getJCRData(ire.getRecordId());

				eo.add("id", ire.getRecordId());
				eo.add("dq", ire.get("PLAN2204",DmCodetables.class).getCaption());
				eo.add("jgdm", ire.get("PLAN2202"));
				eo.add("dwmc", ire.get("PLAN2203"));
				eo.add("dz",  ire.get("PLAN2205"));
				eo.add("lxr", ire.get("PLAN2206"));
				eo.add("phone", ire.get("PLAN2207"));
				eo.add("jcnr",  ire.get("PLAN2208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);
				
				eo.add("sjly", this.getJSLY(ire.get("PLAN2202").toString()));
				eo.add("ParentDqId", ire.get("PLAN2204",DmCodetables.class).getId());
				eo.add("dqid",  null);
				eoc.add(eo);
			}
		}
		return eoc.toString();	
	}
	/**
	 * 页面加载的时候得到检查人的信息
	 * @param plan12id
	 * @return
	 */
	private String [] getJCRData(UUID plan12id){
		String ids = "";
		String names = "";
		String inform[] = new String[2];
		
		List<IRecord> ires = this.recordDao.getByParentId("PLAN1201", plan12id);
		for(IRecord ire:ires){
			names+= ire.get("PLAN120102").toString()+",";
			ids+=ire.getRecordId().toString()+",";
		}

		if(ids.length()>0){
			inform[0] = ids.substring(0, ids.length()-1);
			inform[1] = names.substring(0, names.length()-1);
		}else{
			inform[0] = "无";
			inform[1] = "无";
		}
		return inform;
	}
	/**
	 * 得到涉及领域
	 * @param jgdm
	 * @return
	 */	
	private String getJSLY(String jgdm){
		String sql = "select * from org02 where  parentid =(select org01.org00 from org01 where org01.org_code = '"+jgdm+"')";
		List<Map<String,Object>> org2List = this.jdbcTemplate.queryForList(sql);
		
		String retStr = "";
		if(org2List.size()>0){
			Map<String,Object> map = org2List.get(0);
			if(map.get("ORG0201")!=null&&"1".equals(map.get("ORG0201").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '1'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0202")!=null&&"1".equals(map.get("ORG0202").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '2'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0203")!=null&&"1".equals(map.get("ORG0203").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '3'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0204")!=null&&"1".equals(map.get("ORG0204").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '4'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0205")!=null&&"1".equals(map.get("ORG0205").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '5'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
		}
		return retStr.substring(0, retStr.length()-1);
	}	
	/**
	 * 获取方案浏览列表数据
	 * @param 年份
	 * @return
	 */
	@Action
	public String getFALBData(String zfnd){ 

		ExtObjectCollection eoc = new ExtObjectCollection();
		String sql = "select  t.plan00,tt.recordid,tt.parentid,t.plan0102 as yf,t.plan0107 as mc,aa.caption as qx,t.plan0101||t.plan0102 as zfyf,counorg.qys as cycczs , case when p6.plan0602 is null then round(counorg.qys*0.01)  else p6.plan0602  end as ccqys , "+
					 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs,fqs as fqfas,case when tt.plan0302 ='5' then '是' else '否' end as cz from plan01 t  "+
					 " inner join plan03 tt on tt.parentid = t.plan00 "+				   
					" left join dm_codetable_data aa on aa.cid=tt.plan0301 and aa.codetablename='DB064' " +
					 " left join (select count(*) qys,org.reg_district_dic from org01 org group by org.reg_district_dic) counorg on counorg.reg_district_dic = tt.plan0301"+
					" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
					" left join (select count(*) fqs , p4.plan2103,p4.parentid from plan21 p4 group by p4.plan2103 ,p4.parentid) countorg on countorg.parentid=t.plan00 and countorg.plan2103=tt.plan0301 " + 
					" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301 where ";
		
		String sql1="select plan00,plan00 as recordid,null as parentid,plan0102 as yf,plan0107 as mc,'' as qx,plan0101||plan0102 as zfyf,null as cycczs , null as ccqys , null as zfryzs,  null as cyzfrs,null as fqfas,'' as cz from plan01 where plan0105 = 1  ";
		
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
				eo.add("yf", map.get("yf"));
				eo.add("qx", map.get("qx"));
				eo.add("parentid", map.get("parentid"));
				if (map.get("parentid")==null){
					eo.add("zfyf", map.get("zfyf"));
				} else{
					eo.add("zfyf","<a  id = \"zfyf\"   Style=\"color:black;\" onclick=\"showSchemeView()\">"+map.get("zfyf")+"</a>");
				}

				
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
		String sql = "select a.recordid,a.plan2104 from plan03 b "+
					" inner join  plan21 a on a.parentid=b.parentid "+
					" where b.recordid='"+faid+"' ";
		
		if(null!=zone&&!"".equals(zone)){
			sql +=" and  a.plan2103='"+zone+"'";
		} 

		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("id", map.get("recordid"));
			eo.add("mc", "第 "+map.get("plan0107")+" 次");
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
		
		String querySql = "select * from plan01,plan12 where plan01.plan00=plan12.parentid and plan1210='2'";


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
				ire.put("PLAN1210", "3");
				this.recordDao.saveObject(ire);
			}
		}
		return "success";		

	}
}
