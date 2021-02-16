package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.constants.AppConstants;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.ReturnDetails;
import com.billing.dto.StatusDTO;
import com.billing.service.ProductService;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class SalesReturnRepository {

	private static final Logger logger = LoggerFactory.getLogger(SalesReturnRepository.class);

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	private static final String SAVE_RETURN_DETAILS = "INSERT INTO SALES_RETURN_DETAILS (RETURN_NUMBER,RETURN_DATE,COMMENTS,INVOICE_NUMBER,CUST_MOB_NO,INVOICE_DATE,"
			+ "GST_TYPE,NO_OF_ITEMS,TOTAL_QTY,PAYMENT_MODE,RETURN_TOTAL_AMOUNT,INVOICE_NET_SALES_AMOUNT,NEW_INVOICE_NET_SALES_AMOUNT,GST_AMOUNT,DISCOUNT,DISCOUNT_AMOUNT,SUB_TOTAL,RETURN_PURCHASE_AMT,CREATED_BY,CUST_ID)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String SAVE_RETURN_ITEM_DETAILS = "INSERT INTO SALES_RETURN_ITEMS_DETAILS (RETURN_NUMBER,ITEM_NO,ITEM_NAME,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,AMOUNT,PURCHASE_AMOUNT,GST_RATE,GST_NAME,CGST,SGST,GST_AMOUNT,GST_TAXABLE_AMT,GST_INCLUSIVE_FLAG,DISC_PERCENT,DISC_AMOUNT,UNIT,HSN) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String GET_RETURN_DETAILS = "SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE  SRD.CUST_ID=CD.CUST_ID"
			+ " AND INVOICE_NUMBER=?";

	private static final String GET_RETURN_DETAILS_FOR_RETURN_NUMBER = "SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE  SRD.CUST_ID=CD.CUST_ID"
			+ " AND RETURN_NUMBER=?";

	private static final String IS_SALES_RETURED = "SELECT RETURN_NUMBER FROM SALES_RETURN_DETAILS WHERE INVOICE_NUMBER=?";

	private static final String GET_RETURN_ITEM_DETAILS = "SELECT SRID.*,PD.PRODUCT_NAME FROM SALES_RETURN_ITEMS_DETAILS SRID,PRODUCT_DETAILS PD WHERE RETURN_NUMBER=? AND SRID.ITEM_NO=PD.PRODUCT_ID";

	private static final String NEW_RETURN_NUMBER = "SELECT (MAX(RETURN_NUMBER)+1) AS RETURN_NUMBER FROM SALES_RETURN_DETAILS ";

	private static final String GET_RETURN_DETAILS_WITHIN_DATE_RANGE = "SELECT SRD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM SALES_RETURN_DETAILS SRD,CUSTOMER_DETAILS CD WHERE DATE(SRD.RETURN_DATE) BETWEEN ? AND ?  AND SRD.CUST_ID=CD.CUST_ID ORDER BY SRD.RETURN_DATE DESC";

	public Integer getNewReturnNumber() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Integer newReturnNumber = 0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(NEW_RETURN_NUMBER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				newReturnNumber = rs.getInt("RETURN_NUMBER");
				if (newReturnNumber == 0) {
					newReturnNumber = 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return newReturnNumber;
	}

	// Save Return Details
	public StatusDTO saveReturnDetails(ReturnDetails returnDetails) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		boolean isItemsSaved = false;
		boolean isStockUpdated = false;
		try {
			if (returnDetails != null) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(SAVE_RETURN_DETAILS);
				stmt.setInt(1, returnDetails.getReturnNumber());
				stmt.setString(2, returnDetails.getTimestamp());
				stmt.setString(3, returnDetails.getComments());
				stmt.setInt(4, returnDetails.getInvoiceNumber());
				stmt.setLong(5, returnDetails.getCustomerMobileNo());
				stmt.setString(6, returnDetails.getInvoiceDate());
				stmt.setString(7, returnDetails.getGstType());
				stmt.setInt(8, returnDetails.getNoOfItems());
				stmt.setDouble(9, returnDetails.getTotalQuantity());
				stmt.setString(10, returnDetails.getPaymentMode());
				stmt.setDouble(11, returnDetails.getTotalReturnAmount());
				stmt.setDouble(12, returnDetails.getInvoiceNetSalesAmt());
				stmt.setDouble(13, returnDetails.getNewInvoiceNetSalesAmt());
				stmt.setDouble(14, returnDetails.getGstAmount());
				stmt.setDouble(15, returnDetails.getDiscount());
				stmt.setDouble(16, returnDetails.getDiscountAmount());
				stmt.setDouble(17, returnDetails.getSubTotal());
				stmt.setDouble(18, returnDetails.getReturnPurchaseAmt());
				stmt.setString(19, returnDetails.getCreatedBy());
				stmt.setInt(20, returnDetails.getCustomerId());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					System.out.println("Sales Return Details Saved !");
					isItemsSaved = saveReturnItemDetails(returnDetails.getItemDetails(), conn);
					isStockUpdated = productService.updateStockAndLedger(returnDetails.getItemDetails(),
							AppConstants.STOCK_IN, AppConstants.SALES_RETURN, conn);
				}

				if (!isStockUpdated || !isItemsSaved) {
					status.setStatusCode(-1);
					// If any step fails rollback Transaction
					System.out.println("Save Return Transaction Rollbacked !");
					conn.rollback();
				} else {
					// Commit Transaction
					System.out.println("Save Return Transaction Committed !");
					conn.commit();
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
	private boolean saveReturnItemDetails(List<ItemDetails> itemList, Connection conn) {
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			if (!itemList.isEmpty()) {
				stmt = conn.prepareStatement(SAVE_RETURN_ITEM_DETAILS);
				for (ItemDetails item : itemList) {
					stmt.setInt(1, item.getBillNumber());
					stmt.setInt(2, item.getItemNo());
					stmt.setString(3, item.getItemName());
					stmt.setDouble(4, item.getMRP());
					stmt.setDouble(5, item.getRate());
					stmt.setDouble(6, item.getQuantity());
					stmt.setDouble(7, item.getAmount());
					stmt.setDouble(8, item.getPurchasePrice());
					stmt.setDouble(9, item.getGstDetails().getRate());
					stmt.setString(10, item.getGstDetails().getName());
					stmt.setDouble(11, item.getGstDetails().getCgst());
					stmt.setDouble(12, item.getGstDetails().getSgst());
					stmt.setDouble(13, item.getGstDetails().getGstAmount());
					stmt.setDouble(14, item.getGstDetails().getTaxableAmount());
					stmt.setString(15, item.getGstDetails().getInclusiveFlag());
					stmt.setDouble(16, item.getDiscountPercent());
					stmt.setDouble(17, item.getDiscountAmount());
					stmt.setString(18, item.getUnit());
					stmt.setString(19, item.getHsn());
					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Sales Return Items Details saved!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return flag;
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
				returnDetails.setInvoiceNumber(rs.getInt("INVOICE_NUMBER"));
				returnDetails.setInvoiceDate(rs.getString("INVOICE_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getString("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalReturnAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewInvoiceNetSalesAmt(rs.getDouble("NEW_INVOICE_NET_SALES_AMOUNT"));
				returnDetails.setInvoiceNetSalesAmt(rs.getDouble("INVOICE_NET_SALES_AMOUNT"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				returnDetails.setReturnPurchaseAmt(rs.getDouble("RETURN_PURCHASE_AMT"));
				returnDetails.setGstType(rs.getString("GST_TYPE"));
				returnDetails.setGstAmount(rs.getDouble("GST_AMOUNT"));
				returnDetails.setCreatedBy(rs.getString("CREATED_BY"));
				returnDetails.setCustomerId(rs.getInt("CUST_ID"));

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
				returnDetails.setInvoiceNumber(rs.getInt("INVOICE_NUMBER"));
				returnDetails.setInvoiceDate(rs.getString("INVOICE_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getString("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerId(rs.getInt("CUST_ID"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalReturnAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewInvoiceNetSalesAmt(rs.getDouble("NEW_INVOICE_NET_SALES_AMOUNT"));
				returnDetails.setInvoiceNetSalesAmt(rs.getDouble("INVOICE_NET_SALES_AMOUNT"));
				// returnDetails.setTax(rs.getDouble("TAX"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				// returnDetails.setTaxAmount(rs.getDouble("TAX_AMOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				// returnDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return returnDetails;
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

	// Get Returned Item Details for Return Number
	public List<ItemDetails> getReturnedItemList(Integer returnNumber) {
		List<ItemDetails> itemDetailsList = new ArrayList<ItemDetails>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ItemDetails itemDetails = null;

		conn = dbUtils.getConnection();
		try {
			stmt = conn.prepareStatement(GET_RETURN_ITEM_DETAILS);

			stmt.setInt(1, returnNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				itemDetails = new ItemDetails();
				itemDetails.setItemNo(rs.getInt("ITEM_NO"));
				itemDetails.setItemName(rs.getString("PRODUCT_NAME"));
				itemDetails.setMRP(rs.getDouble("ITEM_MRP"));
				itemDetails.setRate(rs.getDouble("ITEM_RATE"));
				itemDetails.setQuantity(rs.getDouble("ITEM_QTY"));
				itemDetails.setDiscountPercent(rs.getDouble("DISC_PERCENT"));
				itemDetails.setDiscountAmount(rs.getDouble("DISC_AMOUNT"));
				itemDetails.setUnit(rs.getString("UNIT"));
				itemDetails.setHsn(rs.getString("HSN"));
				itemDetails.setBillNumber(rs.getInt("RETURN_NUMBER"));

				GSTDetails gstDetails = new GSTDetails();

				gstDetails.setRate(rs.getDouble("GST_RATE"));
				gstDetails.setName(rs.getString("GST_NAME"));
				gstDetails.setCgst(rs.getDouble("CGST"));
				gstDetails.setSgst(rs.getDouble("SGST"));
				gstDetails.setGstAmount(rs.getDouble("GST_AMOUNT"));
				gstDetails.setTaxableAmount(rs.getDouble("GST_TAXABLE_AMT"));
				gstDetails.setInclusiveFlag(rs.getString("GST_INCLUSIVE_FLAG"));
				itemDetails.setGstDetails(gstDetails);

				itemDetailsList.add(itemDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return itemDetailsList;
	}

	// Get Return Details
	public List<ReturnDetails> getReturnDetails(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ReturnDetails returnDetails = null;
		List<ReturnDetails> returnDetailsList = new ArrayList<ReturnDetails>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_RETURN_DETAILS_WITHIN_DATE_RANGE);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				returnDetails = new ReturnDetails();
				returnDetails.setReturnNumber(rs.getInt("RETURN_NUMBER"));
				returnDetails.setInvoiceNumber(rs.getInt("INVOICE_NUMBER"));
				returnDetails.setInvoiceDate(rs.getString("INVOICE_DATE"));
				returnDetails.setComments(rs.getString("COMMENTS"));
				returnDetails.setTimestamp(rs.getString("RETURN_DATE"));
				returnDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				returnDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				returnDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				returnDetails.setTotalQuantity(rs.getDouble("TOTAL_QTY"));
				returnDetails.setTotalReturnAmount(rs.getDouble("RETURN_TOTAL_AMOUNT"));
				returnDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				returnDetails.setNewInvoiceNetSalesAmt(rs.getDouble("NEW_INVOICE_NET_SALES_AMOUNT"));
				returnDetails.setInvoiceNetSalesAmt(rs.getDouble("INVOICE_NET_SALES_AMOUNT"));
				returnDetails.setDiscount(rs.getDouble("DISCOUNT"));
				returnDetails.setDiscountAmount(rs.getDouble("DISCOUNT_AMOUNT"));
				returnDetails.setSubTotal(rs.getDouble("SUB_TOTAL"));
				returnDetails.setReturnPurchaseAmt(rs.getDouble("RETURN_PURCHASE_AMT"));
				returnDetails.setGstType(rs.getString("GST_TYPE"));
				returnDetails.setGstAmount(rs.getDouble("GST_AMOUNT"));
				returnDetails.setCreatedBy(rs.getString("CREATED_BY"));
				returnDetails.setCustomerId(rs.getInt("CUST_ID"));

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
