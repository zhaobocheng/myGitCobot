package com.bop.web.ssj.ssjscheme;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.bop.web.ssj.taskmanage.TaskOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class SchemeResult {
	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private UserSession userSession;

	private static final Logger log = LoggerFactory.getLogger(TaskOperation.class);
	
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
		if(zone==null||"".equals(zone)){
			zone = ActionContext.getActionContext().getHttpServletRequest().getParameter("zone");
		}
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
	public String getSchemeAndResult(String fzid,String qxid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'  and PLAN1204 = '"+qxid+"'";
			zone = qxid;
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
		}	
		//List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);
		Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList("select * from plan12 where "+whereSql);
		
		if(ires.size()>0){
			for(Map<String,Object> ire:list){
				ExtObject eo = new ExtObject();

				eo.add("id", ire.get("recordid"));
				eo.add("PLAN1202", ire.get("PLAN1202"));
				eo.add("PLAN1203", ire.get("PLAN1203"));
				eo.add("PLAN1221",  ire.get("PLAN1221"));
				//eo.add("PLAN1222",  ire.get("PLAN1222")==null?"":ire.get("PLAN1222",DmCodetables.class).getId());
				eo.add("PLAN1222",  ire.get("PLAN1222"));
				eo.add("PLAN1223", ire.get("PLAN1223"));
				eo.add("PLAN1224",  ire.get("PLAN1224"));
				eo.add("PLAN1225",  ire.get("PLAN1225"));
				eo.add("PLAN1226",  ire.get("PLAN1226"));
				eo.add("PLAN1227",  ire.get("PLAN1227"));
				String personInf[] = this.getJCRData(UUID.fromString(ire.get("recordid").toString()),"yx");			
				eo.add("dq", ire.get("PLAN1204"));
				eo.add("jgdm", ire.get("PLAN1202"));
				eo.add("dwmc", ire.get("PLAN1203"));
				eo.add("dz",  ire.get("PLAN1205"));
				eo.add("lxr", ire.get("PLAN1206"));
				eo.add("phone", ire.get("PLAN1207"));
				eo.add("jcnr",  ire.get("PLAN1208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);
				eo.add("sjly", this.getJSLY(fzid,zone,ire.get("PLAN1202").toString()));
				eo.add("ParentDqId", ire.get("PLAN1204"));
				eo.add("dqid",  null);

				eoc.add(eo);
				String planId=ire.get("PLAN1201").toString();
				String ZXJC=this.getZXJC(fzid,planId);
				
				eo.add("zxjc",ZXJC);
				 
			}
		}
		return eoc.toString();		
	}
	
	private String getZXJC(String fzid, String planId) {
		List<Map<String,Object>> SPList = this.jdbcTemplate.queryForList("select  vp.SP0101 from v_ssj_sp vp where  vp.PLAN00='"+fzid+"' and vp.sp0201 ='"+planId+"'");
		String zxjc=null;
		
		if(SPList.size()>0){
			for(Map<String,Object> map:SPList){
				zxjc=map.get("SP0101").toString();
			}
		}
		return zxjc;
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
			whereSql = "parentid = '"+fzid+"' and FQ0204 = '"+zone+"'";
		}

		Records ires  = this.recordDao.queryRecord("FQ02", whereSql);

		if(ires.size()>0){
			for(IRecord ire:ires){
				ExtObject eo = new ExtObject();
				String personInf[] = this.getJCRData(ire.getRecordId(),"fq");

				eo.add("id", ire.getRecordId());
				eo.add("dq", ire.get("FQ0204",DmCodetables.class).getCaption());
				eo.add("jgdm", ire.get("FQ0202"));
				eo.add("dwmc", ire.get("FQ0203"));
				eo.add("dz",  ire.get("FQ0205"));
				eo.add("lxr", ire.get("FQ0206"));
				eo.add("phone", ire.get("FQ0207"));
				eo.add("jcnr",  ire.get("FQ0208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);
				
				eo.add("sjly", this.getJSLY(ire.get("FQ0201",IRecord.class).getRecordId().toString()));
				eo.add("ParentDqId", ire.get("FQ0204",DmCodetables.class).getId());
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
	private String [] getJCRData(UUID plan12id,String flag){
		String ids = "";
		String names = "";
		String inform[] = new String[2];
		
		List<IRecord> ires = null;
		
		if("fq".equals(flag)){
			ires = this.recordDao.getByParentId("FQ03", plan12id);
			for(IRecord ire:ires){
				names+= ire.get("FQ0302").toString()+",";
				ids+=ire.getRecordId().toString()+",";
			}
		}else{
			ires = this.recordDao.getByParentId("PLAN1201", plan12id);
			for(IRecord ire:ires){
				names+= ire.get("PLAN120102").toString()+",";
				ids+=ire.getRecordId().toString()+",";
			}
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
		
		String sql = " select * from plan0401 t  where t.parentid = '"+jgdm+"'";
		List<Map<String,Object>> plan0401List = this.jdbcTemplate.queryForList(sql);
		
		String retStr = "";
		if(plan0401List.size()>0){
			for(Map<String,Object> map:plan0401List){
				retStr += map.get("PLAN040103")+",";
			}
			return  retStr.substring(0, retStr.length()-1);
		}else{
			return "";
		}
	}

	
	private String getJSLY(String fzid,String zone,String jgdm){
		
		String sql = " select * from plan0401 t  where t.parentid in (select recordid from plan04 where  parentid = '"+fzid+"' and plan0404 = '"+zone+"' and plan0402 = '"+jgdm+"')";
		List<Map<String,Object>> plan0401List = this.jdbcTemplate.queryForList(sql);
		
		String retStr = "";
		if(plan0401List.size()>0){
			for(Map<String,Object> map:plan0401List){
				retStr += map.get("PLAN040103")+",";
			}
			return  retStr.substring(0, retStr.length()-1);
		}else{
			return "";
		}
		
	}
	
	
	/**
	 * 获取方案浏览列表数据
	 * @param 年份
	 * @return
	 */
	@Action
	public String getFALBData(){
		ExtObjectCollection eoc = new ExtObjectCollection();
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String zfnd = request.getParameter("zfnd")==null?null:request.getParameter("zfnd").toString();
		String rwmc = request.getParameter("rwmc")==null?null:request.getParameter("rwmc").toString();

		String sql = " select  t.plan00,tt.recordid,tt.parentid,to_char(tt.plan0303,'yyyy-MM-dd') as tjsj,t.plan0102 as yf,t.plan0107 as mc,aa.caption as qx,tt.PLAN0301 as qxid,t.plan0101||t.plan0102 as zfyf,counorg.qys as cycczs , case when p6.plan0602 is null then 0  else p6.plan0602  end as ccqys , "+
					 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs,fqs as fqfas,case when tt.plan0302 <5 then '否' else '是' end as cz, to_char(ppp.gss) as sfgs from plan01 t  "+
					 " inner join plan03 tt on tt.parentid = t.plan00 "+
					" left join dm_codetable_data aa on aa.cid=tt.plan0301 and aa.codetablename='DB064' " +
					 " left join (select count(*) qys,org.PLAN0404,org.parentid from plan04 org group by org.parentid,org.PLAN0404 ) counorg on counorg.PLAN0404 = tt.plan0301 and t.plan00 = counorg.parentid "+
					" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
					" left join (select count(*) as fqs, pp.fq0105, pp.fq0103  from fq01 pp  group by pp.fq0105, pp.fq0103) countorg on countorg.fq0105=t.plan00 and countorg.fq0103=tt.plan0301 " + 
					" left join (select sum(decode(p12.plan1210,5,1,0)) as gss,p12.parentid,p12.plan1204 from plan12 p12 group by p12.parentid ,p12.plan1204) ppp on ppp.parentid = t.plan00 and ppp.plan1204 = tt.plan0301"+
					" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301 where ";

		String sql1="select plan00,plan00 as recordid,null as parentid,null as tjsj,plan0102 as yf,plan0107 as mc,'' as qx,null as qxid,plan0101||plan0102 as zfyf,null as cycczs , null as ccqys , null as zfryzs,  null as cyzfrs,null as fqfas,'' as cz,'' as sfgs from plan01 where plan0105 = 1   ";

		String wheresql = " t.plan0105 = 1 ";
		String zone=this.userSession.getCurrentUserZone();
		if(zone!=null&&!"".equals(zone)){
			wheresql += "and tt.plan0301='"+zone+"' ";
		}
		if(zfnd!=null&&!"".equals(zfnd)){
			wheresql += " and t.plan0101 = "+zfnd;
			sql1+=" and plan0101="+zfnd;
		}
		if  (null!=rwmc &&!"".equals(rwmc)){
			wheresql += " and plan0107 like '%"+rwmc+"%'";
			sql1+= " and plan0107 like '%"+rwmc+"%'";
		}

		//List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql+wheresql+" order by t.plan0102 desc");
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql+wheresql+" UNION ALL "+sql1+" order by yf desc,qxid ");
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				UUID uid = UUID.randomUUID();
				eo.add("id", map.get("recordid"));
				eo.add("zfyf", map.get("zfyf"));
				eo.add("yf", map.get("yf"));
				eo.add("qx", map.get("qx"));
				eo.add("qxid", map.get("qxid"));
				eo.add("parentid", map.get("parentid"));
				if (map.get("parentid")==null){
					eo.add("mc", map.get("mc"));
				} else{
					eo.add("mc","<a  id = \"mc\"   Style=\"color:black;\" onclick=\"showSchemeView()\">"+map.get("mc")+"</a>");
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
				eo.add("cz", map.get("cz"));
				eo.add("tjsj", map.get("tjsj"));

				eo.add("sfgs", map.get("sfgs"));
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
		String qxdm=ActionContext.getActionContext().getHttpServletRequest().getParameter("zone");
		String zone = this.userSession.getCurrentUserZone();
		String sql = "select a.fq00,a.fq0104+1 as cs from fq01 a "+
					" where a.fq0105='"+faid+"' ";
		
		if(null==zone || "".equals(zone)){
			sql +=" and  a.fq0103='"+qxdm+"'";
		}else{
			sql +=" and  a.fq0103='"+zone+"'";
		}

		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql+" order by fq0104 ");
		
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("id", map.get("fq00"));
			eo.add("mc", "第 "+map.get("cs")+" 次");
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
		querySql = "select t.item00 as cid,t.ITEM0101 as caption from item01 t  ";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();	
				eo.add("id", map.get("cid"));
				//eo.add("id", map.get("cid"));
				eo.add("text", map.get("caption"));
			//	eo.add("value", map.get("cid"));
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
		String year = request.getParameter("year")==null?null:request.getParameter("year").toString();

		String zone = this.userSession.getCurrentUserZone();
		String querySql = "select * from plan01,plan12 where plan01.plan00=plan12.parentid and plan1210<='5'";

		if(null!=zone&&!"".equals(zone)){
			querySql += " and plan1204='"+zone+"'";
		}
		if  (null!=month &&!"".equals(month)){
			querySql += " and plan0102='"+month+"'";
		}
		if  (null!=year &&!"".equals(year)){
			querySql += " and plan0101='"+year+"'";
		}
		if  (null!=rwmc &&!"".equals(rwmc)){
			querySql += " and plan0107 like '%"+rwmc+"%'";
		}
		
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				eo.add("id", map.get("RECORDID"));
				eo.add("parentid", map.get("parentid"));				
				eo.add("PLAN1202", map.get("PLAN1202"));
				eo.add("PLAN1203", map.get("PLAN1203"));
				eo.add("PLAN1221",  map.get("PLAN1221"));
				eo.add("PLAN1222", map.get("PLAN1222"));
				eo.add("PLAN1223", map.get("PLAN1223"));
				eo.add("PLAN1224",  map.get("PLAN1224"));
				eo.add("PLAN1225",  map.get("PLAN1225"));
				eo.add("PLAN1226",  map.get("PLAN1226"));
				eo.add("PLAN1227",  map.get("PLAN1227"));
				eo.add("PLAN1210", map.get("PLAN1210"));
				eo.add("PLAN1229", map.get("PLAN1229"));
				
				 if("3".equals(map.get("PLAN1210").toString())){
					 eo.add("zf","已保存");
				 }else if("4".equals(map.get("PLAN1210").toString())){
					 eo.add("zf","已提交");
				 }else if("5".equals(map.get("PLAN1210").toString())){
					 eo.add("zf","已公示");
				 }else{
					 eo.add("zf","未保存");
				 }
				eoc.add(eo);
			}
		}
		return eoc.toString();	
		
	}
	/**
	 * 保存提交的公示数据
	 * @param 
	 */
	@Action
	public String commitGSData(String faid){
		String zone = this.userSession.getCurrentUserZone();
		String data = ActionContext.getActionContext().getHttpServletRequest().getParameter("data");
		JSONArray oa = JSONArray.fromObject(data);
		
		for(int i=0;i<oa.size();i++){
			JSONObject jo = oa.getJSONObject(i);
			String upsql = "update plan12 t set t.plan1210=5,t.plan1228=sysdate where t.plan1210 = 4 and t.recordid='"+jo.getString("id")+"'";
			this.jdbcTemplate.execute(upsql);
		}

		String querysql = " select case when zs=tjs then 0 else 1 end as zt from( select count(*) as zs,sum(decode(t.plan1210,3,1,0)) as tjs"+
						  " from  plan12 t where t.plan1204 = '"+zone+"' and t.parentid = '"+faid+"')";

		int zt = this.jdbcTemplate.queryForInt(querysql);
		if(zt==1){
			//没有公示完还不能更新plan0302的状态
		}else{
			String whereSql=" PARENTID ='"+faid+"' and plan0301='"+zone+"'";
			Records ires  = this.recordDao.queryRecord("PLAN03", whereSql);
			if (ires.size()>0){
				IRecord ire =ires.get(0);
				ire.put("PLAN0302", "6");
				this.recordDao.saveObject(ire);
			}
		}
		return "success";
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
				ire.put("PLAN1210", 3);
				ire.put("PLAN1229", jsonObject.get("PLAN1229").toString());
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
		ExtResultObject eor = new ExtResultObject();
		String backStr = "";
		
		eor.add("flag",true);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			
			String p1221 = jsonObject.get("PLAN1221").toString();
			String p1224 = jsonObject.get("PLAN1224").toString();
			String p12id = jsonObject.get("id").toString();

			String whereSql=" RECORDID='"+p12id+"'";
			Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);
			if (ires.size()>0){
				if(!"".equals(p1221) && !"".equals(p1224)){
					if("5".equals(jsonObject.get("PLAN1210"))){
						
					}else{
						IRecord ire =ires.get(0);
						ire.put("PLAN1210", "4");
						this.recordDao.saveObject(ire);
					}
				}else{
					backStr+=jsonObject.get("_id")+",";
					eor.add("flag",false);
				}
			}
		}

		if(backStr.equals("")){
			eor.add("info","方案已提交");
		}else{
			eor.add("info",backStr.substring(0, backStr.length()-1));
		}
		return eor.toString();
	}

	@Action
	public String getGSstatus(String rwmc,String year,String month){
		String zone=this.userSession.getCurrentUserZone();
		ExtResultObject eor=new ExtResultObject();

		String wheresql = " 1=1 ";
		if  (null!=rwmc &&!"".equals(rwmc)){
			wheresql += " and plan0107 like '%"+rwmc+"%'";
		}
		if  (null!=month &&!"".equals(month)){
			wheresql += " and plan0102='"+month+"'";
		}
		if  (null!=year &&!"".equals(year)){
			wheresql += " and plan0101='"+year+"'";
		}
		 
		IRecord p1 = this.recordDao.queryTopOneRecord("plan01", wheresql, "plan0107");

		if(p1.get("PLAN00")==null){
			eor.add("flag", false);
		}else{
			String faid = p1.get("PLAN00").toString();
			IRecord plan3 = this.recordDao.queryTopOneRecord("PLAN03", "parentid='"+faid+"' and plan0301='"+zone+"' and plan0302 >4 ", "pindex");//判断这个任务是否提交
			
			if(plan3==null){
				eor.add("flag", "unconmmit");
			}else{
				IRecord plan6 = this.recordDao.queryTopOneRecord("PLAN06", "parentid='"+faid+"' and plan0601='"+zone+"'", "pindex");
				int p12 = this.jdbcTemplate.queryForInt("select count(*) from plan12 t where t.parentid = '"+faid+"' and t.PLAN1204 = '"+zone+"' and plan1210=5");
				
				if(p12==plan6.get("PLAN0602",Integer.class)){
					eor.add("flag", "false");
				}else{
					eor.add("flag", "true");
				}
			}
		}
		return eor.toString();
	}

	
	/**
	 * 得到下拉框数据
	 * @return
	 */
	@Action
	public String getYData(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.plan0101 nd from plan01 t");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("nd"));
			eo.add("text", ire.get("nd"));
			eoc.add(eo);
		}
		return eoc.toString();
	}

	
	

	/**
	 * 升级旧数据
	 * @return
	 */
	@Action
	public String chengeAll(){
		
		String sql = "select recordid,plan1222 from plan12 where plan1222 is not null ";
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		for(Map<String,Object> pir:list){
			List<Map<String,Object>>  listMap  = this.jdbcTemplate.queryForList("select column_value from table(strsplit("+pir.get("plan1222")+"))  order by column_value ");
			String recordid = pir.get("recordid").toString();
			log.error("开始导入数据，导入的plan12 recordid是"+recordid);
			String insertSql = "";

			for(Map<String,Object> map:listMap){
				String str = map.get("column_value").toString();
				switch(str){
					case "1":
						insertSql += "bd0c1da9-251a-4c93-8f32-ee02e3988cb8,";
						break;
					case "2":
						insertSql += "ed700f4d-a28e-42bf-a91f-e1716a3691bb,";
						break;
					case "3":
						insertSql += "d341ba8b-0063-4979-bbac-013035e3b1b4,";
						break;
					case "4":
						insertSql += "adca2274-5579-4718-ae90-5589e662bb7c,";
						break;
					case "5":
						insertSql += "15bf5655-53cb-4b0a-83ad-0babf8c2eb35,";
						break;
					default:
						insertSql += "";
						break;
				} 
			}
			
			this.jdbcTemplate.execute("update  plan12 t set t.plan1222 = '"+insertSql.substring(0,insertSql.length()-1)+"' where t.recordid = '"+recordid+"'");
			log.error("导入数据结束，导入的plan12 recordid是"+recordid);
		}
		return "success";
	}
	
	
	
	
	
	/**
	 * 得到下拉框数据
	 * @return
	 */
	@Action
	public String getMData(String year){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.plan0102 yf from plan01 t where t.plan0101 = "+year +" order by plan0102 desc");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("yf"));
			eo.add("text", ire.get("yf"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	
	
}
