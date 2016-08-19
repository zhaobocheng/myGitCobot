<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法人员管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<!-- <span>方案时间：</span><input class="mini-combobox" id="zftime" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/> -->
	<!-- <a class="mini-button" id = "upbur" iconCls="icon-upload" onclick="sbRow()">上报</a> -->
	<span>年度：</span><input class="mini-combobox" id="zfnd" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" url="/ssj/personmanage/personmanage/getCBData/nd?theme=none"/>
	<span>状态：</span><input class="mini-combobox" id="fazt" style="width:150px;"  onvaluechanged="valueChange" textField="text" valueField="id" data="[{id:'0',text:'未上报'},{id:'1',text:'已上报'}]"  />
</div>

<div class="mini-fit">

   <!-- 上侧选择人员 -->
    <div title="方案列表" region="north"   showSplitIcon="true" minHeight = "200px"  >
		<div id="griddata" class="mini-datagrid" style="width:100%;height:100%" url="/ssj/personmanage/personmanage/getFALBData//?theme=none"  
		idFiled="id"  showPager="false" >
			<div property="columns">
				<div type="checkcolumn" width="10"></div>
				<div type="indexcolumn" width="10">序号</div>
				<div field="id" name="id"  width="100" visible="false">方案id</div>
				<div field="famc" name="famc" width="100" headerAlign="center"   allowSort="true">方案名称</div>
				<div field="zftime" name="zftime" width="60" headerAlign="center"   allowSort="true">执法日期</div>
				<div field="cjtime" name="cjtime" width="80" headerAlign="center" dateFormat="yyyy-MM-dd" allowSort="true">创建时间</div>
				<div field="zt" name="zt" width="60" headerAlign="center"  >状态</div>
				<div field="cz" name="cz" width="30" headerAlign="center"  >操作</div>
			</div>
		</div>
    </div>

</div>
<!-- 绘制页面结束 -->
<script>
mini.parse();
var zfnd = mini.get("zfnd");
zfnd.select(0);
var zfzt = mini.get("fazt");
zfzt.select(0);
var gridurl = '/ssj/personmanage/personmanage/getFALBData/'+zfnd.value+'/'+zfzt.value+'?theme=none';
var datagrid=mini.get("griddata");
datagrid.setUrl(gridurl);
datagrid.load();

function isup(e){
	var left = mini.get("left");
	var leftall = mini.get("leftall");
	var right = mini.get("right");
	var rightall = mini.get("rightall");
	jQuery.ajax({
		url:'/ssj/personmanage/personmanage/getZT/'+e+'?theme=none',
		type:'post',
		success:function(e){
			if(e=="ups"){
				 left.setEnabled(false);
				 leftall.setEnabled(false);
				 right.setEnabled(false);
				 rightall.setEnabled(false);
			}else{
				 left.setEnabled(true);
				 leftall.setEnabled(true);
				 right.setEnabled(true);
				 rightall.setEnabled(true);
			}
		}	
	});
};

/* isup(zfcom.value); */

onSelectionChanged = function(e){
	  var data = e.sender;
      var record = data.getSelected();
      if (record) {
		var left = mini.get("left");
		var leftall = mini.get("leftall");
		var right = mini.get("right");
		var rightall = mini.get("rightall");
		var nd = mini.get("zfnd").value;
		var url = "/ssj/personmanage/PersonManage/getSelectedGridData/"+record.id+"?theme=none&nd="+nd;
		grid.setUrl(url);
		grid.load();
		var url2 = "/ssj/personmanage/PersonManage/getUnSelectedGridData/"+record.id+"?theme=none&nd="+nd;
		ungrid.setUrl(url2);
		ungrid.load(); 

   		jQuery.ajax({
   			url:'/ssj/personmanage/personmanage/getZT/'+record.id+'?theme=none',
   			type:'post',
   			success:function(e){
   				if(e=="ups"){
   					 left.setEnabled(false);
   					 leftall.setEnabled(false);
   					 right.setEnabled(false);
   					 rightall.setEnabled(false);
   				}else{
   					 left.setEnabled(true);
   					 leftall.setEnabled(true);
   					 right.setEnabled(true);
   					 rightall.setEnabled(true);
   				}
   			}	
   		});
      }
}

valueChange = function(){

	var nd = mini.get("zfnd").value;
	var zt = mini.get("fazt").value;
	url = "/ssj/personmanage/PersonManage/getFALBData/"+nd+"/"+zt+"?theme=none";
	datagrid.setUrl(url);
	datagrid.load();
	//isup(e.value);
}

//选择人员
function sbRow(){
	var faid = datagrid.getSelected().id;
	var nd = mini.get("zfnd").value;
	
	mini.open({
		url:'/ssj/personmanage/choosePerson.jsp?theme=2&faid='+faid+"&nd="+nd,
		showMaxButton: false,
	    allowResize: true,
	    title: '选择人员情况',
	    width: 1000,
	    height: 600,
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