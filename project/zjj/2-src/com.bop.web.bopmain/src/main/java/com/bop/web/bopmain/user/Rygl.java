package com.bop.web.bopmain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.bop.bopmain.dao.Dept;
import com.bop.bopmain.dao.Person;
import com.bop.module.user.dao.User01;
import com.bop.common.StringUtility;
import com.bop.hibernate.HibernateDaoBaseImpl;
import com.bop.json.ExtFormObject;
import com.bop.json.ExtGrid;
import com.bop.json.ExtGridRow;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.json.ExtResultObject;
import com.bop.json.ExtTreeNode;
import com.bop.module.user.UserChangedEvent;
import com.bop.module.user.UserProvider;
import com.bop.module.user.UserService;
import com.bop.module.user.defaults.DefaultPasswordService;
import com.bop.web.rest.Action;
import com.bop.web.rest.ActionContext;
import com.bop.web.rest.Controller;
import com.bop.web.rest.renderer.Renderer;
import com.bop.web.rest.renderer.TemplateRenderer;

@Controller
public class Rygl implements UserProvider {
	private HibernateDaoBaseImpl<Person, String> personDao;
	private HibernateDaoBaseImpl<Dept, String> deptDao;
	private HibernateDaoBaseImpl<User01, String> UserDao;
	private UserService userService;
	
	public void setUserDao(HibernateDaoBaseImpl<User01, String> userDao) {
		UserDao = userDao;
	}

	@Action
	public Renderer index() {
		return new TemplateRenderer(this.getClass(), "index");
	}
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Action
	public String personList() {
		
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String deptId = request.getParameter("departmentId");

		List<Person> persons;
		if("0".equals(deptId)){
			persons = this.personDao.findBySql("select * from ry01");
		}else{
			persons = this.personDao.findBySql("select * from ry01 where deptid = '" + deptId + "'");
		}

		ExtGrid grid = new ExtGrid();
		List<ExtGridRow> rows = new ArrayList<ExtGridRow>();
		for (Person p : persons) {
			ExtGridRow row = new ExtGridRow();
			row.add("id", p.getPersonId());
			row.add("loginname", p.getLoginName());
			row.add("realname", p.getUserName());
			rows.add(row);
		}
		grid.rows.addAll(rows);
		return grid.toString();
	}

	public void setPersonDao(HibernateDaoBaseImpl<Person, String> personDao) {
		this.personDao = personDao;
	}

	public void setDeptDao(HibernateDaoBaseImpl<Dept, String> deptDao) {
		this.deptDao = deptDao;
	}

	@Action
	public String deptList() {
		ExtObjectCollection col = new ExtObjectCollection();
		ExtTreeNode eo = new ExtTreeNode();
		eo.setID(StringUtility.emptyUUID().toString());
		eo.setText("部门列表");
		List<Dept> depts = this.deptDao.findBySql("select * from bm01 where bm0104 = '" + StringUtility.emptyUUID().toString() + "'");

		eo.add("children", this.getSubDeptTree(depts));
		col.add(eo);
		return col.toString();
	}

	private ExtObjectCollection getSubDeptTree(List<Dept> list) {
		ExtObjectCollection col = new ExtObjectCollection();
		
		for (Dept d : list) {
			List<Dept> list1 = this.deptDao.findBySql("select * from bm01 where bm0104 = '" + d.getDeptId() + "'");

			ExtTreeNode nd = new ExtTreeNode();
			nd.setID(d.getDeptId().toString());
			nd.setText(d.getName());
			nd.add("parentid", d.getParentId());
			
			nd.add("children", this.getSubDeptTree(list1));
			col.add(nd);
		}
		
		return col;
	}

	/**
	 * 查询该部门下是否有同名
	 * 
	 * @param data
	 * @param id
	 * @return
	 */
	@Action
	public String isDeptName(String deptId, String deptname) {
		ExtResultObject obj = new ExtResultObject(true);
		
		List<Dept> depts = this.deptDao.findBySql("select * from bm01 where BM0104 = '" + deptId + "' and BM0101 = '" + deptname + "'");
		if (depts.size() > 0) {
			obj = new ExtResultObject(false);
			obj.add("info", "已占用");
		} else {
			obj = new ExtResultObject(true);
			obj.add("info", "yes");
		}
		return obj.toString();
	}

	/**
	 * 新增部门
	 * 
	 * @param formCollection
	 *            返回来的数据集合
	 * */
	@Action
	public String addDept(String parentId) {
		ExtResultObject ero = new ExtResultObject(true);
		
		Dept dept = new Dept();
		HttpServletRequest req  = ActionContext.getActionContext().getHttpServletRequest();
		
		String deptname = req.getParameter("BM0101");
		dept.setName(deptname);
		dept.setParentId(parentId);
		this.deptDao.save(dept);

		return ero.toString();
	}

	/**
	 * 删除部门
	 */
	@Action
	public String delDept(String deptId) {
		ExtResultObject obj = new ExtResultObject(true);
		Dept d = this.deptDao.findById(deptId);
		if(d != null) this.deptDao.delete(d);
		obj.add("info", "删除成功！");

		return obj.toString();
	}

	/**
	 * 更新部门信息
	 */
	@Action
	public String editDept(String deptId) {
		ExtResultObject ero = new ExtResultObject(true);

		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		String name = request.getParameter("name");
		String desciption = request.getParameter("description");
		
		Dept d = this.deptDao.findById(deptId);
		if(d == null) {
			d = new Dept();
			d.setDeptId(deptId);
		}

		d.setName(name);
		d.setDescription(desciption);
		this.deptDao.save(d);
		return ero.toString();
	}

	/**
	 * 查询登录名是否存在
	 * 
	 * @param departmentId
	 * @param loginName
	 * @return
	 */
	@Action
	public String isLoginName(String loginName, String deptId) {
		ExtResultObject obj = new ExtResultObject(true);
		List<Person> persons = this.personDao.findBySql("select * from ry01 where RY0102 = '" + loginName + "'");
		if (persons.size() > 0) {
			obj = new ExtResultObject(false);
			obj.add("info", "已占用");
		} else {
			obj = new ExtResultObject(true);
			obj.add("info", "yes");
		}
		return obj.toString();
	}

	@Action
	public String addUser(String id,String flag) {
		HttpServletRequest req = ActionContext.getActionContext().getHttpServletRequest();
		ExtResultObject obj = new ExtResultObject(true);

		String userName;
		String loginName;
		String password;
		String description;
		String usetime;
		
		String IP;
		String MAC;
		
		if("add".equals(flag)){
			 userName = req.getParameter("RY0103");
			 loginName = req.getParameter("RY0102");
			 password = req.getParameter("password");
			 description = req.getParameter("RY0104");
			 usetime = req.getParameter("usetime");
			 IP = req.getParameter("IP");
			 MAC = req.getParameter("MAC");
		}else{
			 userName = req.getParameter("ERY0103");
			 loginName = req.getParameter("ERY0102");
			 password = req.getParameter("Epassword");
			 description = req.getParameter("ERY0104");
			 usetime = req.getParameter("Eusetime");
			 IP = req.getParameter("IP");
			 MAC = req.getParameter("MAC");
		}
		
		Person psr = new Person();
		Dept dept = this.deptDao.findById(id);
		psr.setLoginName(loginName);
		psr.setUserName(userName);
		psr.setEnabled(true);
		psr.setDeleted(false);
		if(dept==null){
			obj.add("info", "选择部门有误！");
		}else{
			psr.setDept(dept);
		}
		this.personDao.save(psr);
		User01 user = new User01();

		user.setLoginName(loginName);
		user.setUserName(userName);
		user.setValidate(true);
		user.setPassword(password);
		user.setDescription(description);
		user.setUserforTime(Integer.parseInt(usetime));
		user.setMac(MAC);
		user.setIp(IP);
		user.setWpasswordTime("0");

		this.UserDao.save(user);
		return obj.toString();
	}

	/**
	 * 删除人员
	 * 
	 * @param userID
	 * @return
	 */
	@Action
	public String deleteUser(String userId) {
		ExtResultObject obj = new ExtResultObject();

		Person p = this.personDao.findById(userId);
		if(p != null) {
			this.personDao.delete(p);
			for(UserChangedEvent event : EventList) {
				event.userDeleted(p.getLoginName());
			}
		}

		obj.add("info", "删除成功");
		return obj.toString();
	}

	/**
	 * 更新人员信息
	 */
	@Action
	public String editUser(String userId) {
		ExtResultObject ero = new ExtResultObject(true);
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		
		this.editUser(userId, request.getParameter("deptId"),
				request.getParameter("loginName"),
				request.getParameter("userName"),
				request.getParameter("description"));
		
		return ero.toString();
	}
	
	/**
	 * 添加或修改用户
	 * @param userId 用户ID
	 * @param deptId 用户所在的部门ID
	 * @param loginName 登录名
	 * @param userName 用户真实姓名
	 * @param description 描述
	 * @return 保存成功的用户对象
	 */
	public Person editUser(String userId, String deptId,
			String loginName, String userName, String description) {
		Person p = this.personDao.findById(userId);
		if(p == null) {
			p = new Person();
			p.setPersonId(userId);
			
			String did = deptId;
			Dept d = this.deptDao.findById(did);
			p.setDept(d);
		}
		
		p.setLoginName(loginName);
		p.setUserName(userName);
		p.setDescription(description);
		
		p.setEnabled(true);
		p.setDeleted(false);
		this.personDao.save(p);
		
		for(UserChangedEvent event : EventList) {
			User01 u = new User01();
			u.setUser00(UUID.randomUUID().toString());
			u.setLoginName(p.getLoginName());
			u.setUserName(p.getUserName());
			u.setValidate(p.getEnabled());
			u.setDescription(p.getDescription());
			u.setPassword(DefaultPasswordService.getDefaultPassword());
			event.UserChanged(p.getLoginName(), u);
		}
		
		return p;
	}
	
	private static final List<UserChangedEvent> EventList = new ArrayList<UserChangedEvent>();

	public synchronized void onBind(UserChangedEvent event, Map<?, ?> props) {
		EventList.add(event);
	}

	public synchronized void onUnbind(UserChangedEvent event, Map<?, ?> props) {
		EventList.remove(event);
	}

	@Override
	public List<User01> getUsers() {
		List<Person> persons = this.personDao.findAll();
		List<User01> us = new ArrayList<User01>();
		for(Person p : persons) {
			User01 u = new User01();
			u.setLoginName(p.getLoginName());
			u.setUserName(p.getUserName());
			u.setDescription(p.getDescription());
			u.setValidate(p.getEnabled());
			us.add(u);
		}
		return us;
	}

	@Action
	public String changePassword() {
		HttpServletRequest request = ActionContext.getActionContext().getHttpServletRequest();
		ExtResultObject ero=new ExtResultObject(true);
		String pwd = request.getParameter("password");
		String oldpassword = request.getParameter("oldpassword");
		String id = request.getParameter("id");

		User01 user = this.userService.getByLoginName(id);
		
		if(oldpassword.equals(user.getPassword())) {
			//this.userService.changePassword(id, pwd);
		} else {
			ero.add("success", false);
			ero.add("info", "旧密码输入有误");
		}
		return ero.toString();
	}
	

	@Override
	public User01 getByLoginName(String name) {
		List<Person> persons = this.personDao.findByProperty("loginName", name);
		if(persons.size() == 0) return null;

		Person p = persons.get(0);
		User01 u = new User01();
		u.setLoginName(p.getLoginName());
		u.setUserName(p.getUserName());
		u.setDescription(p.getDescription());
		u.setValidate(p.getEnabled());
		u.setZone(p.getZone());
		return u;
	}

	@Action
	public String loaduser(String id) {
		ExtFormObject form = new ExtFormObject();
		List<User01> userList = this.UserDao.findBySql("select * from user01 where user0101 = '"+id+"'");
		User01 user =null;
		
		if(userList.size() >0){
			user = userList.get(0);
		}else{
			form.add("form", "信息获取错误！");
		}
		if (user != null)
		{
			form.add("ERY0102", user.getLoginName());
			form.add("ERY0103", user.getUserName());
			form.add("Epassword", user.getPassword());
			form.add("Eusetime", user.getUserforTime());
			form.add("ERY0104", user.getDescription()==null?"":user.getDescription());
		} 
		return form.toString();
	}

	@Action
	public String checkPassword() {
		ExtObject eo = new ExtObject();
		eo.add("info", true);
		
		return eo.toString();
	}
}
