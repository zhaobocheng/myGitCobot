<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>随机企业权重管理</title>
</head>
<body>
	<div class="mini-panel" style="width: 100%; height: 100%"
		showHeader="false" showToolbar="true" showFooter="false">
		
		<div property="toolbar">
			<table>
				<tr>
					<td>
						<span>&nbsp;&nbsp;方案时间：</span><input class="mini-combobox" id="zftime"
							name="zftime" onvaluechanged="valueChange"
							textField="text" valueField="id"
							url="/ssj/personmanage/personmanage/getZfcData?theme=none" />
							 <a class="mini-button" iconCls="icon-upload" id="setWeigBut" onclick="setAuthenRow()">设置权重</a>
					</td>					
					<td>
						<span>&nbsp;&nbsp;随机结果规则：至少特设：</span><input class="mini-textbox" id="zsts"
							style="width: 150px;" /> <span>&nbsp;&nbsp;至少计量：</span><input
							class="mini-textbox" id="zsjl" style="width: 150px;" />
					</td>
					<td><a class="mini-button" id="mommitBut" onclick="mommit()">提交</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit">
			<!-- 数据列表 -->
			<div id="griddata" class="mini-datagrid"
				style="width: 100%; height: 100%"
				url="/ssj/companymanage/CompanyManage/getGridData/ss?theme=none"
				idFiled="id" showPager="false" multiSelect="true"
				allowCellEdit="true" allowCellSelect="true"
				editNextOnEnterKey="true" editNextRowCell="true"
				oncellvalidation="onCellValidation(e)">
				<div property="columns">
					<div type="checkcolumn" width="10"></div>
					<div type="indexcolumn" width="15">序号</div>
					<div field="id" name="id" width="100" visible="false">id</div>
					<div field="qxid" name="qxid" width="100" visible="false">qxid</div>
					<div field="qx" name="qx" width="100" headerAlign="center"
						align="center" allowSort="true">区县</div>
					<div field="cyqys" name="cyqys" width="100" headerAlign="center"
						align="center" allowSort="true">参与企业数</div>
					<div field="cyzfrys" name="cyzfrys" width="100"
						headerAlign="center" align="center" allowSort="true">参与执法人员数</div>
					<div field="sjqyzs" name="sjqyzs" vtype="required;int" width="100"
						headerAlign="center" allowSort="true">
						随机企业总数 <input property="editor" class="mini-spinner" minValue="0"
							maxValue="200" value="4" style="width: 100%;" />
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- 权限设置窗口 -->
	<div id="newWin" class="mini-window" title="设置权重"
		style="display: none; width: 500px; height: 300px;" showToolbar="true"
		showFooter="true">
		<div property="toolbar" style="padding: 5px;">
			<label>企业权重规则</label>
			<!-- <input type='button' value='' onclick="hideWindow()" style='vertical-align:middle;'/> -->
		</div>
		<div property="footer"
			style="text-align: right; padding: 5px; padding-right: 15px;">
			<input type='button' value="确定" onclick="commitWindow()"
				style='vertical-align: middle;' /> <input type='button' value="取消"
				onclick="hideWindow()" style='vertical-align: middle;' />
		</div>

		<div id="newForm" class="input_form" style="width: 100%;">
			<table style="width: 100%; text-align: center;">
				<tr>
					<td width="15%"><label>序号</label></td>
					<td width="25%"><label>企业特性</label></td>
					<td width="30%"><label>权重值</label></td>
					<td width="20%"><label></label></td>
				</tr>
				<tr>
					<td>1</td>
					<th><label>特设<font color="red">*</font>：
					</label></th>
					<td><input id="ts" name="ts" class="mini-textbox"
						style="width: 100%" required="true" /></td>
					<td><input id="tssy" name="tssy" class="mini-checkbox"
						style="width: 100%" text="不适用" required="true" /></td>
				</tr>
				<tr>
					<td>2</td>
					<th width="20%"><label>计量<font color="red">*</font>：
					</label></th>
					<td><input id="jl" name="jl" class="mini-textbox"
						style="width: 100%" required="true" /></td>
					<td><input id="jlsy" name="jlsy" class="mini-checkbox"
						style="width: 100%" text="不适用" required="true" /></td>
				</tr>
				<tr>
					<td>3</td>
					<th width="20%"><label>强制性认证<font color="red">*</font>：
					</label></th>
					<td><input id="qzrz" name="qzrz" class="mini-textbox"
						style="width: 100%" required="true" /></td>
					<td><input id="qzrzsy" name="qzrzsy" class="mini-checkbox"
						style="width: 100%" text="不适用" required="true" /></td>
				</tr>
				<tr>
					<td>4</td>
					<th width="20%"><label>标准<font color="red">*</font>：
					</label></th>
					<td><input id="bz" name="bz" class="mini-textbox"
						style="width: 100%" required="true" /></td>
					<td><input id="bzsy" name="bzsy" class="mini-checkbox"
						style="width: 100%" text="不适用" required="true" /></td>
				</tr>
			</table>
		</div>
	</div>

	<script>
		mini.parse();
		var newwin = mini.get("newWin");
		var zfcom = mini.get("zftime");
		zfcom.select(0);

		var grid = mini.get("griddata");
		var url = "/ssj/companymanage/CompanyManage/getGridData/" + zfcom.value
				+ "?theme=none";
		grid.setUrl(url);
		grid.load();

		

		function isup(e){
			var setWeigBut = mini.get("setWeigBut");
			var mommitBut = mini.get("mommitBut");
			
			jQuery.ajax({
				url:'/ssj/companymanage/CompanyManage/getZT/'+e+'?theme=none',
				type:'post',
				success:function(e){
					if(e=="2"){
						mommitBut.setEnabled(true);
						setWeigBut.setEnabled(false);
					}else if(e=="1" || e=="select"){
						mommitBut.setEnabled(true);
						setWeigBut.setEnabled(true);
					}else{
						mommitBut.setEnabled(false);
						setWeigBut.setEnabled(false);
					}
				}	
			});
		};

		isup(zfcom.value);
		
		valueChange = function(e) {
			url = "/ssj/companymanage/CompanyManage/getGridData/" + e.value
					+ "?theme=none";
			grid.setUrl(url);
			grid.reload();
			isup(e.value);
		}

		setAuthenRow = function() {
			newwin.show();
		}

		//提交权重
		function commitWindow() {
			var formdata = new mini.Form("#newForm");
			//验证表单
			formdata.validate();
			if (!formdata.isValid()) {
				return;
			}

			var data = formdata.getData();
			var zfid = mini.get("zftime").value;
			var json = mini.decode(data);

			$.ajax({
				url : '/ssj/companymanage/companymanage/addWeightCon/' + zfid
						+ '?theme=none',
				type : 'post',
				data : json,
				success : function(e) {
					if (e == "success") {
						alert("保存成功！");
						datagrid.reload();
						newwin.hide();
					} else {
						alert("保存失败！");
						datagrid.reload();
					}
					newwin.hide();
				}
			})
		}
		function hideWindow() {
			var win = mini.get("newWin");
			win.hide();
		}

		//提交按钮触发
		function mommit() {

			grid.validate();
			if (grid.isValid() == false) {
				var error = grid.getCellErrors()[0];
				grid.beginEditCell(error.record, error.column);
				return;
			}
			var data = grid.getChanges();
			var json = mini.encode(data);
			var zfid = mini.get("zftime").value;
			var zsts = mini.get("zsts").value;
			var zsjl = mini.get("zsjl").value;

			grid.loading("保存中，请稍后......");
			$.ajax({
				url : "/ssj/companymanage/companymanage/addSJResult/" + zfid
						+ "?theme=none",
				data : {
					data : json,
					zsts : zsts,
					zsjl : zsjl
				},
				type : "post",
				success : function(text) {
					if(text=="false"){
						grid.reload();
						alert("请先设置权重");
					}else{
						grid.reload();
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					alert(jqXHR.responseText);
				}
			});
		}
		//每次数值校验企业的数量不能为空
		grid.on("cellcommitedit", function(e) {
			if (e.field == "sjqyzs") {
				if (e.value == "") {
					alert("企业总数不能设置为空！");
					e.cancel = true;
				}
			}
		});

		function onCellValidation(e) {
			if (e.field == "sjqyzs") {
				if (e.value = "") {
					e.isValid = false;
					e.errorText = "随机总人数不能为空！";
				}
			}
		}
	</script>
</body>
</html>