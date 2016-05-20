
var deptform = new mini.Form("#deptform");


//------公用及校验性的方法------------

//密码复杂度判断
function keyUp(event){
	var password = mini.get("password").getValue();
	var tsy = null;
	if(event=="edit")
		tsy = document.getElementById("Emmjy");
	else 	
	    tsy = document.getElementById("mmjy");

	var aLvTxt = ['','低','中','高'];
	var lv = 0;
	if(password.match(/[a-z]/g)) {lv++;}
	if(password.match(/[0-9]/g)) {lv++;}
	if(password.match(/(.[^a-z0-9])/g)) {lv++;}
	if(password.length < 6) {lv=0;}
	if(lv > 3) {lv=3;}
	tsy.className = 'strengthLv' + lv;
	tsy.innerHTML = aLvTxt[lv];
	}

//密码一致校验
function checkPassword(){
	var password = mini.get("password").getValue();
	var rpassword = mini.get("rpassword").getValue();
	if(password!=rpassword){
		document.getElementById("RY0102ERROR").innerHTML="两次输入的密码必须一致!";
		//mini.alert('两次输入的密码必须一致!');
		return;
	}
}

//密码是否符合配置的规则校验
function checkConfigpassWord(newPwd){
	var prompt;
	$.ajax({
		url:'/bopmain/user/rygl/checkPassword?theme=none',
		type:'get',
		data:{password:newPwd},
		async: false,
		success:function(text){
			var data = mini.decode(text);
			if (data.info) {
				prompt ="yes";
			} else {
				prompt = data.prompt;
			}
		}
	})
	
	return prompt;
}

//------------校验用户名是否重复----------------
function checkUpdateNode(){
	var value = updateNodeText.getValue();
	var id = tree.getSelectedNode().id;
	$.ajax({
		url: "/bopmain/user/rygl/isupddeptname/" + id+ "/"+ value+ "?theme=none",
		type: "post",
		success: function (text) {
			var data = mini.decode(text);    
			if (data.info == "yes") {
				document.getElementById("BM0101EORROR").innerHTML="";
			} else {
				document.getElementById("BM0101EORROR").innerHTML=data.info;
			}
		}
	});
}

function successed(text, time){
	  mini.showTips({
        content: text,
        state: "success",
        x: "center",
        y: "top",
        timeout: 3000
    });
}

//-----------------显示一个部门员工信息-----------------
function onNodeClick(e) {
	var node = mini.get("deptTree").getSelectedNode();
	grid.load({departmentId : node.id});
}

//-----------------新增部门-----------------
var addNodeText = mini.get("BM0101");//文本框
var temp;


function checkNode(){//检查下级部门名是否同名
	var value = addNodeText.getValue();
	temp = value;
	var node = tree.getSelectedNode();
	var id = null;
	if (node === null) {
		id = '00000000-0000-0000-0000-000000000000'; 
	} else {
		id = node.id;
	}
	$.ajax({
		url: "/bopmain/user/rygl/isDeptName/" + id+ "/"+ value+"?theme=none",
		type: "get",
		success: function (text) {
			 var data = mini.decode(text);   
			if (data.info == "yes") {
	    		document.getElementById("BM01011EORROR").innerHTML="";
			} else {
				document.getElementById("BM01011EORROR").innerHTML=data.info;
			}
		}
	});
}

function onAddNode(id){
	var node = mini.get("deptTree").getSelectedNode();
	if(node.id == null) {
		mini.showTips({
		    content: '请选择一个部门',    
		    state: 'info',
		});
		return ;
	}
	
	var win3 = mini.get("win3");
	win3.show();
}
//根据部门名称选中该部门
function selectNodeDeptByName(deptname,parentid){
	$.ajax({
		url: "/bopmain/user/rygl/selectnodedeptbyname/"+deptname+"/" + parentid+"?theme=none",
		type: "get",
		success: function (text) {
			var data = mini.decode(text);   
			tree.selectNode(data.info);
			tree.expandPath(data.info);
		}
	});
}
function onAddNodeYes(){
	//checkNode();
    deptform.validate();
	if (deptform.isValid() == false) return;
	var data = deptform.getData();  //获取表单多个控件的数据
	var json = mini.decode(data);   //序列化成JSON
	var node = tree.getSelectedNode();
	var id = null;
	if (node === null) {
		id = '00000000-0000-0000-0000-000000000000';
	} else {
		id = node.id;
	}
	$.ajax({
		url: "/bopmain/user/rygl/addDept/"+id+"?theme=none",
		data: json,
		type: "post",
		success: function (text) {
			var data = mini.decode(text);
			if (data.success) {
				tree.load( "/bopmain/user/rygl/deptlist?theme=none");
				deptform.reset();
				if (confirm("继续添加？")) {
					onAddNode(id);
					addNodeText.focus();//定位光标
				}else {
					selectNodeDeptByName(temp,id);
					win3.hide();
				}
			} else {
				return;
			}
		}
	});
}

function onKeyEnter1(e){
	onAddNodeYes();
}
function onAddNodeNo(){
	//form3.clear();
	document.getElementById("BM01011EORROR").innerHTML="";
	win3.hide();
}
//------------------删除部门------------
function onDelNode(){
    var node = tree.getSelectedNode();
	 if(node==null) {
		 successed("请选择一个要删除的记录",1000);
        	return;
        }
	if(!tree.isLeaf(node.id)){
		successed("该节点下已有部门，不可删除",1000);
    		return;
		}
	  var flag = false; // 查看当前节点以及其下的节点下是否有工作产品
      if (node) {
          if (confirm("确定删除选中分组?")) {
              tree.removeNode(node);
              $.ajax({
                  url: "/bopmain/user/rygl/deldept/"+node.id+"?theme=none",
                  type: "post",
                  success: function (text) {
                	  var data = mini.decode(text);   
                	  successed(data.info,500);
                  },
                  error: function (jqXHR, textStatus, errorThrown) {
                	  successed(jqXHR.responseText,1000);
                  }
              });
          }
      }
}

//------------------修改部门-----------------
var updateNodeText = mini.get("BM0101");//文本框

//-------------------------添加员工-------------------------
var addRowText = mini.get("RY0102");//登录名文本框
var userwin = mini.get("userwin");
var edituserwin = mini.get("edituserwin");
var userform;

function checkLoginName(flag){
	var value = addRowText.getValue();
	var reqflag = true;
	if(flag=="edit"){
		var loginname = grid.getSelected().loginname;
		if(loginname==value){
			reqflag = false;
		}
	}
	if(reqflag){
		$.ajax({
			url: "/bopmain/user/rygl/isloginname/" + value + "/?theme=none",
			type: "get",
			success: function (text) {
				var data = mini.decode(text);
				if (data.info != "yes") {
					document.getElementById("RY0102ERROR").innerHTML=data.info;
				} else{
					document.getElementById("RY0102ERROR").innerHTML="";
				}
			}
		});
	}
}

//---------增加人员--------
function addRow(){
	var node = tree.getSelectedNode();
	if (node == null) {
		successed("请选择部门",1000);
		return;
	}
	userwin.show();
	userform = new mini.Form("#userform")
}

function addRowYes(){
	checkLoginName("add");
	var newPwd = mini.get("password").getValue();
    userform.validate();
    if (userform.isValid() == false) return false;

    var prompt = checkConfigpassWord(newPwd);

    if(prompt!="yes"){
    	mini.alert(prompt);
    	return ;
    }

	var data = userform.getData();
	var json = mini.decode(data);
	var node = tree.getSelectedNode();
	$.ajax({
	    url: "/bopmain/user/rygl/adduser/"+node.id+"/add?theme=none",
	    data: json,
	    type: "post",
	    success: function (text) {
	    	var data = mini.decode(text);
	    	if (data.success) {
				userform.reset();
				grid.load({flag:'queryUserDataOrderNew',departmentId:node.id})//选中部门，显示 该部门下最新添加的数据
				if (confirm("添加成功，是否继续添加")) {
					addRow();
					mini.get("RY01011").focus();//光标定位在第一个文本框
				}else {
					userform.reset();
					userwin.hide();
		    		grid.reload();
				}
			} else {
				return;
			}
	    }
	});
}

function onKeyEnter3(e) {
	addRowYes();
}
function addRowNo(){
	userform.reset();
	userwin.hide();
}

//-------------------------删除员工 -------------------------
function removeRow(){
	var rows = grid.getSelecteds();
	if(rows.length > 0) {
		if (confirm("确定删除选中记录？")) {
			var ids = [];
			for ( var i = 0; i < rows.length; i++) {
				var r = rows[i];
				if (r.id == "") {
					grid.removeRow(r, false);
				} else {
					ids.push(r.id);
				}
			}
			var id = ids.join(',');
			$.ajax({
				url: "/bopmain/user/rygl/deleteuser/"+id+"?theme=none",
				type: "post",
				success: function (text) {
					var data = mini.decode(text);
					successed(data.info,500);
					grid.reload();
				}
			});
		}
    }else{
    	successed("至少选择一条记录",1000);
    	return;
    }
}

//-------------------查询--------------- 
function onKeyEnter(e) {
	findNode();
}
function findNode(){
	var node = tree.getSelectedNode();
	var key = mini.get("key").getValue();
	if (key == "") {
		return;
	}
	if (node == null) {//全局查询
		grid.load({departmentId : 'null', username:key});
	} else {//按部门查询
		var departmentId = node.id;
		grid.load({departmentId:departmentId,username:key});
	}
	grid.deselectAll();
	mini.get("key").setValue("");
}

//------------------------------显示所有人员信息----------------------
function showAll(){
	tree.selectNode("");
	grid.deselectAll();
	tree.collapseAll();
	grid.load( {departmentId : '0'});
}


//----------------人员修改-----------------
function editRowYes(){
	checkLoginName("edit");
	var newPwd = mini.get("password").getValue();
	edituserform.validate();
    if (edituserform.isValid() == false) return false;
    

    var prompt = checkConfigpassWord(newPwd);
    if(prompt!="yes"){
    	mini.alert(prompt);
    	return ;
    }

	var data = edituserform.getData();
	var json = mini.decode(data);
	var node = tree.getSelectedNode();
	$.ajax({
	    url: "/bopmain/user/rygl/adduser/"+node.id+"/edit?theme=none",
	    data: json,
	    type: "post",
	    success: function (text) {
	    	var data = mini.decode(text);
	    	if (data.success) {
				userform.reset();
				grid.load({flag:'queryUserDataOrderNew',departmentId:node.id})
				if (confirm("添加成功，是否继续添加")) {
					addRow();
					mini.get("RY01011").focus();
				}else {
					edituserform.reset();
					edituserwin.hide();
		    		grid.reload();
				}
			} else {
				return;
			}
	    }
	});
}

function onKeyEnter4(e) {
	editRowYes();
}

function editRowNo(){	
	edituserform.reset();
	edituserwin.hide();
}

function editRow(){
	var rows = grid.getSelecteds();
	if (rows.length > 1) {
		successed("一次仅能修改一条记录",1000);
		return;
	}
	var row = grid.getSelected();
	if(row==null) {
		successed("请选择一条记录",1000);
  	return;
  }
	var row = grid.getSelected();
	var fileid = row.filedid;
	edituserwin.show();
  $.ajax({
  	url: "/bopmain/user/rygl/loaduser/"+row.loginname+"?theme=none",
  	type: "get",
  	success: function (text) {
  		debugger;
  		var data = mini.decode(text);
  		edituserform.setData(data);
  	}
  });
}

//------------部门修改-------------
var form4;
var win4;
function oneditNodeYes(){
	checkUpdateNode();
	form4.validate();
	if (form4.isValid() == false) return;
	var node = tree.getSelectedNode();
	var data = form4.getData();      //获取表单多个控件的数据
	var json = mini.encode(data);   //序列化成JSON
	$.ajax({
	    url: "/bopmain/user/rygl/editdept/"+node.id+"?theme=none",
	    data: { data: json },
	    type: "post",
	    success: function (text) {
	    	 var data = mini.decode(text);    //反序列化成对象
	    	if(data.info=="YES"){
	    		win4.hide();
	    		successed("修改成功!",500);
	    		tree.load( "/bopmain/user/rygl/deptlist?theme=none");
	    		tree.selectNode(node.id);
	    	}else{
	    		return;
    		}
	    }
	});
}

function oneditNodeNo(){
	document.getElementById("BM0101EORROR").innerHTML="";
	win4.hide();
}

function oneditNode(){
    var node = tree.getSelectedNode();
    if(node==null) {
    	successed("请选择要修改的部门",1000);
    	return;
    }
    win4.show();
    form4 = new mini.Form("#form4");
    updateNodeText.focus();
	  $.ajax({
	      url: "/example/rygl/loaddept/"+node.id+"?theme=none",
	      type: "get",
	      success: function (text) {
	    	   var data = mini.decode(text);    //反序列化成对象
	          form4.setData(mini.decode(data.data));             //设置多个控件数据
	      }
	  });
}

function onKeyEnter2(e){
	oneditNodeYes();
}


//-------以前留下的暂时没用的------
function showDeptNameByRow(){   //根据员工ID显示部门名
	var rows = grid.getSelecteds();
	if (rows.length > 1) {
		return;
	}
	var row = grid.getSelected();
	if(row==null)return;
	var id = row.id;
	$.ajax({
		url: "/bopmain/user/rygl/showdeptnamebyrow/"+id+"?theme=none",
		type: "POST",
		success: function (text) {
			var data = mini.decode(text);
			tree.collapseAll();
			tree.selectNode(data.info);
			tree.expandPath(data.info);//展开节点路径
		}
	});
}


//---------------显示详细信息--------------------------
function onShowRowDetail(e) {
	var row = e.record;
  //将editForm元素，加入行详细单元格内
  var td = grid.getRowDetailCellEl(row);
  td.appendChild(editForm);
  editForm.style.display = "";

  var form = new mini.Form("form5");//表单加载员工信息
  if (grid.isNewRow(row)) {                
      form.reset();
  } else {
      grid.loading();
      $.ajax({
      	url: "/bopmain/user/rygl/loaduser/"+row.id+"?theme=none",
      	type:"get",
          success: function (text) {
              var data = mini.decode(text);
              form.setData(mini.decode(data.data));                        
              grid.unmask();
          }
      });
  }
  showDeptNameByRow();
}

//-------------增加附件的方法-----------
function loadfile(){
	var row = grid.getSelected();
	var fileid = row.filedid;
	link = "/domain/downloadfileajax.cmd?fileid=" +  fileid;
	document.location.href= link;
}


function refreshGrid() {
	generateGridFilter();
	grid.load();
}

