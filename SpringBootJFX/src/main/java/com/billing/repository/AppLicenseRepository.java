package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class AppLicenseRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final String INS_APP_SECURITY_DATA = "INSERT INTO APP_SECURITY_DATA (SECURITY_DATA) VALUES(?)";

	private static final String DELETE_APP_SECURITY_DATA = "DELETE FROM APP_SECURITY_DATA";

	private static final String SELECT_APP_SECURITY_DATA = "SELECT * FROM APP_SECURITY_DATA";

	private static final Logger logger = LoggerFactory.getLogger(AppLicenseRepository.class);

	public String getAppSecurityData() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String appSecData = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_APP_SECURITY_DATA);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				appSecData = appUtils.dec(rs.getString("SECURITY_DATA"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return appSecData;
	}

	public boolean insertAppSecurityData(String key) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			if (key != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_APP_SECURITY_DATA);
				stmt.setString(1, key);

				int i = stmt.executeUpdate();
				if (i > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	public boolean deleteAppSecurityData() {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_APP_SECURITY_DATA);

			int i = stmt.executeUpdate();
			if (i > 0) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

}
