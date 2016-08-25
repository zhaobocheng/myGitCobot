<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
<head>
<meta name="decorator" content="miniui">
<title>废弃方案浏览</title>
<%
	String yf=request.getParameter("yf");
	String nd=request.getParameter("nd");
	String faid = request.getParameter("faid");
%>
</head>
<body>
<div><h2><span><%=nd%> 年<%=yf %>月废弃执法方案</span></h2></div>
<div>
	<span>生成的次数：</span><input class="mini-combobox" id="sccs" style="width:150px;"  onvaluechanged="valueChange" textField="mc" valueField="id" url=""/>
</div>
<div id="datagrid" class="mini-datagrid" style="width:100%;height:80%;" url=""  idFiled="id"  allowResize="false" showPager="false" >
    <div property="columns">
	        <div type="indexcolumn">序号</div>
	        <div field="jgdm" width="80">机构代码</div>
	        <div field="dwmc" width="60" align="right">单位名称</div>
	        <div field="dz" width="80" >地址</div>
	        <div field="lxr" width="80" >联系人</div>
	        <div field="phone" width="60" >电话</div>
	        <div field="jcnr" width="80" >检查内容</div>
	        <div field="jcr" width="80" >检查人</div> 
	         <div field="jcrid" width="80" visible="false" >检查人id</div> 
	        <div field="sjly" width="80" >涉及领域</div>     
    </div>
</div>
<script>
mini.parse();

var faid ='<%=faid %>';
var flag = '<%= request.getParameter("flag") %>';

var sccs = mini.get("sccs");
sccs.setUrl("/ssj/ssjscheme/SchemeResult/getFQScheme/"+faid+"?theme=none");
sccs.select(0);

valueChange = function(){
	var nd = mini.get("sccs").value;

	url = "/ssj/ssjscheme/SchemeResult/getFQCYQYData/"+nd+"?theme=none";
	grid.setUrl(url);
	grid.reload();
}

var grid = mini.get("datagrid");
var url = "/ssj/ssjscheme/SchemeResult/getFQCYQYData/" +sccs.value+"?theme=none";
grid.setUrl(url);
grid.load();


</script>
</body>
</html>