//package com.bop.web.bopmain.internal;
//
//import org.hibernate.FlushMode;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.dao.DataAccessResourceFailureException;
//import org.springframework.orm.hibernate4.SessionFactoryUtils;
//import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
//
//public class CustomOpenSessionInViewFilter extends OpenSessionInViewFilter {
//	Logger log = LoggerFactory.getLogger(CustomOpenSessionInViewFilter.class);
//	private SessionFactory sessionFactory;
//	
//	@Override
//	protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
//		Session session = SessionFactoryUtils.openSession(sessionFactory);
//		session.setFlushMode(FlushMode.COMMIT);
//		
//		log.trace("create session:" + session.hashCode());
//        return session;
//	}
//	
//    protected SessionFactory lookupSessionFactory() {
//    	return this.sessionFactory;
//	}
//    
//    public void setSessionFactory(SessionFactory sessionFactory) {
//    	this.sessionFactory = sessionFactory;
//    }
//}
//
