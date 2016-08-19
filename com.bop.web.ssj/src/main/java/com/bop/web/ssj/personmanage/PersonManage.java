package com.bop.web.ssj.personmanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridRow;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class PersonManage {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private UserSession userSession;
	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	
	/**
	 * 得到为选中人员列表数据
	 * @return
	 */
	@Action
	public String getUnSelectedGridData(String faid){
		//查询数据库
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		int pageIndex = request.getParameter("pageIndex")==null?0:Integer.parseInt(request.getParameter("pageIndex").toLowerCase());
		int pageSize = request.getParameter("pageSize")==null?20:Integer.parseInt(request.getParameter("pageSize").toLowerCase());
		Object key=request.getParameter("key");
		String zone = this.userSession.getCurrentUserZone();
		String zfnd=request.getParameter("nd");
		String whereSql = "plan0204 = '1' ";
		String sql=null,sql1="",sql2="";
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}
		
		if(zone==null||"".equals(zone)){
			whereSql += " and parentid = '"+faid+"'";
			sql="select * from plan02 t "+
					" left join (select plan0201,plan0202,count(*) as cs from plan02 t"+
					" inner join plan01 a on a.plan00=t.parentid and plan0101='"+zfnd+"' "+					
					" group by plan0201,plan0202 ) a on a.plan0201=t.plan0201 "+
					" where t.plan0204 = '1' and t.parentid='"+faid+"'";
			sql1="select count(*) from plan02 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where a.plan0101='"+zfnd+"'";	
			sql2="select sum(plan0602) from plan06 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where a.plan0101='"+zfnd+"'";
		}else{
			whereSql += "  and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
			sql="select * from plan02 t "+
					" left join (select plan0201,plan0202,count(*) as cs from plan02 t"+
					" inner join plan01 a on a.plan00=t.parentid and plan0101='"+zfnd+"' "+
					" where  t.plan0205='"+zone+"' "+
					" group by plan0201,plan0202 ) a on a.plan0201=t.plan0201 "+
					" where t.plan0204 = '1' and t.plan0205='"+zone+"' and t.parentid='"+faid+"'";
			sql1="select count(*) from plan02 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where  t.plan0205='"+zone+"' and a.plan0101='"+zfnd+"'";
			sql2="select sum(plan0602) from plan06 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where t.plan0601='"+zone+"' and a.plan0101='"+zfnd+"'";
		}
		
		if(key!=null&& !"".equals(key)){
			whereSql+=" and plan0202 like '%"+key.toString()+"%'";
			sql+=" and t.plan0202 like '%"+key.toString()+"%'";
		}

		String strSQL="SELECT * FROM  "+ 
					"(SELECT A.*, ROWNUM RN "+   
					" FROM (" +sql+") A "+   
					" WHERE ROWNUM < "+start+pageSize+" )WHERE RN >= "+ start ;
							
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(strSQL);

		
		int ryzs=this.jdbcTemplate.queryForInt(sql1);
		
		int qys=this.jdbcTemplate.queryForInt(sql2);
		double yj=qys*2/ryzs;
		
		//Records rds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205", start, pageSize);
		Records totalrds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205");
		
		ExtGrid eg = new ExtGrid();
		eg.setTotal(totalrds.size());
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){

		//for(IRecord ird:rds){
				ExtGridRow eo = new ExtGridRow();
				eo.add("id", map.get("recordid"));
				eo.add("unSeletedName", map.get("plan0202"));
				//eo.add("seletedDept", ird.get("PLAN0205",DmCodetables.class).getCaption());
				eo.add("bnyj", map.get("cs").toString()+"-"+qys+"-"+yj);
				eg.rows.add(eo);
			}
		}
		
		return eg.toString();
	}
	/**
	 * getP1ReocrdId
	 * 得到已选中人员列表数据
	 * @return
	 */
	@Action
	public String getSelectedGridData(String faid){

		//查询数据库
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		int pageIndex = request.getParameter("pageIndex")==null?0:Integer.parseInt(request.getParameter("pageIndex").toLowerCase());
		int pageSize = request.getParameter("pageSize")==null?20:Integer.parseInt(request.getParameter("pageSize").toLowerCase());
		Object key=request.getParameter("key");
		String zfnd=request.getParameter("nd");
		String zone = this.userSession.getCurrentUserZone();
		String whereSql = "plan0204 <> '1' ";
		
		String sql=null,sql1="",sql2="";;
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}
		
		if(zone==null||"".equals(zone)){
			whereSql += " and parentid = '"+faid+"'";
			sql="select * from plan02 t "+
					" left join (select plan0201,plan0202,count(*) as cs from plan02 t"+
					" inner join plan01 a on a.plan00=t.parentid and plan0101='"+zfnd+"' "+					
					" group by plan0201,plan0202 ) a on a.plan0201=t.plan0201 "+
					" where t.plan0204 <> '1' and t.parentid='"+faid+"'";
			sql1="select count(*) from plan02 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where a.plan0101='"+zfnd+"'";	
			sql2="select sum(plan0602) from plan06 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where a.plan0101='"+zfnd+"'";			
		}else{
			whereSql += "  and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
			sql="select * from plan02 t "+
					" left join (select plan0201,plan0202,count(*) as cs from plan02 t"+
					" inner join plan01 a on a.plan00=t.parentid and plan0101='"+zfnd+"' "+
					" where  t.plan0205='"+zone+"' "+
					" group by plan0201,plan0202 ) a on a.plan0201=t.plan0201 "+
					" where t.plan0204 <> '1' and t.plan0205='"+zone+"' and t.parentid='"+faid+"'";  					
			sql1="select count(*) from plan02 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where  t.plan0205='"+zone+"' and a.plan0101='"+zfnd+"'";
			sql2="select sum(plan0602) from plan06 t "+
					" inner join plan01 a on a.plan00=t.parentid "+
					" where t.plan0601='"+zone+"' and a.plan0101='"+zfnd+"'";

		}
		
		if(key!=null&& !"".equals(key)){
			whereSql+=" and plan0202 like '%"+key.toString()+"%'";
			sql+=" and t.plan0202 like '%"+key.toString()+"%'";
		}

		String strSQL="SELECT * FROM  "+ 
					"(SELECT A.*, ROWNUM RN "+   
					" FROM (" +sql+") A "+   
					" WHERE ROWNUM < "+start+pageSize+" )WHERE RN >= "+ start ;
							
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(strSQL);
		//
		 
		int ryzs=this.jdbcTemplate.queryForInt(sql1);
		
		int qys=this.jdbcTemplate.queryForInt(sql2);
		double yj=qys*2/ryzs;
		//Records rds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205", start, pageSize);		
		Records totalrds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205");
		ExtGrid eg = new ExtGrid();
		eg.setTotal(totalrds.size());

		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){

		//for(IRecord ird:rds){
				ExtGridRow eo = new ExtGridRow();
				eo.add("id", map.get("recordid"));
				eo.add("seletedName", map.get("plan0202"));
				//eo.add("seletedDept", ird.get("PLAN0205",DmCodetables.class).getCaption());
				eo.add("bnyj", map.get("cs").toString()+"-"+qys+"-"+yj);
				eg.rows.add(eo);
			}
		}
		return eg.toString();
	}
	/**
	 * 根据方案列表时间得到对应的方案ID
	 */
	private String getP1ReocrdId(String faid){
		String year = faid.substring(0, 4);
		String month = faid.substring(4);
		Records p1rds = this.recordDao.queryRecord("PLAN01", "plan0101 ="+year+" and plan0102 ="+month);
		return p1rds.get(0).getRecordId().toString();
	}


	/**
	 * 得到方案列表数据
	 * @return
	 */
	@Action
	public String getFALBData(String zfnd,String zfzt){

		ExtObjectCollection eoc = new ExtObjectCollection();
		String wheresql = " t.PLAN0105 = 1 and tt.plan0301='"+this.userSession.getCurrentUserZone()+"' ";
		String orderby =" order by t.plan0102 desc";
		
		if(zfnd!=null&&!"".equals(zfnd)){
			wheresql += " and t.plan0101 = "+zfnd;
		}
		if(zfzt!=null&&!"".equals(zfzt)){
			if("0".equals(zfzt)){
				wheresql += " and tt.plan0302 = "+zfzt;
				orderby= " order by t.plan0102 ";
			}else{
				wheresql += " and tt.plan0302 <> 0";
			}
		}
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList("select * from plan01 t  left join plan03 tt on tt.parentid = t.plan00 where "+wheresql+orderby);
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		for(Map<String,Object> rd :list){
			ExtObject eo = new ExtObject();
			eo.add("id", rd.get("plan00"));
			eo.add("zftime",rd.get("PLAN0101").toString()+"0"+rd.get("PLAN0102").toString());
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
			eo.add("famc",rd.get("PLAN0107").toString());
			eo.add("zt", "0".equals(rd.get("PLAN0302").toString())?"未上报":"已上报");
			
			if("0".equals(rd.get("PLAN0302").toString())){
				eo.add("cz", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\"  onclick=\"sbRow()\">选择人员</a>");
			}else{
				eo.add("cz", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\"  onclick=\"sbRow('ll')\">浏览人员</a>");
			}
			
			eoc.add(eo);
		}
		return eoc.toString();
	}
	
	
	/**
	 * 得到下拉时间列表数据
	 * @return
	 */
	@Action
	public String getZfcData(){
		Records rds = this.recordDao.queryRecord("PLAN01", "PLAN0105 = 1", "PLAN0102 asc");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(IRecord ire:rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.getObjectId());
			eo.add("text", ire.get("PLAN0101")+"年"+ire.get("PLAN0102")+"月");
			//eo.add("id", ire.get("PLAN0101").toString()+ire.get("PLAN0102"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	
	/**
	 * 得到下拉框数据
	 * @return
	 */
	@Action
	public String getCBData(String flag){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select distinct t.plan0101 nd from plan01 t");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("nd"));
			eo.add("text", ire.get("nd"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	

	/**
	 * 人员页面左移右移的改动
	 * @param zfid
	 * @return
	 */
	@Action
	public String personChange(String zfid){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		String fro = request.getParameter("fro");

		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			IRecord ird = this.recordDao.getRecord("PLAN02", UUID.fromString(jsonObject.get("id").toString()));
			
			if("leftToright".equals(fro)){
				ird.put("PLAN0204", "0");
			}else{
				ird.put("PLAN0204", "1");
			}
			this.recordDao.saveObject(ird);
		}
		return "success";
	}
	/**
	 * 处理上报人员逻辑
	 * @return
	 */
	@Action
	public String upShow(String faid){
		String zone = this.userSession.getCurrentUserZone();
		List<String> unSelectList = new ArrayList<String>();
		ExtResultObject ero = new ExtResultObject();
		
		if(zone==null||"".equals(zone)){
			String qureySql= "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid <> '110000'";
			List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(qureySql);
			for(Map<String,Object> map:resultList){
				String returnsql = this.changeUpshow(faid, map.get("cid").toString());
				if(!"ups".equals(returnsql) &&!"success".equals(returnsql)){
					unSelectList.add(returnsql);
				}
			}
			if(unSelectList.size()>0){
				ero.add("text", unSelectList.toString());
				ero.add("flag", "allu");
			}else{
				ero.add("flag", "alls");
			}
			return ero.toString();
		}else{
			String returnsql = this.changeUpshow(faid, zone);
			if("success".equals(returnsql)){
				ero.add("flag", "success");
			}else if("ups".equals(returnsql)){
				ero.add("flag", "ups");
			}else{
				ero.add("flag", "select");
			}
			return ero.toString();
		}
	}
	
	private String changeUpshow(String faid,String zone){
		String upSql = "select count(*) from plan02 where plan0204 = 0 and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		
		int allUpCount = this.jdbcTemplate.queryForInt(upSql);
		if(allUpCount>0){
			String updataSql= "update plan02 set plan0204 = 2 where plan0204 = 0 and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
			this.jdbcTemplate.execute(updataSql);
			
			String updataPlan03Sql= "update plan03 set plan0302 = 1 where  parentid = '"+faid+"' and plan0301 = '"+zone+"'";
			this.jdbcTemplate.execute(updataPlan03Sql);
			
			return "success";
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				for(IRecord ire:rds){
					if(ire.get("plan0302")=="1"){
						return "ups";  //以上报
					}else{
						return zone;   //需要选择对象
					}
				}
			}
			return zone;
		}
	}

	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();
		if(zone==null||"".equals(zone)){
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0302=0");
			if(rds.size()==17){
				return "select";   //需要选择对象
			}else{
				return "ups";  //以上报
			}
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"' and plan0302 = 0");
			if(rds.size()>0){
				return "select";   //需要选择对象
			}else{
				return "ups";  //以上报
				
			}
		}
	}

}
