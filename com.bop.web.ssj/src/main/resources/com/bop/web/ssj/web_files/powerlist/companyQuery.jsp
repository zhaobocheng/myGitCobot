<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>企业责任查询</title>
</head>
<body>
<div  class="input_form" class="width:100%;">
	<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
		<span>企业名称：</span><input class="mini-textbox" id="qymc" name="qymc" style="width:260px;" onvaluechanged="findRow()"/>
		<span>机构代码：</span><input class="mini-textbox" id="jgdm" name="jgdm" style="width:260px;" onvaluechanged="findRow()"/>
		<span>机构信用代码：</span><input class="mini-textbox" id="jgxydm" name="jgxydm" style="width:260px;" onvaluechanged="findRow()"/>
		</br>
		<span>检查事项：</span><input class="mini-textbox" id="jcsx" name="jcsx" style="width:260px;" onvaluechanged="findRow()"/> 
		<span>权利编码：</span><input class="mini-textbox" id="qlbm" name="qlbm" style="width:260px;" onvaluechanged="findRow()"/>
		<span style="padding-left:20px">
			<a class="mini-button" id="find" iconCls = "icon-find"  onclick="findRow()">查找</a>
		</span>
    </div>
</div>

<div class="mini-fit">
<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getCompanyQData?theme=none" showFooter="true" idField="id"
	pageSize="20" sizeList="[20,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" width="20" headerAlign="center">序号</div>
		<div field="qymc" width="120" headerAlign="center">企业名称</div>
		<div field="dm" width="60" headerAlign="center">机构代码</div>
		<div field="dz" width="200" headerAlign="center">企业地址</div>
		<div field="lxr" width="60" headerAlign="center">联系人</div>
		<div field="sjjcsx" width="120" headerAlign="center">涉及的检查事项</div>
		<div field="sjqlqd" width="120" headerAlign="center">涉及的权力清单</div>
	</div>
</div>
</div>
<script >
mini.parse();
var datagrid=mini.get("datagrid");
datagrid.load();


function findRow(){
	var qymc = mini.get("qymc").value;
	var jgdm = mini.get("jgdm").value;
	var jgxydm = mini.get("jgxydm").value;
	var jcsx = mini.get("jcsx").value;
	var qlbm = mini.get("qlbm").value;
	datagrid.load({qymc:qymc,jgdm:jgdm,jgxydm:jgxydm,jcsx:jcsx,qlbm:qlbm});
}

</script>

</body>
</html>