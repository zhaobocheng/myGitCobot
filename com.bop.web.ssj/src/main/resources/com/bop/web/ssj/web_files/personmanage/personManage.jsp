<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法人员管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<span>方案时间：</span><input class="mini-combobox" id="zftime" style="width:150px;" value="e53aa6aa-a9c5-4ef1-bb8d-f64190e7fb78" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/>
	<a class="mini-button" iconCls="icon-upload" onclick="sbRow()">上报</a>
</div>

<div id="layout" class="mini-layout" style="width:100%;height:100%;"  borderStyle="border:solid 1px #aaa;">
    <!-- 左侧，未选择人员-->
    <div title="未参与随机执法人员"   region="west" minWidth = "500px" expanded="true" showSplitIcon="true" >
		<div id="unSelectedgrid" class="mini-datagrid" style="width:100%;height:98%" url="/ssj/personmanage/personmanage/getUnSelectedGridData?theme=none" 
		 idFiled="id" allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true" multiSelect="true" >
			<div property="columns">
				<div type="checkcolumn" width="20"></div>
				<div type="indexcolumn" width="20">序号</div>
				<div field="id" name="id"  width="100" visible="false">方案id</div>
				<div field="unSeletedName" name="unSeletedName" width="80" headerAlign="center"   allowSort="true">姓名</div>
				<div field="unSeletedDept" name="unSeletedDept" width="80" headerAlign="center"   allowSort="true">区县</div>
			</div>
		</div>
    </div>
    
    <!-- 右侧选择人员 -->
    <div title="参与随机执法人员" region="east"   showSplitIcon="true" minWidth = "500px" multiSelect="true"  >
		<div id="selectedgrid" class="mini-datagrid" style="width:100%;height:98%" url="/ssj/personmanage/personmanage/getSelectedGridData?theme=none"  
		idFiled="id" allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true" multiSelect="true" >
			<div property="columns">
				<div type="checkcolumn" width="20"></div>
				<div type="indexcolumn" width="20">序号</div>
				<div field="id" name="id"  width="100" visible="false">方案id</div>
				<div field="seletedName" name="seletedName" width="80" headerAlign="center"   allowSort="true">姓名</div>
				<div field="seletedDept" name="seletedDept" width="80" headerAlign="center"   allowSort="true">区县</div>
			</div>
		</div>
    </div>

    <!-- 中间按钮 -->
    <div title="center" region="center" >
    	<div style="width:100%;height:100%;padding-top:260;">
    		 <div class="mini-toolbar" style="border-bottom:0;padding:2px;">
	            <table style="width:100%;">
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button"  onclick="leftMove()"  >左移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button"  onclick="allLeftMove()" >全部左移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button"  onclick="rightMove()"  >右移</a>
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:100%;" align="center">
	                        <a class="mini-button"   onclick="allRightMove()"  >全部右移</a>
	                    </td>
	                </tr>
	            </table>
	        </div>
   	 	</div>
   	 </div>
</div>

<!-- 绘制页面结束 -->
<script>
mini.parse();
var selectGrid = mini.get("selectedgrid");
var unselectGrid = mini.get("unSelectedgrid");
 
selectGrid.load();
unselectGrid.load();

//上报
function sbRow(){
	alert("需要确认具体业务");
}

allRightMove=function(){
	unselectGrid.selectAll(true);
	var selectplan = unselectGrid.getSelecteds();
	move(selectplan,'leftToright')
}

allLeftMove=function(){
	selectGrid.selectAll(true);
	var selectplan = selectGrid.getSelecteds();
	move(selectplan,'rightToleft');
}

rightMove=function(){
	var selectplan = unselectGrid.getSelecteds();
	move(selectplan,'leftToright')
}

leftMove=function(){
	var selectplan = selectGrid.getSelecteds();
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
				selectGrid.reload();
				unselectGrid.reload();
			}
		}
	});
	
	
}
</script>

</body>
</html>