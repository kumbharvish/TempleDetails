package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.dto.Product;
import com.billing.dto.ProductAnalysis;
import com.billing.dto.StatusDTO;
import com.billing.dto.StockLedger;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class ProductHistoryRepository {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	private static final Logger logger = LoggerFactory.getLogger(ProductHistoryRepository.class);

	private static final String INS_PRODUCT_PURCHASE_PRICE = "INSERT INTO PRODUCT_PURCHASE_PRICE_HISTORY (PRODUCT_ID,PURCHASE_PRICE,ENTRY_DATE,NARRATION,PURCHASE_RATE,PRODUCT_TAX,SUPPLIER_ID)"
			+ " VALUES(?,?,?,?,?,?,?)";

	private static final String GET_PRODUCT_PURCHASE_PRICE_HIST = "SELECT  PD.PRODUCT_ID,PD.PURCHASE_PRICE"
			+ ",PD.ENTRY_DATE,PD.NARRATION,PD.PURCHASE_RATE,PD.PRODUCT_TAX,SP.SUPPLIER_NAME "
			+ "FROM PRODUCT_PURCHASE_PRICE_HISTORY PD,SUPPLIER_DETAILS SP WHERE PD.PRODUCT_ID=? AND PD.SUPPLIER_ID = SP.SUPPLIER_ID ORDER BY PD.ENTRY_DATE DESC";

	private static final String PRODUCT_STOCK_LEDGER = "SELECT * FROM PRODUCT_STOCK_LEDGER WHERE PRODUCT_CODE=? AND DATE(TIMESTAMP) BETWEEN ? AND ? ORDER BY TIMESTAMP DESC";

	private static final String PRODUCT_WISE_PROFIT = "SELECT ITEM_NUMBER ,SUM(ITEM_QTY) AS TOTAL_QTY FROM BILL_ITEM_DETAILS WHERE BILL_NUMBER IN (SELECT BILL_NUMBER FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?) GROUP BY ITEM_NUMBER ORDER BY SUM(ITEM_QTY) DESC";

	private static final String PRODUCT_WISE_SALES = "SELECT BID.ITEM_NUMBER , PD.PRODUCT_NAME,BID.ITEM_MRP,SUM(BID.ITEM_QTY) AS TOTAL_QTY FROM BILL_ITEM_DETAILS BID,PRODUCT_DETAILS PD WHERE BID.BILL_NUMBER IN (SELECT BILL_NUMBER FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?) AND BID.ITEM_NUMBER = PD.PRODUCT_ID GROUP BY BID.ITEM_NUMBER ORDER BY SUM(BID.ITEM_QTY) DESC";

	// Add Product Purchase Price History
	public StatusDTO addProductPurchasePriceHistory(List<Product> productList, Connection conn) {
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			stmt = conn.prepareStatement(INS_PRODUCT_PURCHASE_PRICE);
			for (Product product : productList) {
				stmt.setInt(1, product.getProductCode());
				stmt.setDouble(2, product.getPurcasePrice());
				stmt.setString(3, appUtils.getCurrentTimestamp());
				stmt.setString(4, product.getDescription());
				stmt.setDouble(5, product.getPurcaseRate());
				stmt.setDouble(6, product.getProductTax());
				stmt.setInt(7, product.getSupplierId());
				stmt.addBatch();
			}

			int batch[] = stmt.executeBatch();
			if (batch.length == productList.size()) {
				status.setStatusCode(0);
				System.out.println("Product Purchase Price  History Added");
			}

		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
			logger.info("Exception : ", e);
		}
		return status;
	}

	// Get Product Purchase Price History
	public List<Product> getProductPurchasePriceHist(int productCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(GET_PRODUCT_PURCHASE_PRICE_HIST);
			stmt.setInt(1, productCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setTimeStamp(rs.getString("ENTRY_DATE"));
				pc.setDescription(rs.getString("NARRATION"));
				pc.setSupplierName(rs.getString("SUPPLIER_NAME"));

				productList.add(pc);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return productList;
	}

	// Get Stock Ledger for Product
	public List<StockLedger> getProductStockLedger(int productCode, String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StockLedger pc = null;
		List<StockLedger> stockLedgerList = new ArrayList<StockLedger>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_STOCK_LEDGER);
			stmt.setInt(1, productCode);
			stmt.setString(2, fromDate);
			stmt.setString(3, toDate);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new StockLedger();
				pc.setProductCode(rs.getInt("PRODUCT_CODE"));
				pc.setTimeStamp(rs.getString("TIMESTAMP"));
				pc.setNarration(rs.getString("NARRATION"));
				pc.setTransactionType(rs.getString("TRANSACTION_TYPE"));
				pc.setStockIn(rs.getDouble("STOCK_IN"));
				pc.setStockOut(rs.getDouble("STOCK_OUT"));

				stockLedgerList.add(pc);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}

		return stockLedgerList;
	}

	// Get Product total Quantity between date
	public List<ProductAnalysis> getProductTotalQuantity(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductAnalysis product = null;
		List<ProductAnalysis> productAnalysisList = new ArrayList<ProductAnalysis>();
		try {
			if (fromDate == null) {
				fromDate = "1947-01-01";
			}
			if (toDate == null) {
				toDate = appUtils.getCurrentTimestamp();
			}
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_WISE_PROFIT);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				product = new ProductAnalysis();
				product.setProductCode(rs.getInt("ITEM_NUMBER"));
				product.setTotalQty(rs.getDouble("TOTAL_QTY"));

				productAnalysisList.add(product);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return productAnalysisList;
	}

	// Product Wise Sales Analysis
	public List<ProductAnalysis> getProductWiseSales(String fromDate, String toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductAnalysis product = null;
		List<ProductAnalysis> productAnalysisList = new ArrayList<ProductAnalysis>();
		try {
			if (fromDate == null) {
				fromDate = "1947-01-01";
			}
			if (toDate == null) {
				toDate = appUtils.getCurrentTimestamp();
			}
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_WISE_SALES);
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				product = new ProductAnalysis();
				product.setProductCode(rs.getInt("ITEM_NUMBER"));
				product.setProductName(rs.getString("PRODUCT_NAME"));
				product.setProductMRP(rs.getDouble("ITEM_MRP"));
				product.setTotalQty(rs.getDouble("TOTAL_QTY"));

				productAnalysisList.add(product);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ", e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return productAnalysisList;
	}
}
