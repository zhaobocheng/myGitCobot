<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>人员列表</title>
</head>
<body>
<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url=""  idFiled="id"  allowResize="true" showPager="false" >
    <div property="columns">
       <div type="indexcolumn" headerAlign="center">序号</div>
        <div field="id" headerAlign="center" visible="false">id</div>
        <div name="mc" field="mc" width="50" headerAlign="center" align="center">姓名</div>
        <div field="xb" width="30" headerAlign="center" align="center">性别</div>
        <div field="sfzh" width="60" headerAlign="center"  align="center">身份证号</div>
        <div field="chcs" width="50" headerAlign="center" >本年度已参与抽查次数</div>
    </div>
</div>

<script>
mini.parse();
<%String faid = request.getParameter("faid"); %>
var faid ='<%=faid %>';
var flag = '<%= request.getParameter("flag") %>';
var zone = '<%= request.getParameter("zone") %>';

var grid = mini.get("datagrid");
var url = "/ssj/ssjscheme/SchemeInfoShow/getRYcount/" +faid+"?theme=none&flag="+flag+"&zone="+zone;
grid.setUrl(url);
grid.load();
</script>
</body>
</html>