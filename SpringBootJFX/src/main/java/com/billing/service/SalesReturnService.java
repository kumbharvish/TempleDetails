package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.ItemDetails;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Service
public class SalesReturnService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(SalesReturnService.class);

	private static final String INS_BILL_DETAILS = "INSERT INTO SALES_RETURN_DETAILS (RETURN_NUMBER,RETURN_DATE,COMMENTS,BILL_NUMBER,CUST_MOB_NO,BILL_DATE,"
			+ "BILL_PAYMENT_MODE,NO_OF_ITEMS,TOTAL_QTY,PAYMENT_MODE,RETURN_TOTAL_AMOUNT,BILL_NET_SALES_AMOUNT,NEW_BILL_NET_SALES_AMOUNT,TAX,DISCOUNT,TAX_AMOUNT,DISCOUNT_AMOUNT,SUB_TOTAL,GRAND_TOTAL,RETURN_PURCHASE_AMT)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String INS_BILL_ITEM_DETAILS = "INSERT INTO SALES_RETURN_ITEMS_DETAILS (RETURN_NUMBER,ITEM_NO,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,AMOUNT) VALUES(?,?,?,?,?,?)";

	private static final String UPDATE_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY+? WHERE PRODUCT_ID=?";

	private static final String GET_RETURN_DETAILS = "SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE  SRD.CUST_MOB_NO=CD.CUST_MOB_NO"
			+ " AND BILL_NUMBER=?";

	private static final String GET_RETURN_DETAILS_FOR_RETURN_NUMBER = "SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE  SRD.CUST_MOB_NO=CD.CUST_MOB_NO"
			+ " AND RETURN_NUMBER=?";

	private static final String IS_SALES_RETURED = "SELECT RETURN_NUMBER FROM SALES_RETURN_DETAILS WHERE BILL_NUMBER=?";

	private static final String SELECT_ITEM_DETAILS = "SELECT SRID.*,PD.PRODUCT_NAME FROM SALES_RETURN_ITEMS_DETAILS SRID,PRODUCT_DETAILS PD WHERE RETURN_NUMBER=? AND SRID.ITEM_NO=PD.PRODUCT_ID";

	// Save Return Details
	public StatusDTO saveReturnDetails(ReturnDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_BILL_DETAILS);
				stmt.setInt(1, bill.getReturnNumber());
				stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
				stmt.setString(3, bill.getComments());
				stmt.setInt(4, bill.getBillNumber());
				stmt.setLong(5, bill.getCustomerMobileNo());
				stmt.setDate(6, bill.getBillDate());
				stmt.setString(7, bill.getBillPaymentMode());
				stmt.setInt(8, bill.getNoOfItems());
				stmt.setDouble(9, bill.getTotalQuantity());
				stmt.setString(10, bill.getReturnpaymentMode());
				stmt.setDouble(11, bill.getTotalAmount());
				stmt.setDouble(12, bill.getBillNetSalesAmt());
				stmt.setDouble(13, bill.getNewBillnetSalesAmt());
				stmt.setDouble(14, bill.getTax());
				stmt.setDouble(15, bill.getDiscount());
				stmt.setDouble(16, bill.getTaxAmount());
				stmt.setDouble(17, bill.getDiscountAmount());
				stmt.setDouble(18, bill.getSubTotal());
				stmt.setDouble(19, bill.getGrandTotal());
				stmt.setDouble(20, bill.getReturnPurchaseAmt());
				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					saveReturnItemDetails(bill.getItemDetails());
					updateProductStock(bill.getItemDetails());
				}
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

	// Update Item Stock
	private StatusDTO updateProductStock(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					status.setStatusCode(0);
					System.out.println("Sales Return Product Stock  updated");
				}
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

	// Save Return Item Details
	private StatusDTO saveReturnItemDetails(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(INS_BILL_ITEM_DETAILS);
				for (ItemDetails item : itemList) {
					stmt.setInt(1, item.getBillNumber());
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(3, item.getMRP());
					stmt.setDouble(4, item.getRate());
					stmt.setDouble(5, item.getQuantity());
					stmt.setDouble(6, item.getAmount());
					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					status.setStatusCode(0);
					System.out.println("Sales Return Items Details saved!");
				}
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

	// Get Sales Return Details for bill Number
	public ReturnDetails getReturnDetails(Integer billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ReturnDetails returnDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_RETURN_DETAILS);
			stmt.setInt(1, billNumber);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				returnDetails = new ReturnDetails();
				returnDetails.setReturnNumber(rs.getInt("RETURN_NUMBER"));
				returnDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				returnDetails.setBillDate(rs.getDate("BILL_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getTimestamp("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setReturnpaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewBillnetSalesAmt(rs.getDouble("NEW_BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillNetSalesAmt(rs.getDouble("BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillPaymentMode(rs.getString("BILL_PAYMENT_MODE"));
				returnDetails.setTax(rs.getDouble("TAX"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				returnDetails.setTaxAmount(rs.getDouble("TAX_AMOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				returnDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return returnDetails;
	}

	// Get Sales Return Details for Return Number
	public ReturnDetails getReturnDetailsForReturnNo(Integer returnNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ReturnDetails returnDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_RETURN_DETAILS_FOR_RETURN_NUMBER);
			stmt.setInt(1, returnNumber);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				returnDetails = new ReturnDetails();
				returnDetails.setReturnNumber(rs.getInt("RETURN_NUMBER"));
				returnDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				returnDetails.setBillDate(rs.getDate("BILL_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getTimestamp("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setReturnpaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewBillnetSalesAmt(rs.getDouble("NEW_BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillNetSalesAmt(rs.getDouble("BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillPaymentMode(rs.getString("BILL_PAYMENT_MODE"));
				returnDetails.setTax(rs.getDouble("TAX"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				returnDetails.setTaxAmount(rs.getDouble("TAX_AMOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				returnDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return returnDetails;
	}

	// Update Bill Items details
	private StatusDTO updateBillItemsDetails(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					status.setStatusCode(0);
					System.out.println("Sales Return Product Stock  updated");
				}
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

	// Is Sales Return Available for this Bill
	public StatusDTO isSalesReturned(int billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO(-1);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(IS_SALES_RETURED);
			stmt.setInt(1, billNumber);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				status.setStatusCode(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			return status;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Get Item Details for Bill Number
	public List<ItemDetails> getReturnedItemDetails(Integer returnNumber) {
		List<ItemDetails> itemDetailsList = new ArrayList<ItemDetails>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ItemDetails itemDetails = null;

		conn = dbUtils.getConnection();
		try {
			stmt = conn.prepareStatement(SELECT_ITEM_DETAILS);

			stmt.setInt(1, returnNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				itemDetails = new ItemDetails();
				itemDetails.setItemNo(rs.getInt("ITEM_NO"));
				itemDetails.setItemName(rs.getString("PRODUCT_NAME"));
				itemDetails.setMRP(rs.getDouble("ITEM_MRP"));
				itemDetails.setRate(rs.getDouble("ITEM_RATE"));
				itemDetails.setQuantity(rs.getInt("ITEM_QTY"));

				itemDetailsList.add(itemDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}

		return itemDetailsList;
	}

	// Get Bill Details
	public List<ReturnDetails> getReturnDetails(Date fromDate, Date toDate, Long customerMobile) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ReturnDetails returnDetails = null;
		List<ReturnDetails> returnDetailsList = new ArrayList<ReturnDetails>();
		StringBuilder SELECT_BILL_DETAILS = new StringBuilder(
				"SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE DATE(SRD.RETURN_DATE) BETWEEN ? AND ?  AND SRD.CUST_MOB_NO=CD.CUST_MOB_NO ");

		String ORDER_BY_CLAUSE = "ORDER BY SRD.RETURN_DATE ASC";
		String CUSTOMER_MOB_QEUERY = " AND SRD.CUST_MOB_NO LIKE ? ";
		try {
			if (fromDate == null) {
				fromDate = new Date(1947 / 01 / 01);
			}
			if (toDate == null) {
				toDate = new Date(System.currentTimeMillis());
			}
			if (customerMobile != null) {
				SELECT_BILL_DETAILS.append(CUSTOMER_MOB_QEUERY);
			}
			SELECT_BILL_DETAILS.append(ORDER_BY_CLAUSE);
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_DETAILS.toString());
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			if (customerMobile != null) {
				stmt.setString(3, "%" + customerMobile + "%");
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				returnDetails = new ReturnDetails();
				returnDetails.setReturnNumber(rs.getInt("RETURN_NUMBER"));
				returnDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				returnDetails.setBillDate(rs.getDate("BILL_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getTimestamp("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setReturnpaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewBillnetSalesAmt(rs.getDouble("NEW_BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillNetSalesAmt(rs.getDouble("BILL_NET_SALES_AMOUNT"));
				returnDetails.setBillPaymentMode(rs.getString("BILL_PAYMENT_MODE"));
				returnDetails.setTax(rs.getDouble("TAX"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				returnDetails.setTaxAmount(rs.getDouble("TAX_AMOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				returnDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));

				returnDetailsList.add(returnDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return returnDetailsList;
	}

}
