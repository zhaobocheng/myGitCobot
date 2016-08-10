<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法结果录入</title>
</head>
<body>
<div><span>执法结果回填</span></div>
<div>
	<span>任务名称：</span><input class="mini-textbox" id="rwmc" name="rwmc" style="width:150px;"/>
	<span>抽查月份：</span><input class="mini-combobox" id="month" style="width:150px;" textField="text" valueField="id" onvaluechanged="valueChangeMonth" data="Months"/>
	 <a class="mini-button" id="find" iconCls = "icon-find"  onclick="search()">查找</a>
</div>
<div style="padding:5px;10px;5px;0">
	<a class="mini-button" id="createBut" iconCls = "icon-save"  onclick="saveData()" plain="true">保存</a>
	<a class="mini-button" id="commitBut" iconCls = "icon-ok" onclick="commitFa()" plain="true" >提交</a>
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()" plain="true">导出Excel</a>
</div>

<div id="datagrid1" class="mini-datagrid" style="width:100%;height:90%;" 
        url="/ssj/ssjScheme/SchemeResult/getSchemeDate?theme=none" idField="id" allowResize="true" allowCellEdit="true" allowCellSelect="true" multiSelect="true"
        allowCellValid="true" oncellvalidation="onCellValidation">
    <div property="columns">
     <div type="checkcolumn"></div>
        <div type="indexcolumn"></div>
        <div field="PLAN1202" width="100">机构代码</div>
        <div field="PLAN1203" width="100" align="right">单位名称</div>
        <div field="PLAN1221" vtype="required" width="100"   align="center" headerAlign="center" type="comboboxcolumn">是否发现问题
            <input property="editor" class="mini-combobox"  style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY06?theme=none"/>                
        </div>  
         <div field="PLAN1222" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">问题涉及事项
            <input property="editor" class="mini-combobox" style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY01?theme=none"/>                
        </div> 
  		<div field="PLAN1223"  width="120" headerAlign="center" allowSort="true">问题描述
               <input property="editor" class="mini-textarea" style="width:100%;" minHeight="80"/>
        </div> 
         <div field="PLAN1224" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">立案情况
             <input property="editor" class="mini-combobox" style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY08?theme=none"/>                
         </div>       
         <div field="PLAN1225" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">企业是否变化
             <input property="editor" class="mini-combobox" style="width:100%;" data="Bits"/>                
         </div>        
         <div field="PLAN1226" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">企业变化情况
             <input property="editor" class="mini-combobox"  style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY09?theme=none"/>                
         </div>       
		<div field="PLAN1227"  width="120" headerAlign="center" allowSort="true">实际生产地
               <input property="editor" class="mini-textarea" style="width:100%;" minHeight="80"/>
        </div>  
                       
    </div>
</div>


<script >
var Months=[{ id: 1, text: '一月' }, { id: 2, text: '二月'}, { id: 3, text: '三月'}, { id: 4, text: '四月'}, { id: 5, text: '五月'}, { id: 6, text: '六月'}, { id: 7, text: '七月'}, { id: 8, text: '八月'}, { id: 9, text: '九月'}, { id: 10, text: '十月'}, { id: 11, text: '十一月'}, { id: 12, text: '十二月'}];
var Bits=[{id:0,text:'否'},{id:1,text:'是'}];

mini.parse();
var grid = mini.get("datagrid1");
//grid.load();

function onCellValidation(e) {
 
}

valueChangeMonth = function(e){
	
	search();
}

function onDrawCell(e){
	debugger;
	var value = e.value; 
	var parm = /\{.*\}/;    
	 if(value!=null){
		 if(parm.exec(value)){ 
			 var str = value.split(":")[2].replace(/\'/g, "").replace(/\}/g, "");    
			 e.cellHtml =  str;
		 }
	 } 
}

function onGenderRenderer(e) {
	debugger
	var editor=e.editor;
	//if (editor.data!=null){
	    for (var i = 0, l = Months.length; i < l; i++) {
	        var g = Months[i];
	        if (g.id == e.value) return g.text;
	    }
	    return "";
	//}
}

function commitFa(){
	debugger
  
    if (grid.isChanged() == true) {
    	alert(1);
    	saveData();
    }
	var rows = grid.getSelecteds();
    if (rows.length > 0) {
		var json = mini.encode(rows);
		//alert(json);
		grid.loading("提交中，请稍后......");
		$.ajax({
			url:'/ssj/ssjScheme/SchemeResult/commitGridData?theme=none',
			type:'post',
			data:{data:json},
			success:function(e){
				if(e=="success"){
					alert("提交成功！");
					grid.reload();
				}else{
					alert("提交失败！");
				}
			}
		})      
    	
    }else {
    	alert("请选择要提交的记录！");
    }	
}

function saveData() {
    grid.validate();
    if (grid.isValid() == false) {
        var error = grid.getCellErrors()[0];
        grid.beginEditCell(error.record, error.column);
        return;
    }

    var data = grid.getChanges();
    var json = mini.encode(data);

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

function search(){
	var rwmc=mini.get("rwmc").value;	
	var month=mini.get("month").value;	

	grid.load({rwmc:rwmc,month:month});

}

importExc = function(){
	var faid = mini.get("month").value;
	grid.loading("正在导出，请稍后......");
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
			grid.reload();
		}
	});
}

</script>
</body>
</html>