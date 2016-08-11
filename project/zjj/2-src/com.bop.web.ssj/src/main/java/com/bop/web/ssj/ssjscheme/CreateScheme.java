package com.bop.web.ssj.ssjscheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcOperations;

import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Color;
import com.aspose.cells.License;
import com.aspose.cells.Row;
import com.aspose.cells.RowCollection;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.XlsSaveOptions;
import com.bop.domain.IRecordDao;
import com.bop.domain.Records;
import com.bop.domain.dao.DmCodetables;
import com.bop.domain.dao.IRecord;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridRow;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;
import com.bop.web.CommonSession;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;


@Controller
public class CreateScheme {

	private JdbcOperations jdbcTemplate;
	private IRecordDao recordDao;
	private UserSession userSession;

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	/**
	 * 创建任务方法
	 * @author lh
	 * @param json 新增时传入的数据
	 * @return
	 */
	@Action
	public String addScheme(String json){
		
		ExtResultObject eor = new ExtResultObject();
		HttpServletRequest repquest = ActionContext.getActionContext().getHttpServletRequest();
		String addMonthcom = repquest.getParameter("addMonthcom").toString();
		String addYearcom = repquest.getParameter("addYearcom").toString();
		String faname = repquest.getParameter("faname").toString();
		String sql = "PLAN0102= "+addMonthcom+" and PLAN0101 = "+addYearcom;
		Records rds = this.recordDao.queryRecord("PLAN01", sql);


		if(rds.size()>0){
			//已经存在这种记录
			eor.add("inf", "false");
			eor.add("text", "已经存在相同时间段记录！");
			return eor.toString();
		}

		Records u1 = this.recordDao.queryRecord("USER01", "USER0101 = '"+this.userSession.getCurrentUserId()+"'");

		UUID uid = UUID.randomUUID();
		IRecord red =this.recordDao.createNew("PLAN01",uid, uid);
		red.put("PLAN0101", addYearcom);
		red.put("PLAN0102", addMonthcom);
		red.put("PLAN0103", new Date());
		red.put("PLAN0104",u1.get(0).get("user00"));
		red.put("PLAN0105", "0");
		red.put("PLAN0107", faname);
		this.recordDao.saveObject(red);

		eor.add("inf", "true");
		return eor.toString();
	}

	/**
	 * 删除任务方法
	 * @author bdsoft lh
	 * @return
	 */
	@Action
	public String deleteScheme(){
		ExtResultObject ero = new ExtResultObject();
		String id = ActionContext.getActionContext().getHttpServletRequest().getParameter("id");

		IRecord p1 = this.recordDao.getRecord("PLAN01", UUID.fromString(id));
		if(p1.get("PLAN0105")!=null && "1".equals(p1.get("PLAN0105").toString())){
			return "false";
		}else{
			this.recordDao.deleteObject("PLAN01", UUID.fromString(id));
			ero.add("result", true);
			return "success";
		}
	}

	/**
	 * 加载任务列表的方法
	 * @return
	 * @author lh
	 */
	@Action
	public String getGridData(){
		//查询数据库
		ExtObjectCollection eoc = new ExtObjectCollection();
		Records rds = this.recordDao.queryRecord("PLAN01");

		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		for(IRecord rd :rds){
			ExtObject eo = new ExtObject();

			eo.add("id", rd.getObjectId());
			eo.add("zftime",rd.get("PLAN0101").toString()+"0"+rd.get("PLAN0102").toString());
			eo.add("cjtime",format.format(rd.get("PLAN0103")));
			eo.add("famc",rd.get("PLAN0107").toString());
			eo.add("zt", "1".equals(rd.get("PLAN0105").toString())?"已启用":"未启用");
			
			eoc.add(eo);
		}
		return eoc.toString();
	}

	/**
	 * 启用方案
	 * @return
	 */
	@Action
	public String goStart(){
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String data = request.getParameter("data");
		JSONArray array = JSONArray.fromObject(data);
		for(int i=0;i<array.size();i++){
			JSONObject jsonObject = (JSONObject) array.get(i);
			String faid = jsonObject.get("id").toString();
			IRecord ird = this.recordDao.getRecord("PLAN01", UUID.fromString(faid));
			ird.put("PLAN0105", 1);
			this.recordDao.saveObject(ird);
			//方案启动生成一条记录状态的记录，复制人员信息到plan02,复制企业信息到plan04后两步用触发器实现
			this.insertPlan3(faid);
		}
		return "success";
	}
	
	/**
	 * 启动任务生出各区县状态表
	 * @author bdsoft lh
	 * @param faid
	 */
	private void insertPlan3(String faid){
		//需要区分是市局还是区县的用户
		String zone = this.userSession.getCurrentUserZone();
		if(zone!=null&&!"".equals(zone)){
			Object [] args = new Object[4];
			String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
			args[0]=faid;
			args[1]=faid;
			args[2]=this.userSession.getCurrentUserZone();
			args[3]=0;
			this.jdbcTemplate.update(sql, args);
		}else{
			Records irds = this.recordDao.queryRecord("dm_codetable_data", "codetablename='DB064'");
			for(IRecord ird:irds){
				Object [] args = new Object[4];
				String sql = "insert into plan03(recordid,parentid,plan00,pindex,plan0301,plan0302) values(get_uuid,?,?,1,?,?)";
				args[0]=faid;
				args[1]=faid;
				args[2]=ird.get("CID");
				args[3]=0;
				this.jdbcTemplate.update(sql, args);
			}
		}
	}
	
	
	/**
	 * 得到设计领域
	 * @param jgdm
	 * @return
	 */
	private String getJSLY(String jgdm){
		String sql = "select * from org02 where  parentid =(select org01.org00 from org01 where org01.org_code = '"+jgdm+"')";
		List<Map<String,Object>> org2List = this.jdbcTemplate.queryForList(sql);
		
		String retStr = "";
		if(org2List.size()>0){
			Map<String,Object> map = org2List.get(0);
			if(map.get("ORG0201")!=null&&"1".equals(map.get("ORG0201").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '1'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0202")!=null&&"1".equals(map.get("ORG0202").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '2'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0203")!=null&&"1".equals(map.get("ORG0203").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '3'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0204")!=null&&"1".equals(map.get("ORG0204").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '4'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
			if(map.get("ORG0205")!=null&&"1".equals(map.get("ORG0205").toString())){
				String Codesql = "select * from dm_codetable_data t where t.codetablename = 'ZDY01' and t.cid = '5'";
				Map<String,Object> codeMap = this.jdbcTemplate.queryForMap(Codesql);
				retStr+=codeMap.get("caption")+"，";
			}
		}
		return retStr.substring(0, retStr.length()-1);
	}	
	
	
	/**
	 * 页面加载的时候得到检查人的信息
	 * @param plan12id
	 * @return
	 */
	private String [] getJCRData(UUID plan12id){
		String ids = "";
		String names = "";
		String inform[] = new String[2];
		
		List<IRecord> ires = this.recordDao.getByParentId("PLAN1201", plan12id);
		for(IRecord ire:ires){
			names+= ire.get("PLAN120102").toString()+",";
			ids+=ire.getRecordId().toString()+",";
		}

		if(ids.length()>0){
			inform[0] = ids.substring(0, ids.length()-1);
			inform[1] = names.substring(0, names.length()-1);
		}else{
			inform[0] = "无";
			inform[1] = "无";
		}
		return inform;
	}
	
	
	
	
	
	
	
	/**
	 * 提交随机的抽查企业
	 * @param faid  方案id
	 * @return
	 */
	@Action
	public String commitSchemeDate(String faid){
		String zone = this.userSession.getCurrentUserZone();
		
		if(zone==null||"".equals(zone)){
			String plan12UpSql = "update plan12 set plan1210 = '提交' where parentid = '"+faid+"'";
			String plan1201UpSql = "update plan1201 set plan120104 = '提交' where parentid in (select t.recordid from plan12 t where t.parentid = '"+faid+"')";
			
			this.jdbcTemplate.execute(plan1201UpSql);
			this.jdbcTemplate.execute(plan12UpSql);
			
			String plan03Sql = "update plan03 set plan0302 = 5 where parentid = '"+faid+"'";
			this.jdbcTemplate.execute(plan03Sql);
		}else{
			String plan12UpSql = "update plan12 set plan1210 = '提交' where parentid = '"+faid+"' and plan1204 = '"+zone+"'";
			String plan1201UpSql = "update plan1201 set plan120104 = '提交' where parentid in (select t.recordid from plan12 t where t.parentid = '"+faid+"') and t.plan1204 = '"+zone+"'";
			
			this.jdbcTemplate.execute(plan1201UpSql);
			this.jdbcTemplate.execute(plan12UpSql);
			String plan03Sql = "update plan03 set plan0302 = 5 where parentid = '"+faid+"' and plan1204 = '"+zone+"'";
			this.jdbcTemplate.execute(plan03Sql);
		}

		return "success";
	}

	
    @Action
    public String exportExcel(String faid) throws Exception {
        
    	ExtResultObject ero = new ExtResultObject();
        ActionContext context = ActionContext.getActionContext();
        HttpServletRequest request = context.getHttpServletRequest();
        String path = "/temp/gjxfj.xls";
        // 下载文件流
        try {
            String filePath = System.getProperty("resourceFiles.location")+path;
            
            File file = new File(filePath);
            if (file.exists()) {
                // 删除文件
                file.delete();
            }

            // 获得页面当前字段
            String ziduan = request.getParameter("ziduan");

            // 表头名列表
            List<String> titleList = new ArrayList<String>();
            List<String> fieldList = new ArrayList<String>();
            JSONArray zdjsonarry = JSONArray.fromObject(ziduan);
            
            
            titleList.add("地区");
            titleList.add("机构代码");
            titleList.add("单位名称");
            titleList.add("地址");
            titleList.add("联系人");
            titleList.add("电话");
      //      titleList.add("检查内容");
            titleList.add("检查人");
            titleList.add("涉及事项");
            
            fieldList.add("dq");
            fieldList.add("jgdm");
            fieldList.add("dwmc");
            fieldList.add("dz");
            fieldList.add("lxr");
            fieldList.add("phone");
      //      fieldList.add("jcnr");
            fieldList.add("jcr");
            fieldList.add("sjly");
            
            
           /* for (int i = 0; i < zdjsonarry.size(); i++) {
                titleList.add(zdjsonarry.getJSONObject(i).get("text").toString());
                fieldList.add(zdjsonarry.getJSONObject(i).get("id").toString());
            }*/
            
            String sheetName = "随机方案清单";
  
            // 调用方法查询返回数据
            String list = this.getData(faid);
            // 将返回的list转json对象
            JSONObject a = JSONObject.fromObject(list);
            // 取得对象中key为"ArrData"的集合
            JSONArray jsonarry = a.getJSONArray("ArrData");
            List<JSONObject> jsonList = new ArrayList<JSONObject>();
            // 遍历集合,转成json对象加入集合
            for (int i = 0; i < jsonarry.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonarry.get(i);
                jsonList.add(jsonObject);
            }
            // 是否有记录
            if (jsonList.size() > 0 && jsonList != null) {
                // 生成excel
                createExcelForExtRow(titleList, fieldList, jsonList, filePath, sheetName);
                ero.add("flag", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ero.add("flag", false);
        }
        ero.add("path", path);
        return ero.toString();
    }
    

    @Action
    private void createExcelForExtRow(List<String> titleList, List<String> fieldList,
            List<JSONObject> recordList, String filePath, String sheetName) throws IOException {

        try {
            // 服务器路径
            String servicePath = this.getServicePath();
            String licFilePath = servicePath + File.separator + "license" + File.separator
                    + "Aspose.Total.Product.Family.lic";

            License cellLic = new License();
            cellLic.setLicense(licFilePath);

            // 新建excel
            Workbook wb = new Workbook();
            // 打开excel中第一个sheet
            Worksheet worksheet = wb.getWorksheets().get(0);
            // 设置sheet名称
            worksheet.setName(sheetName);
            // 获取行集合
            RowCollection rows = worksheet.getCells().getRows();
            int styleIndex = wb.getStyles().add();
            // 设置cell样式
            Style style = wb.getStyles().get(styleIndex);
            // 设置文本自动换行
            style.setTextWrapped(false);
            style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
            style.setHorizontalAlignment(TextAlignmentType.CENTER);
            int stratRow = 0;

            // 写入表头
            for (int ti = 0; ti < titleList.size(); ti++) {
                Row row = rows.get(0);
                Cell cell = row.get(ti);
                cell.setValue(titleList.get(ti));
                cell.setStyle(style);
            }
 
            // 写入记录
            stratRow = stratRow + 1;
            for (JSONObject jsonRows : recordList) {
                Row row = rows.get(stratRow);
                for (int fi = 0; fi < fieldList.size(); fi++) {
                    // 获得表头字段
                    String feildName = fieldList.get(fi);
                    // Object objfeild = jsonRows.get(feildName);
                    // 获得当前行的单元格
                    Cell cell = row.get(fi);
                    // 根据表头字段设置单元格值
                    // 查询码
                    if (StringUtils.equals("dq", feildName)) {
                        if(jsonRows.has("dq")){
                         cell.setValue(jsonRows.get("dq"));   
                        }else{
                            cell.setValue("");   
                           }
                    } else if (StringUtils.equals("jgdm", feildName)) {
                        if(jsonRows.has("jgdm")){
                            cell.setValue(jsonRows.get("jgdm"));  
                        }else{
                            cell.setValue("");   
                           }
                    } else if (StringUtils.equals("dwmc", feildName)) {
                        if(jsonRows.has("dwmc")){
                            cell.setValue(jsonRows.get("dwmc")); 
                        }else{
                            cell.setValue("");   
                           }

                    } else if (StringUtils.equals("dz", feildName)) {
                        if(jsonRows.has("dz")){
                            cell.setValue(jsonRows.get("dz")); 
                        }else{
                            cell.setValue("");   
                           }

                    } else if (StringUtils.equals("lxr", feildName)) {
                        if(jsonRows.has("lxr")){
                            cell.setValue(jsonRows.get("lxr")); 
                        }else{
                            cell.setValue("");   
                           }

                    } else if (StringUtils.equals("phone", feildName)) {
                        if(jsonRows.has("phone")){
                            cell.setValue(jsonRows.get("phone")); 
                        }else{
                            cell.setValue("");   
                           }

                    } else if (StringUtils.equals("jcnr", feildName)) {
                        if(jsonRows.has("jcnr")){
                            cell.setValue(jsonRows.get("jcnr")); 
                        }else{
                            cell.setValue("");
                           }
                    }else if (StringUtils.equals("jcr", feildName)) {
                        if(jsonRows.has("jcr")){
                            cell.setValue(jsonRows.get("jcr")); 
                        }else{
                            cell.setValue("");   
                           }

                    }else if (StringUtils.equals("sjly", feildName)) {
                        if(jsonRows.has("sjly")){
                            cell.setValue(jsonRows.get("sjly")); 
                        }else{
                            cell.setValue("");   
                           }

                    }
                    // 设置单元格样式
                    cell.setStyle(style);
                }
                stratRow++;
            }


            worksheet.autoFitColumns();
            // 保存文件
            wb.save(filePath, new XlsSaveOptions());

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得服务器的文件路径
     * 
     * @return 文件路径
     */
    public static final String getServicePath() {
        String path = System.getProperty("resourceFiles.location");
        if (StringUtils.isEmpty(path)) {
            return StringUtils.EMPTY;
        }

        File filePath = new File(path);
        if (filePath == null || !filePath.isDirectory()) {
            return StringUtils.EMPTY;
        } else {
            return filePath.getAbsolutePath();
        }
    }
    
    
    /**
     * 导出
     * 
     * @param 导出列表的查询条件
     *            、页面信息
     * @return 导出
     * @throws Exception
     * @author yangruizhi
     * @since 2014/10/29
     */
    @Action
    private String getData(String fzid) {

        String rtnlist = StringUtils.EMPTY;
        ExtGrid rtnExtGrid = new ExtGrid();


		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
//		String querySql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
//			querySql = "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid not in ('110302','110000')";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
//			querySql = "select t.cid,t.caption from dm_codetable_data t  where t.codetablename = 'DB064' and t.cid = '"+zone+"'";
		}
    // 根据querySql查询返回 ExtGridRow结果集
    //    List<Map<String,Object>> rowlist = this.jdbcTemplate.queryForList(whereSql);
		Records ires  = this.recordDao.queryRecord("PLAN12", whereSql,"plan1204");
		
        List<ExtGridRow> rowlist2 = new ArrayList<ExtGridRow>();
        for (IRecord ire : ires) {
        	ExtGridRow eRow = new ExtGridRow();
			String personInf[] = this.getJCRData(ire.getRecordId());
			eRow.add("id", ire.getRecordId());
			eRow.add("dq", ire.get("PLAN1204",DmCodetables.class).getCaption());
			eRow.add("jgdm", ire.get("PLAN1202"));
			eRow.add("dwmc", ire.get("PLAN1203"));
			eRow.add("dz",  ire.get("PLAN1205"));
			eRow.add("lxr", ire.get("PLAN1206"));
			eRow.add("phone", ire.get("PLAN1207"));
			eRow.add("jcnr",  ire.get("PLAN1208"));
			eRow.add("jcrid", personInf[0]);
			eRow.add("jcr",  personInf[1]);
			eRow.add("sjly", this.getJSLY(ire.get("PLAN1202").toString()));
            rowlist2.add(eRow);
        }
        rtnExtGrid.rows.addAll(rowlist2);
        rtnlist = rtnExtGrid.toString();
        
        return rtnlist;
    }
    
	@Action
	public String getZT(String faid){
		String zone = this.userSession.getCurrentUserZone();

		if(zone==null||"".equals(zone)){
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0302 = 5");
			if(rds.size()==17){
				return rds.get(0).get("plan0302").toString();
			}else{
				return "select"; 
			}
		}else{
			Records rds = this.recordDao.queryRecord("plan03", "parentid='"+faid+"' and plan0301 = '"+zone+"'");
			if(rds.size()>0){
				return rds.get(0).get("plan0302").toString();  //以上报
			}else{
				return "select";
			}
		}
	}

}