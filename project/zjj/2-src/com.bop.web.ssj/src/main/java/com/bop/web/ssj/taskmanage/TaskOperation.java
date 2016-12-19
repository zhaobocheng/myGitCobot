package com.bop.web.ssj.taskmanage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcOperations;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;

public class TaskOperation {
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
	 * 创建任务方法
	 * @author lh
	 * @param json 新增时传入的数据
	 * @return
	 */
	@Action
	public String addScheme(String json){
		ExtResultObject eor = new ExtResultObject();
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String addMonthcom = request.getParameter("addMonthcom").toString();
		String addYearcom = request.getParameter("addYearcom").toString();
		String faname = request.getParameter("faname").toString();
		String itemsId = request.getParameter("itemsId");
		
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
		red.put("PLAN0107", faname);
		SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd");
		String str =addYearcom+"-"+addMonthcom+"-"+"02";
		Date date=null;
		try {
			date = sd.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		red.put("PLAN0108", date);
		//red.put("PLAN0112", itemsId);
		this.recordDao.saveObject(red);
		
		//保存抽查任务关联的检查事项
		String [] strArray = itemsId.split(";");
		if(strArray.length>0){
			for(String item:strArray){
				UUID plan11Uid = UUID.randomUUID();
				IRecord p11Ire =this.recordDao.createNew("PLAN11",plan11Uid, uid);
				p11Ire.put("PLAN1101", item);
				this.recordDao.saveObject(p11Ire);
			}
		}

		//保存风险系数
		String gjfx = request.getParameter("gjfx").toString();
		String djfx = request.getParameter("djfx").toString();
		String zjfx = request.getParameter("zjfx");

		String gjsql = "update fx01 set fx0102 = '"+gjfx+"' where fx0101 = '高'";
		String djsql = "update fx01 set fx0102 = '"+djfx+"' where fx0101 = '低'";
		String zjsql = "update fx01 set fx0102 = '"+zjfx+"' where fx0101 = '中'";
		
		this.jdbcTemplate.execute(gjsql);
		this.jdbcTemplate.execute(djsql);
		this.jdbcTemplate.execute(zjsql);
		
		eor.add("inf", "true");
		return eor.toString();
	}

	/**
	 * 删除任务方法
	 * @author bdsoft lh
	 * @return
	 */
	@Action
	public String deleteScheme(){
		ExtResultObject ero = new ExtResultObject();
		String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");

		IRecord p1 = this.recordDao.getRecord("PLAN01", UUID.fromString(id));
		if(p1.get("PLAN0105")!=null && "1".equals(p1.get("PLAN0105").toString())){
			return "false";
		}else{
			this.recordDao.deleteObject("PLAN01", UUID.fromString(id));
			ero.add("result", true);
			return "success";
		}
	}

	/**
	 * 加载任务列表的方法
	 * @return
	 * @author lh
	 */
	@Action
	public String getGridData(){
		//查询数据库
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String year = request.getParameter("year");

		ExtObjectCollection eoc = new ExtObjectCollection();
		Records rds = this.recordDao.queryRecord("PLAN01","PLAN0101 = "+year,"plan0102");

		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		for(IRecord rd :rds){
			ExtObject eo = new ExtObject();
			eo.add("id", rd.getObjectId());
			
			if(rd.get("PLAN0102").toString().length()>1){
				eo.add("zftime",rd.get("PLAN0101").toString()+rd.get("PLAN0102").toString());
			}else{
				eo.add("zftime",rd.get("PLAN0101").toString()+"0"+rd.get("PLAN0102").toString());
			}
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
			eo.add("famc",rd.get("PLAN0107").toString());
			eo.add("zt", "1".equals(rd.get("PLAN0105").toString())?"已启用":"未启用");
			eoc.add(eo);
		}
		return eoc.toString();
	}

	/**
	 * 启用方案
	 * @author bdsoft lh
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
			//方案启动生成一条记录状态的记录，复制人员信息到plan02,复制企业信息到plan04后两步用触发器实现
			//v3版本需要复制items01的信息到plan0401中去,这个在触发其中不能同步的做
			String insertSql = "insert into plan0401 select get_uuid,get_uuid,t.recordid,1,t4.org0401,t4.org0402,i.item0101,dm.caption"+
					" from plan04 t left join org04 t4 on t4.parentid = t.plan0401 left join item01 i on i.item00=t4.org0401  left join dm_codetable_data dm on dm.codetablename = 'ZDY02' and dm.cid = i.item0102"+
					" where t.parentid = '"+faid+"'";
			
			this.jdbcTemplate.execute(insertSql);
			this.insertPlan3(faid);
		}
		return "success";
	}
	/**
	 * 启动任务生出各区县状态表
	 * @author bdsoft lh
	 * @param faid
	 */
	private void insertPlan3(String faid){
		//需要区分是市局还是区县的用户
		String zone = this.userSession.getCurrentUserZone();
		if(zone!=null&&!"".equals(zone)){
			Object [] args = new Object[4];
			String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
			args[0]=faid;
			args[1]=faid;
			args[2]=this.userSession.getCurrentUserZone();
			args[3]=0;
			this.jdbcTemplate.update(sql, args);
		}else{
			Records irds = this.recordDao.queryRecord("dm_codetable_data", "codetablename='DB064' and cid<>'110000'");
			for(IRecord ird:irds){
				Object [] args = new Object[4];
				String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
				args[0]=faid;
				args[1]=faid;
				args[2]=ird.get("CID");
				args[3]=0;
				this.jdbcTemplate.update(sql, args);
			}
		}
	}
	
	@Action
	public String getitemsData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String qlmc = request.getParameter("itemmc")==null?null:request.getParameter("itemmc").toString().trim();
		String qldx = request.getParameter("itemdx")==null?null:request.getParameter("itemdx").toString().trim();
		String qlfl = request.getParameter("itemfl")==null?null:request.getParameter("itemfl").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		
		if(qlmc!=null&&!"".equals(qlmc)){
			whereString +=" and ITEM0101 like '%"+qlmc+"%'";
		}
		if(qldx!=null&&!"".equals(qldx)){
			whereString +=" and ITEM0103 = '"+qldx+"'";
		}
		if(qlfl!=null&&!"".equals(qlfl)&&!"0000".equals(qlfl)){
			whereString +=" and ITEM0102 ='"+qlfl+"'";
		}

		ExtGrid eg = new ExtGrid();
		Records rds = this.recordDao.queryRecord("ITEM01", whereString,"ITEM0102",pageIndex*pageSize,pageSize);
		int total= this.jdbcTemplate.queryForInt("select count(*) from ITEM01 where "+whereString);
		eg.setTotal(total);
		
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getObjectId());
			eo.add("itemmc", ird.get("ITEM0101"));
			eo.add("itemdx", ird.get("ITEM0103",DmCodetables.class).getCaption());
			eo.add("itemfl", ird.get("ITEM0102",DmCodetables.class).getCaption());
			eg.rows.add(eo);
		}
		return eg.toString();
	}
	
	@Action
	public String addXydj(String json){
		
		ExtResultObject eor = new ExtResultObject();
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String ajxy = request.getParameter("ajxy").toString();
		String bjxy = request.getParameter("bjxy").toString();
		String cjxy = request.getParameter("cjxy").toString();
		String djxy = request.getParameter("djxy").toString();
		
		
		this.jdbcTemplate.execute("update xy01 set xy0102 = "+ajxy +" where xy0101 = 'A'");
		this.jdbcTemplate.execute("update xy01 set xy0102 = "+bjxy +" where xy0101 = 'B'");
		this.jdbcTemplate.execute("update xy01 set xy0102 = "+cjxy +" where xy0101 = 'C'");
		this.jdbcTemplate.execute("update xy01 set xy0102 = "+djxy +" where xy0101 = 'D'");

		return eor.toString();
		
	}
	
	
	
}
