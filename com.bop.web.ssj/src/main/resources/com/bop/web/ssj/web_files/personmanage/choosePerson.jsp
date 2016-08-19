<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>人员选择</title>
<%	String nd=request.getParameter("nd");
	String faid = request.getParameter("faid");
%>
</head>
<body>
<div class="mini-fit">
   <div title="" minHeight = "20px" align="right">
   	<a class="mini-button" id="upload" iconCls="icon-upload" onclick="commit()">上报</a>
	</div>
<div id="layout" class="mini-layout" style="width:100%;height:100%;"  borderStyle="border:solid 1px #aaa;">

    <!-- 左侧，未选择人员-->
    <div title="未参与随机执法人员"   region="west" minWidth = "400px"  expanded="true" showSplitIcon="true" >
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
				<div field="unSeletedName" name="unSeletedName" width="60" headerAlign="center"   allowSort="true">姓名</div>
				<div field="unSeletedDept" name="unSeletedDept" visible="false" width="80" headerAlign="center"   allowSort="true">区县</div>
				<div field="bnyj" name="ndyj" width="100" headerAlign="center" align="center">本年度业绩</div>
			</div>
		</div>
    </div>

    <!-- 右侧选择人员 -->
    <div title="参与随机执法人员" region="east"  minwidth="400px"  showSplitIcon="true"  >
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
				<div field="seletedName" name="seletedName" width="60" headerAlign="center"   allowSort="true">姓名</div>
				<div field="seletedDept" name="seletedDept" width="80" headerAlign="center"  visible="false" allowSort="true">区县</div>
				<div field="bnyj" name="ndyj" width="100" headerAlign="center" align="center">本年度业绩</div>				
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

<script type="text/javascript">
mini.parse();
var faid ='<%=faid %>';
var nd='<%=nd %>';
var grid = mini.get("selectedgrid");
var ungrid = mini.get("unSelectedgrid");

var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+faid+"?theme=none&nd="+nd;
grid.setUrl(url);
grid.load();
var url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+faid+"?theme=none&nd="+nd;
ungrid.setUrl(url2);
ungrid.load();

commit=function(){
	var data=grid.data;
	if (data.length==0){
		alert("请先选择随机人员再上报！");
		return;
	}
	
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/upShow/'+faid+'?theme=none',
		type:'post',
		success:function(e){
			var inf = mini.decode(e);

			if(inf.flag=="success"||inf.flag=="alls"){
				isup();
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

function isup(){
	var left = mini.get("left");
	var leftall = mini.get("leftall");
	var right = mini.get("right");
	var rightall = mini.get("rightall");
	var upload=mini.get("upload");
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/getZT/'+faid+'?theme=none',
		type:'post',
		success:function(e){
			if(e=="ups"){
				 left.setEnabled(false);
				 leftall.setEnabled(false);
				 right.setEnabled(false);
				 rightall.setEnabled(false);
				 upload.setEnabled(false);
			}else{
				 left.setEnabled(true);
				 leftall.setEnabled(true);
				 right.setEnabled(true);
				 rightall.setEnabled(true);
				 upload.setEnabled(true);
			}
		}	
	});
};
isup();

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
	
	var json = mini.encode(selectplan);

	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/personChange/'+faid+'?theme=none',
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

	if(e=="un"){
		var key=mini.get("unselectkey").value;
		var url = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+faid+"?theme=none&key="+key+"&nd="+nd;
		ungrid.setUrl(url);
		ungrid.load();
	}else{
		var key=mini.get("selectkey").value;
		var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+faid+"?theme=none&key="+key+"&nd="+nd;
		grid.setUrl(url);
		grid.load();
	}
}

</script>

</body>
</html>