<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>启动方案</title>
</head>
<body>
<div style="padding:5px;10px;5px;0">
	<span>年份：</span><input class="mini-combobox" id="year" style="width:150px;" valueField="id"/>
</div>
    <div style="width:100%;">
        <div class="mini-toolbar" style="border-bottom:0;padding:2px;">
            <table style="width:100%;">
                <tr>
                    <td style="width:100%;">
                        <a class="mini-button" iconCls="icon-add" onclick="addRow()"  tooltip="">增加</a>
                        <a class="mini-button" iconCls="icon-remove" onclick="removeRow()">删除</a>
                        <span class="separator"></span>
                        <a class="mini-button" iconCls="icon-save" onclick="qdRow()">启动</a>            
                    </td>
                </tr>
            </table>           
        </div>
    </div>
    
 <!-- 数据列表 -->
<div id="griddata" class="mini-datagrid" style="width:100%;height:98%" url="/ssj/ssjscheme/CreateScheme/getGridData?theme=none"  idFiled="id" allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true">
	<div property="columns">
		<div type="checkcolumn" width="10"></div>
		<div type="indexcolumn" width="10">序号</div>
		<div field="id" name="id"  width="100" visible="false">方案id</div>
		<div field="zftime" name="zftime" width="100" headerAlign="center"   allowSort="true">执法日期</div>
		<div field="cjtime" name="cjtime" width="100" headerAlign="center" dateFormat="yyyy-MM-dd" allowSort="true">创建时间</div>
		<div field="zt" name="zt" width="100" headerAlign="center"  >状态</div>
	</div>
</div>

<!-- 新增窗口 -->
<div id="newWin" class="mini-window" title="新增方案" style="width:300px;height:200px;" showToolbar="true" showFooter="true" >
 	<div property="toolbar" style="padding:5px;">
        <label>方案时间</label>
        <!-- <input type='button' value='' onclick="hideWindow()" style='vertical-align:middle;'/> -->
    </div>
    <div property="footer" style="text-align:right;padding:5px;padding-right:15px;">
    	<input type='button' value="确定" onclick="commitWindow()" style='vertical-align:middle;'/>
        <input type='button' value="取消" onclick="hideWindow()" style='vertical-align:middle;'/>
    </div>

    <div id="newForm" class="input_form" style="width:100%;">
		<table style="width:100%;">
			<tr><th width="20%"><label>年<font color="red">*</font>：</label></th>
				<td><input id="addYearcom" name="addYearcom" class="mini-combobox" style="width:100%" required="true"/></td>
			</tr>
			<tr><th width="20%"><label>月<font color="red">*</font>：</label></th>
				<td><input id="addMonthcom" name="addMonthcom" class="mini-combobox"  style="width:100%"  required="true"/></td>
			</tr>
		</table>
    </div>
</div>
<script >
mini.parse();
var datagrid=mini.get("griddata");
var newwin = mini.get("newWin");
var year = mini.get("year");

/* 
 * 
 这个有点问题
datagrid.url = '/ssj/ssjscheme/CreateScheme/getGridData?theme=none';
 */
 datagrid.load();

//得到年月下拉框数据
getMonthCombox = function(){
	var monthData = "[";
	for(var i=1;i<12;i++){
		var jj = "{id:'"+i+"',text:'"+i+"月'},";
		monthData+=jj;
	}
	monthData+="{id:'12',text:'12月'}]"
	return monthData;
}

var monthData = getMonthCombox();

//拼接年份下拉框数据
getYearCombox = function(){
	var d = new Date();
	var nowYear = d.getFullYear();
	var nextYear = nowYear+1;
	var next2Year = nowYear+2;
	var comboxdata = "[{id:'"+nowYear+"',text:'"+nowYear+"年'},{id:'"+nextYear+"',text:'"+nextYear+"年'},{id:'"+next2Year+"',text:'"+next2Year+"年'}]";
	year.setValue(nowYear);
	return comboxdata;
}
var yearData = getYearCombox();
year.setData(yearData);


addRow = function(){
	//控件初始化值
	var addYearcom = mini.get("addYearcom");
	var addMonthcom = mini.get("addMonthcom");
	var nowTime = new Date();

	addYearcom.setData(yearData);
	addYearcom.setValue(nowTime.getFullYear());
	addMonthcom.setData(monthData);
	addMonthcom.setValue(nowTime.getMonth()+1);

	newwin.show();
}
//删除记录
removeRow = function(){
	var selected = datagrid.getSelected();
	if(selected){
		//请求后台 传参对应的记录ID，
		var id = selected.id;
		jQuery.ajax({
			url:'/ssj/ssjscheme/createscheme/deleteScheme?theme=none',
			type:'post',
			data:{id:id},
			seccess:function(e){
				if(e=="success"){
					alert("删除成功！");
				}else if(e=="false"){
					alert("该记录已经启动不能删除！");
				}else{
					alert("删除失败！");
				}
			}
		});
		datagrid.reload();
	}else{
		mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
	}

}

qdRow = function(){
	var selectRows = datagrid.getSelecteds();

	if(selectRows.length>0){
		if(selectRows[0].zt == "已启用"){
			alert("该方案已启用！");
			return;
		}
		debugger;
		var json = mini.encode(selectRows);
		$.ajax({
			url:'/ssj/ssjscheme/CreateScheme/goStart?theme=none',
			type:'post',
			data:{data:json},
			success:function(e){
				if(e=="success"){
					alert("启用成功！");
					datagrid.reload();
				}else{
					alert("启用失败！");
				}
			}
		})
	}else{
		alert("请选择要启用的方案！");
	}
	
	
	
}

//窗口函数
function hideWindow() {
    var win = mini.get("newWin");
    win.hide();
}

function commitWindow(){
	var formdata = new mini.Form("#newForm");
	//验证表单

	formdata.validate();
 	if(!formdata.isValid()){
 		return;	
 	}

	var data = formdata.getData();
	var json = mini.decode(data);

	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/addScheme/'+json+'?theme=none',
		type:'post',
		data:json,
		success:function(e){
			var obj = mini.decode(e);
			if(obj.inf=="true"){
				alert("保存成功！");
				datagrid.reload();
			}else{
				alert(obj.text);
			}
			newwin.hide();
		}
	})
}

</script>
</body>
</html>