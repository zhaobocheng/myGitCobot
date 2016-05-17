package com.bop.web.bopmain.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridRow;
import com.bop.json.ExtResultObject;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.bop.web.rest.renderer.Renderer;
import com.bop.web.rest.renderer.TemplateRenderer;


@Controller
public class LogView{
	private IRecordDao recordDao;
	
	public IRecordDao getRecordDao() {
		return recordDao;
	}
	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}
	@Action
	public Renderer view(){
		//用于展现日志的内容，暂时作出一个展示所有用的所有日志，如果有其他的需求，使用条件选择展现对应日志内容
		Map<String, Object> vc = new HashMap<String, Object>();
		return new TemplateRenderer(this.getClass(), "view", vc);
	}
	/**
	 * @author li
	 * @param 查询日志的条件
	 */
	@Action
	public String getView(String queryString){
		ExtResultObject ero = new ExtResultObject();
		//得到filter 表中的数据，然后在到映射表中期找到对应的规则，将对应的数据显示出来

		String sql = "select * from sy_log ";
		if(!"all".equals(queryString)){
			HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
			
		}
		
		Records recs = this.recordDao.queryRecord("sy_log");
		Records logMapRecs = this.recordDao.queryRecord("sy_logmap");
		ExtGrid grid = new ExtGrid();
		List<ExtGridRow> rows = new ArrayList<ExtGridRow>();
		
		for(IRecord rec : recs){
			for(IRecord logmap: logMapRecs){
				String mapRule = logmap.get("mapRule").toString();
				String url = rec.get("url").toString();
				String querystring = rec.get("querystring").toString();
				
				
				if(rec.get("url").toString().contains(mapRule)){
					ExtGridRow row = new ExtGridRow();
					row.add("logaction", logmap.get("mapContent"));
					row.add("logtime", rec.get("logTime"));
					row.add("loguser", rec.get("userId"));
					row.add("querystring", rec.get("querystring"));
					rows.add(row);
					break;
				}
			}
		}
		grid.rows.addAll(rows);
		
		return grid.toString();
	}
}
