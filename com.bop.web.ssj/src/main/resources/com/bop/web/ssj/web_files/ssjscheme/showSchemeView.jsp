<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head>
<meta name="decorator" content="miniui">
<title>执法方案</title>
<%String faid = request.getParameter("faid"); %>
<%String zone = request.getParameter("zone"); %>
<%String p3id = request.getParameter("p3id"); %> 
</head>
<body>

<div style="padding:5px;10px;5px;0">
<table width="800px" >
	<tr>
		<td  > <span>任务名称：</span><input class="mini-textbox" id="famc" readOnly="true" style="width:270px;"/> </td>
		<td  > <span>抽查月份：</span><input class="mini-textbox" id="ccyf" readOnly="true"  style="width:270px;"/> </td>
	</tr>
	<tr>
		<td><span>参与抽查执法人员数：</span><input class="mini-textbox" id="rs" readOnly="true"  style="width:200px;"/></td>
		<td><span>参与抽查企业数：</span><input class="mini-textbox" id="qys" readOnly="true"  style="width:228px;"/>		
		</td>
	</tr>
</table>

</div>
<div class="mini-fit">
	<div id="datagrid" class="mini-datagrid" style="width:100%;height:100%;" url="" showPager="false" allowResize="true">
	    <div property="columns">
	        <div type="indexcolumn">序号</div>
	        <div field="jgdm" width="80">机构代码</div>
	        <div field="dwmc" width="60" align="right">单位名称</div>
	        <div field="dz" width="80" >地址</div>
	        <div field="lxr" width="80" >联系人</div>
	        <div field="phone" width="60" >电话</div>
	        <div field="jcr" width="80" >检查人</div> 
	         <div field="jcrid" width="80" visible="false" >检查人id</div> 
	        <div field="sjly"  width="80" >对象数据来源 </div>
	        <div field="PLAN1221" vtype="required" width="100"   align="center" headerAlign="center" type="comboboxcolumn">是否发现问题
	            <input property="editor" class="mini-combobox"  style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY06?theme=none"/>                
	        </div>  
	         <div field="PLAN1222" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">问题涉及事项
	            <input property="editor" class="mini-combobox"  style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY01?theme=none"/>                
	        </div> 
	         <div field="zxjc" width="80" >专项名称</div> 
	  		<div field="PLAN1223"  width="120" headerAlign="center" allowSort="true">问题描述
	               <input property="editor" class="mini-textarea" style="width:100%;" minHeight="80"/>
	        </div>
	         <div field="PLAN1224" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">立案情况
	             <input property="editor" class="mini-combobox" style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY08?theme=none"/>                
	         </div>       
	         <div field="PLAN1225" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">企业是否变化
	             <input property="editor" class="mini-combobox" style="width:100%;" data="Bits"/>                
	         </div>        
	         <div field="PLAN1226" vtype="required" width="100" type="comboboxcolumn" align="center" headerAlign="center">企业变化情况
	             <input property="editor" class="mini-combobox"  style="width:100%;" url="/ssj/ssjScheme/SchemeResult/getCode/ZDY09?theme=none"/>                
	         </div>       
			<div field="PLAN1227"  width="120" headerAlign="center" allowSort="true">实际生产地
	               <input property="editor" class="mini-textarea" style="width:100%;" minHeight="80"/>
	        </div>  
	    </div>
	</div>
</div>
<script >
var Bits=[{id:0,text:'否'},{id:1,text:'是'}];
mini.parse();
var p3id ='<%=p3id %>';
var faid='<%=faid %>';
var zone='<%=zone %>';

$.ajax({
	url:'/ssj/ssjScheme/SchemeResult/getViewBaseInfo/'+p3id+'?theme=none',
	type:'get',
	data:{zone:zone},
	success:function(e){
		var info=mini.decode(e);
		mini.get("famc").setValue(info.mc);
		mini.get("ccyf").setValue(info.yf);
		mini.get("rs").setValue(info.rs);
		mini.get("qys").setValue(info.qys);
	}
});

var grid = mini.get("datagrid");
var url = '/ssj/ssjScheme/SchemeResult/getSchemeAndResult/'+faid+'/'+zone+'?theme=none';
grid.setUrl(url);
grid.load();

gridLoad = function(value){
	grid.setUrl(url);
	grid.reload();
}

</script>
</body>
</html>