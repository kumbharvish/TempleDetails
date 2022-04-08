package com.billing.main;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AppContext implements ApplicationContextAware {

	public ApplicationContext springContext;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.springContext= context;	
	}

	
}
