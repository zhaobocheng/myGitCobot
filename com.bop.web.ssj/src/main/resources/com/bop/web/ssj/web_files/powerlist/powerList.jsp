<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>权力清单</title>
</head>
<body>
<div  class="input_form" class="width:100%;">
	<table  style="width:100%;">
		<tr>
			<th width="20%">权力编码</th>
			<td width="50%"><input class="mini-textbox" id="qlbm" name="qlbm" style="width:100%" /></td>
			<td><a class="mini-button" iconCls="icon-find" onclick="findRow">查询</a></td>
		</tr>
		<tr>
			<th width="20%">权力名称</th>
			<td width="50%"><input class="mini-textbox" id="qlmc" style="width:100%" /></td>
			<td><a class="mini-button" iconCls="icon-find" onclick="findRow">查询</a></td>
		</tr>
	</table>
</div>
<div class="mini-fit">
<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getPowerListData?theme=none" showFooter="true" idField="id"
	pageSize="20" sizeList="[20,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" width="20" headerAlign="center">序号</div>
		<div field="qlqdbm" width="80" headerAlign="center">权力清单编码</div>
		<div field="qlsxmc" width="220" headerAlign="center">权力事项名称</div>
		<div field="ssqx" width="40" headerAlign="center">编码所在区</div>
	</div>
</div>
</div>
<script >
mini.parse();
var datagrid=mini.get("datagrid");
datagrid.load();


function findRow(){
	var qlbm = mini.get("qlbm").value;
	var qlmc = mini.get("qlmc").value;

	datagrid.load({qlbm:qlbm,qlmc:qlmc});
}

</script>

</body>
</html>