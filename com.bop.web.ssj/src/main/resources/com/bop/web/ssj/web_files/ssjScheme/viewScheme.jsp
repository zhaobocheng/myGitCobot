<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>方案浏览</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<span>抽查方案：</span><input class="mini-combobox" id="faid" style="width:150px;" textField="text" valueField="id" onvaluechanged="valueChange" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/>
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()" >导出Excel</a>
</div>

<div id="treegrid" class="mini-treegrid" style="width:100%;height:100%;" url="" showTreeIcon="true" 
    treeColumn="dq" idField="dqid" parentField="ParentDqId" resultAsTree="false"  allowResize="true" expandOnLoad="true">
    <div property="columns">
        <div type="indexcolumn"></div>
        <div name="dq" field="dq" width="160" >地区</div>
        <div field="jgdm" width="80">机构代码</div>
        <div field="dwmc" width="60" align="right">单位名称</div>
        <div field="dz" width="80" >地址</div>
        <div field="lxr" width="80" >联系人</div>
        <div field="phone" width="60" >电话</div>
        <div field="jcnr" width="80" >检查内容</div>
        <div field="jcr" width="80" >检查人</div> 
         <div field="jcrid" width="80" visible="false" >检查人id</div> 
        <div field="sjly" width="80" >涉及事项</div>                  
    </div>
</div>


<script >
mini.parse();

var zfcom = mini.get("faid");
zfcom.select(0);


var grid = mini.get("treegrid");
var url = '/ssj/ssjScheme/CreateScheme/getSchemeDate/'+zfcom.value+'?theme=none';
grid.load(url);

valueChange = function(e){
	url = "/ssj/ssjScheme/CreateScheme/getSchemeDate/"+e.value+"?theme=none";
	grid.setUrl(url);
	grid.reload();
}

importExc = function(){
	var faid = mini.get("faid").value;
	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/exportExcel/'+faid+"?theme=none",
		type:'get',
		success:function(e){
			var inf = mini.decode(e);
			if(inf.flag){
				location.href = decodeURI("/ResourceFiles"+inf.path);
			}else{
				alert("导出失败咧！");
			}
		}
	});
}
</script>
</body>
</html>