package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.StatusDTO;
import com.billing.main.MyStoreApplication;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class BillingService {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

	private static final String UPDATE_BILL_DETAILS = "UPDATE CUSTOMER_BILL_DETAILS SET CUST_MOB_NO=?,CUST_NAME=?,BILL_TAX=?,BILL_DISCOUNT=?,BILL_DISC_AMOUNT =?,"
			+ "PAYMENT_MODE=?,GRAND_TOTAL=?,NET_SALES_AMOUNT=?,GST_AMOUNT=?,GST_TYPE=? WHERE BILL_NUMBER=?";

	private static final String INS_OPENING_CASH = "INSERT INTO CASH_COUNTER " + "(DATE,AMOUNT)" + " VALUES(?,?)";

	private static final String UPDATE_OPENING_CASH = "UPDATE CASH_COUNTER SET AMOUNT=?" + " WHERE DATE=?";

	// Modify Bill Details
	public StatusDTO modifyBillDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO staus = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_BILL_DETAILS);
				stmt.setLong(1, bill.getCustomerMobileNo());
				stmt.setString(2, bill.getCustomerName());
				stmt.setDouble(3, bill.getDiscount());
				stmt.setDouble(4, bill.getDiscountAmt());
				stmt.setString(5, bill.getPaymentMode());
				stmt.setDouble(6, bill.getNetSalesAmt());
				stmt.setDouble(7, bill.getGstAmount());
				stmt.setString(8, bill.getGstType());
				stmt.setInt(9, bill.getBillNumber());
				int i = stmt.executeUpdate();
				if (i > 0) {
					staus.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			staus.setStatusCode(-1);
			staus.setException(e.getMessage());
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return staus;
	}

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
