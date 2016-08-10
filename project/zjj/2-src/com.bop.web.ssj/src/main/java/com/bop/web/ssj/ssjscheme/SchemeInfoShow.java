package com.bop.web.ssj.ssjscheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
		String sql = "select  t.plan00,t.plan0107 as mc,t.plan0101||t.plan0102 as zfyf,counorg.qys as cycczs , case when p6.plan0602 is null then round(counorg.qys*0.01)  else p6.plan0602  end as ccqys , "+
					 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs from plan01 t  left join plan03 tt on tt.parentid = t.plan00 "+
					" left join (select count(*) qys,org.reg_district_dic from org01 org group by org.reg_district_dic) counorg on counorg.reg_district_dic = tt.plan0301"+
					" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
					" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301 where ";
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
				eo.add("wtjfas", map.get("wtjfas"));
				eo.add("cz", "");//是否提交过
				
			//	if("0".equals(zfzt)){
					eo.add("zffa", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\" style=\"width: 48%;height:100%\" onclick=\"createFa()\">生成执法方案</a>"
							+ " <a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\" style=\"width: 48%;height:100%\" onclick=\"viewFa()\">浏览执法方案</a>");
		/*		}else{
					eo.add("zffa", "");
				}*/
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}
	
	

//	---------------------------------------------------------------------方案浏览-------------------------------------------------------
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
				
				eo.add("sjly", this.getJSLY(ire.get("PLAN1202").toString()));
				eo.add("ParentDqId", ire.get("PLAN1204",DmCodetables.class).getId());
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
	
	
	//得到设计领域
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
		 * 方案浏览时获取列表头部加载信息
		 * @param faid
		 * @return
		 */
		@Action
		public String getViewBaseInfo(String faid){
			
			ExtResultObject ero = new ExtResultObject();
			String zone = this.userSession.getCurrentUserZone();
			String sql = " select t.plan0107 as mc ,t.plan0102 as yf, count(p2.recordid) as rs ,p6.plan0602 qys"+
			" from plan01 t left join plan02 p2 on p2.parentid = t.plan00 and p2.plan0205 = '"+zone+"' and p2.plan0204 = 2 "+
			" left join plan06 p6 on p6.parentid = t.plan00 and p6.plan0601 = '"+zone+"' where t.plan00 = '"+faid+"' group by t.plan0107,t.plan0102,p6.plan0602";
			
			Map<String,Object> map = this.jdbcTemplate.queryForMap(sql);
			ero.add("mc", map.get("mc"));
			ero.add("yf", map.get("yf"));
			ero.add("rs", map.get("rs"));
			ero.add("qys", map.get("qys"));
			return ero.toString();
		}
		

	//---------------------------执法人员数----------------------------------------
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
		String zone = this.userSession.getCurrentUserZone();
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
	
	@Action
	public String getORGcount(String faid){
		ExtObjectCollection ero = new ExtObjectCollection();
		String flag=ActionContext.getActionContext().getHttpServletRequest().getParameter("flag");
		String zone = this.userSession.getCurrentUserZone();
		String sql = null;
		
		if("zs".equals(flag)){
			 sql = "select  t.org_code as jgdm,t.org_name as dwmc,t.reg_addr dz,t.legal_repre as lxr ,t.legal_repre_tel phone "+
					" from org01 t where t.reg_district_dic = '110108' ";
		}else{
			 sql = "select t.plan1202 as jgdm,t.plan1203 as dwmc,t.plan1205 dz,t.plan1206 as lxr ,t.plan1207 phone 	from plan12 t where t.parentid='"+faid+"' and t.plan1204='"+zone+"'";
		}

		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(sql);
		
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("jgdm", map.get("jgdm"));
			eo.add("dwmc", map.get("mc"));
			eo.add("dz", map.get("dz"));
			eo.add("lxr", map.get("lxr"));
			eo.add("phone", map.get("phone"));
			ero.add(eo);
		}
		return ero.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//----------------------------------------------------------------------方案生成---------------------------------------------------------
	
	/**
	 * 判断任务状态，是否符合生成条件，是否是重复生成
	 * @param faid
	 * @return
	 */
	@Action
	public String isRepertCreate(String faid){
		String zone = this.userSession.getCurrentUserZone();
		ExtResultObject ero = new ExtResultObject();
		Records p3s = this.recordDao.queryRecord("PLAN03", "parentid = '"+faid+"' and plan0301 = '"+zone+"'");
		if(p3s.size()>0){
			Object p3 = p3s.get(0).get("plan0302");
			if(p3!=null && "4".equals(p3.toString())){
				ero.add("flag", "4");
				ero.add("text", "该方案已经生成方案，重新生成将产生记录！");
			}else if(p3!=null && "3".equals(p3.toString())){
				ero.add("flag", "3");
				ero.add("text", "第一次生成方案");
			}
		}else{
			ero.add("flag", "2");
			ero.add("text", "该方案不符合生成规则！");
		}
		return ero.toString();
	}
	
	
	
	/**
	 * 生成方案（再次生成方案删除，方案生成）
	 * @param faid  方案ID
	 * @return
	 */
	@Action
	public String createSchemeData(String fzid){
		String zone = this.userSession.getCurrentUserZone();
		String replace  =  ActionContext.getActionContext().getHttpServletRequest().getParameter("replace");
		Map<String,ArrayList> orgmap = new HashMap<String,ArrayList>();
		
		if("replace".equals(replace)){
			//记录作废方案的结果
			this.saveReplacedSchema(fzid,zone,"ss");
		}else{

		}

		IRecord rand01 = this.recordDao.queryTopOneRecord("RAND01", "RAND0101 = '"+fzid+"' and RAND0102='"+zone+"'","RAND0101");
		IRecord plan06ire = this.recordDao.queryTopOneRecord("PLAN06", "parentid='"+fzid+"' and plan0601='"+zone+"'","pindex");
		int allCqOrg = Integer.parseInt(plan06ire.get("PLAN0602").toString());
		this.getQxCqOrg(allCqOrg,rand01,orgmap);

		//储存抽取的结果
		for (Map.Entry<String, ArrayList> entry : orgmap.entrySet()) {
		   IRecord orgIcd = this.recordDao.getRecord("ORG01", UUID.fromString(entry.getKey()));
		   this.saveTempPlan12(orgIcd, entry.getValue(),fzid);
		  }
		
		//更新该区县的方案状态
		String sql = "update plan03 set plan0302 = 4 where parentid='"+fzid+"' and plan0301 = '"+zone+"'";
		this.jdbcTemplate.execute(sql);
		
		return "seccess";
	}
	
	/**
	 * 多次生成方案时，将之前的生成记录删除，并将删除的记录放到废弃方案表中（这里放到数据库触发器中实现）
	 * @param faid
	 * @param zone
	 * @param ss
	 */
	private void saveReplacedSchema(String faid,String zone,String ss){
		this.jdbcTemplate.execute("delete from PLAN1201 where parentid in (select recordid from PLAN12 where parentid = '"+faid+"' and plan1210 = '保存' and plan1204 = '"+zone+"')");
		this.jdbcTemplate.execute("delete from PLAN12 where parentid = '"+faid+"' and plan1210 = '保存' and plan1204 = '"+zone+"'");
	}
	
	/**
	 * 抽取每个区县设置的数量的企业和人员
	 * @param allCqOrg 每个区县要抽取的企业数
	 * @param rand01ID	区县的ID（rand01）
	 * @param orglist	存放抽取的企业code的容器
	 */
	private void getQxCqOrg(int allCqOrg,IRecord rand01,Map<String,ArrayList> orgmap){
		List<String> dqOrglist = new ArrayList<String>();//本次已经抽取过的企业，记录用来判重
		Random rd = new Random();
		String faid = rand01.get("rand0101").toString();
		String zone = rand01.get("RAND0102",DmCodetables.class).getId();
		
		
		
		
		
		//获取抽取人员组合
		Map<Integer ,ArrayList<String>> zhMap = this.randPerson(faid,zone,allCqOrg);  //存放按企业数随机好人员的map
		List<IRecord> rand02s = this.recordDao.getByParentId("RAND02", rand01.getRecordId());//得到该区县所有的企业信息

		//抽取特殊企业
		int zoneqystart=0;
		zoneqystart = this.getSpecileOrg(zoneqystart,rand01.getRecordId().toString(),orgmap,zhMap);

		//抽取特殊企业外的
		for(int i=zoneqystart;i<allCqOrg;i++){
			boolean doubleflag = false;
			int orgindex = rd.nextInt(rand02s.size());
			IRecord  cqRand02= this.recordDao.queryTopOneRecord("RAND02", "RAND0201 = "+orgindex+"+1 and parentid = '"+rand01.getRecordId()+"'", "RAND0201");
			UUID orgCode = cqRand02.get("RAND0202",IRecord.class).getRecordId();

			for(int k=0;k<dqOrglist.size();k++){
				String dqorg = dqOrglist.get(k);
				if(orgCode.equals(dqorg)){
					doubleflag = true;
					break;
				}
			}

			if(doubleflag){
				i--;
			}else{
				dqOrglist.add(orgCode.toString());
				orgmap.put(orgCode.toString(), zhMap.get(i));
			}
		}
	}


	/**
	 * 抽取特殊类型企业的方法，这里默认的是一次抽取一个计量类和一个特殊类的企业，如果以后接口放开，这动态加载抽取的总数
	 * @param zoneqystart  记录特殊抽取的企业数
	 * @param rand01id	 rand01的ID
	 * @param orgmap	存储抽取内容的容器
	 */
	private int getSpecileOrg(int zoneqystart,String rand01id,Map<String,ArrayList> orgmap,Map<Integer ,ArrayList<String>> zhMap){
		Records jlIre = this.recordDao.queryRecord("RAND02", "parentid = '"+rand01id+"' and RAND0204 = 1");
		Records tsIre = this.recordDao.queryRecord("RAND02", "parentid = '"+rand01id+"' and RAND0203 = 1");

		if(jlIre.size()>0){
			Random rd = new Random();
			int jlorgindex = rd.nextInt(jlIre.size());
			orgmap.put(jlIre.get(jlorgindex).get("RAND0202",IRecord.class).getRecordId().toString(), zhMap.get(zoneqystart));
			zoneqystart++;
		}
		if(tsIre.size()>0){
			Random rd = new Random();
			int tsorgindex = rd.nextInt(tsIre.size());
			orgmap.put(tsIre.get(tsorgindex).get("RAND0202",IRecord.class).getRecordId().toString(), zhMap.get(zoneqystart));
			zoneqystart++;
		}
		
		return zoneqystart;
	}

	
	/**
	 * 得到不同的随机号
	 * @param p1  第一次随机的数
	 * @param rdm 随机函数
	 * @param conutNum 随机总数
	 * @return
	 */
	private int getDifPerson(int p1,Random rdm,int conutNum){
		int p2 = rdm.nextInt(conutNum);
		if(p2 == p1 ){
			return this.getDifPerson(p1, rdm, conutNum);
		}
		return p2;
	}

	/**
	 * 
	 * @param faid 方案ID
	 * @param zone 区县ID
	 * @param allCqOrg 抽取企业数
	 * @return
	 */
	private Map<Integer ,ArrayList<String>> randPerson(String faid,String zone,int allCqOrg){
		String personSql = "select * from plan02 t where PLAN0204 = 2 and parentid = '"+faid+"' and  PLAN0205 = '"+zone+"'";
		Map<Integer ,ArrayList<String>> zhMap = new HashMap<Integer,ArrayList<String>>();
		
		
		List<Map<String,Object>> personMap = this.jdbcTemplate.queryForList(personSql);
		List<Map<String,Object>> OrdypersonMap2= new ArrayList<Map<String,Object>>();
		int personmapLc = 0;
		int ordypersonmapLc = 0;

		for(int i=0;i<allCqOrg;i++){
			ArrayList<String> personIreList = new ArrayList<String>();
			Random prd = new Random();
			//抽取先以轮次为主，如果是personmapLc>=ordypersonmapLc 说明以personMap集合为主，反之以OrdypersonMap2集合为主，要考虑当集合为一个元素，而要抽取连个的情况
			if(personmapLc>=ordypersonmapLc&&personMap.size()>=2){
				int p= prd.nextInt(personMap.size());
				int p2= prd.nextInt(personMap.size());
				if(p==p2){
					p2 = this.getDifPerson(p, prd, personMap.size());
				}
				
				Map<String,Object> ire = personMap.get(p);
				Map<String,Object> ire2 = personMap.get(p2);

				personIreList.add(ire.get("PLAN0201").toString());
				personIreList.add(ire2.get("PLAN0201").toString());

				OrdypersonMap2.add(ire);
				OrdypersonMap2.add(ire2);
				personMap.remove(ire2);
				personMap.remove(ire);
				if(personMap.size()==0){
					ordypersonmapLc+=1;
				}
			}else if(personmapLc>=ordypersonmapLc&&personMap.size()>0){
				//这是当personMap集合只剩下一个元素的情况
				//先将personmap抽完，再将ordypersonmap的抽取轮次变大，最后再将ordypersonmap中抽取过的人放到personmap中
				int p2= prd.nextInt(OrdypersonMap2.size());
				
				Map<String,Object> ire = personMap.get(0);
				Map<String,Object> ire2 = OrdypersonMap2.get(p2);
				personIreList.add(ire.get("PLAN0201").toString());
				personIreList.add(ire2.get("PLAN0201").toString());
				
				personMap.remove(ire);
				OrdypersonMap2.add(ire);
				OrdypersonMap2.remove(ire2);
				personMap.add(ire2);
				ordypersonmapLc+=1;
			}else if(personmapLc<ordypersonmapLc&&OrdypersonMap2.size()>=2){
				int p= prd.nextInt(OrdypersonMap2.size());
				int p2= prd.nextInt(OrdypersonMap2.size());
				if(p==p2){
					p2 = this.getDifPerson(p, prd, OrdypersonMap2.size());
				}

				Map<String,Object> ire = OrdypersonMap2.get(p);
				Map<String,Object> ire2 = OrdypersonMap2.get(p2);

				personIreList.add(ire.get("PLAN0201").toString());
				personIreList.add(ire2.get("PLAN0201").toString());

				personMap.add(ire);
				personMap.add(ire2);
				OrdypersonMap2.remove(ire2);
				OrdypersonMap2.remove(ire);
				if(OrdypersonMap2.size()==0){
					personmapLc+=1;
				}
			}else if(personmapLc<ordypersonmapLc&&OrdypersonMap2.size()>0){
				int p2= prd.nextInt(personMap.size());
				Map<String,Object> ire = OrdypersonMap2.get(0);
				Map<String,Object> ire2 = personMap.get(p2);
				personIreList.add(ire.get("PLAN0201").toString());
				personIreList.add(ire2.get("PLAN0201").toString());
				
				OrdypersonMap2.remove(ire);
				personMap.add(ire);
				personMap.remove(ire2);
				OrdypersonMap2.add(ire2);
				personmapLc+=1;
			}
			zhMap.put(i, personIreList);
		}
		return zhMap;
	}
	
	/**
	 * 每次抽取展现前将抽取的数据放到对应的抽取企业表和抽取人员表中，标识状态为一保存
	 * @param informIRe 抽取的记录
	 * @param list	抽取的人员信息
	 * @param faid 方案ID
	 */
	public void saveTempPlan12(IRecord informIRe,ArrayList list,String faid){

		UUID uid = UUID.randomUUID();
		IRecord ire = this.recordDao.createNew("PLAN12", uid, UUID.fromString(faid));
		//存储企业
		ire.put("PLAN1201", informIRe.getRecordId());
		ire.put("PLAN1202", informIRe.get("ORG_CODE"));
		ire.put("PLAN1203", informIRe.get("ORG_NAME"));
		ire.put("PLAN1205", informIRe.get("REG_ADDR"));
		ire.put("PLAN1204", informIRe.get("REG_DISTRICT_DIC"));
		ire.put("PLAN1206", informIRe.get("LEGAL_REPRE"));
		ire.put("PLAN1207", informIRe.get("LEGAL_REPRE_TEL"));
		ire.put("PLAN1208", "");
		ire.put("PLAN1209", 0);
		ire.put("PLAN1210", "保存");
		this.recordDao.saveObject(ire);
		
		
		for(int i=0;i<list.size();i++){
			String id = list.get(i).toString();
			IRecord personIre = this.recordDao.getRecord("A01", UUID.fromString(id));
			personIre.getRecordId().toString();
			IRecord zfryIre = this.recordDao.createNew("PLAN1201", uid, ire.getRecordId());
			zfryIre.put("PLAN120101", id);
			zfryIre.put("PLAN120102", personIre.get("PERSON_NAME").toString());
			zfryIre.put("PLAN120103", personIre.get("PERSON_IDCARD").toString());
			zfryIre.put("PLAN120104", "保存");
			this.recordDao.saveObject(zfryIre);
		}
	}

}
