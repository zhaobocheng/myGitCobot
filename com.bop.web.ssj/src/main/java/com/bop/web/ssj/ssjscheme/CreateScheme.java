package com.bop.web.ssj.ssjscheme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetabledefine;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.CommonSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.sun.org.apache.bcel.internal.classfile.Code;

@Controller
public class CreateScheme {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private CommonSession commontSession;
	private UserService userService;
	public void setCommontSession(CommonSession commontSession) {
		this.commontSession = commontSession;
	}

	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 创建方案方法
	 * @author lh
	 * @param json 新增时传入的数据
	 * @return
	 */
	@Action
	public String addScheme(String json){
		String str = json;
		HttpServletRequest repquest = ActionContext.getActionContext().getHttpServletRequest();

/*		JSONArray array = JSONArray.fromObject(params);
		JSONObject jsonObject = (JSONObject) array.get(0);
*/
		String addMonthcom = repquest.getParameter("addMonthcom").toString();
		String addYearcom = repquest.getParameter("addYearcom").toString();

		User01 u1 = this.userService.getByLoginName(this.commontSession.getCurrentUserName());

		UUID uid = UUID.randomUUID();
		IRecord red =this.recordDao.createNew("PLAN01",uid, uid);
		red.put("PLAN0101", addMonthcom);
		red.put("PLAN0102", addYearcom);
		red.put("PLAN0103", new Date());
		red.put("PLAN0104", u1.getUser00());
		red.put("PLAN0105", "1");
		this.recordDao.saveObject(red);

		return "seccess";
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
			eo.add("zftime",rd.get("PLAN0102").toString()+"0"+rd.get("PLAN0101").toString());
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
			eo.add("zt", rd.get("PLAN0105").toString());
			eoc.add(eo);
		}
		return eoc.toString();
	}

	
	
	
	
	
	@Action
	public String getSchemeDate(String faid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		
		String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid <> '110000'";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
	
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				eo.add("ParentDqId", "0");
				eo.add("dq", map.get("cid"));
				eo.add("dqid", map.get("caption"));
				eoc.add(eo);
			}
		}
		
		List<IRecord> ires = this.recordDao.getByParentId("PLAN12", UUID.fromString(faid));
		
		if(ires.size()>0){
			for(IRecord ire:ires){
				ExtObject eo = new ExtObject();
				String personInf[] = this.getJCRData(ire.getRecordId());
				
				eo.add("id", ire.getRecordId());
				eo.add("dq", ire.get("PLAN1204"));
				eo.add("jgdm", ire.get("PLAN1202"));
				eo.add("dwmc", ire.get("PLAN1203"));
				eo.add("dz",  ire.get("PLAN1205"));
				eo.add("lxr", ire.get("PLAN1206"));
				eo.add("phone", ire.get("PLAN1207"));
				eo.add("jcnr",  ire.get("PLAN1208"));
				eo.add("jcrid", personInf[0]);
				eo.add("jcr",  personInf[1]);
				eo.add("sjly",  ire.get("PLAN1209"));
				eo.add("ParentDqId",  ire.get("PLAN1204"));
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
	public String createSchemeDate(String faid){
		
		ExtObjectCollection eoc = new ExtObjectCollection();

		List<IRecord> plan06List = this.recordDao.getByParentId("PLAN06", UUID.fromString(faid));
		List<String> orglist = new ArrayList<String>();  //存放所有抽取企业的容器
		Map<String,ArrayList> orgmap = new HashMap<String,ArrayList>();
		//对每个区县进行抽取
		for(IRecord plan06:plan06List){
			this.rultGridData("root", plan06,null, eoc);
			//开始抽取 得到每个区县的抽取个数,并放入总抽取容器中
			if(plan06.get("PLAN0602")!=null){
				IRecord rand01 = this.recordDao.queryTopOneRecord("RAND01", "RAND0101 = '"+faid+"' and RAND0102='"+plan06.get("PLAN0601")+"'","");
				int allCqOrg = Integer.parseInt(plan06.get("PLAN0602").toString());//得到该区县要抽取的企业个数
				this.getQxCqOrg(allCqOrg,rand01,orgmap);
			}else{
				//这个区县不抽取任何企业
			}
		}

		//抽取完毕，进行遍历返回台展现
		  for (Map.Entry<String, ArrayList> entry : orgmap.entrySet()) {
			   System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			   IRecord orgIcd = this.recordDao.getRecord("ORG01", UUID.fromString(entry.getKey()));
			   this.rultGridData("leaf", orgIcd, entry.getValue(),eoc);
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
		Records personRds = this.recordDao.queryRecord("PLAN02", "PLAN0204 = '是' and PLAN0205 = '"+rand01.get("RAND0102",DmCodetables.class).getCid()+"'");
		
		//循环对应的企业
		for(int i=0;i<allCqOrg;i++){
			List<IRecord> rand02s = this.recordDao.getByParentId("RAND02", rand01.getRecordId());//得到该区县所有的企业信息
			boolean doubleflag = false;

			int orgindex = rd.nextInt(rand02s.size());
			IRecord  cqRand02= this.recordDao.queryTopOneRecord("RAND02", "RAND0201 = "+orgindex+" and parentid = '"+rand01.getRecordId()+"'", "RAND0201");
			String orgCode = cqRand02.get("RAND0202").toString();
			
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
				dqOrglist.add(orgCode);
				orgmap.put(orgCode, this.getCqPerson(prd,personRds));
			}
		}
	}

	/**
	 * 将抽到的企业和人的信息格式化成griddata
	 * @param rootType  是否是根节点的数据
	 * @param informIRe	一条数据记录
	 * @param eoc	返回的datagrid集合
	 */
	private void rultGridData(String rootType,IRecord informIRe,ArrayList list,ExtObjectCollection eoc){
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
			eo.add("dqid", null);
		}
		eoc.add(eo);
	}
	private String [] getJCRData(ArrayList list){
		String ids = null;
		String names = null;
		String inform[] = new String[2];
		for(int i=0;i<list.size();i++){
			String id = list.get(i).toString();
			IRecord personIre = this.recordDao.queryTopOneRecord("A01", "PLAN0201 = '"+id+"'","plan0201");
			names+= personIre.get("PLAN0202").toString()+",";
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
		
		personIreList.add(personRds.get(p).get("PLAN0201",IRecord.class).getRecordId().toString());
		personIreList.add(personRds.get(p2).get("PLAN0201",IRecord.class).getRecordId().toString());
		return personIreList;
	}
	
	
	/**
	 * 页面加载的时候得到检查人的信息
	 * @param plan12id
	 * @return
	 */
	private String [] getJCRData(UUID plan12id){
		String ids = null;
		String names = null;
		String inform[] = new String[2];
		
		List<IRecord> ires = this.recordDao.getByParentId("PLAN1201", plan12id);
		for(IRecord ire:ires){
			names+= ire.get("PLAN120102").toString()+",";
			ids+=ire.getRecordId().toString()+",";
		}
		
		inform[0] = ids.substring(0, ids.length()-1);
		inform[1] = names.substring(0, ids.length()-1);
		return inform;
	}
	
	
	public String commitSchemeDate(String faid){
		//得到对应的方案ID
		//区县ID、机构代码ID、检查人员ID
		//分别存入 PLAN12表和PLAN1201表
		//在显示的时候就存到对应的表里面去，设置一个状态，如果这里提交就将状态变为已提交
		

		return "success";
	}
	
}