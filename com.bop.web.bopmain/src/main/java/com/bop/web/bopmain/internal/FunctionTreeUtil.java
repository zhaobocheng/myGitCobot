package com.bop.web.bopmain.internal;

import java.util.Set;

import com.bop.common.StringUtility;
import com.bop.json.ExtObject;
import com.bop.json.ExtObjectCollection;
import com.bop.module.function.Menu;
import com.bop.module.function.MenuItem;
import com.bop.module.function.service.FunctionTree;

public class FunctionTreeUtil {
	public static String toNavHtml(FunctionTree tree, MenuItem m) {
		return toNavHtml(tree, m, m.getParent().getId(), "");
	}
	
	public static ExtObjectCollection toJson(FunctionTree tree) {
		Set<MenuItem> children = tree.getByParentId("");
		ExtObjectCollection eoc = new ExtObjectCollection();
		
		if(children == null) return eoc;
		
		for(MenuItem c : children) {
			ExtObject eo = new ExtObject();
			eo.add("id", c.getId());
			eo.add("name", c.getName());
			eo.add("url", getNodeUrl(tree, c));
			eo.add("imgPath", c.getIconPath());
			eo.add("pId", "");
			eo.add("open", true);
			
			eoc.add(eo);
			if(c instanceof Menu) {
				ExtObjectCollection eocc = toJson(tree, (Menu)c);
				eoc.addAll(eocc);
			}
		}
		
		return eoc;
	}
	
	public static ExtObjectCollection toJson(FunctionTree tree, Menu item) {
		Set<MenuItem> children = tree.getByParentId(item.getId());
		ExtObjectCollection eoc = new ExtObjectCollection();
		
		for(MenuItem c : children) {
			ExtObject eo = new ExtObject();
			eo.add("id", c.getId());
			eo.add("name", c.getName());
			eo.add("url", getNodeUrl(tree, c));
			eo.add("imgPath", c.getIconPath());
			eo.add("pId", item.getId());
			eo.add("open", true);
			eo.add("target", "_self");
			
			eoc.add(eo);
			if(c instanceof Menu) {
				ExtObjectCollection eocc = toJson(tree, (Menu)c);
				eoc.addAll(eocc);
			}
		}
		
		return eoc;
	}
	
	public static String toNavHtml(FunctionTree tree, MenuItem m, String sid, String pid){
		StringBuffer sb = new StringBuffer();
		
		if(StringUtility.isNullOrEmpty(pid)) {
			pid = "";
		}
		
		Set<MenuItem> menus = tree.getByParentId(m.getId());
		if(menus == null) return "";
		
		for(MenuItem child : menus) {
			sb.append("<tr>");
			sb.append("<td>");
			int level = getMenuLevel(tree, m);
			for(int i = 0; i < level-1; i++) {
				sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			String IconPath="/bopmain/images/function_empty.gif";
			sb.append("<img src=\"" + IconPath + "\" width=\"16\" height=\"16\">");
			
			sb.append(child.getName() + "</td>");
			if(StringUtility.isNullOrEmpty(child.getUrl())){
				sb.append("<td>" + (child.getDescription() == null ? "" : child.getDescription()) + "&nbsp</td>"); // 描述为空的时候，前台样式不一样了，所以加上一个&nbsp
			} else {
				String url = getNodeUrl(tree, child);
				sb.append("<td><a href=\""+url+"\">" + (child.getDescription() == null ? "" : child.getDescription()) + "&nbsp</a></td>");
			}
			
			sb.append("</tr>");
			sb.append(toNavHtml(tree, child, sid, pid));
		}
		
		return sb.toString();
	}
	
	public static String toFunctionHtml(FunctionTree tree, String currentSid, String currentFid, String pid) {
		Set<MenuItem> menus = tree.getByParentId(currentSid);
		boolean projectView = !(StringUtility.isNullOrEmpty(pid));
		
		if(menus == null) return "";
		
		String html = "";
		for(MenuItem m : menus) {
			if(m.isNeedProject() == projectView)
				html += getMenuHtml(tree, m, currentSid, pid);
		}
		
		return html;
	}
	
	public static String toSystemHtml(FunctionTree tree, String pid, StringBuilder defaultUrl) {
		Set<MenuItem> menus = tree.getByParentId("");
		boolean projectView = !(StringUtility.isNullOrEmpty(pid));
		if(menus == null) return "";
		
		String html = "";
		for(MenuItem m : menus) {
			if(m.isNeedProject() == projectView) {
				String url = StringUtility.isNullOrEmpty(m.getUrl()) ? "/desktop.cmd" : m.getUrl();
				if(url.contains("?")) {
					url += String.format("&sid=%s&pid=%s", m.getId(), pid);
				} else {
					url += String.format("?sid=%s&pid=%s", m.getId(), pid);
				}
				
				if(StringUtility.isNullOrEmpty(defaultUrl)) defaultUrl.append(url);
					
				html += String.format("<span><a id=\"%s\" href=\"%s\">%s</a></span>", m.getId(), url, m.getName());
			}
		}
		
		return html;
	}
	
	private static String getMenuHtml(FunctionTree tree, MenuItem m, String sid,String pid) {
		StringBuilder sb = new StringBuilder();
		int level = getMenuLevel(tree, m);
		if(level == 2) {
			if(m instanceof Menu) {
				sb.append("<div><span>" + m.getName() + "</span>");
				
				Menu mm = (Menu)m;
				for(MenuItem item : tree.getByParentId(mm.getId())){
					sb.append(getMenuHtml(tree, item, sid, pid));
				}
				
				sb.append("</div>");
			} else {
				String url = getNodeUrl(tree, m);
				String fix = getMenuFixHtml(m);
				sb.append(String.format("<a id=\"%s\" href=\"%s\" %s>%s%s</a>", m.getId(), url, "", m.getName(), fix));
			}
		} else if(level == 3) {
			String url = getNodeUrl(tree, m);
			String fix = getMenuFixHtml(m);
			sb.append(String.format("<a id=\"%s\" href=\"%s\" %s>%s%s</a>", m.getId(), url, "", m.getName(), fix));
		}
		
		return sb.toString();
	}
	
	private static String getMenuFixHtml(MenuItem m) {
		if(m.getMenuItemFix() == null) return "";
		
		int todo = 0;
		int total = 0;
		try {
			todo = m.getMenuItemFix().getTodos();
			total = m.getMenuItemFix().getTotals();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		if(todo > 0 || total > 0) sb.append("("); 
			
		if(todo > 0) sb.append(String.format("<font class=\"todoCount\">%s</font>", todo));
		if(total > 0) sb.append(String.format("%s<font class=\"totalCount\">%s</font>", (todo > 0 ? "/" : ""), total));
		
		if(todo > 0 || total > 0) sb.append(")");
		return sb.toString();
	}

	private static String getNodeUrl(FunctionTree tree, MenuItem m) {
		String url = m.getUrl();
		if(url == null) return "#";
		String fix = "fid=" + m.getId();
		
		if (url.contains("?"))
			url = url + "&" + fix;
		else
			url = url + "?" + fix;
		
		return url;
	}
	
	private static int getMenuLevel(FunctionTree tree, MenuItem m) {
		MenuItem pm = tree.getById(m.getId());
		int i = 0;
		while (pm != null) {
			pm = pm.getParent();
			i++;
		}

		return i;
	}
}
