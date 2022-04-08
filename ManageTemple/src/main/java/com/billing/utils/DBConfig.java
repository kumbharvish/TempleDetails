package com.billing.utils;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DBConfig {
	
	@Autowired
	private Environment env;
	
    @Bean
	public DataSource dataSource() {
	        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
	        dataSourceBuilder.driverClassName(env.getProperty("spring.datasource.driverclassname"));
	        dataSourceBuilder.url(env.getProperty("spring.datasource.url"));
	        return dataSourceBuilder.build();   
	}
}