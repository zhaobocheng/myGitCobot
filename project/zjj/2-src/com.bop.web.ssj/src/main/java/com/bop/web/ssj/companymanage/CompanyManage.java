package com.bop.web.ssj.companymanage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.CommonSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class CompanyManage {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private CommonSession commontSession;
	private UserService userService;
	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	
	public void setCommontSession(CommonSession commontSession) {
		this.commontSession = commontSession;
	}
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Action
	public String getGridData(){
		ExtObjectCollection eoc = new ExtObjectCollection();
		String sql= "select t.cid,t.caption,org.orgnum,per.pernum from dm_codetable_data t "+
					" left join (select count(*) orgnum,org_code  from ORG01 group by ORG_CODE ) org on org.org_code = t.cid"+
					" left join (select count(*) pernum,DEPARTMENT_ID from A01 group by DEPARTMENT_ID ) per on per.DEPARTMENT_ID = t.cid"+
					" where t.codetablename = 'DB064' and t.cid <> '110000' order by t.cid ";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				UUID uid = UUID.randomUUID();
				eo.add("id", uid);
				eo.add("qx", map.get("caption"));
				eo.add("qxid", map.get("cid"));
				eo.add("cyqys", map.get("orgnum"));
				eo.add("cyzfrys", map.get("pernum"));
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}

	/**
	 * 设置权重
	 * @return
	 */
	@Action
	public String addWeightCon(String zfid){
		//PLAN08
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();

		int ts = Integer.parseInt(request.getParameter("ts").toString());
		int jl = Integer.parseInt(request.getParameter("jl").toString());
		int qzrz = Integer.parseInt(request.getParameter("qzrz").toString());
		int bz = Integer.parseInt(request.getParameter("bz").toString());
		
		String tssy = request.getParameter("tssy").toString();
		String jlsy = request.getParameter("jlsy").toString();
		String qzrzsy = request.getParameter("qzrzsy").toString();
		String bzsy = request.getParameter("bzsy").toString();
		
		this.saveWeightConfig("ts",ts,tssy,zfid);
		this.saveWeightConfig("jl",jl,jlsy,zfid);
		this.saveWeightConfig("qzrz",qzrz,qzrzsy,zfid);
		this.saveWeightConfig("bz",bz,bzsy,zfid);


		//当得到权重的时候要向随机库中放对应的随机基数
		String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid <> '110000'";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
	
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				//生成对应的rand01记录
				UUID rand1ID = UUID.randomUUID();
				IRecord ran1 = this.recordDao.createNew("RAND01", rand1ID, rand1ID);
				ran1.put("RAND0102", map.get("cid"));
				ran1.put("RAND0101", zfid);
				this.recordDao.saveObject(ran1);

				//生成对应的rand02记录，随机基础数据
				Records orgRec = this.recordDao.queryRecord("ORG01", "org_code='"+map.get("cid")+"'");//得到该地区所有的企业记录
				for(IRecord org:orgRec){
					IRecord org2 = this.recordDao.queryTopOneRecord("ORG02", "parentid='"+org.getRecordId()+"'", "parenid");//得到企业对应的权重特性
					boolean flag = true;
					//企业是特设
					if("0".equals(org2.get("ORG0201"))){
						this.createRandBase(ts,org2,rand1ID);
						flag = false;
					}
					//企业是计量
					if("0".equals(org2.get("ORG0202"))){
						this.createRandBase(jl,org2,rand1ID);
						flag = false;
					}
					if("0".equals(org2.get("ORG0203"))){
						this.createRandBase(qzrz,org2,rand1ID);
						flag = false;
					}
					if("0".equals(org2.get("ORG0204"))){
						this.createRandBase(bz,org2,rand1ID);
						flag = false;
					}

					if(flag){
						this.createRandBase(1,org2,rand1ID);
					}
				}
			}
		}
		return "success";
	}
	
	
	
	/**
	 * 
	 * @param times 企业加权后的次数
	 * @param org2 企业性质特性
	 * @param rand1ID  父类ID
	 */
	public void createRandBase(int times,IRecord org2,UUID rand1ID){
		for(int i=0;i<times;i++){
			UUID rand2ID = UUID.randomUUID();
			IRecord rand2 = this.recordDao.createNew("RAND02", rand2ID, rand1ID);
			int sequence = this.jdbcTemplate.queryForInt("select emp_sequence.nextval from dual");
			rand2.put("RAND0201", sequence);//存入序列号，根据以上记录往下排
			rand2.put("RAND0202", org2.getParentId());//机构ID
			rand2.put("RAND0203", org2.get("ORG0201"));
			rand2.put("RAND0204", org2.get("ORG0202"));
			rand2.put("RAND0205", org2.get("ORG0203"));
			rand2.put("RAND0206", org2.get("ORG0204"));
			this.recordDao.saveObject(rand2);
		}
	}
	
	/**
	 * 
	 * @param weightType 权重类型
	 * @param weight	权重值
	 * @param isSY		是否适用
	 * @param zfid		方案ID
	 */
	private void saveWeightConfig(String weightType,int weight,String isSY,String zfid){
		UUID objectID = UUID.randomUUID();
		//父类ID是方案ID
		IRecord ird = this.recordDao.createNew("PLAN08", objectID, UUID.fromString(zfid));
		ird.put("PLAN0801", weightType);
		ird.put("PLAN0802", weight);
		ird.put("PLAN0803", isSY=="true"?0:1);  //0表示适用，1表示不适用
		this.recordDao.saveObject(ird);
	}

	
	/**
	 * 存储每个方案各个地区需要随机的企业数
	 * @return
	 */
	@Action
	public String addSJResult(String zfid){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		String zsts = request.getParameter("zsts");//至少特设
		String zsjl = request.getParameter("zsjl");//至少计量
		User01 u1 = this.userService.getByLoginName(this.commontSession.getCurrentUserName());
		JSONArray array = JSONArray.fromObject(data);

		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			IRecord ird = this.recordDao.createNew("PLAN06", UUID.fromString(jsonObject.get("id").toString()), UUID.fromString(zfid));
			ird.put("PLAN0601", jsonObject.get("qxid"));
			ird.put("PLAN0602", jsonObject.get("sjqyzs"));
			ird.put("PLAN0603", u1.getUser00());
			ird.put("PLAN0604", new Date());
			this.recordDao.saveObject(ird);
		}
		return "success";
	}
}
