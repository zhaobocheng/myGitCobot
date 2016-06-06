<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>检查事项</title>
</head>
<body>
<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getCheckData?theme=none" showFooter="true" idField="id"
	pageSize="20" sizeList="[20,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" width="10" headerAlign="center">序号</div>
		<div field="jcsxmc" width="60" headerAlign="center" align="center">检查事项名称</div>
		<div field="jcdx" width="80" headerAlign="center" align="center">检查对象</div>
		<div field="ccyj" headerAlign="center"  align="center">抽查依据</div>
	</div>
</div>

<script >
mini.parse();
var datagrid=mini.get("datagrid");
datagrid.load();

</script>

</body>
</html>