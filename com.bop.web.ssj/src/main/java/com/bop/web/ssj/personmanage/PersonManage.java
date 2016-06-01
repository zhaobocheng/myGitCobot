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
		
		String zone = this.userSession.getCurrentUserZone();
		
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}
		
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "plan0204 = '1' and parentid = '"+faid+"'";
		}else{
			whereSql = "plan0204 = '1' and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		}
		
		Records rds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205", start, pageSize);
		Records totalrds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205");
		
		ExtGrid eg = new ExtGrid();
		eg.setTotal(totalrds.size());

		for(IRecord ird:rds){
			ExtGridRow eo = new ExtGridRow();
			eo.add("id", ird.getRecordId());
			eo.add("unSeletedName", ird.get("plan0202"));
			eo.add("unSeletedDept", ird.get("PLAN0205",DmCodetables.class).getCaption());
			eg.rows.add(eo);
		}
		
		
/*		ExtObjectCollection eoc = new ExtObjectCollection();
		for(IRecord ird:rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getRecordId());
			eo.add("unSeletedName", ird.get("plan0202"));
			eo.add("unSeletedDept", ird.get("PLAN0205",DmCodetables.class).getCaption());
			eoc.add(eo);
		}*/
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
		ExtObjectCollection eoc = new ExtObjectCollection();
		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "plan0204 <> '1' and parentid = '"+faid+"'";
		}else{
			whereSql = "plan0204 <> '1' and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		}
		
		// 获取第几页
		int start = 0;
		if (pageIndex != 0){
			start = pageSize*pageIndex;
		}
				
		Records rds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205", start, pageSize);
		Records totalrds = this.recordDao.queryRecord("PLAN02", whereSql,"plan0205");
		
		ExtGrid eg = new ExtGrid();
		eg.setTotal(totalrds.size());

		for(IRecord ird:rds){
			ExtGridRow eo = new ExtGridRow();
			eo.add("id", ird.getRecordId());
			eo.add("seletedName", ird.get("plan0202"));
			eo.add("seletedDept", ird.get("PLAN0205",DmCodetables.class).getCaption());
			eg.rows.add(eo);
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
				ero.add("text", unSelectList.toArray());
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
		String updataSql= "update plan02 set plan0204 = 2 where plan0204 = 0 and parentid = '"+faid+"' and plan0205 = '"+zone+"'";
		
		int allUpCount = this.jdbcTemplate.queryForInt(upSql);
		if(allUpCount>0){
			this.jdbcTemplate.execute(updataSql);
			Object [] args = new Object[3];
			String sql = "insert into plan03 values(get_uuid,get_uuid,?,1,?,?)";
			args[0]=faid;
			args[1]=zone;
			args[2]=1;
			this.jdbcTemplate.update(sql, args);
			return "success";
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				return "ups";  //以上报
			}else{
				return zone;   //需要选择对象
			}
		}
	}
	
	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();
		
		if(zone==null||"".equals(zone)){
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"'");
			if(rds.size()==17){
				return "ups";  //以上报
			}else{
				return "select";   //需要选择对象
			}
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				return "ups";  //以上报
			}else{
				return "select";   //需要选择对象
			}
		}
	}

}
