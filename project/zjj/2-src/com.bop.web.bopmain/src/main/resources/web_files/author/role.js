var sysrole = new Beidasoft.Bap.TreePanel({
    title: '',
    border: false,
    id:'sysrole',
    rootVisible: false,
    url: '/bopmain/roleajax.cmd?op=list&type=1',
    tbar :
    [{
		text : '新增',
		iconCls: 'page_add',
		handler: function() {
			formRole.setTitle('添加系统角色');
			formRole.submitParams = {type: 1};
			formRole.Show();
			formRole.NewData({type: 1});
		}
	}, {
		text: '修改',
		iconCls: 'page_edit',
		handler: function() {
			var nd = sysrole.getSelected();
			
			if (nd === null) {
				Beidasoft.Bap.alert("提示","请选择一个系统角色");
				return;
			}
			
			var ISIN = nd.attributes.isin;
			if (ISIN == 1)
			{
				Beidasoft.Bap.alert("该角色是系统内置角色，不允许修改！");
				return;
			}
			
			ChangeformRole.setTitle('修改系统角色：' + nd.text);
			ChangeformRole.submitParams = {type: 1};
			ChangeformRole.Show();
			ChangeformRole.LoadData(nd.id);
		}
	}, {
		text : '删除',
		iconCls: 'page_delete',
		handler: function() {
			var nd = sysrole.getSelected();
			if (nd === null) {
				Beidasoft.Bap.alert("请选择一个系统角色");
				return;
			}
			var ISIN = nd.attributes.isin;
			if (ISIN == 1)
			{
				Beidasoft.Bap.alert("该角色是系统内置角色，不允许删除！");
				return;
			}
			
			Ext.Msg.confirm('确认删除', '确定删除系统角色' + nd.text + '吗？' , function(id) {
				if (id === 'yes') {
					Beidasoft.Bap.Ajax.request({
						url: '/bopmain/roleajax.cmd?op=delete&id=' + nd.id,
						success: function(data) {
							sysrole.refresh();
			            }
					});
				}
			});
		}
	}]
});

var formRole = new Beidasoft.Bap.FormPanel({
	loaddataUrl: '/bopmain/roleajax.cmd?op=create',
	submitUrl: '/bopmain/roleajax.cmd?op=save',
	height: 200,
	titile: '角色',
	items: 
	[{
		fieldLabel : '角色名称',
		name : 'name',
		id: 'createrolename',
		anchor : '95%',
		xtype: 'textfield',
		allowBlank: false
	}, {
		fieldLabel : '描述',
		name : 'description',
		xtype: 'textfield',
		anchor : '95%',
		allowBlank: false
	},{
		fieldLabel : '排序',
		name : 'sort',
		id:'sort',
		xtype: 'textfield',
		anchor : '95%',
		allowBlank: false,
		maxLength:10,
		regex : /^\d{0,10}$/,
		regexText : '输入的内容只能为正整数'
	}, {
		xtype: 'hidden',
		name: 'type'
	}, {
		xtype: 'hidden',
		name: 'pindex'
	}]
});

formRole.on('submit', function(action) {
	var data = eval('(' + action.response.responseText + ')');
	if (data.msg !== '') {
		Beidasoft.Bap.alert("提示", data.msg);
	}
	sysrole.refresh();
});
//add by sjw 测试部提出过滤重复角色名称 2011/10/16
formRole.on('beforesubmit', function() {
	var params = this.submitParams;
	var roleName = Ext.getCmp('createrolename').getValue() || '';
	var tree = sysrole;
	
	//检查是否存在重复角色名称
	var childNodes = tree.root.childNodes || [];
	var isExist = false;
	Ext.each(childNodes, function(node) {
		if(node.text.trim() == roleName.trim()) isExist = true;
	});
	if(isExist) {
		Beidasoft.Bap.alert('提示', '角色列表中已经存在【'+roleName+'】角色');
		return false;
	}
	return true;
});

String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}

var ChangeformRole = new Beidasoft.Bap.FormPanel({
	loaddataUrl: '/bopmain/roleajax.cmd?op=get',
	submitUrl: '/bopmain/roleajax.cmd?op=save',
	height: 200,
	titile: '修改角色',
	items: 
	[{
		fieldLabel : '角色名称',
		name : 'name',
		id: 'modifyrolename',
		anchor : '95%',
		xtype: 'textfield',
		allowBlank: false
	}, {
		fieldLabel : '描述',
		name : 'description',
		xtype: 'textfield',
		anchor : '95%',
		allowBlank: false
	},{
		fieldLabel : '排序',
		name : 'sort',
		xtype: 'textfield',
		anchor : '95%',
		allowBlank: false,
		maxLength:10,
		regex : /^\d{0,10}$/,
		regexText : '输入的内容只能为正整数'
	},{
		xtype: 'hidden',
		name: 'type'
	}, {
		xtype: 'hidden',
		name: 'pindex'
	}]
});

ChangeformRole.on('submit', function(action) {
	var data = eval('(' + action.response.responseText + ')');
	if (data.msg !== '') {
		Beidasoft.Bap.alert("提示", data.msg);
	}

	sysrole.refresh();
});
//add by sjw 测试部提出过滤重复角色名称 2011/10/16
ChangeformRole.on('beforesubmit', function() {
	var params = this.submitParams;
	var roleName = Ext.getCmp('modifyrolename').getValue() || '';
	var tree = sysrole;
	var selNode = tree.getSelected();
	//检查是否存在重复角色名称
	var childNodes = tree.root.childNodes || [];
	var isExist = false;
	Ext.each(childNodes, function(node) {
		if(node.text.trim() == roleName.trim() && node.id != selNode.id) isExist = true;
	});
	if(isExist) {
		Beidasoft.Bap.alert('提示', '角色列表中已经存在【'+roleName+'】角色');
		return false;
	}
	return true;
});


Ext.onReady(function() {
	Ext.Window.prototype.hideLoad = function(){
		if(!this.rendered){
			this.render(Ext.getBody());
		}
		if (this.isDestroyed){
			return false;
		}     
		this.proxy.hide();
		this.el.setStyle('display', 'block');
		this.doConstrain();
		this.doLayout();
		if(this.keyMap){
			this.keyMap.enable();
		}
		this.toFront();
		this.updateHandles();
		return this;
	}
});
