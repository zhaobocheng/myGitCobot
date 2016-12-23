<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>启动方案</title>
</head>
<body>
<div style="padding:5px;10px;5px;0">
	<span>年份：</span><input class="mini-combobox" id="year" style="width:150px;" valueField="id" onvaluechanged="valueChange"/>
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
                        <a class="mini-button" iconCls="icon-up" onclick="setxyxs()">设置信用系数</a>   
                    </td>
                </tr>
            </table>
        </div>
    </div>
 <!-- 数据列表 -->
 <div class="mini-fit">
	<div id="griddata" class="mini-datagrid" style="width:100%;height:98%" url=""  idFiled="id" allowResize="true" sizeList="[20,30,50,100]" pageSize="20" showFooter="true">
		<div property="columns">
			<div type="checkcolumn" width="10"></div>
			<div type="indexcolumn" width="20">序号</div>
			<div field="id" name="id"  width="100" visible="false">方案id</div>
			<div field="famc" name="famc" width="100" headerAlign="center"   allowSort="true">方案名称</div>
			<div field="zftime" name="zftime" width="100" headerAlign="center"   allowSort="true">执法日期</div>
			<div field="cjtime" name="cjtime" width="100" headerAlign="center" dateFormat="yyyy-MM-dd" allowSort="true">创建时间</div>
			<div field="zt" name="zt" width="100" headerAlign="center"  >状态</div>
		</div>
	</div>
</div>
<!-- 新增窗口 -->
<div id="newWin" class="mini-window" title="新增方案" style="width:530px;height:430px;" showToolbar="true" showFooter="true" >
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
			<tr><th width="30%"><label>方案名称<font color="red">*</font>：</label></th>
					<td><input id="faname" name="faname" class="mini-textbox"  style="width:100%"  required="true"/></td>
			</tr>
			<tr><th width="30%"><label>方案对应事项<font color="red">*</font>：</label></th>
					<td><input id="items" name="items" class="mini-textbox"  onclick="chooseItem()"  style="width:100%"  required="true"/>
						<input id="itemsId"  class="mini-hidden"   name="itemsId"   style="width:75%" />
					</td>
			</tr>
			<tr><td colspan="2">设置风险系数</td>
			</tr>
			<tr><th width="30%"><label>高级<font color="red">*</font>：</label></th>
					<td><input id="gjfx" name="gjfx" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="1"/></td>
			</tr>
			<tr><th width="30%"><label>中级<font color="red">*</font>：</label></th>
					<td><input id="zjfx" name="zjfx" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="0.5"/></td>
			</tr>
			<tr><th width="30%"><label>低级<font color="red">*</font>：</label></th>
					<td><input id="djfx" name="djfx" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="0.1"/></td>
			</tr>
		</table>
    </div>
   <!--  <div id="cbl1" class="mini-checkboxlist" repeatItems="1" repeatLayout="table" textField="text" valueField="id" onvaluechanged = "valuechang" url="/ssj/count/CountPage/getCheckDate" ></div> -->
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
					<a class="mini-button" id="find" iconCls = "icon-find"  onclick="findRow()">查找</a>
					<a class="mini-button" id="find" iconCls = "icon-save"  onclick="savePowerList()">保存</a>
				</td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div class="mini-datagrid" id="powerDatagrid" style="width:100%;height:100%;" url="/ssj/taskmanage/taskOperation/getitemsData?theme=none" showFooter="true" idField="id" multiSelect="true"
			pageSize="20" sizeList="[20,30,50,100]">
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

<!-- 设置信用等级系数-->
<div id="xydjWindow" class="mini-window" title="设置信用等级" style="width:370px;height:280px;" showToolbar="true" showFooter="true" >
	 <div property="footer" style="text-align:right;padding:5px;padding-right:15px;">
    	<input type='button' value="确定" onclick="xycommit()" style='vertical-align:middle;'/>
        <input type='button' value="取消" onclick="xyhideWindow()" style='vertical-align:middle;'/>
    </div>

    <div id="xydjForm" class="input_form" style="width:100%;">
		<table style="width:100%;">
			<tr><th width="35%"><label>A级信用系数<font color="red">*</font>：</label></th>
					<td><input id="ajxy" name="ajxy" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="2"/></td>
			</tr>
			<tr><th width="35%"><label>B级信用系数<font color="red">*</font>：</label></th>
					<td><input id="bjxy" name="bjxy" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="1"/></td>
			</tr>
			<tr><th width="35%"><label>C级信用系数<font color="red">*</font>：</label></th>
					<td><input id="cjxy" name="cjxy" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="0.5"/></td>
			</tr>
			<tr><th width="35%"><label>D级信用系数<font color="red">*</font>：</label></th>
					<td><input id="djxy" name="djxy" class="mini-textbox"  style="width:100%"  required="true" vtype="float" value="0.2"/></td>
			</tr>
		</table>
    </div>
</div>

<script >
mini.parse();
var datagrid=mini.get("griddata");
var newwin = mini.get("newWin");
var year = mini.get("year");
//选择检查事项的加载列表
var powerDatagrid=mini.get("powerDatagrid");
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

datagrid.setUrl("/ssj/taskmanage/taskoperation/getGridData?theme=none&year="+year.value);
datagrid.load();

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
			url:'/ssj/taskmanage/taskoperation/deleteScheme?theme=none',
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
				datagrid.reload();
			}
		});
		datagrid.reload();
	}else{
		mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
	}
}

qdRow = function(){
	var selectRows = datagrid.getSelecteds();
	datagrid.loading("正在启动，请稍后......");

	if(selectRows.length>0){
		if(selectRows[0].zt == "已启用"){
			alert("该方案已启用！");
			return;
		}
		var json = mini.encode(selectRows);
		$.ajax({
			url:'/ssj/taskmanage/taskoperation/goStart?theme=none',
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
		url:'/ssj/taskmanage/taskoperation/addScheme/'+json+'?theme=none',
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



valueChange = function(){
	var nd = mini.get("year").value;
	url = "/ssj/taskmanage/taskoperation/getGridData?theme=none&year="+year.value;
	datagrid.setUrl(url);
	datagrid.reload();
}


<!--选择选择检查事项-->
chooseItem=function(){
	chooseItemWin=mini.get("chooseItemWin");
	powerDatagrid.load();
	chooseItemWin.show();
}

<!--保存选中的检查事项-->
function savePowerList(){
	var st = powerDatagrid.getSelecteds();
	var strId="";
	var strName="";
	for(i=0;i<st.length;i++){
		if(i!=st.length-1){
			strId += st[i].id + ";";
			strName+=st[i].itemmc+";";
		}else{
			strId += st[i].id;
			strName+=st[i].itemmc;
		}
	}

	//var flag=$("#temp").val();
	//if(flag==1){
		var sdId=mini.get("itemsId");
		var sdName=mini.get("items");
		sdId.setValue(strId);
		sdName.setValue(strName);
		
/* 	}else if(flag==2){
		var sdId=mini.get("itemsIdEdit");
		var sdName=mini.get("items");
		sdId.setValue(strId);
		sdName.setValue(strName);
	}else if(flag==3){
		var sdId=mini.get("itemsIdUpdate");
		var sdName=mini.get("items");
		sdId.setValue(strId);
		sdName.setValue(strName);
	} */
	chooseItemWin.hide();
}

function findRow(){
	var itemmc = mini.get("itemmc").value;
	var itemdx = mini.get("itemdx").value;
	var itemfl = mini.get("itemfl").value;
	powerDatagrid.load({itemmc:itemmc,itemdx:itemdx,itemfl:itemfl});
}

setxyxs = function(){
	 var win = mini.get("xydjWindow");
	 win.show();
}

//窗口函数
function xyhideWindow() {
    var win = mini.get("xydjWindow");
    win.hide();
}

function xycommit(){
	var formdata = new mini.Form("#xydjForm");
	formdata.validate();
 	if(!formdata.isValid()){
 		return;	
 	}
	var data = formdata.getData();
	var json = mini.decode(data);

	$.ajax({
		url:'/ssj/taskmanage/taskoperation/addXydj/'+json+'?theme=none',
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