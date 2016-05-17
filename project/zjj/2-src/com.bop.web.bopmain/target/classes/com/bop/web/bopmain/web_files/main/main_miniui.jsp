<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.UUID"%>
<%@ page import="com.bop.common.StringUtility"%>
<%@ page import="com.bop.json.ExtObjectCollection"%>
<%@ page import="com.bop.web.bopmain.UserSession"%>
<%@ page import="com.bop.web.bopmain.internal.UserSessionImpl"%>
<%@ page import="com.bop.web.bopmain.internal.NavigationService"%>
<%@ page import="com.bop.web.bopmain.internal.FunctionTreeUtil"%>
<%@ page import="com.bop.module.function.service.FunctionTree"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.bop.web.PathUtil"%>
<!DOCTYPE >
<html>
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=8" />
<title><sitemesh:write property="title" /></title>

<%
	UserSession userSession = (UserSession) UserSessionImpl.getMe();
	
	String uname = userSession.getCurrentUserName();
	String uid = userSession.getCurrentUserId();
	
	String decorator = request.getParameter("decorator");
	
	Object osid = request.getSession().getAttribute("sid");
	Object ofid = request.getSession().getAttribute("fid");
	String sid = (osid == null ? "" : osid.toString());
	String fid = (ofid == null ? "" : ofid.toString());

	
	String theme = request.getParameter("theme");
	if (theme == null || theme.equals("")) {
		theme = "1";
	}
%>

<link rel="stylesheet" type="text/css" href="/ResourceFiles/theme/default/global.css"></link>
<link rel="stylesheet" type="text/css" href="/ResourceFiles/theme/default/icons.css"></link>

<script>
	// 全局变量
	currentUserId = '<%=uid%>'; //当前用户ID
	currentUserName = '<%=uname%>';//当前用户真实姓名
</script>

<% if("clean".equals(decorator)) {%>

<% } else if("extwithbdsoft".equals(decorator)) { %>
<%=PathUtil.userStyle("bdsoftext3") %>
<% } else { %>
<%=PathUtil.userStyle("miniui3") %>
<% } %>

<link rel="stylesheet" href="/bopmain/ztree/css/zTreeStyle/zTreeStyle.css" type="text/css">
<script type="text/javascript" src="/bopmain/ztree/js/jquery.ztree.core-3.5.js"></script>

<sitemesh:write property="head" />

<%
	FunctionTree tree = NavigationService.getMe().getSystemFunctionTree(uid);
	ExtObjectCollection eoc = FunctionTreeUtil.toJson(tree);
%>

<script>
var zNodes = <%= eoc.toString() %>

var setting = {
	view: {
		showLine: true,
		selectedMulti: false,
		dblClickExpand: false
	},
	data: {
		simpleData: {
			enable: true
		}
	},
	callback: {
		onNodeCreated: this.onNodeCreated,
		beforeClick: this.beforeClick,
		onClick: this.onClick
	}
};

function modifyPwd(userName) {
		mini.open({
			url:'/bopmain/user/changePwd.jsp?theme=2&reset=0&userid=' + currentUserId,
			title:'修改密码',
			height:210,
			width:360,
			ondestroy:function(action){
				if(action == 'ok') mini.showTips("修改成功！");
				else mini.showTips("修改密码失败，可能是输入的旧密码不正确");
			}
		});
	}

if(window.mini) {
	mini.showTipsInner = mini.showTips;
	mini.showTips = function(text) {
		if(typeof text == 'string') {
			mini.showTipsInner({
			    content: text,
			    state: 'default'
			});
		} else {
			mini.showTipsInner(text);
		}
	}
}

<% if (theme.equals("1") || theme.equals("3")) { %>
jQuery(document).ready(function(){
	jQuery.fn.zTree.init(jQuery("#leftTree"), setting, zNodes);
	zTree_Menu = jQuery.fn.zTree.getZTreeObj("leftTree");
	
	var curMenus = zTree_Menu.getNodesByParam("id", '<%= fid %>');
	if(curMenus && curMenus.length > 0) {
		var curMenu = curMenus[0];
		zTree_Menu.selectNode(curMenu);
		//var a = $("#" + zTree_Menu.getNodes()[0].tId + "_a");
		//a.addClass("cur");
	}
});
<% } %>

</script>
<style type="text/css">
.ztree {margin: 0px; padding: 0px; }
.ztree * {font-size:14px;}
.ztree li a.level0 {width:150px;height: 30px; text-align: center;  display:block; background-color: #0B61A4; border:1px silver solid;}
.ztree li a.level0.cur {background-color: #66A3D2; }
.ztree li a.level0 span {display: block; valign: middle; color: white; line-height: 30px; font-weight: bold;}
.ztree li {line-height: 20px;}
.ztree li a {height: 20px;}
.ztree li span {line-height: 20px;}
.ztree li a.level0 span.button { float:right; margin-left: 10px; visibility: visible;display:none;}
.ztree li span.button.switch.level0 { display:none; }
</style>

</head>
<body>
	<%if (!theme.equals("2")) {%>
	<div class="header">
		<table style="height: 100%; width: 100%">
			<tr style="height: 100%; width: 100%">
				<td class="text" nowrap align="left">
					<FONT style="FONT-WEIGHT: normal; FONT-SIZE: 20px; LINE-HEIGHT: normal;
						 FONT-STYLE: normal; FONT-VARIANT: normal" face=楷体_gb2312 color=#FFFFFF>
						 <B>北京质监局双随机应用系统</B>
					</FONT>
				</td>
				<td class="text" nowrap align="right" style="">
				<span>	
					<a class="logonUserCss" href="#"
						onclick="javascript:modifyPwd(currentUserName);"> <%=uname%>
					</a>
				</span> <span>|&nbsp;<a class="exitCss" href="<%= PathUtil.getLogoutUrl() %>">退出</a></span>
				</td>
			</tr>
		</table>
	</div>
	<div class="maincontainer">
	<%} else {%>
	<div class="maincontainer_notop">
	<% } %>
	<% if (theme.equals("1") || theme.equals("3")) {%>
	<div class="leftcontainer">
		<div id="leftTree" class="ztree"></div>
	</div>
	<div class="protal_main_div">
		<sitemesh:write property="body" />
	</div>
	<%} else {%>
	<div class="protal_main_div_noleft">
		<sitemesh:write property="body" />
	</div>
	<% } %>
	</div>
</body>

</html>
