package com.bop.web.ssj.count;

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
		
		String querysql = "select * from (select t.plan0404,tt.caption, count(t.plan0402) zj, sum(case when t.PLAN0405 = 1 then 1 else 0 end ) ts,"+
		" sum(decode(t.PLAN0406,1,1,0)) jl, sum(decode(t.PLAN0407,1,1,0)) xk, sum(decode(t.PLAN0408,1,1,0)) bz,"+
		" sum(decode(t.PLAN0409,1,1,0)) cp from plan04 t "+
		" left join dm_codetable_data tt on tt.cid = t.plan0404 and tt.codetablename = 'DB064' where t.parentid = '"+id+
		"' group by t.plan0404,tt.caption  order by t.plan0404 ) qy  left join (select p2.plan0205 ,count(*) zrs , sum(decode(p2.plan0204,2,1,0)) as ccrs from plan02 p2 where p2.parentid = '"+id+"'"+ 
		"  group by p2.plan0205,p2.parentid) pp on  pp.plan0205 = qy.plan0404 ";

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
		String id = request.getParameter("id");
		
		String querysql = "select t.plan0404,tt.caption, count(t.plan0402) zj, sum(case when t.PLAN0405 = 1 then 1 else 0 end ) ts,"+
		" sum(decode(t.PLAN0406,1,1,0)) jl, sum(decode(t.PLAN0407,1,1,0)) xk, sum(decode(t.PLAN0408,1,1,0)) bz,"+
		" sum(decode(t.PLAN0409,1,1,0)) cp from plan04 t "+
		" left join dm_codetable_data tt on tt.cid = t.plan0404 and tt.codetablename = 'DB064' where t.parentid = '"+id+
		"' group by t.plan0404,tt.caption  order by t.plan0404 ";

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
