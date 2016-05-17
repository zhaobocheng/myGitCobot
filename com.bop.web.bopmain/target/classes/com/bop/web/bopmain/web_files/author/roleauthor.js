﻿﻿Ext.namespace("com.bop");

com.bop.RoleFunctionPanel = Ext.extend(Ext.Panel, {
	autoScroll: true,
	item: [],
	
	initComponent: function() {
		com.bop.RoleFunctionPanel.superclass.initComponent.call(this);
	},

	loadData : function(rid) {
		var me = this;
		Beidasoft.Bap.Ajax.request({
			url: '/bopmain/authorajax.cmd?op=get' + '&id=' + rid,
			success: function(data) {
				me.body.update(data);
				me.doLayout();
			}
		});
	}
});

sysrole.on('click', function(nd) {
	if(nd == null) return;
	roleFunctionPanel.loadData(nd.id);
});

var roleFunctionPanel = new com.bop.RoleFunctionPanel({
	title: '角色功能权限',
	body: ''
});
