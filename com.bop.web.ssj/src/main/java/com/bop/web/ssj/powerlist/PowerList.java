package com.bop.web.ssj.powerlist;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
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



	@Action
	public String getPowerListData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String qlbl = request.getParameter("qlbm")==null?null:request.getParameter("qlbm").toString();
		String qlmc = request.getParameter("qlmc")==null?null:request.getParameter("qlmc").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		
		if(qlbl!=null&&!"".equals(qlbl)){
			whereString +=" and Q0101 = '"+qlbl+"'";
		}
		if(qlmc!=null&&!"".equals(qlmc)){
			whereString +=" and Q0103 like '%"+qlmc+"%'";
		}
		
		ExtGrid eg = new ExtGrid();
		Records rds = this.recordDao.queryRecord("Q01", whereString,"Q0102",pageIndex*pageSize,pageSize);
		int total= this.jdbcTemplate.queryForInt("select count(*) from Q01 where "+whereString);
		eg.setTotal(total);
		
		for(IRecord ird :rds){
			ExtObject eo = new ExtObject();
			eo.add("qlqdbm", ird.get("Q0101"));
			eo.add("qlsxmc", ird.get("Q0103"));
			eo.add("ssqx", ird.get("Q0102",DmCodetables.class).getCaption());
			eg.rows.add(eo);
		}
		return eg.toString();
	}
	
	
	@Action
	public String getCompanyQData(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String qymc = request.getParameter("qymc")==null?null:request.getParameter("qymc").toString();
		String qydm = request.getParameter("qydm")==null?null:request.getParameter("qydm").toString();
		
		int pageIndex =Integer.parseInt(request.getParameter("pageIndex").toString());
		int pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
		String whereString = "1=1";
		
		if(qymc!=null&&!"".equals(qymc)){
			whereString +=" and ORG_NAME = '"+qymc+"'";
		}
		if(qymc!=null&&!"".equals(qydm)){
			whereString +=" and ORG_CODE like '%"+qydm+"%'";
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
			eo.add("sjsx", ird.get("ORG_CODE"));
			eg.rows.add(eo);
		}
		return eg.toString();
	}
	
	@Action
	public String getCheckData(){
		ExtObjectCollection eoc = new ExtObjectCollection();
		
		ExtObject eo = new ExtObject();
		eo.add("jcsxmc", "商品条码监督检查");
		eo.add("jcdx","生产者");
		eo.add("ccyj","《商品条码管理办法》");
		eoc.add(eo);
		ExtObject eo2 = new ExtObject();
		eo2.add("jcsxmc", "检查机构监督检查");
		eo2.add("jcdx","本市取得检查检测机构资质认定证书的法人或其他组织；<br/>本市取得检验检测机构资质认定证书的让人或组织");
		eo2.add("ccyj","《中华人民共和国计量法》及其实施细则；<br/>《检查检测机构资质认定管理办法》；<br/>《检查检测机构资质认定评审准则》");
		eoc.add(eo2);
		ExtObject eo3 = new ExtObject();
		eo3.add("jcsxmc", "3C认证日常监督检查");
		eo3.add("jcdx","生产者");
		eo3.add("ccyj","《中华人民共和国认证认可条例》；<br/>《强制性产品认证管理规定》");
		eoc.add(eo3);
		ExtObject eo4 = new ExtObject();
		eo4.add("jcsxmc", "产品质量日常监督检查");
		eo4.add("jcdx","生产者");
		eo4.add("ccyj","《中华人民共和国产品质量法》；<br/>《北京市产品质量监督管理条例》");
		eoc.add(eo4);
		ExtObject eo5 = new ExtObject();
		eo5.add("jcsxmc", "特种设备监督检查");
		eo5.add("jcdx","生产者");
		eo5.add("ccyj","《中华人民共和国特种设备安全法》");
		
		eoc.add(eo5);
		
		return eoc.toString();
	}
}
