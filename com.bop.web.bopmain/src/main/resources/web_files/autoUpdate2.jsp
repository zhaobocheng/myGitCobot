<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.bop.web.PathUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>初始化/升级数据结构</title>
	<meta http-equiv="content-type" content="text/html;charset=UTF-8">
	<script type="text/javascript" src="/module/ext3/ext-bdsoft/jquery-1.6.2.min.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" src="/module/ext3/ext-3.2.0/ext-all.js"></script>
    <script type="text/javascript" src="/module/ext3/ext-3.2.0/ext-lang-zh_CN.js"></script>
    <script type="text/javascript" src="/module/ext3/ext-bdsoft/Beidasoft.Bap.Ajax.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-bdsoft/Beidasoft.Bap.GridPanel.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-bdsoft/Beidasoft.Bap.Common.js"></script>
    <script type="text/javascript" src="/module/ext3/ext-bdsoft/Beidasoft.bopmain.Common.js"></script>
    <link type="text/css" href="/module/ext3/ext-3.2.0/resources/css/ext-all.css" rel="stylesheet"/>
    
    <script>
    	var logonUrl = '<%= PathUtil.getLogonUrl() %>';
    </script>
    
    <script type="text/javascript" src="AutoUpdate2.js"></script>
</head>
<body>
	<div id="updateList">
    </div>
</body>
</html>