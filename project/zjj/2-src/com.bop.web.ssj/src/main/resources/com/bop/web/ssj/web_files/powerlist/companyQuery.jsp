<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>企业责任查询</title>
</head>
<body>
<div  class="input_form" class="width:100%;">
	<table  style="width:100%;">
		<tr>
			<th width="20%">企业名称</th>
			<td width="50%"><input class="mini-textbox" id="qymc" name="qymc" style="width:100%" /></td>
			<td><a class="mini-button" iconCls="icon-find" onclick="findRow">查询</a></td>
		</tr>
		<tr>
			<th width="20%">企业代码</th>
			<td width=50%"><input class="mini-textbox" id="qydm" style="width:100%" /></td>
			<td><a class="mini-button" iconCls="icon-find" onclick="findRow">查询</a></td>
		</tr>
	</table>
</div>
<div class="mini-fit">
<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getCompanyQData?theme=none" showFooter="true" idField="id"
	pageSize="20" sizeList="[20,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" width="20" headerAlign="center">序号</div>
		<div field="qymc" width="120" headerAlign="center">企业名称</div>
		<div field="dm" width="60" headerAlign="center">代码</div>
		<div field="dz" width="200" headerAlign="center">地址</div>
		<div field="lxr" width="60" headerAlign="center">联系人</div>
		<div field="sjsx" width="120" headerAlign="center">涉及的检查事项</div>
	</div>
</div>
</div>
<script >
mini.parse();
var datagrid=mini.get("datagrid");
datagrid.load();


function findRow(){
	var qymc = mini.get("qymc").value;
	var qydm = mini.get("qydm").value;

	datagrid.load({qymc:qymc,qydm:qydm});
}

</script>

</body>
</html>