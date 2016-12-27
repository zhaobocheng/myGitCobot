package com.bop.web.ssj.ssjscheme;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.bop.web.rest.renderer.Renderer;
import com.bop.web.rest.renderer.TemplateRenderer;

@Controller
public class SchemeInfoShow {
	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private UserSession userSession;
	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	/**
	 * 得到方案列表数据
	 * @return
	 */
	@Action
	public String getFALBData(String zfnd,String zfzt){

		ExtObjectCollection eoc = new ExtObjectCollection();
		String sql = "select  t.plan00,t.plan0107 as mc,t.plan0101||t.plan0102 as zfyf, decode(counorg.qys,null,0,counorg.qys) as cycczs , case when p6.plan0602 is null then round(counorg.qys*0.01)  else p6.plan0602  end as ccqys ,  "+
					 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs , decode(p21.fqs,null,0,p21.fqs) as fqs,tt.plan0302 from plan01 t  left join plan03 tt on tt.parentid = t.plan00 "+
					" left join (select count(*) qys,org.parentid, org.plan0404  from plan04 org group by org.parentid,org.plan0404) counorg on counorg.plan0404 =  tt.plan0301 and tt.parentid = counorg.parentid"+
					" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
					" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301    left join (select count(*) as fqs, pp.fq0105, pp.fq0103 from fq01 pp group by pp.fq0105, pp.fq0103) p21 on p21.fq0105 = t.plan00 and p21.fq0103 = tt.plan0301 where ";
		String wheresql = " t.PLAN0105 = 1 and tt.plan0301='"+this.userSession.getCurrentUserZone()+"' ";

		if(zfnd!=null&&!"".equals(zfnd)){
			wheresql += " and t.plan0101 = "+zfnd;
		}
		if(zfzt!=null&&!"".equals(zfzt)){
			if("0".equals(zfzt)){
				wheresql += " and tt.plan0302 <4 ";
			}else{
				wheresql += " and tt.plan0302 >= 4";
			}
		}

		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql+wheresql+" order by t.plan0102 desc");
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				UUID uid = UUID.randomUUID();
				eo.add("id", map.get("plan00"));
				eo.add("mc", map.get("mc"));
				eo.add("zfyf", map.get("zfyf"));
				eo.add("zfryzs", "<a  id = \"zs\"   Style=\"color:black;\" onclick=\"showRy('zs')\">"+map.get("zfryzs")+"</a>");
				eo.add("cyzfrs", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showRy('zf')\">"+map.get("cyzfrs")+"</a>");
				eo.add("cycczs", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showOrg('zs')\">"+map.get("cycczs")+"</a>");
				eo.add("ccqys", "<a  id = \"zfrs\" Style=\"color:black;\" onclick=\"showOrg('zf')\">"+map.get("ccqys")+"</a>");
				eo.add("zffa", map.get("zffa"));
				eo.add("wtjfas", "<a  id = \"wtjfas\" Style=\"color:black;\" onclick=\"showFQScheme()\">"+map.get("fqs")+"</a>");
				eo.add("cz", "");//是否提交过
				
				int zt = Integer.parseInt(map.get("plan0302").toString());
				if(zt>3){
					eo.add("zffa",  " <a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\" style=\"width: 80%;height:100%\" onclick=\"viewFa()\">浏览执法方案</a>");
				}else{
					eo.add("zffa", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\" style=\"width: 80%;height:100%\" onclick=\"createFa()\">生成执法方案</a>");
				}
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}
	

	/**
	 * 浏览生成方案
	 */
	@Action
	public String getSchemeDate(String fzid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
		}
		Records ires  = this.recordDao.queryRecord("PLAN12", whereSql);

		if(ires.size()>0){
			for(IRecord ire:ires){
				ExtObject eo = new ExtObject();
				String personInf[] = this.getJCRData(ire.getRecordId());

				eo.add("id", ire.getRecordId());
				eo.add("dq", ire.get("PLAN1204",DmCodetables.class).getCaption());
				eo.add("jgdm", ire.get("PLAN1202"));
				eo.add("dwmc", ire.get("PLAN1203"));
				eo.add("dz",  ire.get("PLAN1205"));
				eo.add("lxr", ire.get("PLAN1206"));
				eo.add("phone", ire.get("PLAN1207"));
				eo.add("jcnr",  ire.get("PLAN1208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);

				eo.add("sjly", this.getJSLY(fzid,zone,ire.get("PLAN1202").toString()));
				eo.add("ParentDqId", ire.get("PLAN1204",DmCodetables.class).getId());
				eo.add("dqid",  null);
				String planId=ire.get("PLAN1201",IRecord.class).getObjectId().toString();
				String ZXJC=this.getZXJC(fzid,planId);
				eo.add("zxjc",ZXJC);
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}

	
	private String getZXJC(String fzid,String  planId) {
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
	
	//得到涉及领域
		private String getJSLY(String fzid,String zone,String jgdm){
			/*String sql = "select * from plan04 where  parentid = '"+fzid+"' and plan0404 = '"+zone+"' and plan0402 = '"+jgdm+"'";
			List<Map<String,Object>> org2List = this.jdbcTemplate.queryForList(sql);*/
			/*String retStr = "";
			if(org2List.size()>0){
				Map<String,Object> map = org2List.get(0);
				if(map.get("plan0405")!=null&&"1".equals(map.get("plan0405").toString())){
					String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '1'";
					Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
					retStr+=codeMap.get("caption")+"，";
				}
				if(map.get("plan0406")!=null&&"1".equals(map.get("plan0406").toString())){
					String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '2'";
					Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
					retStr+=codeMap.get("caption")+"，";
				}
				if(map.get("plan0407")!=null&&"1".equals(map.get("plan0407").toString())){
					String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '3'";
					Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
					retStr+=codeMap.get("caption")+"，";
				}
				if(map.get("plan0408")!=null&&"1".equals(map.get("plan0408").toString())){
					String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '4'";
					Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
					retStr+=codeMap.get("caption")+"，";
				}
				if(map.get("plan0409")!=null&&"1".equals(map.get("plan0409").toString())){
					String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '5'";
					Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
					retStr+=codeMap.get("caption")+"，";
				}
				return  retStr.substring(0, retStr.length()-1);
			}else{
				return "";
			}*/
			
			//适用v3版
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
		 * 方案浏览时获取列表头部加载信息
		 * @param faid
		 * @return
		 */
		@Action
		public String getViewBaseInfo(String faid){
			ExtResultObject ero = new ExtResultObject();
			String zone = this.userSession.getCurrentUserZone();
			String sql = " select t.plan00,t.plan0107 as mc ,p3.plan0302 as zt ,t.plan0102 as yf, count(p2.recordid) as rs ,p6.plan0602 qys"+
			" from plan01 t left join plan03 p3 on p3.parentid = t.plan00 and p3.plan0301 = '"+zone+"'left join plan02 p2 on p2.parentid = t.plan00 and p2.plan0205 = '"+zone+"' and p2.plan0204 = 2 "+
			" left join plan06 p6 on p6.parentid = t.plan00 and p6.plan0601 = '"+zone+"' where t.plan00 = '"+faid+"' group by t.plan00,t.plan0107,p3.plan0302,t.plan0102,p6.plan0602";
			
			Map<String,Object> map = this.jdbcTemplate.queryForMap(sql);
			ero.add("id", map.get("plan00"));
			ero.add("mc", map.get("mc"));
			ero.add("yf", map.get("yf"));
			ero.add("rs", map.get("rs"));
			ero.add("qys", map.get("qys"));
			ero.add("zt", map.get("zt"));
			return ero.toString();
		}

	/**
	 * 展现下钻的人员和抽取的人员
	 * @return
	 */
	@Action
	public Renderer getRYcount(){
		Map<String, Object> vc = new HashMap<String, Object>();
		vc.put("name", "名称");
		return new TemplateRenderer(this.getClass(), "desktop", vc);
	}

	/**
	 * 返回人员信息的列表，提供给方案生成和浏览的人数下钻使用
	 * @param faid
	 * @return
	 */
	@Action
	public String getRYcount(String faid){
		ExtObjectCollection ero = new ExtObjectCollection();
		String flag=ActionContext.getActionContext().getHttpServletRequest().getParameter("flag");
		String qxdm=ActionContext.getActionContext().getHttpServletRequest().getParameter("zone");
		String zone = this.userSession.getCurrentUserZone();
		if(zone==null||"".equals(zone)){
			zone = qxdm;
		}
		
		String sql = null;

		if("zs".equals(flag)){
			 sql = "select p2.plan0202 as mc,p2.plan0203 as sfzh,case when a1.person_sex=1 then '女' else '男' end as xb,zs.zfcs as zfcs "+
					" from plan02 p2  left join a01 a1 on a1.a00=p2.plan0201 left join plan01 p1 on p1.plan00=p2.parentid"+
					" left join (  select plan01.plan0101 as nd,t.plan0201,count(t.recordid) as zfcs   from plan02 t left join plan01 on plan01.plan00 = t.parentid"+
					" where  t.plan0204 = 2   group by plan01.plan0101,t.plan0201 ) zs on zs.plan0201 = p2.plan0201 and zs.nd = p1.plan0101"+
					" where p2.parentid = '"+faid+"' and p2.plan0205 = '"+zone+"'";
		}else{
			 sql = "select p2.plan0202 as mc,p2.plan0203 as sfzh,case when a1.person_sex=1 then '女' else '男' end as xb,zs.zfcs as zfcs "+
					" from plan02 p2  left join a01 a1 on a1.a00=p2.plan0201 left join plan01 p1 on p1.plan00=p2.parentid"+
					" left join (  select plan01.plan0101 as nd,t.plan0201,count(t.recordid) as zfcs   from plan02 t left join plan01 on plan01.plan00 = t.parentid"+
					" where  t.plan0204 = 2   group by plan01.plan0101,t.plan0201 ) zs on zs.plan0201 = p2.plan0201 and zs.nd = p1.plan0101"+
					" where p2.plan0204 = 2 and p2.parentid = '"+faid+"' and p2.plan0205 = '"+zone+"'";
		}
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("mc", map.get("mc"));
			eo.add("xb", map.get("xb"));
			eo.add("sfzh", map.get("sfzh"));
			eo.add("chcs", map.get("zfcs"));
			ero.add(eo);
		}
		return ero.toString();
	}

	/**
	 * 展现先钻企业
	 * @param faid
	 * @return
	 */
	@Action
	public String getORGcount(String faid){
		ExtObjectCollection ero = new ExtObjectCollection();
		String flag=ActionContext.getActionContext().getHttpServletRequest().getParameter("flag");
		String qxdm=ActionContext.getActionContext().getHttpServletRequest().getParameter("zone");
		String zone = this.userSession.getCurrentUserZone();
		if(zone==null||"".equals(zone)){
			zone = qxdm;
		}
		String sql = null;
		
		if("zs".equals(flag)){
			
			sql = "select t.org_code as jgdm,t.org_name as dwmc,t.reg_addr dz,t.legal_repre as lxr ,t.legal_repre_tel phone "+
						  " from plan04 tt left join org01 t on t.org_code = tt.plan0402 where tt.parentid = '"+faid+"' and tt.plan0404 = '"+zone+"' ";
			
			/*sql = "select  t.org_code as jgdm,t.org_name as dwmc,t.reg_addr dz,t.legal_repre as lxr ,t.legal_repre_tel phone "+
					" from org01 t where t.reg_district_dic = '"+zone+"' ";*/
		}else{
			 sql = "select t.plan1202 as jgdm,t.plan1203 as dwmc,t.plan1205 dz,t.plan1206 as lxr ,t.plan1207 phone 	from plan12 t where t.parentid='"+faid+"' and t.plan1204='"+zone+"'";
		}

		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("jgdm", map.get("jgdm"));
			eo.add("dwmc", map.get("dwmc"));
			eo.add("dz", map.get("dz"));
			eo.add("lxr", map.get("lxr"));
			eo.add("phone", map.get("phone"));
			ero.add(eo);
		}
		return ero.toString();
	}
}
