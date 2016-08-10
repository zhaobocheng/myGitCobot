<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法方案</title>
<%String faid = request.getParameter("faid"); %>
</head>
<body>

<div style="padding:5px;10px;5px;0">
<table width="700px" >
	<tr>
		<td  > <span>任务名称：</span><input class="mini-textbox" id="famc" readOnly="true" style="width:270px;"/> </td>
		<td  > <span>抽查月份：</span><input class="mini-textbox" id="ccyf" readOnly="true"  style="width:270px;"/> </td>
	</tr>
	<tr>
		<td><span>参与抽查执法人员数：</span><input class="mini-textbox" id="rs" readOnly="true"  style="width:200px;"/></td>
		<td><span>参与抽查企业数：</span><input class="mini-textbox" id="qys" readOnly="true"  style="width:228px;"/></td>
	</tr>
</table>
	<a class="mini-button" iconCls = "icon-new" onclick="" >提交</a>
	<a class="mini-button" iconCls = "icon-new" onclick="" >重新生成</a>
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()" >导出Excel</a>
</div>
<div class="mini-fit">
	<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url="" showPager="false" allowResize="true">
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
</div>

<script >
mini.parse();
var faid ='<%=faid %>';
$.ajax({
	url:'/ssj/ssjScheme/SchemeInfoShow/getViewBaseInfo/'+faid+'?theme=none',
	type:'get',
	data:{},
	success:function(e){
		var info=mini.decode(e);
		mini.get("famc").setValue(info.mc);
		mini.get("ccyf").setValue(info.yf);
		mini.get("rs").setValue(info.rs);
		mini.get("qys").setValue(info.qys);
	}
});



var grid = mini.get("datagrid");
var url = '/ssj/ssjScheme/SchemeInfoShow/getSchemeDate/'+faid+'?theme=none';
grid.setUrl(url);
grid.load();

importExc = function(){
	var faid = mini.get("faid").value;
	grid.loading("正在导出，请稍后......");
	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/exportExcel/'+faid+"?theme=none",
		type:'get',
		success:function(e){
			var inf = mini.decode(e);
			if(inf.flag){
				location.href = decodeURI("/ResourceFiles"+inf.path);
			}else{
				alert("导出失败！");
			}
			grid.reload();
		}
	});
}



</script>
</body>
</html>