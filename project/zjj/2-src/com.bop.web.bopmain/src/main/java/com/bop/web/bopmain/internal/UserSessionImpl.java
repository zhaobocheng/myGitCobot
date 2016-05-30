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
	
	/**
	 * 得到当前登录的用户ID（Bap_User.UserId） （设置此用户ID是在用户登录成功时）
	 * 
	 * @return 用户ID，如果没找到，则返回null
	 */
	public String getCurrentUserId() {
		return this.getCurrentUserName();
	}
	@Override
	public String getCurrentUserRName(){
		return this.getValue(CURRENT_USER_RName, String.class);
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
	
	
	
	public String getCurrentUserZone(){
		
		String zone = this.getValue(CURRENT_USER_ZONE, String.class);
		if(zone == null || "".equals(zone)) {
			setCurrentUserZone();
			return this.getValue(CURRENT_USER_ZONE, String.class);
		} 
		return zone;
	}

	/**
	 * 设置当前用户在项目和组织中的信息
	 */
	private void setCurrentUserZone() {
		log.debug("Common.setCurrentUserZone");
		HttpSession session = this.getSession();
		String uname = getCurrentUserName();
		String strWhere = "RY0102=:ry0102";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ry0102", uname);
		List<IRecord> listA01 = this.recordDao.queryRecord("RY01", strWhere, params);
		
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）在组织资产中的对应。");
		} else {
			session.setAttribute(CURRENT_USER_ZONE, rec.get("ry0107"));
		}
	}
	
	
	/**
	 * 设置当前用户在项目和组织中的信息
	 */
	private void setCurrentUserInOrgInfo() {
		log.debug("Common.setCurrentUserInProjectInfo");
		
		HttpSession session = this.getSession();
		
		String uname = getCurrentUserName();
		
		String strWhere = "RY0102=:ry0102";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ry0102", uname);
		
		List<IRecord> listA01 = this.recordDao.queryRecord("RY01", strWhere, params);
		
		IRecord rec = null;
		if(listA01.size() != 0) {
			rec = listA01.get(0);
		}
		
		if(rec == null) {
			log.debug("找不到当前登录的用户（" + uname + "）在组织资产中的对应。");
		} else {
			session.setAttribute(CURRENT_USERID_IN_ORG, rec.get("personid"));
		}
	}

	public void setRecordDao(IRecordDao recordDao) {
		this.recordDao = recordDao;
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
