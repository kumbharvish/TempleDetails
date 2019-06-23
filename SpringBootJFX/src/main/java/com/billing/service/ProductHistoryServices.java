package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.dto.StockLedger;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class ProductHistoryServices {
	
	private static final String INS_PRODUCT_PURCHASE_PRICE = "INSERT INTO PRODUCT_PURCHASE_PRICE_HISTORY (PRODUCT_ID,PURCHASE_PRICE,ENTRY_DATE,NARRATION,PURCHASE_RATE,PRODUCT_TAX,SUPPLIER_ID)" +
			  " VALUES(?,?,?,?,?,?,?)";
	
	private static final String GET_PRODUCT_PURCHASE_PRICE_HIST = "SELECT  PD.PRODUCT_ID,PD.PURCHASE_PRICE" +
			",PD.ENTRY_DATE,PD.NARRATION,PD.PURCHASE_RATE,PD.PRODUCT_TAX,SP.SUPPLIER_NAME " +
			"FROM PRODUCT_PURCHASE_PRICE_HISTORY PD,SUPPLIER_DETAILS SP WHERE PD.PRODUCT_ID=? AND PD.SUPPLIER_ID = SP.SUPPLIER_ID ORDER BY PD.ENTRY_DATE DESC";
	
	private static final String INS_PRODUCT_STOCK_IN_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_IN,NARRATION,TRANSACTION_TYPE)" +
			  " VALUES(?,?,?,?,?)";
	
	private static final String INS_PRODUCT_STOCK_OUT_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_OUT,NARRATION,TRANSACTION_TYPE)" +
			  " VALUES(?,?,?,?,?)";
	
	private static final String PRODUCT_STOCK_LEDGER = "SELECT * FROM PRODUCT_STOCK_LEDGER WHERE PRODUCT_CODE=? AND DATE(TIMESTAMP) BETWEEN ? AND ? ORDER BY TIMESTAMP DESC";
	
	//Add Product Purchase Price History
	public static StatusDTO addProductPurchasePriceHistory(List<Product> productList) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status= new StatusDTO();
		try {
			conn = DBUtils.getConnection();
			stmt = conn.prepareStatement(INS_PRODUCT_PURCHASE_PRICE);
			conn.setAutoCommit(false);
			
			for(Product product:productList){
				stmt.setInt(1,product.getProductCode());
				stmt.setDouble(2,product.getPurcasePrice());
				stmt.setTimestamp(3,new java.sql.Timestamp(System.currentTimeMillis()));
				stmt.setString(4,product.getDescription());
				stmt.setDouble(5,product.getPurcaseRate());
				stmt.setDouble(6,product.getProductTax());
				stmt.setInt(7, product.getSupplierId());
				stmt.addBatch();
			}
			
			int batch[] = stmt.executeBatch();
			conn.commit();
			if(batch.length == productList.size()){
				status.setStatusCode(0);
				System.out.println("Product Purchase Price  History Added");
			}
			
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		return status;
	}
	

	//Get Product Purchase Price History 
	public static List<Product> getProductPurchasePriceHist(int productCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = DBUtils.getConnection();
			stmt = conn.prepareStatement(GET_PRODUCT_PURCHASE_PRICE_HIST);
			stmt.setInt(1, productCode);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setTimeStamp(rs.getTimestamp("ENTRY_DATE"));
				pc.setDescription(rs.getString("NARRATION"));
				pc.setSupplierName(rs.getString("SUPPLIER_NAME"));

				productList.add(pc);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		
		return productList;
	}
	
	//Add Product Stock Ledger
	public static StatusDTO addProductStockLedger(List<Product> productList,String stockInOutFlag,String transactionType) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status= new StatusDTO();
		try {
			conn = DBUtils.getConnection();
			conn.setAutoCommit(false);
			if(stockInOutFlag.equals(AppConstants.STOCK_IN)){
				stmt = conn.prepareStatement(INS_PRODUCT_STOCK_IN_LEDGER);
			}else{
				stmt = conn.prepareStatement(INS_PRODUCT_STOCK_OUT_LEDGER);
			}
			
			for(Product product:productList){
				stmt.setInt(1,product.getProductCode());
				stmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
				stmt.setInt(3, product.getQuanity());
				stmt.setString(4, product.getDescription());
				stmt.setString(5,transactionType);
				stmt.addBatch();
			}
			
			int batch[] = stmt.executeBatch();
			conn.commit();
			if(batch.length == productList.size()){
				status.setStatusCode(0);
				System.out.println("Product Stock Ledger Added");
			}
			
		} catch (Exception e) {
			status.setStatusCode(-1);
			status.setException(e.getMessage());
			e.printStackTrace();
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		return status;
	}
	
	//Get Stock Ledger for Product
	public static List<StockLedger> getProductStockLedger(int productCode,Date fromDate,Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StockLedger pc = null;
		List<StockLedger> stockLedgerList = new ArrayList<StockLedger>();
		try {
			conn = DBUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_STOCK_LEDGER);
			stmt.setInt(1, productCode);
			stmt.setDate(2, fromDate);
			stmt.setDate(3, toDate);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new StockLedger();
				pc.setProductCode(rs.getInt("PRODUCT_CODE"));
				pc.setTimeStamp(rs.getTimestamp("TIMESTAMP"));
				pc.setNarration(rs.getString("NARRATION"));
				pc.setTransactionType(rs.getString("TRANSACTION_TYPE"));
				pc.setStockIn(rs.getInt("STOCK_IN"));
				pc.setStockOut(rs.getInt("STOCK_OUT"));

				stockLedgerList.add(pc);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			AppUtils.closeConnectionAndStatment(conn, stmt);
		}
		
		return stockLedgerList;
	}
}
