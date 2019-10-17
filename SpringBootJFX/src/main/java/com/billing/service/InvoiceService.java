package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.BillDetails;
import com.billing.dto.GSTDetails;
import com.billing.dto.InvoiceSearchCriteria;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class InvoiceService {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	CustomerService customerService;

	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

	private static final String INS_BILL_DETAILS = "INSERT INTO CUSTOMER_BILL_DETAILS (BILL_NUMBER,BILL_DATE_TIME,CUST_MOB_NO,CUST_NAME,NO_OF_ITEMS,"
			+ "BILL_QUANTITY,TOTAL_AMOUNT,PAYMENT_MODE,"
			+ "BILL_DISCOUNT,BILL_DISC_AMOUNT,NET_SALES_AMOUNT,BILL_PURCHASE_AMT,GST_TYPE,GST_AMOUNT,CREATED_BY,LAST_UPDATED)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String UPDATE_BILL_DETAILS = "UPDATE CUSTOMER_BILL_DETAILS SET BILL_DATE_TIME=?,CUST_MOB_NO=?,CUST_NAME=?,NO_OF_ITEMS=?,BILL_QUANTITY=?,TOTAL_AMOUNT=?,PAYMENT_MODE=?,BILL_DISCOUNT=?,BILL_DISC_AMOUNT =?,"
			+ "NET_SALES_AMOUNT=? ,BILL_PURCHASE_AMT=?,GST_TYPE=?,GST_AMOUNT=?,CREATED_BY=?,LAST_UPDATED=? WHERE BILL_NUMBER=?";

	private static final String INS_BILL_ITEM_DETAILS = "INSERT INTO BILL_ITEM_DETAILS (BILL_NUMBER,ITEM_NUMBER,ITEM_NAME,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,ITEM_AMOUNT,ITEM_PURCHASE_AMT,GST_RATE,GST_NAME,CGST,SGST,GST_AMOUNT,GST_TAXABLE_AMT,GST_INCLUSIVE_FLAG,DISC_PERCENT,DISC_AMOUNT,UNIT,HSN) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String UPDATE_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY-? WHERE PRODUCT_ID=?";

	private static final String SELECT_BILL_WITH_BILLNO_AND_DATE = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO AND DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String SELECT_BILL_WITH_BILLNO = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO";

	private static final String SELECT_ITEM_DETAILS = "SELECT BID.*,PD.PRODUCT_NAME FROM BILL_ITEM_DETAILS BID,PRODUCT_DETAILS PD WHERE BILL_NUMBER=? AND BID.ITEM_NUMBER=PD.PRODUCT_ID";

	private static final String NEW_BILL_NUMBER = "SELECT (MAX(BILL_NUMBER)+1) AS BILL_NO FROM CUSTOMER_BILL_DETAILS ";

	private static final String DELETE_BILL_DETAILS = "DELETE FROM CUSTOMER_BILL_DETAILS WHERE BILL_NUMBER=?";

	private static final String DELETE_BILL_ITEM_DETAILS = "DELETE FROM BILL_ITEM_DETAILS WHERE BILL_NUMBER=?";

	private static final String UPDATE_DELETED_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY+? WHERE PRODUCT_ID=?";

	private static final String INS_PRODUCT_STOCK_IN_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_IN,NARRATION,TRANSACTION_TYPE)"
			+ " VALUES(?,?,?,?,?)";

	private static final String INS_PRODUCT_STOCK_OUT_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_OUT,NARRATION,TRANSACTION_TYPE)"
			+ " VALUES(?,?,?,?,?)";

	private static final String GET_BILL_DETAILS_WITHIN_DATE_RANGE = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE DATE(CBD.BILL_DATE_TIME) BETWEEN ? AND ?  AND CBD.CUST_MOB_NO=CD.CUST_MOB_NO ORDER BY CBD.BILL_DATE_TIME DESC";

	// Save Invoice Details
	private StatusDTO saveInvoiceDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean isItemsSaved = false;
		boolean isStockUpdated = false;
		boolean isStockLedgerUpdated = false;
		StatusDTO status = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(INS_BILL_DETAILS);
				stmt.setInt(1, bill.getBillNumber());
				stmt.setString(2, bill.getTimestamp());
				stmt.setLong(3, bill.getCustomerMobileNo());
				stmt.setString(4, bill.getCustomerName());
				stmt.setInt(5, bill.getNoOfItems());
				stmt.setDouble(6, bill.getTotalQuantity());
				stmt.setDouble(7, bill.getTotalAmount());
				stmt.setString(8, bill.getPaymentMode());
				stmt.setDouble(9, bill.getDiscount());
				stmt.setDouble(10, bill.getDiscountAmt());
				stmt.setDouble(11, bill.getNetSalesAmt());
				stmt.setDouble(12, bill.getPurchaseAmt());
				stmt.setString(13, bill.getGstType());
				stmt.setDouble(14, bill.getGstAmount());
				stmt.setString(15, bill.getCreatedBy());
				stmt.setString(16, appUtils.getCurrentTimestamp());
				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					System.out.println("Invoice Details Saved !");
					isItemsSaved = saveBillItemDetails(bill.getItemDetails(), conn);
					isStockUpdated = updateProductStock(bill.getItemDetails(), conn);
					isStockLedgerUpdated = addProductStockLedger(
							getProductListForStockLedger(bill.getItemDetails(), "SAVE_INVOICE"), AppConstants.STOCK_OUT,
							AppConstants.SALES, conn);
				}

				if (!isStockUpdated || !isItemsSaved || !isStockLedgerUpdated) {
					status.setStatusCode(-1);
					// If any step fails rollback Transaction
					System.out.println("Save Invoice Transaction Rollbacked !");
					conn.rollback();
				} else {
					// Commit Transaction
					System.out.println("Save Invoice Transaction Committed !");
					conn.commit();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.getMessage());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public StatusDTO saveInvoice(BillDetails bill) {
		StatusDTO statusSaveInvoice = new StatusDTO();
		if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
			// Save Bill Details
			StatusDTO status = saveInvoiceDetails(bill);
			StatusDTO statusAddPendingAmt = new StatusDTO(-1);
			if (status.getStatusCode() == 0) {
				String narration = "Invoice Amount based on No : " + bill.getBillNumber();
				statusAddPendingAmt = customerService.addCustomerPaymentHistory(bill.getCustomerMobileNo(),
						bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narration);
			}
			if (status.getStatusCode() != 0 || statusAddPendingAmt.getStatusCode() != 0) {
				statusSaveInvoice.setStatusCode(-1);
			}
		}
		// Other than PENDING pay mode ex: cash,cheque etc
		if (!AppConstants.PENDING.equals(bill.getPaymentMode())) {
			// Save Bill Details
			StatusDTO status = saveInvoiceDetails(bill);
			if (status.getStatusCode() != 0) {
				statusSaveInvoice.setStatusCode(-1);
			}
		}

		return statusSaveInvoice;

	}

	private List<Product> getProductListForStockLedger(List<ItemDetails> itemList, String type) {
		List<Product> productList = new ArrayList<>();
		for (ItemDetails p : itemList) {
			Product product = new Product();
			product.setProductCode(p.getItemNo());
			product.setQuantity(p.getQuantity());
			if ("SAVE_INVOICE".equals(type)) {
				product.setDescription("Sales based on Invoice No.: " + p.getBillNumber());
			} else if ("DELETE_INVOICE".equals(type)) {
				product.setDescription("Delete Invoice based on Invoice No.: " + p.getBillNumber());
			}
			productList.add(product);
		}
		return productList;
	}

	// Add Product Stock Ledger
	public boolean addProductStockLedger(List<Product> productList, String stockInOutFlag, String transactionType,
			Connection conn) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			if (stockInOutFlag.equals(AppConstants.STOCK_IN)) {
				stmt = conn.prepareStatement(INS_PRODUCT_STOCK_IN_LEDGER);
			} else {
				stmt = conn.prepareStatement(INS_PRODUCT_STOCK_OUT_LEDGER);
			}

			for (Product product : productList) {
				stmt.setInt(1, product.getProductCode());
				stmt.setString(2, appUtils.getCurrentTimestamp());
				stmt.setDouble(3, product.getQuantity());
				stmt.setString(4, product.getDescription());
				stmt.setString(5, transactionType);
				stmt.addBatch();
			}

			int batch[] = stmt.executeBatch();
			if (batch.length == productList.size()) {
				status = true;
				System.out.println("Product Stock Ledger Added");
			}

		} catch (Exception e) {
			status = false;
			e.printStackTrace();
			logger.info("Exception : ", e);
		}
		return status;
	}

	// Save Invoice Item Details
	private boolean saveBillItemDetails(List<ItemDetails> itemList, Connection conn) {
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				stmt = conn.prepareStatement(INS_BILL_ITEM_DETAILS);
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
					stmt.setString(19,item.getHsn());

					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Invoice Item saved !");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return flag;
	}

	// Update Product Stock
	public boolean updateProductStock(List<ItemDetails> itemList, Connection conn) {
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Product Stock  updated");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return flag;
	}

	// Get Bill Details
	public List<BillDetails> getBillDetails(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		List<BillDetails> billDetailsList = new ArrayList<BillDetails>();

		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_BILL_DETAILS_WITHIN_DATE_RANGE);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
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
				billDetails.setCreatedBy(rs.getString("CREATED_BY"));
				billDetails.setLastUpdated(rs.getString("LAST_UPDATED"));

				billDetailsList.add(billDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	// Get Item Details for Bill Number
	public List<ItemDetails> getItemDetails(int billNumber) {
		List<ItemDetails> itemDetailsList = new ArrayList<ItemDetails>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ItemDetails itemDetails = null;

		conn = dbUtils.getConnection();
		try {
			stmt = conn.prepareStatement(SELECT_ITEM_DETAILS);

			stmt.setInt(1, billNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				itemDetails = new ItemDetails();
				itemDetails.setItemNo(rs.getInt("ITEM_NUMBER"));
				itemDetails.setItemName(rs.getString("PRODUCT_NAME"));
				itemDetails.setMRP(rs.getDouble("ITEM_MRP"));
				itemDetails.setRate(rs.getDouble("ITEM_RATE"));
				itemDetails.setQuantity(rs.getDouble("ITEM_QTY"));
				itemDetails.setDiscountPercent(rs.getDouble("DISC_PERCENT"));
				itemDetails.setDiscountAmount(rs.getDouble("DISC_AMOUNT"));
				itemDetails.setUnit(rs.getString("UNIT"));
				itemDetails.setHsn(rs.getString("HSN"));
				itemDetails.setBillNumber(rs.getInt("BILL_NUMBER"));

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

	// Get Bill Details for Bill Number with Date Range
	public BillDetails getBillDetailsOfBillNumberWithinDateRange(Integer billNumber, Date fromDate, Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_WITH_BILLNO_AND_DATE);
			stmt.setInt(1, billNumber);
			stmt.setDate(2, fromDate);
			stmt.setDate(3, toDate);
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetails;
	}

	// Get Bill Details for Bill Number
	public BillDetails getBillDetailsOfBillNumber(Integer billNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_BILL_WITH_BILLNO);
			stmt.setInt(1, billNumber);
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
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetails;
	}

	public Integer getNewBillNumber() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Integer newBillNumber = 0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(NEW_BILL_NUMBER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				newBillNumber = rs.getInt("BILL_NO");
				if (newBillNumber == 0) {
					newBillNumber = 1;
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

	// Search Invoice
	public List<BillDetails> getSearchedInvoices(InvoiceSearchCriteria criteria) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		List<BillDetails> billDetailsList = new ArrayList<BillDetails>();

		String sqlQuery = getQuery(criteria);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(sqlQuery);
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
				billDetails.setCreatedBy(rs.getString("CREATED_BY"));
				billDetails.setLastUpdated(rs.getString("LAST_UPDATED"));

				billDetailsList.add(billDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetailsList;
	}

	private static String getQuery(InvoiceSearchCriteria criteria) {

		StringBuilder selectQuery = new StringBuilder(
				"SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE  CBD.CUST_MOB_NO=CD.CUST_MOB_NO AND ");

		if (criteria.getInvoiceNumber() != null) {
			selectQuery.append(" CBD.BILL_NUMBER = ").append(criteria.getInvoiceNumber());
		} else {
			boolean conditionApplied = false;

			// date
			if (criteria.getStartDate() != null) {
				conditionApplied = true;
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
				String startDateString = criteria.getStartDate().format(dateFormatter);
				String endDateString = criteria.getEndDate().format(dateFormatter);

				selectQuery.append(" DATE(CBD.BILL_DATE_TIME) BETWEEN '").append(startDateString).append("' AND '")
						.append(endDateString).append("' ");
			}

			// Payment Mode
			if (criteria.getPendingInvoice() != null) {
				String paymode = "CASH";
				if ("Y".equals(criteria.getPendingInvoice())) {
					paymode = "PENDING";
				}
				if (conditionApplied) {
					selectQuery.append(" AND ");
				}
				conditionApplied = true;
				selectQuery.append(" CBD.PAYMENT_MODE = '").append(paymode).append("' ");
			}

			// amount
			String amount = criteria.getStartAmount();
			if (amount != null) {
				if (conditionApplied) {
					selectQuery.append(" AND ");
				}
				conditionApplied = true;
				selectQuery.append(" CBD.NET_SALES_AMOUNT BETWEEN ").append(amount).append(" AND ")
						.append(criteria.getEndAmount());

			}

		}
		selectQuery.append(" ORDER BY CBD.BILL_DATE_TIME DESC");
		System.out.println(selectQuery);
		return selectQuery.toString();
	}

	public StatusDTO deleteInvoice(BillDetails bill) {
		StatusDTO statusCustBlanceUpdate = new StatusDTO(-1);
		StatusDTO statusDeleteBill = new StatusDTO(-1);
		// Pending Invoice
		if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
			statusDeleteBill = deleteInvoiceDetails(bill);
			if (statusDeleteBill.getStatusCode() == 0) {
				String narration = "Delete Invoice adjustment based on Invoice No : " + bill.getBillNumber();
				statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerMobileNo(), 0,
						bill.getNetSalesAmt(), AppConstants.DEBIT, narration);

				if (statusCustBlanceUpdate.getStatusCode() != 0) {
					statusDeleteBill.setStatusCode(-1);
				}
			}
		}
		// Cash Bill
		if (!AppConstants.PENDING.equals(bill.getPaymentMode())) {
			statusDeleteBill = deleteInvoiceDetails(bill);
		}
		return statusDeleteBill;
	}

	// Delete Bill Details including Items
	private StatusDTO deleteInvoiceDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		boolean statusDeleteItems = false;
		boolean statusUpdateStock = false;
		boolean isStockLedgerUpdated = false;
		try {
			conn = dbUtils.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(DELETE_BILL_DETAILS);
			stmt.setInt(1, bill.getBillNumber());

			int i = stmt.executeUpdate();
			if (i > 0) {
				System.out.println("Invoice deleted !");
				status.setStatusCode(0);
				statusDeleteItems = deleteBillItemDetails(conn, bill.getBillNumber());
				statusUpdateStock = updateDeletedBillProductStock(conn, bill.getItemDetails());
				isStockLedgerUpdated = addProductStockLedger(
						getProductListForStockLedger(bill.getItemDetails(), "DELETE_INVOICE"), AppConstants.STOCK_IN,
						AppConstants.DELETE_INVOICE, conn);
			}
			if (!statusDeleteItems || !statusUpdateStock || !isStockLedgerUpdated) {
				status.setStatusCode(-1);
				System.out.println("Delete Invoice Transaction Rollbacked !");
				conn.rollback();
			} else {
				System.out.println("Delete Invoice Transaction Comitted !");
				conn.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
			logger.info("Exception : ", e);
			return status;
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Delete bill only Bill Item Details
	private boolean deleteBillItemDetails(Connection conn, int billNumber) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			stmt = conn.prepareStatement(DELETE_BILL_ITEM_DETAILS);
			stmt.setInt(1, billNumber);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status = true;
				System.out.println("Invoice Items Deleted !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		}
		return status;
	}

	// Update Product Stock for Deleted Invoice
	private boolean updateDeletedBillProductStock(Connection conn, List<ItemDetails> itemList) {
		PreparedStatement stmt = null;
		boolean status = false;
		try {
			if (!itemList.isEmpty()) {
				stmt = conn.prepareStatement(UPDATE_DELETED_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				if (batch.length == itemList.size()) {
					status = true;
					System.out.println("Product Stock  updated");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		}
		return status;
	}

	// Edit Invoice
	public StatusDTO editInvoice(BillDetails bill) {
		StatusDTO statusEditInvoice = new StatusDTO(-1);
		StatusDTO statusCustBlanceUpdate = new StatusDTO(-1);
		// Update Invoice Details
		StatusDTO status = editInvoiceDetails(bill);
		if (status.getStatusCode() == 0) {
			// Customer payment history
			if (AppConstants.PENDING.equals(bill.getPaymentMode())) {
				if (bill.getCopyCustMobile() == bill.getCustomerMobileNo()) {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {
						double newNetSalesAmt = bill.getCopyNetSalesAmt() - bill.getNetSalesAmt();
						if (newNetSalesAmt > 0) {

							String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
							statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(
									bill.getCustomerMobileNo(), 0, newNetSalesAmt, AppConstants.DEBIT, narration);
						}
						if (newNetSalesAmt < 0) {

							String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
							statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(
									bill.getCustomerMobileNo(), Math.abs(newNetSalesAmt), 0, AppConstants.CREDIT,
									narration);
						}
					} else {
						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerMobileNo(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narration);
					}

				} else {
					// if Existing bill is pending then Debit Existing bill customer bill amount and
					// credit into new customer balance amount
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustMobile(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);

						String narrationCredit = "Edit Invoice correction based on Invoice No : "
								+ bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerMobileNo(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narrationCredit);

					} else {
						String narrationCredit = "Edit Invoice correction based on Invoice No : "
								+ bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCustomerMobileNo(),
								bill.getNetSalesAmt(), 0, AppConstants.CREDIT, narrationCredit);
					}
				}
			} else {
				if (bill.getCopyCustMobile() == bill.getCustomerMobileNo()) {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustMobile(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);
					} else {
						statusEditInvoice.setStatusCode(0);
						System.out.println("------------- Update Code Here 1 ----------------");
						return statusEditInvoice;
					}
				} else {
					if (AppConstants.PENDING.equals(bill.getCopyPaymode())) {

						String narration = "Edit Invoice correction based on Invoice No : " + bill.getBillNumber();
						statusCustBlanceUpdate = customerService.addCustomerPaymentHistory(bill.getCopyCustMobile(), 0,
								bill.getCopyNetSalesAmt(), AppConstants.DEBIT, narration);

					} else {
						System.out.println("------------- Update Code Here 2 ----------------");
						statusEditInvoice.setStatusCode(0);
						return statusEditInvoice;
					}
				}

			}
		}

		if (status.getStatusCode() == 0 && statusCustBlanceUpdate.getStatusCode() == 0) {
			System.out.println("------------- Both True ----------------");

			statusEditInvoice.setStatusCode(0);
		}

		return statusEditInvoice;

	}

	// Save Invoice Details
	private StatusDTO editInvoiceDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean isItemsSaved = false;
		boolean isItemsDeleted = false;
		StatusDTO status = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_BILL_DETAILS);
				stmt.setString(1, bill.getTimestamp());
				stmt.setLong(2, bill.getCustomerMobileNo());
				stmt.setString(3, bill.getCustomerName());
				stmt.setInt(4, bill.getNoOfItems());
				stmt.setDouble(5, bill.getTotalQuantity());
				stmt.setDouble(6, bill.getTotalAmount());
				stmt.setString(7, bill.getPaymentMode());
				stmt.setDouble(8, bill.getDiscount());
				stmt.setDouble(9, bill.getDiscountAmt());
				stmt.setDouble(10, bill.getNetSalesAmt());
				stmt.setDouble(11, bill.getPurchaseAmt());
				stmt.setString(12, bill.getGstType());
				stmt.setDouble(13, bill.getGstAmount());
				stmt.setString(14, bill.getCreatedBy());
				stmt.setString(15, appUtils.getCurrentTimestamp());
				stmt.setInt(16, bill.getBillNumber());
				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					System.out.println("Invoice Details Updated !");
					if (bill.isItemsEdited()) {
						isItemsDeleted = deleteBillItemDetails(conn, bill.getBillNumber());
						isItemsSaved = saveBillItemDetails(bill.getItemDetails(), conn);
						updateStock(bill, conn);
					}
				}

				if (bill.isItemsEdited() && (!isItemsDeleted || !isItemsSaved)) {
					status.setStatusCode(-1);
					// If any step fails rollback Transaction
					System.out.println("Edit Invoice Transaction Rollbacked !");
					conn.rollback();
				} else {
					// Commit Transaction
					System.out.println("Edit Invoice Transaction Committed !");
					conn.commit();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.getMessage());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	private void updateStock(BillDetails bill, Connection conn) {
		List<ItemDetails> finalItemList = new ArrayList<>();
		HashMap<Integer, ItemDetails> existingItemsMap = new HashMap<>();
		HashMap<Integer, ItemDetails> newItemsMap = new HashMap<>();
		for (ItemDetails item : bill.getCopyItemDetails()) {
			existingItemsMap.put(item.getItemNo(), item);
		}

		for (ItemDetails newItem : bill.getItemDetails()) {
			newItemsMap.put(newItem.getItemNo(), newItem);
			if (existingItemsMap.containsKey(newItem.getItemNo())) {
				ItemDetails extingItem = existingItemsMap.get(newItem.getItemNo());
				double newQty = extingItem.getQuantity() - newItem.getQuantity();
				if (newQty < 0) {
					newItem.setStockInOutFlag(AppConstants.STOCK_OUT);
					newItem.setQuantity(Math.abs(newQty));
				}
				if (newQty > 0) {
					newItem.setStockInOutFlag(AppConstants.STOCK_IN);
					newItem.setQuantity(newQty);
				}
			} else {
				newItem.setStockInOutFlag(AppConstants.STOCK_OUT);
			}
			finalItemList.add(newItem);

		}

		for (ItemDetails exstingItem : bill.getCopyItemDetails()) {
			if (!newItemsMap.containsKey(exstingItem.getItemNo())) {
				exstingItem.setStockInOutFlag(AppConstants.STOCK_IN);
				finalItemList.add(exstingItem);
			}
		}
		System.out.println("Final List : " + finalItemList);
		for (ItemDetails item : finalItemList) {
			List<Product> productList = new ArrayList<>();
			List<ItemDetails> itemList = new ArrayList<>();
			itemList.add(item);
			Product product = new Product();
			product.setProductCode(item.getItemNo());
			product.setQuantity(item.getQuantity());
			product.setDescription("Edit Invoice correction based on Invoice No.: " + item.getBillNumber());
			productList.add(product);
			if (AppConstants.STOCK_IN.equals(item.getStockInOutFlag())) {
				updateDeletedBillProductStock(conn, itemList);
				addProductStockLedger(productList, AppConstants.STOCK_IN, AppConstants.EDIT_INVOICE, conn);
			} else {
				updateProductStock(itemList, conn);
				addProductStockLedger(productList, AppConstants.STOCK_OUT, AppConstants.EDIT_INVOICE, conn);
			}

		}

	}
}
