Ext.namespace("com.bop");
var btnAddUser = new Ext.Button({
	text: '用户授权',
	iconCls: 'table_add',
   	handler: function() {
   		if(sysroleid === ""){
   			Beidasoft.Bap.alert('请选择角色');
    		return;
   		}
   		Beidasoft.Semip.PersonList.loadObjectData("parentid in(select B00 from A01 where recordid = '"+currentOrgUserId +"')" + roleSqlWhere);
		winAdd = null;
   		if (!winAdd) {
	        winAdd = new Ext.Window({
	        	title:'授权部门中的人员',
		    	items: [Beidasoft.Semip.PersonPanel],
	            layout:'fit',
	            width: 700,
		        height: 550,
	            buttonAlign: 'right',
	            closeAction:'hide',
	            autoScroll:true,
	            modal:true,
	            buttons: [{
	                text: '确定',
	                iconCls: 'btn_ok',
	                handler: function() {
	                	var store = Beidasoft.Semip.PersonSelected.getStore();
	                	var selecteds = "";
	                	store.each(function(record) {
	                		selecteds += record.data.A0102+",";
	                	});
	                	if (selecteds === "") {
	                		Beidasoft.Bap.alert( '请选择人员');
	                		return;
	                	}
	                	toRole(sysroleid, selecteds);
	                	Beidasoft.Semip.PersonList.getStore().removeAll();
				        Beidasoft.Semip.PersonSelected.getStore().removeAll();
	                	winAdd.hide();   
	                }
	            }, {
	                text: '取消',
	                iconCls: 'btn_cancel',
	                handler: function() {
	                	Beidasoft.Semip.PersonList.getStore().removeAll();
				        Beidasoft.Semip.PersonSelected.getStore().removeAll();
	                    winAdd.hide();   
	            	}
	            }]
	        });
        }
        winAdd.show();
   	}
});
com.bop.RoleUserPanel = Ext.extend(Ext.Panel, {
	autoScroll : false,
	item : [],
	layout : 'border',
	left : null,
	right : null,
	center : null,
	addOneButton : null,
	addAllButton : null,
	removeOneButton : null,
	removeOneButton : null,
	rid : null,
	roleWhereStr : null,
	unRoleWhereStr : null,
	initComponent : function() {
		this.left = new Beidasoft.Bap.GridPanel({
			border : false,
			autoScroll:false,
			autoLoadData : false,
		    havePagingToolbar : true,
		    useDWR: false,
		    pageSize:30,
			title : '已授权用户',
			region : 'east',
			border : true,
			width : '50%',
			tbar:['查询：', searchContent_role = new Ext.form.TextField({
		        width: 150,
		        emptyText:'输入查询内容',
		        enableKeyEvents: true,
		        id:'searchContent_role',
		        listeners: {
		            keyup : function(tf, e) {
		            	this.roleWhereStr = tf.getValue();
		            	if(this.roleWhereStr == '')           	
		            		this.left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + this.rid + '?theme=none');
		            	else
		            		this.left.loadData('/bopmain/author/AuthorAjaxCommand/getSearchRoleUsers/' + this.rid + '/' + this.roleWhereStr + '?theme=none');
		            },
		            scope: this
		        }
		    })]
		})

		this.right = new Beidasoft.Bap.GridPanel({
			border : false,
			autoScroll:false,
			autoLoadData : false,
		    havePagingToolbar : true,
		    useDWR: false,
		    pageSize:30,
			title : '未授权用户',
			border : true,
			region : 'west',
			width : '45%',
			tbar:['查询：', searchContent_unrole = new Ext.form.TextField({
			        width: 150,
			        emptyText:'输入查询内容',
			        enableKeyEvents: true,
			        id:'searchContent_unrole',
			        listeners: {
			            keyup : function(tf, e) {
			            	this.unRoleWhereStr = tf.getValue();
			            	if(this.unRoleWhereStr == '')           	
			            		this.right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + this.rid + '?theme=none');
			            	else
			            		this.right.loadData('/bopmain/author/AuthorAjaxCommand/getSearchUser/' + this.rid + '/' + this.unRoleWhereStr + '?theme=none');
			            },
			            scope: this
			        }
			    }),'-',btnAddUser]
		})
		this.addOneButton = new Ext.Button({
			xtype : "button",
			handler : this.addOne,
			scope : this,
			tooltip : "授权选中的",
			icon : "/module/author/images/page-next.gif",
			cls : "x-btn-icon"
		});
		this.addAllButton = new Ext.Button({
			xtype : "button",
			handler : this.addAll,
			scope : this,
			tooltip : "全部授权",
			icon : "/module/author/images/page-last.gif",
			cls : "x-btn-icon"
		});
		this.removeOneButton = new Ext.Button({
			xtype : "button",
			handler : this.removeOne,
			scope : this,
			tooltip : "取消选中的",
			icon : "/module/author/images/page-prev.gif",
			cls : "x-btn-icon"
		});
		this.removeAllButton = new Ext.Button({
			xtype : "button",
			handler : this.removeAll,
			scope : this,
			tooltip : "全部取消",
			icon : "/module/author/images/page-first.gif",
			cls : "x-btn-icon"
		});
		this.center = new Ext.Panel({
			region : 'center',
			layout:"table",
			width: 50,
			bodyStyle:'border-style: none; padding: 10px',
			defaults:{
		            style:"margin-bottom: 10px"
		        },
		    layoutConfig:{
		            columns:1
		        },
	        items: [this.addOneButton,this.addAllButton,this.removeOneButton,this.removeAllButton]

		})

		this.items = [ this.left, this.center, this.right ];
		com.bop.RoleUserPanel.superclass.initComponent.call(this);
	},

	loadData : function(rid) {
		this.left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + rid + '?theme=none');
		this.right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + rid + '?theme=none');
		this.rid = rid;
	},
	addOne:function(){
        moveItems(this.rid,this.right,this.left,'toleft')
    },
    addAll:function(){
    	var url = "";
    	var me = this;
    	if(this.unRoleWhereStr == null) {
    		url = '/bopmain/authorajax.cmd?op=setusers&id=' + me.rid + '&filter=none&check=true';
		} else {
    		url = '/bopmain/authorajax.cmd?op=setusers&id=' + me.rid + '&filter=' + encodeURIComponent(this.unRoleWhereStr) + '&check=true';
		}
        Ext.MessageBox.show({
        	msg : '授权中，请稍候...',
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
			url: url,
			timeout: 300000,
			success: function() {
				Ext.MessageBox.hide();
				this.left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + me.rid + '?theme=none');
				this.right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + me.rid + '?theme=none');
			}
		});
    },
    removeOne:function(){
    	moveItems(this.rid,this.left,this.right,'toright')
    },
    removeAll:function(){
    	var url = "";
    	var me = this;
    	if(this.roleWhereStr == null) {
    		url = '/bopmain/authorajax.cmd?op=setusers&id=' + me.rid + '&filter=none&check=flase';
		} else {
    		url = '/bopmain/authorajax.cmd?op=setusers&id=' + me.rid + '&filter=' + encodeURIComponent(this.roleWhereStr) + '&check=flase';
		}
    	Ext.MessageBox.show({
         	msg : '取消授权中，请稍候...',
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
			url: url,
			timeout: 300000,
			success: function() {
				Ext.MessageBox.hide();
				this.left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + me.rid + '?theme=none');
				this.right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + me.rid + '?theme=none');
			}
		});
    }
});

roleUserPanel = new com.bop.RoleUserPanel({
	title : '角色对应的用户',
	id:'test',
	body : ''
});

sysrole.on('click', function(nd) {
	if (nd == null)
		return;
	roleUserPanel.loadData(nd.id);
	//显示
	authorPanel.unhideTabStripItem(1);
});

Ext.QuickTips.init();
var left = roleUserPanel.left;
var right = roleUserPanel.right;
//点击表头排序 
left.on('headerclick', function(left, col, e){
	left.store.sort(left.getColumnModel().getDataIndex(col));
});
right.on('headerclick', function(right, col, e){
	right.store.sort(right.getColumnModel().getDataIndex(col));
});

left.on('dblclick', function(e){
	roleUserPanel.removeOne();
});
right.on('dblclick', function(e){
	roleUserPanel.addOne();
});
function moveItems(rid, menu, allMenu, way) {
	if(menu.getSelected()!=null){
		var sm = menu.getSelectionModel().getSelections();
		var l = menu.getStore();
		var r = allMenu.getStore();
		var str = "";
		for(i=0;i<sm.length;i++){
			if(sm[i].id=="beidasoft")
				continue;
			str += sm[i].id + ",";
		}
		if(str=="") return;
		if(way=='toright'){
			Ext.MessageBox.show({
	         	msg : '取消授权中，请稍候...',
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
				url: '/bopmain/authorajax.cmd?op=setuser&id='
						+ rid + '&uid=' + str + '&check=flase',
				success: function() {
					Ext.MessageBox.hide();
					left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + rid + '?theme=none');
					right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + rid + '?theme=none');
				}
			});
		}
		if(way=='toleft'){
			Ext.MessageBox.show({
	        	msg : '授权中，请稍候...',
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
				url: '/bopmain/authorajax.cmd?op=setuser&id='
						+ rid + '&uid=' + str + '&check=true',
				success: function() {
					Ext.MessageBox.hide();
					left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + rid + '?theme=none');
					right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + rid + '?theme=none');
				}
			});
		}
	}
}

function toRole(rid, selecteds) {
	Ext.MessageBox.show({
    	msg : '授权中，请稍候...',
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
		url: '/bopmain/authorajax.cmd?op=setuser&id='
				+ rid + '&uid=' + selecteds + '&check=true',
		success: function() {
			Ext.MessageBox.hide();
			left.loadData('/bopmain/author/AuthorAjaxCommand/getRoleUsers/' + rid + '?theme=none');
			right.loadData('/bopmain/author/AuthorAjaxCommand/getUsersForRole/' + rid + '?theme=none');
		}
	});
}