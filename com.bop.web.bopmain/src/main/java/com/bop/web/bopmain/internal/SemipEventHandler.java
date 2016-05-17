package com.bop.web.bopmain.internal;

import java.util.UUID;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.bop.event.EventParam;
import com.bop.module.log.OpLogger;
import com.bop.module.log.OpLoggerFactory;

public final class SemipEventHandler implements EventHandler {
	private OpLoggerFactory opLoggerFactory;
	@Override
	public void handleEvent(Event event) {
		EventParam p = EventParam.fromEvent(event);
		OpLogger logger = opLoggerFactory.getLogger(p.getProjectID(), p.getEntityTypeName());
		if (p.getContext() == null) {
			p.setContext(p.getEntityAction());
		} else {
			p.setContext(p.getEntityAction() + "：" + p.getContext());
		}
		
		logger.log(UUID.fromString(p.getEntityId()), p.getEntityName(), p.getContext(), p.getUserName());
	}
	
	public void setOpLoggerFactory(OpLoggerFactory opLoggerFactory) {
		this.opLoggerFactory = opLoggerFactory;
	}
}
