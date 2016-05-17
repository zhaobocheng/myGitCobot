package com.bop.bopmain.dao.internal;

import java.util.ArrayList;

import com.bop.bopmain.dao.Dept;
import com.bop.bopmain.dao.Person;
import com.bop.hibernate.EntityRegister;

public class EntityRegisterImpl implements EntityRegister {

	@Override
	public Class<?>[] register() {
		ArrayList<Class<?>> list = new ArrayList<Class<?>>();
		
		list.add(Dept.class);
		list.add(Person.class);
		
		return list.toArray(new Class[list.size()]);
	}
}
