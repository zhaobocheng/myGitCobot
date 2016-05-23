package com.bop.web.ssj.personmanage;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.sun.org.apache.bcel.internal.classfile.Code;

@Controller
public class PersonManage {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;

	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	/**
	 * 得到为选中人员列表数据
	 * @return
	 */
	@Action
	public String getUnSelectedGridData(){
		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		
		Records rds = this.recordDao.queryRecord("PLAN02", "plan0204 = '否'");
		for(IRecord ird:rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getRecordId());
			eo.add("unSeletedName", ird.get("plan0202"));
			eo.add("unSeletedDept", ird.get("PLAN0205",Code.class));
			eoc.add(eo);
		}
		return eoc.toString();
	}
	/**
	 * 得到已选中人员列表数据
	 * @return
	 */
	@Action
	public String getSelectedGridData(){

		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		Records rds = this.recordDao.queryRecord("PLAN02", "plan0204 = '是'");
		for(IRecord ird:rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ird.getRecordId());
			eo.add("seletedName", ird.get("plan0202"));
			eo.add("seletedDept", ird.get("PLAN0205"));
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
		
		Records rds = this.recordDao.queryRecord("PLAN01");
		ExtObjectCollection eoc = new ExtObjectCollection();

		for(IRecord ire:rds){
			ExtObject eo = new ExtObject();
			eo.add("id", ire.getObjectId());
			eo.add("text", ire.get("PLAN0102")+"年"+ire.get("PLAN0101")+"月");
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
				ird.put("PLAN0204", "是");
			}else{
				ird.put("PLAN0204", "否");
			}
			this.recordDao.saveObject(ird);
		}
		return "success";
	}
}
