<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>随机抽查情况统计</title>
</head>
<body>
	
	<div class="mini-toolbar">
		<span style="maggin-left:20px">统计区县：</span> <div id="cbl1" class="mini-checkboxlist" repeatItems="11" repeatLayout="table" textField="text" valueField="id" onvaluechanged = "valuechang" url="/ssj/count/CountPage/getCheckDate" ></div>
		<span>统计开始时间：</span><input class="mini-datepicker" id="starttime" style="width:150px;" />
		<span>统计结束时间：</span><input class="mini-datepicker" id="endtime" style="width:150px;" />
		<a class="mini-button" onclick="count()">统计</a>
		<a class="mini-button" iconCls = "icon-new" onclick="importExcCCTJ()">导出Excel</a>
	</div>
	<div class="mini-fit"> 
		<div   class="mini-datagrid" id="datagrid" style="width:100%;height:100%;"
			   allowHeaderWrap="true"  showPager="false" url=""  summaryRenderer="changeStyle()"
			   showSummaryRow="true"   ondrawsummarycell="onDrawSummaryCell">
			<div property="columns">
				<div type="indexcolumn" width="30">序号</div>
				<div field="qx" width="60" headerAlign="center" align="center">区县</div>
				<div field="ryzs" width="50" headerAlign="center" align="center" summaryType="sum">总执法人员</div>
				<div field="cyrys" width="50" headerAlign="center" align="center" summaryType="sum">参与人员数</div>
				<div field="zqys" width="60" headerAlign="center" align="center" summaryType="sum">总企业数</div>
				<div field="qys_jcs" width="40" headerAlign="center" align="center" summaryType="sum">检查任务数</div>
				<div header="抽查企业数" headerAlign="center">
	                <div property="columns">
	                    <div field="qys_uf" width="50" headerAlign="center" summaryType="sum">未发现问题</div>
	                    <div field="qys_fd" width="60" headerAlign="center" summaryType="sum">发现问题</div>
	                    <div field="qys_ucc"  width="50" headerAlign="center" summaryType="sum">未成功检查</div>
	                    <div field="qys_qt"  width="40" headerAlign="center" summaryType="sum">其他</div>
	                     <div field="qys_ucmt"  width="60" headerAlign="center" summaryType="sum">未提交检查结果</div>
	                </div>
				</div>
				<div field="qys_las" width="40" headerAlign="center" align="center" summaryType="sum">立案数</div>
				<div header="企业情况" headerAlign="center">
	                <div property="columns" headerAlign="center">
	                    <div field="qys_yzx" width="50" headerAlign="center" align="center" summaryType="sum">企业已注销</div>
	                    <div field="qys_drrbf" width="60" headerAlign="center" align="center" summaryType="sum">企业生产地与注册地不符</div>
	                    <div field="qys_bd"  width="60" headerAlign="center" align="center" summaryType="sum">企业执照未变，企业找不到</div>
	                    <div field="qys_infobf"  width="70" headerAlign="center" align="center" summaryType="sum">企业实际经营内容和信息不符</div>
	                    <div field="qys_upro"  width="60" headerAlign="center" align="center" summaryType="sum">企业停产停业</div>
	                </div>
				</div>
				<div field="qys_gss" width="40" headerAlign="center" align="center" summaryType="sum">已公示数</div>
			</div>
		</div>
	</div>

	<script>
	mini.parse();
	var datagrid=mini.get("datagrid");
	var url="/ssj/count/CountPage/getsjCq";
	datagrid.setUrl(url);
	datagrid.load();

	valueChangeYear = function(){
	   var id = mini.get("year").getValue();
	   var rwmc = mini.get("rwmc");
	   rwmc.setValue("");
	   var url = "/ssj/count/CountPage/getRW/" +id
	   rwmc.setUrl(url);
	   rwmc.select(0);
	   search();
	}
	
	function search(){
		var id=mini.get("rwmc").value;	
		var year=mini.get("year").value;
		datagrid.load({id:id,year:year});
	}
	
	
	function count(){
		var values = mini.get("cbl1").value;
		var stardata = mini.get("starttime").value;
		var enddata = mini.get("endtime").value;
		
		if(stardata=="" || enddata ==""){
			alert("请选择统计时间！");
			return;
		}else if(values==""){
			alert("请选择统计区县！");
			return;
		}else{
			datagrid.load({qxdm:values,startdata:stardata,enddata:enddata});
		}
	}


	function valuechang(){
		var checkBox = mini.get("cbl1");
		var checkValue = checkBox.value;
		if(checkValue.indexOf("000000")==-1){
			
		}else{
			checkBox.setValue("");
			checkBox.select("000000");
		};
	}
	

	//导出excel
	importExcCCTJ = function(){
		var qx = mini.get("cbl1").value;
		var starttime = mini.get("starttime").value;
		var endtime = mini.get("endtime").value;
		if(qx==""){
			alert("请选择统计区县！");
			return;
		}else if(starttime=="" || endtime ==""){
			alert("请选择统计时间！");
			return;
		}else{
			datagrid.loading("正在导出，请稍后......");
			var columns = datagrid.getBottomColumns();
			var columns1 = getColumns(columns);
			var json = mini.encode(columns1);
			$.ajax({
				url:'/ssj/ssjscheme/ExportExcle/RandomStaticsExportExcel?qx='+qx+'&theme=none',
				type:'post',
				data:{gridcolmun:json,starttime:starttime,endtime:endtime},
				success:function(e){
					var inf = mini.decode(e);
					if(inf.flag){
						location.href = decodeURI("/ResourceFiles"+inf.path);
					}else{
						alert("导出失败！");
					}
					datagrid.reload();
				}
			});
		}
		
	}
	function getColumns(columns) {
	    columns = columns.clone();
	    for (var i = columns.length - 1; i >= 0; i--) {
	        var column = columns[i];
	        if (!column.field) {
	            columns.removeAt(i);
	        }else {
	            var c = { header: column.header, field: column.field,visible:column.visible };
	            columns[i] = c;
	        }
	    }
	    return columns;
	}
	
	//添加统计行————计算统计值
	 function onDrawSummaryCell(e) {
		 if (e.field == "qx") {   //合计    
        	 e.cellHtml = "合计：";
         }else if (e.field == "ryzs") {   //总执法人数    
        	 e.cellHtml = e.value;
         }else if (e.field == "cyrys") {
        	 e.cellHtml = e.value;
         }else if (e.field == "zqys") {//总企业数
        	 e.cellHtml = e.value;
         }else if (e.field == "qys_uf") {//未发现问题
        	 e.cellHtml = e.value;
       	 }else if (e.field == "qys_fd") {//发现问题
       		 e.cellHtml = e.value;
       	 }else if (e.field == "qys_ucc") {//未成功检
       		 e.cellHtml = e.value;
       	 }else if (e.field == "qys_qt") {//其他
             e.cellHtml = e.value;
       	 }else if (e.field == "qys_ucmt") {//未提交检查结果
             e.cellHtml = e.value;
       	 }else if (e.field == "qys_jcs") {//检查数
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_las") {//立案数
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_yzx") {//企业已注销
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_drrbf") {//企业生产地与注册地不符
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_bd") {//企业执照未变，企业找不到
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_infobf") {//企业实际经营内容和信息不符
      		  e.cellHtml = e.value;
  		 }else if (e.field == "qys_upro") {//企业停产停业
      		  e.cellHtml = e.value;
  		 }
		 
		 var grid = e.sender;
		 e.cellStyle="height:40px;text-align:center";
	      
     }
	
	
	</script>
</body>
</html>