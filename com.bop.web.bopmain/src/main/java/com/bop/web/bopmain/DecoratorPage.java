package com.bop.web.bopmain;

import com.bop.web.rest.Action;
import com.bop.web.rest.Controller;

@Controller
public class DecoratorPage {
	
	@Action
	public String def() {
		return "<h1>hello, world!</h1>";
	}
}
