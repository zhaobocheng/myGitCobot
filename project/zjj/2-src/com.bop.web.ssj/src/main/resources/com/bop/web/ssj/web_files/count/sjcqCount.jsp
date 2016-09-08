<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>随机抽查情况统计</title>
</head>
<body>

	<span style="maggin-left:20px">统计区县：</span> <div id="cbl1" class="mini-checkboxlist" repeatItems="12" repeatLayout="table" textField="text" valueField="id" url="/ssj/count/CountPage/getCheckDate" ></div>
	<div class="mini-toolbar">
		<span>统计开始时间：</span><input class="mini-datepicker" id="starttime" style="width:150px;" />
		<span>统计结束时间：</span><input class="mini-datepicker" id="endtime" style="width:150px;" />
	</div>

	<div class="mini-fit"> 
		<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" showPager="false" allowHeaderWrap="true" url="" >
			<div property="columns">
				<div property="index" width="30">序号</div>
				<div field="qx" width="60" headerAlign="center" align="center">区县</div>
				
				<div field="ryzs" width="60" headerAlign="center" align="center">总执法人员</div>
				<div field="cyrys" width="60" headerAlign="center" align="center">参与人员数</div>
				<div field="zqys" width="50" headerAlign="center" align="center">总企业数</div>
				<div header="抽查企业数" headerAlign="center">
	                <div property="columns">
	                    <div field="qys_uf" width="60" headerAlign="center">未发现问题</div>
	                    <div field="qys_fd" width="50" headerAlign="center">发现问题</div>
	                    <div field="qys_ucc"  width="60" headerAlign="center">未成功检查</div>
	                </div>
				</div>
				<div field="bz" width="40" headerAlign="center" align="center">立案数</div>
				<div header="企业情况" headerAlign="center">
	                <div property="columns" headerAlign="center">
	                    <div field="qys_yzx" width="60" headerAlign="center">企业已注销</div>
	                    <div field="qys_drrbf" width="60" headerAlign="center">企业生产地与注册地不符</div>
	                    <div field="qys_bd"  width="60" headerAlign="center">企业执照未变，企业找不到</div>
	                    <div field="qys_infobf"  width="60" headerAlign="center">企业实际经营内容和信息不符</div>
	                    <div field="qys_upro"  width="60" headerAlign="center">企业停产停业</div>
	                </div>
				</div>
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
	</script>
</body>
</html>