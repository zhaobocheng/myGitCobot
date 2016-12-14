<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>手工导入的企业信息</title>

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
<input type="hidden" id="temp" value="0" style="width:220px;"/>
<div style="padding-top: 5px;padding-bottom: 5px;padding-left:15px">
		<span style="padding-left:20px">
			<iframe name="cs" id="cs" style="display: none;"></iframe>  
 			 <form name="uploadDataName" id="uploadData" target="cs" method="post" enctype="multipart/form-data">
     				<table  cellspacing="0" cellpadding="0" border="0"> 
	     				<tr>
	    				 	<td id='dd' style="width:70%" > 
	    		 				<input class='mini-htmlfile'  style="width:100%" required='true' id='textfield'   name='Fdata' /> 
							</td>
							<td width="145px">
								 <a class="mini-button"   iconCls = "icon-find"  onclick="uploadSave()">导入</a>	 					
							</td>
						</tr>
					</table>
			</form>
		</span>
</div>
    
<div class="mini-fit">
		<div class="mini-datagrid" id="datagridItem"   style="width:100%;height:100%;"
			 url="/ssj/powerlist/PowerList/checkData?theme=none" showFooter="true" idField="id" showToolbar="true"
			pageSize="15" sizeList="[15,30,50,100]">
			<div property="columns">
				<div type="indexcolumn">序号</div>
				<div field="sxfl">事项分类</div>
				<div field="jcsxmc" width="280px">检查事项名称</div>
				<div field="cjfs" width="80px" >采集方式</div>
				<div field="orgNum"  name="orgNum">有效企业数</div>
				
				<!-- <div field="id" name="action" >操作列</div> -->
			</div>
		</div>
</div>
   
<div  id="errorData" class="mini-window" title="错误信息" style="width:980px;height:530px;padding-left:15px">
	<div>
		<div style="padding-top: 10px; padding-bottom:15px; text-align: center">
			<a class="mini-button" id="goon"	 onclick="goon()">继续导入</a> &nbsp;&nbsp;
			<a class="mini-button" id="cancel"  onclick="cancel()">取消导入</a>
		</div>
		<div class="mini-datagrid" id="errorGrid" style="padding-top:15px"  showPager="false" idField="id">
			<div property="columns">
				<div field="code"  headerAlign="center"  align="center">组织机构代码</div>
				<div field="orgName"    headerAlign="center"  align="center">企业名称</div>
				<div field="orgAddressCode"    headerAlign="center"  align="center">注册区县代码</div>
				<div field="city"   headerAlign="center"  align="center">注册区县</div>
				<div field="index"    headerAlign="center"  align="center">错误数据坐标</div>
				<div field="errorInfo"    headerAlign="center"  align="center">错误信息</div>
			</div>
		</div>
	</div>
 </div>
   	 
   	 
   	 <div   id="handsData" class="mini-window" title="详细信息" style="width:980px;height:530px;"  showToolbar="true" showFooter="true" >
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
//操作列的超链接
datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "action") {
        e.cellStyle = "color:#fceee2;font-weight:bold;";
        e.cellHtml = '<a href="javascript:auto(\'' + record.sxId + '\')"><button style="color:blue">自动</button> </a>&nbsp;'+
        '<a href="javascript:hand(\'' + record.sxId + '\')"><button style="color:blue">手动</button> </a>&nbsp;'
           
    }
});

//有效企业数
datagrid.on("drawcell", function (e) {
	var record = e.record,	column = e.column,	field = e.field,	value = e.value;
    if (column.name == "orgNum") {
        if(value!='0'){
        	e.cellHtml = '<a href="javascript:hand(\'' + record.sxId + '\')" style="color:blue;width:80px">'+value+'  </a>&nbsp;';
        }else{
        	e.cellHtml =0;
        }
    }
});



//自动
function auto(sxId) {
	
}
//手动
function hand(sxId) {
	mini.get("handsData").show();
 	var datagrid=mini.get("handsDataGrid");
	datagrid.load({sxId:sxId});
}

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
	//为0 不提示   为1 提示
	$('#uploadData')[0].disabled=true;
	var formData = new FormData($( "#uploadData" )[0]);
	var flag=$("#temp").val();
	$('#uploadData')[0].disabled=true;
	var formData = new FormData($( "#uploadData" )[0]);
	var fileName=document.uploadDataName.Fdata.value;
	if(flag==1){
		 mini.confirm("点击\"确定\"按钮，excel中错误的<br/>记录将被忽略不入库！","警告",function(action) {
			   if (action == "ok") {
				   var s=new Date();
			       $.ajax({
			             url : "/ssj/powerlist/PowerList/impOrgInfo/"+encodeURI(fileName)+"?theme=none",
			             type : "POST",
			             data : formData,
			             async: false,
				         cache: false,
				         contentType: false,
				         processData: false,
				         success: function (returndata) {
				        	 $("#temp").val(0);
				        	 var e=new Date();
				        	 alert("导入完成。 用时： "+(e-s)/1000+"秒");
				       		 
				         }
			       });
			   }else{
				   mini.get("errorData").hide(); 
			   } 
		 }); 
	 }else{
		 var s=new Date();
		 $.ajax({
			 url : "/ssj/powerlist/PowerList/impOrgInfo/"+encodeURI(fileName)+"?theme=none",
	         type: 'POST',
	         data: formData,
	         async: false,
	         cache: false,
	         contentType: false,
	         processData: false,
	         success: function (returndata) {
	        	 var e=new Date();
	        	 alert("导入完成。 用时： "+(e-s)/1000+"秒");
	        	 
	       		// mini.get("errorData").hide();
	         }
	      });
	 }
}
//中止
function cancel(){
	mini.get("errorData").hide();
	 $("#temp").val(0);
}
 
//上传文件
function uploadSave(){
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
		$.ajax({
		      url: '/ssj/powerlist/PowerList/identifyInfo?theme=none',
		      type: 'POST',
		      data: formData,
		      async: false,
		      cache: false,
		      contentType: false,
		      processData: false,
		      success: function (returndata) {
		    	  if(returndata=="error"){
		    		  alert("excel的中的检查事项存在问题，请核查后再导入");
		    		  return;
		    	  }else{
		    		  mini.get("errorData").show();
			    	  var json=eval("("+returndata+")").content;
			    	  var sub = eval("("+json+")");
			    	  mini.get("errorGrid").setData(sub);
			    	  if(sub!=null&&sub!=""){
			    		  $("#temp").val(1);     //标记为含有错误
			    	  }
		    	  }
		    	  
		      }
		  });
 	    
	 }
}
</script>
 
</body>
</html>