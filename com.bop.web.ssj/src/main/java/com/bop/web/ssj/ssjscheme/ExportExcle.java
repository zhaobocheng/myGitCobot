package com.bop.web.ssj.ssjscheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.bop.json.ExtObject;
import com.bop.json.ExtResultObject;
import com.bop.web.bopmain.UserSession;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;

public class ExportExcle {
	/**
	 * createExcelForExtRow d导出文件方法是通用
	 * 两个导出引导方法是针对不同导出的接口exportExcel 
	 */
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
				" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301    left join (select count(*) as fqs ,pp.fq0105 from fq01 pp group by pp.fq0105) p21 on p21.fq0105 = t.plan00 where ";
		String wheresql = " t.PLAN0105 = 1 and tt.plan0301='"+this.userSession.getCurrentUserZone()+"' order by plan0102 ";
		return sql + wheresql;
	}
	
	/**
	 * 执法结果录入导出
	 * @param rwmc 
	 * @param month 
	 * @param gridcolmun
	 * @return
	 */
	@Action
	public String ResultExportExcel(){
		String result = null;
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String gridcolmun = request.getParameter("gridcolmun");
		String rwmc = request.getParameter("rwmc")==null?null:request.getParameter("rwmc").toString();
		String month = request.getParameter("month")==null?null:request.getParameter("month").toString();

		String zone = this.userSession.getCurrentUserZone();
		
		//String querySql = "select * from plan01,plan12 where plan01.plan00=plan12.parentid and plan1210='保存'";
		String querySql = "select a.plan0102,a.plan0107,t.recordid,t.parentid,t.plan1202,t.plan1203,t.plan1204,t.plan1205,t.plan1206,t.plan1207,"+
						" c.caption as plan1221,c1.caption as plan1222,t.plan1223,c2.caption as plan1224,case when t.plan1225='1' then '是' else '否' end as plan1225,c3.caption as plan1226,t.plan1227 from plan12 t "+
						" left join plan01 a on t.parentid=a.plan00 "+
						" left join dm_codetable_data c on c.cid=t.plan1221 and c.codetablename='ZDY06' "+
						" left join dm_codetable_data c1 on c1.cid=t.plan1222 and c1.codetablename='ZDY01' "+
						" left join dm_codetable_data c2 on c2.cid=t.plan1224 and c2.codetablename='ZDY08' "+
						" left join dm_codetable_data c3 on c3.cid=t.plan1226 and c3.codetablename='ZDY09' "+
						" where t.plan1210='2' ";
		if(null!=zone&&!"".equals(zone)){	
			querySql += " and t.plan1204='"+zone+"'";
		}
		
		if  (null!=rwmc &&!"".equals(rwmc)){
			querySql += " and a.plan0107 like '%"+rwmc+"%'";
		}
		if  (null!=month &&!"".equals(month)){
			querySql += " and a.plan0102='"+month+"'";
		}
		
		try {
			result = this.exportExcel(gridcolmun, querySql);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return result;
	}
	/**
	 * 执法方案浏览，导出
	 * @param zfnd
	 * @return
	 */
	@Action
	public String SchemeViewExportExcel(){
		String result = null;
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String gridcolmun = request.getParameter("gridcolmun");
		String rwmc = request.getParameter("rwmc")==null?null:request.getParameter("rwmc").toString();
		String zfnd = request.getParameter("zfnd")==null?null:request.getParameter("zfnd").toString();

		String sql = "select  t.plan00,tt.recordid,tt.parentid,tt.PLAN0301 as qxid,to_char(tt.plan0303,'yyyy-MM-dd') as tjsj,t.plan0102 as yf,t.plan0107 as mc,aa.caption as qx,t.plan0101||t.plan0102 as zfyf,counorg.qys as cycczs , case when p6.plan0602 is null then 0 else p6.plan0602  end as ccqys , "+
				 " counp2.zrs as zfryzs,  counp2.cqrs as cyzfrs,fqs as fqfas,case when tt.plan0302 ='5' then '是' else '否' end as cz,to_char(ppp.gss) as sfgs from plan01 t  "+
				 " inner join plan03 tt on tt.parentid = t.plan00 "+				   
				" left join dm_codetable_data aa on aa.cid=tt.plan0301 and aa.codetablename='DB064' " +
				 " left join (select count(*) qys,org.reg_district_dic from org01 org group by org.reg_district_dic) counorg on counorg.reg_district_dic = tt.plan0301"+
				" left join (select count(*) zrs ,sum(decode(p2.plan0204,2,1,0)) as cqrs, p2.plan0205,p2.parentid from plan02 p2 group by p2.plan0205 ,p2.parentid) counp2 on counp2.parentid = t.plan00 and counp2.plan0205=tt.plan0301 "+
				" left join (select count(*) fqs , p4.fq0103,p4.fq0105 from fq01 p4 group by p4.fq0103 ,p4.fq0105) countorg on countorg.fq0105=t.plan00 and countorg.fq0103=tt.plan0301 " + 
				" left join (select sum(decode(p12.plan1210,5,1,0)) as gss,p12.parentid,p12.plan1204 from plan12 p12 group by p12.parentid ,p12.plan1204) ppp on ppp.parentid = t.plan00 and ppp.plan1204 = tt.plan0301"+
				" left join  plan06 p6  on p6.parentid = t.plan00 and p6.plan0601 = tt.plan0301 where ";
	
	String sql1="select plan00,plan00 as recordid,null as parentid,null as qxid,'' as tjsj,plan0102 as yf,plan0107 as mc,'' as qx,plan0101||plan0102 as zfyf,null as cycczs , null as ccqys , null as zfryzs,  null as cyzfrs,null as fqfas,'' as cz,'' as sfgs from plan01 where plan0105 = 1  ";
	
	String wheresql = " t.plan0105 = 1 ";
	String zone=this.userSession.getCurrentUserZone();
	if(zone!=null&&!"".equals(zone)){
		wheresql += "and tt.plan0301='"+zone+"' ";
	}
	if(zfnd!=null&&!"".equals(zfnd)){
		wheresql += " and t.plan0101 = "+zfnd;
		sql1+=" and plan0101="+zfnd;
	}		
	if  (null!=rwmc &&!"".equals(rwmc)){
		wheresql += " and plan0107 like '%"+rwmc+"%'";
		sql1+= " and plan0107 like '%"+rwmc+"%'";
	}	
	try {
		result = this.exportExcel(gridcolmun,sql+wheresql+" UNION ALL "+sql1+" order by yf,qxid desc");
	} catch (Exception e) {
		e.printStackTrace();
	}		
		return result;
	}
	
	/*
	 * 抽查情况统计导出excel
	 */
	@Action 
	public String RandomStaticsExportExcel(){
		String result=null;
		try {
			HttpServletRequest request=ActionContext.getActionContext().getHttpServletRequest();
			String gridcolmun = request.getParameter("gridcolmun");
			String qx = request.getParameter("qx");
			String starttime = request.getParameter("starttime"); 
			String endtime = request.getParameter("endtime");
			 
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");

			String startDate = sfd.format(new Date(starttime));
			String endDate  = sfd.format(new Date(endtime));
			String faidQuery = " select plan00 from plan01 t where to_char(t.plan0108,'yyyy-MM-dd') >= '"+ startDate +"' and to_char(t.plan0108,'yyyy-MM-dd') <= '" + endDate+"'";
			String lasttQuery = " select tt.plan00  from (select plan00 from plan01 sp where to_char(sp.plan0108,'yyyy-MM-dd') >= '"+ startDate +"' and to_char(sp.plan0108,'yyyy-MM-dd') <= '" + endDate+"' order by sp.plan0108 desc) tt where rownum =1";
			String qxidQuery ="" ;
			if("000000".equals(qx)){
				qxidQuery = "select t.cid from dm_codetable_data t where t.codetablename = 'DB064' and t.cid <>'110000'";
			}else{
				qxidQuery = "'"+qx.replaceAll(",", "','")+"'";
			}
			String	querysql ="select  cp.caption as qxmc,qy.*,cyry.cyrys as cyrys,zqys.zs as zqys,zrys.zs as ryzs from dm_codetable_data cp "+
				  	"  left join ( select t.plan1204,dm.caption, count(*) qys_jcs, sum(decode(t.plan1210, 5, 1, 0)) as qys_gss,   sum(decode(t.plan1221,2,1,0)) as qys_uf,  sum(decode(t.plan1221,1,1,0)) as qys_fd,sum(decode(t.plan1221,4,1,0)) as qys_qt,"+
				    " sum(decode(t.plan1221,3,1,0)) as qys_ucc, sum(case when t.plan1221 is null then 1 else 0 end) as qys_ucmt,sum(case when t.plan1224=2 or t.plan1224 = 3 then 1 else 0 end ) as qys_las,  sum(decode(t.plan1226,1,1,0)) as qys_yzx,  sum(decode(t.plan1226,2,1,0)) as qys_drrbf,"+
				    " sum(decode(t.plan1226,3,1,0)) as qys_bd, sum(decode(t.plan1226,4,1,0)) as qys_infobf,  sum(decode(t.plan1226,5,1,0)) as qys_upro "+
				    " from plan12 t "+
				    " left join dm_codetable_data dm on dm.codetablename = 'DB064' and dm.cid = t.plan1204   where t.parentid in ("+faidQuery+")  and t.plan1204 in ("+qxidQuery+") group by t.plan1204,dm.caption) qy on qy.plan1204 = cp.cid"+ 
				    " left join (select count(distinct p2.plan0201) as cyrys ,p2.plan0205  from plan02 p2  where p2.plan0204 = 2 and p2.parentid in ("+faidQuery+")  and p2.plan0205 in ("+qxidQuery+") group by p2.plan0205) cyry on cyry.plan0205 = qy.plan1204 "+
				    " left join (select count(*) zs,t.plan0404 from plan04 t where t.parentid = ("+lasttQuery+") group by t.plan0404) zqys on zqys.plan0404 = qy.plan1204 "+
				    " left join (select count(*) zs,t.plan0205 from plan02 t where t.parentid = ("+lasttQuery+") group by t.plan0205) zrys on zrys.plan0205 = qy.plan1204 "+
				    " where cp.codetablename = 'DB064' and cp.cid in ("+qxidQuery+") order by cp.cid ";
		    List<Map<String,Object>> list = this.jdbcTemplate.queryForList(querysql);
		    
		    ExtGrid eg= new ExtGrid();
		    int index=1;
		    int ryzsAll=0;
		    int cyrysAll=0;
		    int zqysAll=0;
		    int qys_ufAll=0;
		    int qys_fdAll=0;
		    int qys_uccAll=0;
		    int qys_qtAll=0;
		    int qys_ucmtAll=0;
		    int qys_jcsAll=0;
		    int qys_lasAll=0;
		    int qys_yzxAll=0;
		    int qys_drrbfAll=0;
		    int qys_bdAll=0;
		    int qys_infobfAll=0;
		    int qys_uproAll=0;
		    int qys_gssAll=0;
		    
		    for(Map<String,Object> map:list){
				ExtObject eo = new ExtObject();
				eo.add("xh", index++);
				
				eo.add("qx", map.get("qxmc"));
				if(map.get("ryzs")!=null||"".equals(map.get("ryzs"))){
					int ryzsNum=Integer.valueOf(map.get("ryzs").toString());
					ryzsAll+=ryzsNum;
					eo.add("ryzs", ryzsNum);
				}else{
					eo.add("ryzs", "");
				}
				if(map.get("cyrys")!=null||"".equals(map.get("cyrys"))){
					int cyrysNum=Integer.valueOf(map.get("cyrys").toString());
					cyrysAll+=cyrysNum;
					eo.add("cyrys", cyrysNum);
				}else{
					eo.add("cyrys", "");
				}
				if(map.get("zqys")!=null||"".equals(map.get("zqys"))){
					int zqysNum=Integer.valueOf(map.get("zqys").toString());
					zqysAll+=zqysNum;
					eo.add("zqys", zqysNum);
				}else{
					eo.add("zqys", "");
				}
				if(map.get("qys_uf")!=null||"".equals(map.get("qys_uf"))){
					int qys_ufNum=Integer.valueOf(map.get("qys_uf").toString());
					qys_ufAll+=qys_ufNum;
					eo.add("qys_uf", qys_ufNum);
				}else{
					eo.add("qys_uf", "");
				}
				if(map.get("qys_fd")!=null||"".equals(map.get("qys_fd"))){
					int qys_fdNum=Integer.valueOf(map.get("qys_fd").toString());
					qys_fdAll+=qys_fdNum;
					eo.add("qys_fd", qys_fdNum);
				}else{
					eo.add("qys_fd", "");
				}
				if(map.get("qys_ucc")!=null||"".equals(map.get("qys_ucc"))){
					int qys_uccNum=Integer.valueOf(map.get("qys_ucc").toString());
					qys_uccAll+=qys_uccNum;
					eo.add("qys_ucc", qys_uccNum);
				}else{
					eo.add("qys_ucc", "");
				}
				if(map.get("qys_qt")!=null||"".equals(map.get("qys_qt"))){
					int qys_qtNum=Integer.valueOf(map.get("qys_qt").toString());
					qys_qtAll+=qys_qtNum;
					eo.add("qys_qt", qys_qtNum);
				}else{
					eo.add("qys_qt", "");
				}
				if(map.get("qys_ucmt")!=null||"".equals(map.get("qys_ucmt"))){
					int qys_ucmtNum=Integer.valueOf(map.get("qys_ucmt").toString());
					qys_ucmtAll+=qys_ucmtNum;
					eo.add("qys_ucmt",qys_ucmtNum);
				}else{
					eo.add("qys_ucmt", "");
				}
				if(map.get("qys_jcs")!=null||"".equals(map.get("qys_jcs"))){
					int qys_jcsNum=Integer.valueOf(map.get("qys_jcs").toString());
					qys_jcsAll+=qys_jcsNum;
					eo.add("qys_jcs",qys_jcsNum);
				}else{
					eo.add("qys_jcs", "");
				}
				if(map.get("qys_las")!=null||"".equals(map.get("qys_las"))){
					int qys_lasNum=Integer.valueOf(map.get("qys_las").toString());
					qys_lasAll+=qys_lasNum;
					eo.add("qys_las", qys_lasNum);
				}else{
					eo.add("qys_las", "");
				} 
				if(map.get("qys_yzx")!=null||"".equals(map.get("qys_yzx"))){
					int qys_yzxNum=Integer.valueOf(map.get("qys_yzx").toString());
					qys_yzxAll+=qys_yzxNum;
					eo.add("qys_yzx", qys_yzxNum);
				}else{
					eo.add("qys_yzx", "");
				} 
				if(map.get("qys_drrbf")!=null||"".equals(map.get("qys_drrbf"))){
					int qys_drrbfNum=Integer.valueOf(map.get("qys_drrbf").toString());
					qys_drrbfAll+=qys_drrbfNum;
					eo.add("qys_drrbf", qys_drrbfNum);
				}else{
					eo.add("qys_drrbf", "");
				} 
				
				if(map.get("qys_bd")!=null||"".equals(map.get("qys_bd"))){
					int qys_bdNum=Integer.valueOf(map.get("qys_bd").toString());
					qys_bdAll+=qys_bdNum;
					eo.add("qys_bd", qys_bdNum);
				}else{
					eo.add("qys_bd", "");
				} 
				if(map.get("qys_infobf")!=null||"".equals(map.get("qys_infobf"))){
					int qys_infobfNum=Integer.valueOf(map.get("qys_infobf").toString());
					qys_infobfAll+=qys_infobfNum;
					eo.add("qys_infobf", qys_infobfNum);
				}else{
					eo.add("qys_infobf", "");
				}
				if(map.get("qys_upro")!=null||"".equals(map.get("qys_upro"))){
					int qys_uproNum=Integer.valueOf(map.get("qys_upro").toString());
					qys_uproAll+=qys_uproNum;
					eo.add("qys_upro",qys_uproNum);
				}else{
					eo.add("qys_upro", "");
				} 
				
				if(map.get("qys_gss")!=null||"".equals(map.get("qys_gss"))){
					int qys_uproNum=Integer.valueOf(map.get("qys_gss").toString());
					qys_gssAll+=qys_uproNum;
					eo.add("qys_gss",qys_uproNum);
				}else{
					eo.add("qys_gss", "");
				} 
				eg.rows.add(eo);
			}
		    ExtObject eo = new ExtObject();
		    eo.add("qx", "合计");
		    eo.add("ryzs", ryzsAll);
			eo.add("cyrys", cyrysAll);
			eo.add("zqys",zqysAll);
			eo.add("qys_uf", qys_ufAll);
			eo.add("qys_fd", qys_fdAll);
			eo.add("qys_ucc", qys_uccAll);
			eo.add("qys_qt", qys_qtAll);
			eo.add("qys_ucmt", qys_ucmtAll);
			eo.add("qys_jcs", qys_jcsAll);
			eo.add("qys_las", qys_lasAll);
			eo.add("qys_yzx", qys_yzxAll);
			eo.add("qys_drrbf", qys_drrbfAll);
			eo.add("qys_bd", qys_bdAll);
			eo.add("qys_infobf", qys_infobfAll);
			eo.add("qys_upro",qys_uproAll);
			eo.add("qys_gss",qys_gssAll);
			eg.rows.add(eo);
			String sub=gridcolmun.substring(1, gridcolmun.length());
			sub="[{\"header\":\"序号\",\"field\":\"xh\",\"visible\":true},"+sub;
		    result = this.exportExcel(sub,eg);
	} catch (Exception e) {
		e.printStackTrace();
	}		
		return result;
	 
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
            titleList.add("对象数据来源");
            titleList.add("专项名称");
            
            fieldList.add("dq");
            fieldList.add("jgdm");
            fieldList.add("dwmc");
            fieldList.add("dz");
            fieldList.add("lxr");
            fieldList.add("phone");
            fieldList.add("jcr");
            fieldList.add("sjly");
            fieldList.add("zxjc");

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
			eRow.add("zxjc",  this.getZXJC(fzid,ire.get("PLAN1201",IRecord.class).getRecordId().toString()));
            rowlist2.add(eRow);
        }
        rtnExtGrid.rows.addAll(rowlist2);
        rtnlist = rtnExtGrid.toString();
        
        return rtnlist;
    }
    
	private String getZXJC(String fzid,String  planId) {
		List<Map<String,Object>> SPList = this.jdbcTemplate.queryForList("select  vp.SP0101 from v_ssj_sp vp where  vp.PLAN00='"+fzid+"' and vp.sp0201 ='"+planId+"'");
		String zxjc=null;
		
		if(SPList.size()>0){
			for(Map<String,Object> map:SPList){
				zxjc=map.get("SP0101").toString();
			}
		}
		return zxjc;
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
		if(retStr.length()>0){
			return retStr.substring(0, retStr.length()-1);
		}else{
			return "";
		}
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
	 * 展示的数据不需要程序转换(即可以通过sql一次查询得到显示结果的)的都可以用这个导出方法，
	 * 如果需要程序转换就要在对应个性化的方法写对应的转换展示字段
	 * 使用miniui的方法能够得到对应的列的header和filed,注意sql的拼写要与grid的filed一致
	 * @param gridcolmun 前端传入的grid的列属性集合，通过mini.encode(grid.columns)得到
	 * @param sql 查询导出列表数据的sql
	 * @return
	 */
	@Action
	public String exportExcel(String gridcolmun,String sql) throws Exception {
		JSONArray ja = JSONArray.fromObject(gridcolmun);
		List<String> titleList = this.getTitleList(ja);
	    List<String> fieldList = this.getFieldList(ja);
        
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

	
	//考虑内存溢出的情况，导出表数据过大
	/**
	 * 导出接口二，传入对应的导出列名和对应的简称名，以及数据
	 * @param gridcolmun 对应的标头列名和简称名集合
	 * @param listString	对应的导出数据集合
	 * @return
	 * @throws Exception
	 */
    @Action
    public String exportExcel(String gridcolmun,ExtGrid listgrid) throws Exception {
    	JSONArray ja = JSONArray.fromObject(gridcolmun);
    	List<String> titleList = this.getTitleList(ja);
	    List<String> fieldList = this.getFieldList(ja);
    	ExtResultObject ero = new ExtResultObject();
        String path = "/temp/gjxfj.xls";
        
        try {
            String filePath = System.getProperty("resourceFiles.location")+path;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            String sheetName = "随机方案清单";
            JSONObject a = JSONObject.fromObject(listgrid.toString());
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
    
    /**
     * 得到列表头的内容
     * @param gridcolum
     * @return
     */
    private List<String> getTitleList(JSONArray gridcolum){
    	List<String> titleList = new ArrayList<String>();
        for (int i = 0; i < gridcolum.size(); i++) {
            JSONObject jsonObject = (JSONObject) gridcolum.get(i);
            if(jsonObject.getBoolean("visible")){
            	  if(jsonObject.get("header")!=null)
                  	titleList.add(jsonObject.get("header").toString());
            }
        }
        return titleList;
    }

    /**
     * 得到列表简称头
     * @param gridcolum
     * @return
     */
    private List<String> getFieldList(JSONArray gridcolum){
    	List<String> fieldList = new ArrayList<String>();
        for (int i = 0; i < gridcolum.size(); i++) {
            JSONObject jsonObject = (JSONObject) gridcolum.get(i);
            if(jsonObject.getBoolean("visible")){
            	 if(jsonObject.get("field")!=null)
                   	fieldList.add(jsonObject.get("field").toString());
            }
        }
        return fieldList;
    }
}
