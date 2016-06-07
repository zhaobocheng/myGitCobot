package com.bop.web.ssj.companymanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.bop.json.ExtFormObject;
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

		String sql= "select t.cid,t.caption,org.orgnum,per.pernum,p6.recordid, p6.plan0602,p6.plan0605 from dm_codetable_data t "+
					" left join (select count(*) orgnum,reg_district_dic  from ORG01 group by reg_district_dic ) org on org.reg_district_dic = t.cid"+
					" left join (select count(*) pernum,plan0205 from plan02 where plan0204 = 2 and parentid = '"+fzid+"' group by plan0205) per on per.plan0205 = t.cid"+
					" left join plan06 p6 on p6.plan0601 = t.cid and p6.parentid = '"+ fzid+
					"' where t.codetablename = 'DB064' and  t.cid != '110000'  order by t.cid ";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){
				ExtObject eo = new ExtObject();
				UUID uid = UUID.randomUUID();
				eo.add("id", map.get("recordid"));
				eo.add("qx", map.get("caption"));
				eo.add("qxid", map.get("cid"));
				eo.add("cyqys", map.get("orgnum"));
				eo.add("cyzfrys", map.get("pernum"));
				eo.add("sjqyzs", map.get("plan0602"));
				eo.add("zt", map.get("plan0605"));//是否提交过
				eoc.add(eo);
			}
		}
		return eoc.toString();
	}
	
	/**
	 * 得到权重值，用于判定这个方案是否设置了权重
	 * @return
	 */
	@Action
	public String getWFormData(String faid){
		String wheresql = " parentid = '"+faid+"'";
		Records rds = this.recordDao.queryRecord("PLAN08", wheresql);
		ExtFormObject form = new ExtFormObject();
		
		for(IRecord rd :rds){
			if("ts".equals(rd.get("plan0801"))){
				form.add("ts", rd.get("plan0802"));	
			}
			if("jl".equals(rd.get("plan0801"))){
				form.add("jl", rd.get("plan0802"));	
			}
			if("qzrz".equals(rd.get("plan0801"))){
				form.add("qzrz", rd.get("plan0802"));	
			}
			if("bz".equals(rd.get("plan0801"))){
				form.add("bz", rd.get("plan0802"));	
			}
		}
		
		return form.toString();
	}
	
	/**
	 * 设置权重
	 * @return
	 */
	@Action
	public String addWeightCon(String fzid){
		//PLAN08
		ExtResultObject ero = new ExtResultObject();
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();

		int ts = Integer.parseInt(request.getParameter("ts").toString());
		int jl = Integer.parseInt(request.getParameter("jl").toString());
		int qzrz = Integer.parseInt(request.getParameter("qzrz").toString());
		int bz = Integer.parseInt(request.getParameter("bz").toString());
		
		String tssy = request.getParameter("tssy").toString();
		String jlsy = request.getParameter("jlsy").toString();
		String qzrzsy = request.getParameter("qzrzsy").toString();
		String bzsy = request.getParameter("bzsy").toString();
		
		this.saveWeightConfig("ts",ts,tssy,fzid);
		this.saveWeightConfig("jl",jl,jlsy,fzid);
		this.saveWeightConfig("qzrz",qzrz,qzrzsy,fzid);
		this.saveWeightConfig("bz",bz,bzsy,fzid);
		this.jdbcTemplate.execute("update plan01 set plan0106=1 where plan00 = '"+fzid+"'");//1表示已经设置权重
		
		
		
		/*String zone = this.userSession.getCurrentUserZone();
		List<String> unSetList = new ArrayList<String>();
		//如果是某个区县设置权重 或者市局设置权重
		if(zone==null||"".equals(zone)){
			String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid   <> '110000'";
			List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
		
			if(resultList.size()>0){
				for(Map<String,Object> map:resultList){
					String flag = this.setRandm(fzid, map.get("cid").toString());
					if("false".equals(flag)){
						unSetList.add(map.get("cid").toString());
					}
				}
				
				if(unSetList.size()>0){
					ero.add("flag", "unset");
					ero.add("text", unSetList);
				}else{
					ero.add("flag", "success");
				}
			}
		}else{
			String flag = this.setRandm(fzid, zone);
			if("true".equals(flag))
				ero.add("flag", "success");
			else 
				ero.add("flag", "faile");
		}*/
		
		//当得到权重的时候要向随机库中放对应的随机基数
		/*String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid <> '110000'";
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
				}
				
				String upSql = "update plan03 set plan0302 = 2 where parentid = '"+fzid+"' and plan0301 = '"+map.get("cid").toString()+"'";
				this.jdbcTemplate.execute(upSql);
			}
		}*/
		ero.add("flag", "success");
		return ero.toString();
	}
	

	/**
	 * 初始化随机基数表
	 * @param zone
	 * @return
	 */
	private String setRandm(String fzid,String zone){
/*		String sql= "select * from plan03   where parentid = '"+fzid+"' and PLAN0301 = '"+zone+"'";
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
		if(resultList.size()>0 && "1".equals(resultList.get(0).get("plan0302").toString())){
			Map<String,Object> map = resultList.get(0);*/
			//生成对应的rand01记录    这里做判定如果已经产生了就不在插入了
			UUID rand1ID = UUID.randomUUID();
			IRecord ran1 = this.recordDao.createNew("RAND01", rand1ID, rand1ID);
			ran1.put("RAND0102", zone);
			ran1.put("RAND0101", fzid);
			this.recordDao.saveObject(ran1);

			 //建立序列号的语句
			String sequencesSql = "CREATE SEQUENCE emp_sequence  INCREMENT BY 1   START WITH 1  NOMAXVALUE   NOCYCLE  CACHE 10";
			String isSwq = "SELECT count(*) FROM All_Sequences where sequence_name='EMP_SEQUENCE'";
			int swq = this.jdbcTemplate.queryForInt(isSwq);

			if(swq>0){
				String dropSeqSql = "DROP SEQUENCE emp_sequence";
				this.jdbcTemplate.execute(dropSeqSql);
			}
			this.jdbcTemplate.execute(sequencesSql);

			//这种情况不考虑权重，既所有设置的权重都是1，并且去除已经抽取过的企业
			String exeSql = "insert into RAND02 select get_uuid,get_uuid,'"+rand1ID+"',null,emp_sequence.nextval,"+
							" t.parentid,t.org0201,t.org0202,t.org0203,t.org0204 "+
							" from org02 t where t.parentid in (select tt.org00 from ORG01 tt where tt.REG_DISTRICT_DIC = '"+zone+"' and tt.org00 not in(select PLAN1201 from plan12 where PLAN1204 = '"+zone+"'))";
			this.jdbcTemplate.execute(exeSql);
			return "true";
/*		}else{
			//请选确认执法人员
			return "false";
		}*/
		
		
		
		
		/*
		
				//一下是适合多权重的，sql和程序还需要优化一下
				//生成对应的rand02记录，随机基础数据
				//Records orgRec = this.recordDao.queryRecord("ORG01", "REG_DISTRICT_DIC='"+map.get("cid")+"'");//得到该地区所有的企业记录

				
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
				}
				
				String upSql = "update plan03 set plan0302 = 2 where parentid = '"+fzid+"' and plan0301 = '"+map.get("cid").toString()+"'";
				this.jdbcTemplate.execute(upSql);
			}
		}
		*/
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
		//校验有没有设置权重
		ExtResultObject ero = new ExtResultObject();

		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		String zsts = request.getParameter("zsts");//至少特设
		String zsjl = request.getParameter("zsjl");//至少计量
		JSONArray array = JSONArray.fromObject(data);

		/*如果按选择的条数来，就要去除企业总数为空的时机
		 String sdata = request.getParameter("sdata");
		JSONArray sarray = JSONArray.fromObject(sdata);
		*/
		
		//一下是考虑各个区县可以自己设置权重的情况，
/*		List<Map<String,String>> useQX = new ArrayList<Map<String,String>>();
		List<String> unUseQX = new ArrayList<String>();
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			String qxid = jsonObject.get("qxid").toString();
			Records ird = this.recordDao.queryRecord("PLAN03", "parentid = '"+fzid+"' and plan0301 = '"+qxid+"'");
			
			if(ird.size()>0){
				Object p3 = ird.get(0).get("plan0302");
				if(p3!=null && "2".equals(p3.toString())){
					Map<String,String> map = new HashMap<String,String>();
					map.put("id", jsonObject.get("id").toString());
					map.put("qxid", jsonObject.get("qxid").toString());
					map.put("sjqyzs", jsonObject.get("sjqyzs").toString());
					useQX.add(map);
				}else{
					unUseQX.add(qxid);
				}
			}else{
				unUseQX.add(qxid);
			}
		}

		//对于已经设置权重的
		Records u1 = this.recordDao.queryRecord("USER01", "user0101 = '"+this.userSession.getCurrentUserId()+"'");
		for(int i=0;i<useQX.size();i++){
			Map<String,String> map = useQX.get(i);

			IRecord ird = this.recordDao.createNew("PLAN06", UUID.fromString(map.get("id")), UUID.fromString(fzid));
			ird.put("PLAN0601", map.get("qxid"));
			ird.put("PLAN0602", map.get("sjqyzs"));
			ird.put("PLAN0603", u1.get(0).get("user00"));
			ird.put("PLAN0604", new Date());
			this.recordDao.saveObject(ird);

			String upSql = "update plan03 set plan0302 = 3 where parentid = '"+fzid+"' and plan0301 = '"+map.get("qxid")+"'";
			this.jdbcTemplate.execute(upSql);
		}
		
		ero.add("flag", true);
		ero.add("text", unUseQX.toString());
		return ero.toString();
		
		if(zone==null||"".equals(zone)){
			String sql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid   <> '110000'";
			List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
		
			if(resultList.size()>0){
				for(Map<String,Object> map:resultList){
					String flag = this.setRandm(fzid, map.get("cid").toString());
					if("false".equals(flag)){
						unSetList.add(map.get("cid").toString());
					}
				}
				
				if(unSetList.size()>0){
					ero.add("flag", "unset");
					ero.add("text", unSetList);
				}else{
					ero.add("flag", "success");
				}
			}
		}else{
			String flag = this.setRandm(fzid, zone);
			if("true".equals(flag))
				ero.add("flag", "success");
			else 
				ero.add("flag", "faile");
		}
		*/
		List<String> unUseQX = new ArrayList<String>();
		//现在使用的是由市局统一配置对应的权重,先判定市局有没有设置权重plan01添加对应的字段，初始化抽取企业（去除已抽取）
		IRecord p1 = this.recordDao.getRecord("PLAN01", UUID.fromString(fzid));
		String p6flag = p1.get("plan0106").toString();
		
		if("1".equals(p6flag)){//已经设置权重
			Records u1 = this.recordDao.queryRecord("USER01", "user0101 = '"+this.userSession.getCurrentUserId()+"'");
			for(int i=0;i<array.size();i++){
				JSONObject jsonObject = (JSONObject) array.get(i);
				String qxid = jsonObject.get("qxid").toString();
				
				String sql= "select * from plan03   where parentid = '"+fzid+"' and PLAN0301 = '"+qxid+"'";
				List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(sql);
				if(resultList.size()>0 && "1".equals(resultList.get(0).get("plan0302").toString())){//已经设置人员的
					UUID uid = UUID.randomUUID();
					IRecord ird = this.recordDao.createNew("PLAN06", uid, UUID.fromString(fzid));
					ird.put("PLAN0601", qxid);
					ird.put("PLAN0602", jsonObject.get("sjqyzs"));
					ird.put("PLAN0603", u1.get(0).get("user00"));
					ird.put("PLAN0605", "1");//表示已将提交过了
					ird.put("PLAN0604", new Date());
					this.recordDao.saveObject(ird);

					//给每个区县设置抽取企业
					this.setRandm(fzid, qxid);
					String upSql = "update plan03 set plan0302 = 3 where parentid = '"+fzid+"' and plan0301 = '"+qxid+"'";
					this.jdbcTemplate.execute(upSql);
				}else{
					//未设置人员的情况或者已提交的情况
					if(resultList.size()==0){
						unUseQX.add(qxid);
					}
				}
			}
			ero.add("text", unUseQX.toString());
			ero.add("flag", "f1");
		}else{
			ero.add("text", "请先设置权重");
			ero.add("flag", "f2");
		}
		
		return ero.toString();
	}
	
	
	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();
		
		if(zone==null||"".equals(zone)){
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0302 >= 3");
			if(rds.size()==17){
				return "3";  //以上报
			}else{
				Records rds2 = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0302 >= 2");
				if(rds2.size()==17){
					return "2";
				}else{
					return "select";
				}
			}
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
