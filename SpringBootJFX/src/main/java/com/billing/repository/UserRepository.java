package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.billing.dto.Customer;
import com.billing.dto.StatusDTO;
import com.billing.dto.UserDetails;
import com.billing.service.GraphService;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class UserRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(GraphService.class);

	private static final String VALIDATE_USER = "SELECT FIRST_NAME,LAST_NAME,USER_ID,USERNAME,USER_TYPE FROM "
			+ "APP_USER_DETAILS WHERE USERNAME=? AND PASSWORD=? AND IS_ACTIVE='Y' ";

	private static final String GET_USER_DEATILS = "SELECT FIRST_NAME,LAST_NAME,USER_ID,USERNAME,MOBILE_NO,EMAIL FROM "
			+ "APP_USER_DETAILS WHERE USER_ID=?";

	private static final String UPDATE_USER_PWD = "UPDATE APP_USER_DETAILS SET PASSWORD=? WHERE USER_ID=? AND PASSWORD=?";

	private static final String UPDATE_USERNAME = "UPDATE APP_USER_DETAILS SET USERNAME=? WHERE USERNAME=? AND USER_ID=?";

	private static final String UPDATE_USER_DETAILS = "UPDATE APP_USER_DETAILS SET FIRST_NAME=?,LAST_NAME=?,EMAIL=?,MOBILE_NO=? WHERE USER_ID=?";

	private static final String UPDATE_USER_ACTIVE_STATUS = "UPDATE APP_USER_DETAILS SET IS_ACTIVE=? WHERE USER_TYPE=? AND USERNAME=?";

	private static final String ADD_USER = "INSERT INTO APP_USER_DETAILS (FIRST_NAME,LAST_NAME,EMAIL,MOBILE_NO,USER_TYPE,IS_ACTIVE,USERNAME,PASSWORD) "
			+ "VALUES(?,?,?,?,?,?,?,?)";

	public StatusDTO addUser(UserDetails user) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(ADD_USER);
			stmt.setString(1, user.getFirstName());
			stmt.setString(2, user.getLastName());
			stmt.setString(3, user.getEmail());
			stmt.setLong(4, user.getMobileNo());
			stmt.setString(5, "INTERNAL");
			stmt.setString(6, "Y");
			stmt.setString(7, user.getUserName());
			stmt.setString(8, user.getPassword());

			int records = stmt.executeUpdate();

			if (records > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public UserDetails validateUser(String userName, String password) {
		Connection conn = null;
		PreparedStatement stmt = null;
		UserDetails userDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(VALIDATE_USER);
			stmt.setString(1, userName);
			stmt.setString(2, appUtils.enc(password));
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				userDetails = new UserDetails();
				userDetails.setFirstName(rs.getString("FIRST_NAME"));
				userDetails.setLastName(rs.getString("LAST_NAME"));
				userDetails.setUserId(rs.getInt("USER_ID"));
				userDetails.setUserName(rs.getString("USERNAME"));
				userDetails.setUserType(rs.getString("USER_TYPE"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return userDetails;
	}

	public UserDetails getUserDetails(UserDetails userDtls) {
		Connection conn = null;
		PreparedStatement stmt = null;
		UserDetails userDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_USER_DEATILS);
			stmt.setInt(1, userDtls.getUserId());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				userDetails = new UserDetails();
				userDetails.setFirstName(rs.getString("FIRST_NAME"));
				userDetails.setLastName(rs.getString("LAST_NAME"));
				userDetails.setUserId(rs.getInt("USER_ID"));
				userDetails.setUserName(rs.getString("USERNAME"));
				userDetails.setEmail(rs.getString("EMAIL"));
				userDetails.setMobileNo(rs.getLong("MOBILE_NO"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return userDetails;
	}

	// Update username
	public StatusDTO changeUserName(UserDetails userDetails, String newUserName) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_USERNAME);
			stmt.setString(1, newUserName);
			stmt.setString(2, userDetails.getUserName());
			stmt.setInt(3, userDetails.getUserId());
			int records = stmt.executeUpdate();
			if (records > 0) {
				status.setStatusCode(0);
			} else {
				status.setStatusCode(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			DBUtils.closeConnection(stmt, conn);

		}
		return status;
	}

	// Update password
	public StatusDTO changePassword(UserDetails userDetails, String existingPwd, String newPassword) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_USER_PWD);
			stmt.setString(1, newPassword);
			stmt.setInt(2, userDetails.getUserId());
			stmt.setString(3, existingPwd);
			int records = stmt.executeUpdate();

			if (records > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Update Personal Details
	public StatusDTO updatePersonalDetails(UserDetails userDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_USER_DETAILS);
			stmt.setString(1, userDetails.getFirstName());
			stmt.setString(2, userDetails.getLastName());
			stmt.setString(3, userDetails.getEmail());
			stmt.setLong(4, userDetails.getMobileNo());
			stmt.setInt(5, userDetails.getUserId());

			int records = stmt.executeUpdate();

			if (records > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.toString());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Mark Admin as user as Inactive
	public StatusDTO markUserAsInactive(UserDetails userDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_USER_ACTIVE_STATUS);
			stmt.setString(1, "N");
			stmt.setString(2, userDetails.getUserType());
			stmt.setString(3, userDetails.getUserName());

			int records = stmt.executeUpdate();

			if (records > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.toString());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

}
