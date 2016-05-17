Ext.onReady(function(){
   
	var updateaction = new Ext.Action(
		    {
		        text: '升级',
		        handler: function()
		        {	        	
		        	var rows = moduleGrid.getSelectionModel().getSelections();
					if (rows.length == 0)
					{
						Ext.popAlert.msg('警告', '最少选择一个模块进行升级!');
					}
					else
					{
						// 模块名称
						var selectedModuleName='';
						// 是否已经升级
						var isUpdated='false';
						var msgBox = null;
						for (var i = 0; i < rows.length; i++)
						{ 
							var row = rows[i];
							
							selectedModuleName= row.data.moduleName;							
							isUpdated = row.data.isTrue
							
							// 如果没有升级，进行升级
							if (isUpdated=='false')
							{
								$.ajax({
							        url: '/bopmain/mainpage/upgrademodule/' + selectedModuleName + "?theme=none",
							        type : 'post',
							        cache: false,
							        async: false,
							        success: function (text) {
										if(msgBox!=null) msgBox.hide();
										if(text){
											row.set("status",'<img src="/bopmain/images/label_suc.gif"></img>');
											row.set("isTrue", "true");
											row.commit();
										}else{
											Ext.Msg.alert('失败', selectedModuleName +'升级失败!<br>失败原因：'+text);
										}
							        },
							        error: function (jqXHR, textStatus, errorThrown) {
							        }
							    });
							}
												
					  	}
					}	
		        }
		    });
	var goaction = new Ext.Action(
		    {
		        text: '软件工程管理集成平台',
		        handler: function()
		        {	        	
		        	location.href = logonUrl;	
		        }
		    });
	
    var moduleGrid = new Beidasoft.Bap.GridPanel({
    	title : '数据库结构需要升级模块信息',
    	checkBoxType:2,
    	region: 'center',
    	border: true,
    	url:'/bopmain/mainpage/upgraders?theme=none',
    	tbar:[updateaction, goaction]
    });
    
	var mainPanel = new Ext.Panel({
		items:[moduleGrid],
		layout: 'border',
		renderTo:'updateList',
		border: false,
		height : 500
	});

});