package com.bop.web.bopmain.author;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bop.json.ExtFormObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.json.ExtTreeNode;
import com.bop.module.author.dao.Role;
import com.bop.module.author.RoleService;
import com.bop.web.command.AutoNamedWebCommandImpl;

public class RoleAjaxCommand extends AutoNamedWebCommandImpl {
	private RoleService roleService;

	@Override
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String op = request.getParameter("op");
		if("list".equals(op)) {
			return this.getRoleList().toString();
		} else if("get".equals(op)) {
			String id = request.getParameter("id");
			return this.getRole(id).toString();
		} else if("create".equals(op)) {
			String type = request.getParameter("type");
			return this.createRole(type, 0).toString();
		} else if("save".equals(op)) {
			return this.saveRole(request).toString();
		} else if("delete".equals(op)) {
			String id = request.getParameter("id");
			return this.deleteRole(id).toString();
		}
		
		return "";
	}
   
	private ExtResultObject deleteRole(String id) {
		this.roleService.deleteRole(id);
		return new ExtResultObject(true);
	}

	private ExtResultObject saveRole(HttpServletRequest request) {
		ExtResultObject obj=new ExtResultObject(true);
		String id = request.getParameter("id");
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		String sort = request.getParameter("sort");

		this.roleService.saveRole(id, name, description, Integer.parseInt(sort));
		obj.add("msg", "保存成功");
		return obj;
	}
	
	private ExtObjectCollection getRoleList() {
		List<Role> roles = this.roleService.getRoles();
		return this.convertRolesToExtTreeNodes(roles);
	}
	
	private ExtObjectCollection convertRolesToExtTreeNodes(List<Role> roles) {
		ExtObjectCollection eoc = new ExtObjectCollection();
		for(Role item : roles)
			eoc.add(this.convertRoleToExtTreeNode(item));
		return eoc;
	}

	private ExtTreeNode convertRoleToExtTreeNode(Role role) {
		ExtTreeNode nd = new ExtTreeNode();
		nd.setID(role.getAu00());
		nd.setText(role.getRoleName());
		nd.add("leaf", true);
		return nd;
	}
	
	private ExtFormObject getRole(String id) {
		Role role = this.roleService.getRole(id);
		return this.convertRoleToExtFormObject(role);
	}
	
	private ExtFormObject createRole(String type, int order) {
		Role role = new Role();
		role.setAu00(UUID.randomUUID().toString());
		role.setRoleName("");
		role.setIndex(order);
		return this.convertRoleToExtFormObject(role);
	}
	
	public void setRoleService(RoleService service) {
		this.roleService = service;
	}
	
	private ExtFormObject convertRoleToExtFormObject(Role role) {
		ExtFormObject form = new ExtFormObject();
		form.setId(role.getAu00());
		form.add("name", role.getRoleName());
		form.add("description", role.getDescription());
		form.add("pindex", role.getIndex());
		form.add("sort", role.getIndex());
		return form;
	}
}
