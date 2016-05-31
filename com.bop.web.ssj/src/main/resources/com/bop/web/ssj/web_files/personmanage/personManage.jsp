<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法人员管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<span>方案时间：</span><input class="mini-combobox" id="zftime" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/>
	<a class="mini-button" id = "upbur" iconCls="icon-upload" onclick="sbRow()">上报</a>
</div>
<div class="mini-fit">
<div id="layout" class="mini-layout" style="width:100%;height:100%;"  borderStyle="border:solid 1px #aaa;">
    <!-- 左侧，未选择人员-->
    <div title="未参与随机执法人员"   region="west" minWidth = "500px" expanded="true" showSplitIcon="true" >
		<div id="unSelectedgrid" class="mini-datagrid" style="width:100%;height:98%" url=""
		   allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true" multiSelect="true" >
			<div property="columns">
				<div type="checkcolumn" width="20"></div>
				<div type="indexcolumn" width="20">序号</div>
				<div field="id" name="id"  width="100" visible="false">id</div>
				<div field="unSeletedName" name="unSeletedName" width="80" headerAlign="center"   allowSort="true">姓名</div>
				<div field="unSeletedDept" name="unSeletedDept" width="80" headerAlign="center"   allowSort="true">区县</div>
			</div>
		</div>
    </div>

    <!-- 右侧选择人员 -->
    <div title="参与随机执法人员" region="east"   showSplitIcon="true" minWidth = "500px"  >
		<div id="selectedgrid" class="mini-datagrid" style="width:100%;height:98%" url=""
		allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true" multiSelect="true" >
			<div property="columns">
				<div type="checkcolumn" width="20"></div>
				<div type="indexcolumn" width="20">序号</div>
				<div field="id" name="id"  width="100" visible="false">id</div>
				<div field="seletedName" name="seletedName" width="80" headerAlign="center"   allowSort="true">姓名</div>
				<div field="seletedDept" name="seletedDept" width="80" headerAlign="center"   allowSort="true">区县</div>
			</div>
		</div>
    </div>

    <!-- 中间按钮 -->
    <div title="center" region="center" >
    	<div style="width:100%;padding-top:260;">
    		 <div class="mini-toolbar" style="border-bottom:0;padding:2px;">
	            <table style="width:100%;">
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button" id="left" onclick="leftMove()"  >左移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button" id="leftall" onclick="allLeftMove()" >全部左移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button" id="right" onclick="rightMove()"  >右移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button"  id="rightall" onclick="allRightMove()"  >全部右移</a>
	                    </td>
	                </tr>
	            </table>
	        </div>
   	 	</div>
   	 </div>
</div>
</div>
<!-- 绘制页面结束 -->
<script>
mini.parse();

var zfcom = mini.get("zftime");
zfcom.select(0);


var grid = mini.get("selectedgrid");
var ungrid = mini.get("unSelectedgrid");
var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+zfcom.value+"?theme=none";
var url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+zfcom.value+"?theme=none";
grid.setUrl(url);
ungrid.setUrl(url2);
grid.load(); 
ungrid.load(); 


function isup(e){
	var upButton = mini.get("upbur");
	var left = mini.get("left");
	var leftall = mini.get("leftall");
	var right = mini.get("right");
	var rightall = mini.get("rightall");
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/getZT/'+e+'?theme=none',
		type:'post',
		success:function(e){
			if(e=="ups"){
				 upButton.setEnabled(false);
				 left.setEnabled(false);
				 leftall.setEnabled(false);
				 right.setEnabled(false);
				 rightall.setEnabled(false);
			}else{
				 upButton.setEnabled(true);
				 left.setEnabled(true);
				 leftall.setEnabled(true);
				 right.setEnabled(true);
				 rightall.setEnabled(true);
			}
		}	
	});
};

isup(zfcom.value);


valueChange = function(e){
	url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+e.value+"?theme=none";
	url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+e.value+"?theme=none";
	grid.setUrl(url);
	ungrid.setUrl(url2);
	grid.load();
	ungrid.load();
	isup(e.value);
}

//上报
function sbRow(){
	var zfid = mini.get("zftime").value;
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/upShow/'+zfid+'?theme=none',
		type:'post',
		success:function(e){
			if(e=="success"){
				alert("上报成功！");
			}else if(e=="select"){
				alert("请先选择随机人员在上报！");
			}else if(e=="selects"){
				alert("有为选择人员的区县，请查看！");
			}else{
				alert("该区县以上报！");
			}
		}
	});
}

allRightMove=function(){
	ungrid.selectAll(true);
	var selectplan = ungrid.getSelecteds();
	move(selectplan,'leftToright')
}

allLeftMove=function(){
	grid.selectAll(true);
	var selectplan = grid.getSelecteds();
	move(selectplan,'rightToleft');
}

rightMove=function(){
	var selectplan = ungrid.getSelecteds();
	move(selectplan,'leftToright')
}

leftMove=function(){
	var selectplan = grid.getSelecteds();
	move(selectplan,'rightToleft')
}

move = function(selectplan,fro){
	var zfid = mini.get("zftime").value;
	var json = mini.encode(selectplan);

	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/personChange/'+zfid+'?theme=none',
		type:'post',
		data:{data:json,fro:fro},
		success:function(e){
			if(e=="success"){
				grid.reload();
				ungrid.reload();
			}
		}
	});
}
</script>

</body>
</html>