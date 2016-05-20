<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>修改密码</title>
</head>
<body>
<%
	String reset = request.getParameter("reset");
	String userId = request.getParameter("userid");
	String pwRegular = System.getProperty("bop.safety.passwordRule.regular", ".*");
	String pwPrompt = System.getProperty("bop.safety.passwordRule.errorPromptText", "输入的新密码格式不符合要求");
%>
<div class="mini-panel" style="width:100%;height:100%" showHeader="false" showFooter="true" allowResize="false">
	<div class="input_form" id="form1">
		<table style="width: 100%; heigth: 100%">
			<tr>
				<th width="30%"><label for="name">用户名：</label></th>
				<td width="70%"><input style="width:98%;" id="name1" name="name1"
					class="mini-textbox" readonly="true" required="true" value="<%=userId%>"/>
				</td>
			</tr>
			<tr>
				<th><label for="name">旧密码<font color=red>*</font>：</label></th>
				<td><input style="width: 98%" id="oldpassword" name="oldpassword"
					class="mini-password" required="true" />
				</td>
			</tr>
			<tr>
				<th><label for="name">新密码<font color=red>*</font>：</label></th>
				<td><input style="width: 98%" id="password2" name="password2"
					class="mini-password" required="true" />
				</td>
			</tr>
			<tr>
				<th><label for="name">确认新密码<font color=red>*</font>：</label></th>
				<td><input style="width: 98%" id="password3" name="password3"
					class="mini-password" required="true" />
				</td>
			</tr>
		</table>
	</div>
	<div property="footer" style="text-align:right;">
		<a type="button" class="mini-button" iconCls="icon-save" onclick="submitInput()">确定</a>
		<a type="button" class="mini-button" iconCls="icon-cancel" onclick="cancel()">取消</a>
	</div>
</div>
<script>
mini.parse();
var reset = '<%=reset%>'; 
var userId = '<%=userId%>';

var pwlength = null;
var pwgz = null;
var pwRegular = new RegExp('<%= pwRegular %>');
var pwPrompt = '<%= pwPrompt %>';

function submitInput() {
	var newPwd = mini.get("password2").getValue();

	var form = new mini.Form("#form1");
    form.validate();
    if (form.isValid() == false) return;

  	if (newPwd != mini.get("password3").getValue()) {
  		mini.showTips('两次输入的密码必须一致！');
  		return;
  	}
    
    //校验密码输入的合法性
  	if(!pwRegular.test(newPwd)){
  		mini.showTips(pwPrompt);
  		return;
  	}
  	
	//如果是修改密码，校验旧密码的正确性
	$.ajax({
		url: '/bopmain/user/rygl/changePassword?theme=none',
		data: {id:userId , password: newPwd,oldpassword: mini.get("oldpassword").getValue()},
		type:"post",
		success: function(json) {
			var data = mini.decode(json);
			if(!data.success) {
				mini.showTips("修改密码失败，可能是输入的旧密码不正确");
				CloseWindow("no");
			} else {
				mini.showTips("密码修改成功！");
				CloseWindow("ok");
			}
		}
	});
}

function CloseWindow(action) {
	if (window.CloseOwnerWindow) return window.CloseOwnerWindow(action);
	else window.close();
}
function cancel() {
	CloseWindow("close");
}

</script>
</body>
</html>