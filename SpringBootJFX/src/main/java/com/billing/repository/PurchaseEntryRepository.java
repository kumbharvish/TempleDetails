package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.constants.AppConstants;
import com.billing.dto.GSTDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Supplier;
import com.billing.dto.PurchaseEntry;
import com.billing.service.ProductHistoryService;
import com.billing.service.ProductService;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class PurchaseEntryRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductService productService;

	@Autowired
	ProductHistoryService productHistoryService;

	private static final Logger logger = LoggerFactory.getLogger(PurchaseEntryRepository.class);

	private static final String ADD_PURCHASE_ENTRY_ITEM_DETAILS = "INSERT INTO PURCHASE_ENTRY_ITEM_DETAILS (PURCHASE_ENTRY_NO,ITEM_NUMBER,ITEM_NAME,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,ITEM_AMOUNT,GST_RATE,GST_NAME,CGST,SGST,GST_AMOUNT,GST_TAXABLE_AMT,UNIT,HSN) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String ADD_PURCHASE_ENTRY = "INSERT INTO PURCHASE_ENTRY_DETAILS (PURCHASE_ENTRY_NO,SUPPLIER,BILL_NO,PURCHASE_ENTRY_DATE,COMMENTS,NO_OF_ITEMS,TOTAL_QTY,"
			+ "TOTAL_AMT_BEFORE_TAX,TOTAL_TAX,EXTRA_CHARGES,PAYMENT_MODE,TOTAL_AMOUNT,SUPPLIER_ID,BILL_DATE,DISCOUNT_AMOUNT) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String GET_PURCHASE_ENTRY_ITEM_DETAILS = "SELECT * FROM PURCHASE_ENTRY_ITEM_DETAILS WHERE PURCHASE_ENTRY_NO=?";
	
	private static final String NEW_PURCHASE_ENTRY_NUMBER = "SELECT (MAX(PURCHASE_ENTRY_NO)+1) AS ENTRY_NO FROM PURCHASE_ENTRY_DETAILS ";


	// Add Purchase Entry
	public StatusDTO addPurchaseEntry(PurchaseEntry bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		boolean isItemsSaved = false;
		boolean isStockUpdated = false;

		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(ADD_PURCHASE_ENTRY);
				stmt.setInt(1, bill.getPurchaseEntryNo());
				stmt.setString(2, bill.getSupplierName());
				stmt.setInt(3, bill.getBillNumber());
				stmt.setString(4, appUtils.getCurrentTimestamp());
				stmt.setString(5, bill.getComments());
				stmt.setInt(6, bill.getNoOfItems());
				stmt.setDouble(7, bill.getTotalQuantity());
				stmt.setDouble(8, bill.getTotalAmtBeforeTax());
				stmt.setDouble(9, bill.getTotalGSTAmount());
				stmt.setDouble(10, bill.getExtraCharges());
				stmt.setString(11, bill.getPaymentMode());
				stmt.setDouble(12, bill.getTotalAmount());
				stmt.setInt(13, bill.getSupplierId());
				stmt.setString(14, bill.getBillDate());
				stmt.setDouble(15, bill.getDiscountAmount());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					System.out.println("Purchase Entry Details Saved !");
					isItemsSaved = savePurchaseEntryItemDetails(bill.getItemDetails(), conn);
					isStockUpdated = productService.updateStockAndLedger(bill.getItemDetails(), AppConstants.STOCK_IN,
							AppConstants.PURCHASE, conn);
				}
				if (!isStockUpdated || !isItemsSaved) {
					status.setStatusCode(-1);
					// If any step fails rollback Transaction
					System.out.println("Save Purchase Entry Transaction Rollbacked !");
					conn.rollback();
				} else {
					// Commit Transaction
					System.out.println("Save Purchase Entry Transaction Committed !");
					conn.commit();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	// Save Purchase Entry Item Details
	private boolean savePurchaseEntryItemDetails(List<ItemDetails> itemList, Connection conn) {
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				stmt = conn.prepareStatement(ADD_PURCHASE_ENTRY_ITEM_DETAILS);
				for (ItemDetails item : itemList) {
					stmt.setInt(1, item.getPurchaseEntryNo());
					stmt.setInt(2, item.getItemNo());
					stmt.setString(3, item.getItemName());
					stmt.setDouble(4, item.getMRP());
					stmt.setDouble(5, item.getRate());
					stmt.setDouble(6, item.getQuantity());
					stmt.setDouble(7, item.getAmount());
					stmt.setDouble(8, item.getGstDetails().getRate());
					stmt.setString(9, item.getGstDetails().getName());
					stmt.setDouble(10, item.getGstDetails().getCgst());
					stmt.setDouble(11, item.getGstDetails().getSgst());
					stmt.setDouble(12, item.getGstDetails().getGstAmount());
					stmt.setDouble(13, item.getGstDetails().getTaxableAmount());
					stmt.setString(14, item.getUnit());
					stmt.setString(15, item.getHsn());

					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Purchase Entry Item saved !");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return flag;
	}

	// Get Item Details for Bill Number
		public List<ItemDetails> getItemDetails(int purchaseEntryNo) {
			List<ItemDetails> itemDetailsList = new ArrayList<ItemDetails>();
			Connection conn = null;
			PreparedStatement stmt = null;
			ItemDetails itemDetails = null;

			conn = dbUtils.getConnection();
			try {
				stmt = conn.prepareStatement(GET_PURCHASE_ENTRY_ITEM_DETAILS);

				stmt.setInt(1, purchaseEntryNo);
				ResultSet rs = stmt.executeQuery();

				while (rs.next()) {
					itemDetails = new ItemDetails();
					itemDetails.setItemNo(rs.getInt("ITEM_NUMBER"));
					itemDetails.setItemName(rs.getString("PRODUCT_NAME"));
					itemDetails.setMRP(rs.getDouble("ITEM_MRP"));
					itemDetails.setRate(rs.getDouble("ITEM_RATE"));
					itemDetails.setQuantity(rs.getDouble("ITEM_QTY"));
					itemDetails.setUnit(rs.getString("UNIT"));
					itemDetails.setHsn(rs.getString("HSN"));
					itemDetails.setPurchaseEntryNo(rs.getInt("PURCHASE_ENTRY_NO"));

					GSTDetails gstDetails = new GSTDetails();

					gstDetails.setRate(rs.getDouble("GST_RATE"));
					gstDetails.setName(rs.getString("GST_NAME"));
					gstDetails.setCgst(rs.getDouble("CGST"));
					gstDetails.setSgst(rs.getDouble("SGST"));
					gstDetails.setGstAmount(rs.getDouble("GST_AMOUNT"));
					gstDetails.setTaxableAmount(rs.getDouble("GST_TAXABLE_AMT"));
					//gstDetails.setInclusiveFlag(rs.getString("GST_INCLUSIVE_FLAG"));
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


	// Update purchase price and purchase history
	public StatusDTO updateStockInformation(List<ItemDetails> newStockList, int purchaseEntryNo, int supplier) {
		HashMap<Integer, Product> productMap = productService.getProductMap();
		List<Product> productList = new ArrayList<Product>();
		StatusDTO status = new StatusDTO(-1);
		logger.error("newStockList : " + newStockList);
		System.out.println("newStockList : " + newStockList);
		for (ItemDetails st : newStockList) {
			Product p = productMap.get(st.getItemNo());
			if (p != null) {
				if (st.getPurchasePrice() != p.getPurcasePrice()) {
					p.setProductTax(st.getGstDetails().getRate());
					p.setPurcaseRate(st.getRate());
					p.setPurcasePrice(st.getPurchasePrice());
					p.setDescription("Stock Purchase Invoice No.: " + purchaseEntryNo);
					p.setSupplierId(supplier);
					productList.add(p);
				}
			}
		}
		System.out.println("productList : " + productList);
		// Update Purchase price for Product
		StatusDTO statusUpdatePurPrice = productService.updateProductPurchasePrice(productList);
		// Update Product Purchase Price History
		StatusDTO statusAddPurPriceHist = productHistoryService.addProductPurchasePriceHistory(productList);

		if (statusUpdatePurPrice.getStatusCode() == 0 && statusAddPurPriceHist.getStatusCode() == 0) {
			status.setStatusCode(0);
		}
		return status;
	}
	
	public Integer getNewPurchaseEntryNumber() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Integer newBillNumber = 0;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(NEW_PURCHASE_ENTRY_NUMBER);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				newBillNumber = rs.getInt("ENTRY_NO");
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

}
