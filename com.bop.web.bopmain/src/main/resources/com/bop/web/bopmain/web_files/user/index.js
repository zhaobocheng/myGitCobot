var parentid;
var type;
var newdate = new Date();
var userDeptWin = null;

//查看用户管理的部门
function userDept(uid){
	var userDeptGrid = new Beidasoft.Bap.GridPanel({
       // title: '管理的部门',
        serviceUrl: 'personManageListService/getUserDept/'+uid,
        autoLoadData:true,
        pageSize: 20,
        havePagingToolbar:true
	});
	userDeptWin  = new Ext.Window({
		title: '管理的部门',
		layout : 'fit',
		width : 460,
		height : 550,
		closeAction : 'hide',
		resizable : true,
		modal : true,
		items : [userDeptGrid]
	});
	userDeptWin.show();
};
Ext.onReady(function(){	
	//去除输入的空格
	function removeSpace(realName){
		var re = /\s/g;
		//去除字符串中的空格
		realName = realName.replace(re,"");
		return realName;
	}
	
	function getSearchSql(column, realNameValue) {
		sqlWhere = " "+column+" like '%"+realNameValue.toUpperCase()+"%' or "+ column +" like '%"+realNameValue.toLowerCase()+"%' ";
		
		return sqlWhere;
	}
	
    var bmzwtree = new Beidasoft.Bap.TreePanel({
        region: 'west',
        title: '组织机构',
        rootText:'组织机构',
        layout: 'fit',
        serviceUrl: 'semip_orgService/getMulDeptTree',
        collapsible: true, 
        split: true,
        width: 160,
        border: false,
        minSize: 100,
        maxSize: 200,
        autoScroll:true,
        tbar: 
        [{
           	text: '新增',
           	iconCls: 'page_add',
           	handler: function() {
           		type='add';
           		var nd=bmzwtree.getSelected();
           		if(nd==null){
           			Beidasoft.Bap.alert('请选择一个节点!');
           			return;
           		}
           		var dep=nd.getDepth();
           		if(dep==0)
           			{
           			parentid="00000000-0000-0000-0000-000000000000";
           			}
           		else {
           			parentid=nd.id;
           		}
           		zzjgForm.title='新增部门';
           		zzjgForm.Show();
           		zzjgForm.NewData({"parentid":parentid});
           	}
        },
        {
           	text: '修改',
           	iconCls: 'page_edit',
           	handler: function() {
           		type='update';
           		var nd = bmzwtree.getSelected();
           		if (nd != null){
           			if (nd.getDepth() == 0) {
               			Beidasoft.Bap.alert('请选择一个非根点!');
               			return;
               		}
           			
           			if (nd.getDepth() > 0) {
               			zzjgForm.title='修改组织机构';
                   		zzjgForm.Show();
                   		zzjgForm.LoadData(nd.id);
               		}
           		} else {
           			Beidasoft.Bap.alert('提示','请选择要修改的组织机构！');
    				return ;
           		}
           	}
        },
        {
           	text: '删除',
           	iconCls: 'page_delete',
           	handler: function() {
           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
           		if (selNode == null) {
           			Beidasoft.Bap.alert('请先选择一个树节点!');
           			return;
           		}
           		
           		if (selNode.getDepth() == 0) {
           			Beidasoft.Bap.alert('请选择一个非根点!');
           			return;
           		} else {
           			// 判断是否被项目资源所引用
            		Beidasoft.Bap.Ajax.request({
	                    serviceUrl: 'personManageListService/isDeptOrDeptPersonUsed/' + selNode.id,
	                    success: function(data) {
	                    	if (data === true) {
	                    		Beidasoft.Bap.alert('该部门已有资源被使用!');
	                    		return;
	                    	} else {
	                    		 Ext.MessageBox.confirm('提示框', '将会级联删除用户，是否继续？', function(btn)
            			         {
            			            if (btn == 'yes')
            			            {
            			            	Beidasoft.Bap.Ajax.request(
    	                		        {
    	                		            serviceUrl: 'orgService/deleteDep/' + selNode.id,
    	                		            success: function(data) 
    	                		            {
    	                		            	Beidasoft.Bap.alert("删除成功");
    	                		            	bmzwtree.refreshTree();
    	                		            	gridRY.getStore().removeAll();
    	                						gridJN.getStore().removeAll();
    	                						gridFL.getStore().removeAll();
    	                		            },
    	                		            failure: function(data)
    	                		            {
    	            		                	Beidasoft.Bap.alert("删除失败");
    	                		            }
    	                		        });
            			            }
            			         });
	                    		
//	                    		deleteItem('B01', selNode.id);
	                    	}
	                    }
	                });
           		}
           	}
        }],
        refreshTree: function()
        {
            this.refresh();
        },
        listeners:{
        	'render' : function(){
        		var tbar = new Ext.Toolbar({
        			items : [{
        	           	text: '与AD同步',
        	           	iconCls: 'page_edit',
        	           	handler: function() {
        	           		Ext.MessageBox.confirm('提示框', '部门及人员将会与AD同步？', function(btn) {
        			            if (btn == 'yes')
        			            {
        			            	Ext.MessageBox.show({
        			    	         	msg : '用户功能加载中，请稍后...',
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
        		    		            serviceUrl: 'orgService/ADOperTest',
        		    		            success: function(data) 
        		    		            {
        		    		            	Ext.MessageBox.hide();
        		    		            	var create = data.create;
        		    		            	var update = data.update;
        		    		            	var error = data.error;
        		    		            	var errorName = data.errorName;
        		    		            	
        		    		            	Ext.Msg.alert("AD同步信息","更新" +update+ "个；新创建" +create+ "个；有"+error+"个同步存在问题:" + errorName);
        		    		            	bmzwtree.refreshTree();
        		    		            },
        		    		            failure: function(data)
        		    		            {
        				                	Beidasoft.Bap.alert("同步失败");
        		    		            }
        		    		        });
        			            }
        	           		});
        	        	}
        	        }]
        		});
        		tbar.render(this.tbar);
        	}
        }
   	});
   
   	bmzwtree.on('click', function(nd, e){
		var depth = nd.getDepth()
		switch (depth){
			case 0 :
				gridRY.getStore().removeAll();
				gridJN.getStore().removeAll();
				gridFL.getStore().removeAll();
				break;
			default :
				gridRY.loadData("personManageListService/createPersonGrid/" + nd.id);
			break;
		}
		//清空查询框
		Ext.getCmp('cx').setValue();
		Ext.getCmp('cxbox').setValue();
	});
   	
   	
	//*****************写一个组织机构的表单*********************//
   	
   	var zzjgForm = new Beidasoft.Bap.FormPanel({   
   		newDataServiceUrl:'personManageListService/createDepartment',
   		loadDataServiceUrl:'personManageListService/loadDepartment',
   		submitServiceUrl:'personManageListService/addDepartment',
   		frame: false,
		border: false,
		name: 'zzjgForm',
		id: 'zzjgForm',
		plain: true,
   		labelWidth: 80,
   		items:
   		[{
   			fieldLabel:'部门名称',
   			xtype : 'textfield',
   			name : 'bname',
   			anchor : '95%',
   			allowBlank:false,
   			blankText:'部门名称不能为空',
   			maxLength : 400,
   			maxLengthText : '最大长度400'
   		}, {
   			fieldLabel : '描述',
   			xtype : 'textfield',
   			name : 'description',
   			anchor : '95%',
   			allowBlank: false,
   			maxLength : 400,
   			maxLengthText : '最大长度400'
   		}, {
   			xtype: 'numberfield',
   			allowDecimals : false, // 不允许小数

   		    allowNegative : false, // 不允许负数

   			fieldLabel : '序号',
   			allowBlank:false,
   			maxLength : 10,
   			maxLengthText : '最大长度10',
   			invalidText : '请输入数字',
   			name : 'pindex',
   			anchor : '95%'
   		},{
   			xtype: 'checkboxex',
			fieldLabel: '科研主承部门',
			name: 'mainDepartment',
			id: 'mainDepartment',
			checked: true
   		},
   		{
   			xtype: 'hidden',
   			id: 'parentid',
   			name : 'parentid',
   			anchor : '95%'
   		}
   		]
   	});
   	
   	zzjgForm.on('submit',function(){
   		bmzwtree.refreshTree();
   		comboStore.load();  // 重新加载一下部门combo
   	})
   	zzjgForm.on('beforesubmit',function(){
   		if(type=='add')
   		{
   		zzjgForm.findById("parentid").setValue(parentid);}
       	});
    function addForm(infsetName,oid,pid) { 	
        var form = new Beidasoft.Bap.DynamicFormPanel({
            title:'新增部门',
            width:'365',
            infsetName: infsetName
        });

        form.on('loadtemplate', function() {
 			form.NewData({oid:oid, pid:r});
 		});	
 		form.show();
        form.on('submit', bmzwtree.refreshTree, bmzwtree);
   	} 	
   	function updateBm() {
  		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
  		if (selNode === null) {
  			Beidasoft.Bap.alert('请先选择一个树节点!');
  			return;
  		}
  		
  		if (selNode.getDepth() === 0) {
  			Beidasoft.Bap.alert('请选择一个非根点!');
  		} else {
  			chgForm(selNode.id, "B01");
  		}
  	}
      	
   	function chgForm(rid,infsetName) {
   		var form = new Beidasoft.Bap.DynamicFormPanel({
			title:'修改部门',
			width:'365',
			infsetName: infsetName
		});
		
		form.on('loadtemplate', function() {
   			form.LoadData({rid: rid});
   		});
   		
   		form.show();
    	form.on('submit', bmzwtree.refreshTree, bmzwtree);
   	}
   	
   	// 人员grid
   	var op = {isAdd: true, id: undefined};
    var gridRY = new Beidasoft.Bap.GridPanel({
        checkBoxType: 2,
        region: 'center',
        title: '人员列表',
        serviceUrl: 'personManageListService/createPersonGrid',
        autoLoadData:false,
        havePagingToolbar:true,
        tbar: 
        [{
           	text: '新增',
           	iconCls: 'table_add',
           	handler: function() {
           		op.isAdd = true; op.id = undefined;
           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
           		if (selNode == null || selNode.getDepth() == 0) {            			
           			Beidasoft.Bap.alert('请先选择一个部门!');
           			return;
           		}
				var	oid = selNode.id;
				var pid = selNode.id;
				
				personForm.form.reset(); // 清空form
				
           		// 动态设置当前操作类型:create
				var actionTypeObj = personForm.getComponent("actionType_person");
	            actionTypeObj.setValue('create');
        		
           		// 初始新创建的form表单,并弹出窗口		        personForm.NewData({depId:pid});
		        personForm.title='新增人员信息';
		        personForm.Show('新增人员信息', false);
		        setFormWinTitle(personForm);
            }
        }, {
           	text: '修改',
           	iconCls: 'table_edit',
           	id : 'ryedit',
           	handler: function() {
           		op.isAdd = false; op.id = gridRY.getSelected() == undefined ? undefined : gridRY.getSelected().id;
           		var selecteds = gridRY.getChecked();
				var rid = null;
				if (selecteds && selecteds.length > 1) {
					Beidasoft.Bap.alert('一次只能修改一条记录!');
					return;
				} else if (!selecteds || selecteds.length <= 0) {
					Beidasoft.Bap.alert('请先选择需要修改的记录!');
					return ;
				}
				
				rid = selecteds[0].id;
				personForm.form.reset(); // 清空form
				
           		// 动态设置当前操作类型:create
				var actionTypeObj = personForm.getComponent("actionType_person");
	            actionTypeObj.setValue('update');
        		
           		// 初始新创建的form表单,并弹出窗口		        personForm.LoadData(rid);
		        personForm.title='修改人员信息';
		        personForm.Show('修改人员信息', false);
		        setFormWinTitle(personForm);
            }
        }, {
           	text: '删除',
           	iconCls: 'table_delete',
           	id : 'rydelete',
           	handler: function() {
           		var selecteds = gridRY.getChecked();
           		if (selecteds && selecteds.length > 1) {
					Beidasoft.Bap.alert('一次只能删除一条记录!');
					return;
				}
				else if (!selecteds || selecteds.length <= 0)
				{
					Beidasoft.Bap.alert('请先选择需要删除的记录!');
					return;
				}
				var selected = selecteds[0];
           		
				if (currentUserName == selected.row.data.A0102) {
					Beidasoft.Bap.alert('不能删除自己!');
					return;
				}
				
				var id = selected.id; // 人员ID
				deletePerson(id);
			}
        }, /*{
           	text:'批量新增人员',
           	iconCls: 'table_add',
           	handler:function() {
           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
           		if (selNode == null) {
           			Beidasoft.Bap.alert('请先选择一个部门!');
           			return;
           		}
           		xmlPerson();
           	}
        },*/ {
           	text: '批量新增费率',
           	iconCls: 'table_add',
           	handler: function() {
           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
           		if (selNode == null) {
           			Beidasoft.Bap.alert('请先选择一个部门!');
           			return;
           		}
           		
				var rows = gridRY.getSelectionModel().getSelections();
				
				if(rows.length == 0){
					Beidasoft.Bap.alert('请选择新增费率的人员！');
					return ;
				}
				if(rows.length == 1){
					Beidasoft.Bap.alert('批量新增费率需要两人及两人以上！')
					return ;
				}
				var	oid = selNode.id;
				var pid = gridRY.getSelected().id;
				if (rows) {
					var selectedRowID='';
					for (var i = 0; i < rows.length; i = i + 1) { 
						selectedRowID += rows[i].id + ",";
					}
					Ext.getCmp("oid").setValue(oid);
					Ext.getCmp("pid").setValue(selectedRowID);
				}
				personRate('批量新增费率');
           	}
        },/* {
           	text:'批量导入费率',
           	iconCls: 'table_add',
           	handler:function() {
           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
           		if (selNode == null) {
           			Beidasoft.Bap.alert('请先选择一个部门!');
           			return;
           		}
           		xmlRate();
           	}
        },*/ {
           	text: '重设密码',
           	iconCls: 'table_reset',
           	id : 'ryreset',
           	handler: function() {
           		var selected = gridRY.getSelected();
				if (!selected) {
					Beidasoft.Bap.alert('请选择人员!');
					return ;
				}
				
				resetPwd(selected.row.get('A0102'));
           	}
        },
        '-',
        {
           	text: '导出人员信息',
           	iconCls: 'table_add',
           	handler: function() {
//           		var sqlWhere = "A01.recordid <>' '";
//           		
//           		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
//           		if(selNode != null && selNode.id.length == 36){
//           			sqlWhere = "A01.B00='"+ selNode.id +"'";
//           		}
//           		
//           	    var report = new Beidasoft.Bap.layout.ReportPanel({
//			        reportName:'组织资源列表',
//			        reportTable:'A01',
//			        where : sqlWhere,
//			        orderBy:'PARENTID'
//			    });
//			    
//			    report.show();
            	var selNode = bmzwtree.getSelectionModel().getSelectedNode();
            	if(selNode == undefined) {
            		Beidasoft.Bap.alert('提示', '请选择组织机构');
            		return;
            	}
			    exportUsers(selNode.id);
           	}
//        },{
//           	text: '导入人员信息',
//           	iconCls: 'table_add',
//           	handler: function() {
//            	var selNode = bmzwtree.getSelectionModel().getSelectedNode();
////            	if(selNode == undefined) {
////            		Beidasoft.Bap.alert('提示', '请选择组织机构');
////            		return;
////            	}
//            	var deptid = selNode != undefined ? selNode.id : '';
//			    importUsers(deptid);
//           	}
        },'-', {
        	iconCls: 'page_gear',
        	text: '管理的部门',
        	id : 'rygear',
        	handler: function() {
    			var selecteds = gridRY.getChecked();
           		if (selecteds && selecteds.length > 1) {
           			Beidasoft.Bap.alert('提示','请选择一个人员！');
					return;
				}
    			formRoleDeptWin();
        	}
        }],
        listeners:{
			rowclick:function(grid, rowIndex, e) {
				if (!grid.getSelected() || !grid.getSelected().id) {
					return;
				}
				var item = grid.getSelected();
				setAttribute(item);
				var A01id = grid.getSelected().id;
				gridFL.loadData('personManageService/createRateGrid/' + A01id);
      			gridJN.loadObjectData("parentid = '" + A01id + "'");
      			
      			Ext.getCmp('ryedit').enable();
  				Ext.getCmp('rydelete').enable();
  				Ext.getCmp('ryreset').enable();
  				Ext.getCmp('rygear').enable();
  				
  				Ext.getCmp('fladd').enable();
  				Ext.getCmp('fledit').enable();
  				Ext.getCmp('fldelete').enable();
  				
  				Ext.getCmp('jnadd').enable();
  				Ext.getCmp('jnedit').enable();
  				Ext.getCmp('jndelete').enable();
      			
      			if (!item.row.data.valid) {
      				Ext.getCmp('ryedit').disable();
      				Ext.getCmp('rydelete').disable();
      				Ext.getCmp('ryreset').disable();
      				Ext.getCmp('rygear').disable();
      				
      				Ext.getCmp('fladd').disable();
      				Ext.getCmp('fledit').disable();
      				Ext.getCmp('fldelete').disable();
      				
      				Ext.getCmp('jnadd').disable();
      				Ext.getCmp('jnedit').disable();
      				Ext.getCmp('jndelete').disable();
      			}
			},
			rowdblclick:function(grid, rowIndex, e)
			{}
		}
    });
    
//    var exportCellWin;
//    var exportCell;
    var reportWin;
    var exportReport;
    function exportUsers(deptId) {
    	if(!exportReport) {
    		exportReport = new Beidasoft.Bap.Birt.ReportPanel({
        		reportPath: '组织资产管理/人员管理/人员信息.rptdesign',
        		exportFileName: '导出人员信息',
        		exportWord: true,
        		exportExcel: true,
        		autoLoad: false
        	});
    	}
    	exportReport.load({deptId: deptId});
		if(!reportWin) {
			reportWin = new Ext.Window({
				 title: '导出人员信息',
				 layout: 'fit',
				 closeAction: "hide",
				 maximizable: true,
				 width: document.body.clientWidth*0.7,
		         height: document.body.clientHeight*0.8,
				 items: [exportReport],
				 buttons:[{
					 text: '关闭',
					 iconCls: 'btn_cancel',
					 handler: function() {
						 reportWin.hide();
					 }
				 }]
				
			 })
	    }
		reportWin.show();
//		if(!exportCell) {
//			exportCell = new com.bop.CellPanel({
//				border: true,
//				renderTo: Ext.getBody(),
//				tbarCfg:{
//					EXCEL:true,
//					PDF:true,
//					PRINT:true,
//					PRINTVIEW:true
//				}
//			});
//		}
//		
//		if(!exportCellWin) {
//			exportCellWin = new Ext.Window({
//				 title: '导出人员信息',
//				 layout: 'fit',
//				 closeAction: "hide",
//				 maximizable: true,
//				 width: document.body.clientWidth*0.7,
//		         height: document.body.clientHeight*0.8,
//				 items: [exportCell],
//				 buttons:[{
//					 text: '关闭',
//					 iconCls: 'btn_cancel',
//					 handler: function() {
//						 exportCellWin.hide();
//					 }
//				 }]
//				
//			 })
//		 }
//		exportCellWin.show();
//		 exportCell.OpenFile('/module/bopmain/user/exportusers.cll');
//		 var url = '/Domain/DynamicAjax.do?serviceUrl=orgService/exportUsers/'+deptId;
//		 exportCell.FillData(url);
    }
    
    /**
     * 从Excel中导入人员信息
     */
    var importCell;
    var importCellWin;
    function importUsers(deptId) {
    	//与选择的组织单位没关系，根据Cell中的单位导入
    	if(!importCell) {
    		importCell = new com.bop.CellPanel({
				border: true,
				renderTo: Ext.getBody(),
				tbarCfg:{
					IMPORTEXCEL: true,
					DOWNLOADTEMPLATE: true
				}
			});
		}
		
		if(!importCellWin) {
			importCellWin = new Ext.Window({
				 title: '导入人员信息',
				 layout: 'fit',
				 closeAction: "hide",
				 maximizable: true,
				 width: document.body.clientWidth*0.7,
		         height: document.body.clientHeight*0.8,
				 items: [importCell],
				 buttons:[{
					 text: '导入',
					 iconCls: 'btn_save',
					 handler: function() {
						 var a = importCell.getAllCellValue(3, 8, 0);
						 if(a.length == 0) {
							 Beidasoft.Bap.alert('提示', '空数据不能导入');
							 return;
						 }
						 var result = checkAndGetDept(a, importCell);
						 if(result === false) return;
						 var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"正在导入数据..."});
						 myMask.show();
						 Ext.Ajax.request({
							 url: '/Domain/DynamicAjax.do?serviceUrl=orgService/importUsers',
							 params: {superdept: result.superdept.join(','), dept: result.dept.join(','), data: Ext.encode(a)},
							 success: function(r) {
								 var rt = r.responseText.trim();
								 rt = eval('(' + rt + ')');
								 if(rt.success) {
									 Beidasoft.Bap.alert('提示', '导入数据成功！');
									 bmzwtree.refresh();
									 importCellWin.hide();
									 a = null, result = null, rt = null;
									 myMask.hide();
									 return;
								 } else {
									 Beidasoft.Bap.alert('提示', '导入数据失败！'+rt.errors);
									 importCellWin.hide();
									 bmzwtree.refresh();
									 myMask.hide();
									 return;
								 }
							 },
							 failure: function(r) { 
								 Beidasoft.Bap.alert('提示', '导入数据失败！'+r.responseText);
								 myMask.hide();
								 return;
							 }
						 });
					}
				 },{
					 text: '关闭',
					 iconCls: 'btn_cancel',
					 handler: function() {
						 importCellWin.hide();
					 }
				 }]
				
			 })
		 }
		importCellWin.show();
		importCell.OpenFile('/module/bopmain/user/importusers.cll');
    }
    
    function checkAndGetDept(array, cell) {
    	if(typeof array != 'object') {
    		Beidasoft.Bap.alert('提示', '参数错误');
    		return false;
    	}
    	var msgtext = function(columnIndex) {
    		if(columnIndex == 0) return '员工编号';
    		if(columnIndex == 1) return '姓名';
    		if(columnIndex == 2) return '登录名';
    		if(columnIndex == 3) return '工作单位/所属部门';
    		return '';
    	}
    	var superdept = [], dept = [], loginNameObj = {};
    	for(var i=0,l=array.length; i<l; i++) {
    		var row = array[i];
    		for(var j=0; j<8; j++) {
    			var index = 'col'+j; 
    			if(row[index] == undefined) continue;
    			var column = removeSpace(row[index]);
    			var rowIndex = i+3;
    			
    			if(j == 4) superdept.push(column);//上级部门
    			else if(j == 3) dept.push(column);//所属部门
    			else if( j == 2 || j == 0) {
    				//检验登录名是否唯一
        			var name = loginNameObj[column];
        			var desc = j == 2 ? "登录名" : "员工编号";
        			if(name !== undefined) {
        				Beidasoft.Bap.alert('提示', '第'+rowIndex+'行'+desc+'【'+name+'】与前面的重复，登录名必须唯一!');
        				cell.object.SelectRange(j+1, rowIndex, j+1, rowIndex);
    					return false;
        			} 
        			loginNameObj[column] = column;
    			}
    			//不能为空 (0：员工编号；1：姓名；2：登录名；3：工作单位/所属部门)
        		if(j==0 || j==1 || j==2 || j==3) {
        			if(column != '') continue;
        			
        			var msg = msgtext(j);
    				if(msg == '') continue;
    				
					Beidasoft.Bap.alert('提示', '第'+rowIndex+'行'+msg+'列不能为空!');
					cell.object.SelectRange(j+1, rowIndex, j+1, rowIndex);
					return false;
        		}
    		}
    		
    	}
    	var obj = {superdept: superdept, dept: dept};
    	superdept = null, dept = null, loginNameObj = null; 
    	return obj;
    }
    
    function setAttribute(item){
    	nodeId = item.row.data.deptId;
    	selectTreeNode(bmzwtree.getRootNode());
    }
    // 选中人员列表 同步选中相应部门
    function selectTreeNode(me) { 
    	if (nodeId != null) {
    		var nodes = me.childNodes;
    		// 循环数的每个节点 判断节点ID 是否和taskId 相同 ，是 为此节点设置为 选中状态
    		for ( var i = 0; i < nodes.length; i++) {
    			selectTreeNode(nodes[i]);
    			if (nodes[i].id === nodeId) {
    				nodes[i].getUI().addClass("x-tree-selected"); // 是否选中 
    				if(nodes[i].isSelected() == false)
    				{
    					bmzwtree.getNodeById(nodes[i].id).select();
    				}
    			}
    		}
    	}
    };
    
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
    var depttree = new Beidasoft.Bap.TreePanel({
		border: false,
        layout: 'fit',
        title: '',
        rootVisible: false,
        checkBox: true,
        name : 'roledepts',
		id : 'roledeptsID',
		anchor : '95%',
        checkModel : 'childCascade',
        url: '/Domain/DynamicTreeAjax.do',
        serviceUrl: 'semip_orgService/getMulDeptTree',
        clearCheckedNodes: function() {
        	Ext.each(this.getChecked(), function(nd) {
        		if(nd != undefined) {
        			nd.getUI().checkbox.checked = false;
        			nd.attributes.checked = false;
        		}
        	});
        }
    });
    
    depttree.on('expandnode', function(nd){
    	var child = nd.childNodes;
		for(var i=0;i<child.length;i++){
			child[i].expand();
		}
	});
    var formRoleDept = new Ext.Window({
        title: '用户管理的部门',
        width:350,
        height:450,
        layout : 'fit',
        resizable: true,
        closeAction:'hide', //关闭动作设为隐藏
        plain: true,
        border: false,
        autoScroll : true,
        items: [depttree],
        buttons: [{
            text: '确定',
            iconCls : 'btn_ok',
            handler: function() {
            	var item = gridRY.getSelected();
            	var checkedNodes = depttree.getChecked();
            	var depts = [];
            	Ext.each(checkedNodes, function(nd) {
            		if(nd != undefined) depts.push(nd.id);
            	})
            	Beidasoft.Bap.Ajax.request({
    				serviceUrl : 'semip_orgService/saveUserDepts/' + item.id + '/' + depts.join(','),
    				method: 'POST',
    				success : function(result) {
    					if (result.success) {
    						Beidasoft.Bap.alert('设置成功!');
    						gridRY.refresh();
    					}
    				}
    			});
            	
            	formRoleDept.hide();
            }
        }, {
        	text: '取消',
        	iconCls : 'btn_cancel',
        	handler: function() {
        		formRoleDept.hide();
        	}
        }]
        	
    });
    formRoleDept.hideLoad();
    
    function formRoleDeptWin() {
    	var item = gridRY.getSelected();
    	if(item == null || item == undefined) {
    		Beidasoft.Bap.alert('提示', '请选择人员');
    		return;
    	}
		depttree.clearCheckedNodes();
    	
    	Beidasoft.Bap.Ajax.request({
    		serviceUrl : 'semip_orgService/getUserDepts/' + item.id,
    		method: 'POST',
    		success : function(result) {
    			formRoleDept.show();
    			//赋值，选中所选项    		
        		var depts = result.roledepts || '';
        		var arr = depts.split(',');
        		
        		for(var i = 0; i < arr.length; i ++) {
        			var nd = depttree.getNodeById(arr[i]);
        			if(nd != undefined) {
        				nd.attributes.checked = true;
        				nd.getUI().checkbox.checked = true;
        			}
        		}
        		depts = null;
        		arr = null;
    		}
    	});
    	    	
    }

    gridRY.on('loaddatacomplete', function(){
        if (gridRY.store.data.length > 0) {
        	
	     	gridRY.getSelectionModel().selectFirstRow();
			var A01id = gridRY.getSelected().id;
			gridFL.loadData('personManageService/createRateGrid/' + A01id);
  			gridJN.loadObjectData("parentid = '" + A01id + "'");	 
		}     
        
        var item = gridRY.getSelected();
        if (item != null && !item.row.data.valid) {
        	Ext.getCmp('ryedit').disable();
			Ext.getCmp('rydelete').disable();
			Ext.getCmp('ryreset').disable();
			Ext.getCmp('rygear').disable();
			
			Ext.getCmp('fladd').disable();
			Ext.getCmp('fledit').disable();
			Ext.getCmp('fldelete').disable();
			
			Ext.getCmp('jnadd').disable();
			Ext.getCmp('jnedit').disable();
			Ext.getCmp('jndelete').disable();
        }
        
        // 添加空值判断 部门中没有人员 item == null 出现js错误
        if (item != null){
        	setAttribute(item);
        } else {
        	gridJN.getStore().removeAll();
			gridFL.getStore().removeAll();
        }
		
    });
    
    // 组织机构下拉框Ext.data.ArrayStore
    var comboStore = new Ext.data.Store({
    	proxy : new Ext.data.HttpProxy({url:'/Domain/DynamicAjax.do?serviceUrl=personManageListService/getBmInfo'}),
    	reader: new Ext.data.ArrayReader({},
			[
			{name:'value',mapping:'value'},
	        {name:'text',mapping:'text'}
    	])
    })
    comboStore.load();
    //--------------------------人员表单--------------------------------------------------------
	var formChange = false;
    var personForm = new Beidasoft.Bap.FormPanel({
		title:'新增人员',
		border: false,
       	region: 'center',
       	height: 420,
       	width : 360,
       	labelWidth :65,
       	resizable:false,
       	scope: this,
       	failureHide:false,
       	labelAlign :'left',
       	// 防止checkbox居中对齐
       	bodyStyle :'text-align:left;margin-left:10px;margin-right:10px;',
		loadDataServiceUrl: 'personManageListService/loadPerson',
		newDataServiceUrl: 'personManageListService/newPerson',
		submitServiceUrl: 'personManageListService/savePerson',
		items: 
		[{
			fieldLabel : '员工编号',
			name : 'A0101',
			maxLength:12,
			id:'personNo',
			allowBlank:false,
			stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			anchor : '90%',
			
			enableKeyEvents : true,
			listeners: {
				change:function(){
					formChange=true;
				
				},
				keyup: function(tf, e){
					formChange = true;
					var value = Ext.getCmp('personNo').getValue();
					var username = Ext.getCmp('loginName').getValue();
					if (value === '') {
						return;
					} 
					Beidasoft.Bap.Ajax.request({
						serviceUrl:'personManageListService/isNumCf/'+value+'/'+username,
						success: function(data) {
		                    	if (data === true) {
		                    		Beidasoft.Bap.alert('编号重复，请重新填写编号!');
		                    		return;
		                    	}
		                    }
					});
				}
			}
		},
		{
			fieldLabel : '员工姓名',
			name : 'A0103',
			maxLength:100,
			id : 'personName',
			allowBlank:false,
			stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			anchor : '90%',
			enableKeyEvents : true,
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}	
		},
		{
			fieldLabel : '登录名',
			name : 'A0102',
			maxLength:100,
			id : 'loginName',
			allowBlank:false,
			vtype:'alphanum',
			stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			anchor : '90%',
			enableKeyEvents : true,
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}
		},
		{
			fieldLabel : '工作单位',
			name : 'A0108',
			maxLength:1000,
			allowBlank:true,
			stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			anchor : '90%',
			enableKeyEvents : true,
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}	
		},
		{
			fieldLabel : '人员密级',
			name : 'A0113',
			allowBlank:true,
			codeTableName:'GJB001',  
			xtype:'codefield',
			anchor : '90%',
			enableKeyEvents : true
		},
		{
			xtype : 'textarea',
			fieldLabel : '描述',
			name : 'A0104',
			maxLength:1000,
			allowBlank:true,
			enableKeyEvents : true,
			//stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			height:80,
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}	
		},
   		{
   			xtype: 'numberfield',
   			allowDecimals : false, // 不允许小数
   		    allowNegative : false, // 不允许负数
   		    enableKeyEvents : true,
   			fieldLabel : '序号',
   			allowBlank:false,
   			maxLength : 10,
   			maxLengthText : '最大长度10',
   			invalidText : '请输入数字',
   			name : 'A0110',
   			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}
   		},
		{
   			xtype : 'textfield',
   			fieldLabel : '联系电话',
			name : 'A0107',
			maxLength:100,
			enableKeyEvents : true,
			//allowBlank:false,
			//blankText:'不能为空',
			//stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			//regex:/(^((0\d{2,3})-)?(\d{7,8})(-(\d{3,}))?$)|(^1[35]\d{9}$)|(^0\d{10,11}$)/,
			//regexText:'联系电话格式不正确',
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}
		},
		{
			xtype : 'textfield',
			fieldLabel : '邮箱',
			name : 'A0106',
			maxLength:100,
			enableKeyEvents : true,
			allowBlank :true,
			//vtype:'email',
			//stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			//blankText :'不能为空',
			//regex :/^([a-zA-Z0-9]|[._])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/,
			//regexText :'邮箱格式不正确',
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}
		},
		{
			xtype : 'textfield',
			fieldLabel : '职位信息',
			name : 'A0109',
			maxLength:100,
			enableKeyEvents : true,
			//allowBlank:false,
			//stripCharsRe :/^\s+|\s+$/g,//禁止左右空格
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				},
				keyup: function(tf, e){
					formChange=true;
				}
			}
		},
		{
			xtype:'hidden',
			name : 'createUser',
			id : 'createUser',
			anchor : '90%'	,
			listeners: {
				change:function(){
					formChange=true;
				}
			}
		},
		{
			fieldLabel : '部门id',
			id : 'departmentId',
			name : 'departmentId',
			hidden : true,
			hideLabel : true,
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				}
			}
		},
		{
			fieldLabel : '人员操作类型',
			id : 'actionType_person',
			name : 'actionType',
			hidden : true,
			hideLabel : true,
			anchor : '90%',
			listeners: {
				change:function(){
					formChange=true;
				}
			}
		},{
			xtype: 'combo',
			fieldLabel:'组织机构',
			id:'deptCombo',
			typeAhead : true,
        	lazyRender : true,
			name:'deptCombo',
			anchor:'90%',
			mode:'remote',
			triggerAction:'all',
           	store:comboStore,      
     	    valueField:'value',
        	displayField:'text',
        	editable : false,
			listeners: {
				change:function(){
					formChange=true;
				}
			}
       /// 	hiddenName:'deptCombo'
        	
		}]
	});
	
	// 刷新列表
	personForm.on('submit',function(action)
	{
		if (action.result.failMsg != undefined)
		{
			Beidasoft.Bap.alert(action.result.failMsg);
		}
		else
		{
			gridRY.refresh();
		}
    });
    
    personForm.on('submitfail', function(action)
    {
        var data = Ext.decode(action.response.responseText);
        if (data && data.failMsg)
        {
            Beidasoft.Bap.alert("保存失败", data.failMsg);
        }
        else
        {
			gridRY.refresh()
		}
    });
    personForm.on('beforesubmit', function(action){
    	//验证员工编号是否重复  sjw 2011/10/16
    	var personNo = Ext.getCmp('personNo').getValue() || '';
    	var store = gridRY.getStore();
    	var a = store.data.items || [];
    	for(var i = 0, l = a.length; i < l; i++) {
    		var item = a[i];
    		var num = item.data.A0101 || '';
    		if(num == personNo) {
    			if(!op.isAdd && op.id == item.data.id) continue;
    			Beidasoft.Bap.alert('提示', '员工编号重复');
    			return false;
    		}
    	}
    	// 验证结束
    	var deptid = Ext.getCmp('deptCombo').getValue();
    	if(deptid!=null && deptid != ''){
//    		personForm.submitParams["deptId"] = deptid;
    		personForm.findById('departmentId').setValue(deptid);
    	}
    });
    
	// 新增人员时,解禁部分表单域及显示"创建用户"表单域	personForm.on('newdata', function()
	{
		var items = ['loginName'];
		enableFormItems(items);
		
		var items2 = ['createUser'];
		showFormItems(items2);
		
		var items3 = ['deptCombo'];
		hideFormItems(items3);
	});
	
	// 修改人员时,禁用部分表单域及隐藏"创建用户"表单域	personForm.on('loaddata', function()
	{
		var items = ['loginName'];
		disableFormItems(items);
		
		var items2 = ['createUser'];
		hideFormItems(items2);
		
		var items3 = ['deptCombo'];
		showFormItems(items3);
	});
	
	// 设置表单弹出窗口的title
    var setFormWinTitle = function(form, title)
    {
        var winID = form.id + '_win'
        var win = form[winID]
        if (win)
        {
        	fireHiddenEvent(form);
        }
    }
    
	// 设置表单弹出窗口 关闭窗口时执行提问是否保存
    var fireHiddenEvent = function(form)
    {
    	
    	var winID = form.id + '_win'
        var win = form[winID]
    	if (win){
    		win.on('hide',function(){
    			if(event==null)return;
    			if(event.keyCode==27){
    		    	if(formChange==false){
    		    		return;
    		    	}
    				if (win.isVisible() == false)
    				{
    					win.show();
    				}
	            	Ext.Msg.confirm('提示','是否提交保存',function(btn){
	            	       if(btn=='yes'){
	            	    	   if(form.form.isValid() && form.Checkblank()){
	            	    		 form.Submit();
	            	    	   }else{
	            	    		   Beidasoft.Bap.alert("提示","非法，保存失败！");
	            	    	   }
	            	    	   if (win.isVisible() == true)
	            	    	   {
	            	    		   win.hide();
	            	    	   }
	            	       }else{
	            	    	   win.hide();
	            	    	   Beidasoft.Bap.alert("提示","不保存退出！");
	            	       }
	            	       formChange=false;
	            	})
            	}
            },null,{single:true})
    	}
        
    }
    
	/**
	 * 禁用部分表单域(当然,对任何组件都可用,不一定是表单域)
	 * @param items 数组类型; 包含表单域对象或id
	 */
	var disableFormItems = function(items)
	{
		commonOperate(items, 'disable');
	}
	
	/**
	 * 解禁用表单域
	 * @param items 数组类型; 包含组件对象或id
	 */
	var enableFormItems = function(items)
	{
		commonOperate(items, 'enable');
	}

	/**
	 * 隐藏表单域	
	 * @param items 数组类型; 包含组件对象或id
	 */
	var hideFormItems = function(items)
	{
		commonOperate(items, 'hide');
	}
	
	var showFormItems = function(items)
	{
		commonOperate(items, 'show');
	}
	
	/**
	 * private 
	 * 
	 * 通用操作方法; 
	 */
	var commonOperate  = function(items , operateType)
	{
		if (!items || items.length <= 0)
		{
			return ;
		}
		var item = null;
		for (var i = 0, len = items.length; i < len ; i = i + 1)
		{
			item = items[i];
			if (item)
			{
				item = Ext.getCmp(item);
				if (item)
				{
					item[operateType]();
					// 同步隐藏label
					if (operateType == 'hide' && item.fieldLabel)
					{
						item.getEl().up('.x-form-item').setDisplayed(false);
					}
					else if (operateType == 'show' && item.fieldLabel)
					{
						item.getEl().up('.x-form-item').setDisplayed(true);
					}
				}
			}
		}
	}
	
    var deletePerson = function(id)
    {
        Ext.MessageBox.confirm('提示框', '您确定删除？', function(btn)
        {
            if (btn == 'yes')
            {
				Beidasoft.Bap.Ajax.request(
		        {
		            serviceUrl: 'personManageListService/deletePersons/' + id,
		            success: function(data) 
		            {
		            	gridRY.refresh();
		            },
		            failure: function(data)
		            {
		            	if (data && data.success == false)
		            	{
		            		Beidasoft.Bap.alert(data.failMsg);
		            	}
		            	else
		            	{
		                	Beidasoft.Bap.alert("删除失败");
		                }
		            }
		        });
            }
        });
    }
    
    // ----------------------------------------------------------------------------------
    // 批量导入费率弹出窗口
    var xmlWindow;
    function xmlRate()
    {
    	if (!xmlWindow)
    	{
    		xmlWindow = new Ext.Window(
    		{ 	
				title:'批量导入费率',
		        layout: 'fit',
		        //关闭按钮
		        closeAction:'hide',
		        resizable : false,
		        modal : true,
		        width : 390,
		        height : 260,
		        buttons:
		        [{
	                text: '确定',
	                iconCls: 'btn_ok',
	                handler: function()
	                {
	                	if(Ext.getCmp("xmlfileID1")==undefined || Ext.getCmp("xmlfileID1").getValue() == '') {
	                		Beidasoft.Bap.alert('提示','请选择文件');
	                		return;
	                	}
						if (xmlRateForm.form.isValid())
						{
							xmlRateForm.form.submit(
							{
								url:'/Domain/DynamicFormPostAjax.do',
		                        method:'POST',
		                        waitMsg:"正在保存......",
						      	waitTitle:"请稍后",
						      	waitWidth:400, 
		                        params:{serviceUrl:xmlRateForm.submitServiceUrl},
		                        success:function(form, action)
		                        {
		                        	xmlRateForm.getForm().reset();
			                		var arrForms = document.getElementsByTagName('form');
		    						arrForms[0].reset();
		    						gridFL.refresh();
		                        	Beidasoft.Bap.alert("成功","保存成功!");
		                        },
		                        failure:function(form, action)
		                        {
	                           		xmlRateForm.getForm().reset();
			                		var arrForms = document.getElementsByTagName('form');
									arrForms[0].reset();
		                        	Beidasoft.Bap.alert("失败"," 保存失败！");
		                        }
		                    })
						}	     
	                	xmlWindow.hide();
	                }
				},
				{
	                text: '取消',
	                iconCls: 'btn_cancel',
	                handler: function()
	                {
	                	xmlRateForm.getForm().reset();
                		var arrForms = document.getElementsByTagName('form');
   						arrForms[0].reset();
	                	xmlWindow.hide();
	                }
				}],
                items: [xmlRateForm]
            });
        }
    	xmlWindow.show();
    }
    
    //批量新增人员 窗口
    var xmlWindow1;
    function xmlPerson()
    {
    	if (!xmlWindow1)
    	{
    		xmlWindow1 = new Ext.Window(
    		{ 	
				title:'批量新增人员',
		        layout: 'fit',
		        //关闭按钮
		        closeAction:'hide',
		        resizable : false,
		        modal : true,
		        width : 390,
		        height : 260,
		        buttons:
		        [{
	                text: '确定',
	                iconCls: 'btn_ok',
	                handler: function()
	                {	
	                	if(Ext.getCmp("xmlfileID")==undefined || Ext.getCmp("xmlfileID").getValue() == '') {
	                		Beidasoft.Bap.alert('提示','请选择文件');
	                		return;
	                	}
                		xmlPersonForm.form.submit(
						{
							url:'/Domain/DynamicFormPostAjax.do',
	                        method:'post',
	                        waitMsg:"正在保存......",
					      	waitTitle:"请稍后",
					      	waitWidth:400, 
	                        params:{serviceUrl:xmlPersonForm.submitServiceUrl,names:['xmlfileID']},
	                        success:function(form, action)
	                        {
	                        	xmlPersonForm.getForm().reset();
		                		var arrForms = document.getElementsByTagName('form');
	    						arrForms[0].reset();
	    						gridRY.refresh();
	    						if(action.result.info!=undefined){
	    							Beidasoft.Bap.alert(action.result.info);
	    						}else{
	    							Beidasoft.Bap.alert("成功","保存成功!");
	    						}
	                        	
	                        },
	                        failure:function(form, action)
	                        {
	                        	xmlPersonForm.getForm().reset();
		                		var arrForms = document.getElementsByTagName('form');
								arrForms[0].reset();
	                        	Beidasoft.Bap.alert("失败"," 保存失败！");
	                        }
	                    })
				     
	                    xmlWindow1.hide();
	                	
	                	
	                }
				},
				{
	                text: '取消',
	                iconCls: 'btn_cancel',
	                handler: function()
	                {
	                	xmlWindow1.hide();
	                }
				}],
                items: [xmlPersonForm]
            });
        }
    	xmlWindow1.show();
    }
    
   	// 批量入费率表单导
    var xmlRateForm = new Beidasoft.Bap.FormPanel(
    {
		submitServiceUrl: 'personManageService/submitXMLRate',    	
		frame :false,
		border:false,
		fileUpload:true,
		name :'xmlRate',
		id :'xmlRate',
		labelWidth:80,
        items:
        [{
			//xml文件
			xtype:'textfield',
			anchor:'95%',
			fieldLabel:'选择XML文件',
			id:'xmlfileID1',
			regex:'',
			regexText:'',
			autoCreate :
			{   
	            tag : "input",   
	            type : "file",
	            name : 'file',
	           	size : "20",   
	            autocomplete : "off"
	        },
	        validator:null,
	        listeners:
	        {
	        	"focus" : function()
	        	{
		        	if (this.getValue() == "")
		        	{
		        			return;
	        		}
	        		if(xmlWindow != undefined) xmlWindow.buttons[0].enable();
	            	var filePath = Ext.getCmp("xmlfileID1").getValue();
	            	var suffix = filePath.substring(filePath.lastIndexOf(".") , filePath.length);
	            	if(suffix != '.xml'|| suffix != '.XML'||count==''||count == null){
	            		Ext.getCmp("xmlfileID1").markInvalid("请选择正确的XML文件！");
	            		xmlWindow.buttons[0].disable();
	            		return;
	            	}
	            	
					Beidasoft.Bap.Ajax.request({
	                    serviceUrl: 'personManageService/getNames/' + filePath,
	                    success: function(data) 
	                    {
	                    	Ext.getCmp("personTotal").setValue(data.personTotal);
	                    	Ext.getCmp("person").setValue(data.person);
	                    }
	                });
		        }
	        }
		},
		{
			xtype:'textfield',
			name:'names',
			inputType:'hidden',
			value:'xml_file_up'
		},
		{
			fieldLabel:'导入的人数',
			xtype:'textfield',
			anchor:'95%',
			readOnly : true,
			id:'personTotal1',
			name:'personTotal'
        },
        {
			fieldLabel:'导入的人员',
			xtype:'textarea',
			height:120,
			readOnly : true,
			anchor:'95%',
			id:'person1',
			name:'person'
        }]
    });
    
    
    // 批量人员导入
    var xmlPersonForm = new Beidasoft.Bap.FormPanel({
		submitServiceUrl: 'personManageService/savePersonInfo',    	
		frame :false,
		border:false,
		fileUpload:true,
		name :'xmlPerson',
		id :'xmlPerson',
		labelWidth:80,
        items:
        [{
			//xml文件
			xtype:'textfield',
			anchor:'95%',
			fieldLabel:'选择XML文件',
			id:'xmlfileID',
			regex:'',
			regexText:'',
			autoCreate :
			{   
	            tag : "input",   
	            type : "file",
	            name : 'file',
	           	size : "20",   
	            autocomplete : "off"
	        },
	        validator:null,
	        listeners:
	        {
	        	"focus" : function()
	        	{
		        	if (this.getValue() == "")
		        	{
		        			return;
	        		}
		        	if(xmlWindow != undefined) xmlWindow.buttons[0].enable();
	            	var filePath = Ext.getCmp("xmlfileID").getValue();
	        		var suffix = filePath.substring(filePath.length-4 , filePath.length);
	            	if(suffix != '.xml' && suffix != '.XML'){
	            		Ext.getCmp("xmlfileID").markInvalid("请选择正确的XML文件！");
	            //		var winID = xmlRateForm.id +'_win';
	            		xmlWindow.buttons[0].disable();
	            		return;
	            	}
	            	var selNode = bmzwtree.getSelectionModel().getSelectedNode();
	            	Ext.getCmp('departmentId').setValue(selNode.id);
	            	Beidasoft.Bap.Ajax.request(
					{
	                    serviceUrl: 'personManageService/getNames/'+filePath,
	                    success: function(data) 
	                    {
	                    	if(data.info!=null){
	                    		Beidasoft.Bap.alert(data.info);
	                    	}
	                    	Ext.getCmp("personTotal").setValue(data.personTotal);
	                    	Ext.getCmp("person").setValue(data.person);
	                    	gridRY.refresh();
	                    }
	                });
		        }
	        }
		},
		{
			xtype:'textfield',
			id:'departmentId',
			name:'departmentId',
			inputType:'hidden',
			value:'xml_file_up',
			anchor:'95%',
			readOnly : true
		},
		{
			fieldLabel:'导入的人数',
			xtype:'textfield',
			anchor:'95%',
			readOnly : true,
			id:'personTotal',
			name:'personTotal'
        },
        {
			fieldLabel:'导入的人员',
			xtype:'textarea',
			height:120,
			readOnly : true,
			anchor:'95%',
			id:'person',
			name:'person'
        }]
    });
    
	
    /////// 费率grid
    var gridFL = new Beidasoft.Bap.GridPanel(
    {
        checkBoxType:2,
        region: 'center',
        autoLoadData:true,
        serviceUrl: '',
        border:false,
        havePagingToolbar:false,
        tbar:
        [{
        	text: '新增',
        	iconCls: 'table_add',
        	id : 'fladd',
        	handler: function()
        	{
        		var selNode = bmzwtree.getSelectionModel().getSelectedNode();
        		if (selNode == null)
        		{
        			Beidasoft.Bap.alert('请先选择一个部门!');
        			return;
        		}
        		
        		if (gridRY.getSelectionModel().getSelections().length === 0) {
        			Beidasoft.Bap.alert('请选择人员！');
					return;
        		}
        		
        		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时添加多个人员费率！');
					return;
        		}
        		
        		var	oid = selNode.id;
                var pid = gridRY.getSelected().id;
                var rows = gridRY.getSelectionModel().getSelections();
                if (rows)
                {
					var selectedRowID='';
					selectedRowID+=rows[0].id+",";
					Ext.getCmp("oid").setValue(oid);
					Ext.getCmp("pid").setValue(selectedRowID);
				}
                personRate('新增费率');
        	}
        },
        {
        	text: '修改',
        	iconCls: 'table_edit',
        	id : 'fledit',
        	handler: function()
        	{
        		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时修改多个人员费率！');
					return;
        		}
        		
        		var rowsUpdate = gridFL.getSelectionModel().getSelections();
        		if (rowsUpdate.length == 0)
        		{
        			Beidasoft.Bap.alert('请选择一条费率记录！');
					return;
        		}
        		
        		if (rowsUpdate.length > 1)
        		{
					Beidasoft.Bap.alert('不能批量修改人员费率！');
					return;
        		}
        		//当前费率 不允许修改 可以修改非当前费率
        		var row = gridFL.getSelected();
        		var crate = row.row.get('currentRate');
//            	if(crate == "true"){
//            		Beidasoft.Bap.alert('警告', '当前费率使用中，不允许修改!');
//            		return;
//            	}
                rateFormUpdate.LoadData(gridFL.getSelected().id);
                Ext.getCmp("rateIdUpdate").setValue(gridFL.getSelected().id);
                if(crate == "true"){
                	Ext.getCmp('currentRateUpdate').setDisabled(true);
                	Ext.getCmp('efficientDateUpdate').setDisabled(true);
                }else{
                	Ext.getCmp('currentRateUpdate').setDisabled(false);
                	Ext.getCmp('efficientDateUpdate').setDisabled(false);
                }
                personRateUpdate();
        	}
        },
        {
        	text: '删除',
        	iconCls: 'table_delete',
        	id : 'fldelete',
        	handler: function()
        	{
        		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时删除多个人员费率！');
					return;
        		}
        		
        		var rows = gridFL.getSelectionModel().getSelections();// 返回值为 Record 数组 
        		var count = gridFL.getCount();//获取全部费率记录数
        		
                if (rows.length == 0)
                {
                    Beidasoft.Bap.alert('警告', '最少选择一条信息，进行删除!');
                }else if(count == 1){
                	Beidasoft.Bap.alert('警告', '请至少保留一条费率信息!!');
                }
                else
                {
                	var row = gridFL.getSelected();
            		var crate = row.row.get('currentRate');
                	if(crate == "true"){
                		Beidasoft.Bap.alert('警告', '当前费率不允许删除!');
                	}else{
                	     Ext.MessageBox.confirm('提示框', '您确定要进行该操作？', function(btn)
                                 {
                                     if (btn == 'yes')
                                     {
                                         if (rows)
                                         {
                                             var selectedRateID='';
                                             for (var i = 0; i < rows.length; i = i + 1)
                                             { 
             									selectedRateID += rows[i].id + ",";
             								}
             			
             								Beidasoft.Bap.Ajax.request(
             								{
             							        serviceUrl:'personManageService/deleteRate/' + selectedRateID,
             							        success:function(data)
             							        {
             										for (var i = 0; i < rows.length; i = i + 1)
             										{
             											gridFL.store.remove(rows[i]); 
             										}
             							        },
             							        failure: function()
             							        {
             								        Beidasoft.Bap.alert("删除失败");
                                                 } 
             							    });
                                         }
                                     }
                                     else
                                     {
                                         return;
                                     }
                                 })
                	}
                	
                	
               
                }
        	}
        }]
    });
    
	// 费率修改弹出窗口
    var rateWindowUpdate;
    function personRateUpdate()
    {
    	if (!rateWindowUpdate)
    	{
    		rateWindowUpdate = new Ext.Window(
    		{ 	
				title:'修改费率',
		        layout: 'fit',
		        closeAction:'hide',
		        resizable : false,
		        modal : true,
		        width : 390,
		        height : 260,
		        buttons:
		        [{
	                text: '确定',
	                iconCls: 'btn_ok',
	                handler: function()
	                {
	                	var selectItem = gridFL.getSelectionModel().getSelected();
				        var isRate=selectItem.get("currentRate");
				        var itemid = selectItem.get('id');
				        var gridFLsize = gridFL.store.data.length;
	                	var currentRate1 = Ext.getCmp('currentRateUpdate').getValue();
	                	
//	                	if (currentRate1 == '')
//	                	{
//	                		currentRate1='false';
//	                	}
//	                	
//	                	for (var i = 0; i < gridFLsize; i = i + 1)
//	                	{
//	                		var item = gridFL.store.getAt(i);
//	                		var id = item.get('id');
//	                		if (itemid != id)
//	                		{
//	                			var pl = item.get('currentRate');
//		                		if (isRate == 'false' && currentRate1 == 'true' && pl == 'true')
//		                		{
//		                			Beidasoft.Bap.alert("提示","已经存在当前费率!");
//		                			return ;
//		                		}
//	                		}
//	                	}
				        
						if (rateFormUpdate.form.isValid())
						{
							rateFormUpdate.form.submit(
							{
								url:'/Domain/DynamicFormPostAjax.do',
		                        method:'POST',
		                        waitMsg:"正在保存......",
						      	waitTitle:"请稍后",
						      	waitWidth:400, 
		                        params:{serviceUrl:rateFormUpdate.submitServiceUrl},
		                        success:function(form, action)
		                        {
		                        	Beidasoft.Bap.alert("成功", "保存成功!");
				                	gridFL.refresh();
		                        },
		                        failure:function(form, action)
		                        {
		                        	 Beidasoft.Bap.alert("失败"," 保存失败！");
		                        }
		                    })
						}	     
						
						Ext.getCmp('currentRateUpdate').setDisabled(false);
	                	Ext.getCmp('efficientDateUpdate').setDisabled(false);
	                	rateWindowUpdate.hide();
	                }
				},
				{
	                text: '取消',
	                iconCls: 'btn_cancel',
	                handler: function()
	                {
	                	rateWindowUpdate.hide();
	                }
				}],
                items: [rateFormUpdate]
            });
    	}
    	
    	rateWindowUpdate.show();
    }
    
    // 费率修改表单
    var rateFormUpdate = new Beidasoft.Bap.FormPanel(
    {
        loadDataServiceUrl: 'personManageService/loadRate',
		submitServiceUrl: 'personManageService/submitUpdateRate',    	
		frame :false,
		border:false,
		fileUpload:false,
		name :'rateFormUpdate',
		id :'rateFormUpdate',
		plain:true,
		labelWidth:80,
		// 是否显示等待提示
		waitMsgTarget :true,
		// 前台验证不提交表单        monitorValid:true,
        items:
        [{
			xtype: 'compositefield',
   			fieldLabel:'标准费率',
   			anchor:'95%',
   			items:
   			[{	
				xtype:'numberfield',
				name:'normalRateUpdate',
				id:'normalRateUpdate',
				allowBlank : false
			},
			{
				//费率单位
				xtype:'codefield',
				codeTableName:'GJB062',
				emptyText:'请选择费率单位...',
				name:'rateUnitNormalUpdate',
				id:'rateUnitNormalUpdate',
				editable : false,
				flex:1,
				listeners:
				{
					"blur":function()
					{
                        Ext.getCmp("rateUnitAddedUpdate").setValue(Ext.getCmp("rateUnitNormalUpdate").getValue().id);
                    }
				}
			}]
		},
		{
			xtype: 'compositefield',
   			fieldLabel:'加班费率',
   			anchor:'95%',
   			items:
   			[{
				xtype:'numberfield',
				name:'addedRateUpdate',
				id:'addedRateUpdate',
				allowBlank : false
			},
			{
				xtype:'codefield',
				codeTableName:'GJB062',
				emptyText:'请选择费率单位...',
				name:'rateUnitAddedUpdate',
				id:'rateUnitAddedUpdate',
				readOnly:true,
				flex:1,
				value : "{'id':'1', 'text':'元/工时'}"
            }]
        },
		{
			xtype:'textfield',
			name:'rateIdUpdate',
			id:'rateIdUpdate',
			hidden:true,
			hideLabel :true
		},
		{
			xtype:'datefieldex',
			fieldLabel:'生效时间',
			blankText : '生效时间不能为空！',
			allowBlank : false,
			name:'efficientDateUpdate',
			id:'efficientDateUpdate',
			editable : false,
			anchor:'95%'
		},
		{
			xtype:'checkboxex',//'checkbox',
			fieldLabel:'是否当前费率',
			name:'currentRateUpdate',
			id:'currentRateUpdate',
			allowBlank : false/*,这里处理存在缺陷，并且没什么用，暂时去掉了。liwei 20120327
			listeners: {
				check:function(){
					
					if(this.checked){
						if(this.disabled){
							
						}else{
							
							Ext.getCmp('efficientDateUpdate').setValue();
							Ext.getCmp('efficientDateUpdate').setMinValue(new Date().add(Date.DAY,-1));
							Ext.getCmp('efficientDateUpdate').setDisabledDates([new Date().add(Date.DAY,-1)]);
						}
					}else{
						Ext.getCmp('efficientDateUpdate').setValue();
						Ext.getCmp('efficientDateUpdate').setMinValue();
						Ext.getCmp('efficientDateUpdate').setDisabledDates([null]);
					}
				}
		}*/
		},
		{
			xtype:'textarea',
			fieldLabel:'说明',
			name:'noteUpdate',
			id:'noteUpdate',
			blankText : '费率说明不能为空！',
			anchor:'95%'
		}]
    });

    // 费率增加弹出窗口
    var rateWindow;
    function personRate(title)
    {
    	if (!rateWindow)
    	{
    		rateWindow = new Ext.Window(
    		{ 	
				name :'addassetspanel',
				title:title,
		        layout: 'fit',
		        closeAction:'hide',
		        resizable : false,
		        modal : true,
		        width : 390,
		        height : 280,
		        buttons:
		        [{
	                text: '确定',
	                iconCls: 'btn_ok',
	                handler: function()
	                {
	                	var gridFLsize = gridFL.store.data.length;
	                	var boo = false;
	                	var currentRate =Ext.getCmp('currentRate').getValue();
	                	for (var i = 0; i < gridFLsize; i = i + 1)
	                	{
	                		var item = gridFL.store.getAt(i);
	                		var isRate = item.get('currentRate');
	                		if (isRate == 'true')
	                		{
	                			boo = 'true';
	                		}
	                	}
	                	
						if (rateForm.form.isValid() && Ext.getCmp("rateUnitAdded").getValue().id != '')
						{
							rateForm.form.submit(
							{
								url:'/Domain/DynamicFormPostAjax.do',
		                        method:'POST',
		                        waitMsg:"正在保存......",
						      	waitTitle:"请稍后",
						      	waitWidth:400, 
		                        params:{serviceUrl:rateForm.submitServiceUrl},
		                        success:function(form, action)
		                        {
		                        	Beidasoft.Bap.alert("成功","保存成功!");
		                        	Ext.getCmp('rateId').setValue('');
		                        	rateForm.form.reset();
				                	gridFL.refresh();
		                        },
		                        failure:function(form, action)
		                        {
		                           
		                        	 Beidasoft.Bap.alert("失败"," 保存失败！");
		                        }
		                    })
		                    
		                    rateWindow.hide();
						}
	                }
				},
				{
	                text: '取消',
	                iconCls: 'btn_cancel',
	                handler: function()
	                {
	                	Ext.getCmp('rateId').setValue('');
	                	rateWindow.hide();
	                }
				}],
                items: [rateForm]
            });
        }
        
    	rateWindow.setTitle(title);
    	rateWindow.show();
    }
    
    // 费率增加表单
    var rateForm = new Beidasoft.Bap.FormPanel(
    {
		submitServiceUrl: 'personManageService/submitRate',    	
		frame :false,
		border:false,
		fileUpload:false,
		name :'rateForm',
		id :'rateForm',
		plain:true,
		labelWidth:80,
		//是否显示等待提示
		waitMsgTarget :true,
        monitorValid:true,
        items:
        [{
			xtype: 'compositefield',
   			fieldLabel:'标准费率',
   			anchor:'95%',
   			items:
   			[{	
				xtype:'numberfield',
				name:'normalRate',
				id:'normalRate',
				allowBlank : false
			},
			{
				xtype:'codefield',
				allowBlank : false,
				codeTableName:'GJB062',
				emptyText:'请选择费率单位...',
				name:'rateUnitNormal',
				id:'rateUnitNormal', 
				editable : false,
				flex:1,
				readOnly : true,
				value : "{'id':'1', 'text':'元/工时'}",
				listeners:
				{
					"blur":function()
					{
						Ext.getCmp("rateUnitAdded").setValue(Ext.getCmp("rateUnitNormal").getValue().id);
					}
				}
			}]
		},
		{
			xtype: 'compositefield',
   			fieldLabel:'加班费率',
   			anchor:'95%',
   			items: 
   			[{
				xtype:'numberfield',
				name:'addedRate',
				id:'addedRate',
				allowBlank : false
			},
			{
				xtype:'codefield',
				codeTableName:'GJB062',
				emptyText:'请选择费率单位...',
				name:'rateUnitAdded',
				id:'rateUnitAdded',
				readOnly:true,
				flex:1,
				value : "{'id':'1', 'text':'元/工时'}"
			}]
		},
		{
			xtype:'textfield',
			name:'rateId',
			id:'rateId',
			hidden:true,
			hideLabel :true
		},
		{
			xtype:'textfield',
			name:'oid',
			id:'oid',
			hidden:true,
			hideLabel :true
		},
		{
			xtype:'textfield',
			name:'pid',
			id:'pid',
			hidden:true,
			hideLabel :true
		},
		{
			xtype:'datefieldex',
			fieldLabel:'生效时间',
			blankText : '生效时间不能为空！',
			allowBlank : false,
			name:'efficientDate',
			id:'efficientDate',
			editable : false,
			anchor:'95%',
			value : new Date()

		},
		{
			xtype:'checkboxex',
			fieldLabel:'是否当前费率',
			name:'currentRate',
			id:'currentRate',
			checked : true,
			allowBlank : false/*,
			listeners: {
				check:function(){
					if(this.checked){
						var sxdate = Ext.getCmp('efficientDate').getValue();
						if(sxdate){
							var today = new Date().add(Date.DAY,-1);
							if(sxdate<today){
								Ext.getCmp('efficientDate').setValue();
							}
						}
						Ext.getCmp('efficientDate').setMinValue(new Date().add(Date.DAY,-1));
						Ext.getCmp('efficientDate').setDisabledDates([new Date().add(Date.DAY,-1)]);
					}else{
						
						Ext.getCmp('efficientDate').setMinValue();
						Ext.getCmp('efficientDate').setDisabledDates([null]);
					}
				}
			}*/
		},
		{
			xtype:'textarea',
			fieldLabel:'说明',
			name:'note',
			id:'note',
			blankText : '费率说明不能为空！',
			anchor:'95%'
		}]
    });
    
    var editJNWin = null; // 新增技能表单
    var czCode = 0; // 0为修改，1为新增
    function editJN(flag)
    {
        czCode = flag;
    	var selNode = bmzwtree.getSelectionModel().getSelectedNode();
        if (selNode == null)
        {
            Beidasoft.Bap.alert('请先选择一个部门!');
            return;
        }
        
        if (gridRY.getSelectionModel().getSelections().length === 0) {
			Beidasoft.Bap.alert('请选择人员！');
			return;
		}
        
        if (!editJNWin)
        {
            editJNWin = new Ext.Window(
            {
                items: [newJNForm],
                layout : 'fit',
                width: 300,
                height: 180,
                buttonAlign: 'right',
                closeAction: 'hide',
                resizable : false,
                autoScroll : true,
                modal : true,
                buttons: 
                [{
                    text: '确定',
                    iconCls : 'btn_ok',
                    handler : function()
                    {
                        if (!newJNForm.form.isValid() || !newJNForm.Checkblank())
                        {
                            return;
                        }
                        
                        // 修改
                        if (czCode == 0)
                        {
                            var data11 = eval('(' + gridJN.getSelected().row.get('A0303') + ')');
                        }
                        
                        for (var n = 0; n < gridJN.store.data.length; n = n + 1)
                        {   
                            var data = eval('(' + gridJN.store.getAt(n).get('A0303') + ')');
                            if (czCode == 1)
                            {
                            	// 新增时的情况
                            	if (Ext.getCmp('A0303ID').getValue().text == data.text)
                            	{
                            		Beidasoft.Bap.alert("已经存在同名的技能");
//                                    gridJN.getSelectionModel().selectRow(n);
                                    return;
                            	}
                            }
                            else
                            {
                            	// 修改时的情况
                            	if (Ext.getCmp('A0303ID').getValue().text == data.text && Ext.getCmp('A0303ID').getValue().text != data11.text)
                            	{
                            		Beidasoft.Bap.alert("已经存在同名的技能");
//                                    gridJN.getSelectionModel().selectRow(n);
                                    return;
                            	}
                            }
                        }
                        
                        var oid = selNode.id;
                        var pid = gridRY.getSelected().id;
                        newJNForm.submitParams = {oid:oid,pid:pid};
                        newJNForm.recordId = null;//sjw add
                        if (czCode == 0)
                        {
                            // 修改
                            newJNForm.recordId = gridJN.getSelected().id;
                        }
                        
                        newJNForm.Submit();
                        editJNWin.hide();
                    }
                },
                {
                    text : '取消',
                    iconCls : 'btn_cancel',
                    handler : function()
                    {
                        editJNWin.hide();
                    }
                }]
            });
        }
        
        editJNWin.show();
        if (czCode == 1)
        {
            editJNWin.setTitle('新增技能');
        }
        else
        {
            newJNForm.LoadData(gridJN.getSelected().id);
            editJNWin.setTitle('修改技能');
        }
        
        newJNForm.form.reset();
    }
    
    // 新增技能
    var newJNForm = new Beidasoft.Bap.FormPanel(
    {
        submitServiceUrl: 'personManageService/submitJN',
        loadDataServiceUrl : 'personManageService/getJN',
        layout : 'form',
        border : false,
        height : 190,
        items :
        [{
            xtype : 'codefield',
	        fieldLabel: '技能名称',
	        codeTableName:'GJB060',
	        name : 'A0303',
	        id : 'A0303ID',
	        editable : false,
	        blankText : '不能为空',
	        hidden:false,
	        checkBox:false,
	        allowBlank:false,
	        readOnly:false,
	        hideLabel:false,
	        anchor:'95%'
	    },
	    {
            xtype : 'codefield',
            codeTableName : 'GJB061',
            fieldLabel: '技能熟练程度',
            name : 'A0304',
            editable : false,
	        blankText : '不能为空',
	        hidden:false,
	        checkBox:false,
	        allowBlank:false,
	        readOnly:false,
	        hideLabel:false,
	        anchor:'95%'
        },
      /*  {
            xtype : 'datefieldex',
            fieldLabel: '开始日期',
            editable : false,
            allowBlank : false,
            blankText : '不能为空',
            name : 'A0301',
            id : 'startDate',
            anchor:'95%',
            hidden:false,
	        readOnly:false,
	        hideLabel:false,
            listeners:{
				select : function(me,newValue,oldValue){
					Ext.getCmp('endDate').setMinValue(newValue);
				},
				focus : function(me){
					Ext.getCmp('startDate').setMaxValue(Ext.getCmp('endDate').getValue());
				}
			}
        },
        {
            xtype : 'datefieldex',
            fieldLabel: '结束日期',
            editable : false,
            name : 'A0302',
            anchor:'95%',
            //allowBlank : false,
            minText : '结束日期不能早于开始日期',
            id : 'endDate',
            listeners:{
				select : function(me,newValue,oldValue){
					Ext.getCmp('startDate').setMaxValue(newValue);
				},
				focus : function(me){
					Ext.getCmp('endDate').setMinValue(Ext.getCmp('startDate').getValue());
				}
			}
        },*/
        {
            xtype : 'checkboxex',
            fieldLabel: '是否当前技能',
            name : 'A0305',
            inputValue:'1',
            disabled:false,
            falseValue:'0',
            hidden:false,
            trueValue:'1',
            hideLabel:false,
            anchor:'95%'
        }]
    });
    
    newJNForm.on('submit', function(action)
    {
        var data = eval('(' + action.response.responseText + ')');
        if (!data.success)
        {
            Beidasoft.Bap.alert(data.msg);
        }
        else
        {
            gridJN.refresh();
        }
     });
    
    // 技能grid
    var gridJN = new Beidasoft.Bap.ObjectGridPanel(
    {
        region: 'center',
        title: '',
        objectName: 'A03',
        objectCriteria: '',
        objectColumns : 'A0303,A0304,A0305',
        orderby : 'A0303',
        autoLoadData:false,
        havePagingToolbar:false,
        tbar:
        [{
         	text: '新增',
         	iconCls: 'table_add',
         	id : 'jnadd',
         	handler: function()
         	{
         		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时新增多个人员技能！');
					return;
        		}
         		
         		editJN(1);
         	}
        },
        {
            text: '修改',
         	iconCls: 'table_edit',
         	id : 'jnedit',
         	handler: function()
         	{
         		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时修改多个人员技能！');
					return;
        		}
         		
         		if (gridJN.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能批量修改人员技能！');
					return;
        		}
         		
         		var selNode = gridJN.getSelected();
         		if (selNode == null)
         		{
                    Beidasoft.Bap.alert('请先选择一个技能!');
                    return;
         		}
         		
         		editJN(0);
         	}
        },
        {
         	text: '删除',
         	iconCls: 'table_delete',
         	id : 'jndelete',
         	handler: function()
         	{
         		if (gridRY.getSelectionModel().getSelections().length > 1)
        		{
					Beidasoft.Bap.alert('不能同时删除多个人员技能！');
					return;
        		}
         		
         		var selNode = gridJN.getSelected();
                if (selNode == null)
                {
                    Beidasoft.Bap.alert('请先选择一个技能!');
                    return;
                }
                
         		gridJN.deleteRecord();
         	}
        }],
        listeners:
        {
            rowdblclick:function(grid, rowIndex, e)
            {
                gridJN.modifyRecord();	
            }
        }
    });
  		
	var tabs = new Ext.TabPanel(
	{
		height: 250,
		region:'south',
		activeTab: 0,
		items:
		[{	
            title:'费率',
			layout:'fit',
			items:[gridFL]
		},{	
            title:'技能',
			layout:'fit',
			items:[gridJN]
		}]
	});
	
	// simple array store'
	//新增查询人员功能
	var querystore = new Ext.data.ArrayStore({
	    fields: ['colName', 'displayName'],
	    data : [['A0103','员工姓名'],['A0101','员工编号'],['A0102','登录名'],['A0108','单位名']]
	});
	
	var searchPanel = new Ext.Panel({
		region:'north',
		height:50,
		frame:true,
		bodyStyle: 'padding:5px',
	    layout: 'form',
		broder:false,
		items:[{
			layout:'column',
			items:[{
				columnWidth : '.35',
				layout : 'form',
				labelWidth:70,
				items:[{
					xtype:'textfield',
					id:'cx',
					anchor:'95%',
					fieldLabel:'查询内容',
			        emptyText:'默认按登录名查询',
			        enableKeyEvents: true
				}]
			},{
				columnWidth : '.35',
				layout : 'form',
				labelWidth:70,
				items:[   
		            new Ext.form.ComboBox({
		       	    store: querystore,
		       	    id:'cxbox',
		       	    anchor:'95%',
		       	    fieldLabel: '查询条件',
		       	    displayField:'displayName',
		       	    typeAhead: true,
		       	    editable:false,
		       	    mode: 'local',
		       	    triggerAction: 'all',
		       	    emptyText:'查询条件(默认是登录名)...',
		       	    selectOnFocus:true
		       	})]
			}, {
				columnWidth : '.1',
				layout : 'form',
				items:[{
					xtype:'button',
					text:'查 询',
					iconCls:'table_find',
			    	handler:function(){
			    		var cxValue = Ext.escapeRe(Ext.getCmp("cx").getValue().trim());
			    		var cxtj = Ext.escapeRe(Ext.getCmp("cxbox").getValue().trim());
			    		// 去掉输入进来的空格
			    		realNameValue = removeSpace(cxValue);
			    		var cxzd = "";
			    		if("员工姓名" == cxtj){
			    			cxzd = "A0103";
			    			if(realNameValue.length==2){
			    				realNameValue=realNameValue.substring(0,1)+' '+realNameValue.substring(1,2);
			    			}
			    		}else if ("登录名" == cxtj){
			    			cxzd ="A0102";
			    		}else if ("单位名" ==cxtj){
			    			cxzd ="A0108";
			    		}else if("员工编号" == cxtj){
			    			cxzd = "A0101";
			    		}
			    		
			    		var sqlWhere = "";
			    		var orderString ="";
			    		if(cxValue ==""){
			    			sqlWhere = " 1=1  ";
			    			orderString = "A0101";
			    			Beidasoft.Bap.alert("请输入查询内容！");
			    			return;
			    		}else if(cxValue != ""&& cxtj == ""){
			    			sqlWhere = getSearchSql("A0102", realNameValue);
			    			orderString = "A0102";
			    		}else if(cxValue != ""&& cxtj != ""){
			    			sqlWhere = getSearchSql(cxzd, realNameValue);				    			
			    			if("员工姓名" == cxtj){
			    				sqlWhere +=' or '+ getSearchSql(cxzd, removeSpace(cxValue));
			    			}
			    			orderString = ""+cxzd+"";
			    		}

			    		gridRY.serviceUrl = 'personManageListService/findPersonGrid/'+sqlWhere+'/'+orderString;
			    		gridRY.refresh();
			    	}
				}]
			},{
				columnWidth : '.1',
				layout : 'form',
				items:[{
					xtype:'button',
					text:'重 置',
					iconCls:'table_reset',
			    	handler:function(){
			    		Ext.getCmp('cx').setValue();
			    		Ext.getCmp('cxbox').setValue();
			    		//重置 清空所有grid
			    		gridRY.getStore().removeAll();
		      			gridFL.getStore().removeAll();
		      			gridJN.getStore().removeAll();
			    	}
				}]
			}]
			
		}]
	});
    var panel = new Ext.Panel(
    {
        items:
        [bmzwtree,
        {	
        	region:'center',
        	layout:'border',
           	items:[searchPanel,gridRY,tabs]
        }],
        layout: 'border',
        renderTo: 'mydiv',
        border: false,
        plugins: [new Ext.ux.plugins.FitToParent()]
    });
});

