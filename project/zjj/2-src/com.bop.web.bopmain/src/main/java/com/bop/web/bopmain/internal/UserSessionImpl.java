package com.bop.web.bopmain.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bop.domain.IRecordDao;
import com.bop.domain.dao.IRecord;
import com.bop.web.bopmain.UserSession;
import com.bop.module.authorweb.CommonSessionImpl;
/**
 * 
 * @Modify by YinHua
 * 20160411
 * 20160519
 */
public class UserSessionImpl extends CommonSessionImpl implements UserSession {
	private static final Logger log = LoggerFactory.getLogger(UserSessionImpl.class);
	private static UserSessionImpl _cache = null;
	private IRecordDao recordDao;

	public UserSessionImpl() {
		_cache  = this;
	}
	
	public static UserSession getMe() {
		return _cache;
	}
	
	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
	}

	/**
	 * 得到当前登录的用户ID（Bap_User.UserId） （设置此用户ID是在用户登录成功时）
	 * @return 用户ID，如果没找到，则返回null
	 */
	public String getCurrentUserId() {
//		return this.getCurrentUserKeyId().toString();
		return this.getCurrentUserName();
	}
	
	/**
	 * 得到当前登录的用户的实名
	 * @return 用户实名，如果没找到，则返回null
	 */
	@Override
	public String getCurrentUserRName(){
		String realname = this.getValue(CURRENT_USER_RName, String.class);
		if(realname == null) {
			setCurrentUserRealName();
			return this.getValue(CURRENT_USER_RName, String.class);
		} 
		return realname;	
	}

	/**
	 * 得到当前登录的用户的ID（主键UUID）
	 * @return 用户ID，如果没找到，则返回null
	 */
	public UUID getCurrentUserKeyId(){
		UUID userPKID = this.getValue(CURRENT_USER_KEY_ID, UUID.class);
		if(userPKID == null) {
			setCurrentUserPKID();
			return this.getValue(CURRENT_USER_KEY_ID, UUID.class);
		} 
		return userPKID;
	}
		
	/**
	 * 得到当前登录人所属组织（部门）的ID，（设置此用户ID是在用户登录成功时）
	 * @return 登录人描述。没有找到返回null
	 */
	public UUID getCurrentUserOrgId(){
		//TODO:下面2行是测试临时加的代码，在修正setCurrentUserOrgID方法后需注释掉
		@SuppressWarnings("static-access")
		HttpSession session = this.getSession();
		session.setAttribute(CURRENT_USER_ORG_ID, "00000000-0000-0000-0000-000000000000");

		UUID orgID = this.getValue(CURRENT_USER_ORG_ID, UUID.class);
		if(orgID == null) {
			setCurrentUserOrgID();
			return this.getValue(CURRENT_USER_ORG_ID, UUID.class);
		} 
		return orgID;
	}


	/**
	 * 设置当前用户在Session中的用户主键ID信息
	 */
	private void setCurrentUserPKID() {
		log.debug("Common.setCurrentUserPidInfo");
		@SuppressWarnings("static-access")
		HttpSession session = this.getSession();
		String uname = getCurrentUserName();
		String strWhere = "USER0101=:u0101";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("u0101", uname);
		List<IRecord> listA01 = this.recordDao.queryRecord("USER01", strWhere, params);
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）的UUID。");
		} else {
			session.setAttribute(CURRENT_USER_KEY_ID, rec.get("USER00"));
		}
	}
	
	/**
	 * 设置当前用户在Session的实名信息
	 */
	private void setCurrentUserRealName() {
		log.debug("Common.setCurrentUserPidInfo");
		@SuppressWarnings("static-access")
		HttpSession session = this.getSession();
		String uname = getCurrentUserName();
		String strWhere = "USER0101=:u0101";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("u0101", uname);
		List<IRecord> listA01 = this.recordDao.queryRecord("USER01", strWhere, params);
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）的实名。");
		} else {
			session.setAttribute(CURRENT_USER_RName, rec.get("USER0103"));
		}
	}
	
	/**
	 * 设置当前用户所属组织部门ID信息
	 */
	private void setCurrentUserOrgID() {
		log.debug("Common.setCurrentUserOrgIdInfo");
		@SuppressWarnings("static-access")
		HttpSession session = this.getSession();
		String uname = getCurrentUserName();
		//TODO: 修正读取字段
		String strWhere = "A0102=:a0102";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("a0102", uname);
		List<IRecord> listA01 = this.recordDao.queryRecord("A01", strWhere, params);
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）的组织单位信息。");
		} else {
			session.setAttribute(CURRENT_USERID_IN_ORG, rec.getRecordId());
		}
	}
	
	
	/**
	 * 设置当前用户在项目和组织中的信息
	 */
	private void setCurrentUserInOrgInfo() {
		log.debug("Common.setCurrentUserInProjectInfo");
		
		@SuppressWarnings("static-access")
		HttpSession session = this.getSession();
		
		String uname = getCurrentUserName();
		
		String strWhere = "A0102=:a0102";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("a0102", uname);
		
		List<IRecord> listA01 = this.recordDao.queryRecord("A01", strWhere, params);
		
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）在组织资产中的对应。");
		} else {
			session.setAttribute(CURRENT_USERID_IN_ORG, rec.getRecordId());
		}
	}

	@Override
	public UUID getCurrentOrgUserId() {
		UUID userID = this.getValue(CURRENT_USERID_IN_ORG, UUID.class);
		if(userID == null) {
			setCurrentUserInOrgInfo();
			return this.getValue(CURRENT_USERID_IN_ORG, UUID.class);
			//return getCurrentOrgUserId();
		} 
		return userID;
	}

	
	@SuppressWarnings("unchecked")
	protected <T> T getValue(String key, Class<T> c) {
		Object o = this.getSessionKey(key);
		
		if(o == "") o = null;
		
		Object result;
		if(c.equals(UUID.class)) {
			result =  (o == null ? null : UUID.fromString(o.toString()));
		} else {
			result = (o == null ? "" : o.toString());
		}
		return (T) result;
	}
}
