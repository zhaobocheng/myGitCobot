<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法结果录入</title>
</head>
<body>
<div>
	<span>任务名称：</span><input class="mini-textbox" id="rwmc" name="rwmc" style="width:150px;" onvaluechanged="valueChangeMonth"/>
	<span>抽查年份：</span><input class="mini-combobox" id="year" style="width:150px;" textField="text" valueField="id"   onvaluechanged="valueChangeYear" url="/ssj/ssjscheme/SchemeResult/getYData"/>
	<span>抽查月份：</span><input class="mini-combobox" id="month" style="width:150px;" textField="text" valueField="id"  onvaluechanged="valueChangeMonth" url=""/>
	 <a class="mini-button" id="find" iconCls = "icon-find"  onclick="search()">查找</a>
</div>
<div style="padding:5px;10px;5px;0">
	<!-- <a class="mini-button" id="createBut" iconCls = "icon-save"  onclick="saveData()" >保存</a> -->
	<a class="mini-button" id="commitBut" iconCls = "icon-ok" onclick="commitFa()" >提交结果</a>
	<a class="mini-button" id="commitGS" iconCls = "icon-node"  onclick="onCommitGS()" >公示</a>	
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()" >导出Excel</a>
</div>
<div class="mini-fit">
<div id="datagrid1" class="mini-datagrid" style="width:100%;height:100%;" showPager="false" onrowdblclick="rowthing"
        url="" idField="id" allowResize="true" allowCellEdit="false" allowCellSelect="true" multiSelect="true"
        allowCellValid="true"  oncellbeginedit="OnCellBeginEdit"  >
    <div property="columns">
     <div type="checkcolumn"></div>
        <div type="indexcolumn">序号</div>
        <div field="zf" headerAlign="center" align="center" >状态</div>
        <div field="PLAN1202" width="80" headerAlign="center">机构代码</div>
        <div field="PLAN1203" width="100" align="right" headerAlign="center">单位名称</div>
        <div field="PLAN1210" visible="false">PLAN1210</div>
        <div field="parentid" visible="false">parentid</div>
        <div field="PLAN1229" name="PLAN1229" vtype="required" width="100"   headerAlign="center"  dateFormat="yyyy-MM-dd"  >检查时间
            <input property="editor" class="mini-datepicker" format="yyyy-MM-dd"  style="width:100%;" />  
        </div>
        
        <div field="PLAN1221" name="PLAN1221" vtype="required" width="100"   headerAlign="center"    type="comboboxcolumn">是否发现问题
            <input property="editor" class="mini-combobox"  style="width:100%;" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY06&isSync=1"/>  
        </div>
         <div field="PLAN1222" name="PLAN1222" vtype="required" width="100"   align="center" headerAlign="center" type="comboboxcolumn" >问题涉及事项
             <input property="editor" class="mini-combobox"   style="width:100%;"  url="/ssj/ssjScheme/SchemeResult/getCode/ZDY08?theme=none"  minWidth="100" />
        </div> 
  		<div field="PLAN1223"  vtype="required" width="120" headerAlign="center" allowSort="true">问题描述
               <input property="editor" class="mini-textarea" style="width:100%;"  minWidth="240" minHeight="80"/>
        </div> 
         <div field="PLAN1224" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">立案情况
             <input property="editor" class="mini-combobox" style="width:100%;" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY08&isSync=1"/>                
         </div> 
         <div field="PLAN1225" vtype="required" width="60" type="comboboxcolumn" align="center" headerAlign="center">企业是否变化
             <input property="editor" class="mini-combobox" style="width:100%;" data="Bits"/>        
         </div>
         <div field="PLAN1226" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">企业变化情况
             <input property="editor" class="mini-combobox"  style="width:100%;" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY09&isSync=1"/>                
         </div>
		<div field="PLAN1227"  vtype="required" width="120" headerAlign="center" allowSort="true">实际生产地
               <input property="editor" class="mini-textarea" style="width:100%;" minWidth="200" minHeight="80"/>
        </div>
    </div>
</div>








<div  id="addwindow" class="mini-window" title="填写结果" style="width:720px;height:400px;" showToolbar="true" showFooter="true">
	<div class = "input_form" id = "winform"  style="width:100%;">
	
		 <table style="width:100%;"> 
			     <tr>
			        <th width="15%"><label>机构代码：</label></th>
			        <td width="35%"><input style="width: 100%" id="PLAN1202"
			            name="PLAN1202" class="mini-textbox" allowInput="false" required = "true"/>
			            <input class="mini-hidden" name="id" id="id" />
			        </td>
			        <th width="17%"><label>机构名称：</label></th>
			        <td width="33%"><input style="width: 100%" id="PLAN1203" required = "true"
			            name="PLAN1203" class="mini-textbox" allowInput="false" /></td>
			    </tr>
			    <tr>
			        <th width="15%"><label>检查时间<font color = "red">*</font>：</label></th>
			        <td width="35%"><input style="width: 100%" id="PLAN1229" required = "true"
			             format="yyyy-MM-dd"   name="PLAN1229" class="mini-datepicker" allowInput="false" /></td>
			        <th width="17%"><label>是否发现问题<font color = "red">*</font>：</label></th>
			        <td width="33%"><input style="width: 100%" id="PLAN1221" required = "true" 
			            name="PLAN1221" class="mini-combobox" allowInput="false" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY06&isSync=1" /></td>
			    </tr>
			    <tr>
			        <th width="15%"><label>问题涉及事项：</label></th>
			        <td colspan="3">
				        <input style="width: 100%" id="PLAN1222" name="PLAN1222"  class="mini-combobox" required = "false" multiSelect="true" allowInput = "false"
				        textField="text" valueField="id"  url="/ssj/ssjScheme/SchemeResult/getCode/ZDY08?theme=none"  />
			   		</td>
			    </tr>
			    
			    <tr>
			        <th width="15%"><label>问题描述：</label></th>
			        <td colspan="3">
				        <input style="width: 100%" id="PLAN1223" name="PLAN1223"  class="mini-textarea" required = "false"
				        textField="text" valueField="id"    />
			   		</td>
			    </tr>
			    
			    <tr >
				     <th width="15%"><label>立案情况<font color = "red">*</font>：</label></th>
				     <td width="35%"><input style="width: 100%;" id="PLAN1224" required = "true"
				            name="PLAN1224" class="mini-combobox" allowInput="false" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY08&isSync=1" />
				     </td>
				     <th width="17%"><label>企业是否变化<font color = "red">*</font>：</label></th>
				     <td width="33%"><input style="width: 100%;" id="PLAN1225" required = "true" 
				            name="PLAN1225" class="mini-combobox" allowInput="false"  data="[{id:0,text:'否'},{id:1,text:'是'}]"/>
				     </td>
			    </tr>
			    <tr >
				     <th width="15%"><label>企业变化情况：</label></th>
				     <td colspan="3"><input style="width: 100%;" id="PLAN1226" required = "false"
				            name="PLAN1226" class="mini-combobox"  url="/Domain/DMDefineTreeAjax.do?tableName=ZDY09&isSync=1"  />
				     </td>
			    </tr>
			    <tr >
				     <th width="15%"><label>实际生产情况：</label></th>
				     <td colspan="3"><input style="width: 100%;" id="PLAN1227" required = "false"
				            name="PLAN1227" class="mini-textArea" allowInput="true" />
				     </td>
			    </tr>
		   </table>
		   
		   <div property = "footer" id = "hysubminButtondiv"  Align = "right" > 
       			<a class = "mini-button" iconCls="mini-save" id = "hysubminButton" onclick = "onSubmit()">保存</a>
       			<a class = "mini-button" iconCls="mini-consel" id = "hyconcleButton" onclick = "onConcle()">取消</a> &nbsp;&nbsp;
       		</div>
     </div>
	</div>

</div>





<!-- 选择检查事项窗口-->
<div id="chooseItemWin" class="mini-window" title="选择检查事项" style="width:870px;height:580px;" showToolbar="true" showFooter="true" >
	<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
		<table>
			<tr>
				<td>检查事项名称：</td>
				<td><input class="mini-textbox" id="itemmc" name="itemmc" style="width:220px;" onvaluechanged="findRow()"/></td>
				<td>检查事项对象：</td>
				<td><input class="mini-combobox" id="itemdx" name="itemdx" style="width:220px;" onvaluechanged="findRow()" textField="text" valueField="id" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY04&isSync=1"/></td>
			</tr>
			<tr>
				<td>检查事项业务分类：</td>
				<td colspan="3"><input class="mini-combobox" id="itemfl" name="itemfl" style="width:220px;" onvaluechanged="findRow()" textField="text" valueField="id" url="/Domain/DMDefineTreeAjax.do?tableName=ZDY02&isSync=1"/>&nbsp;&nbsp;
					<a class="mini-button" id="find" iconCls = "icon-save"  onclick="savePowerList()">保存</a>
				</td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div class="mini-datagrid" id="powerDatagrid" style="width:100%;height:100%;" url="/ssj/taskmanage/taskOperation/getitemsData?theme=none" showFooter="true" idField="id" multiSelect="true"
			pageSize="30" sizeList="[20,30,50,100]">
			<div property="columns">
				<div type="checkcolumn"></div>
				<div type="indexcolumn" width="20" headerAlign="center">序号</div>
				<div field=itemmc width="80" headerAlign="center">检查事项名称</div>
				<div field="itemdx" width="120" headerAlign="center">检查事项对象</div>
				<div field="itemfl" width="80" headerAlign="center">检查事项业务分类</div>
			</div>
		</div>
	</div>
</div>

</div>

<script >
mini.parse();
var Bits=[{id:0,text:'否'},{id:1,text:'是'}];
var grid = mini.get("datagrid1");
var btngs = mini.get("commitGS");

var mc=mini.get("rwmc").value;
var yf=mini.get("month").value;

var url = '/ssj/ssjScheme/SchemeResult/getSchemeDate?theme=none';
grid.setUrl(url);
//grid.load({month:mc.value});
var powerDatagrid=mini.get("powerDatagrid");

valueChangeYear = function(){
	 var id = mini.get("year").getValue();
	 var month = mini.get("month");
   month.setValue("");
   var url = "/ssj/ssjscheme/SchemeResult/getMData/" +id
   month.setUrl(url);
   month.select(0);
   valueChangeMonth();
}

getGSzt = function(rwmc,year,month){
	$.ajax({
		url:'/ssj/ssjScheme/SchemeResult/getGSstatus/'+rwmc+'/'+year+'/'+month+'?theme=none',
		type:'post',
		success:function(e){
			var info = mini.decode(e);
			if(info.flag=="true"){
				btngs.setEnabled(true);
				grid.load({rwmc:rwmc,month:month,year:year});
			}else if(info.flag=="false"){
				btngs.setEnabled(false);
				grid.load({rwmc:rwmc,month:month,year:year});
			}else if(info.flag=="unconmmit"){
				mini.alert("由于该任务方案尚未提交，无法进行结果回填。请进入「方案生成」点击「浏览执法方案」按钮交方案后，再填写检查结果!","警告");
				grid.setData();
			}else{
				return ;
			}
		}
	}); 
}

valueChangeMonth = function(){
	search();
}

function search(){
	var rwmc=mini.get("rwmc").value;	
	var month=mini.get("month").value;
	var year=mini.get("year").value;
	getGSzt(rwmc,year,month);
	
}

function OnCellBeginEdit(e) {
     var record = e.record, field = e.field;
     
     //企业是否变化 PLAN1225
     if (field == "PLAN1225" && record.PLAN1225 == "1") {
    	 record.PLAN1226="";
     }
     
     if (field == "PLAN1221" && record.PLAN1221 == "1") {
    	 record.PLAN1222="";
    	 record.PLAN1223="";
     }
     
     //企业变化情况
     if (field == "PLAN1226" && record.PLAN1225 == "0") {
         e.cancel = true;
     }
     
     
     
      if (field == "PLAN1227" && record.PLAN1226 != "2") {
         e.cancel = true;
     }

      //问题涉及事项
      if (field == "PLAN1222" && record.PLAN1221 != "1") {
          e.cancel = true;
      }
/*        if (field == "PLAN1224" && record.PLAN1221 != "1") {
          e.cancel = true;
      }  */
      //问题描述
      if (field == "PLAN1223" && record.PLAN1221 != "1") {
          e.cancel = true;
      }
      
      
      //
      
 }

function onCellValidation(e) {
/* 	oncellvalidation="onCellValidation" */
	var rec=e.record;	

}

function onCommitGS(){
	//var data=grid.data;
	var data = grid.getSelecteds();
	
	for (var i = data.length - 1; i >= 0; i--) {
		if (data[i].PLAN1210<'4'){
			alert("有未提交结果的录入，请先确保录入结果已经提交再进行公示！");
			return;
		}
	}

	if (data.length>0){
		var faid=data[0].parentid;
		grid.loading("提交公示中，请稍后......");
		$.ajax({
			url:'/ssj/ssjScheme/SchemeResult/commitGSData/'+faid+'?theme=none',
			data:{data:mini.encode(data)},
			type:'post',
			success:function(e){
				if(e=="success"){
					alert("公示成功！");	
				}else{
					alert("公示失败！");
				}
				grid.reload();
			}
		})  		
	}
}

function commitFa(){
/*     if (grid.isChanged() == true) {
    	saveData();
    } */
	var rows = grid.getSelecteds();

    if (rows.length > 0) {
		var json = mini.encode(rows);
		grid.loading("提交中，请稍后......");
		$.ajax({
			url:'/ssj/ssjScheme/SchemeResult/commitGridData?theme=none',
			type:'post',
			data:{data:json},
			success:function(e){
				var text = mini.decode(e);
				if(text.flag){
					grid.reload();					
					alert("提交成功！");
				}else{
					grid.reload();
					alert("第"+text.info+"行数据未填写内容，请先填写保存再提交!");
				}
			}
		})      
    }else {
    	alert("请选择要提交的记录！");
    }	
}

function saveData() {
  // grid.validate();
/*      if (grid.isValid() == false) {
        var error = grid.getCellErrors()[0];
        grid.beginEditCell(error.record, error.column);
        return;
    } */

    var datas = grid.getChanges();
    
    if(datas.length<1){
    	alert("请先填写执法结果，在保存！");
    	return ;
    }
    var json = mini.encode(datas);
    for(var i=0;i<datas.length;i++){
    	var data = datas[i];
    	var ss = data.PLAN1222;
    	
    	if(data.PLAN1229==undefined){
    		alert("请填写检查时间！");
    		grid.validateRow(data);
    		return;
    	}
    	if(data.PLAN1221==1&&data.PLAN1222==""){
    		alert("请选择问题涉及事项及问题描述");
    		grid.validateRow(data);
    		return;
    	}
    	if(data.PLAN1225==1){
    		if(data.PLAN1226==""||data.PLAN1226==0){
    			alert("请选择企业变化情况");
        		grid.validateRow(data);
        		return;
    		}
    	}
    	if(data.PLAN1224==""){
    		alert("请选择立案情况");
    		grid.validateRow(data);
    		return;
    	}
    }
    
    
    
    grid.loading("保存中，请稍后......");

    $.ajax({
        url: "/ssj/ssjScheme/SchemeResult/saveGridData?theme=none",
        data: { data: json },
        type: "post",
        success: function (e) {
			if(e=="success"){
				alert("保存成功！");
				
				grid.reload();
			}else{
				alert("保存失败！");
			}            
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
        }
    });
}


function getColumns(columns) {
    columns = columns.clone();
    for (var i = columns.length - 1; i >= 0; i--) {
        var column = columns[i];
        if (!column.field) {
            columns.removeAt(i);
        } else {
            var c = { header: column.header, field: column.field,visible:column.visible };
            columns[i] = c;
        }
    }
    return columns;
}

importExc = function(){
	var faid = mini.get("month").value;
	var rwmc=mini.get("rwmc").value;
	grid.loading("正在导出，请稍后......");
	//var columns = grid.columns;		
	var columns = grid.getBottomColumns();
	var columns1 = getColumns(columns);
	var json = mini.encode(columns1);	
	
	$.ajax({
		url:'/ssj/ssjscheme/ExportExcle/ResultExportExcel?month='+faid+'&rwmc='+rwmc+'&theme=none',
		type:'post',
		data:{gridcolmun:json},
		success:function(e){
			var inf = mini.decode(e);
			if(inf.flag){
				location.href = decodeURI("/ResourceFiles"+inf.path);
			}else{
				alert("导出失败咧！");
			}
			grid.reload();
		}
	});
}




rowthing= function(e){
	//alert("rowthing");
	if(e.row.zf == "已提交"){
		alert("本条记录已经提交不可再修改！");
		return ;
	}
	var win = mini.get("addwindow");
	win.show();
	
	mini.get("PLAN1202").setValue(e.row.PLAN1202);
	mini.get("PLAN1203").setValue(e.row.PLAN1203);
	mini.get("id").setValue(e.row.id);
}

onConcle = function(e){
	var win = mini.get("addwindow");
	win.hide();
	
}

function onSubmit(){
	var hyform = new mini.Form("#winform");
	hyform.validate();	 
	 
	
	 var p21 = mini.get("PLAN1221").getValue();
	 var p22 = mini.get("PLAN1222").getValue();
	 var p23 = mini.get("PLAN1223").getValue();
	 if((p21 == 1 && p22 =="")||(p21 == 1 && p23 =="")){
		 alert("请填写问题涉及事项以及问题描述!");
	 }

	 var p25 = mini.get("PLAN1225").getValue();
	 var p26 = mini.get("PLAN1226").getValue();
	 if(p25==1 && p26==null){
		 alert("请选择企业变化情况！");
	 }
	 
	 var p27 = mini.get("PLAN1226").getValue();
	 if(p26==2 && p26==null){
		 alert("请填写实际生产地！");
	 }
	 
	 
	var json = hyform.getData();
	 
    $.ajax({
        url: "/ssj/ssjScheme/SchemeResult/saveGridData?theme=none",
        data: json,
        type: "post",
        success: function (e) {
			if(e=="success"){
				alert("保存成功！");
				mini.get("addwindow").hide();
				grid.reload();
			}else{
				alert("保存失败！");
			}            
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(jqXHR.responseText);
        }
    });
}



chooseItem=function(){
	var chooseItemWin=mini.get("chooseItemWin");
	powerDatagrid.load();
	chooseItemWin.show();
}

</script>
</body>
</html>