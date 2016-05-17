<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.bop.web.PathUtil" %>
<html>
<head>
<meta charset="utf-8" >
<meta http-equiv="X-UA-Compatible" content="IE=edge" >
<meta name="viewport" content="width=device-width, initial-scale=1" >
<meta name="renderer" content="webkit" >
	<title>sss</title>
	<link rel="stylesheet" type="text/css" href="/module/edo/zui/css/zui.css" ></link>

	<script src="/module/edo/zui/jquery-1.11.0.min.js" type="text/javascript"></script>
	<script src="/module/edo/zui/js/zui.js" type="text/javascript"></script>

<style>html,body{background-color: #036;}
#container{margin: 10% auto 0 auto;}
#login-panel{margin: 0 auto;width: 540px;min-height: 280px;background-color: #fff;border: 1px solid #dfdfdf;-moz-border-radius:3px; -webkit-border-radius:3px; border-radius:3px;-moz-box-shadow:0px 0px 30px rgba(0,0,0,0.75); -webkit-box-shadow:0px 0px 30px rgba(0,0,0,0.75); box-shadow:0px 0px 30px rgba(0,0,0,0.75);}
#login-panel .panel-head{min-height: 70px;background-color: #edf3fe;border-bottom: 1px solid #dfdfdf;position: relative;}
#login-panel .panel-head h3{margin: 0 0 0 20px;padding: 0;line-height: 70px;}
#login-panel .panel-head .nav{float: right;position: absolute;right: 0;top: 0;padding: 0;}
#login-panel .panel-head .nav .btn{clear: both;display: block;margin: 10px 10px 0 0;position: absolute;top: 6px;right: 6px;border-color: #ccc;}
#login-panel #mobile{height: 28px;}
#login-panel #mobile i{margin: 0;padding: 0;}
#login-panel #lang{right: 60px;padding: 8px;width: auto;word-spacing:nowrap; overflow:visible;min-width: 80px;text-align: center;}
#login-panel .panel-content{padding-left: 150px;}
#login-panel .panel-content table{border: none;width: 300px;margin: 20px auto;}
#login-panel .panel-content table td{padding: 6px;}
#login-panel .panel-content table td.attr{font-weight: bold;text-align: right;vertical-align: middle;}
#login-panel .panel-content input.text-2{width: 212px;}
#login-panel .panel-content .button-s{width: 80px;}
#login-panel .panel-content .button-c{width: 88px;margin-right: 0;}
#login-panel .panel-foot{text-align: center;padding: 15px;line-height: 2em;background-color: #e5e5e5;border-top: 1px solid #dfdfdf;}
.droppanel{display: none;margin: 0;padding: 4px 0;position: absolute;left: 0;top: 55px;background-color: #fff;-moz-box-shadow:0px 2px 6px rgba(0,0,0,0.5); -webkit-box-shadow:0px 2px 6px rgba(0,0,0,0.5); box-shadow:0px 2px 6px rgba(0,0,0,0.5);z-index: 9999;}
#langs{left: -166px;}
#qrcode{left: -310px;padding: 0;}
#qrcode h4{background-color: #e5e5e5;line-height: 2em;padding:5px 15px;}
.droppanel li{list-style: none;min-width: 120px;font-size: 14px;}
.droppanel li a{display: block;padding: 8px 15px;border-bottom: 1px dashed #eee;}
.droppanel li a:hover{background-color: #e5e5e5;color: #333;}
.droppanel li a.active{background-color: #FAFFBD;}
#poweredby{float: none; color: #eee;text-align: center;margin-top: 30px;}
#poweredby a{color: #fff;}
</style>

</head>
<body>
	
<div id="container">
  <div id="login-panel">
    <div class="panel-head">
      <h3>欢迎使用</h3>
    </div>
    <div class="panel-content" id="login-form">
		<h3></h3>
      <form class="form-horizontal" role="form" action="/bopmain/mainpage/logon" method="post">
		<div class="form-group">
            <label class="col-md-2 control-label">用户名</label>
            <div class="col-md-4">
               <input class='text-2' type='text' name="userName" id="userName" class='form-control' />
            </div>
          </div>

          <div class="form-group">
            <label class="col-md-2 control-label">密码</label>
            <div class="col-md-4">
              <div class="input-group">
                <input class='text-2' type='password' name="password" id="password" class='form-control' />
				<div class="help-block alert alert-warning"><%= request.getAttribute("error") == null ? "" : request.getAttribute("error") %></div>
              </div>
            </div>
          </div>
		  <div class="form-group">
            <div class="col-md-offset-2 col-md-10">
               <input type='submit' id='submit' value='登录' class='btn btn-primary' />
            </div>
          </div>
      </form>
    </div>      
  </div>
</div>

<script laguage='Javascript'>
$(document).ready(function() {
    $('#userName').focus();
})
</script>
</body>
</html>