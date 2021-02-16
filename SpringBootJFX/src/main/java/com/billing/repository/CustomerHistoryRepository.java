package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.dto.BillDetails;
import com.billing.dto.CustomerPaymentHistory;
import com.billing.dto.CustomerProfit;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class CustomerHistoryRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(CustomerHistoryRepository.class);

	private static final String GET_ALL_CUSTOMERS_HISTORY = "SELECT CPH.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_PAYMENT_HISTORY CPH,CUSTOMER_DETAILS CD WHERE CPH.CUST_ID=? AND CPH.CUST_ID=CD.CUST_ID ORDER BY TIMESTAMP DESC";

	private static final String GET_ALL_CUST_BILLS = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.CUST_ID=? AND CBD.CUST_ID=CD.CUST_ID ORDER BY BILL_DATE_TIME DESC";

	private static final String CUSTOMER_WISE_PROFIT = "SELECT CUST_MOB_NO,CUST_NAME,SUM(NET_SALES_AMOUNT) AS SUM_BILL_AMT ,SUM(BILL_PURCHASE_AMT)AS SUM_BILL_PUR_AMT,SUM(NO_OF_ITEMS) AS TOTAL_NO_OF_ITEMS,SUM(BILL_QUANTITY)  AS TOTAL_QUANTITY FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ? GROUP BY CUST_ID";

	// Get All Customers Payment History
	public List<CustomerPaymentHistory> getAllCustomersPayHistory(int customerId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		CustomerPaymentHistory customer = null;
		List<CustomerPaymentHistory> customerList = new ArrayList<CustomerPaymentHistory>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_CUSTOMERS_HISTORY);
			stmt.setLong(1, customerId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				customer = new CustomerPaymentHistory();
				customer.setCustMobNo(rs.getLong("CUST_MOB_NO"));
				customer.setCustName(rs.getString("CUSTOMER_NAME"));
				customer.setClosingBlanace(rs.getDouble("AMOUNT"));
				customer.setEntryDate(rs.getString("TIMESTAMP"));
				customer.setStatus(rs.getString("STATUS"));
				customer.setNarration(rs.getString("NARRATION"));
				customer.setCredit(rs.getDouble("CREDIT"));
				customer.setDebit(rs.getDouble("DEBIT"));
				customer.setCustId(rs.getInt("CUST_ID"));
				
				customerList.add(customer);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return customerList;
	}

	// Get Bill Details
	public List<BillDetails> getBillDetails(Integer customerId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		List<BillDetails> billDetailsList = new ArrayList<BillDetails>();
		try {
			if (customerId != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(GET_ALL_CUST_BILLS);
				System.out.println("GET_ALL_CUST_BILLS " + GET_ALL_CUST_BILLS);
				stmt.setLong(1, customerId);
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					billDetails = new BillDetails();
					billDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
					billDetails.setTimestamp(rs.getString("BILL_DATE_TIME"));
					billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
					billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
					billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
					billDetails.setTotalQuantity(rs.getDouble("BILL_QUANTITY"));
					billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
					billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
					billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
					billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
					billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
					billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));
					billDetails.setGstType(rs.getString("GST_TYPE"));
					billDetails.setGstAmount(rs.getDouble("GST_AMOUNT"));
					billDetails.setCustomerId(rs.getInt("CUST_ID"));
					
					billDetailsList.add(billDetails);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	// Customer wise profit report
	public List<CustomerProfit> getCustomerWiseProfit(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		CustomerProfit customer = null;
		List<CustomerProfit> customerList = new ArrayList<CustomerProfit>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(CUSTOMER_WISE_PROFIT);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				customer = new CustomerProfit();
				customer.setCustName(rs.getString("CUST_NAME"));
				customer.setCustMobileNumber(rs.getLong("CUST_MOB_NO"));
				customer.setSumOfBillAmt(rs.getDouble("SUM_BILL_AMT"));
				customer.setSumOfBillPurAmt(rs.getDouble("SUM_BILL_PUR_AMT"));
				customer.setTotalNoOfItems(rs.getInt("TOTAL_NO_OF_ITEMS"));
				customer.setTotalQty(rs.getDouble("TOTAL_QUANTITY"));

				customerList.add(customer);
				Collections.sort(customerList);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return customerList;
	}

}
