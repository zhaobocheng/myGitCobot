<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Set" %>
<%@ page import="com.bop.module.function.MenuItem" %>
<%@ page import="com.bop.module.function.service.FunctionTree"%>
<%@ page import="com.bop.web.bopmain.internal.NavigationService"%>
<%@ page import="com.bop.json.ExtObjectCollection"%>
<%@ page import="com.bop.web.bopmain.internal.FunctionTreeUtil"%>
<%@ page import="com.bop.web.bopmain.UserSession"%>
<%@ page import="com.bop.web.bopmain.internal.UserSessionImpl"%>

<html>
<head>
<meta name="decorator" content="miniui">
<title>首页</title>
<link rel="stylesheet" type="text/css" href="/theme/default/index.css"></link>
<script>
<%
UserSession userSession = (UserSession) UserSessionImpl.getMe();
String uid = userSession.getCurrentUserId();
String sid = request.getParameter("sid");
FunctionTree tree = NavigationService.getMe().getSystemFunctionTree(uid);
ExtObjectCollection eoc = FunctionTreeUtil.toJson(tree, sid);
Set<MenuItem> set = tree.getByParentId("");
if(set.size()==1){
	for(MenuItem mi:set){
%>
window.onload =  function(){
	window.location.href = "/bopmain/mainpage/desktop?fid=1105&sid="+"<%=mi.getId()%>";
}
<%}
} %>

</script>
</head>
<body >
	<div class="mini-fit">
		<div class="main-t" >
			<div class="zhanghu">
		      <!--   <p style="color: #000;font-weight: bold;font-size:14px;margin-bottom: 5px;">你好,haidian01</p> -->
		        <p style="font-size: 14px;">上次登录时间：2016.12.19 13:12:21</p>
		    </div>
		    <div class="nav">
		        <div class="nav-l">
		            <img src="/theme/img/qy.png" alt="" style="vertical-align: -7px;margin-left: 10px;"/>
		            企业数据采集情况：总企业数 <a href="" style="color:#ff0c35;font-weight: bold;font-size: 24px;">2312345</a>
		        </div>
		        <div class="nav-r">
		            <img src="/theme/img/ssj.png" alt="" style="vertical-align: -7px;margin-left: 10px;"/>
		            双随机应用（201611月抽查方案）：成功执法企业数 <a href="" style="color:#e58a09;font-weight: bold;font-size: 24px;">8800</a>
		        </div>
		    </div>
		    <div class="back"></div>
		</div>
		
		<div class="main-b">
			<p>首页内容</p>
		</div>
	</div>
	
	<div class="sy_foot" >
	  		  北京北大软件工程股份有限公司  技术支持
	</div>
<script >
mini.parse();
</script>
</body>
</html>