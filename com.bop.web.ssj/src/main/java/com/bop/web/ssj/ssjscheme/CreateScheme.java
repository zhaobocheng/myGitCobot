package com.bop.web.ssj.ssjscheme;

import java.util.ArrayList;
import java.util.Date;
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
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;


@Controller
public class CreateScheme {

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
	 * 提交随机的抽查企业
	 * @param faid  方案id
	 * @return
	 */
	@Action
	public String commitSchemeDate(String faid){
		String zone = this.userSession.getCurrentUserZone();

		//String plan12UpSql = "update plan12 set plan1210 = '提交' where parentid = '"+faid+"' and plan1204 = '"+zone+"'";
		//zxy 修改plan1210 保存为代码 
		String plan12UpSql = "update plan12 set plan1210 = '2' ,plan1211=sysdate where parentid = '"+faid+"' and plan1204 = '"+zone+"'";
		String plan1201UpSql = "update plan1201 set plan120104 = '提交' where parentid in (select t.recordid from plan12 t where t.parentid = '"+faid+"' and t.plan1204 = '"+zone+"' )";
		
		this.jdbcTemplate.execute(plan1201UpSql);
		this.jdbcTemplate.execute(plan12UpSql);
		String org01Sql = "update org01 set influence_value = case when influence_value/10 > 0.01 then influence_value/10 else 0.01 end  where org00 in (select plan1201 from plan12 where parentid = '"+faid+"' and plan1204 = '"+zone+"')";
		//对已经抽取过的进行降权
		this.jdbcTemplate.execute(org01Sql);
		String plan03Sql = "update plan03 set plan0302 = 5,plan0303=sysdate where parentid = '"+faid+"' and plan0301 = '"+zone+"'";
		this.jdbcTemplate.execute(plan03Sql);
		return "success";
	}

    /**
     * 获取任务状态
     * @author bdsoft lh
     * @param faid
     * @return
     */
	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();

		if(zone==null||"".equals(zone)){
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0302 = 5");
			if(rds.size()==17){
				return rds.get(0).get("plan0302").toString();
			}else{
				return "select"; 
			}
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				return rds.get(0).get("plan0302").toString();  //以上报
			}else{
				return "select";
			}
		}
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
		String replace  =  ActionContext.getActionContext().getHttpServletRequest().getParameter("isreplace");
		Map<String,ArrayList> orgmap = new HashMap<String,ArrayList>();
		
		if("replace".equals(replace)){
			//记录作废方案的结果
			this.saveReplacedSchema(fzid,zone,"ss");
		}else{

		}

		IRecord rand01 = this.recordDao.queryTopOneRecord("RAND01", "RAND0101 = '"+fzid+"' and RAND0102='"+zone+"'","RAND0101");
		IRecord plan06ire = this.recordDao.queryTopOneRecord("PLAN06", "parentid='"+fzid+"' and plan0601='"+zone+"'","pindex");
		int allCqOrg = Integer.parseInt(plan06ire.get("PLAN0602").toString());

		//20161028加入专项的概念，得到专项的企业由getQxCqOrg统一抽取。
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList("select sp2.SP0201 from sp02 sp2 where sp2.sp0204 = '"+zone+"'  and sp2.parentid in (select p1.plan0111 from plan01 p1  where p1.plan00 = '"+fzid+"')");

		this.getQxCqOrg(allCqOrg,rand01,list,orgmap);
		//储存抽取的结果
		for (Map.Entry<String, ArrayList> entry : orgmap.entrySet()) {
		  //IRecord orgIcd = this.recordDao.getRecord("ORG01", UUID.fromString(entry.getKey()));
		   
		 //v3改动
		   IRecord orgIcd = this.recordDao.getRecord("PLAN04", UUID.fromString(entry.getKey()));
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
		this.createPlan21(faid, zone);    //创建废弃信息主表
	//	this.jdbcTemplate.execute("delete from PLAN1201 where parentid in (select recordid from PLAN12 where parentid = '"+faid+"' and plan1210 = '保存' and plan1204 = '"+zone+"')");
		//this.jdbcTemplate.execute("delete from PLAN12 where parentid = '"+faid+"' and plan1210 = '保存' and plan1204 = '"+zone+"'");   //上一步不用操作，因为这里使用的是级联删除，同时触发删除的表记录到废弃表中
		//zxy 修改 plan1210 改为代码
		this.jdbcTemplate.execute("delete from PLAN12 where parentid = '"+faid+"' and plan1210 = '1' and plan1204 = '"+zone+"'");   //上一步不用操作，因为这里使用的是级联删除，同时触发删除的表记录到废弃表中

	}

	/**
	 * 创建废弃的信息表
	 * @param faid
	 * @param zone
	 */
	private void createPlan21(String faid,String zone){
		UUID uid=UUID.randomUUID();
		int latestIndex = 0;

		IRecord latestIre = this.recordDao.queryTopOneRecord("FQ01", "FQ0105='"+faid+"' and FQ0103='"+zone+"'", "FQ0104 desc");
		if(latestIre!=null){
			latestIndex = latestIre.get("FQ0104",Integer.class)+1;
		}
		IRecord ire=this.recordDao.createNew("FQ01", uid, uid);
		
		ire.put("FQ0101", "废弃原因");
		ire.put("FQ0102", new Date());
		ire.put("FQ0103", zone);
		ire.put("FQ0104", latestIndex);
		ire.put("FQ0105", faid);
		this.recordDao.saveObject(ire);
	}


	/**
	 * 抽取每个区县设置的数量的企业和人员
	 * @param allCqOrg 每个区县要抽取的企业数
	 * @param rand01ID	区县的ID（rand01）
	 * @param orglist	存放抽取的企业code的容器
	 */
	private void getQxCqOrg(int allCqOrg,IRecord rand01,List<Map<String,Object>> spObjecOrg,Map<String,ArrayList> orgmap){
		List<String> dqOrglist = new ArrayList<String>();//本次已经抽取过的企业，记录用来判重
		Random rd = new Random();
		String faid = rand01.get("rand0101").toString();
		String zone = rand01.get("RAND0102",DmCodetables.class).getId();
		
		//获取抽取人员组合
		Map<Integer ,ArrayList<String>> zhMap = this.randPerson(faid,zone,allCqOrg);  //存放按企业数随机好人员的map
		List<IRecord> rand02s = this.recordDao.getByParentId("RAND02", rand01.getRecordId());//得到该区县所有的企业信息

		int zoneqystart=0;
		//抽取专项企业
		if(spObjecOrg.size()>0){
			zoneqystart = getSpecileObjectOrg(zoneqystart,spObjecOrg,orgmap,zhMap,dqOrglist);
		}
		//抽取特殊企业
		zoneqystart = this.getSpecileOrg(zoneqystart,rand01.getRecordId().toString(),orgmap,zhMap,dqOrglist);

		//抽取特殊企业外的
		for(int i=zoneqystart;i<allCqOrg;i++){
			boolean doubleflag = false;
			int orgindex = rd.nextInt(rand02s.size());

			IRecord  cqRand02= this.recordDao.queryTopOneRecord("RAND02", "RAND0201 = "+orgindex+"+1 and parentid = '"+rand01.getRecordId()+"'", "RAND0201");
			UUID orgCode = cqRand02.get("RAND0202",IRecord.class).getRecordId();

			for(int k=0;k<dqOrglist.size();k++){
				String dqorg = dqOrglist.get(k);
				if(orgCode.toString().equals(dqorg) || orgCode.toString() == dqorg){
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
	 * @param dqOrglist 判重容器
	 */
	private int getSpecileOrg(int zoneqystart,String rand01id,Map<String,ArrayList> orgmap,Map<Integer ,ArrayList<String>> zhMap, List<String> dqOrglist){
		Records jlIre = this.recordDao.queryRecord("RAND02", "parentid = '"+rand01id+"' and RAND0204 = 1");
		Records tsIre = this.recordDao.queryRecord("RAND02", "parentid = '"+rand01id+"' and RAND0203 = 1");

		if(jlIre.size()>0){
			Random rd = new Random();
			boolean flog ;
			do{
				int jlorgindex = rd.nextInt(jlIre.size());
				String orgId = jlIre.get(jlorgindex).get("RAND0202",IRecord.class).getRecordId().toString();
				flog = isRepeat(dqOrglist,orgId);
				//如果不是重复的就装入，抽查列表中
				if(!flog){
					orgmap.put(orgId, zhMap.get(zoneqystart));
					dqOrglist.add(orgId);
					zoneqystart++;
				};
			}while(flog);
		}

		if(tsIre.size()>0){
			Random rd2 = new Random();
			boolean flog ;
			do{
				int tsorgindex = rd2.nextInt(tsIre.size());
				String orgId = tsIre.get(tsorgindex).get("RAND0202",IRecord.class).getRecordId().toString();
				flog = isRepeat(dqOrglist,orgId);
				//如果不是重复的就装入，抽查列表中
				if(!flog){
					orgmap.put(orgId, zhMap.get(zoneqystart));
					dqOrglist.add(orgId);
					zoneqystart++;
				};
			}while(flog);
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
		/*ire.put("PLAN1201", informIRe.getRecordId());
		ire.put("PLAN1202", informIRe.get("ORG_CODE"));
		ire.put("PLAN1203", informIRe.get("ORG_NAME"));
		ire.put("PLAN1205", informIRe.get("REG_ADDR"));
		ire.put("PLAN1204", informIRe.get("REG_DISTRICT_DIC"));
		ire.put("PLAN1206", informIRe.get("LEGAL_REPRE"));
		ire.put("PLAN1207", informIRe.get("LEGAL_REPRE_TEL"));
		ire.put("PLAN1208", "");
		ire.put("PLAN1209", 0);
		ire.put("PLAN1210", "1"); //zxy 修改为代码 
		this.recordDao.saveObject(ire);*/

		//v3版以后plan12中的数据从plan04里面取
		ire.put("PLAN1201", informIRe.getRecordId());
		ire.put("PLAN1202", informIRe.get("PLAN0402"));
		ire.put("PLAN1203", informIRe.get("PLAN0403"));
		ire.put("PLAN1205", informIRe.get("PLAN0416")==null?informIRe.get("PLAN0415"):informIRe.get("PLAN0416"));
		ire.put("PLAN1204", informIRe.get("PLAN0417")==null?informIRe.get("PLAN0404"):informIRe.get("PLAN0417"));
		ire.put("PLAN1206", informIRe.get("PLAN0418"));
		ire.put("PLAN1207", informIRe.get("PLAN0419"));
		ire.put("PLAN1208", "");
		ire.put("PLAN1209", 0);
		ire.put("PLAN1210", "1"); //zxy 修改为代码 
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
	

	/**
	 * 用于抽查专项的企业信息
	 * @param startcount  抽取的企业数
	 * @param spObjecOrg  专项企业内容
	 * @param orgmap	总抽取企业容器
	 * @param zhMap		人员内容
	 * @param dqOrglist	判重组
	 * @return
	 */
	private int getSpecileObjectOrg(int startcount,List<Map<String,Object>> spObjecOrg,Map<String,ArrayList> orgmap,Map<Integer ,ArrayList<String>> zhMap,List<String> dqOrglist){

		for(int i=0;i<spObjecOrg.size();i++){
			Map<String,Object> orgMap = spObjecOrg.get(i);
			orgmap.put(orgMap.get("sp0201").toString(), zhMap.get(startcount));
			dqOrglist.add(orgMap.get("sp0201").toString());
			startcount++;
		}		
		return startcount;
	};
	
	
	
	/**
	 * 判断抽取企业有没有重复的
	 * @param dqOrglist   判重底数
	 * @param orgCode	判重项
	 * @return
	 */
	private boolean isRepeat(List<String> dqOrglist,String orgCode){
		boolean doubleflag = false;
		for(int k=0;k<dqOrglist.size();k++){
			String dqorg = dqOrglist.get(k);
			if(orgCode.equals(dqorg) || orgCode == dqorg){
				doubleflag = true;
				break;
			}
		}
		return doubleflag;
	}
	

}