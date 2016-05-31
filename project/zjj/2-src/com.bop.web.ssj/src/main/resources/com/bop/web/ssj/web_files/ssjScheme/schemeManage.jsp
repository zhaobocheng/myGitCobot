<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>方案管理</title>
</head>
<body>

<div style="padding:5px;10px;5px;0">
	<span>抽查方案：</span><input class="mini-combobox" id="faid" style="width:150px;" textField="text" valueField="id" onvaluechanged="valueChange" url="/ssj/personmanage/personmanage/getZfcData?theme=none"/>
	<a class="mini-button" iconCls = "icon-goto"  onclick="createFa()" >生成随机方案</a>
	<a class="mini-button" iconCls = "icon-save" onclick="commitFa()" >提交随机方案</a>
	<a class="mini-button" iconCls = "icon-new" onclick="importExc()" >导出Excel</a>
</div>

<div id="treegrid" class="mini-treegrid" style="width:100%;height:100%;" url="" showTreeIcon="true" 
    treeColumn="dq" idField="dqid" parentField="ParentDqId" resultAsTree="false"  allowResize="true" expandOnLoad="true">
    <div property="columns">
        <div type="indexcolumn"></div>
        <div name="dq" field="dq" width="160" >地区</div>
        <div field="jgdm" width="80">机构代码</div>
        <div field="dwmc" width="60" align="right">单位名称</div>
        <div field="dz" width="80" >地址</div>
        <div field="lxr" width="80" >联系人</div>
        <div field="phone" width="60" >电话</div>
        <div field="jcnr" width="80" >检查内容</div>
        <div field="jcr" width="80" >检查人</div> 
         <div field="jcrid" width="80" visible="false" >检查人id</div> 
        <div field="sjly" width="80" >涉及事项</div>                  
    </div>
</div>


<script >
mini.parse();

var zfcom = mini.get("faid");
zfcom.select(0);


var grid = mini.get("treegrid");
var url = '/ssj/ssjScheme/CreateScheme/getSchemeDate/'+zfcom.value+'?theme=none';
grid.load(url);

valueChange = function(e){
	url = "/ssj/ssjScheme/CreateScheme/getSchemeDate/"+e.value+"?theme=none";
	grid.setUrl(url);
	grid.reload();
}

createFa=function(){
	var faid = mini.get("faid").value;

	$.ajax({
		url:'/ssj/ssjScheme/CreateScheme/createSchemeDate/'+faid,
		type:'get',
		success:function(e){
			if(e=="seccess"){
				alert("生成完毕！");
				gird.reload();
			}else if(e=="false"){
				alert("有为设置人员或企业数的区县请先设置！");
			}
		}
	});
}
commitFa = function(){
	var faid = mini.get("faid").value;

	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/commitSchemeDate/'+faid+"?theme=none",
		type:'get',
		success:function(e){
			if(e=="seccess"){
				alert("生成完毕！");
				gird.reload();	
			}
		}
	});
}

importExc = function(){
	var faid = mini.get("faid").value;
	$.ajax({
		url:'/ssj/ssjscheme/CreateScheme/exportExcel/'+faid+"?theme=none",
		type:'get',
		success:function(e){
			debugger;
			var inf = mini.decode(e);
			if(inf.flag){
				location.href = decodeURI("/ResourceFiles"+inf.path);
			}else{
				alert("导出失败咧！");
			}
		}
	});
}


function exportExcel() {
    $('#ziduan').val(ziduan_t1);
    $('#table_index').attr("value","1");
  // 拿到查询条件
  var ggsclbFormData = ggsclb.getData();
  var ggsclbFormJson = mini.encode([ ggsclbFormData ]);
  var temp1 = $("#cxtj1Data").val();
  var temp2 = $("#cxtj2Data").val();
  if(temp1){
    $("#cxtjForm").attr("action","/gjxfj/common/dcservice/exportexcel/xfjcxlist/民政部信访件信息.xls?theme=none");
  }else if(temp2){
    $("#cxtjForm").attr("action","/gjxfj/common/dcservice/exportexcel/xfjgjlist/民政部信访件信息.xls?theme=none");
  }

  //var data1 = $("#cxtj1Data").val();
  //var data2 = $("#cxtj2Data").val();
  if (mini.get("#ggscgrid").totalCount < 1) {
    mini.alert("数据为空");
    return;
  }
  
  $("#cxtjForm").submit();
  showTipsMsgSuccess("导出文件准备中，请稍后下载！");
}

</script>
</body>
</html>