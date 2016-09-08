<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>机构人员统计</title>
</head>
<body>
	<div class="mini-toolbar">
		<span>抽查年份：</span><input class="mini-combobox" id="year" style="width:150px;" textField="text" valueField="id"   onvaluechanged="valueChangeYear" url="/ssj/count/Countpage/getYData"/>
		<span>抽查任务：</span><input class="mini-combobox" id="rwmc" style="width:150px;" textField="text" valueField="id"  onvaluechanged="search" url=""/>
	</div>
	<div class="mini-fit"> 
		<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" showPager="false"  url="" >
			<div property="columns">
				<div field="qx" width="60" headerAlign="center" align="center">区县</div>
				<div field="ts" width="60" headerAlign="center" align="center">特设</div>
				<div field="jl" width="60" headerAlign="center" align="center">计量</div>
				<div field="xk" width="60" headerAlign="center" align="center">强制认证</div>
				<div field="bz" width="60" headerAlign="center" align="center">标准</div>
				<div field="cp" width="60" headerAlign="center" align="center">产品</div>
				<div field="zj" width="60" headerAlign="center" align="center">企业总计</div>
				<div field="zrs" width="60" headerAlign="center" align="center">总人数</div>
				<div field="ccrs" width="60" headerAlign="center" align="center">抽查人数</div>
			</div>
		</div>
	</div>
	<script>
	mini.parse();
	var datagrid=mini.get("datagrid");
	var url="/ssj/count/CountPage/getCq";
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