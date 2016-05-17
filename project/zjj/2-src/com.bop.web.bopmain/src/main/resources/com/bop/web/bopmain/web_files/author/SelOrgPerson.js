Ext.namespace("Beidasoft.Semip");
var sysroleid = "";
var roleSqlWhere = "";
sysrole.on('click', function(nd) {
	sysroleid = nd.id;
	roleSqlWhere = " and A0102 not in (select AU0201 from AU02 where PARENTID='"+sysroleid+"')"
});
deptSearch = new Ext.form.TextField({
	anchor : '98%',
    emptyText:'输入部门名称查询',
    enableKeyEvents: true,
    listeners: {
        keyup : function(field, e) {
        	Beidasoft.Semip.Department.expandAll();
        	var value = Ext.escapeRe(field.getValue());
            var root = Beidasoft.Semip.Department.root;
            var nodes = root.childNodes;
            findTreeNodes(nodes, value);
        },
        scope : this
    }
});
var findTreeNodes = function(nodes, text) {
	if(nodes == null) return;
	var parentShow = false;
	for(var i = 0, l = nodes.length; i < l; i++) {
    	var nd = nodes[i];
    	if(nd == undefined) return;
    	if(nd.hasChildNodes()) {
    		var isShow = findTreeNodes(nd.childNodes, text);
    		if(isShow) nd.ui.show();
    		else if(nd.text.indexOf(text) != -1) {nd.ui.show();isShow = true;}
    		else nd.ui.hide();
    		parentShow = isShow;
    	}
    	else if(nd.text.indexOf(text) == -1) nd.ui.hide();
    	else {nd.ui.show();parentShow = true;}
    }
	return parentShow;
}

Beidasoft.Semip.Department = new Beidasoft.Bap.TreePanel({
    region : 'west',
    title : '部门列表',
    root : '部门',
    layout: 'fit',
    serviceUrl : 'orgService/getMulDeptTree',
    collapsible : true, 
    split : true,
    width : 150,
    border : false,
    minSize : 100,
    maxSize : 200,
    autoScroll : true
});

Beidasoft.Semip.Department.on('click', function(nd, e) {
	var depth = nd.getDepth();
	switch (depth) {
		case 0 :
			if(this.sqlWhere){
				Beidasoft.Semip.PersonList.loadObjectData('1=1 and '+this.sqlWhere + roleSqlWhere);
			}else{
				Beidasoft.Semip.PersonList.loadObjectData('1=1' + roleSqlWhere);
			}
			break;
		default :
			if(this.sqlWhere){
				Beidasoft.Semip.PersonList.loadObjectData('PARENTID=\'' + nd.id + '\' and '+this.sqlWhere + roleSqlWhere);
			}else{
				Beidasoft.Semip.PersonList.loadObjectData('PARENTID=\'' + nd.id + '\'' + roleSqlWhere);
			}
            break;
	}
});

// simple array store
var querystore = new Ext.data.ArrayStore({
    fields: ['colName', 'displayName'],
    data : [['A0103','员工姓名'],['A0102','登录名'],['A0108','单位名'],['A0113','人员密级']]
});

var rightClick1 = new Ext.menu.Menu({
	id : 'rightClick1',
	items : 
	[{
		id : 'insertFields1',
		text : '添加',
		handler : addSelPerson
	}]
});

function addSelPerson() {
	var store = Beidasoft.Semip.PersonSelected.getStore();
	var item = Beidasoft.Semip.PersonList.getSelectionModel().getSelections();
	
	var flag = false;
	
	for (var i = 0; i < item.length; i = i + 1) {
		var record = item[i];
		
		for (var j = 0; j < store.getCount(); j = j + 1) {
			if (record.id === store.getAt(j).id) {
				flag = true;
			}
		}
		
		if (!flag) {
			store.add([record]);
		}
		
		flag = false; // 重置
	}
}

function isEnglish(s){
    s = s.replace(/\s/g,"");
    var p = /^[A-Za-z]+$/;
    return p.test(s);
}
function isChinese(s){
	 s = s.replace(/\s/g,"");
    var p = /^[\u0391-\uFFE5]+$/;
    return p.test(s);
}
Beidasoft.Semip.PersonList = new Beidasoft.Bap.ObjectGridPanel({
	//checkBoxType : 2, //避免引起歧义，去掉了复选框 2011-11-24 liwei
    region: 'center',
    title: '人员列表',
    objectName : 'A01',
    objectCriteria : '',
    objectColumns : 'A0103,A0102,A0108,A0107,A0113',
    orderby : 'A0102',	//通过登录名来排序
    autoLoadData : false,
    havePagingToolbar : true,	//modify 2011-8-27 启用分页功能
    pageSize:10,
    queryCondition : '',
    tbar:
    ['查询：',
     searchContent = new Ext.form.TextField({
        width: 180,
        emptyText:'输入查询内容',
        id:'searchContent_1',
        enableKeyEvents: true,
        listeners: {
            keyup : function(tf, e) {
//            	if(e.keyCode==13){
        			var sqlWhere = Beidasoft.Semip.PersonList.sqlWhere;
            		var projectID = Beidasoft.Semip.PersonList.projectID;
            		var whereStr = Ext.escapeRe(tf.getValue());
                    if (Beidasoft.Semip.PersonList.queryCondition) {
                    		if(sqlWhere){
                    			//姓名筛选
                				Beidasoft.Semip.PersonList.loadObjectData("( replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%') and "+sqlWhere+ roleSqlWhere);
                    		}else{
                    			//姓名筛选
                				Beidasoft.Semip.PersonList.loadObjectData("replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or  replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%'" + roleSqlWhere);
                    		}
                    } else {
                    	
                    	if(sqlWhere){
                    		//登录名筛选
                			if(isEnglish(tf.getValue())){
                				Beidasoft.Semip.PersonList.loadObjectData("(A0102 like '%"+whereStr.toUpperCase()+"%' or A0102 like '%"+whereStr.toLowerCase()+"%') and "+sqlWhere+ roleSqlWhere);
                			}else if(isChinese(tf.getValue())){
                				Beidasoft.Semip.PersonList.loadObjectData("(replace(A0103,' ','') like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace(A0103,' ','') like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%') and "+sqlWhere+ roleSqlWhere);
                			}
                    	}else{
                    		//登录名筛选
                			if(isEnglish(tf.getValue())){
                				Beidasoft.Semip.PersonList.loadObjectData("A0102 like '%"+whereStr.toUpperCase()+"%' or A0102 like '%"+whereStr.toLowerCase()+"%'"+ roleSqlWhere);
                			}else if(isChinese(tf.getValue())){
                				Beidasoft.Semip.PersonList.loadObjectData("replace(A0103,' ','') like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace(A0103,' ','') like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%'"+ roleSqlWhere);
                			}
                    	}
                    }
//            	}
            },
            scope: this
        }
    }),
    '查询条件：',
    new Ext.form.ComboBox({
	    store: querystore,
	    width: 180,
	    displayField:'displayName',
	    typeAhead: true,
	    mode: 'local',
	    triggerAction: 'all',
	    emptyText:'查询条件(默认是登录名)...',
	    selectOnFocus:true,
	    listeners:{
            select : function(cb, rcd, index) {
                Beidasoft.Semip.PersonList.queryCondition=querystore.getAt(index).get('colName');
            },
            blur : function(cb) {
                if (cb.getValue() === '') {
                    Beidasoft.Semip.PersonList.queryCondition='A0102';
                }
            },
            scope: this
	    }
	}),{
    	text:'查询',
    	iconCls:'table_find',
    	handler:function(){
    		var sqlWhere = Beidasoft.Semip.PersonList.sqlWhere;
    		var projectID = Beidasoft.Semip.PersonList.projectID;
    		var whereStr = Ext.escapeRe(Ext.getCmp('searchContent_1').getValue());
    		if (Beidasoft.Semip.PersonList.queryCondition) {
	     		if(sqlWhere){
	     			//姓名筛选
	 				Beidasoft.Semip.PersonList.loadObjectData("( replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%') and "+sqlWhere+ roleSqlWhere);
	     		}else{
	     			//姓名筛选
	 				Beidasoft.Semip.PersonList.loadObjectData("replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or  replace("+Beidasoft.Semip.PersonList.queryCondition+",' ','')  like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%'" + roleSqlWhere);
	     		}
    		} else {
	     	
	         	if(sqlWhere){
	         		//登录名筛选
	     			if(isEnglish(Ext.getCmp('searchContent_1').getValue())){
	     				Beidasoft.Semip.PersonList.loadObjectData("(A0102 like '%"+whereStr.toUpperCase()+"%' or A0102 like '%"+whereStr.toLowerCase()+"%') and "+sqlWhere+ roleSqlWhere);
	     			}else if(isChinese(Ext.getCmp('searchContent_1').getValue())){
	     				Beidasoft.Semip.PersonList.loadObjectData("(replace(A0103,' ','') like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace(A0103,' ','') like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%') and "+sqlWhere+ roleSqlWhere);
	     			}
	         	}else{
	         		//登录名筛选
	     			if(isEnglish(Ext.getCmp('searchContent_1').getValue())){
	     				Beidasoft.Semip.PersonList.loadObjectData("A0102 like '%"+whereStr.toUpperCase()+"%' or A0102 like '%"+whereStr.toLowerCase()+"%'"+ roleSqlWhere);
	     			}else if(isChinese(Ext.getCmp('searchContent_1').getValue())){
	     				Beidasoft.Semip.PersonList.loadObjectData("replace(A0103,' ','') like '%"+whereStr.toUpperCase().replace(/\s/g,"")+"%' or replace(A0103,' ','') like '%"+whereStr.toLowerCase().replace(/\s/g,"")+"%'"+ roleSqlWhere);
	     			}
	         	}
    		}
    	}
    }],
    listeners: {
		rowdblclick: function(grid, rowIndex, e) {
			var record = grid.getSelected().row;
			//加入判断 选完一个人后还双击这个人 
			var store = Beidasoft.Semip.PersonSelected.getStore();
			if(store.getAt(0) != undefined){
				for(var i=0;i<store.getCount();i++){
					if(record.id == store.getAt(i).id){
						return;
					}
				}
			}
			store.add([record]);
		},
		contextmenu: function(event) {
			event.preventDefault(); // 这行是必须的，使用preventDefault方法可防止浏览器的默认事件操作发生。
			
			if (Beidasoft.Semip.PersonList.getSelectionModel().getSelections().length > 0) {
				rightClick1.showAt(event.getXY()); // 取得鼠标点击坐标，展示菜单
			}
		}
	}
});

Beidasoft.Semip.PersonSelected = new Beidasoft.Bap.ObjectGridPanel({
    region : 'south',
    height : 140, 
    title : '选择的人员列表',
    objectName : 'A01',
    objectCriteria : '1=0',
    objectColumns : 'A0103,A0102,A0108,A0107,A0113',
    autoLoadData : false,
    havePagingToolbar : false,
    split : true,
    listeners: {
		rowdblclick: function(grid, rowIndex, e) {
			var record = grid.getSelected().row;
			var store = Beidasoft.Semip.PersonSelected.getStore();
			store.remove(record);
		}
	}
});

Beidasoft.Semip.PersonPanel = new Ext.Panel({
	id : "personpanel_picker",
	height : 300,
	width : 720,
    items: 
    [Beidasoft.Semip.Department,{
   		region:'center',
   		layout:'border',
       	items: [Beidasoft.Semip.PersonList, Beidasoft.Semip.PersonSelected]
	}],
    layout: 'border',
    border: false,
    listeners: {
        render : function(bsp) {
            Beidasoft.Semip.PersonSelected.loadObjectData("RECORDID=''");
        }
    }
});
