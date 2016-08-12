<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>方案管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<span>年度：</span><input class="mini-combobox" id="zfnd" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getCBData/nd?theme=none"/>
	<span>状态：</span><input class="mini-combobox" id="fazt" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" data="[{id:'0',text:'未生成'},{id:'1',text:'已生成'}]"  />
</div>
<div class="mini-fit">
	<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url=""  idFiled="id"  allowResize="true" >
	    <div property="columns">
	        <div type="indexcolumn"></div>
	        <div field="id" visible="false">id</div>
	        <div name="mc" field="mc" width="60" >任务名称</div>
	        <div field="zfyf" width="60">执法月份</div>
	        <div field="zfryzs" width="60" align="right">执法人员总数</div>
	        <div field="cyzfrs" width="60" >参与执法人员数</div>
	        <div field="cycczs" width="60" >参与抽查企业总数</div>
	        <div field="ccqys" width="60" >抽查企业数</div>
	        <div field="zffa" width="80" headerAlign="center">执法方案</div>
	        <div field="wtjfas" width="50" headerAlign="center" >未提交方案数</div> 
	    </div>
	</div>
</div>
<script >
mini.parse();
var zfnd = mini.get("zfnd");
zfnd.select(0);
var zfzt = mini.get("fazt");
zfzt.select(0);

var grid = mini.get("datagrid");
var url = "/ssj/ssjscheme/SchemeInfoShow/getFALBData/" + zfnd.value+"/"+zfzt.value + "?theme=none";
grid.setUrl(url);
grid.load();

 
valueChange = function(){
	var nd = mini.get("zfnd").value;
	var zt = mini.get("fazt").value;

	url = "/ssj/ssjscheme/SchemeInfoShow/getFALBData/"+nd+"/"+zt+"?theme=none";
	grid.setUrl(url);
	grid.reload();
}

//生成方案
createFa=function(){
	var faid = grid.getSelected().id;
    $.ajax({
    	url:'/ssj/ssjScheme/CreateScheme/isRepertCreate/'+faid,
    	type:'get',
    	success:function(e){
    		var info = mini.decode(e);
    		if(info.flag==4){
    			 mini.confirm("该任务已经生成方案，重新生成将产生记录,确定重新生成？", "确定",
    			            function (action) {
    			                if (action == "ok") {
    			                	createRepert('replace');
    			                } else {

    			                }
    			            }
    			        );
 	 		}else if(info.flag==3){
 	 			createRepert('new');
    		}else{
    			alert(info.text);
    		}
    	}
    });
}

createRepert = function(e){
    mini.mask({
        el: document.body,
        cls: 'mini-mask-loading',
        html: '方案生成中，请稍等...'
    });
    var faid = grid.getSelected().id;

	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/createSchemeData/'+faid,
		type:'get',
		data:{isreplace:e},
		success:function(e){
			if(e=="seccess"){
				mini.unmask(document.body);
				gridLoad(faid);
				viewFa("sc");
			}else if(e=="false"){
				alert("有未设置人员或企业数的区县请先设置！");
			}
			//打开窗口展现生成的页面
		}
	});
}

viewFa = function(e){
	var faid = grid.getSelected().id;
	mini.open({
		url:'/ssj/ssjScheme/viewScheme2.jsp?theme=2&faid='+faid+'&flag='+e,
		showMaxButton: false,
	    allowResize: true,
	    title: '方案浏览',
	    width: 1000,
	    height: 700,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    	
	    }	
	});
	
}

showRy = function(e){
	var faid = grid.getSelected().id;
	mini.open({
		url:'/ssj/ssjscheme/showPerson.jsp?theme=2&faid='+faid+"&flag="+e,
		showMaxButton: false,
	    allowResize: true,
	    title: '执法人员情况',
	    width: 800,
	    height: 600,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    	gridLoad();
	    }	
	});
}

showOrg = function(e){
	var faid = grid.getSelected().id;
	mini.open({
		url:'/ssj/ssjscheme/showOrg.jsp?theme=2&faid='+faid+"&flag="+e,
		showMaxButton: false,
	    allowResize: true,
	    title: '执法人员情况',
	    width: 800,
	    height: 600,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	    },
	    ondestroy: function (action) {
	    
	    }	
	});
}


gridLoad = function(value){
	grid.setUrl(url);
	grid.reload();
}
/* showRy = function(){
	var faid = grid.getSelected().id;
	mini.open({
		url:'/ssj/ssjscheme/SchemeInfoShow/getRYcount?theme=2&faid='+faid,
		showMaxButton: false,
	    allowResize: true,
	    title: '执法人员情况',
	    width: 800,
	    height: 600,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    
	    }	
	});
} */




</script>
</body>
</html>