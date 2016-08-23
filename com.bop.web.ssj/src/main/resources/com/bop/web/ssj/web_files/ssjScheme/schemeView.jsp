<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法方案浏览</title>
</head>
<body>
<div style="padding:5px;10px;5px;0">
	<span>年度：</span><input class="mini-combobox" id="zfnd" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getCBData/nd?theme=none"/>
	<span>任务名称：</span><input class="mini-textbox" id="rwmc" name="rwmc" style="width:150px;" onvaluechanged="valueChange"/>	
	<a class="mini-button" id="find" iconCls = "icon-find"  onclick="search()">查找</a>
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()">导出Excel</a>
</div>
<div class="mini-fit">
	<div id="treegrid1" class="mini-treegrid" style="width:100%;height:100%;"     
    url="" showTreeIcon="true" treeColumn="mc" idField="id" parentField="parentid" resultAsTree="false"  
    allowResize="false" expandOnLoad="true" showPager="false" >
	    <div property="columns">
	        <div type="indexcolumn" headerAlign="center" >序号</div>
	        <div field="id" visible="false">id</div>
	        <div field="yf" visible="false">yf</div>
	        <div name="mc" field="mc" width="60" headerAlign="center" >任务名称</div>
	        <div field="qx" width="60" headerAlign="center" align="center">区县</div>
	        <div field="zfryzs" width="60" headerAlign="center"  align="center">执法人员总数</div>
	        <div field="cyzfrs" width="60" headerAlign="center"  align="center">参与执法人员数</div>
	        <div field="cycczs" width="60" headerAlign="center"  align="center">参与抽查企业总数</div>
	        <div field="ccqys" width="60" headerAlign="center"  align="center">抽查企业数</div>
	        <div field="cz" width="80" headerAlign="center"  align="center">是否提交方案 </div>
	        <div field="fqfas" width="50" headerAlign="center"  align="center">废弃方案数</div>
	        <div field="sfgs" width="50" headerAlign="center"  align="center">是否公示</div>  
	    </div>
	</div>
</div>

<script type="text/javascript">
var Bits=[{id:0,text:'否'},{id:1,text:'是'}];

mini.parse();
var zfnd = mini.get("zfnd");
zfnd.select(0);
var rwmc=mini.get("rwmc").value;

var grid = mini.get("treegrid1");
var url = "/ssj/ssjscheme/SchemeResult/getFALBData?zfnd=" + zfnd.value + "&rwmc="+rwmc+"&theme=none";
grid.setUrl(url);
grid.load();



function search(){
	var mc=mini.get("rwmc").value;
	var zfnd=mini.get("zfnd").value;
	var url2 = "/ssj/ssjscheme/SchemeResult/getFALBData?zfnd=" + zfnd + "&rwmc="+mc+"&theme=none";
	
	grid.load(url2);
	/* grid.load({rwmc:rwmc,month:month}); */
	
}

valueChange = function(){
	search();
}

importExc = function(){
	var faid = mini.get("zfnd").value;
	rwmc=mini.get("rwmc").value;
	grid.loading("正在导出，请稍后......");
	var columns = grid.columns;
	var json = mini.encode(columns);

	$.ajax({
		url:'/ssj/ssjscheme/ExportExcle/SchemeViewExportExcel?zfnd='+faid+'&rwmc='+rwmc+'&theme=none',
		type:'get',
		data:{gridcolmun:json},
		success:function(e){
			var inf = mini.decode(e);
			if(inf.flag){
				location.href = decodeURI("/ResourceFiles"+inf.path);
			}else{
				alert("导出失败咧！");
			}
			grid.reload();
		}
	});	
}

showRy = function(e){
	var faid = grid.getSelected().parentid;

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
	    
	    }	
	});
}

showOrg = function(e){
	var faid = grid.getSelected().parentid;
	mini.open({
		url:'/ssj/ssjscheme/showOrg.jsp?theme=2&faid='+faid+"&flag="+e,
		showMaxButton: false,
	    allowResize: true,
	    title: '参与企业情况',
	    width: 800,
	    height: 600,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    
	    }	
	});
} 

showFQScheme = function(e){
	var faid = grid.getSelected().id;
	var qx=grid.getSelected().qx;
	var yf=grid.getSelected().yf;
	var nd = mini.get("zfnd").value;
	
	mini.open({
		url:'/ssj/ssjscheme/showFeiQiScheme.jsp?theme=2&faid='+faid+"&flag="+e+"&qx="+qx+"&yf="+yf+"&nd="+nd,
		showMaxButton: false,
	    allowResize: true,
	    title: '废弃方案',
	    width: 800,
	    height: 600,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    
	    }	
	});
}

showSchemeView = function(e){
	var faid = grid.getSelected().id;
	var parentid=grid.getSelected().parentid;
	mini.open({
		url:'/ssj/ssjScheme/showSchemeView.jsp?theme=2&faid='+faid+'&parentid='+parentid+'&flag='+e,
		showMaxButton: false,
	    allowResize: true,
	    title: '方案浏览',
	    width: 1000,
	    height: 680,
	    onload: function(){
	        var iframe = this.getIFrameEl();
	       // iframe.contentWindow.setData(data);
	    },
	    ondestroy: function (action) {
	    	
	    }	
	});
	
}
</script>
</body>
</html>