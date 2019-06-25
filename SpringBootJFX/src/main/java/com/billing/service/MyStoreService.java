package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.MyStoreDetails;
import com.billing.starter.MyStoreApplication;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class MyStoreService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(MyStoreApplication.class);

	private static final String UPDATE_STORE_DETAILS = "UPDATE MY_STORE_DETAILS SET NAME=?,"
			+ "ADDRESS=?, ADDRESS2=?,CITY=?,DISTRICT=?,STATE=?,PHONE=?,CST_NUMBER=?,PAN_NUMBER=?,VAT_NUMBER=?,ELECTRICITY_NO=?,"
			+ " OWNER_NAME=?,MOBILE_NO=?,GST_NO=?";

	private static final String GET_MY_STORE_DETAILS = "SELECT * FROM MY_STORE_DETAILS";

	public MyStoreDetails getMyStoreDetails() {
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
				myStoreDetails.setImage(rs.getBytes("IMAGE"));
				myStoreDetails.setGstNo(rs.getString("GST_NO"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return myStoreDetails;
	}

	// Save My Store Details
	public boolean updateStoreDetails(MyStoreDetails myStoreDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		StringBuilder sb = new StringBuilder(UPDATE_STORE_DETAILS);

		try {
			if (myStoreDetails != null) {
				conn = dbUtils.getConnection();
				if (myStoreDetails.getImagePath() != null) {
					sb.append(",IMAGE=? WHERE STORE_ID=?");
					stmt = conn.prepareStatement(sb.toString());
				} else {
					sb.append(" WHERE STORE_ID=?");
					stmt = conn.prepareStatement(sb.toString());
				}
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
				if (myStoreDetails.getImagePath() != null) {
					stmt.setBlob(15, myStoreDetails.getImagePath());
					stmt.setInt(16, myStoreDetails.getMyStoreId());
				} else {
					stmt.setInt(15, myStoreDetails.getMyStoreId());
				}

				int i = stmt.executeUpdate();
				if (i > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);
		} finally {
			AppUtils.closeStatment(stmt);
		}
		return flag;
	}
}
