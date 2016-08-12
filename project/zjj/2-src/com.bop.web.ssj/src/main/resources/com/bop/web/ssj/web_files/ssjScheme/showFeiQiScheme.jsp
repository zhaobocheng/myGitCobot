<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>废弃方案浏览</title>
</head>
<body>
<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url=""  idFiled="id"  allowResize="true" showPager="false" >
    <div property="columns">
        <div type="indexcolumn" headerAlign="center">序号</div>
        <div field="id" headerAlign="center" visible="false">id</div>
        <div name="mc" field="mc" width="50" headerAlign="center" align="center">方案名称</div>
        <div name="nd" field="nd" width="50" headerAlign="center" align="center">年度</div>
        <div field="yf" width="30" headerAlign="center" align="center">月份</div>
        <div field="yy" width="60" headerAlign="center"  align="center">废弃原因</div>
    </div>
</div>
<script>
mini.parse();
<%String faid = request.getParameter("faid"); %>
var faid ='<%=faid %>';
var flag = '<%= request.getParameter("flag") %>';

var grid = mini.get("datagrid");
var url = "/ssj/ssjscheme/SchemeResult/getFQScheme/" +faid+"?theme=none&flag="+flag;
grid.setUrl(url);
grid.load();
</script>
</body>
</html>