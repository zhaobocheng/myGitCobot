<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>企业列表展示</title>
</head>
<body>
<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url=""  idFiled="id"  allowResize="true" showPager="false" >
    <div property="columns">
        <div type="indexcolumn" headerAlign="center">序号</div>
        <div field="id" headerAlign="center" visible="false">id</div>
        <div name="jgdm" field="jgdm" width="50" headerAlign="center" align="center">机构代码</div>
        <div name="dwmc" field="dwmc" width="50" headerAlign="center" align="center">单位名称</div>
        <div field="dz" width="30" headerAlign="center" align="center">地址</div>
        <div field="lxr" width="60" headerAlign="center"  align="center">联系人</div>
        <div field="phone" width="50" headerAlign="center" >电话</div>
    </div>
</div>
<script>
mini.parse();
<%String faid = request.getParameter("faid"); %>
var faid ='<%=faid %>';
var flag = '<%= request.getParameter("flag") %>';
var zone = '<%= request.getParameter("zone") %>';

var grid = mini.get("datagrid");
var url = "/ssj/ssjscheme/SchemeInfoShow/getORGcount/" +faid+"?theme=none&flag="+flag+"&zone="+zone;
grid.setUrl(url);
grid.load();
</script>
</body>
</html>