package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class BillingService {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

	private static final String INS_OPENING_CASH = "INSERT INTO CASH_COUNTER " + "(DATE,AMOUNT)" + " VALUES(?,?)";

	private static final String UPDATE_OPENING_CASH = "UPDATE CASH_COUNTER SET AMOUNT=?" + " WHERE DATE=?";


	// Add Opening Cash

	public StatusDTO addOpeningCash(double amount) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(INS_OPENING_CASH);
			stmt.setString(1, appUtils.getTodaysDate());
			stmt.setDouble(2, amount);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Update Opening Cash

	public StatusDTO updateOpeningCash(double amount, String date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_OPENING_CASH);
			stmt.setDouble(1, amount);
			stmt.setString(2, date);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

}
