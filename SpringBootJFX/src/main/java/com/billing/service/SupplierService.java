package com.billing.service;

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
import org.springframework.stereotype.Service;

import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.dto.StockItemDetails;
import com.billing.dto.Supplier;
import com.billing.dto.SupplierInvoiceDetails;
import com.billing.utils.DBUtils;

@Service
public class SupplierService {

	@Autowired
	DBUtils dbUtils;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	ProductHistoryService productHistoryService;

	private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

	private static final String GET_ALL_SUPPLIERS = "SELECT * FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID NOT IN(001)";

	private static final String INS_SUPPLIER = "INSERT INTO SUPPLIER_DETAILS "
			+ "(SUPPLIER_NAME,EMAIL,MOBILE,ADDRESS,CITY,PHONE_NO,PAN_NO,GST_NO,COMMENTS)" + " VALUES(?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_SUPPLIER = "DELETE FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID=?";

	private static final String UPDATE_SUPPLIER = "UPDATE SUPPLIER_DETAILS SET SUPPLIER_NAME=?,"
			+ "EMAIL=?, MOBILE=?,ADDRESS=?,CITY=?,PHONE_NO=?,PAN_NO=?,GST_NO=?,COMMENTS=?" + " WHERE SUPPLIER_ID=?";

	private static final String UPDATE_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY+? WHERE PRODUCT_ID=?";

	private static final String INS_STOCK_ITEM_DETAILS = "INSERT INTO STOCK_ITEM_DETAILS (STOCK_NUMBER,ITEM_NO,ITEM_NAME,MRP,RATE,"
			+ "QUANTITY,AMOUNT,TAX,PURCHASE_PRICE) VALUES(?,?,?,?,?,?,?,?,?)";

	private static final String INS_STOCK_STOCKS_DETAILS = "INSERT INTO STOCK_INVOICE_DETAILS (STOCK_NUMBER,SUPPLIER,SUPP_INVOICE_NO,INVOICE_DATE,COMMENTS,"
			+ "NO_OF_ITEMS	,TOTAL_QTY,TOTAL_AMT_WO_TAX,TOTAL_TAX,TOTAL_MRP_AMT,"
			+ "EXTRA_CHARGES,PAYMENT_MODE,SUPP_INVOICE_AMOUNT,TIMESTAMP,SUPPLIER_ID)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String SELECT_STOCK_ITEM_DETAILS = "SELECT * FROM STOCK_ITEM_DETAILS WHERE STOCK_NUMBER=?";

	private static final String IS_SUPPLIER_ENTRY_AVAILABLE = "SELECT SUPPLIER_ID FROM STOCK_INVOICE_DETAILS WHERE SUPPLIER_ID=?";

	public List<Supplier> getAllSuppliers() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Supplier sp = null;
		List<Supplier> SupplierList = new ArrayList<Supplier>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_SUPPLIERS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				sp = new Supplier();
				sp.setSupplierName(rs.getString("SUPPLIER_NAME"));
				sp.setSupplierID(rs.getInt("SUPPLIER_ID"));
				sp.setSupplierMobile(rs.getLong("MOBILE"));
				sp.setEmailId(rs.getString("EMAIL"));
				sp.setSupplierAddress(rs.getString("ADDRESS"));
				sp.setCity(rs.getString("CITY"));
				sp.setPanNo(rs.getString("PAN_NO"));
				sp.setGstNo(rs.getString("GST_NO"));
				sp.setPhoneNumber(rs.getLong("PHONE_NO"));
				sp.setComments(rs.getString("COMMENTS"));
				SupplierList.add(sp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return SupplierList;
	}

	public boolean addSupplier(Supplier sp) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			if (sp != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_SUPPLIER);
				stmt.setString(1, sp.getSupplierName());
				stmt.setString(2, sp.getEmailId());
				stmt.setLong(3, sp.getSupplierMobile());
				stmt.setString(4, sp.getSupplierAddress());
				stmt.setString(5, sp.getCity());
				stmt.setLong(6, sp.getPhoneNumber());
				stmt.setString(7, sp.getPanNo());
				stmt.setString(8, sp.getGstNo());
				stmt.setString(9, sp.getComments());
				int i = stmt.executeUpdate();
				if (i > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	public boolean deleteSupplier(int supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_SUPPLIER);
			stmt.setInt(1, supplierId);

			int i = stmt.executeUpdate();
			if (i > 0) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	public boolean updateSupplier(Supplier sp) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			if (sp != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_SUPPLIER);
				stmt.setString(1, sp.getSupplierName());
				stmt.setString(2, sp.getEmailId());
				stmt.setLong(3, sp.getSupplierMobile());
				stmt.setString(4, sp.getSupplierAddress());
				stmt.setString(5, sp.getCity());
				stmt.setLong(6, sp.getPhoneNumber());
				stmt.setString(7, sp.getPanNo());
				stmt.setString(8, sp.getGstNo());
				stmt.setString(9, sp.getComments());
				stmt.setInt(10, sp.getSupplierID());

				int i = stmt.executeUpdate();
				if (i > 0) {
					flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Save Stock Details
	public boolean saveStockDetails(SupplierInvoiceDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_STOCK_STOCKS_DETAILS);
				stmt.setInt(1, bill.getStockNumber());
				stmt.setString(2, bill.getSupplierName());
				stmt.setInt(3, bill.getInvoiceNumber());
				stmt.setString(4, bill.getInvoiceDate());
				stmt.setString(5, bill.getComments());
				stmt.setInt(6, bill.getNoOfItems());
				stmt.setDouble(7, bill.getTotalQuanity());
				stmt.setDouble(8, bill.getTotalAmtWOTax());
				stmt.setDouble(9, bill.getTotalTax());
				stmt.setDouble(10, bill.getTotalMRPAmt());
				stmt.setDouble(11, bill.getExtraCharges());
				stmt.setString(12, bill.getPaymentMode());
				stmt.setDouble(13, bill.getSupplierInvoiceAmt());
				stmt.setString(14, bill.getTimeStamp());
				stmt.setInt(15, bill.getSupplierId());
				int i = stmt.executeUpdate();
				if (i > 0) {
					flag = true;
					System.out.println("Stock Details Saved !");
				}
				if (flag) {
					saveStockItemDetails(bill.getItemDetails());
					updateProductStock(bill.getItemDetails());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Save Bill Details
	public boolean saveStockItemDetails(List<StockItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(INS_STOCK_ITEM_DETAILS);
				for (StockItemDetails item : itemList) {
					stmt.setInt(1, item.getStockNumber());
					stmt.setInt(2, item.getItemNo());
					stmt.setString(3, item.getItemName());
					stmt.setDouble(4, item.getMRP());
					stmt.setDouble(5, item.getRate());
					stmt.setDouble(6, item.getQuantity());
					stmt.setDouble(7, item.getAmount());
					stmt.setDouble(8, item.getTax());
					stmt.setDouble(9, item.getPurchasePrice());
					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Stock Item Saved!!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Update Product Stock
	public boolean updateProductStock(List<StockItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (StockItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setDouble(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Product Stock  incremented updated");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return flag;
	}

	// Stock History Services

	public List<SupplierInvoiceDetails> getStockEntryDetails(Integer supplierId, Integer invoiceNumber, String fromDate,
			String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		SupplierInvoiceDetails supplierInvoiceDetails = null;
		List<SupplierInvoiceDetails> supplierInvoiceDetailsList = new ArrayList<SupplierInvoiceDetails>();
		boolean invoiceNumberFlag = false;
		boolean dateFlag = false;
		StringBuilder SELECT_STOCK_ENTRY_DETAILS = new StringBuilder(
				"SELECT SID.*,SP.SUPPLIER_NAME FROM STOCK_INVOICE_DETAILS SID,SUPPLIER_DETAILS SP WHERE SID.SUPPLIER_ID=? AND SID.SUPPLIER_ID=SP.SUPPLIER_ID");
		if (invoiceNumber != null) {
			invoiceNumberFlag = true;
			SELECT_STOCK_ENTRY_DETAILS.append(" AND SID.SUPP_INVOICE_NO=?");
		}
		if (fromDate != null && toDate != null) {
			dateFlag = true;
			SELECT_STOCK_ENTRY_DETAILS.append(" AND DATE(SID.INVOICE_DATE) BETWEEN ? AND ? ");
		}
		SELECT_STOCK_ENTRY_DETAILS.append(" ORDER BY SID.INVOICE_DATE DESC");
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_STOCK_ENTRY_DETAILS.toString());
			stmt.setInt(1, supplierId);
			if (invoiceNumberFlag) {
				stmt.setInt(2, invoiceNumber);
			}
			if (!invoiceNumberFlag && dateFlag) {
				stmt.setString(2, fromDate);
				stmt.setString(3, toDate);
			}
			if (invoiceNumberFlag && dateFlag) {
				stmt.setString(3, fromDate);
				stmt.setString(4, toDate);
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				supplierInvoiceDetails = new SupplierInvoiceDetails();

				supplierInvoiceDetails.setStockNumber(rs.getInt("STOCK_NUMBER"));
				supplierInvoiceDetails.setInvoiceNumber(rs.getInt("SUPP_INVOICE_NO"));
				supplierInvoiceDetails.setSupplierName(rs.getString("SUPPLIER_NAME"));
				supplierInvoiceDetails.setInvoiceDate(rs.getString("INVOICE_DATE"));
				supplierInvoiceDetails.setComments(rs.getString("COMMENTS"));
				supplierInvoiceDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				supplierInvoiceDetails.setTotalQuanity(rs.getDouble("TOTAL_QTY"));
				supplierInvoiceDetails.setTotalAmtWOTax(rs.getDouble("TOTAL_AMT_WO_TAX"));
				supplierInvoiceDetails.setTotalTax(rs.getDouble("TOTAL_TAX"));
				supplierInvoiceDetails.setTotalMRPAmt(rs.getDouble("TOTAL_MRP_AMT"));
				supplierInvoiceDetails.setExtraCharges(rs.getDouble("EXTRA_CHARGES"));
				supplierInvoiceDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				supplierInvoiceDetails.setSupplierInvoiceAmt(rs.getDouble("SUPP_INVOICE_AMOUNT"));
				supplierInvoiceDetails.setTimeStamp(rs.getString("TIMESTAMP"));
				supplierInvoiceDetailsList.add(supplierInvoiceDetails);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return supplierInvoiceDetailsList;
	}

	// Get Item Details for Stock Number
	public List<StockItemDetails> getItemDetails(int stockNumber) {
		List<StockItemDetails> stockItemDetailsList = new ArrayList<StockItemDetails>();
		Connection conn = null;
		PreparedStatement stmt = null;
		StockItemDetails stockItemDetails = null;

		conn = dbUtils.getConnection();
		try {
			stmt = conn.prepareStatement(SELECT_STOCK_ITEM_DETAILS);

			stmt.setInt(1, stockNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				stockItemDetails = new StockItemDetails();
				stockItemDetails.setItemNo(rs.getInt("ITEM_NO"));
				stockItemDetails.setItemName(rs.getString("ITEM_NAME"));
				stockItemDetails.setMRP(rs.getDouble("MRP"));
				stockItemDetails.setRate(rs.getDouble("RATE"));
				stockItemDetails.setQuantity(rs.getDouble("QUANTITY"));
				stockItemDetails.setAmount(rs.getDouble("AMOUNT"));
				stockItemDetails.setTax(rs.getDouble("TAX"));
				stockItemDetails.setPurchasePrice(rs.getDouble("PURCHASE_PRICE"));
				stockItemDetailsList.add(stockItemDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}

		return stockItemDetailsList;
	}

	// Update purchase price and purchase history
	public StatusDTO updateStockInformation(List<StockItemDetails> newStockList, int purchaseEntryNo, int supplier) {
		HashMap<Integer, Product> productMap = productService.getProductMap();
		List<Product> productList = new ArrayList<Product>();
		StatusDTO status = new StatusDTO(-1);
		logger.error("newStockList : " + newStockList);
		System.out.println("newStockList : " + newStockList);
		for (StockItemDetails st : newStockList) {
			Product p = productMap.get(st.getItemNo());
			if (p != null) {
				if (st.getPurchasePrice() != p.getPurcasePrice()) {
					p.setProductTax(st.getTax());
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

	public StatusDTO isSupplierEntryAvailable(Integer supplierId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO(-1);
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(IS_SUPPLIER_ENTRY_AVAILABLE);
			stmt.setInt(1, supplierId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				status.setStatusCode(0);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

}
