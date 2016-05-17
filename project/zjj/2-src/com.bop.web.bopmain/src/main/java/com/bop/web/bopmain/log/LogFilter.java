package com.bop.web.bopmain.log;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class LogFilter implements Filter {
	private final Logger log = LoggerFactory.getLogger(LogFilter.class); 
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub   初始化加载

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		String logid = UUID.randomUUID().toString();
		
		//日志记录内容 操作用户、用户IP、操作的url、传递的参数。
		//这里针对日志管理，做简单的记录，如果需要日志内容，这通过访问路径映射，得到对应的内容
		HttpServletRequest req = (HttpServletRequest) request;
		String method = req.getMethod();			 //得到请求的方式 get或者post
		Principal userName = req.getUserPrincipal(); //当前登陆人的登陆名称

		String context = req.getServletPath();
		String context2 = req.getRequestURI();	 //请求的路径路径
		StringBuffer url = req.getRequestURL();  //请求的完整地址，包括请求协议类型，IP，端口，路径 ，这个路径包括每次的数据请求
		String ieurl = req.getHeader("referer");	//浏览器显示的请求地址，
		String query = req.getQueryString();		//请求路径后面的参数
		String accept = req.getHeader("accept");	//请求的头部信息
		String userAgent = req.getHeader("user-agent");
		String ip = req.getRemoteAddr();


		/**
		 * 一下是两种得到请求的formdate的方法
		 */
		
/*        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                	System.out.println(paramName+"-------------"+paramValue+"---------"); 
                }
            }
        }*/

        Map formmap = request.getParameterMap();
        StringBuffer formdate = new StringBuffer();
        for(Object key:formmap.keySet()){
        	formdate.append(key+"="+request.getParameter(key.toString())+"&");
        }

        MDC.put("logid", logid);
		MDC.put("method", method);
		MDC.put("userName", userName == null ? "" : userName.getName());
		MDC.put("ip", ip);
		MDC.put("url", url.toString());
		MDC.put("queryString", query == null ? "" : query);
		MDC.put("formdatestring", formdate.toString());
		MDC.put("title", "日志标题");
		MDC.put("content", "日志内容");

/*		if("post".equalsIgnoreCase(method) || "delete".equalsIgnoreCase(method))
			log.warn("");
		else if("get".equalsIgnoreCase(method))
			log.debug("");
*/
	//	log.warn("");
	//	log.debug("");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
