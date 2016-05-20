<%@ page language="java" pageEncoding="utf-8"%>
<%@ page import="com.bop.web.PathUtil" %>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=8,chrome=1" />
<title>权限管理</title>

</head>

<body style="width: 100%; height: 100%; margin: 0px;">
<div class="mini-splitter" style="width: 100%; height: 100%;" allowResize="false">
    <!--mini-splitter左边区域-->
    <div size="19%"  minSize="250px" showCollapseButton="false" showRadioButton="false">
        <div class="mini-fit">
        	<div class="mini-toolbar" style="padding:2px;border-top:0;border-left:0;border-right:0;"> 
        		<a class="mini-button" iconCls="icon-add" plain="true" onclick="onAddNode()">
					新增
				</a>
            	<span class="separator"></span>
            	<a class="mini-button" iconCls="icon-remove" plain="true" onclick="onDelNode()">
					删除
				</a>  
            	<span class="separator"></span>
            	<a class="mini-button" iconCls="icon-edit" plain="true" onclick="oneditNode()">
					修改
				</a>   
        	</div>
            	<!-- <ul id="roleTree" class="mini-tree"  style="width:100%;height:95%;" 
				showTreeIcon="true" textField="text" idField="id" parentField="pid" resultAsTree="true"  
				expandOnLoad="true" onnodeclick="onNodeClick" url="/bopmain/roleajax.cmd?op=list&type=1">
				</ul> -->
				<ul id="roleTree" class="mini-tree" url="/bopmain/roleajax.cmd?op=list&type=1" style="width:100%;height:95%;" 
		        showTreeIcon="true" textField="text" idField="id" parentField="pid" resultAsTree="false" 
		        showArrow="true" expandOnNodeClick="true"
		        >
    </ul>  
        </div>
    </div>
    
    <!--mini-splitter右边区域-->
	    <div id="tabs1" class="mini-tabs" activeIndex="0" style="width:100%;height:100%;" contextMenu="#tabsMenu">
			<div title="角色功能权限">
				<div id="jsgnqx" class="mini-fit" style="height:100%;">
			        
			    </div>
    		</div>
		    <div title="角色对应用户">
		    	<table >
		    	<tr>
		    	<td>用户名：<input class="mini-textbox" id="userNameLeLeft" name="userNameLeLeft" style="width: 100px;" />
		    	<a class="mini-button"  iconCls="icon-find" plain="true" onclick="">查询</a></td>
		    	<td></td>
		    	<td>用户名：<input class="mini-textbox" id="userNameLeRight" name="userNameLeRight" style="width: 100px;" />
		    	<a class="mini-button"  iconCls="icon-find" plain="true" onclick="">查询</a></td>
		    	</tr>
			        <tr>
			            <td >
			                <div id="grid1" class="mini-datagrid" style="width:250px;height:400px;"
			                    idField="id" multiSelect="true"
			                    url="../data/countrys.txt" resultAsData="true" showPager="false">
			                    <div property="columns">
			                        <div type="checkcolumn"></div>
			                        <div header="用户名" field="userName"></div>
			                    </div>
			                </div>
			            </td>
			            <td style="width:60px;text-align:center;">
			                <input type="button" value=">" onclick="adds()" style="width:40px;"/><br />
			                <input type="button" value=">>" onclick="addAll()" style="width:40px;"/><br />
			                <input type="button" value="&lt;&lt;" onclick="removeAll()" style="width:40px;"/><br />
			                <input type="button" value="&lt;" onclick="removes()" style="width:40px;"/><br />
			
			            </td>
			            <td>
			                <div id="grid2" class="mini-datagrid" style="width:250px;height:400px;"                     
			                    idField="id"  multiSelect="true" showPager="false"
			                    allowCellEdit="true" allowCellSelect="true"
			                >
			                    <div property="columns">
			                        <div type="checkcolumn"></div>
			                        <div header="用户名" field="userName"></div>
			                    </div>
			                </div>
			            </td>
			        </tr>
			    
			    </table>
		    </div>
    	</div>
</div>

<!--角色维护页面  -->
<div id="addRoleTypeWindow" class="mini-window" 
       style="width: 200px; height: 100px; padding: 0; margin: 0"
       showToolbar="true" showFooter="true" allowResize="false">
        <div id="roleType" class="mini-radiobuttonlist" 
            data="[{id:'2',text:'二级管理员角色'},{id:'0',text:'用户角色'}]" value="2" >
        </div>
        <br/>
        <a class="mini-button" onclick="decidedAdd()"
		style="width: 60px; margin-right: 20px;" iconCls="icon-save">确定</a> 
		<a class="mini-button" onclick="cancelAdd()" style="width: 60px;" iconCls="icon-cancel">取消</a>
</div>

<!--管理员角色维护页面  -->
<div id="mngerRoleWindow" class="mini-window" 
       style="width: 600px; height: 300px; padding: 0; margin: 0"
       showToolbar="true" showFooter="true" allowResize="false">
        <form id="mngerRoleForm" method="post">
            <div
				style="padding-left: 0px;  padding-bottom: 5px; padding-top: 20px;"
				align="center">
				<table>
            <!--组织机构  -->
				<tr>
				<td>组织机构</td>
				<td><input id="btnEditOrganization" name="btnEditOrganization" class="mini-buttonedit" onbuttonclick="onOrganizationEdit"/> </td>
				</tr>
				<tr>
            <!--角色名  -->
				<td>角色名</td>
				<td><input name="mngerRoleName" id="mngerRoleName" class="mini-textbox"
							required="true" maxlength="20"   style="width: 100%;" /></td>
				</tr>
				</table>
            </div>
            <div style="text-align: center; padding: 10px;">
				<a class="mini-button" onclick="mngerRoleSave()"
				style="width: 60px; margin-right: 20px;" iconCls="icon-save">确定</a> 
				<a class="mini-button" onclick="mngerRoleCancel()" style="width: 60px;" iconCls="icon-cancel">取消</a>
			</div>
			<div align="center" valign="middle">
				<table width="100%" align="center" border="0">
					<tr style="height: 30px"></tr>
				</table>
			</div>
        </form>
</div>

<!--组织结构树  -->
<div id="organizationTreeWindow" class="mini-window" 
       style="width: 400px; height: 300px; padding: 0; margin: 0"
       showToolbar="true" showFooter="true" allowResize="false">
           <div class="mini-fit">
        
		        <ul id="organizationTree" class="mini-tree" style="width:100%;height:100%;" 
		            showTreeIcon="true" textField="text" idField="id" parentField="pid" resultAsTree="false"  
		            showCheckBox="true" checkRecursive="true"
		            expandOnLoad="true" allowSelect="false" enableHotTrack="false" 
		            >
		        </ul>
		    
		    </div>                
		    <div class="mini-toolbar" style="text-align:center;padding-top:8px;padding-bottom:8px;" 
		        borderStyle="border-left:0;border-bottom:0;border-right:0;">
		        <a class="mini-button" style="width:60px;" onclick="organizationTreeOk()">确定</a>
		        <span style="display:inline-block;width:25px;"></span>
		        <a class="mini-button" style="width:60px;" onclick="organizationTreeCancel()">取消</a>
		    </div>
</div>
       
<!--用户角色维护页面  -->
<div id="userRoleWindow" class="mini-window" 
       style="width: 600px; height: 300px; padding: 0; margin: 0"
       showToolbar="true" showFooter="true" allowResize="false">
        <form id="userRoleForm" method="post">
            <div
				style="padding-left: 0px;  padding-bottom: 5px; padding-top: 20px;"
				align="center">
				<table>
            <!--父节点管理员角色  -->
				<tr>
				<td>父节点角色</td>
				<td><input name="userRoleParent" id="userRoleParent" class="mini-combobox"
							required="true" url=""
							valueField="id" emptyText="--请选择--" textField="name"  style="width: 100%;" /> </td>
				</tr>
				<tr>
            <!--角色名  -->
				<td>角色名</td>
				<td><input name="userRoleName" id="userRoleName" class="mini-textbox"
							required="true" maxlength="20"   style="width: 100%;" /></td>
				</tr>
				</table>
            </div>
            <div style="text-align: center; padding: 10px;">
				<a class="mini-button" onclick="userRoleSave()"
				style="width: 60px; margin-right: 20px;" iconCls="icon-save">确定</a> 
				<a class="mini-button" onclick="userRoleCancel()" style="width: 60px;" iconCls="icon-cancel">取消</a>
			</div>
			<div align="center" valign="middle">
				<table width="100%" align="center" border="0">
					<tr style="height: 30px"></tr>
				</table>
			</div>
        </form>
</div>
<%=PathUtil.jsLink("com.bop.web.bopmain", "/author/userRoleMng.js")%>
</body>
</html>