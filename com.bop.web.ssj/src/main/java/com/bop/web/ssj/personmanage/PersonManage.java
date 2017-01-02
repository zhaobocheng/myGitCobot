package com.bop.web.ssj.personmanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		String sql=null,ryssql="",zqysslq="";
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}

		whereSql += "  and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		sql = " select t.recordid as recordid ,t.plan0202,t.plan0204,decode(rw.rws,null,0,rw.rws) as rws,decode(ccs.qys,null,0,ccs.qys) as qys,case when rws is null then 0 else qys/rws end as bl"+
				" from plan02 t left join ( select count(*) rws,p2.plan0203,p2.plan0202  from plan02 p2 "+
				" where p2.plan0204 = 2 and p2.plan0205 ='"+zone+"' and p2.parentid in (select plan00 from plan01 p1 where p1.plan0101='"+zfnd+"') group by p2.plan0203,p2.plan0202) rw on rw.plan0203 = t.plan0203"+
				" left join (select count(*) qys,t.plan120103 ,t.plan120102  from plan1201 t  where t.parentid in ( select p12.recordid from plan12 p12 "+
				" where p12.parentid in (select plan00 from plan01 p1 where p1.plan0101='"+zfnd+"') and p12.plan1204 = '"+zone+"') group by t.plan120103 ,t.plan120102) ccs on ccs.plan120103 = t.plan0203"+
				" where t.parentid = '"+faid+"' and t.plan0205 = '"+zone+"' and t.plan0204 = 1 ";

		if(key!=null&& !"".equals(key)){
			whereSql+=" and plan0202 like '%"+key.toString()+"%'";
			sql+=" and t.plan0202 like '%"+key.toString()+"%'";
		}

		String strSQL="SELECT * FROM  "+ 
					"(SELECT A.*, ROWNUM RN "+
					" FROM (" +sql+" order by qys,plan0202 ) A "+
					" WHERE ROWNUM <= "+pageSize*(pageIndex+1)+" ) WHERE RN > "+ start ;
							
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(strSQL);

		double yj=getAveOfcompany(zone,zfnd);
		//Records rds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205", start, pageSize);
		Records totalrds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205");

		ExtGrid eg = new ExtGrid();
		eg.setTotal(totalrds.size());
		
		if(resultList.size()>0){
			for(Map<String,Object> map:resultList){

				ExtGridRow eo = new ExtGridRow();
				eo.add("id", map.get("recordid"));
				eo.add("unSeletedName", map.get("plan0202"));
				eo.add("bnyj", map.get("rws").toString()+"/"+map.get("qys").toString()+"/"+yj);
				double bl = Double.parseDouble(map.get("qys").toString());
				eo.add("ys", bl>=yj?false:true);   //true表示需要亮显或预警
				eg.rows.add(eo);
			}
		}
		return eg.toString();
	}
	/**
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

		String sql=null;
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}
		
		sql = " select t.recordid as recordid ,t.plan0202,t.plan0204,decode(rw.rws,null,0,rw.rws) as rws,decode(ccs.qys,null,0,ccs.qys) as qys,case when rws is null then 0 else qys/rws end as bl "+
				" from plan02 t left join ( select count(*) rws,p2.plan0203,p2.plan0202  from plan02 p2 "+
				" where p2.plan0204 = 2 and p2.plan0205 ='"+zone+"' and p2.parentid in (select plan00 from plan01 p1 where p1.plan0101='"+zfnd+"') group by p2.plan0203,p2.plan0202) rw on rw.plan0203 = t.plan0203"+
				" left join (select count(*) qys,t.plan120103 ,t.plan120102  from plan1201 t  where t.parentid in ( select p12.recordid from plan12 p12 "+
				" where p12.parentid in (select plan00 from plan01 p1 where p1.plan0101='"+zfnd+"') and p12.plan1204 = '"+zone+"') group by t.plan120103 ,t.plan120102) ccs on ccs.plan120103 = t.plan0203"+
				" where t.parentid = '"+faid+"' and t.plan0205 = '"+zone+"' and t.plan0204 <> 1 ";

		whereSql += "  and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		if(key!=null&& !"".equals(key)){
			whereSql+=" and plan0202 like '%"+key.toString()+"%'";
			sql+=" and t.plan0202 like '%"+key.toString()+"%'";
		}

		String strSQL="SELECT * FROM  "+ 
					"(SELECT A.*, ROWNUM RN "+
					" FROM (" +sql+" order by qys,plan0202 ) A "+
					" WHERE ROWNUM <= "+pageSize*(pageIndex+1)+" ) WHERE RN > "+ start ;
							
		List<Map<String,Object>> resultList = this.jdbcTemplate.queryForList(strSQL);
		 
		double yj=getAveOfcompany(zone,zfnd);
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
				eo.add("bnyj", map.get("rws").toString()+"/"+map.get("qys").toString()+"/"+yj);
				double bl = Double.parseDouble(map.get("qys").toString());
				eo.add("ys", bl>yj?false:true);   //true表示需要亮显或预警
				eg.rows.add(eo);
			}
		}
		return eg.toString();
	}

	private double getAveOfcompany(String zone,String zfnd){
		String ryssql=null;
		String zqysslq=null;
		ryssql="select count(distinct t.plan0203) from plan02 t inner join plan01 a on a.plan00=t.parentid  where  t.plan0205='"+zone+"' and a.plan0101='"+zfnd+"' and t.plan0204=2";
		zqysslq="select sum(plan0602) from plan06 t  inner join plan01 a on a.plan00=t.parentid "+
				" where t.plan0601='"+zone+"' and a.plan0101='"+zfnd+"'";
		int ryzs=this.jdbcTemplate.queryForInt(ryssql);
		int qys=this.jdbcTemplate.queryForInt(zqysslq);
		
		if(ryzs==0){
			return 0;
		}else{
			return qys*2/ryzs;
		}
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
				eo.add("cz", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\"  onclick=\"sbRow()\">上报</a>");
			}else{
				/*eo.add("cz", "<a class=\"mini-button\" id = \"upbur\" iconCls=\"icon-upload\"  onclick=\"sbRow('ll')\">浏览人员</a>");*/
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
