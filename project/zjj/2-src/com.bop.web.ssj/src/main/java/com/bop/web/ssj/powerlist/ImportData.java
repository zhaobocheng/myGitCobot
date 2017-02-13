package com.bop.web.ssj.powerlist;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.StringValueExp;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;

import com.aspose.cells.Cells;
import com.aspose.cells.Workbook;
import com.bop.common.StringUtility;
import com.bop.domain.IRecordDao;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.MultipartHttpServletRequest;

import net.sf.json.JSONObject;

public class ImportData {
	private IRecordDao recordDao;
	private UserSession userSession;
	private JdbcOperations jdbcTemplate;
	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private static final Logger log = LoggerFactory.getLogger(ImportData.class);
	
	
	
	/**
	 * 按分页的形式获取错误信息
	 */
	@Action
	public String errorInfo(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		ArrayList<JSONObject> errorLs =new ArrayList<JSONObject>(); 
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String cuid=request.getParameter("cuid")==null?"":request.getParameter("cuid").toString();
		ExtGrid eg = new ExtGrid();
		if(StringUtils.isNotBlank(cuid)){
			int start=pageIndex*pageSize+1;
			int end=pageIndex*pageSize+pageSize;
			int total=this.getTotal(cuid);
			//校验对应的数据,找不到机构的代码
					String noOrgCodeSql = "select ttt.* from ( select outsql.orgcode,outsql.orgname,outsql.regAddrName,outsql.regAddrCode,outsql.procAddrCode,outsql.notorg01 + outsql.notcity + outsql.notyield as flag ,rownum as rn "+
							" from (select t.orgcode,t.orgname,t.regAddrName,t.regAddrCode,t.procAddrCode, decode(org1.org_code,null,1,0) as notorg01,"+
							" case when dm.caption = t.regAddrName then 0 else 3 end as notcity,"+
							" case when dm2.caption is null then 5 else 0 end as notyield "+
							" from log03 t left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.regAddrCode "+
							" left join dm_codetable_data dm2 on dm2.codetablename = 'DB064' and dm2.cid = t.procAddrCode "+
							" left join org01 org1 on  org1.org_code = t.orgcode where t.parentid in (select log02.recordid from log02 where log02.parentid  = '"+cuid+"') order by t.orgcode ) outsql "+
							" where outsql.notorg01 + outsql.notcity + outsql.notyield > 0 ) ttt where ttt.rn between "+ start+" and "+ end;
					List<Map<String,Object>> listmap = this.jdbcTemplate.queryForList(noOrgCodeSql);
					for(Map<String,Object> map:listmap){
						String flag = map.get("flag").toString();
						String error="";
						//HashMap<String,String> sub=new HashMap<String,String>();
						ExtObject eo = new ExtObject();
						eo.add("orgName", map.get("orgname").toString());
						eo.add("city", map.get("regAddrName").toString());
						eo.add("code", map.get("orgcode")==null?"":map.get("orgcode").toString());
						eo.add("orgAddressCode",map.get("regAddrCode")==null?"":map.get("regAddrCode").toString());
						eo.add("yieldlyCode",map.get("procAddrCode")==null?"":map.get("procAddrCode").toString());

						if("1".equals(flag)){
							eo.add("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
							error="组织机构代码错误;";
						}else if("3".equals(flag)){
							eo.add("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
							error="注册地区划代码错误;";
						}else if("5".equals(flag)){
							eo.add("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
							error="生产地区划代码错误;";
						}else if("4".equals(flag)){
							eo.add("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
							eo.add("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
							error="组织机构代码错误;注册地区划代码错误;";
						}else if("6".equals(flag)){
							eo.add("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
							eo.add("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
							error="组织机构代码错误;生产地区划代码错误;";
						}else if("8".equals(flag)){
							eo.add("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
							eo.add("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
							error="注册地区划代码错误;生产地区划代码错误;";
						}else if("9".equals(flag)){
							eo.add("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
							eo.add("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
							eo.add("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
							error="组织机构代码错误;注册地区划代码错误;生产地区划代码错误;";
						}else {
							error = flag;
						}
						eo.add("errorInfo", error);
						eg.rows.add(eo);
					}
					eg.setTotal(total);
		} 
		return eg.toString();
	}
	
	private int getTotal(String cuid) {
		String noOrgCodeSql = "select ttt.* from ( select outsql.orgcode,outsql.orgname,outsql.regAddrName,outsql.regAddrCode,outsql.procAddrCode,outsql.notorg01 + outsql.notcity + outsql.notyield as flag ,rownum as rn "+
				" from (select t.orgcode,t.orgname,t.regAddrName,t.regAddrCode,t.procAddrCode, decode(org1.org_code,null,1,0) as notorg01,"+
				" case when dm.caption = t.regAddrName then 0 else 3 end as notcity,"+
				" case when dm2.caption is null then 5 else 0 end as notyield "+
				" from log03 t left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.regAddrCode "+
				" left join dm_codetable_data dm2 on dm2.codetablename = 'DB064' and dm2.cid = t.procAddrCode "+
				" left join org01 org1 on  org1.org_code = t.orgcode where t.parentid in (select log02.recordid from log02 where log02.parentid  = '"+cuid+"') order by t.orgcode ) outsql "+
				" where outsql.notorg01 + outsql.notcity + outsql.notyield > 0 ) ttt ";
		List<Map<String,Object>> listmap = this.jdbcTemplate.queryForList(noOrgCodeSql);
		return listmap.size();
	}
	/**
	 *只对文件的列数校验，将数据统一存到数据库中
	 * @param selected
	 * @return
	 */
	@Action
	public String identifyInfo(String selected){
		ArrayList<JSONObject> errorLs =new ArrayList<JSONObject>();
		MultipartHttpServletRequest request = (MultipartHttpServletRequest)ActionContext.getActionContext().getHttpServletRequest();
		 //上传附件input的name
		String[] arr = request.getFileFields();

		String title = arr[0];
		String fileName = request.getFileName("Fdata");
		FileInputStream fs;
		String tempsxid = "";
		
		Workbook designer =null;
		try {
			fs = (FileInputStream) request.getFileInputStream(title);
			designer = new Workbook(fs);
		} catch (IOException e) {
			log.error("读取导入文件IO错误");
			log.error(e.getMessage().toString());
			e.printStackTrace();
		}catch (Exception e) {
			log.error("读取导入文件错误");
			e.printStackTrace();
			log.error(e.getMessage().toString());
		}
		Cells cells = designer.getWorksheets().get(0).getCells();
		int rows=cells.getMaxDataRow()+1;
		int cols = cells.getMaxDataColumn()+1;
		if (log.isDebugEnabled()){
			log.debug("文件行数：" + rows +";文件列数：" + cols);
		}

		/*if(true){
			return "error";
		}*/
		
		if(cols != 12){
			return "error";
		}
		
		log.error("校验选择导入的检查事项和导入文件的的检查事项的一致性");
		selected = "'"+selected.replaceAll(",", "','")+"'";	
		if(selected.contains(",")){//包含的话说明勾选了多个；
			//校验之前先判断excel中的事项名字这列是不是为空，不为空不正确。
			for(int x=1;x<rows;x++){
				String itemNameByExcel=cells.get(x,0).getValue()==null?"": cells.get(x,0).getStringValue().toString();
				if(!"".equals(itemNameByExcel)){
					return "error1";
				}
			}
		}else{//勾选了一个；
			//根据事项名称的id来获取事项名称；
			List<Map<String, Object>> ndlist=this.getItemName(selected);
			String itemName="";
			if(ndlist.size()>0){
				itemName=ndlist.get(0).get("Item0101")==null?"":ndlist.get(0).get("Item0101").toString();
			}else{
				return "sysError"; 
			}
			//校验之前先判断excel中的事项名字是否正确。
			for(int x=1;x<rows;x++){
				String itemNameByExcel=cells.get(x,0).getValue()==null?"": cells.get(x,0).getValue().toString();
				if(!itemName.equals(itemNameByExcel)){
					return "error2";
				}
			}
		}

		log.error("excle校验成功！");
		
		UUID cuid = UUID.randomUUID();
		IRecord log1 =this.recordDao.createNew("LOG01",cuid,cuid);
		log1.put("LOG0101", fileName);	//表格名称
		log1.put("LOG0102", this.userSession.getCurrentUserName());
		log1.put("LOG0103", this.userSession.getCurrentUserId());
		log1.put("LOG0104", new Date());
		log1.put("LOG0105", "");
		log1.put("LOG0106", "");
		this.recordDao.saveObject(log1);
		
		
		log.error("excle导入开始！");
		List<Map<String, Object>> itemList= this.jdbcTemplate.queryForList("select Item00,item0101　from Item01  where Item00 in ("+selected+")");
		if(itemList.size()>0){
			for(Map<String,Object> map:itemList){
				UUID id = UUID.randomUUID();
				IRecord log2 =this.recordDao.createNew("LOG02",id,cuid);
				String itemid = map.get("Item00").toString();
				log2.put("LOG0201", itemid);
				log2.put("LOG0202", map.get("item0101").toString());
				
				String log2Recordid = log2.getRecordId().toString();
				this.recordDao.saveObject(log2);
				
				if(tempsxid.length()<2){
					tempsxid+=id;
				}else{
					tempsxid+=","+id;
				}
				
				for(int i=1;i<rows;i++){
					//组织机构代码
					String orgCodePre = cells.get(i,1).getStringValue()==null?"": cells.get(i,1).getStringValue().trim();
					String orgCode = orgCodePre.replaceAll("-", "");
					String creditCode = "";
					if(orgCode.length()==18){
						creditCode = orgCode;
						orgCode = orgCodePre.substring(8,17);
					}

					//企业名称
					String orgName=cells.get(i,2).getStringValue()==null?"": cells.get(i,2).getStringValue().trim();
					// 注册地址
					String strAddr = cells.get(i,3).getStringValue()==null?"": cells.get(i,3).getStringValue().trim();
					//注册地区划代码
					String orgAddressCode=cells.get(i,4).getStringValue()==null?"": cells.get(i,4).getStringValue().trim();
					//注册区县
					String city=cells.get(i,5).getStringValue()==null?"": cells.get(i,5).getStringValue().trim();
					// 生产地址
					String procAddr = cells.get(i,6).getStringValue()==null?"": cells.get(i,6).getStringValue().trim();
					procAddr = procAddr.replaceAll("'", "");
					
					//生产地区划代码
					String yieldlyCode=cells.get(i,7).getStringValue()==null?"": cells.get(i,7).getStringValue().trim();
					// 联系人
					String personName=cells.get(i,8).getStringValue()==null?"": cells.get(i,8).getStringValue().trim();
					//联系电话
					String personPhone=cells.get(i,9).getStringValue()==null?"": cells.get(i,9).getStringValue().trim();
					//风险等级
					String riskLevel=cells.get(i,10).getStringValue()==null?"": cells.get(i,10).getStringValue().trim();
					//子码
					String subCode=cells.get(i,11).getStringValue()==null?"": cells.get(i,11).getStringValue().trim();
					String insertSql = "insert into log03 values(get_uuid,get_uuid,'"+log2Recordid+"',1,'"+itemid+"','事项名称','"+orgCodePre+"','"+orgCode+"','"+creditCode+"','"+orgName+"','"+strAddr+"','"+orgAddressCode+"','"+city+"','"+procAddr+"','"+yieldlyCode+"','"+personName+"','"+personPhone+"','"+riskLevel+"','"+subCode+"',0,0)";
					this.jdbcTemplate.execute(insertSql);
				}
			}
			log.error("excle导入成功！");
		}else{
			return "error";
		}
		
		int errorNum = 0;
		//校验对应的数据,找不到机构的代码
		String noOrgCodeSql = "select ttt.* from ( select outsql.orgcode,outsql.orgname,outsql.regAddrName,outsql.regAddrCode,outsql.procAddrCode,outsql.notorg01 + outsql.notcity + outsql.notyield as flag ,rownum as rn "+
				" from (select t.orgcode,t.orgname,t.regAddrName,t.regAddrCode,t.procAddrCode, decode(org1.org_code,null,1,0) as notorg01,"+
				" case when dm.caption <> t.regAddrName then 3 else 0 end as notcity,"+
				" case when dm2.caption is null then 5 else 0 end as notyield "+
				" from log03 t left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.regAddrCode "+
				" left join dm_codetable_data dm2 on dm2.codetablename = 'DB064' and dm2.cid = t.procAddrCode "+
				" left join org01 org1 on  org1.org_code = t.orgcode where t.parentid in (select log02.recordid from log02 where log02.parentid  = '"+cuid+"') order by t.orgcode ) outsql "+
				" where outsql.notorg01 + outsql.notcity + outsql.notyield > 0 ) ttt ";
		List<Map<String,Object>> listmap = this.jdbcTemplate.queryForList(noOrgCodeSql);
		/*for(Map<String,Object> map:listmap){
			String flag = map.get("flag").toString();
			errorNum++;
			String error="";
			
			HashMap<String,String> sub=new HashMap<String,String>();
			sub.put("errorIndex", String.valueOf(errorNum));
			sub.put("orgName", map.get("orgname").toString());
			sub.put("city", map.get("regAddrName").toString());
			sub.put("code", map.get("orgcode")==null?"":map.get("orgcode").toString());
			sub.put("orgAddressCode",map.get("regAddrCode")==null?"":map.get("regAddrCode").toString());
			sub.put("yieldlyCode",map.get("procAddrCode")==null?"":map.get("procAddrCode").toString());

			if("1".equals(flag)){
				sub.put("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
				error="组织机构代码错误;";
			}else if("3".equals(flag)){
				sub.put("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
				error="注册地区划代码错误;";
			}else if("5".equals(flag)){
				sub.put("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
				error="生产地区划代码错误;";
			}else if("4".equals(flag)){
				sub.put("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
				sub.put("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
				error="组织机构代码错误;注册地区划代码错误;";
			}else if("6".equals(flag)){
				sub.put("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
				sub.put("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
				error="组织机构代码错误;生产地区划代码错误;";
			}else if("8".equals(flag)){
				sub.put("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
				sub.put("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
				error="注册地区划代码错误;生产地区划代码错误;";
			}else if("9".equals(flag)){
				sub.put("orgAddressCode", "<label style=\"color:red\";>"+map.get("regAddrCode")+"</label>");
				sub.put("yieldlyCode", "<label style=\"color:red\";>"+map.get("procAddrCode")+"</label>");
				sub.put("code", "<label style=\"color:red\";>"+map.get("orgcode")+"</label>");
				error="组织机构代码错误;注册地区划代码错误;生产地区划代码错误;";
			}else {
				error = flag;
			}
			sub.put("errorInfo", error);
			JSONObject jsonObject = JSONObject.fromObject(sub);
			errorLs.add(jsonObject);
		}*/
		errorNum=listmap.size();
		
		Double s=Double.valueOf(errorNum)/Double.valueOf(rows-1);
		DecimalFormat df=new DecimalFormat("0.00");
		HashMap<String, String> mp=new HashMap<String, String>();
		mp.put("content",errorLs.toString());
		mp.put("percent",df.format(s*100)+"%");
		mp.put("tempsxid", cuid.toString());
		mp.put("errorNum",String.valueOf(errorNum));
		JSONObject jsonObject = JSONObject.fromObject(mp);
		return jsonObject.toString();
	}
	
	
	
	private List<Map<String, Object>> getItemName(String selected) {
		List<Map<String, Object>> itemList= this.jdbcTemplate.queryForList("select Item0101　from Item01  where Item00 in ("+selected+")");
		return itemList;
	}

	@Action
	public String impOrgInfo(String log1id,String flag,String selectId){
		String info="";
		this.upLog3Isuserfor(log1id);
		this.upLog3Isupdate(log1id);	
		
		log.error("更新LOG03中的数据到org04中");
		this.upOrg04(log1id);
		log.error("插入LOG03中的数据到org04中");
		this.insertOrg04(log1id);
		
		String getdatasql =  "  select tt.isup ,tt.isuse,tt.isuse-tt.isup as isinsert,tt.alldata,tt.alldata - tt.isuse as errordata from "+
				"(select sum(decode(t.isupdate,1,1,0)) as isup,   " +
				"  sum(decode(t.isusefor,1,1,0)) isuse, " +
				"  count(t.recordid) as alldata                                " +
				"  from log03 t                " +
				" where  t.parentid in (select log02.recordid from log02 where log02.parentid = '"+log1id+"')) tt";
		
		Map<String,Object> map = this.jdbcTemplate.queryForMap(getdatasql);
		
		info+=map.get("alldata")+";";
		info+=map.get("isinsert")+";";
		info+=map.get("isup")+";";
		info+=map.get("errordata");

		//写入日志
		IRecord log =this.recordDao.getRecord("LOG01", UUID.fromString(log1id));
		log.put("LOG0107", map.get("isinsert"));		//插入数量
		log.put("LOG0108", map.get("errordata"));		//错误数量
		log.put("LOG0109", map.get("isup"));		    //更新数量
		log.put("LOG0110", map.get("alldata"));			//文档总数量
		this.recordDao.saveObject(log);
		return info;
	}
	
	
	private void upLog3Isuserfor(String log1id){
		log.error("更新LOG03的isuserfor字段，标记插入的excle数据是否可用");
		//更新文件中的记录是否可用
		String updateUserForSql = " update log03 temp4 set temp4.isusefor = 1												"+
					" where temp4.recordid in (select outsql.recordid                                                       "+
					" from (select t.recordid ,decode(org1.org_code,null,1,0) as notorg01,                                  "+
					" case when dm.caption <> t.regAddrName then 3 else 0 end as notcity,                                   "+
					" case when dm2.caption is null then 5 else 0 end as notyield                                           "+
					" from log03 t left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.regAddrCode "+
					" left join dm_codetable_data dm2 on dm2.codetablename = 'DB064' and dm2.cid = t.procAddrCode               "+
					" left join org01 org1 on  org1.org_code = t.orgcode where t.parentid in (select log02.recordid from log02 where log02.parentid = '"+log1id+"')) outsql    "+
					" where outsql.notorg01 + outsql.notcity + outsql.notyield = 0)                                ";
		this.jdbcTemplate.execute(updateUserForSql);

	}
	
	private void upLog3Isupdate(String log1id){
		log.error("更新LOG03的isupdate字段，标记插入的excle数据是需要更新到org04中去的");
		String updateUpdateSql = " update log03 temp4 set temp4.isupdate = 1                        "+
		" where temp4.recordid in  (                               "+
		" select temp.recordid                                                              "+
		" from org04 t  left join org01 tt on tt.org00 = t.parentid                         "+
		" left join log03 temp on temp.orgcode = tt.org_code                           "+
		" where t.org0401 = temp.itemid and t.org0408 = temp.subcode and temp.isusefor = 1 and temp.parentid in (select log02.recordid from log02 where log02.parentid = '"+log1id+"'))";
		this.jdbcTemplate.execute(updateUpdateSql);
	}
	
	private int upOrg04(String log1id){
		
		String getUpSql =  " select org4.recordid,tt.risklevel,tt.procaddr,tt.procaddrcode,tt.personname,tt.personphone "+
				 " from org04 org4 left join org01 g1 on g1.org00 = org4.parentid                             "+
				 " left join log03 tt on tt.orgcode = g1.org_code                                             "+
				 " where org4.org0401 in (select log02.LOG0201 from log02 where log02.parentid = '"+log1id+"') "+
				 " and tt.isusefor = 1 and tt.isupdate = 1";
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(getUpSql);
		for(Map<String,Object> map : list){
			IRecord ire = this.recordDao.getRecord("ORG04", UUID.fromString(map.get("recordid").toString()));
			ire.put("org0402", map.get("risklevel"));
			ire.put("org0403", new Date());
			ire.put("org0404", map.get("procaddr"));
			ire.put("org0405", map.get("procaddrcode"));
			ire.put("org0406", map.get("personname"));
			ire.put("org0407", map.get("personphone"));
			this.recordDao.saveObject(ire);
		}
		return list.size();
	}
	private void insertOrg04(String log1id){
		
		String insertSql = " insert into org04 org4                                       " +
				" select get_uuid,get_uuid,outsql.org00,1,outsql.itemid,case when outsql.risklevel='高' then 1 when outsql.risklevel='低' then 3 else 2 end as risllevel ,   " +
				" sysdate,outsql.procaddr,outsql.procAddrCode,outsql.personname,outsql.personphone, " +
				" outsql.subcode, outsql.orgcode||outsql.subcode end    " +
				" from ( select t.*,org1.org00       " +
				" from log03 t " +
				" left join org01 org1 on  org1.org_code = t.orgcode where t.isusefor=1 and t.isupdate=0 and t.parentid in (select log02.recordid from log02 where log02.parentid = '"+log1id+"')) outsql  " ;
		this.jdbcTemplate.execute(insertSql);
	}
	
}
