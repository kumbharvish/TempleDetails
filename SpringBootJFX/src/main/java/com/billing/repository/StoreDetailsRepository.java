package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.billing.dto.MyStoreDetails;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Repository
public class StoreDetailsRepository {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(StoreDetailsRepository.class);

	private static final String UPDATE_STORE_DETAILS = "UPDATE MY_STORE_DETAILS SET NAME=?,"
			+ "ADDRESS=?, ADDRESS2=?,CITY=?,DISTRICT=?,STATE=?,PHONE=?,CST_NUMBER=?,PAN_NUMBER=?,VAT_NUMBER=?,ELECTRICITY_NO=?,"
			+ " OWNER_NAME=?,MOBILE_NO=?,GST_NO=? WHERE STORE_ID=?";

	private static final String INS_STORE_DETAILS = "INSERT INTO MY_STORE_DETAILS (STORE_ID,NAME,ADDRESS,ADDRESS2,CITY,DISTRICT,"
			+ "STATE,PHONE,CST_NUMBER,PAN_NUMBER,VAT_NUMBER,ELECTRICITY_NO,OWNER_NAME,MOBILE_NO,GST_NO)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String GET_MY_STORE_DETAILS = "SELECT * FROM MY_STORE_DETAILS";

	public MyStoreDetails getMyStoreDetailsFromDB() {
		Connection conn = null;
		PreparedStatement stmt = null;
		MyStoreDetails myStoreDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_MY_STORE_DETAILS);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				myStoreDetails = new MyStoreDetails();
				myStoreDetails.setMyStoreId(rs.getInt("STORE_ID"));
				myStoreDetails.setStoreName(rs.getString("NAME"));
				myStoreDetails.setAddress(rs.getString("ADDRESS"));
				myStoreDetails.setAddress2(rs.getString("ADDRESS2"));
				myStoreDetails.setCity(rs.getString("CITY"));
				myStoreDetails.setDistrict(rs.getString("DISTRICT"));
				myStoreDetails.setState(rs.getString("STATE"));
				myStoreDetails.setPhone(rs.getLong("PHONE"));
				myStoreDetails.setCstNo(rs.getLong("CST_NUMBER"));
				myStoreDetails.setPanNo(rs.getString("PAN_NUMBER"));
				myStoreDetails.setVatNo(rs.getLong("VAT_NUMBER"));
				myStoreDetails.setElectricityNo(rs.getLong("ELECTRICITY_NO"));
				myStoreDetails.setOwnerName(rs.getString("OWNER_NAME"));
				myStoreDetails.setMobileNo(rs.getLong("MOBILE_NO"));
				myStoreDetails.setGstNo(rs.getString("GST_NO"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return myStoreDetails;
	}

	// Save My Store Details
	public StatusDTO updateStoreDetails(MyStoreDetails myStoreDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		status.setStatusCode(1); // Store Details not available

		try {
			if (myStoreDetails != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_STORE_DETAILS);

				stmt.setString(1, myStoreDetails.getStoreName());
				stmt.setString(2, myStoreDetails.getAddress());
				stmt.setString(3, myStoreDetails.getAddress2());
				stmt.setString(4, myStoreDetails.getCity());
				stmt.setString(5, myStoreDetails.getDistrict());
				stmt.setString(6, myStoreDetails.getState());
				stmt.setLong(7, myStoreDetails.getPhone());
				stmt.setLong(8, myStoreDetails.getCstNo());
				stmt.setString(9, myStoreDetails.getPanNo());
				stmt.setLong(10, myStoreDetails.getVatNo());
				stmt.setLong(11, myStoreDetails.getElectricityNo());
				stmt.setString(12, myStoreDetails.getOwnerName());
				stmt.setLong(13, myStoreDetails.getMobileNo());
				stmt.setString(14, myStoreDetails.getGstNo());
				stmt.setInt(15, myStoreDetails.getMyStoreId());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
			logger.error("Exception: ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Save My Store Details
	public StatusDTO addStoreDetails(MyStoreDetails myStoreDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();

		try {
			if (myStoreDetails != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_STORE_DETAILS);

				stmt.setInt(1, 1);
				stmt.setString(2, myStoreDetails.getStoreName());
				stmt.setString(3, myStoreDetails.getAddress());
				stmt.setString(4, myStoreDetails.getAddress2());
				stmt.setString(5, myStoreDetails.getCity());
				stmt.setString(6, myStoreDetails.getDistrict());
				stmt.setString(7, myStoreDetails.getState());
				stmt.setLong(8, myStoreDetails.getPhone());
				stmt.setLong(9, myStoreDetails.getCstNo());
				stmt.setString(10, myStoreDetails.getPanNo());
				stmt.setLong(11, myStoreDetails.getVatNo());
				stmt.setLong(12, myStoreDetails.getElectricityNo());
				stmt.setString(13, myStoreDetails.getOwnerName());
				stmt.setLong(14, myStoreDetails.getMobileNo());
				stmt.setString(15, myStoreDetails.getGstNo());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
			logger.error("Exception: ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
}
