package com.bop.web.ssj.ssjscheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.jdbc.core.JdbcOperations;

import com.bop.domain.IRecordDao;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;

@Controller
public class CreateScheme {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;

	
	
	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	
	/**
	 * 创建方案方法
	 * @author lh
	 * @param json 新增时传入的数据
	 * @return
	 */
	@Action
	public String addScheme(String json){
		String str = json;
		String params = ActionContext.getActionContext().getHttpServletRequest().getParameter("params");
		System.out.println(str);
		System.out.println(params);
		return "secces";
	}
	
	/**
	 * 删除方案方法
	 * @return
	 */
	@Action
	public String deleteScheme(){
		ExtResultObject ero = new ExtResultObject();
		String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");
		//做数据库查询工作
		//一种返回方式，这里直接用第二种了
		ero.add("result", true);
		return "success";
	}
	
	/**
	 * 得到方案列表的方法
	 * @return
	 */
	@Action
	public String getGridData(){
		
		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		
		ExtObject eo = new ExtObject();
		eo.add("id", "111111111111111");
		eo.add("zftime", "201605");
		eo.add("cjtime", "2016-05-17");
		eo.add("zt", "未执行");
		eoc.add(eo);

		return eoc.toString();
		
	}
	

	public String [] getSJSData(){
		String sjStr [] = null ;
		
		Random rd = new Random();
		List lt = new ArrayList();
		
/*		lt.add("sss");
		lt.remove("sss");
*/		
		
		int j = rd.nextInt(33);
		return sjStr;
	}
	
	//随机的步骤
	/**
	 * 1、得到一个区县的所有的企业的信息，有多少个企业(已经通过判定得到的都是未随机过的企业)，每个企业的信息是
	 * 2、同权重确定最终的各个类型的企业的数量，每个企业一个编号
	 * 3、以上一步企业的数量为上线，通过设置的要随机的企业的数量，得到对应数量的随机数。
	 * 4、拿3中得到的随机数的对比得到对应的企业编号，加载对应的企业信息到前台列表
	 * 
	 * 
	 * 5、当页面点击提交的时候，做方案入库记录，同时标记企业表表示在该轮已经抽查过这个企业。
	 */

}
