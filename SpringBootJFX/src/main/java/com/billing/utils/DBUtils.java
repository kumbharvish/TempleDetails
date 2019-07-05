package com.billing.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
		Connection con = null;
		try {
			con = dataSource.getConnection();
		} catch (Exception e) {
			logger.error("Get Connection Exception :", e);
			e.printStackTrace();
		}
		return con;
	}

	public static void closeConnection(Statement stmt, Connection conn) {

		try {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException se2) {
			AppUtils.logger.info("close Statment and Connection : Exception : ", se2);
		}
	}

}
