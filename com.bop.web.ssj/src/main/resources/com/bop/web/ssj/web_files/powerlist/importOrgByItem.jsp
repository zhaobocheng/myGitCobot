<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>检查对象导入</title>
 <style type="text/css">
	.mini-messagebox-content td{
  	  font-size:20px;
	}
	.mini-messagebox-content {
  	  width:380px;
  	  hegiht:90px;
	}
 	
 </style>
</head>
<body>

 <div style="margin-bottom:10px; padding-top: 10px;padding-left:1px">
 		<div>
	 	 	 <input id="itemIdInput" class='mini-hidden'   value=""  style="width:60%;" />
	 		<div>
	 		<span>&nbsp;&nbsp;&nbsp;选择检查事项：</span>
	 	 	 <input id="itemInput" class='mini-textbox'   value=""  style="width:60%;" />
	 			 <a class="mini-button"   iconCls = "icon-find"  onclick="selectName()">选择</a>
			</div>
			<div  style="margin-top:2px;margin-bottom:1px ;padding-top: 4px;padding-bottom: 1px;">			
			 <form name="uploadDataName" id="uploadData"  method="post" enctype="multipart/form-data">
			 	<span>&nbsp;&nbsp;&nbsp;选择导入文件:&nbsp;&nbsp;&nbsp;</span>
	    		<input class='mini-htmlfile' style="width:60%;"  id='textfield'   name='Fdata' /> 
				<a class="mini-button"   iconCls = "icon-find"  onclick="uploadSave()">导入</a>	 					
			</form>
			</div>
 		</div>
 </div>
 <input type="hidden" id="temp" value="0" style="width:220px;"/> 
 <div class="mini-fit">	 
		<div class="mini-datagrid" id="datagridItem"  url="/ssj/powerlist/PowerList/checkData?theme=none" showFooter="true" idField="id" style="width:100%;height:100%;"
	pageSize="30" sizeList="[15,30,50,100]">
	<div property="columns">
		<div type="indexcolumn" headerAlign="center">序号</div>
		<div field="sxfl"  headerAlign="center" align="center" width="80px">事项业务分类</div>
		<div field="jcsxmc" width="280px" headerAlign="center">检查事项名称</div>
		<div field="cjfs" width="80px" headerAlign="center" align="center">采集方式</div>
		<div field="orgNum"  name="orgNum" headerAlign="center" align="center">有效企业数</div>
		<!-- <div field="id" name="action" >操作列</div> -->
				</div>
			</div>
</div>
  
 
 
<div  id="selectItemName" class="mini-window" title="选择导入文件对应的事项" style="padding-left:1px;width:980px;height:480px;">
 		<div class="mini-fit">
 			<div>
					<ul id="itemTree" class="mini-tree" url="/ssj/powerlist/PowerList/getItemList" showTreeIcon="true" textField="text" idField="id" showCheckBox="true"
				   		checkRecursive="false"  onbeforenodeselect="onBeforeNodeSelect" showFolderCheckBox="false"		 allowSelect="true" >        
					</ul>
			</div>
		</div> 
		<div style="width:100%;text-align: center">
				<span>
					<a class="mini-button" id="goon"	 onclick="saveItemName()">确定</a> &nbsp;&nbsp;
					<a class="mini-button" id="cancel"  onclick="cancelItemName()">取消</a> 
				</span>
	 	</div> 
 </div>
 

<div  id="errorData" class="mini-window" title="错误信息" style="width:1080px;height:550px;padding-left:15px" showFooter="true" showToolbar="true">
			<div property="toolbar" style="padding:5px;"><p id="first" style="font-size:15px;color: red"></p></div>
			<div style="height:80%">
				<div class="mini-datagrid" id="errorGrid"  showPager="true" idField="id">
					<div property="columns">
						<div field="errorIndex" width="70px"  headerAlign="center"  align="center">序号</div>
						<div field="code" width="90px"  headerAlign="center"  align="center">组织机构代码</div>
						<div field="orgName"    headerAlign="center"  align="center">企业名称</div>
						<div field="orgAddressCode"  width="90px"  headerAlign="center"  align="center">注册地区划代码</div>
						<div field="city"   headerAlign="center"  align="center">注册区县</div>
						<div field="yieldlyCode" width="85px"  headerAlign="center"  align="center">生产地区划代码</div>
						<div field="errorInfo"  width="250px"  headerAlign="center"  align="center">错误信息</div>
					</div>
				</div>
			</div>
			<div property="footer" style="width:100%;padding:25px;text-align: center">
				<span>
					<a class="mini-button" id="goon" onclick="goon()">继续导入</a> &nbsp;&nbsp;
					<a class="mini-button" id="cancel"  onclick="cancel()">取消导入</a>
				</span>
			</div>
</div>
					   	 
					   	 
<div id="handsData" class="mini-window" title="详细信息" style="width:980px;height:530px;"  showToolbar="true" showFooter="true" >
	 	 <div class="mini-fit">
			<div class="mini-datagrid" id="handsDataGrid"   
				 url="/ssj/powerlist/PowerList/getHandOrg?theme=none" showFooter="true" idField="id"
				pageSize="20" sizeList="[20,30,50,100]">
				<div property="columns">
					<div field="orgName"  width="100" headerAlign="center"  align="center">企业名称</div>
					<div field="itemName"  width="100" headerAlign="center"  align="center">事项名称</div>
					<div field="insertTime" name="formatTime" width="100" headerAlign="center"  align="center">最后一次时间</div>
				</div>
			</div>
	   	 </div>
</div>
 
<script >
mini.parse();
var datagrid=mini.get("datagridItem");
datagrid.load();
var sxid11 = null;
//打开选择事项名称窗口
function selectName(){
	mini.get("selectItemName").show();
}
//保存事项名称
function saveItemName(){
	var tree = mini.get("itemTree");
	var selected=tree.getText();
	var select=tree.getValue();
	mini.get("itemInput").setValue(selected);
	mini.get("itemIdInput").setValue(select);
	mini.get("selectItemName").hide();
	
}

//关闭窗口
function cancelItemName(){
	mini.get("selectItemName").hide();
	
}
//上传文件
function uploadSave(){
	//获取事项名称的值
	var selectedName=mini.get("itemInput").value;
	var selectedId=mini.get("itemIdInput").value;
	if((selectedId.trim()==null||selectedId.trim()=="")||(selectedName.trim()==null||selectedName.trim()=="")){
		alert("请选择事项");
		return;
	}else{
		 $("#temp").val(0);
		 var s=document.uploadDataName.Fdata.value;
	     if(s==""){
	         alert("请选择文件!");
	         return;
	     }else{
	    	var type = s.split('.')[1];
	     	if(type!="xlsx"&&type!="xls"){
	     		alert("请选择excel文件");
	     		return;
	     	}
	 		$('#uploadData')[0].disabled=true;
	 	    var formData = new FormData($( "#uploadData" )[0]);
	 	   mini.mask({
	            el: document.body,
	            cls: 'mini-mask-loading',
	            html: '后台校验中，请稍后...'
	        });
			$.ajax({
			      url: '/ssj/powerlist/ImportData/identifyInfo/'+selectedId+'?theme=none',
			      type: 'POST',
			      data: formData,
			      async: true,
			      cache: false,
			      contentType: false,
			      processData: false,
			      success: function (returndata) {
			    	  if(returndata=="error"){
			    		  mini.unmask(document.body);
			    		  alert("导入文件应该为12列！请核查后再导入！");
			    		  return;
			    	  }else if(returndata=="error1"){
			    		  mini.unmask(document.body);
			    		  alert("excel中的事项名称这一列应为空！请核查后再导入！");
			    		  return;
			    	  }else if(returndata=="error2"){
			    		  mini.unmask(document.body);
			    		alert("excel的中的检查事项和选中的事项不一致，请核查后再导入");
			    		return;
			    		  
			    	  }else{
			    		  mini.unmask(document.body);
			    		  sxid11 = eval("("+returndata+")").tempsxid;
			    		  mini.get("errorData").show();
				    	  var json=eval("("+returndata+")").content;
				    	  var sub = eval("("+json+")");
				    	  mini.get("errorGrid").setData(sub);
				    	  if(sub!=null&&sub!=""){
				    		  $("#temp").val(1);     //标记为含有错误
				    	  }
				    	  var percent=eval("("+returndata+")").percent;
				    	  var errorNum=eval("("+returndata+")").errorNum;
				    	  if(errorNum!="0"){
				    		  var hehe = document.getElementById("first");
					    	  hehe.innerText = "含有错误！ 错误数为："+errorNum+";    错误率为："+percent+";  建议修正后再导入!";
				    	  }
				    	  var ItemJson=eval("("+returndata+")").itemId;
				    	  mini.get("itemIdTemp").setValue(ItemJson);
			    	  }
			      }
			  });
		 }
	}
	
	
} 
 
 
//手动
function hand(sxId) {
	mini.get("handsData").show();
 	var datagrid=mini.get("handsDataGrid");
	datagrid.load({sxId:sxId});
}



//操作列的超链接
datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "action") {
        e.cellStyle = "color:#fceee2;font-weight:bold;";
        e.cellHtml = '<a href="javascript:auto(\'' + record.sxId + '\')"><button style="color:blue">自动</button> </a>&nbsp;'+
        '<a href="javascript:hand(\'' + record.sxId + '\')"><button style="color:blue">手动</button> </a>&nbsp;';    
    }
});
 
//有效企业数
/* datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "orgNum") {
        if(value!='0'){
        	e.cellHtml = '<a href="javascript:hand(\'' + record.sxId + '\')" style="color:blue;width:80px">'+value+'  </a>&nbsp;';
        }else{
        	e.cellHtml =0;
        }
    }
}); */
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
 
//校验完后在error页面选择“继续”
function goon(){
	//flag为0 不提示   为1 提示
	$('#uploadData')[0].disabled=true;
	var formData = new FormData($( "#uploadData" )[0]);
	var flag=$("#temp").val();
	$('#uploadData')[0].disabled=true;
	var formData = new FormData($( "#uploadData" )[0]);
	var fileName=document.uploadDataName.Fdata.value;
	fileName=fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length);
	var selectedId=mini.get("itemIdInput").value;
	var size=selectedId.split(",").length;
	if(flag==1){
		 mini.confirm("点击\"确定\"按钮，excel中错误的<br/>记录将被忽略不入库！","警告",function(action) {
			   if (action == "ok") {
				   var s=new Date();
				   
				   mini.mask({
			            el: document.body,
			            cls: 'mini-mask-loading',
			            html: '正在入库，请稍后...'
			        });
				   

			       $.ajax({
						 url : "/ssj/powerlist/ImportData/impOrgInfo/"+sxid11+"/"+flag+"/"+selectedId+"?theme=none",
			             type : "POST",
			             data : formData,
			             async: true,
				         cache: false,
				         contentType: false,
				         processData: false,
				         success: function (returndata) {
				        	 $("#temp").val(0);
				        	 var e=new Date();
				        	 mini.get("errorData").hide(); 
				        	 datagrid.load();
			        		 var result=returndata.split(";");
			        		 mini.unmask(document.body);
				        	 alert("导入完成。 用时： "+((e-s)/1000)+"秒。"+"总数："+result[0]+"插入："+result[1]+";更新："+result[2]+";失败："+result[3]);
				       		 
				         }
			       });
			   }else{
				   mini.get("errorData").hide(); 
			   } 
		 }); 
	 }else{
		 var s=new Date();
		 $.ajax({
			 url : "/ssj/powerlist/ImportData/impOrgInfo/"+sxid11+"/"+flag+"/"+selectedId+"?theme=none",
	         type: 'POST',
	         data: formData,
	         async: false,
	         cache: false,
	         contentType: false,
	         processData: false,
	         success: function (returndata) {
	        	 var e=new Date();
	        	 var result=returndata.split(";");
	        	 alert("导入完成。 用时： "+((e-s)/1000)+"秒。"+"总数："+result[0]+"插入："+result[1]+";更新："+result[2]+";失败："+result[3]);
	        	 mini.get("errorData").hide(); 
	        	 datagrid.load();
	         }
	      });
	 }
}
//中止
function cancel(){
	mini.get("errorData").hide();
	 $("#temp").val(0);
}
//禁止选择父节点
function onBeforeNodeSelect(e) {
    var tree = e.sender;
    var node = e.node;
    if (tree.hasChildren(node)) {
        e.cancel = true;
    }
} 

 

</script>
 
</body>
</html>