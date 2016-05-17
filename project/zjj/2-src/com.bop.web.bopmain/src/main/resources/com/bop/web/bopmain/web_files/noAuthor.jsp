<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" %>
<%@ page import="com.bop.web.bopmain.internal.VerifyLicenseFilter" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>提示信息</title>
</head>
<body>
<%
	String info = VerifyLicenseFilter.message;
	if(info.equals("")) info = "非授权系统，如有需要请与北大软件联系！";
%>
<script language="Javascript" type="text/javascript">
	var info = '<%=info%>';
	alert(info);
	window.close();
</script>
</body>
</html>