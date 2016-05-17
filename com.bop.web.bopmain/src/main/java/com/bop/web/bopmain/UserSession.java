package com.bop.web.bopmain;

import java.util.UUID;
import com.bop.web.CommonSession;

public interface UserSession extends CommonSession {
	
	public static final String CURRENT_USER_ID = "currentUserID";
	public static final String CURRENT_USER_RName = "currentUserRName";//当前登录人的真实姓名
	public static final String CURRENT_USERID_IN_ORG = "currentUserIdInOrg";
	public static final String NAVIGATION_STRING = "NavigationString";
	public static final String SSO_TOKEN = "sso_token";		// 单点登录的token值
	
	/**
	 * 得到当前登录的用户的LoginName
	 * @return 用户Name，如果没找到，则返回null
	 */
	public String getCurrentUserId();
	
	/**
	 * 得到当前登录人的描述，（设置此用户ID是在用户登录成功时）
	 * @return 登录人描述。没有找到返回null
	 */
	public String getCurrentUserRName();
	
	/**
	 * 当前用户在组织中对应的资源ID（A01.RecordId）
	 * @return 当前用户在组织中对应的资源ID，如果没找到，则返回null
	 */
	public abstract UUID getCurrentOrgUserId();
}