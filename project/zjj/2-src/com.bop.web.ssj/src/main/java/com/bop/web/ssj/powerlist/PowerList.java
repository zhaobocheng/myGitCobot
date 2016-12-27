package com.bop.web.ssj.powerlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.framework.adaptor.FilePath;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Cells;
import com.aspose.cells.Color;
import com.aspose.cells.License;
import com.aspose.cells.Row;
import com.aspose.cells.RowCollection;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.XlsSaveOptions;
import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtFormObject;
import com.bop.json.ExtGrid;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.json.ExtTreeNode;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.bop.web.rest.MultipartHttpServletRequest;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PowerList {
	private IRecordDao recordDao;
	private UserSession userSession;
	private JdbcOperations jdbcTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(PowerList.class);
	
	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	/**
	 * 获取拉列表里面的值(权力分类)
	 * @author liupx
	 * @return
	 */
	
	@Action
	public String getPowerFL(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.cid,t.caption  from dm_codetable_data t where t.codetablename='ZDY07'");
		ExtObjectCollection eoc = new ExtObjectCollection();
		ExtObject eo = new ExtObject();
		eo.add("id", "0000");
		eo.add("text", "--请选择--");
		eoc.add(eo);
		for(Map<String,Object> ire:ndlist){
			eo = new ExtObject();
			eo.add("id", ire.get("cid"));
			eo.add("text", ire.get("caption"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	/**
	 * 权力清单查询
	 * @author liupx
	 * @return
	 */
	@Action
	public String getPowerListData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String qlbm = request.getParameter("qlbm")==null?null:request.getParameter("qlbm").toString().trim();
		String qlmc = request.getParameter("qlmc")==null?null:request.getParameter("qlmc").toString().trim();
		String qlfl = request.getParameter("qlfl")==null?null:request.getParameter("qlfl").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		
		if(qlbm!=null&&!"".equals(qlbm)){//权力清单
			whereString +=" and Q0101 like '%"+qlbm+"%'";
		}
		if(qlmc!=null&&!"".equals(qlmc)){//权力名称
			whereString +=" and Q0103 like '%"+qlmc+"%'";
		}
		if(qlfl!=null&&!"".equals(qlfl)&&!"0000".equals(qlfl)){//权力分类
			whereString +=" and Q0102 ='"+qlfl+"'";
		}

		ExtGrid eg = new ExtGrid();
		Records rds = this.recordDao.queryRecord("Q01", whereString,"Q0102",pageIndex*pageSize,pageSize);
		int total= this.jdbcTemplate.queryForInt("select count(*) from Q01 where "+whereString);
		eg.setTotal(total);
		
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getObjectId());
			eo.add("qlqdbm", ird.get("Q0101"));
			eo.add("qlsxmc", ird.get("Q0103"));
			eo.add("qlfl", ird.get("Q0102",DmCodetables.class).getCaption());
			eg.rows.add(eo);
		}
		return eg.toString();
	}
	
	/**
	 * 检查事项数据获取
	 * @author liupx
	 * @return
	 */
	@Action
	public String getCheckData(){
		ExtGrid eg = new ExtGrid();
		HttpServletRequest request =ActionContext.getActionContext().getHttpServletRequest();
		String sxmc=request.getParameter("sxmc")==null?null:request.getParameter("sxmc").toString();
		String sxfl=request.getParameter("sxfl")==null?null:request.getParameter("sxfl").toString();
		String qlbm=request.getParameter("qlbm")==null?null:request.getParameter("qlbm").toString();
		String qlmc=request.getParameter("qlmc")==null?null:request.getParameter("qlmc").toString();
		String feiqi=request.getParameter("feiqi")==null?null:request.getParameter("feiqi").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		
		String whereString = "1=1 ";   //状态为3的为废弃的。
		if(sxmc!=null&&!"".equals(sxmc)){
			whereString +=" and ITEM0101 like '%"+sxmc+"%'";	//事项名称
		}
		if(sxfl!=null&&!"".equals(sxfl)){
			whereString +=" and ITEM0102 = '"+sxfl+"'";			//事项业务分类
		}
		if(feiqi!=null&&!"".equals(feiqi)&&"2".equals(feiqi)){
			whereString +=" and ITEM0199 != '2'";			  //废弃
		}
		int total= this.jdbcTemplate.queryForInt("select count(*) from ITEM01 where "+whereString);
		eg.setTotal(total); 
		Records rds = this.recordDao.queryRecord("ITEM01", whereString,"item0101",pageIndex*pageSize,pageSize);
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getObjectId());
			eo.add("jcsxmc", ird.get("ITEM0101"));//检查事项名称
			eo.add("sxfl", ird.get("ITEM0102",DmCodetables.class).getCaption());//事项业务分类
			eo.add("ccdx", ird.get("ITEM0103",DmCodetables.class).getCaption());//抽查对象
			String status=ird.get("ITEM0199").toString();
			String statusStr="";
			if("0".equals(status)){
				statusStr="未提交";
			}else if("1".equals(status)){
				statusStr="已提交";
			}else if("2".equals(status)){
				statusStr="已废弃";
			}
			eo.add("status",statusStr);
		 
			List<Map<String, Object>> ccyjList= this.jdbcTemplate.queryForList("select distinct r.lr00, r.lr0101 from Item03 t, LR01 r  where  t.item0301=r.lr00 and  t.parentid='"+ird.getObjectId()+"'");
			String str="";
			for(Map<String,Object> ire:ccyjList){
				str+=ire.get("LR0101")+"</br>" ;
			}
			eo.add("ccyj", str);	//抽查依据
			List<Map<String, Object>> powerList= this.jdbcTemplate.queryForList("select distinct  q.q0101,q.q0103  from  Item02 t,Q01 q   where t.item0201=q.q00  and  t.parentid='"+ird.getObjectId()+"'");
			String qlqdStr="";
			for(Map<String,Object> ire:powerList){
				qlqdStr+=ire.get("Q0103")+"</br>" ;
			}
			//两者都为空
			if(((qlmc==null||"".equals(qlmc))&&(qlbm==null||"".equals(qlbm)))){
				 eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
				 eg.rows.add(eo);				 
				 
			//两者都不为空
			}else if(qlmc!=null&&!"".equals(qlmc)&&qlbm!=null&&!"".equals(qlbm)){
				if(qlqdStr.contains(qlmc)&&qlqdStr.contains(qlmc)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					 eg.rows.add(eo);
				}
			// 名称为空 编码不为空 
			}else if((qlmc==null||"".equals(qlmc)) &&qlbm!=null&&!"".equals(qlbm)){
				if(qlqdStr.contains(qlbm)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					 eg.rows.add(eo);
				}
			//名称不为空 编码为空	
			}else if((qlmc!=null&&!"".equals(qlmc)) &&(qlbm==null||"".equals(qlbm))){
				if(qlqdStr.contains(qlmc)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					 eg.rows.add(eo);
				}
			}
		}
		
		return eg.toString();
	}
	@Action
	public String checkData(){
		HttpServletRequest request =ActionContext.getActionContext().getHttpServletRequest();
		ExtGrid eg = new ExtGrid();
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		int total= this.jdbcTemplate.queryForInt("select count(*) from Item01 where "+whereString);
		eg.setTotal(total);
		
		Records rds = this.recordDao.queryRecord("ITEM01", whereString,"item0102",pageIndex*pageSize,pageSize);
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject(); 
			eo.add("id", ird.getObjectId());
			eo.add("jcsxmc", ird.get("ITEM0101"));//检查事项名称
			String sxId=ird.get("ITEM00").toString();
			//获取有效企业数
			//String sql="select  o1.org_name  orgName,i1.item0101 itemName,o4.org0403 from  org04 o4,org01 o1,item01 i1 where  o4.org0401='"+sxId+"'  and o1.org00 =o4.parentid and i1.item00=o4.org0401 and i1.item0190='1' and o1.org0199='0'";
			String sql = "select count(*) from org01 a inner join org04 b  on a.org00 = b.parentid and b.org0401 = '"+ sxId +"'";
			int intCode= this.jdbcTemplate.queryForInt(sql);
			eo.add("sxId", ird.get("ITEM00"));
			eo.add("sxfl", ird.get("ITEM0102",DmCodetables.class).getCaption());//事项业务分类
			eo.add("ccdx", ird.get("ITEM0103")==null?"":ird.get("ITEM0103",DmCodetables.class).getCaption());//抽查对象
			eo.add("cjfs", ird.get("ITEM0190").toString().equals("0")?"自动":"手动");
			eo.add("orgNum", intCode);
			eg.rows.add(eo);
		}
		return eg.toString();
	}
 
	/**
	 * 获取下拉列表里面的值(事项业务分类)
	 * @author liupx
	 * @return
	 */
	@Action
	public String getItemFL(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.cid,t.caption  from dm_codetable_data t where t.codetablename='ZDY02'");
		ExtObjectCollection eoc = new ExtObjectCollection();
		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("cid"));
			eo.add("text", ire.get("caption"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	/**
	 * 获取下拉列表里面的值(抽查对象)
	 * @author liupx
	 * @return
	 */
	@Action
	public String getItemCCDX(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.cid,t.caption  from dm_codetable_data t where t.codetablename='ZDY04' ORDER BY  PINDEX ASC ");
		ExtObjectCollection eoc = new ExtObjectCollection();
		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("cid"));
			eo.add("text", ire.get("caption"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	/**
	 * 获取下拉列表里面的值(调查依据)
	 * @author liupx
	 * @return
	 */
	@Action
	public String getItemCCYJ(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.lr00,t.lr0101  from LR01 t ");
		ExtObjectCollection eoc = new ExtObjectCollection();
		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("lr00"));
			eo.add("text", ire.get("lr0101"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	
	/**
	 * 新增或修改检查事项的保存
	 * @param json
	 */
	@Action
	public String addItem(){
		ExtResultObject eor=new ExtResultObject();
		try {
			HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
			String sxmc = request.getParameter("sxmc").toString();
			String sxfl = request.getParameter("sxfl").toString();
			String dyqlqd = request.getParameter("dyqlqdid").toString();  //权力清单
			String ccdx = request.getParameter("ccdx").toString();
			String ccyj = request.getParameter("ccyj").toString(); 
			String username=userSession.getCurrentUserName();
			String id=request.getParameter("id")==null?null:request.getParameter("id").toString();
			String sql = "ITEM00= '"+id+"'";
			Records rds = this.recordDao.queryRecord("ITEM01", sql);
			String powerIds[]=dyqlqd.split(";");
			String ccyjIds[]=ccyj.split(",");
			if(rds.size() == 0) {//新增的保存
				UUID uid = UUID.randomUUID();
				IRecord red =this.recordDao.createNew("ITEM01",uid, uid);
				red.put("ITEM0101", sxmc);				//事项名称
				red.put("ITEM0102", sxfl);				//事项业务分类	
				red.put("ITEM0103", ccdx);				//抽查对象
				red.put("ITEM0199", "0");				//状态
				red.put("ITEM0193", username);			//建立人
				red.put("ITEM0191", new Date());					//创建时间
				this.recordDao.saveObject(red);
				for(String powerId:powerIds){			//对应检查权利清单
					UUID cuid = UUID.randomUUID();
					IRecord cred =this.recordDao.createNew("ITEM02",cuid,cuid);
					cred.put("PARENTID", uid);				 
					cred.put("ITEM0201", powerId);				 
					this.recordDao.saveObject(cred);
				}
				for(String ccyjId:ccyjIds){					//抽查依据
					if(StringUtils.isNotEmpty(ccyjId)){
						UUID cuid = UUID.randomUUID();
						IRecord cred =this.recordDao.createNew("ITEM03",cuid,cuid);
						cred.put("PARENTID", uid);				 
						cred.put("ITEM0301", ccyjId);				 
						this.recordDao.saveObject(cred);
					}
					
				}
				eor.add("inf", "true");
			}else{//编辑的保存
				rds.get(0).put("ITEM0101", sxmc);
				rds.get(0).put("ITEM0102", sxfl);
				rds.get(0).put("ITEM0103", ccdx);
				this.recordDao.saveObject(rds.get(0));
				UUID uid=rds.get(0).getObjectId();
				
				//先删除原有的关联的对应检查权利清单记录。
				String qlqdSql="delete  item02 t where t.parentid='"+uid+"'";
				this.jdbcTemplate.execute(qlqdSql);
				if(powerIds.length>0){
					for(String powerId:powerIds){				//对应检查权利清单
						UUID cuid = UUID.randomUUID();
						IRecord cred =this.recordDao.createNew("ITEM02",cuid,cuid);
						cred.put("PARENTID", uid);				 
						cred.put("ITEM0201", powerId);				 
						this.recordDao.saveObject(cred);
					}
				}
				String ccyjSql="delete  item03 t where t.parentid='"+uid+"'";
				this.jdbcTemplate.execute(ccyjSql);
				//先删除原有的关联的对应的抽查依据的记录
				if(ccyjIds.length>0){
					for(String ccyjId:ccyjIds){					//抽查依据
						UUID cuid = UUID.randomUUID();
						IRecord cred =this.recordDao.createNew("ITEM03",cuid,cuid);
						cred.put("PARENTID", uid);				 
						cred.put("ITEM0301", ccyjId);				 
						this.recordDao.saveObject(cred);
					}
				}
				eor.add("inf", "true");
			}
		} catch (Exception e) {
			eor.add("inf", "false");
			e.printStackTrace();
		}
		return eor.toString();
	}
	/**
	 * 删除选中的检查事项
	 * @param ID 
	 */
	@Action 
	public String deleteItem(){
		ExtResultObject eor = new ExtResultObject();
		try {
			String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");
			this.recordDao.deleteObject("ITEM01", UUID.fromString(id));
			eor.add("inf", "true");
		} catch (Exception e) {
			e.printStackTrace();
			eor.add("inf", "false");
		}
		return eor.toString();
	}
	/**
	 * 编辑选中的检查事项
	 * @param
	 */
	@Action
	public String editItem(){
		ExtFormObject form = new ExtFormObject();
		HttpServletRequest request=ActionContext.getActionContext().getHttpServletRequest();
		String id=request.getParameter("id")==null?null:request.getParameter("id").toString();
		String sql = "ITEM00= '"+id+"'";
		Records rds = this.recordDao.queryRecord("ITEM01", sql);
		if(rds.size() ==0){
			form.add("form", "信息获取错误！");
		}else{
			IRecord ird=rds.get(0);
			form.add("id", ird.get("ITEM00"));
			form.add("sxmc", ird.get("ITEM0101"));										//事项名称
			form.add("sxfl", ird.get("ITEM0102",DmCodetables.class).getId());			//事项业务分类
			form.add("ccdx", ird.get("ITEM0103",DmCodetables.class).getId());			//抽查对象Id
			 
			List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select q.q00,q.q0103　from Q01 q, Item01 I1, Item02 I2 where  I1.Item00=I2.PARENTID and  I2.ITEM0201=q.q00 and I1.Item00='"+ird.get("ITEM00")+"'");
			
			String qlqdid="";
			String qlqdName="";
			for(Map<String, Object> mp:ndlist){
				 
				qlqdid+=mp.get("q00")+";";
				qlqdName+=mp.get("q0103")+";";
			}
			form.add("dyqlqdid", qlqdid.substring(0,qlqdid.length()-1));
			form.add("dyqlqdName", qlqdName.substring(0,qlqdName.length()-1));
			
		}
		return form.toString();
	}
	/**
	 * 提交（只是改变状态）
	 * @author liupx
	 */
	@Action 
	public String submitItem(){
		ExtResultObject eor = new ExtResultObject();
		try {
			HttpServletRequest request =ActionContext.getActionContext().getHttpServletRequest();
			String id=request.getParameter("id")==null?null:request.getParameter("id").toString();
			String sql = "ITEM00= '"+id+"'";
			Records rds = this.recordDao.queryRecord("ITEM01", sql);
			IRecord red=rds.get(0);
			red.put("ITEM0199", "1");				//改变状态
			this.recordDao.saveObject(red);
		} catch (Exception e) {
			e.printStackTrace();
			eor.add("inf", "false"); 
		}
		eor.add("inf", "true"); 
		return eor.toString();
		
	}
	
	/**
	 * 废弃事项
	 * @author liupx
	 * 
	 */
	@Action
	public String quitItem(){
		ExtResultObject eor = new ExtResultObject();
		try {
			HttpServletRequest request =ActionContext.getActionContext().getHttpServletRequest();
			String id=request.getParameter("id")==null?null:request.getParameter("id").toString();
			String sql = "ITEM00= '"+id+"'";
			Records rds = this.recordDao.queryRecord("ITEM01", sql);
			IRecord red=rds.get(0);
			red.put("ITEM0199", "2");				//改变状态 2 代表废弃。
			this.recordDao.saveObject(red);
		} catch (Exception e) {
			e.printStackTrace();
			eor.add("inf", "false"); 
		}
		eor.add("inf", "true"); 
		return eor.toString();
	}
	
	
	 
	/**
	 * 企业责任查询
	 * 获取datgrid数据
	 * @return
	 */
	
	@Action
	public String getCompanyQData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String qymc = request.getParameter("qymc")==null?null:request.getParameter("qymc").toString();
		String jgdm = request.getParameter("jgdm")==null?null:request.getParameter("jgdm").toString();
		//String jgxydm = request.getParameter("jgxydm")==null?null:request.getParameter("jgxydm").toString();//机构信用代码未确定
		String jcsx = request.getParameter("jcsx")==null?null:request.getParameter("jcsx").toString();
		String qlbm = request.getParameter("qlbm")==null?null:request.getParameter("qlbm").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		
		if(qymc!=null&&!"".equals(qymc)){
			whereString +=" and ORG_NAME like '%"+qymc+"%'";
		}
		if(jgdm!=null&&!"".equals(jgdm)){
			whereString +=" and ORG_CODE like '%"+jgdm+"%'";
		}
		ExtGrid eg = new ExtGrid();
		Records rds = this.recordDao.queryRecord("ORG01", whereString,"ORG_CODE",pageIndex*pageSize,pageSize);
		int total= this.jdbcTemplate.queryForInt("select count(*) from org01 where "+whereString);
		eg.setTotal(total);
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("qymc", ird.get("ORG_NAME"));
			eo.add("dm", ird.get("ORG_CODE"));
			eo.add("dz", ird.get("REG_ADDR"));
			eo.add("lxr", ird.get("LEGAL_REPRE"));
			// 涉及事项
			/*String strSJSX = "select case  when t.org0205 ='1' then '产品质量日常监督检查</br>' end  || "
					+" case  when t.org0202 ='1' then '检验机构监督检查</br>' end || "
					+" case  when t.org0203 ='1' then '3C认证日常监督检查</br>' end ||"
					+" case  when t.org0204 ='1' then '商品条码监督检查</br>' end || "
					+" case  when t.org0201 ='1' then '特种设备监督检查</br>' end sjsx"
					+" from ORG02 t where t.org00 ='" + ird.get("ORG00") +"'";*/
			
			Map<String, Object> SJSXMap = null;
			String strSql="select i.item0101 sjsx from ORG04 o,ITEM01 i  where i.item00=o.org0401 AND O.PARENTID='"+ird.get("ORG00")+"'";
 		
			try {
				SJSXMap = this.jdbcTemplate.queryForMap(strSql);
				eo.add("sjjcsx", SJSXMap.get("sjsx"));
				String sd=SJSXMap.get("sjsx").toString();
				if((jcsx!=null&&!"".equals(jcsx))&&!sd.contains(jcsx)){
				  continue;
				}
			} catch (DataAccessException e) {
				if(jcsx==null||"".equals(jcsx)){
					eo.add("sjjcsx", "");
				}else
				   continue;
			}
			
			//涉及的权力清单
			String strQLQD = "select case  when t.org0205 ='1' then '5;' end  || "
					+" case  when t.org0202 ='1' then '2;' end || "
					+" case  when t.org0203 ='1' then '3;' end ||"
					+" case  when t.org0204 ='1' then '4;' end || "
					+" case  when t.org0201 ='1' then '1;' end qlqd"
					+" from ORG02 t where t.org00 ='" + ird.get("ORG00") +"'";
			Map<String, Object> QLQDMap = null;
			List<Map<String, Object>>  ls=null;
			try {
				QLQDMap = this.jdbcTemplate.queryForMap(strQLQD);
				//涉及事项所属类别id
				String  qlqd = QLQDMap.get("qlqd").toString();
				String ids[]=qlqd.split(";");
				String s="";
				for(int x=0;x<ids.length;x++){
					if(x!=ids.length-1){
						s+="'"+ids[x]+"',";
					}else{
						s+="'"+ids[x]+"'";
					}
				} 
				String sql="select distinct t.item00 from Item01 t where t.item0102 in("+s+")";
				ls= this.jdbcTemplate.queryForList(sql);
				
				//
				String d="";
				for(int x=0;x<ls.size();x++){
					if(x!=ls.size()-1){
						d+="'"+ls.get(x).get("item00").toString()+"',";
					}else{
						d+="'"+ls.get(x).get("item00").toString()+"'";
					}
				}
				sql="select distinct t.item0201 as  qid from ITEM02 t  where t.parentid in("+d+")";
				ls= this.jdbcTemplate.queryForList(sql);
				
				//联合查询
				String m="";
				for(int x=0;x<ls.size();x++){
					if(x!=ls.size()-1){
						m+="'"+ls.get(x).get("qid").toString()+"',";
					}else{
						m+="'"+ls.get(x).get("qid").toString()+"'";
					}
				}
				sql="select  q.q0101,q.q0102,q.q0103 from Q01 q  where q.q00 in("+m+")";
				ls= this.jdbcTemplate.queryForList(sql);
				String str="";
				String StrIBianMa="";
				for(Map<String,Object> each:ls){
					str+=each.get("q0101")+"&nbsp;&nbsp;"+each.get("q0103")+"</br>";
					StrIBianMa+=each.get("q0101")+"</br>";
				}
				if((qlbm==null||"".equals(qlbm))||(qlbm!=null&&!"".equals(qlbm)&&StrIBianMa.contains(qlbm))){
					eo.add("sjqlqd", str);
					eg.rows.add(eo);
				} 
				
			} catch (DataAccessException e) {
				eo.add("sjqlqd", "");
				if(qlbm==null||"".equals(qlbm)){
					eg.rows.add(eo);
				} 
			}
			
		}
		return eg.toString();
	}
	@Action
	public String identifyInfo(String selected){
		ArrayList<JSONObject> errorLs =new ArrayList<JSONObject>();
		MultipartHttpServletRequest request = (MultipartHttpServletRequest)ActionContext.getActionContext().getHttpServletRequest();
		 //上传附件input的name
		String[] arr = request.getFileFields();  
		String title = arr[0];   
		FileInputStream fs;
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
		//全部正确之后再进行其他值的校验。	
		int errorNum=0;
		for(int i=1;i<rows;i++){
			int m=0;
			int n=0;
			int z=0;
			//组织机构代码
			String orgCode=cells.get(i,1).getStringValue()==null?"": cells.get(i,1).getStringValue().trim();
			orgCode=orgCode.replace("-", "");
			//企业名称
			String orgName=cells.get(i,2).getStringValue()==null?"": cells.get(i,2).getStringValue().trim();
			//注册地区划代码
			String orgAddressCode=cells.get(i,4).getStringValue()==null?"": cells.get(i,4).getStringValue().trim();
			//注册区县
			String city=cells.get(i,5).getStringValue()==null?"": cells.get(i,5).getStringValue().trim();
			//生产地区划代码
			String yieldlyCode=cells.get(i,7).getStringValue()==null?"": cells.get(i,7).getStringValue().trim();
			
			//校验组织机构代码
			if(StringUtils.isNotBlank(orgCode)&&(orgCode.length()==9||orgCode.length()==18)){
				if(orgCode.length()==18){
					orgCode=orgCode.substring(8,17);
				} 
				int orgSize= this.jdbcTemplate.queryForInt("select count(*)　from Org01  where  ORG_CODE='"+orgCode+"'");
				if(orgSize<1){
					m=1;
				} 
			}else{
				m=1;
			}
			//校验注册区划代码
			int zcSize= this.jdbcTemplate.queryForInt("select count(*) from DM_CODETABLE_DATA where  codetablename='DB064' and cid='"+orgAddressCode+"'");
			if(zcSize<1){
				n=1;
			}
			//校验生产区划代码
			int	scSize= this.jdbcTemplate.queryForInt("select count(*) from DM_CODETABLE_DATA   where  codetablename='DB064' and cid='"+yieldlyCode+"'");
			if(scSize<1){
				z=1;
			}
			if(this.jude(m,n,z)){
				errorNum++;							//错误数量++
				HashMap<String,String> sub=new HashMap<String,String>();
				sub.put("errorIndex", String.valueOf(errorNum));
				sub.put("orgName", orgName);
				sub.put("city", city);
				sub.put("code", orgCode);
				sub.put("orgAddressCode",orgAddressCode);
				sub.put("yieldlyCode",yieldlyCode);
				String error="";
				if(m==1){
					sub.put("code", "<label style=\"color:red\";>"+orgCode+"</label>");
					error+="组织机构代码错误;";
				}
				if(n==1){
					sub.put("orgAddressCode", "<label style=\"color:red\";>"+orgAddressCode+"</label>");
					error+="注册地区划代码错误;";
				}
				if(z==1){
					sub.put("yieldlyCode", "<label style=\"color:red\";>"+yieldlyCode+"</label>");
					error+="生产地区划代码错误;";
				}
				sub.put("errorInfo", error);
				JSONObject jsonObject = JSONObject.fromObject(sub);
				errorLs.add(jsonObject);
			}
		}
		Double s=Double.valueOf(errorNum)/Double.valueOf(rows-1);
		DecimalFormat df=new DecimalFormat("0.00");
		HashMap<String, String> mp=new HashMap<String, String>();
		mp.put("content",errorLs.toString());
		mp.put("percent",df.format(s*100)+"%"); 
		mp.put("errorNum",String.valueOf(errorNum));
		mp.put("content",errorLs.toString());
		JSONObject jsonObject = JSONObject.fromObject(mp);
		return jsonObject.toString();
	}
	
	@Action
	public String impOrgInfo(String fileName,String flag,String selectId){
		//flag为0 说明没有错误。
		String info="";
		try {
			fileName = URLDecoder.decode(fileName,"utf-8");
			MultipartHttpServletRequest request = (MultipartHttpServletRequest)ActionContext.getActionContext().getHttpServletRequest();
			String[] arr = request.getFileFields();
			String title = arr[0];                        //上传附件input的name
			FileInputStream fs = (FileInputStream) request.getFileInputStream(title);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
			String time=sdf.format(new Date());
			//把上传的文件保存到服务器。
			this.saveUpload(fs,fileName,time);
			fs = (FileInputStream) request.getFileInputStream(title);
            if("0".equals(flag)){
            	info=this.intoLibrary(fs,fileName,selectId,time);//入库
    		}else{ 
    			//存在错误信息的话 把校验的错误信息写到excel并存入服务器，正确的入库。
    			info=this.writeErrorAndIntoLibrary(fs,fileName,selectId,time);
    		}	
            String errorPath="/upload/error/"+fileName.substring(0, fileName.indexOf("."))+"_error_"+time+fileName.substring(fileName.indexOf("."),fileName.length());
            String filePath="/upload/uploadFile/"+fileName.substring(0, fileName.indexOf("."))+"_"+time+fileName.substring(fileName.indexOf("."),fileName.length());;
            if("0".equals(flag)){
            	errorPath="";
            }
			//写入日志
			UUID cuid = UUID.randomUUID();
			IRecord log =this.recordDao.createNew("LOG01",cuid,cuid);
			log.put("LOG0101", fileName);	//表格名称
			log.put("LOG0102", this.userSession.getCurrentUserName());
			log.put("LOG0103", this.userSession.getCurrentUserId());
			log.put("LOG0104", new Date());
			log.put("LOG0105", filePath);
			log.put("LOG0106", errorPath);
			String s[]=info.split(";");
			log.put("LOG0107", s[1]);		//插入数量
			log.put("LOG0108", s[2]);		//更新数量
			log.put("LOG0109", s[3]);		//错误数量
			log.put("LOG0110", s[0]);		//插入数量
			this.recordDao.saveObject(log);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		return info;
	}
	
	private String writeErrorAndIntoLibrary(FileInputStream fs, String fileName,String selectId,String time) throws Exception {
	         Workbook designer = new Workbook(fs);
			 Cells cells = designer.getWorksheets().get(0).getCells();
			 //写入到errorExcel
			 Workbook wb = new Workbook();
	         Worksheet worksheet = wb.getWorksheets().get(0);
	         worksheet.setName("导入失败记录");
	         worksheet.autoFitColumns();
	         RowCollection row = worksheet.getCells().getRows();
	         int styleIndex = wb.getStyles().add();
	         Style style = wb.getStyles().get(styleIndex);
	         style.setTextWrapped(false);
	         
	         style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
	         style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
	         style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
	         style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
	         style.setHorizontalAlignment(TextAlignmentType.CENTER);
	         int styleIndexError = wb.getStyles().add();
	         Style styleError = wb.getStyles().get(styleIndexError);
	         
	         styleError.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
	         styleError.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
	         styleError.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
	         styleError.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
	         styleError.setTextWrapped(false);
	         styleError.getFont().setColor(Color.getRed());
	         styleError.setHorizontalAlignment(TextAlignmentType.CENTER);
	         String titles[]={"事项名称","组织机构代码","企业名称","注册地址","注册地区划代码","注册区县","生产地址","生产地区划代码","联系人","联系电话","风险等级","子码"};
	         // 写入表头
	         for (int ti = 0; ti < titles.length; ti++) {
					Row R = row.get(0);
					Cell cell = R.get(ti);
					cell.setValue(titles[ti]);
					cell.setStyle(style);
	         }
	         int rows=cells.getMaxDataRow()+1;
	         int stratRow = 0;
	         int errorNum=0;
	         int insertNum=0;
	         int updateNum=0;
	         for(int i=1;i<rows;i++){
					try {
						int m=0;
						int n=0;
						int z=0;
						String itemName="";
						if(!selectId.contains(",")){
							itemName=cells.get(i,0).getStringValue()==null?"": cells.get(i,0).getStringValue().trim();
						}
						//组织机构代码
						String orgCode=cells.get(i,1).getStringValue()==null?"": cells.get(i,1).getStringValue().trim();
						//企业名称
						String orgName=cells.get(i,2).getValue()==null?"": cells.get(i,2).getStringValue().trim();
						orgCode=orgCode.replace("-", "");
						//企业地址
						String orgAddress=cells.get(i,3).getStringValue()==null?"": cells.get(i,3).getStringValue().trim();
						//注册地区划代码
						String orgAddressCode=cells.get(i,4).getStringValue()==null?"": cells.get(i,4).getStringValue().trim();
						//注册区县
						String city=cells.get(i,5).getStringValue()==null?"": cells.get(i,5).getStringValue().trim();
						//生产地址
						String yieldlyAddress=cells.get(i,6).getStringValue()==null?"": cells.get(i,6).getStringValue().trim();
						//生产地区划代码
						String yieldlyCode=cells.get(i,7).getStringValue()==null?"": cells.get(i,7).getStringValue().trim();
						//联系人
						String linkman=cells.get(i,8).getStringValue()==null?"": cells.get(i,8).getStringValue().trim();
						//联系电话
						String telPhone=cells.get(i,9).getStringValue()==null?"": cells.get(i,9).getStringValue().trim();
						//风险等级
						String riskLevel=cells.get(i,10).getStringValue()==null?"": cells.get(i,10).getStringValue().trim();
						//子码
						String subCode=cells.get(i,11).getStringValue()==null?"": cells.get(i,11).getStringValue().trim();
						if(StringUtils.isNotBlank(orgCode)&&(orgCode.length()==9||orgCode.length()==18)){
							if(orgCode.length()==18){
								orgCode=orgCode.substring(8,17);
							} 
							int orgSize= this.jdbcTemplate.queryForInt("select count(*)　from Org01  where  ORG_CODE='"+orgCode+"'");
							if(orgSize<1){
								m=1;
							}else{
								 //更新到org01里里面去
								 String sql="update org01 set credit_code='"+orgCode+"' where org_code='"+orgCode+"'";
								 this.jdbcTemplate.execute(sql);
							} 
						}else{
							m=1;
						}
						int orgSize= this.jdbcTemplate.queryForInt("select count(*)　from Org01  where  ORG_CODE='"+orgCode+"'");
						if(orgSize<1){
							m=1;
						}
						int citySize= this.jdbcTemplate.queryForInt("select count(*)　from DM_CODETABLE_DATA o where  o.cid='"+orgAddressCode+"'");
						if(citySize<1){
							n=1;
						}
						if(StringUtils.isNotBlank(yieldlyCode)){
							//校验生产区划代码
							int scSize= this.jdbcTemplate.queryForInt("select count(*)　from DM_CODETABLE_DATA o where  o.cid='"+yieldlyCode+"'");
							if(scSize<1){
								z=1;
							}
						}else{
							z=1;
						}
						if(this.jude(m,n,z)){//都为1 说明组织区域代码,注册区县代码,生产区划代码都含有错误。 
							errorNum++;
							//把失败的写入到服务的excel中；
						    ++stratRow;
						    Row everyRow = row.get(stratRow);
						    for(int w=0;w<titles.length;w++){
						    	Cell cell = everyRow.get(w);
						    	if(w==0){
						    		cell.setValue(itemName);
						            cell.setStyle(style);
						    	}else if(w==1){
						    		cell.setValue(orgCode);
						    		if(m==1){
						    			cell.setStyle(styleError);
						    		}else{
						    			cell.setStyle(style);
						    		}
						    	}else if(w==2){
						    		cell.setValue(orgName);
						            cell.setStyle(style);
						    	}else if(w==3){
						    		cell.setValue(orgAddress);
						            cell.setStyle(style);
						    	}else if(w==4){
						    		cell.setValue(orgAddressCode);
						    		if(n==1){
						    			cell.setStyle(styleError);
						    		}else{
						    			cell.setStyle(style);
						    		}
						    	}else if(w==5){
						    		cell.setValue(city);
						            cell.setStyle(style);
						    	}else if(w==6){
						    		cell.setValue(yieldlyAddress);
						            cell.setStyle(style);
						    	}else if(w==7){
						    		cell.setValue(yieldlyCode);
						    		if(z==1){
						    			cell.setStyle(styleError);
						    		}else{
						    			cell.setStyle(style);
						    		}
						    	}else if(w==8){
						    		cell.setValue(linkman);
						            cell.setStyle(style);
						    	}else if(w==9){
						    		cell.setValue(telPhone);
						            cell.setStyle(style);
						    	}else if(w==10){
						    		cell.setValue(riskLevel);
						            cell.setStyle(style);
						    	}else if(w==11){
						    		cell.setValue(subCode);
						            cell.setStyle(style);
						    	} 
						    }
						}
						if(m==0&&n==0&&z==0){
							List<Map<String,Object>> orgList= this.jdbcTemplate.queryForList("select ORG00,ORG_NAME　from Org01  where  ORG_CODE='"+orgCode+"'");
							String orgId=orgList.get(0).get("ORG00").toString();
							
							String ss[]=selectId.split(",");
						    for(String sub:ss){
						    	String sql="select ORG00 from org04  where PARENTID='"+orgId+"' and ORG0401='"+sub+"' and  ORG0408='"+subCode+"'";
						    	if(!StringUtils.isNotEmpty(subCode)){
						    		sql="select ORG00 from org04  where PARENTID='"+orgId+"' and ORG0401='"+sub+"' and  ORG0408 is null";
						    	}
								if("高".equals(riskLevel)){
									riskLevel="1";
								}else if("中".equals(riskLevel)){
									riskLevel="2";
								}else if("低".equals(riskLevel)){
									riskLevel="3";
								}
								
								UUID cuid = UUID.randomUUID();
								IRecord cred =this.recordDao.createNew("ORG04",cuid,UUID.fromString(orgId));
								cred.put("PARENTID", orgId);
								cred.put("ORG0401", sub);
								cred.put("ORG0402", riskLevel);  //风险等级
								cred.put("ORG0403",new Date());
								cred.put("ORG0404", yieldlyAddress);			//生产地址
								cred.put("ORG0405", yieldlyCode);			//生产地区划代码
								cred.put("ORG0406", linkman);			//联系人
								cred.put("ORG0407", telPhone);			//联系电话
								cred.put("ORG0408", subCode);			//子码
								cred.put("ORG0409", orgCode+subCode);			//总码
								List<Map<String, Object>> list= this.jdbcTemplate.queryForList(sql);
								if(!list.isEmpty()){//存在更新org04表；
									SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String updateTime=sdf.format(new Date());
									String upSql="update org04 set org0402='"+riskLevel+"',org0403=timestamp'"+updateTime+"',org0404='"+yieldlyAddress+"', org0405='"+yieldlyCode+"',org0406='"+linkman+"',org0407='"+telPhone+"' where PARENTID='"+orgId+"' and ORG0401='"+sub+"'";
									if(!StringUtils.isNotBlank(subCode)){//subcode 为空
										upSql+=" and ORG0408 is null ";
									}else{//非空
										upSql+=" and ORG0408='"+sub+"'";
									}
									this.jdbcTemplate.execute(upSql);
									updateNum++;
								}else{//插入(新增)
									this.recordDao.saveObject(cred);
									insertNum++;
								}
						    }
						}
					} catch (Exception e) {
						e.printStackTrace();
						//errorNum++;
						//continue;
					}
			}
			String path="/upload/error/"+fileName.substring(0, fileName.indexOf("."))+"_error_"+time+fileName.substring(fileName.indexOf("."),fileName.length());
			String filePath = System.getProperty("resourceFiles.location")+path;
			wb.save(filePath, new XlsSaveOptions()); // 保存错误文件
			int size=selectId.split(",").length; 
			return (rows-1)*size+";"+insertNum+";"+updateNum+";"+errorNum*size;
	}
	private String intoLibrary(FileInputStream fs,String fileName,String selectId,String time) throws Exception {
			Workbook designer = new Workbook(fs);
			Cells cells = designer.getWorksheets().get(0).getCells();
			int rows=cells.getMaxDataRow()+1;
			// 服务器路径
	        String servicePath = this.getServicePath();
	        String licFilePath = servicePath + File.separator + "license" + File.separator+ "Aspose.Total.Product.Family.lic";
	        License cellLic = new License();
	        cellLic.setLicense(licFilePath);
	        int insertNum=0;
	        int updateNum=0;
	        for(int i=1;i<rows;i++){
	        	//事项名称
	        	String itemName=cells.get(i,0).getValue()==null?"": cells.get(i,0).getValue().toString().trim();
	        	//组织机构代码
				String orgCode=cells.get(i,1).getStringValue()==null?"": cells.get(i,1).getStringValue().trim();
				orgCode=orgCode.replace("-", ""); 
				//企业名称
				String orgName=cells.get(i,2).getValue()==null?"": cells.get(i,2).getValue().toString().trim();
				
				//注册地区划代码
				String orgAddressCode=cells.get(i,4).getStringValue()==null?"": cells.get(i,4).getStringValue().trim();
				orgAddressCode=orgAddressCode.split("\\.")[0];
				//企业地址
				String orgAddress=cells.get(i,3).getValue()==null?"": cells.get(i,3).getValue().toString();
				//注册区县
				String city=cells.get(i,5).getValue()==null?"": cells.get(i,5).getValue().toString();
				//生产地址
				String yieldlyAddress=cells.get(i,6).getValue()==null?"": cells.get(i,6).getValue().toString();
				//生产地区划代码
				String yieldlyCode=cells.get(i,7).getStringValue()==null?"": cells.get(i,7).getStringValue().trim();
				yieldlyCode=yieldlyCode.split("\\.")[0];
				//联系人
				String linkman=cells.get(i,8).getValue()==null?"": cells.get(i,8).getValue().toString();
				//联系电话
				String telPhone=cells.get(i,9).getValue()==null?"": cells.get(i,9).getValue().toString();
				//风险等级
				String riskLevel=cells.get(i,10).getValue()==null?"": cells.get(i,10).getValue().toString();
				//子码
				String subCode=cells.get(i,11).getValue()==null?"": cells.get(i,11).getValue().toString();
				
				if(orgCode.length()==18){
					 //更新到org01里里面去
					 String sql="update org01 set credit_code='"+orgCode+"' where org_code='"+orgCode.substring(8,17)+"'";
					 this.jdbcTemplate.execute(sql);
				}
				List<Map<String, Object>> orgList= this.jdbcTemplate.queryForList("select ORG00　from Org01  where  ORG_CODE='"+orgCode+"'");
				String orgId=orgList.get(0).get("ORG00").toString();
				if("高".equals(riskLevel)){
					riskLevel="1";
				}else if("中".equals(riskLevel)){
					riskLevel="2";
				}else if("低".equals(riskLevel)){
					riskLevel="3";
				}
				String[]ss=selectId.split(",");
				for(String sub:ss){
					UUID cuid = UUID.randomUUID();
					IRecord cred =this.recordDao.createNew("ORG04",cuid,cuid);
					cred.put("PARENTID", orgId);
					cred.put("ORG0401", sub);
					cred.put("ORG0402", riskLevel);  //风险等级
					cred.put("ORG0403",new Date());
					cred.put("ORG0404", yieldlyAddress);			//生产地址
					cred.put("ORG0405", yieldlyCode);			//生产地区划代码
					cred.put("ORG0406", linkman);			//联系人
					cred.put("ORG0407", telPhone);			//联系电话
					cred.put("ORG0408", subCode);			//子码
					cred.put("ORG0409", orgCode+subCode);			//总码
	   				String sql="select ORG00 from org04  where PARENTID='"+orgId+"' and ORG0401='"+sub+"' and  ORG0408='"+subCode+"'";
					List<Map<String, Object>> list= this.jdbcTemplate.queryForList(sql);
					if(!list.isEmpty()){//存在更新org04表；
						String upSql="update org04 set org0402='"+riskLevel+"',org0403=timestamp'"+time+"',org0404='"+yieldlyAddress+"', org0405='"+yieldlyCode+"',org0406='"+linkman+"',org0407='"+telPhone+"' where PARENTID='"+orgId+"' and ORG0401='"+sub+"'";
						if(!StringUtils.isNotBlank(subCode)){//subcode 为空
							upSql+=" and ORG0408 is null ";
						}else{//非空
							upSql+=" and ORG0408='"+sub+"'";
						}
						this.jdbcTemplate.execute(upSql);
						updateNum++;
					}else{//插入(新增)
						this.recordDao.saveObject(cred);
						insertNum++;
					}
				}
			}
	        return (rows-1)+";"+insertNum+";"+updateNum+";"+"0";
     }
	@Action
	public String getHandOrg(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String sxId = request.getParameter("sxId")==null?null:request.getParameter("sxId").toString();//事项id
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String sql="select  o1.org_name  orgName,i1.item0101 itemName,o4.org0403 insertTime from  org04 o4 ,org01 o1,item01 i1 where  o4.org0401='"+sxId+"'  and o1.org00 =o4.parentid and i1.item00=o4.org0401 and i1.item0190='1' and o1.org0199='0' and ROWNUM between "+pageIndex+" and "+pageIndex+pageSize;
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList(sql);
		ExtObjectCollection eoc = new ExtObjectCollection();
		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("orgName", ire.get("ORGNAME"));
			eo.add("itemName", ire.get("ITEMNAME"));
			eo.add("insertTime", ire.get("insertTime").toString());
			eoc.add(eo);
		}
		return eoc.toString();
	}
	//事项名称
	@Action 
	public  String getItemList(){
		ExtObjectCollection col = new ExtObjectCollection();
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.cid,t.caption  from dm_codetable_data t where t.codetablename='ZDY02'");
		ExtTreeNode eo = new ExtTreeNode();
		for(Map<String,Object> ire:ndlist){
			eo = new ExtTreeNode();
			eo.add("id", ire.get("cid"));
			eo.add("text", ire.get("caption"));
			List<Map<String, Object>> itemName= this.jdbcTemplate.queryForList("select t.item00,t.item0101  from Item01 t where t.ITEM0102='"+ire.get("cid")+"'");
			ExtTreeNode sub = new ExtTreeNode();
			ExtObjectCollection subCol = new ExtObjectCollection();
			for(Map<String, Object> subMp: itemName){
				sub = new ExtTreeNode();
				sub.setID(subMp.get("item00").toString());
				sub.setText(subMp.get("item0101").toString());
				subCol.add(sub);
			}	
			eo.add("children", subCol);
			sub.add("parentid", ire.get("cid"));
			col.add(eo);	
		}
		return col.toString();
	}
	
    @Action
    public String getLogDate() throws ParseException{
    	HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
    	int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		String sql="select LOG00,LOG0101,LOG0102,LOG0104,LOG0106,LOG0107,LOG0108,LOG0109,LOG0110 from  LOG01   where ROWNUM between "+pageIndex+" and "+pageIndex+pageSize+" ORDER BY LOG0104 desc";
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList(sql);
		ExtObjectCollection eg = new ExtObjectCollection();
		for(Map<String,Object> ird:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.get("LOG00"));
			eo.add("fileName", ird.get("LOG0101"));						//文件名
			eo.add("operUser", ird.get("LOG0102"));						//操作人
			eo.add("loadDate", sdf.format(ird.get("LOG0104")));			//导入时间 
			eo.add("path", ird.get("LOG0105")+";"+ird.get("LOG0106"));	//文件路径(上传的和生成的错误的)
			int x=Integer.valueOf(ird.get("LOG0107").toString());
			int y=Integer.valueOf(ird.get("LOG0108").toString());
			eo.add("successNum", Integer.toString(x+y));			//成功数
			eo.add("errorNum", ird.get("LOG0109"));			//失败数
			eo.add("totalNum", ird.get("LOG0110"));			//失败数
			eg.add(eo);
		}
		return eg.toString();
    }
    @Action
    public void download(String id,String flag){
		String sql="select log0101,log0105,log0106 from log01 where log00='"+id+"'";
		List<Map<String, Object>> ls= this.jdbcTemplate.queryForList(sql);
		if(ls.size()>0){
			String filePath="";
			String path="";
			String fileName=ls.get(0).get("log0101")==null?"":ls.get(0).get("log0101").toString();
			if("0".equals(flag)){
				path=ls.get(0).get("log0105")==null?"":ls.get(0).get("log0105").toString();
			}else{//下载错误的文件
				path=ls.get(0).get("log0106")==null?"":ls.get(0).get("log0106").toString();
			}
			filePath=System.getProperty("resourceFiles.location")+path;
			this.downFile(filePath, fileName);
		}
    }
    private void downFile(String filePath,String fileName){
    	try{
    		HttpServletResponse rp=ActionContext.getActionContext().getHttpServletResponse();
    		rp.setContentType("application/msexcel;charset=GBK");  					
    		rp.setHeader("Content-Disposition", "attachment;filename="+  new String( fileName.getBytes("gb2312"), "ISO8859-1" ));	// 设置响应头，控制浏览器下载该文件
    		File file=new File(filePath);											// 读取要下载的文件，保存到文件输入流
    		FileInputStream in = new FileInputStream(file);							// 创建输出流
    		OutputStream out = rp.getOutputStream();								// 创建缓冲区
    		byte buffer[] = new byte[2048];
    		int len = 0;															// 循环将输入流中的内容读取到缓冲区当中			
    		while ((len = in.read(buffer)) > 0) { 									// 输出缓冲区的内容到浏览器，实现文件下载
    			out.write(buffer, 0, len);
    		}
    		in.close();																// 关闭文件输入流
    		out.close();															// 关闭输出流
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    /**
     * 取得服务器的文件路径
     * @return 文件路径
     */
    private String getServicePath() {
        String path = System.getProperty("resourceFiles.location");
        if (StringUtils.isEmpty(path)) {
            return StringUtils.EMPTY;
        }
        File filePath = new File(path);
        if (filePath == null || !filePath.isDirectory()) {
            return StringUtils.EMPTY;
        } else {
            return filePath.getAbsolutePath();
        }
    }  
    
    
  //把上传的文件保存到服务器
  	private void saveUpload(FileInputStream fs,String fileName,String time) throws IOException{
  		String path="/upload/uploadFile/"+fileName.substring(0, fileName.indexOf("."))+"_"+time+fileName.substring(fileName.indexOf("."),fileName.length());
  		String savefilePath = System.getProperty("resourceFiles.location")+path;
  		File f=new File(savefilePath);
  		byte[] tempbytes = new byte[2048];
  		int byteread = 0;
  		FileOutputStream fos=null;
  		try {
  			fos = new FileOutputStream(f);
  			while ((byteread = fs.read(tempbytes)) != -1) {
  				fos.write(tempbytes, 0, byteread);
  			}
  		} catch (FileNotFoundException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}finally{
  			fos.close();
  		}
  	}
  	
  	private boolean jude(int m, int n, int z) {
		if((m==1&&n==1&&z==1)||(m==1&&n==1&&z==0)||(m==1&&n==0&&z==1)||(m==0&&n==1&&z==1)||(m==1&&n==0&&z==0)||(m==0&&n==0&&z==1)||(m==0&&n==1&&z==0))
			return true;
		else
			return false;
	}
	private List<Map<String, Object>> getItemName(String selected) {
		List<Map<String, Object>> itemList= this.jdbcTemplate.queryForList("select Item0101　from Item01  where Item00 in ("+selected+")");
		return itemList;
	}
}
