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
import com.billing.utils.DBUtils;

@Service
public class BillingService {
	
	@Autowired
	DBUtils dbUtils;
	
	 private static final Logger logger = LoggerFactory.getLogger(BillingService.class);
	
	private static final String UPDATE_BILL_DETAILS = "UPDATE CUSTOMER_BILL_DETAILS SET CUST_MOB_NO=?,CUST_NAME=?,BILL_TAX=?,BILL_DISCOUNT=?,BILL_DISC_AMOUNT =?," +
			"PAYMENT_MODE=?,GRAND_TOTAL=?,NET_SALES_AMOUNT=? WHERE BILL_NUMBER=?";
	
	private static final String DELETE_BILL_DETAILS = "DELETE FROM CUSTOMER_BILL_DETAILS WHERE BILL_NUMBER=?";
	
	private static final String DELETE_BILL_ITEM_DETAILS = "DELETE FROM BILL_ITEM_DETAILS WHERE BILL_NUMBER=?";
	
	private static final String UPDATE_PRODUCT_STOCK ="UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY+? WHERE PRODUCT_ID=?";
	
	private static final String INS_OPENING_CASH = "INSERT INTO CASH_COUNTER " 
			+ "(DATE,AMOUNT)" 
			+ " VALUES(?,?)";
	
	private static final String UPDATE_OPENING_CASH = "UPDATE CASH_COUNTER SET AMOUNT=?" 
			+" WHERE DATE=?";
	
	private static final String NEW_BILL_NUMBER = "SELECT (MAX(BILL_NUMBER)+1) AS BILL_NO FROM CUSTOMER_BILL_DETAILS ";
	
	//Modify Bill Details
	public StatusDTO modifyBillDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO staus = new StatusDTO();
		try {
			if(bill!=null){
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_BILL_DETAILS);
				stmt.setLong(1, bill.getCustomerMobileNo());
				stmt.setString(2, bill.getCustomerName());
				stmt.setDouble(3, bill.getTax());
				stmt.setDouble(4, bill.getDiscount());
				stmt.setDouble(5, bill.getDiscountAmt());
				stmt.setString(6, bill.getPaymentMode());
				stmt.setDouble(7, bill.getGrandTotal());
				stmt.setDouble(8, bill.getNetSalesAmt());
				stmt.setInt(9, bill.getBillNumber());
				int i = stmt.executeUpdate();
				if(i>0){
					staus.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			staus.setStatusCode(-1);
			staus.setException(e.getMessage());
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return staus;
	}
	//Delete Bill Details including Items
	public StatusDTO deleteBillDetails(int billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(DELETE_BILL_DETAILS);
				stmt.setInt(1,billNumber);
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
					status = deleteBillItemDetails(billNumber);
				}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ",e);
			return status;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
	
	//Delete bill only Bill Item Details
	public StatusDTO deleteBillItemDetails(int billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(DELETE_BILL_ITEM_DETAILS);
				stmt.setInt(1,billNumber);
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
				}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ",e);
			return status;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	//Update Product Stock
	public StatusDTO updateDeletedBillProductStock(List <ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();		
		try {
			if(!itemList.isEmpty()){
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for(ItemDetails item : itemList){
					stmt.setInt(2, item.getItemNo());
					stmt.setInt(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if(batch.length == itemList.size()){
					status.setStatusCode(0);
					System.out.println("Product Stock  updated");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			logger.info("Exception : ",e);
			return status;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
	
	
	//Add Opening Cash
	
	public StatusDTO addOpeningCash(double amount) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_OPENING_CASH);
				stmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
				stmt.setDouble(2, amount);
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
				}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
	
	//Update Opening Cash
	
	public StatusDTO updateOpeningCash(double amount,Date date) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_OPENING_CASH);
				stmt.setDouble(1, amount);
				stmt.setDate(2, date);
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
				}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}
	
	public Integer getNewBillNumber() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Integer newBillNumber=0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(NEW_BILL_NUMBER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				newBillNumber = rs.getInt("BILL_NO");
				if(newBillNumber==0) {
					newBillNumber=1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return newBillNumber;
	}
}
