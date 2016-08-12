package com.bop.web.ssj.ssjscheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;

public class ExportExcle {
	
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
	 * 接口二调用示例
	 * @param faid
	 * @return
	 */
	@Action
	public String viewExport(String faid){
		String result = null;
		try {
			result = exportExcel(faid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 接口一调用示例
	 * @param faid
	 * @return
	 */
	@Action
	public String creatExportExcel(){
		String result = null;
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String gridcolmun = request.getParameter("gridcolmun");
		String sql = this.getSql();
		
		try {
			result = this.exportExcel(gridcolmun, sql);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private String getSql(){
		String sql = "select  t.plan00 as id ,t.plan0107 as mc,t.plan0101||t.plan0102 as zfyf, decode(counorg.qys,null,0,counorg.qys) as cycczs , case when p6.plan0602 is null then round(counorg.qys*0.01)  else p6.plan0602  end as ccqys ,  "+
				 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs , decode(p21.fqs,null,0,p21.fqs) as wtjfas,tt.plan0302 from plan01 t  left join plan03 tt on tt.parentid = t.plan00 "+
				" left join (select count(*) qys,org.parentid, org.plan0404  from plan04 org group by org.parentid,org.plan0404) counorg on counorg.plan0404 =  tt.plan0301 and tt.parentid = counorg.parentid"+
				" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
				" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301    left join (select count(*) as fqs ,pp.parentid from plan21 pp group by pp.parentid) p21 on p21.parentid = t.plan00 where ";
		String wheresql = " t.PLAN0105 = 1 and tt.plan0301='"+this.userSession.getCurrentUserZone()+"' order by plan0102 ";
		return sql + wheresql;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	//-------------------------------------下面是导出的公用方法，简单封装，以后应该拆开---------------------------------------
	/**
	 * 导出excle
	 * @author bdsoft lh
	 * @param faid
	 * @return
	 * @throws Exception
	 */
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
            // 表头名列表
            List<String> titleList = new ArrayList<String>();
            List<String> fieldList = new ArrayList<String>();
            
            
            titleList.add("地区");
            titleList.add("机构代码");
            titleList.add("单位名称");
            titleList.add("地址");
            titleList.add("联系人");
            titleList.add("电话");
            titleList.add("检查人");
            titleList.add("涉及事项");
            
            fieldList.add("dq");
            fieldList.add("jgdm");
            fieldList.add("dwmc");
            fieldList.add("dz");
            fieldList.add("lxr");
            fieldList.add("phone");
            fieldList.add("jcr");
            fieldList.add("sjly");

            String sheetName = "随机方案清单";
            String list = this.getData(faid);
            JSONObject a = JSONObject.fromObject(list);
            JSONArray jsonarry = a.getJSONArray("ArrData");
            List<JSONObject> jsonList = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonarry.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonarry.get(i);
                jsonList.add(jsonObject);
            }
            if (jsonList.size() > 0 && jsonList != null) {
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

    /**
     * 创建导出文件
     * @param titleList  显示的列名
     * @param fieldList  对应数据项的列名	
     * @param recordList  数据集合
     * @param filePath	文件存放路径
     * @param sheetName	excle的标签名
     * @throws IOException
     */
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

            Workbook wb = new Workbook();
            Worksheet worksheet = wb.getWorksheets().get(0);
            worksheet.setName(sheetName);
            RowCollection rows = worksheet.getCells().getRows();
            int styleIndex = wb.getStyles().add();
            Style style = wb.getStyles().get(styleIndex);
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
                    String feildName = fieldList.get(fi);
                    // Object objfeild = jsonRows.get(feildName);
                    // 获得当前行的单元格
                    Cell cell = row.get(fi);
                    // 根据表头字段设置单元格值
                    // 查询码
                    if(jsonRows.has(feildName)){
                    	cell.setValue(jsonRows.get(feildName));   
                    }else{
                        cell.setValue("");   
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
     * 得到导出数据
     * @param fzid
     * @return
     */
    @Action
    private String getData(String fzid) {
        String rtnlist = StringUtils.EMPTY;
        ExtGrid rtnExtGrid = new ExtGrid();

		String zone = this.userSession.getCurrentUserZone();
		String whereSql = null;
		if(zone==null||"".equals(zone)){
			whereSql = "parentid = '"+fzid+"'";
		}else{
			whereSql = "parentid = '"+fzid+"' and PLAN1204 = '"+zone+"'";
		}
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


	
	
	
	
	
	//-------------------------------重写导出方法----------------------------------
	/**
	 * 展示的数据不需要程序转换的都可以用这个导出方法，如果需要程序转换就要在对应个性化的方法写对应的转换展示字段
	 * 使用miniui的方法能够得到对应的列的header和filed,注意sql的拼写要与grid的filed一致
	 * @param gridcolmun 前端传入的grid的列属性集合，通过mini.encode(grid.columns)得到
	 * @param sql 查询导出列表数据的sql
	 * @return
	 */
	@Action
	public String exportExcel(String gridcolmun,String sql) throws Exception {
		JSONArray ja = JSONArray.fromObject(gridcolmun);
		List<String> titleList = new ArrayList<String>();
	    List<String> fieldList = new ArrayList<String>();

	     //得到标头和字段简拼
        for (int i = 0; i < ja.size(); i++) {
            JSONObject jsonObject = (JSONObject) ja.get(i);
            if(jsonObject.getBoolean("visible")){
            	  if(jsonObject.get("header")!=null)
                  	titleList.add(jsonObject.get("header").toString());
                  
                  if(jsonObject.get("field")!=null)
                  	fieldList.add(jsonObject.get("field").toString());
            }
        }

        ExtResultObject ero = new ExtResultObject();
        String path = "/temp/gjxfj.xls";
        try {
            String filePath = System.getProperty("resourceFiles.location")+path;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            String sheetName = "随机方案清单";
            //查询对应的数据
            String list = this.getData(sql,fieldList);
            JSONObject a = JSONObject.fromObject(list);
            JSONArray jsonarry = a.getJSONArray("ArrData");
            List<JSONObject> jsonList = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonarry.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonarry.get(i);
                jsonList.add(jsonObject);
            }
            if (jsonList.size() > 0 && jsonList != null) {
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

    /**
     * 得到导出数据
     * @param fzid
     * @return
     */
    @Action
    private String getData(String sql,List<String> fieldList) {
    	String rtnlist =null;
        ExtGrid rtnExtGrid = new ExtGrid();
        List<Map<String,Object>> datalist = this.jdbcTemplate.queryForList(sql);
        
        List<ExtGridRow> rowlist2 = new ArrayList<ExtGridRow>();
        for (Map<String,Object> ire : datalist) {
        	ExtGridRow eRow = new ExtGridRow();
			for(int i=0;i<fieldList.size();i++){
				eRow.add(fieldList.get(i), ire.get(fieldList.get(i)));
			}
            rowlist2.add(eRow);
        }
        rtnExtGrid.rows.addAll(rowlist2);
        rtnlist = rtnExtGrid.toString();
        return rtnlist;
    }

}
