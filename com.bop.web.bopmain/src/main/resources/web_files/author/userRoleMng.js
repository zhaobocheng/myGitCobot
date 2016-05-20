mini.parse();
getRoleAuthor('715a7542-e7e7-4c52-9d08-f45f2d4f4207')

function getRoleAuthor(rid){
	$.ajax({
		url : "/bopmain/AuthorAjax.cmd?op=get&id=" + rid,
		type : "post",
		async : false,
		success : function(data) {
			$("#jsgnqx").html("");
			$("#jsgnqx").html(data);
			}
	});
}

function setFunction2Role(rid, obj) {
	$.ajax({
		url: '/bopmain/authorajax.cmd?op=set&id='
				+ rid + '&fid=' + obj.id + '&check=' + obj.checked,
		success: function() {}
	});
}

function setCommand2Role(rid, obj) {
	var p = document.getElementById(obj.attributes.getNamedItem('pid').value);
	if(obj.checked && p.checked == false) {
		p.checked = true;
		setFunction2Role(rid, p);
	}
	
	$.ajax({
		url: '/bopmain/authorajax.cmd?op=setcmd&id='
				+ rid + '&cid=' + obj.id + '&check=' + obj.checked,
		success: function() {}
	});
}

function checkCids(rid, obj) {
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

function adds() {   
    var items = grid1.getSelecteds();
    grid1.removeRows(items);
    grid2.addRows(items);
}
function addAll() {
    var items = grid1.getData();       
    grid1.removeRows(items);
    grid2.addRows(items);
}
function removes() {
    var items = grid2.getSelecteds();
    grid2.removeRows(items);
    grid1.addRows(items);
}
function removeAll() {
    var items = grid2.getData();
    grid2.removeRows(items);
    grid1.addRows(items);
}

function onAddNode(){
	var addRoleTypeWindow = mini.get("addRoleTypeWindow");
	addRoleTypeWindow.show();
}

function decidedAdd(){
	var winAddRole = mini.get("addRoleTypeWindow");
	winAddRole.hide();
	var roleType = mini.get("roleType").getValue();
	
	if(roleType == "2"){
		var mngerRoleWindow = mini.get("mngerRoleWindow");
		mngerRoleWindow.show();
	}else{
		var userRoleWindow = mini.get("userRoleWindow");
		userRoleWindow.show();
	}
}

function cancelAdd(){
	var addRoleTypeWindow = mini.get("addRoleTypeWindow");
	addRoleTypeWindow.hide();
}

function onOrganizationEdit(){
	var organizationTreeWindow = mini.get("organizationTreeWindow");
    var tree = mini.get("organizationTree");
    tree.load("/bopmain/author/authorajaxcommand/getorganizationtree?theme=none");
	organizationTreeWindow.show();
}

function organizationTreeOk(){
	var btnEdit = mini.get("btnEditOrganization");
	var tree = mini.get("organizationTree");
	var nodes = tree.getCheckedNodes();
    var ids = [], texts = [];
    for (var i = 0, l = nodes.length; i < l; i++) {
        var node = nodes[i];
        ids.push(node.id);
        texts.push(node.text);
    }
    btnEdit.setValue(ids.join(","));
    btnEdit.setText(texts.join(","));
    
    var organizationTreeWindow = mini.get("organizationTreeWindow");
	organizationTreeWindow.hide();
}

function organizationTreeCancel(){
	var organizationTreeWindow = mini.get("organizationTreeWindow");
	organizationTreeWindow.hide();
}