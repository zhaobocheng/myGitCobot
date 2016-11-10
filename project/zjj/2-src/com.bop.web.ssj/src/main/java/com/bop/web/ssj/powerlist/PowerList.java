package com.bop.web.ssj.powerlist;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtFormObject;
import com.bop.json.ExtGrid;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class PowerList {
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
		ExtObjectCollection eoc = new ExtObjectCollection();
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
			whereString +=" and ITEM0102 = '"+sxfl+"'";			//事项分类
		}
		if(feiqi!=null&&!"".equals(feiqi)&&"2".equals(feiqi)){
			whereString +=" and ITEM0199 != '2'";			  //废弃
		}
		 
		Records rds = this.recordDao.queryRecord("ITEM01", whereString,"item0101",pageIndex*pageSize,pageSize);
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getObjectId());
			eo.add("jcsxmc", ird.get("ITEM0101"));//检查事项名称
			eo.add("sxfl", ird.get("ITEM0102",DmCodetables.class).getCaption());//事项分类
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
				qlqdStr+=ire.get("Q0101")+"&nbsp;&nbsp;"+ire.get("Q0103")+"</br>" ;
			}
			//两者都为空
			if(((qlmc==null||"".equals(qlmc))&&(qlbm==null||"".equals(qlbm)))){
				 eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
				 eoc.add(eo);
			//两者都不为空
			}else if(qlmc!=null&&!"".equals(qlmc)&&qlbm!=null&&!"".equals(qlbm)){
				if(qlqdStr.contains(qlmc)&&qlqdStr.contains(qlmc)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					eoc.add(eo);
				}
			// 名称为空 编码不为空 
			}else if((qlmc==null||"".equals(qlmc)) &&qlbm!=null&&!"".equals(qlbm)){
				if(qlqdStr.contains(qlbm)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					eoc.add(eo);
				}
			//名称不为空 编码为空	
			}else if((qlmc!=null&&!"".equals(qlmc)) &&(qlbm==null||"".equals(qlbm))){
				if(qlqdStr.contains(qlmc)){
					eo.add("qlqdStr", qlqdStr);	//权力清单编码和名称
					eoc.add(eo);
				}
			}
		}
		return eoc.toString();
	}
	/**
	 * 获取下拉列表里面的值(事项分类)
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
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.cid,t.caption  from dm_codetable_data t where t.codetablename='ZDY04'");
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
				red.put("ITEM0102", sxfl);				//事项分类		
				red.put("ITEM0103", ccdx);				//抽查对象
				red.put("ITEM0199", "0");				//状态
				red.put("ITEM0193", username);			//建立人
				red.put("ITEM0191", new Date());					//创建时间
				this.recordDao.saveObject(red);
				for(String powerId:powerIds){			//对应权力清单
					UUID cuid = UUID.randomUUID();
					IRecord cred =this.recordDao.createNew("ITEM02",cuid,cuid);
					cred.put("PARENTID", uid);				 
					cred.put("ITEM0201", powerId);				 
					this.recordDao.saveObject(cred);
				}
				for(String ccyjId:ccyjIds){					//抽查依据
					UUID cuid = UUID.randomUUID();
					IRecord cred =this.recordDao.createNew("ITEM03",cuid,cuid);
					cred.put("PARENTID", uid);				 
					cred.put("ITEM0301", ccyjId);				 
					this.recordDao.saveObject(cred);
				}
				eor.add("inf", "true");
			}else{//编辑的保存
				rds.get(0).put("ITEM0101", sxmc);
				rds.get(0).put("ITEM0102", sxfl);
				rds.get(0).put("ITEM0103", ccdx);
				this.recordDao.saveObject(rds.get(0));
				UUID uid=rds.get(0).getObjectId();
				
				//先删除原有的关联的对应权力清单记录。
				String qlqdSql="delete  item02 t where t.parentid='"+uid+"'";
				this.jdbcTemplate.execute(qlqdSql);
				if(powerIds.length>0){
					for(String powerId:powerIds){				//对应权力清单
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
			form.add("sxfl", ird.get("ITEM0102",DmCodetables.class).getId());			//事项分类
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
		String jgxydm = request.getParameter("jgxydm")==null?null:request.getParameter("jgxydm").toString();//机构信用代码未确定
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
			String strSJSX = "select case  when t.org0205 ='1' then '产品质量日常监督检查</br>' end  || "
					+" case  when t.org0202 ='1' then '检验机构监督检查</br>' end || "
					+" case  when t.org0203 ='1' then '3C认证日常监督检查</br>' end ||"
					+" case  when t.org0204 ='1' then '商品条码监督检查</br>' end || "
					+" case  when t.org0201 ='1' then '特种设备监督检查</br>' end sjsx"
					+" from ORG02 t where t.org00 ='" + ird.get("ORG00") +"'";
			Map<String, Object> SJSXMap = null;
			try {
				SJSXMap = this.jdbcTemplate.queryForMap(strSJSX);
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
}
