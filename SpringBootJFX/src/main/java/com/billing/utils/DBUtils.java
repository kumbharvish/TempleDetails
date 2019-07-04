package com.billing.utils;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(DBUtils.class);

	@Autowired
	DataSource dataSource;
	
	public Connection getConnection() {
		System.out.println(" #################### Get Connection ##############");
		Connection con = null;
			try {
				con = dataSource.getConnection();
			} catch (Exception e) {
				logger.error("Get Connection Exception :",e);
				e.printStackTrace();
			}
		return con;
	}

}
