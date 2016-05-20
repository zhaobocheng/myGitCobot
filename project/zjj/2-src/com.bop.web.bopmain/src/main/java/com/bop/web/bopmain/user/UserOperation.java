package com.bop.web.bopmain.user;

import org.springframework.beans.factory.InitializingBean;

import com.bop.module.author.RoleService;
import com.bop.module.user.UserChangedEvent;
import com.bop.module.user.UserService;
import com.bop.module.user.dao.User01;

public class UserOperation implements InitializingBean {
	private static UserOperation _ins;
	private RoleService roleService;
	private UserService userService;
	private UserChangedEvent userChangedEvent;
	
	/**
	 * 向系统中添加用户
	 * @param user 用户信息
	 * @param role 用户的角色
	 * @return 是否成功
	 */
	public static boolean addUser(User01 user, String role) {
		_ins.userChangedEvent.UserChanged(user.getLoginName(), user);
		addUser2Role(role, user.getLoginName());
		return true;
	}
	/**
	 * 删除用户
	 * @param loginName 用户名称
	 * @return
	 */
	public static boolean deleteUser(String loginName) {
		_ins.userChangedEvent.userDeleted(loginName);
		
		return true;
	}
	
	/**
	 * 修改用户
	 * @param user 用户修改后的属性值
	 * @return 
	 */
	public static boolean modifyUser(User01 user) {
		_ins.userChangedEvent.UserChanged(user.getLoginName(), user);
		return true;
	}
	
	/**
	 * 锁定用户，锁定后不能登录
	 * @param loginName 要锁定的用户名称
	 */
	public static void lockUser(String loginName) {
		_ins.userService.lockUser(loginName);
	}
	
	/**
	 * 解锁用户，解锁后可以登录
	 * @param loginName 要解锁的用户名称
	 */
	public static void unLockUser(String loginName) {
		_ins.userService.unLockUser(loginName);
	}
	
	/**
	 * 得到用户信息
	 * @param loginName 用户名称
	 * @return 如果未找到，则返回空，否则，用户信息
	 */
	public static User01 getUser(String loginName) {
		User01 u = _ins.userService.getByLoginName(loginName);
		return u;
	}
	
	/**
	 * 为用户添加角色权限
	 * @param roleId 角色ID
	 * @param loginName 登录名
	 */
	public static void addUser2Role(String roleId, String loginName) {
		_ins.roleService.addUser2Role(roleId, loginName);
	}

	/**
	 * 重置用户密码
	 * @param loginName 用户登录名
	 * @param newPassword 用户新密码
	 */
	public static void resetPassword(String loginName, String newPassword) {
		_ins.userService.changePassword(loginName, newPassword);
	}
	
	/**
	 * 判断用户登录名称是否存在
	 * @param loginName 用户名称
	 * @return 是否存在
	 */
	public static boolean exist(String loginName) {
		User01 u = _ins.getUser(loginName);
		return u != null;
	}
	
	
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setUserChangedEvent(UserChangedEvent userChangedEvent) {
		this.userChangedEvent = userChangedEvent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		_ins = this;
	}
}
