package com.bop.web.bopmain.internal;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcOperations;

import com.bop.common.StringUtility;
import com.bop.domain.IRecordDao;
import com.bop.module.author.AuthorService;
import com.bop.module.function.MenuItem;
import com.bop.module.function.service.FunctionService;
import com.bop.module.function.service.FunctionTree;
import com.bop.web.bopmain.UserSession;

public class NavigationService {
	private AuthorService authorService;
	private FunctionService functionService;
	private UserSession userSession;
	private static NavigationService _cache = null;
	
	public NavigationService() {
		_cache = this;
	}
	
	private FunctionTree getFunctionTree() throws Exception {
		return this.functionService.getFunctionTree();
	}

	public static NavigationService getMe() {
		return _cache;
	}

	public void setFunctionService(FunctionService functionService) {
		this.functionService = functionService;
	}
	
	public FunctionTree getSystemFunctionTree(String userId) {
		Set<String> funcs = this.authorService.getUserFunction(userId);
		
		// 到此得到了用户的功能列表
		FunctionTree usertree = this.getFunctionTreeFromFunctions(funcs);
		
		return usertree;
	}
	
	public FunctionTree getSystemFunctionTree() {
		try {
			return this.getFunctionTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public FunctionTree getSystemTreeMenu() {
		String uid = this.userSession.getCurrentUserName();
		
		FunctionTree tree = this.getSystemFunctionTree(uid);
		return tree;
	}
	
	/**
	 * 得到用户的功能树
	 * @param userFunctions 用户有权限的功能列表
	 * @return
	 */
	private FunctionTree getFunctionTreeFromFunctions(Set<String> userFunctions) {
		try {
			// 过滤后得到用户功能树
			FunctionTree filtered = this.getFunctionTree().filter(userFunctions);
			return filtered;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public void setAuthorService(AuthorService authorService) {
		this.authorService = authorService;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
}
