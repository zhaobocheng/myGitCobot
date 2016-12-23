<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>查看手工采集记录</title>
</head>
<body>
<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
	<!-- 	<span>企业名称：</span><input class="mini-textbox"  id="sxmc_sousuo" name="sxmc" style="width:230px;" onvaluechanged="findItemRow()"/>
	 <span>事项业务分类：</span><input class="mini-combobox" id="sxflei" name="sxfl" style="width:230px;" onvaluechanged="findItemRow()" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemFL?theme=none"/>
	<span>是否显示废弃：</span>
	<label><input style="width:20px;" name="feiqi" type="radio" checked="checked" onclick="findItemRow()" value="1" />是</label> 
	<label><input style="width:20px;" name="feiqi" type="radio" value="2" onclick="findItemRow()" />否 </label>
	</br>
	<span>权力名称：</span><input class="mini-textbox" id="qlmcing" name="qlmc" style="width:230px;" onvaluechanged="findItemRow()"/>
	<span>权力编码：</span><input class="mini-textbox" id="qlbma" name="qlbm" style="width:230px;" onvaluechanged="findItemRow()"/>
	<span style="padding-left:50px">
		<a  class="mini-button" id="find" iconCls = "icon-find"  onclick="findItemRow()">查找</a>
	</span> -->
</div>
<div class="mini-fit">
	<div class="mini-datagrid" id="datagridItem" style="width:100%;height:100%;"
		 url="/ssj/powerlist/PowerList/getLogDate?theme=none" showFooter="true" idField="id"
		pageSize="20" sizeList="[20,30,50,100]">
		<div property="columns">
			<div type="indexcolumn" width="20" headerAlign="center">序号</div>
			<div field="fileName" width="60" headerAlign="center" align="center">文件名</div>
			<div field="loadDate" name="transDate"	width="60" headerAlign="center"  align="center">采集日期</div>
			<div field="operUser"  width="60" headerAlign="center"  align="center">采集人</div>
			<div field="totalNum"  width="60" headerAlign="center"  align="center">总数量</div>
			<div field="successNum"  width="60" headerAlign="center"  align="center">成功数量</div>
			<div field="errorNum"  width="60" headerAlign="center"  align="center">失败数量</div>
			<div field="path" name="viewFile"    headerAlign="center"  align="center">附件</div>
		</div>
	</div>
</div>
<script >
mini.parse();
var datagrid=mini.get("datagridItem");
datagrid.load();
//文件下载
datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "viewFile") {
    	var id=record.id;
    	var s=value.split(";");
    	var flag="0";
        if(s[1]=="null"){  //??????如果字符串中含有/  ;等字符 值无法传到后台   退而求次 改成传id 后台在获取其他列的值
        	flag2="1";
        	e.cellHtml = '<a href=\"/ssj/powerlist/PowerList/download/'+id+'/'+flag+'?theme=none\"  style="color:blue;width:80px">查看采集的文件</a>&nbsp;&nbsp;&nbsp;';
        }else{
        	var s= '<a href=\"/ssj/powerlist/PowerList/download/'+id+'/'+flag+'?theme=none\"  style="color:blue;width:80px">查看采集的文件</a>&nbsp;&nbsp;&nbsp;';
        	flag="1";
        	e.cellHtml=s+'<a href=\"/ssj/powerlist/PowerList/download/'+id+'/'+flag+'?theme=none\"  style="color:blue;width:80px">查看错误信息</a>&nbsp;';
        }
    }
});
function viewUpload(path){
	windows.load("");  
}

function viewError(){
	
}
 
 
 
 
//操作列的超链接
datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "action") {
        e.cellStyle = "color:#fceee2;font-weight:bold;";
        e.cellHtml = '<a href="javascript:auto(\'' + record.sxId + '\')"><button style="color:blue">自动</button> </a>&nbsp;'+
        '<a href="javascript:hand(\'' + record.sxId + '\')"><button style="color:blue">手动</button> </a>&nbsp;'
           
    }
});
//格式化错误信息grid 中的时间值
var handsDataGrid=mini.get("handsDataGrid");
handsDataGrid.load();
function FormatDate (strTime) {
    var date = new Date(strTime);
    return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
}
handsDataGrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "formatTime") {
    	e.cellHtml =FormatDate(value);
    }
});
//中止
function cancel(){
	mini.get("errorData").hide();
	 $("#temp").val(0);
}
</script>
</body>
</html>