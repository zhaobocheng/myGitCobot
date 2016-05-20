package com.bop.web.bopmain.sitemesh;

import java.io.IOException;

import org.sitemesh.SiteMeshContext;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.sitemesh.config.PathBasedDecoratorSelector;
import org.sitemesh.config.PathMapper;
import org.sitemesh.content.Content;
import org.sitemesh.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyConfigurableSiteMeshFilter extends ConfigurableSiteMeshFilter {
	private static final Logger log = LoggerFactory.getLogger(MyConfigurableSiteMeshFilter.class);

	@Override
	protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
		super.applyCustomConfiguration(builder);

		builder.addExcludedPath("/*Ajax.cmd*")
				.addExcludedPath("/*AjaxAction.cmd*")
				.addExcludedPath("/**/mainpage/*logon")
				.addExcludedPath("/**/mainpage/*logout")
				.addExcludedPath("/*theme=none*").addExcludedPath("/frameset*")
				.addExcludedPath("/run*").addExcludedPath("/preview*")
				.addExcludedPath("/parameter*")
				.addExcludedPath("/FeatureServlet*")
				.addExcludedPath("/GridWebServlet*")
				.addExcludedPath("/ResourceFiles/**");

		builder.addDecoratorPath("/*", "/theme/main/main_miniui.jsp");
		MyMetaTagBasedDecoratorSelector<WebAppContext> selector = new MyMetaTagBasedDecoratorSelector<WebAppContext>();
		
		selector.put("miniui", "/theme/main/main_miniui.jsp");
		selector.put("clean", "/theme/main/main_miniui.jsp?decorator=clean");
		selector.put("extwithbdsoft", "/theme/main/main_miniui.jsp?decorator=extwithbdsoft");
		
		builder.setCustomDecoratorSelector(selector);
	}
	
	
	class MyMetaTagBasedDecoratorSelector<C extends SiteMeshContext> extends PathBasedDecoratorSelector<C> {
		PathMapper<String[]> myPaths = new PathMapper<String[]>();
		
	    public MyMetaTagBasedDecoratorSelector<?> put(String contentPath, String... decoratorPaths) {
	        this.myPaths.put(contentPath, decoratorPaths);
	        return this;
	    }

	    public String[] selectDecoratorPaths(Content content, C siteMeshContext) throws IOException {
			String decorator = content.getExtractedProperties()
	                .getChild("meta")
	                .getChild("decorator")
	                .getValue();

	        if (decorator != null) {
	            String[] paths = this.myPaths.get(decorator);
	            if(paths == null) log.error("不可预知的sitemesh修饰" + decorator);
	            return paths;
	        }

	        return super.selectDecoratorPaths(content, siteMeshContext);
	    }
	}
}
