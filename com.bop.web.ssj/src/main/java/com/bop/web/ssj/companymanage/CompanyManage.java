package com.bop.web.ssj.companymanage;

import java.text.SimpleDateFormat;
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
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class CompanyManage {

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
	

	@Action
	public String getGridData(String fzid){
		ExtObjectCollection eoc = new ExtObjectCollection();

		String sql= "select t.cid,t.caption,org.orgnum,per.pernum,p6.plan0602 from dm_codetable_data t "+
					" left join (select count(*) orgnum,reg_district_dic  from ORG01 group by reg_district_dic ) org on org.reg_district_dic = t.cid"+
					" left join (select count(*) pernum,plan0205 from plan02 where plan0204 = 2 and parentid = '"+fzid+"' group by plan0205) per on per.plan0205 = t.cid"+
					" left join plan06 p6 on p6.plan0601 = t.cid and p6.parentid = '"+ fzid+
					"' where t.codetablename = 'DB064' and  t.cid != '110000'  order by t.cid ";
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
				eo.add("sjqyzs", map.get("plan0602"));
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}
	
	/**
	 * 根据方案列表时间得到对应的方案ID
	 */
	private String getP1ReocrdId(String fzid){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		Date dt = new Date();
		String faid = sdf.format(dt);
		String year = faid.substring(0, 4);
		String month = faid.substring(5);

		Records p1rds = this.recordDao.queryRecord("PLAN01", "plan0101 ="+year+" and plan0102 ="+month);
		return p1rds.get(0).getRecordId().toString();
	}
	/**
	 * 设置权重
	 * @return
	 */
	@Action
	public String addWeightCon(String fzid){
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
		
		//String zfid = getP1ReocrdId(faid);
		
		this.saveWeightConfig("ts",ts,tssy,fzid);
		this.saveWeightConfig("jl",jl,jlsy,fzid);
		this.saveWeightConfig("qzrz",qzrz,qzrzsy,fzid);
		this.saveWeightConfig("bz",bz,bzsy,fzid);


		//当得到权重的时候要向随机库中放对应的随机基数
		String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid <> '110000'";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
	
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				//生成对应的rand01记录
				UUID rand1ID = UUID.randomUUID();
				IRecord ran1 = this.recordDao.createNew("RAND01", rand1ID, rand1ID);
				ran1.put("RAND0102", map.get("cid"));
				ran1.put("RAND0101", fzid);
				this.recordDao.saveObject(ran1);


			    //建立序列号的语句
				String sequencesSql = "CREATE SEQUENCE emp_sequence  INCREMENT BY 1   START WITH 1  NOMAXVALUE   NOCYCLE  CACHE 10";
				String isSwq = "SELECT count(*) FROM All_Sequences where sequence_name='EMP_SEQUENCE'";
				int swq = this.jdbcTemplate.queryForInt(isSwq);

				if(swq>0){
					//删除序列函数
					String dropSeqSql = "DROP SEQUENCE emp_sequence";
					this.jdbcTemplate.execute(dropSeqSql);
				}

				this.jdbcTemplate.execute(sequencesSql);
				int ssss = this.jdbcTemplate.queryForInt("select count(*) from ORG01 tt where tt.REG_DISTRICT_DIC = '"+map.get("cid")+"'");

				//这种情况不考虑权重，既所有设置的权重都是1
				String exeSql = "insert into RAND02 select get_uuid,get_uuid,'"+rand1ID+"',null,emp_sequence.nextval,"+
								" t.parentid,t.org0201,t.org0202,t.org0203,t.org0204 "+
								" from org02 t where t.parentid in (select tt.org00 from ORG01 tt where tt.REG_DISTRICT_DIC = '"+map.get("cid")+"')";
				this.jdbcTemplate.execute(exeSql);
				
				//一下是适合多权重的，sql和程序还需要优化一下
				//生成对应的rand02记录，随机基础数据
				//Records orgRec = this.recordDao.queryRecord("ORG01", "REG_DISTRICT_DIC='"+map.get("cid")+"'");//得到该地区所有的企业记录

				/*
				   for(IRecord org:orgRec){
					IRecord org2 = this.recordDao.queryTopOneRecord("ORG02", "parentid='"+org.getRecordId()+"'", "parentid");//得到企业对应的权重特性
					boolean flag = true;
					//企业是特设
					if("1".equals(org2.get("ORG0201"))){
						this.createRandBase(ts,org2,rand1ID);
						flag = false;
					}
					//企业是计量
					if("1".equals(org2.get("ORG0202"))){
						this.createRandBase(jl,org2,rand1ID);
						flag = false;
					}
					if("1".equals(org2.get("ORG0203"))){
						this.createRandBase(qzrz,org2,rand1ID);
						flag = false;
					}
					if("1".equals(org2.get("ORG0204"))){
						this.createRandBase(bz,org2,rand1ID);
						flag = false;
					}

					if(flag){
						this.createRandBase(1,org2,rand1ID);
					}
				}*/
				
				String upSql = "update plan03 set plan0302 = 2 where parentid = '"+fzid+"' and plan0301 = '"+map.get("cid").toString()+"'";
				this.jdbcTemplate.execute(upSql);
				
/*				IRecord p3Ire = this.recordDao.queryTopOneRecord("plan03", "parentid = '"+fzid+"' and plan0301 = '"+map.get("cid").toString()+"'", "plan0301");
				if(p3Ire!=null){
					p3Ire.put("PLAN0302", 2);
					this.recordDao.saveObject(p3Ire);
				}*/
			}
		}
		
/*		IRecord p3Ire = this.recordDao.queryTopOneRecord("plan03", "parentid = '"+fzid+"' and plan0301 = '"+this.userSession.getCurrentUserZone()+"'", "pindex");
		if(p3Ire!=null){
			p3Ire.put("plan0302", "2");
		}*/
		
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
	public String addSJResult(String fzid){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		String zsts = request.getParameter("zsts");//至少特设
		String zsjl = request.getParameter("zsjl");//至少计量
		
		Records u1 = this.recordDao.queryRecord("USER01", "user0101 = '"+this.userSession.getCurrentUserId()+"'");
		
		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			IRecord ird = this.recordDao.createNew("PLAN06", UUID.fromString(jsonObject.get("id").toString()), UUID.fromString(fzid));
			ird.put("PLAN0601", jsonObject.get("qxid"));
			ird.put("PLAN0602", jsonObject.get("sjqyzs"));
			ird.put("PLAN0603", u1.get(0).get("user00"));
			ird.put("PLAN0604", new Date());
			this.recordDao.saveObject(ird);


			String upSql = "update plan03 set plan0302 = 3 where parentid = '"+fzid+"' and plan0301 = '"+jsonObject.get("qxid")+"'";
			this.jdbcTemplate.execute(upSql);

			/*IRecord plan3Ire = this.recordDao.queryTopOneRecord("PLAN03", "PARENTID='"+fzid+"' and plan0301 = '"+jsonObject.get("qxid")+"'", "pindex");
			if(plan3Ire!=null){
				plan3Ire.put("PLAN0302", 3);
				this.recordDao.saveObject(plan3Ire);
			}*/
		}
		return "success";
	}
	
	
	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();
		
		if(zone==null||"".equals(zone)){
			return "all";
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				return rds.get(0).get("plan0302").toString();  //以上报
			}else{
				return "select";   //需要选择对象
			}
		}
	}
}
