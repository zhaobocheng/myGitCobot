<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>权力清单</title>
</head>
<body>
<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
	<span>权力清单编码：</span><input class="mini-textbox" id="qlbm" name="rwmc" style="width:250px;" onvaluechanged="findRow()"/>
	<span>权力清单名称：</span><input class="mini-textbox" id="qlmc" name="qlmc" style="width:250px;" onvaluechanged="findRow()"/>								 
	<span>权力分类：</span>  <input class="mini-combobox" id="qlfl" name="qlfl" style="width:250px;" onvaluechanged="findRow()" textField="text" valueField="id"   url="/ssj/powerlist/PowerList/getPowerFL?theme=none"/>
	 <a class="mini-button" id="find" iconCls = "icon-find"  onclick="findRow()">查找</a>
</div>
 
  
<div class="mini-fit">
<div class="mini-datagrid" id="datagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getPowerListData?theme=none" showFooter="true" idField="id"
	pageSize="20" sizeList="[20,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" width="20" headerAlign="center">序号</div>
		<div field="qlqdbm" width="80" headerAlign="center">权力清单编码</div>
		<div field="qlsxmc" width="120" headerAlign="center">权力清单名称</div>
		<div field="qlfl" width="80" headerAlign="center">权力分类</div>
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
	var qlfl = mini.get("qlfl").value;
	datagrid.load({qlbm:qlbm,qlmc:qlmc,qlfl:qlfl});
}

</script>

</body>
</html>