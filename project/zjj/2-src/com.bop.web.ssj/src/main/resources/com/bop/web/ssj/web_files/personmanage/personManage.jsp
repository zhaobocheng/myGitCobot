<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法人员管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<!-- <span>方案时间：</span><input class="mini-combobox" id="zftime" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/> -->
	<!-- <a class="mini-button" id = "upbur" iconCls="icon-upload" onclick="sbRow()">上报</a> -->
	<span>年度：</span><input class="mini-combobox" id="zfnd" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getCBData/nd?theme=none"/>
	<span>状态：</span><input class="mini-combobox" id="fazt" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" data="[{id:'0',text:'未上报'},{id:'1',text:'已上报'}]"  />
</div>

<div class="mini-fit">
<div id="layout" class="mini-layout" style="width:100%;height:100%;"  borderStyle="border:solid 1px #aaa;">
   <!-- 上侧选择人员 -->
    <div title="方案列表" region="north"   showSplitIcon="true" minHeight = "200px"  >
		<div id="griddata" class="mini-datagrid" style="width:100%;height:100%" url="/ssj/personmanage/personmanage/getFALBData//?theme=none"  
		idFiled="id"  showPager="false"  onselectionchanged="onSelectionChanged" >
			<div property="columns">
				<div type="checkcolumn" width="10"></div>
				<div type="indexcolumn" width="10">序号</div>
				<div field="id" name="id"  width="100" visible="false">方案id</div>
				<div field="famc" name="famc" width="100" headerAlign="center"   allowSort="true">方案名称</div>
				<div field="zftime" name="zftime" width="60" headerAlign="center"   allowSort="true">执法日期</div>
				<div field="cjtime" name="cjtime" width="80" headerAlign="center" dateFormat="yyyy-MM-dd" allowSort="true">创建时间</div>
				<div field="zt" name="zt" width="60" headerAlign="center"  >状态</div>
				<div field="cz" name="cz" width="30" headerAlign="center"  >操作</div>
			</div>
		</div>
    </div>

    <!-- 左侧，未选择人员-->
    <div title="未参与随机执法人员"   region="west" minWidth = "500px"  expanded="true" showSplitIcon="true" >
   		<div class="mini-toolbar" style="border-bottom:0;padding:0px;">
            <table style="width:100%;">
                <tr>
                    <td style="white-space:nowrap;">
                        <input id="unselectkey" class="mini-textbox" emptyText="请输入姓名" style="width:150px;" onenter="onKeyEnter"/>   
                        <a class="mini-button" onclick="search('un')">查询</a>
                    </td>
                </tr>
            </table>
        </div>
		<div id="unSelectedgrid" class="mini-datagrid" style="width:100%;height:92%" url=""
		   allowResize="true" sizeList="[10,30,50,100]" pageSize="10" showFooter="true" multiSelect="true" >
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
    <div title="参与随机执法人员" region="east"  minwidth="500px"  showSplitIcon="true"  >
     <div class="mini-toolbar" style="border-bottom:0;padding:0px;">
            <table style="width:100%;">
                <tr>
                    <td style="white-space:nowrap;">
                        <input id="selectkey" class="mini-textbox" emptyText="请输入姓名" style="width:150px;" onenter="onKeyEnter"/>   
                        <a class="mini-button" onclick="search()">查询</a>
                    </td>
                </tr>
            </table>
        </div>
		<div id="selectedgrid" class="mini-datagrid" style="width:100%;height:92%" url=""
		allowResize="true" sizeList="[10,30,50,100]" pageSize="10" showFooter="true" multiSelect="true">
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
    	<div style="width:100%;">
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
var zfnd = mini.get("zfnd");
zfnd.select(0);
var zfzt = mini.get("fazt");
zfzt.select(0);
var gridurl = '/ssj/personmanage/personmanage/getFALBData/'+zfnd.value+'/'+zfzt.value+'?theme=none';
var datagrid=mini.get("griddata");
datagrid.setUrl(gridurl);
datagrid.load();

var grid = mini.get("selectedgrid");
var ungrid = mini.get("unSelectedgrid");
var url = "/ssj/personmanage/PersonManage/getSelectedGridData/ss?theme=none";
var url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/ss?theme=none";

grid.setUrl(url);
ungrid.setUrl(url2);
grid.load();
ungrid.load();

function isup(e){
	var left = mini.get("left");
	var leftall = mini.get("leftall");
	var right = mini.get("right");
	var rightall = mini.get("rightall");
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/getZT/'+e+'?theme=none',
		type:'post',
		success:function(e){
			if(e=="ups"){
				 left.setEnabled(false);
				 leftall.setEnabled(false);
				 right.setEnabled(false);
				 rightall.setEnabled(false);
			}else{
				 left.setEnabled(true);
				 leftall.setEnabled(true);
				 right.setEnabled(true);
				 rightall.setEnabled(true);
			}
		}	
	});
};

/* isup(zfcom.value); */

onSelectionChanged = function(e){
	  var data = e.sender;
      var record = data.getSelected();
      if (record) {
		var left = mini.get("left");
		var leftall = mini.get("leftall");
		var right = mini.get("right");
		var rightall = mini.get("rightall");
		var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+record.id+"?theme=none";
		grid.setUrl(url);
		grid.load();
		var url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+record.id+"?theme=none";
		ungrid.setUrl(url2);
		ungrid.load(); 

   		jQuery.ajax({
   			url:'/ssj/personmanage/personmanage/getZT/'+record.id+'?theme=none',
   			type:'post',
   			success:function(e){
   				if(e=="ups"){
   					 left.setEnabled(false);
   					 leftall.setEnabled(false);
   					 right.setEnabled(false);
   					 rightall.setEnabled(false);
   				}else{
   					 left.setEnabled(true);
   					 leftall.setEnabled(true);
   					 right.setEnabled(true);
   					 rightall.setEnabled(true);
   				}
   			}	
   		});
      }
}

valueChange = function(){

	var nd = mini.get("zfnd").value;
	var zt = mini.get("fazt").value;
	url = "/ssj/personmanage/PersonManage/getFALBData/"+nd+"/"+zt+"?theme=none";
	datagrid.setUrl(url);
	datagrid.load();
	isup(e.value);
}

//上报
function sbRow(){
	var zfid = datagrid.getSelected().id;
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/upShow/'+zfid+'?theme=none',
		type:'post',
		success:function(e){
			var inf = mini.decode(e);

			if(inf.flag=="success"||inf.flag=="alls"){
				alert("上报成功！");
			}else if(inf.flag=="select"){
				alert("请先选择随机人员再上报！");
			}else if(inf.flag=="allu"){
				alert("有未选择人员的区县"+inf.text+"，请查看！");
			}else{
				alert("该区县已上报！");
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
	var zfid = datagrid.getSelected().id;
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

search = function(e){
	var recordId = datagrid.getSelected();
	if(recordId==undefined){
		alert("请选择一条任务");
		return;
	}
	if(e=="un"){
		var key=mini.get("unselectkey").value;
		var url = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+recordId.id+"?theme=none&key="+key;
		ungrid.setUrl(url);
		ungrid.load();
	}else{
		var key=mini.get("selectkey").value;
		var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+recordId.id+"?theme=none&key="+key;
		grid.setUrl(url);
		grid.load();
	}
}


</script>

</body>
</html>