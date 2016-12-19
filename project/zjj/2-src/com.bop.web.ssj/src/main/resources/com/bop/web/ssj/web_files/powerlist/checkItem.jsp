<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>检查事项</title>
</head>
<body>
<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
	<span>事项名称：</span><input class="mini-textbox"  id="sxmc_sousuo" name="sxmc" style="width:230px;" onvaluechanged="findItemRow()"/>
	<span>事项分类：</span><input class="mini-combobox" id="sxflei" name="sxfl" style="width:230px;" onvaluechanged="findItemRow()" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemFL?theme=none"/>
	<span>是否显示废弃：</span>
	<label><input style="width:20px;" name="feiqi" type="radio" checked="checked" onclick="findItemRow()" value="1" />是</label> 
	<label><input style="width:20px;" name="feiqi" type="radio" value="2" onclick="findItemRow()" />否 </label>
	</br>
	<span>权力名称：</span><input class="mini-textbox" id="qlmcing" name="qlmc" style="width:230px;" onvaluechanged="findItemRow()"/>
	<span>权力编码：</span><input class="mini-textbox" id="qlbma" name="qlbm" style="width:230px;" onvaluechanged="findItemRow()"/>
	<span style="padding-left:50px">
		<a  class="mini-button" id="find" iconCls = "icon-find"  onclick="findItemRow()">查找</a>
	</span>
</div>
 <div style="width:100%;">
        <div class="mini-toolbar" style="border-bottom:0;padding:2px;">
            <table style="width:100%;">
                <tr>
                    <td style="width:100%;">
                        <a class="mini-button" iconCls="icon-add" onclick="addItem()"  tooltip="">新增</a>
                        <a class="mini-button" iconCls="icon-edit" onclick="editItem()"  tooltip="">修改</a>
                        <a class="mini-button" iconCls="icon-remove" onclick="deleteItem()">删除</a>
                        <span class="separator"></span>
                        <a class="mini-button" iconCls="icon-ok" onclick="submitItem()">提交</a>
                       <!--  <a class="mini-button" iconCls="icon-reload" onclick="updateItem()">修订</a> -->
                    	<a class="mini-button" iconCls="icon-cut" onclick="quitItem()">废弃</a>            
                    </td>
                </tr>
            </table>           
        </div>
 </div>

<div class="mini-fit">
	<div class="mini-datagrid" id="datagridItem" style="width:100%;height:100%;"
		 url="/ssj/powerlist/PowerList/getCheckData?theme=none" showFooter="true" idField="id"
		pageSize="20" sizeList="[20,30,50,100]">
		<div property="columns">
			<div type="checkcolumn"></div>
			<div type="indexcolumn" width="20" headerAlign="center">序号</div>
			<div field="sxfl" width="80" headerAlign="center"  align="center">事项分类</div>
			<div field="jcsxmc" width="60" headerAlign="center" align="center">检查事项名称</div>
			<div field="ccdx" 	width="60"  headerAlign="center" align="center">抽查对象</div>
			<div field="ccyj"	width="100" headerAlign="center"  align="center">抽查依据</div>
			<div field="qlqdStr"  width="100" headerAlign="center"  align="center">权力清单名称[编码+名称]</div>
			<div field="status"  width="30" headerAlign="center"  align="center">状态</div>
		</div>
	</div>
</div>

<input type="hidden"  id="temp" value="">



<!-- 新增检查事项窗口 -->
<div id="newItemWin" class="mini-window" title="新增检查事项" style="width:730px;height:530px;" showToolbar="true" showFooter="true" >
 	 <div id="addForm" class="input_form" style="width:100%;">
		<table style="width:100%;">
			<tr height="72px"><th width="20%"><label>事项名称<font color="red">*</font>：</label></th>
				<td><input id="sxmc" name="sxmc" class="mini-textbox" style="width:90%" required="true"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>事项分类<font color="red">*</font>：</label></th>
				<td><input class="mini-combobox" id="sxfl" name="sxfl" style="width:90%" required="true" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemFL?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>对应权力清单<font color="red">*</font>：</label></th>
					<td  hidden="true"><input id="dyqlqdId"  class="mini-textbox"   name="dyqlqdid"   style="width:75%" /></td>
					<td>
						<input id="dyqlqd"   onclick="chooseItem()"  class="mini-textbox"  style="width:75%"  required="true"/>
						<span> <a class="mini-button" iconCls="icon-search"  style="width:15%" onclick="chooseItem()"  tooltip="">搜索</a></span>
					</td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查对象<font color="red">*</font>：</label></th>
					<td><input id="ccdx" name="ccdx" class="mini-combobox" height="100px"  style="width:90%"  textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemCCDX?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查依据<font color="red">*</font>：</label></th>
					<td><input id="ccyj" name="ccyj" class="mini-combobox" height="100px" style="width:90%"   textField="text" valueField="id"  multiSelect="true" url="/ssj/powerlist/PowerList/getItemCCYJ?theme=none"/></td>
			</tr>
		</table>
    </div>
    <div property="footer" style="text-align:right;padding:5px;padding-right:15px;">
    	<input type='button' value="确定" onclick="submitAddItem('1')" style='vertical-align:middle;'/>
        <input type='button' value="取消" onclick="hideWindow('1')" style='vertical-align:middle;'/>
    </div>
    
</div>
<!-- 编辑检查事项窗口 -->
<div id="editItemWin" class="mini-window" title="修改检查事项" style="width:730px;height:530px;" showToolbar="true" showFooter="true" >
 	 <div id="editItemForm" class="input_form" style="width:100%;">
		<table style="width:100%;">
			<tr hidden="true" height="50px"><th width="20%"></th>
				<td><input id="sxmcid" name="id" class="mini-textbox" style="width:90%" required="true"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>事项名称<font color="red">*</font>：</label></th>
				<td><input id="sxmc" name="sxmc" class="mini-textbox" style="width:90%" required="true"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>事项分类<font color="red">*</font>：</label></th>
				<td><input class="mini-combobox" id="sxfl" name="sxfl" style="width:90%" required="true" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemFL?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>对应权力清单<font color="red">*</font>：</label></th>
					
					<td hidden="true"><input id="dyqlqdIdEdit"  class="mini-textbox"   name="dyqlqdid"   style="width:75%" /></td>
					<td>
						<input id="dyqlqdEdit"  name="dyqlqdName"  class="mini-textbox"  style="width:75%"    required="true"/>
						<span> <a class="mini-button" iconCls="icon-search"  style="width:15%" onclick="chooseItem()"  tooltip="">搜索</a></span>
					</td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查对象<font color="red">*</font>：</label></th>
					<td><input id="ccdx" name="ccdx" class="mini-combobox" height="100px"  style="width:90%"  required="true" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemCCDX?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查依据<font color="red">*</font>：</label></th>
					<td><input id="ccyj" name="ccyj" class="mini-combobox" height="100px" style="width:90%"  required="true" textField="text" valueField="id"  multiSelect="true" url="/ssj/powerlist/PowerList/getItemCCYJ?theme=none"/></td>
			</tr>
		</table>
    </div>
    <div property="footer" style="text-align:right;padding:5px;padding-right:15px;">
    	<input type='button' value="确定" onclick="submitAddItem('2')" style='vertical-align:middle;'/>
        <input type='button' value="取消" onclick="hideWindow('2')" style='vertical-align:middle;'/>
    </div>
    
</div>
<!-- 修订检查事项窗口 -->
<div id="updateItemWin" class="mini-window" title="修订检查事项" style="width:730px;height:530px;" showToolbar="true" showFooter="true" >
 	 <div id="updateItemForm" class="input_form" style="width:100%;">
		<table style="width:100%;">
			<tr hidden="true" height="50px"><th width="20%"></th>
				<td><input id="sxmcid" name="id" class="mini-textbox" style="width:90%" required="true"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>事项名称<font color="red">*</font>：</label></th>
				<td><input id="sxmc" name="sxmc" class="mini-textbox" style="width:90%" required="true"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>事项分类<font color="red">*</font>：</label></th>
				<td><input class="mini-combobox" id="sxfl" name="sxfl" style="width:90%" required="true" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemFL?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>对应权力清单<font color="red">*</font>：</label></th>
					
					<td  hidden="true"><input id="dyqlqdIdUpdate"  class="mini-textbox"   name="dyqlqdid"   style="width:75%" /></td>
					<td>
						<input id="dyqlqdUpdate"   class="mini-textbox" value=""  style="width:75%"  required="true"/>
						<span> <a class="mini-button" iconCls="icon-search"  style="width:15%" onclick="chooseItem()"  tooltip="">搜索</a></span>
					</td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查对象<font color="red">*</font>：</label></th>
					<td><input id="ccdx" name="ccdx" class="mini-combobox" height="100px"  style="width:90%"  required="true" textField="text" valueField="id" url="/ssj/powerlist/PowerList/getItemCCDX?theme=none"/></td>
			</tr>
			<tr height="72px"><th width="20%"><label>抽查依据<font color="red">*</font>：</label></th>
					<td><input id="ccyj" name="ccyj" class="mini-combobox" height="100px" style="width:90%"  required="true" textField="text" valueField="id"  multiSelect="true" url="/ssj/powerlist/PowerList/getItemCCYJ?theme=none"/></td>
			</tr>
		</table>
    </div>
    <div property="footer" style="text-align:right;padding:5px;padding-right:15px;">
    	<input type='button' value="确定" onclick="submitAddItem('3')" style='vertical-align:middle;'/>
        <input type='button' value="取消" onclick="hideWindow('3')" style='vertical-align:middle;'/>
    </div>
    
</div>


<!-- 选择权力清单页面 -->
<div id="chooseItemWin" class="mini-window" title="选择权力清单" style="width:870px;height:580px;" showToolbar="true" showFooter="true" >
	<div style="padding-top: 10px;padding-bottom: 10px;padding-left:15px">
		<table>
			<tr>
				<td>权力清单编码：</td>
				<td><input class="mini-textbox" id="qlbm" name="rwmc" style="width:220px;" onvaluechanged="findRow()"/></td>
				<td>权力清单名称：</td>
				<td><input class="mini-textbox" id="qlmc" name="qlmc" style="width:220px;" onvaluechanged="findRow()"/></td>
			</tr>
			<tr>
				<td>权力分类：</td>
				<td colspan="3"><input class="mini-combobox" id="qlfl" name="qlfl" style="width:220px;" onvaluechanged="findRow()" textField="text" valueField="id"    url="/ssj/powerlist/PowerList/getPowerFL?theme=none"/>&nbsp;&nbsp;
					<a class="mini-button" id="find" iconCls = "icon-find"  onclick="findRow()">查找</a>
					<a class="mini-button" id="find" iconCls = "icon-save"  onclick="savePowerList()">保存勾选</a>
				</td>
			
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div class="mini-datagrid" id="powerDatagrid" style="width:100%;height:100%;" url="/ssj/powerlist/PowerList/getPowerListData?theme=none" showFooter="true" idField="id" multiSelect="true"
			pageSize="20" sizeList="[20,30,50,100]">
			<div property="columns">
				<div type="checkcolumn"></div>
				<div type="indexcolumn" width="20" headerAlign="center">序号</div>
				<div field="qlqdbm" width="80" headerAlign="center">权力清单编码</div>
				<div field="qlsxmc" width="120" headerAlign="center">权力清单名称</div>
				<div field="qlfl" width="80" headerAlign="center">权力分类</div>
			</div>
		</div>
	</div>
</div>

<script >
mini.parse();
var datagrid=mini.get("datagridItem");
datagrid.load();

var powerDatagrid=mini.get("powerDatagrid");
powerDatagrid.load();
function findRow(){
	var qlbm = mini.get("qlbm").value;
	var qlmc = mini.get("qlmc").value;
	var qlfl = mini.get("qlfl").value;
	powerDatagrid.load({qlbm:qlbm,qlmc:qlmc,qlfl:qlfl});
}

function findItemRow(){
	var sxmc = mini.get("sxmc_sousuo").value;
	var sxfl = mini.get("sxflei").value;
	var qlbm = mini.get("qlbma").value;
	var qlmc = mini.get("qlmcing").value;
	var feiqi=$('input:radio[name="feiqi"]:checked').val();
	datagrid.load({sxmc:sxmc,sxfl:sxfl,qlbm:qlbm,qlmc:qlmc,feiqi:feiqi});
}
<!--新增-->
newItemWin=mini.get("newItemWin");
addItem = function(){
	new mini.Form("#addForm").clear();
	$("#temp").val(1);//新增
	newItemWin.show();
}
<!--选择权力页面-->
chooseItem=function(){
	chooseItemWin=mini.get("chooseItemWin");
	chooseItemWin.show();
}
<!--保存勾选的权力清单-->
function savePowerList(){
	var st = powerDatagrid.getSelecteds();
	var strId="";
	var strName="";
	for(i=0;i<st.length;i++){
		if(i!=st.length-1){
			strId += st[i].id + ";";
			strName+=st[i].qlsxmc+";";
		}else{
			strId += st[i].id;
			strName+=st[i].qlsxmc;
		}
	}
	var flag=$("#temp").val();
	if(flag==1){
		var sdId=mini.get("dyqlqdId");
		var sdName=mini.get("dyqlqd");
		sdId.setValue(strId);
		sdName.setValue(strName);
		
	}else if(flag==2){
		var sdId=mini.get("dyqlqdIdEdit");
		var sdName=mini.get("dyqlqdEdit");
		sdId.setValue(strId);
		sdName.setValue(strName);
	}else if(flag==3){
		var sdId=mini.get("dyqlqdIdUpdate");
		var sdName=mini.get("dyqlqdUpdate");
		sdId.setValue(strId);
		sdName.setValue(strName);
	}
	chooseItemWin.hide();
}


<!--保存-->
function submitAddItem(flag){
	var formdata
	if(flag==1){
		formdata = new mini.Form("#addForm");
	}else if(flag==2){
		formdata = new mini.Form("#editItemForm");
	}else if(flag==3){
		formdata = new mini.Form("#updateItemForm");
	}
	//验证表单
	formdata.validate();
 	if(!formdata.isValid()){
 		return;	
 	}
	var data = formdata.getData();
	var json = mini.decode(data);
	$.ajax({ 
		url:'/ssj/powerlist/PowerList/addItem?theme=none',
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
			if(flag==1)
				newItemWin.hide();
			else if(flag==2)
				editItemWin.hide();
			else if(flag==3)
				updateItemWin.hide();

		}
	})
}

<!--删除检查事项-->
function deleteItem(){
	var selected = datagrid.getSelected();
	if(selected){
		if(selected.status!="未提交"){
			alert("只能删除未提交状态的记录！");
			return;
		}
		//请求后台 传参对应的记录ID，
		var id = selected.id;
		jQuery.ajax({
			url:'/ssj/powerlist/PowerList/deleteItem?theme=none',
			type:'post',
			data:{id:id},
			success:function(e){
				var obj = mini.decode(e);
				if(obj.inf=="true"){
					alert("删除成功！");
				}else{ 
					alert("删除失败！");
				}
				datagrid.reload();
			}
		});
	}else{
		mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
	}
}
<!--编辑-->
editItemWin=mini.get("editItemWin");
<!--修改(编辑)检查事项-->
function editItem(){
	$("#temp").val(2);//编辑
	var editItemForm = new mini.Form("editItemForm"); 
	var selected = datagrid.getSelected();
	if(selected){
		if(selected.status!="未提交"&&selected.status!="修订中"){
			alert("只能修改未提交状态的记录！");
			return;
		}
		var id = selected.id;
		$.ajax({
			url: "/ssj/powerlist/PowerList/editItem?theme=none",
			type: "post",
			data:{id:id},
			success: function (text) {
				var data = mini.decode(text);
				editItemWin.show();
				editItemForm.setData(data);
			}
		});
  
	}else{
		mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
	}
}
<!--提交 状态为“未提交”的记录-->
submitItem=function(){
	var selected = datagrid.getSelected();
	if(selected){
		if(selected.status!="未提交"&&selected.status!="修订中"){
			alert("只能提交未提交状态的记录！");
			return;
		}
		var id = selected.id;
		$.ajax({
			url: "/ssj/powerlist/PowerList/submitItem?theme=none",
			type: "post",
			data:{id:id},
			success: function (text) {
				var obj = mini.decode(text);
				if(obj.inf=="true"){
					alert("提交成功！");
				}else{ 
					alert("提交失败！");
				}
				datagrid.reload();
				
			}
		});
  
	}else{
		mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
	}
}
<!--编辑-->
updateItemWin=mini.get("updateItemWin");
/**
 * 修订事项
 */
 updateItem=function(){
	 $("#temp").val(3);//修订
		var updateItemForm = new mini.Form("updateItemForm"); 
		var selected = datagrid.getSelected();
		if(selected){
			if(selected.status!="已提交"){
				alert("只能修订已提交的记录！");
				return;
			}
			var id = selected.id;
			$.ajax({
				url: "/ssj/powerlist/PowerList/editItem?theme=none",
				type: "post",
				data:{id:id},
				success: function (text) {
					var data = mini.decode(text);
					updateItemWin.show();
					updateItemForm.setData(data);
				}
			});
	  
		}else{
			mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
		}
}

/**
 * 废弃事项
 */
 quitItem=function(){
		var selected = datagrid.getSelected();
		if(selected){
			if(selected.status!="已提交"){
				alert("只能废弃已提交的记录！");
				return;
			}
			var id = selected.id;
			$.ajax({
				url: "/ssj/powerlist/PowerList/quitItem?theme=none",
				type: "post",
				data:{id:id},
				success: function (text) {
					var obj = mini.decode(text);
					if(obj.inf=="true"){
						alert("废弃成功！");
					}else{ 
						alert("废弃失败！");
					}
					datagrid.reload();
				}
			});
	  
		}else{
			mini.showTips({content:"请先选择一条记录",state:"default",x:"center",y:"top"});
		}
}

<!--取消窗口-->
//窗口函数
function hideWindow(flag) {
	if(flag==1)
    	newItemWin.hide();
	else if(flag==2)
		editItemWin.hide();
	else if(flag==3)
		updateItemWin.hide();
}

</script>

</body>
</html>