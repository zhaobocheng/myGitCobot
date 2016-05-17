<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="com.bop.web.PathUtil" %>

<html>
<head>
  <title>权限管理</title>
</head>
<body>

<!-- Ext Resources -->
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/ext-all.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/ext-lang-zh_CN.js"></script>
	<link type="text/css" href="/module/ext3/ext-3.2.0/resources/css/ext-all.css" rel="stylesheet"/>
	<link rel="stylesheet" type="text/css" href="/module/ext3/ext-3.2.0/resources/css/xtheme-gray.css" />
	
	<!-- Ext ux 目标是只引用两个-all文件，但出现的问题还没有解决办法 -->
	<script src="/module/ext3/ext-3.2.0/examples/ux/fileuploadfield/FileUploadField.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/ItemSelector.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/MultiSelect.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/CheckColumn.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/RowExpander.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/RowEditor.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/Spinner.js" type="text/javascript"></script>
	<script src="/module/ext3/ext-3.2.0/examples/ux/SpinnerField.js" type="text/javascript"></script>
	
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGridSorter.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGridColumnResizer.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGridNodeUI.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGridLoader.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGridColumns.js"></script>
	<script type="text/javascript" src="/module/ext3/ext-3.2.0/examples/ux/treegrid/TreeGrid.js"></script>
	      
	<link rel="stylesheet" type="text/css" href="/module/ext3/ext-3.2.0/examples/ux/css/MultiSelect.css" />
	<link rel="stylesheet" type="text/css" href="/module/ext3/ext-3.2.0/examples/ux/css/ColumnNodeUI.css" />
	<link rel="stylesheet" type="text/css" href="/module/ext3/ext-3.2.0/examples/ux/css/RowEditor.css" />
	<link rel="stylesheet" type="text/css" href="/module/ext3/ext-3.2.0/examples/ux/css/Spinner.css" />
	<link type="text/css" href="/module/ext3/ext-3.2.0/examples/ux/treegrid/treegrid.css" rel="stylesheet" />
	
	
	<!-- beidasoft common js -->
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Ext.ux.plugins.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/RadioboxSelectionModel.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.Ajax.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.Fields.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.FormPanel.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.TablePanel.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.FormPanel2.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.FormPanel3.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.TreePanel.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.GridPanel.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.Layout.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/ColumnNodeUI.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/TreeCheckNodeUI.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/popalert.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/TreeGrid.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Semip.Common.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/Beidasoft.Bap.CustomFields.js") %>
	<%=PathUtil.jsLink("com.bop.module.ext3", "/ext-bdsoft/ext-basex.js") %>
	<%=PathUtil.jsLink("com.bop.module.user", "/password.js") %>
	
	<script>
		Ext.BLANK_IMAGE_URL = '/module/ext3/ext-3.2.0/resources/images/default/s.gif';
	</script>
	
<script>
var id = '<%=request.getParameter("id")%>';

Ext.onReady(function() {
	Ext.namespace("com.bop");

	com.bop.RoleFunctionPanel = Ext.extend(Ext.Panel, {
		autoScroll: true,
		item: [],
		
		initComponent: function() {
			com.bop.RoleFunctionPanel.superclass.initComponent.call(this);
		},
		
		loadData : function(rid) {
			var me = this;
			Ext.MessageBox.show({
	         	msg : '角色功能加载中，请稍候...',
	         	width : 300,
	         	wait : true,
	         	progress : true,
	         	closable : true,
	         	waitConfig : {
	         	 interval : 200
	         	},
	         	 icon : Ext.Msg.INFO
	     	});
			Beidasoft.Bap.Ajax.request({
				url: '/Module/Author/AuthorAjax.cmd?op=get&id=' + rid,
				success: function(data) {
					Ext.MessageBox.hide();
					me.body.update(data);
					me.doLayout();
				}
			});
		}
	});
	
	var roleFunctionPanel = new com.bop.RoleFunctionPanel({
		title: '角色功能权限',
		region: 'center',
		body: ''
	});
	
	var panel = new Ext.Panel({
       	renderTo: 'mydiv',
       	layout: 'border',
       	items: [roleFunctionPanel],
       	plugins: [new Ext.ux.plugins.FitToParent()]
    });
	
	roleFunctionPanel.loadData(id);
});
</script>
<div id="mydiv" style="height:100%;width:100%;"></div>
</body>
</html>