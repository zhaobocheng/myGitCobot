<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法方案</title>
<%String faid = request.getParameter("faid"); %>
<%String flag = request.getParameter("flag"); %>
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
	<a class="mini-button" id="tjbut" iconCls = "icon-new" onclick="commitFa()" >提交</a>
	<%if(!"sc".equals(flag)){ %>
		<a class="mini-button" id="recrebut" iconCls = "icon-new" onclick="createFa()" >重新生成</a>
	<%}else{} %>
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
		if(info.zt==5){
			mini.get("tjbut").setEnabled(false);
			var recrebut = mini.get("recrebut");
			if(recrebut!=undefined){
				recrebut.setEnabled(false);
			}
		}else{
			
		}
	}
});

var grid = mini.get("datagrid");
var url = '/ssj/ssjScheme/SchemeInfoShow/getSchemeDate/'+faid+'?theme=none';
grid.setUrl(url);
grid.load();

commitFa = function(){
    mini.mask({
        el: document.body,
        cls: 'mini-mask-loading',
        html: '方案提交中，请稍等...'
    });
	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/commitSchemeDate/'+faid+"?theme=none",
		type:'get',
		success:function(e){
			 mini.unmask(document.body);
			if(e=="success"){
				alert("提交完毕！");
				gird.reload();
			}
		}
	});
}


//生成方案
createFa=function(){
    $.ajax({
    	url:'/ssj/ssjScheme/CreateScheme/isRepertCreate/'+faid,
    	type:'get',
    	success:function(e){
    		var info = mini.decode(e);
    		if(info.flag==4){
    			 mini.confirm("该任务已经生成方案，重新生成将产生记录,确定重新生成？", "确定？",
    			            function (action) {
    			                if (action == "ok") {
    			                	createRepert('replace');
    			                } else {
    			                }
    			            }
    			        );
 	 		}else if(info.flag==3){
 	 			createRepert('new');
    		}else{
    			alert(info.text);
    		}
    	}
    });
}

createRepert = function(e){
    mini.mask({
        el: document.body,
        cls: 'mini-mask-loading',
        html: '方案生成中，请稍等...'
    });
	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/createSchemeData/'+faid,
		type:'get',
		data:{isreplace:e},
		success:function(e){
			if(e=="seccess"){
				mini.unmask(document.body);
				gridLoad(faid);
			}else if(e=="false"){
				alert("有未设置人员或企业数的区县请先设置！");
			}
		}
	}); 
}

importExc = function(){
	grid.loading("正在导出，请稍后......");
	var columns = grid.columns;
	var json = mini.encode(columns);

	$.ajax({
		url:'/ssj/ssjscheme/ExportExcle/exportExcel/'+faid+'?theme=none',
		type:'get',
		data:{gridcolmun:json},
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


gridLoad = function(value){
	grid.setUrl(url);
	grid.reload();
}

</script>
</body>
</html>