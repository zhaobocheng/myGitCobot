package com.bop.web.ssj.ssjscheme;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.CommonSession;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
	 * 创建方案方法
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
		this.recordDao.saveObject(red);

		eor.add("inf", "true");
		return eor.toString();
	}

	/**
	 * 删除方案方法
	 * @return
	 */
	@Action
	public String deleteScheme(){
		ExtResultObject ero = new ExtResultObject();
		String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");
		//做数据库查询工作
		//一种返回方式，这里直接用第二种了
		this.recordDao.deleteObject("PLAN01", UUID.fromString(id));
		ero.add("result", true);
		return "success";
	}

	/**
	 * 得到方案列表的方法
	 * @return
	 */
	@Action
	public String getGridData(){
		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		Records rds = this.recordDao.queryRecord("PLAN01");
		
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		for(IRecord rd :rds){
			ExtObject eo = new ExtObject();

			eo.add("id", rd.getObjectId());
			eo.add("zftime",rd.get("PLAN0101").toString()+"0"+rd.get("PLAN0102").toString());
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
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
		}
		return "success";
	}
	
	
	@Action
	public String getSchemeDate(String fzid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		String querySql = null;

		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
			querySql = "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid not in ('110302','110000')";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
			querySql = "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid = '"+zone+"'";
		}

		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(querySql);

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				eo.add("ParentDqId", "0");
				eo.add("dq", map.get("caption"));
				eo.add("dqid", map.get("cid"));
				eoc.add(eo);
			}
		}

		//List<IRecord> ires = this.recordDao.getByParentId("PLAN12", UUID.fromString(faid));
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
				
				eo.add("sjly", ire.get("PLAN09")==null?"":ire.get("PLAN1209",DmCodetables.class).getCaption());
				eo.add("ParentDqId", ire.get("PLAN1204",DmCodetables.class).getId());
				eo.add("dqid",  null);
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}

	/**
	 * 创建抽取的企业和人员
	 * @param faid  方案ID
	 * @return
	 */
	@Action
	public String createSchemeDate(String fzid){
		
		
		//先判断是否设置了人员和企业数由plan0302来判断
		
		List<IRecord> plan3List = this.recordDao.getByParentId("PLAN03", UUID.fromString(fzid));
		
		for(int i=0;i<plan3List.size();i++){
			IRecord plan3 = plan3List.get(i);
			Object flag = plan3.get("PLAN0302");
			if(flag==null || !"2".equals(flag.toString())){
				return "false";
			}
		}

		ExtObjectCollection eoc = new ExtObjectCollection();
		//String faid = this.getP1ReocrdId(fzid);
		
		List<IRecord> plan06List = this.recordDao.getByParentId("PLAN06", UUID.fromString(fzid));
		List<String> orglist = new ArrayList<String>();  //存放所有抽取企业的容器
		Map<String,ArrayList> orgmap = new HashMap<String,ArrayList>();
		//对每个区县进行抽取
		for(IRecord plan06:plan06List){
			this.rultGridData("root", plan06,null, eoc,fzid);
			//开始抽取 得到每个区县的抽取个数,并放入总抽取容器中
			if(plan06.get("PLAN0602")!=null){
				IRecord rand01 = this.recordDao.queryTopOneRecord("RAND01", "RAND0101 = '"+fzid+"' and RAND0102='"+plan06.get("PLAN0601",DmCodetables.class).getId()+"'","RAND0101");
				int allCqOrg = Integer.parseInt(plan06.get("PLAN0602").toString());//得到该区县要抽取的企业个数
				this.getQxCqOrg(allCqOrg,rand01,orgmap);
			}else{
				//这个区县不抽取任何企业
			}
		}

		//清除这个方案已经有的临时数据先检查人员后企业
		this.jdbcTemplate.execute("delete from PLAN1201 where plan00 = '"+fzid+"' and plan120104 = '保存'");
		this.jdbcTemplate.execute("delete from PLAN12 where parentid = '"+fzid+"' and plan1210 = '保存'");
		
		//抽取完毕，进行遍历返回台展现
		  for (Map.Entry<String, ArrayList> entry : orgmap.entrySet()) {
			   IRecord orgIcd = this.recordDao.getRecord("ORG01", UUID.fromString(entry.getKey()));
			   this.rultGridData("leaf", orgIcd, entry.getValue(),eoc,fzid);
			  }

		return "seccess";
	}
	
	/**
	 * 抽取每个区县设置的数量的企业和人员
	 * @param allCqOrg 每个区县要抽取的企业数
	 * @param rand01ID	区县的ID（rand01）
	 * @param orglist	存放抽取的企业code的容器
	 */
	private void getQxCqOrg(int allCqOrg,IRecord rand01,Map<String,ArrayList> orgmap){
		List<String> dqOrglist = new ArrayList<String>();
		
		List<String> dqPersonlist = new ArrayList<String>();
		Random rd = new Random();
		Random prd = new Random();
		//得到一个区县的总人数
		//Records personRds = this.recordDao.queryRecord("PLAN02", "PLAN0204 = 0 and PLAN0205 = '"+rand01.get("RAND0102",DmCodetables.class).getId()+"'");
		
		String personSql = "select * from plan02 t where PLAN0204 = 0 and PLAN0205 = '"+rand01.get("RAND0102",DmCodetables.class).getId()+"'";
		List<Map<String,Object>> personMap = this.jdbcTemplate.queryForList(personSql);
		
		List<IRecord> rand02s = this.recordDao.getByParentId("RAND02", rand01.getRecordId());//得到该区县所有的企业信息
		//循环对应的企业
		for(int i=0;i<allCqOrg;i++){
			boolean doubleflag = false;

			int orgindex = rd.nextInt(rand02s.size());
			IRecord  cqRand02= this.recordDao.queryTopOneRecord("RAND02", "RAND0201 = "+orgindex+" and parentid = '"+rand01.getRecordId()+"'", "RAND0201");
			UUID orgCode = cqRand02.get("RAND0202",IRecord.class).getRecordId();

			for(int k=0;k<dqOrglist.size();k++){
				String dqorg = dqOrglist.get(k);
				if(orgCode.equals(dqorg)){
					//如果有相同的值则重新抽
					doubleflag = true;
					break;
				}
			}

			if(doubleflag){
				allCqOrg--;
			}else{
				dqOrglist.add(orgCode.toString());
				orgmap.put(orgCode.toString(), this.getCqPerson(prd,personMap));
			}
		}
	}

	/**
	 * 将抽到的企业和人的信息格式化成griddata
	 * @param rootType  是否是根节点的数据
	 * @param informIRe	一条数据记录
	 * @param eoc	返回的datagrid集合
	 */
	private void rultGridData(String rootType,IRecord informIRe,ArrayList list,ExtObjectCollection eoc,String faid){
		ExtObject eo = new ExtObject();
		if("root".equals(rootType)){
			eo.add("ParentDqId", "0");
			eo.add("dq", informIRe.get("PLAN0601",DmCodetables.class).getCaption());
			eo.add("dqid", informIRe.get("PLAN0601"));
		}else{
			eo.add("id", informIRe.getRecordId());
			eo.add("dq", informIRe.get("REG_DISTRICT_DIC",DmCodetables.class).getCaption());
			eo.add("jgdm", informIRe.get("ORG_CODE"));
			eo.add("dwmc", informIRe.get("ORG_NAME"));
			eo.add("dz", informIRe.get("REG_ADDR"));
			eo.add("lxr", informIRe.get("LEGAL_REPRE"));
			eo.add("phone", informIRe.get("LEGAL_REPRE_TEL"));
			eo.add("jcnr", null);
			eo.add("jcr", this.getJCRData(list)[1]);  //检查人
			eo.add("jcrid", this.getJCRData(list)[0]);
			eo.add("sjly", null);
			eo.add("ParentDqId", informIRe.get("REG_DISTRICT_DIC"));
			eo.add("dqid", informIRe.get("REG_DISTRICT_DIC",DmCodetables.class).getId());
			this.saveTempPlan12(informIRe,list,faid);
		}
		eoc.add(eo);
	}
	private String [] getJCRData(ArrayList list){
		String ids = null;
		String names = null;
		String inform[] = new String[2];
		for(int i=0;i<list.size();i++){
			String id = list.get(i).toString();
		//	IRecord personIre = this.recordDao.queryTopOneRecord("A01", "PLAN0201 = '"+id+"'","plan0201");
			IRecord personIre = this.recordDao.getRecord("A01", UUID.fromString(id));
			names+= personIre.get("PERSON_NAME").toString()+",";
			ids+=id+",";
		}
		inform[0] = ids;
		inform[1] = names;
		return inform;
	}
	/**
	 * 随机每个区县对应的一个配置的两个人
	 * @param prd   随机的范围
	 * @param personRds 随机的人员记录
	 * @return
	 */
	private ArrayList<String> getCqPerson(Random prd,Records personRds){
		ArrayList<String> personIreList = new ArrayList<String>();
		int p= prd.nextInt(personRds.size());
		int p2= prd.nextInt(personRds.size());
		

		IRecord ire = personRds.get(p);
		IRecord ss = ire.get("PLAN0201",IRecord.class);
		
		personIreList.add(personRds.get(p).get("PLAN0201",IRecord.class).getRecordId().toString());
		personIreList.add(personRds.get(p2).get("PLAN0201",IRecord.class).getRecordId().toString());
		return personIreList;
	}
	/**
	 * 随机每个区县对应的一个配置的两个人
	 * @param prd   随机的范围
	 * @param personRds 随机的人员记录
	 * @return
	 */
	private ArrayList<String> getCqPerson(Random prd,List<Map<String,Object>> personlist){
		ArrayList<String> personIreList = new ArrayList<String>();
		int p= prd.nextInt(personlist.size());
		int p2= prd.nextInt(personlist.size());
		
		Map<String,Object> ire = personlist.get(p);
		Map<String,Object> ire2 = personlist.get(p2);

		personIreList.add(ire.get("PLAN0201").toString());
		personIreList.add(ire2.get("PLAN0201").toString());
		return personIreList;
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
	 * 提交随机的抽查企业
	 * @param faid  方案id
	 * @return
	 */
	@Action
	public String commitSchemeDate(String faid){
		String plan12UpSql = "update plan12 set plan1210 = '提交' where parentid = '"+faid+"'";
		String plan1201UpSql = "update plan1201 set plan120104 = '提交' where parentid in (select recorid from plan12 where t.parentid = '"+faid+"')";
		
		this.jdbcTemplate.execute(plan1201UpSql);
		this.jdbcTemplate.execute(plan12UpSql);
		return "success";
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
			
		//	UUID personUid = UUID.randomUUID();
			IRecord zfryIre = this.recordDao.createNew("PLAN1201", uid, ire.getRecordId());
			zfryIre.put("PLAN120101", id);
			zfryIre.put("PLAN120102", personIre.get("PERSON_NAME").toString());
			zfryIre.put("PLAN120103", personIre.get("PERSON_IDCARD").toString());
			zfryIre.put("PLAN120104", "保存");
			this.recordDao.saveObject(zfryIre);
		}
	}
}