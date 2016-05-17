<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui3">
<title>加密测试页面</title>
</head>
<body>

<b>base64加密</b>
<div>原始内容：<input class="mini-textbox" id="orginaltext" name="orginaltext"/></div>
<div>BASE64加密后：</div>
<div>BASE64解密后：</div>


<div><a class="mini-button" onclick="run()">开始运算</a></div>

<script type="text/javascript">
mini.parse();
function run(){
	var orginaltext = mini.get("orginaltext").getValue();
	$.ajax({
		url:'/bopmain/encryption/Coder/runEncryption/'+orginaltext+'?theme=none',
		data:{},
		type:'post',
		success:function(text){
			var data = mini.decode(text);
			alert(data);
		}
	})
}
</script>
</body>
</html>