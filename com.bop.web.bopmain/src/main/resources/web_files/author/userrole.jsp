<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="com.bop.web.PathUtil" %>

<html>
<head>
	<title>权限管理</title>
	<meta name="decorator" content="extwithbdsoft" >
</head>
<body>


<script type="text/javascript" src="/opm/js/SelOrgPerson.js"></script>
<script>
Ext.onReady(function() {
	Ext.namespace("com.bop");

	com.bop.UserFunctionPanel = Ext.extend(Ext.Panel, {
		autoScroll: true,
		item: [],
		
		initComponent: function() {
			com.bop.UserFunctionPanel.superclass.initComponent.call(this);
		},
		
		loadData : function(uid) {
			var me = this;
			Ext.MessageBox.show({
	         	msg : '用户功能加载中，请稍候...',
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
				url: '/Module/Author/AuthorAjax.cmd?op=getuserrole&id=' + uid,
				success: function(data) {
					Ext.MessageBox.hide();
					me.body.update(data);
					me.doLayout();
				}
			});
		}
	});

	var roleFunctionPanel = new com.bop.UserFunctionPanel({
		title: '用户功能权限',
		region: 'center',
		body: '',
		tbar: ['请选择要查看的人员：', {
			xtype: 'personfield',
			listeners: {
				change: function(me, ov, nv) {
					if(nv[0]==undefined) return;
					roleFunctionPanel.loadData(nv[0].record.get("A0102"));
				}
			}
		}]
	});
	
	var panel = new Ext.Panel({
       	renderTo: 'mydiv',
       	layout: 'border',
       	items: [roleFunctionPanel],
       	plugins: [new Ext.ux.plugins.FitToParent()]
    });
});
</script>
<div id="mydiv" style="height:100%;width:100%;"></div>
</body>
</html>