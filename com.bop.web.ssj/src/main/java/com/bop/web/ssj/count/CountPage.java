package com.bop.web.ssj.count;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcOperations;
import com.bop.domain.IRecordDao;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class CountPage {
	private UserSession userSession;
	private IRecordDao recordDao;
	private JdbcOperations jdbcTemplate;
	
	
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Action
	public String getcq(){
		HttpServletRequest request =  ActionContext.getActionContext().getHttpServletRequest();
		String id = request.getParameter("id");

		String querysql = "select * from (select t.plan0417,tt.caption, count(t.plan0402) zj, sum(case when t.PLAN0405 = 1 then 1 else 0 end ) ts,"+
		" sum(decode(t.PLAN0406,1,1,0)) jl, sum(decode(t.PLAN0407,1,1,0)) xk, sum(decode(t.PLAN0408,1,1,0)) bz,"+
		" sum(decode(t.PLAN0409,1,1,0)) cp from plan04 t "+
		" left join dm_codetable_data tt on tt.cid = t.plan0417 and tt.codetablename = 'DB064' where t.parentid = '"+id+
		"' group by t.plan0417,tt.caption  order by t.plan0417 ) qy  left join (select p2.plan0205 ,count(*) zrs , sum(decode(p2.plan0204,2,1,0)) as ccrs from plan02 p2 where p2.parentid = '"+id+"'"+ 
		"  group by p2.plan0205,p2.parentid) pp on  pp.plan0205 = qy.plan0417 ";

		ExtObjectCollection exc = new ExtObjectCollection();
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(querysql);
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("qx", map.get("caption"));
			eo.add("zj", map.get("zj"));
			eo.add("jl", map.get("jl"));
			eo.add("ts", map.get("ts"));
			eo.add("xk", map.get("xk"));
			eo.add("bz", map.get("bz"));
			eo.add("cp", map.get("cp"));
			eo.add("zrs", map.get("zrs"));
			eo.add("ccrs", map.get("ccrs"));
			exc.add(eo);
		}
		return exc.toString();
	}

	@Action
	public String getsjCq(){
		HttpServletRequest request =  ActionContext.getActionContext().getHttpServletRequest();

		String starttime = request.getParameter("startdata");
		String endtime = request.getParameter("enddata");
		String qxdm = request.getParameter("qxdm");
		Date date;
		Date date2;
		SimpleDateFormat  sf = new SimpleDateFormat ("yyyy-MM-dd");

		if(starttime==null|| "".equals(starttime)){
			date = new Date();
		}else{
			date = new Date(starttime);
		}
		if(endtime==null|| "".equals(endtime)){
			date2 = new Date();
		}else{
			date2 = new Date(endtime);
		}
		
		String faidQuery = " select plan00 from plan01 t where to_char(t.plan0108,'yyyy-MM-dd') >= '"+ sf.format(date) +"' and to_char(t.plan0108,'yyyy-MM-dd') <= '" + sf.format(date2)+"'";
		String lasttQuery = " select tt.plan00  from (select plan00 from plan01 sp where to_char(sp.plan0108,'yyyy-MM-dd') >= '"+ sf.format(date) +"' and to_char(sp.plan0108,'yyyy-MM-dd') <= '" + sf.format(date2)+"' order by sp.plan0108 desc) tt where rownum =1";
		String qxidQuery ="" ;
		
		if(qxdm==null){
			qxidQuery = "44444";
		}else if("000000".equals(qxdm)){
			qxidQuery = "select t.cid from dm_codetable_data t where t.codetablename = 'DB064' and t.cid <>'110000'";
		}else{
			qxidQuery = "'"+qxdm.replaceAll(",", "','")+"'";
		}

		String	querysql ="select  cp.caption as qxmc,qy.*,cyry.cyrys as cyrys,zqys.zs as zqys,zrys.zs as ryzs from dm_codetable_data cp "+
	  	"  left join ( select t.plan1204,dm.caption, count(*) qys_jcs,   sum(decode(t.plan1221,2,1,0)) as qys_uf,  sum(decode(t.plan1210, 5, 1, 0)) as qys_gss, sum(decode(t.plan1221,1,1,0)) as qys_fd,sum(decode(t.plan1221,4,1,0)) as qys_qt,"+
	    " sum(decode(t.plan1221,3,1,0)) as qys_ucc, sum(case when t.plan1221 is null then 1 else 0 end) as qys_ucmt,sum(case when t.plan1224=2 or t.plan1224 = 3 then 1 else 0 end )  as qys_las,  sum(decode(t.plan1226,1,1,0)) as qys_yzx,  sum(decode(t.plan1226,2,1,0)) as qys_drrbf,"+
	    " sum(decode(t.plan1226,3,1,0)) as qys_bd, sum(decode(t.plan1226,4,1,0)) as qys_infobf,  sum(decode(t.plan1226,5,1,0)) as qys_upro "+
	    " from plan12 t "+
	    " left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.plan1204   where t.parentid in ("+faidQuery+")  and t.plan1204 in ("+qxidQuery+") group by t.plan1204,dm.caption) qy on qy.plan1204 = cp.cid"+ 
	    " left join (select count(distinct p2.plan0201) as cyrys ,p2.plan0205  from plan02 p2  where p2.plan0204 = 2 and p2.parentid in ("+faidQuery+")  and p2.plan0205 in ("+qxidQuery+") group by p2.plan0205) cyry on cyry.plan0205 = qy.plan1204 "+
	    " left join (select count(*) zs,t.plan0417 from plan04 t where t.parentid = ("+lasttQuery+") group by t.plan0417) zqys on zqys.plan0417 = qy.plan1204 "+
	    " left join (select count(*) zs,t.plan0205 from plan02 t where t.parentid = ("+lasttQuery+") group by t.plan0205) zrys on zrys.plan0205 = qy.plan1204 "+
	    " where cp.codetablename = 'DB064' and cp.cid in ("+qxidQuery+") order by cp.cid ";

		ExtObjectCollection exc = new ExtObjectCollection();
		List<Map<String,Object>> list = this.jdbcTemplate.queryForList(querysql);
		for(Map<String,Object> map:list){
			ExtObject eo = new ExtObject();
			eo.add("qx", map.get("qxmc"));
			eo.add("ryzs", map.get("ryzs"));
			eo.add("cyrys", map.get("cyrys"));
			eo.add("zqys", map.get("zqys"));
			eo.add("qys_uf", map.get("qys_uf"));
			eo.add("qys_fd", map.get("qys_fd"));
			eo.add("qys_ucc", map.get("qys_ucc"));
			eo.add("qys_qt", map.get("qys_qt"));
			eo.add("qys_ucmt", map.get("qys_ucmt"));
			eo.add("qys_jcs", map.get("qys_jcs"));
			eo.add("qys_las", map.get("qys_las"));
			eo.add("qys_yzx", map.get("qys_yzx"));
			eo.add("qys_drrbf", map.get("qys_drrbf"));
			eo.add("qys_bd", map.get("qys_bd"));
			eo.add("qys_infobf", map.get("qys_infobf"));
			eo.add("qys_upro", map.get("qys_upro"));
			eo.add("qys_gss", map.get("qys_gss"));
			exc.add(eo);
		}
		return exc.toString();
	}
	
	
	
	

	/**
	 * 得到下拉框数据
	 * @return
	 */
	@Action
	public String getYData(){
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
	 * 得到下拉框数据
	 * @return
	 */
	@Action
	public String getRW(String year){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select  t.plan0107 mc,t.plan00 as id from plan01 t where t.plan0101 = "+year +" order by plan0102 desc");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("id"));
			eo.add("text", ire.get("mc"));
			eoc.add(eo);
		}
		return eoc.toString();
	}

	/**
	 * 得到区划列表多选数据
	 * @return
	 */
	@Action
	public String getCheckDate(){
		List<Map<String, Object>> ndlist= this.jdbcTemplate.queryForList("select cid,caption from dm_codetable_data t where t.codetablename = 'DB064' and cid <> '110000' order by cid");
		ExtObjectCollection eoc = new ExtObjectCollection();

		ExtObject eo1 = new ExtObject();
		eo1.add("id", "000000");
		eo1.add("text", "全部");
		eoc.add(eo1);
		
		for(Map<String,Object> ire:ndlist){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.get("cid"));
			eo.add("text", ire.get("caption"));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	
}
