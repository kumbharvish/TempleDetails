package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.BillDetails;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.utils.DBUtils;

@Service
public class ProductService {

	@Autowired
	DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

	private static final String GET_ALL_PRODUCTS = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE,"
			+ "PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY,"
			+ "PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID;";

	private static final String GET_ALL_PRODUCTS_WITH_NO_BARCODE = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE,"
			+ "PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY,"
			+ "PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND BAR_CODE=0;";

	private static final String SEARCH_PRODUCTS = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE,"
			+ "PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY,"
			+ "PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND CONCAT(PD.SELL_PRICE,PD.PRODUCT_NAME,PCD.CATEGORY_NAME) LIKE ?;";

	private static final String INS_PRODUCT = "INSERT INTO PRODUCT_DETAILS (PRODUCT_ID,PRODUCT_NAME,MEASURE,QUANTITY,PURCHASE_PRICE,"
			+ "SELL_PRICE,PRODUCT_MRP,DISCOUNT,ENTRY_DATE,LAST_UPDATE_DATE,DESCRIPTION,ENTER_BY,CATEGORY_ID,PURCHASE_RATE,PRODUCT_TAX,BAR_CODE)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_PRODUCT = "DELETE FROM PRODUCT_DETAILS WHERE PRODUCT_ID=?";

	private static final String SELECT_PRODUCT = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE,"
			+ "PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY,"
			+ "PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND PRODUCT_ID=?;";

	private static final String UPDATE_PRODUCT = "UPDATE PRODUCT_DETAILS SET PRODUCT_NAME=?,MEASURE=?,QUANTITY=?,PURCHASE_PRICE=?,"
			+ "SELL_PRICE=?,PRODUCT_MRP=?,DISCOUNT=?,LAST_UPDATE_DATE=?,DESCRIPTION=?,"
			+ "ENTER_BY=?,CATEGORY_ID=?,PURCHASE_RATE=?,PRODUCT_TAX=?,BAR_CODE=? WHERE PRODUCT_ID=?";

	private static final String UPDATE_PRODUCT_PURCHASE_HISTORY = "UPDATE PRODUCT_DETAILS SET PURCHASE_PRICE=?,LAST_UPDATE_DATE=?,PURCHASE_RATE=?,PRODUCT_TAX=? WHERE PRODUCT_ID=?";

	private static final String QUICK_STOCK_CORRECTION = "UPDATE PRODUCT_DETAILS SET QUANTITY=? WHERE PRODUCT_ID=?";

	private static final String SAVE_BARCODE = "UPDATE PRODUCT_DETAILS SET BAR_CODE=? WHERE PRODUCT_ID=?";

	private static final String INS_BILL_DETAILS = "INSERT INTO CUSTOMER_BILL_DETAILS (BILL_NUMBER,BILL_DATE_TIME,CUST_MOB_NO,CUST_NAME,NO_OF_ITEMS,"
			+ "BILL_QUANTITY,TOTAL_AMOUNT,BILL_TAX,GRAND_TOTAL,PAYMENT_MODE,"
			+ "BILL_DISCOUNT,BILL_DISC_AMOUNT,NET_SALES_AMOUNT,BILL_PURCHASE_AMT)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String INS_BILL_ITEM_DETAILS = "INSERT INTO BILL_ITEM_DETAILS (BILL_NUMBER,ITEM_NUMBER,ITEM_NAME,ITEM_MRP,ITEM_RATE,"
			+ "ITEM_QTY,ITEM_AMOUNT,ITEM_PURCHASE_AMT) VALUES(?,?,?,?,?,?,?,?)";

	private static final String UPDATE_PRODUCT_STOCK = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY-? WHERE PRODUCT_ID=?";

	// private static final String SELECT_BILL_DETAILS = "SELECT * FROM
	// CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ? ORDER BY
	// BILL_DATE_TIME ASC";

	private static final String SELECT_BILL_WITH_BILLNO_AND_DATE = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO AND DATE(BILL_DATE_TIME) BETWEEN ? AND ?";

	private static final String SELECT_BILL_WITH_BILLNO = "SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE CBD.BILL_NUMBER=? AND"
			+ " CBD.CUST_MOB_NO=CD.CUST_MOB_NO";

	private static final String SELECT_ITEM_DETAILS = "SELECT BID.*,PD.PRODUCT_NAME FROM BILL_ITEM_DETAILS BID,PRODUCT_DETAILS PD WHERE BILL_NUMBER=? AND BID.ITEM_NUMBER=PD.PRODUCT_ID";

	private static final String ZERO_STOCK_PRODUCTS = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE,"
			+ "PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY,"
			+ "PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND QUANTITY<=0;";

	public List<Product> getAllProducts() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_PRODUCTS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getDate("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getDate("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));

				productList.add(pc);
				Comparator<Product> cp = Product.getComparator(Product.SortParameter.CATEGORY_NAME_ASCENDING);
				Collections.sort(productList, cp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return productList;
	}

	public Product getProduct(int productCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = new Product();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SELECT_PRODUCT);
			stmt.setInt(1, productCode);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getDate("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getDate("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return pc;
	}

	public StatusDTO addProduct(Product product) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (product != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_PRODUCT);
				stmt.setInt(1, product.getProductCode());
				stmt.setString(2, product.getProductName());
				stmt.setString(3, product.getMeasure());
				stmt.setInt(4, product.getQuanity());
				stmt.setDouble(5, product.getPurcasePrice());
				stmt.setDouble(6, product.getSellPrice());
				stmt.setDouble(7, product.getSellPrice());// MRP same as sell price
				stmt.setDouble(8, product.getDiscount());
				stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));
				stmt.setDate(10, new java.sql.Date(System.currentTimeMillis()));
				stmt.setString(11, product.getDescription());
				stmt.setString(12, product.getEnterBy());
				// stmt.setString(13, product.getProductCategory());
				stmt.setInt(13, product.getCategoryCode());
				stmt.setDouble(14, product.getPurcaseRate());
				stmt.setDouble(15, product.getProductTax());
				stmt.setLong(16, product.getProductBarCode());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return status;
	}

	public boolean deleteProduct(int prdouctCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_PRODUCT);
			stmt.setInt(1, prdouctCode);

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

	public StatusDTO updateProduct(Product product) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (product != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_PRODUCT);
				stmt.setString(1, product.getProductName());
				stmt.setString(2, product.getMeasure());
				stmt.setInt(3, product.getQuanity());
				stmt.setDouble(4, product.getPurcasePrice());
				stmt.setDouble(5, product.getSellPrice());
				stmt.setDouble(6, product.getSellPrice());
				stmt.setDouble(7, product.getDiscount());
				stmt.setDate(8, new java.sql.Date(System.currentTimeMillis()));
				stmt.setString(9, product.getDescription());
				stmt.setString(10, product.getEnterBy());
				// stmt.setString(11, product.getProductCategory());
				stmt.setInt(11, product.getCategoryCode());
				stmt.setDouble(12, product.getPurcaseRate());
				stmt.setDouble(13, product.getProductTax());
				stmt.setLong(14, product.getProductBarCode());
				stmt.setInt(15, product.getProductCode());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
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

	public List<Product> searchProduct(String searchString) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(SEARCH_PRODUCTS);
			stmt.setString(1, "%" + searchString + "%");
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getDate("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getDate("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));

				productList.add(pc);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return productList;
	}

	// Save Bill Details
	public StatusDTO saveBillDetails(BillDetails bill) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO staus = new StatusDTO();
		try {
			if (bill != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(INS_BILL_DETAILS);
				stmt.setInt(1, bill.getBillNumber());
				stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
				stmt.setLong(3, bill.getCustomerMobileNo());
				stmt.setString(4, bill.getCustomerName());
				stmt.setInt(5, bill.getNoOfItems());
				stmt.setInt(6, bill.getTotalQuanity());
				stmt.setDouble(7, bill.getTotalAmount());
				stmt.setDouble(8, bill.getTax());
				stmt.setDouble(9, bill.getGrandTotal());
				stmt.setString(10, bill.getPaymentMode());
				stmt.setDouble(11, bill.getDiscount());
				stmt.setDouble(12, bill.getDiscountAmt());
				stmt.setDouble(13, bill.getNetSalesAmt());
				stmt.setDouble(14, bill.getPurchaseAmt());
				int i = stmt.executeUpdate();
				if (i > 0) {
					staus.setStatusCode(0);
					saveBillItemDetails(bill.getItemDetails());
					updateProductStock(bill.getItemDetails());
					System.out.println("Bill Details Saved !");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			staus.setStatusCode(-1);
			staus.setException(e.getMessage());
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return staus;
	}

	// Save Bill Details
	public boolean saveBillItemDetails(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(INS_BILL_ITEM_DETAILS);
				for (ItemDetails item : itemList) {
					stmt.setInt(1, item.getBillNumber());
					stmt.setInt(2, item.getItemNo());
					stmt.setString(3, item.getItemName());
					stmt.setDouble(4, item.getMRP());
					stmt.setDouble(5, item.getRate());
					stmt.setInt(6, item.getQuantity());
					stmt.setDouble(7, item.getAmount());
					stmt.setDouble(8, item.getPurchasePrice());
					stmt.addBatch();
				}

				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
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

	// Update Product Stock
	public boolean updateProductStock(List<ItemDetails> itemList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (!itemList.isEmpty()) {
				conn = dbUtils.getConnection();
				conn.setAutoCommit(false);
				stmt = conn.prepareStatement(UPDATE_PRODUCT_STOCK);
				for (ItemDetails item : itemList) {
					stmt.setInt(2, item.getItemNo());
					stmt.setInt(1, item.getQuantity());
					stmt.addBatch();
				}
				int batch[] = stmt.executeBatch();
				conn.commit();
				if (batch.length == itemList.size()) {
					flag = true;
					System.out.println("Product Stock  updated");
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

	// Get Bill Details
	public List<BillDetails> getBillDetails(Date fromDate, Date toDate, Long customerMobile) {
		Connection conn = null;
		PreparedStatement stmt = null;
		BillDetails billDetails = null;
		List<BillDetails> billDetailsList = new ArrayList<BillDetails>();
		StringBuilder SELECT_BILL_DETAILS = new StringBuilder(
				"SELECT CBD.*,CD.CUST_NAME AS CUSTOMER_NAME FROM CUSTOMER_BILL_DETAILS CBD,CUSTOMER_DETAILS CD WHERE DATE(CBD.BILL_DATE_TIME) BETWEEN ? AND ?  AND CBD.CUST_MOB_NO=CD.CUST_MOB_NO ");

		String ORDER_BY_CLAUSE = "ORDER BY CBD.BILL_DATE_TIME DESC";
		String CUSTOMER_MOB_QEUERY = " AND CBD.CUST_MOB_NO LIKE ? ";
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
			System.out.println("SELECT_BILL_DETAILS " + SELECT_BILL_DETAILS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				billDetails = new BillDetails();
				billDetails.setBillNumber(rs.getInt("BILL_NUMBER"));
				billDetails.setTimestamp(rs.getTimestamp("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuanity(rs.getInt("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));

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
				itemDetails.setQuantity(rs.getInt("ITEM_QTY"));

				itemDetailsList.add(itemDetails);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}

		return itemDetailsList;
	}

	// Get product map product code as key
	public HashMap<Integer, Product> getProductMap() {
		HashMap<Integer, Product> productMap = new HashMap<Integer, Product>();

		for (Product p : getAllProducts()) {
			productMap.put(p.getProductCode(), p);
		}
		return productMap;
	}

	// Get product map barcode as key
	public HashMap<Long, Product> getProductBarCodeMap() {
		HashMap<Long, Product> productMap = new HashMap<Long, Product>();
		for (Product p : getAllProducts()) {
			if (p.getProductBarCode() != 0)
				productMap.put(p.getProductBarCode(), p);
		}
		return productMap;
	}

	// Get Zero Stock Products
	public List<Product> getZeroStockProducts() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(ZERO_STOCK_PRODUCTS);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getDate("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getDate("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));

				productList.add(pc);
				Comparator<Product> cp = Product.getComparator(Product.SortParameter.CATEGORY_NAME_ASCENDING);
				Collections.sort(productList, cp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return productList;
	}

	public List<Product> getProductsWithNoBarcode() {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_PRODUCTS_WITH_NO_BARCODE);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuanity(rs.getInt("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getDate("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getDate("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));

				productList.add(pc);
				Comparator<Product> cp = Product.getComparator(Product.SortParameter.CATEGORY_NAME_ASCENDING);
				Collections.sort(productList, cp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return productList;
	}

	public StatusDTO saveBarcode(Product product) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if (product != null) {
				conn = dbUtils.getConnection();
				stmt = conn.prepareStatement(SAVE_BARCODE);
				stmt.setLong(1, product.getProductBarCode());
				stmt.setInt(2, product.getProductCode());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
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

	// Update Product Stock
	public boolean quickStockCorrection(Product product) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (product != null) {
				conn = dbUtils.getConnection();

				stmt = conn.prepareStatement(QUICK_STOCK_CORRECTION);
				stmt.setInt(2, product.getProductCode());
				stmt.setInt(1, product.getQuanity());

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
				billDetails.setTimestamp(rs.getTimestamp("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuanity(rs.getInt("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));
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
				billDetails.setTimestamp(rs.getTimestamp("BILL_DATE_TIME"));
				billDetails.setCustomerMobileNo(rs.getLong("CUST_MOB_NO"));
				billDetails.setCustomerName(rs.getString("CUSTOMER_NAME"));
				billDetails.setNoOfItems(rs.getInt("NO_OF_ITEMS"));
				billDetails.setTotalQuanity(rs.getInt("BILL_QUANTITY"));
				billDetails.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
				billDetails.setTax(rs.getDouble("BILL_TAX"));
				billDetails.setGrandTotal(rs.getDouble("GRAND_TOTAL"));
				billDetails.setPaymentMode(rs.getString("PAYMENT_MODE"));
				billDetails.setDiscount(rs.getDouble("BILL_DISCOUNT"));
				billDetails.setDiscountAmt(rs.getDouble("BILL_DISC_AMOUNT"));
				billDetails.setNetSalesAmt(rs.getDouble("NET_SALES_AMOUNT"));
				billDetails.setPurchaseAmt(rs.getDouble("BILL_PURCHASE_AMT"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return billDetails;
	}

	// Update Purchase Price,Tax & Rate
	public StatusDTO updateProductPurchasePrice(List<Product> productList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(UPDATE_PRODUCT_PURCHASE_HISTORY);
			conn.setAutoCommit(false);
			for (Product product : productList) {
				stmt.setDouble(1, product.getPurcasePrice());
				stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
				stmt.setDouble(3, product.getPurcaseRate());
				stmt.setDouble(4, product.getProductTax());
				stmt.setInt(5, product.getProductCode());
				stmt.addBatch();
			}
			int batch[] = stmt.executeBatch();
			conn.commit();
			if (batch.length == productList.size()) {
				status.setStatusCode(0);
				System.out.println("Product Purchase Price  updated");
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
}
