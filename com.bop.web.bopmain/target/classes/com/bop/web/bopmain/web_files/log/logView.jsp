<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui3">
<title>日志展现</title>
</head>
<body>

<div class="input_form" id="formdate">
	<table style="width:100%;heigh:100%;">
		<tr>
			<th width="25%">日志类型</th><td><input class="mini-text" id="type" /></td>
			<th width="25%">日志时间</th><td><input class="mini-text" id="time" /></td>
		</tr>
	</table>
</div>

<div class="mini-fit">
	<div class="mini-datagrid" id="logdategrid" idField="id" pageSize="20" style="width:100%;height:100%;" url="/bopmain/log/logview/getView/ss?theme=none" >
		<div property="columns">
			 <div type="indexcolumn"></div>
			 <div field="logaction" headerAlign="center" allowSort="false" width="150" >操作动作</div>
			 <div field="logtime" headerAlign="center" allowSort="false" width="150" >日志时间</div>
			 <div field="loguser" headerAlign="center" allowSort="false" width="150" >操作用户</div>
			 <div field="querystring" headerAlign="center" allowSort="false" width="150" >带入条件</div>
		</div>
	</div>
</div>
<script>
mini.parse();
var datagrid = mini.get("logdategrid");
datagrid.load();
</script>
</body>
</html>