<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="com.bop.web.PathUtil" %>

<html>
<head>
  <title>权限管理</title>
  <meta name="decorator" content="extwithbdsoft" >
</head>
<body>
	<script type="text/javascript" src="role.js"></script>
	<script type="text/javascript" src="roleauthor.js"></script>
	<script type="text/javascript" src="roleuser.js"></script>
	
	<script>
		Ext.onReady(function() {
			setFunction2Role = function(rid, obj) {
				Beidasoft.Bap.Ajax.request({
					url: '/bopmain/authorajax.cmd?op=set&id='
							+ rid + '&fid=' + obj.id + '&check=' + obj.checked,
					success: function() {}
				});
			}
			setCommand2Role = function(rid, obj) {
				var p = document.getElementById(obj.attributes.getNamedItem('pid').value);
				if(obj.checked && p.checked == false) {
					p.checked = true;
					setFunction2Role(rid, p);
				}
				
				Beidasoft.Bap.Ajax.request({
					url: '/bopmain/authorajax.cmd?op=setcmd&id='
							+ rid + '&cid=' + obj.id + '&check=' + obj.checked,
					success: function() {}
				});
			}
			checkCids = function(rid, obj) {
				//var cids = obj.cid.split(',');
				var ciddd = obj.attributes.getNamedItem('cid').value;
				var cids = ciddd.split(',');
				
				var table = document.getElementById('input_table_id');
				for(var i = 0; i < cids.length; i ++) {
					var cid = cids[i];
					var func = document.getElementById(cid);
					
					if(func.disabled) continue;
					
				 	if(func.checked != undefined && func.checked != obj.checked) {
						func.checked = obj.checked;
						setFunction2Role(rid, func);
					}else if(func.checked == undefined){
						var trlist = table.getElementsByTagName('TR');
						for(var i=0;i<trlist.length;i++){
							var tr = trlist[i];
							var tdlist = tr.getElementsByTagName('TD');
							
							for(var j=0;j<tdlist.length;j++){
								var td = tdlist[j];
								var inoutTag = td.getElementsByTagName('INPUT');
								
								for(var k=0;k<inoutTag.length;k++){
									var input = inoutTag[k];
									
							 		if(ciddd.indexOf(input.id) != -1 && input.id !='' && input.checked != obj.checked){
							 				input.checked = obj.checked;
											setFunction2Role(rid, input);
									}
								}
							}
						}
					} 
	
				}
			}
			setUser2Role = function(rid, obj) {
				Beidasoft.Bap.Ajax.request({
					url: '/bopmain/authorajax.cmd?op=setuser&id='
							+ rid + '&uid=' + obj.id + '&check=' + obj.checked,
					success: function() {}
				});
			}

			var rolePanel = new Ext.Panel({
				title : '角色列表',
				region : 'west',
			    width: '19%',
			    border: true,
			    split: true,
				minSize: 175,
			    maxSize: 400,
			    autoScroll:true,
			    collapsible:true,
				activeTab: 0,
				enableDD: true,
				items: [sysrole]
			});
			rolePanel.on('tabchange',function(tabPanel, tab){
				if(tab.id=="sysrole"){
					//显示
					authorPanel.unhideTabStripItem(1);
				}else{
					authorPanel.hideTabStripItem(1);
					authorPanel.setActiveTab(0);
				}
			});
			authorPanel = new Ext.TabPanel({
				region: 'center',
				title: '角色功能权限ss',
				activeTab: 1,
				items: [roleFunctionPanel, roleUserPanel]
			});
			
	        var panel = new Ext.Panel({
	        	renderTo: 'mydiv',
	        	layout: 'border',
	        	items: [rolePanel, authorPanel],
	        	plugins: [new Ext.ux.plugins.FitToParent()]
	        });
	        
	        authorPanel.setActiveTab(0);
		});
	</script>
	<div id="mydiv" style="height:100%;width:100%;">
	</div>
</body>
</html>