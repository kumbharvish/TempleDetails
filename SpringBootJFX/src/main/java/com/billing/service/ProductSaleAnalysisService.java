package com.billing.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Product;
import com.billing.dto.ProductAnalysis;
import com.billing.utils.DBUtils;

@Service
public class ProductSaleAnalysisService {

	@Autowired
	DBUtils dbUtils;
	
	@Autowired
	ProductService productService;

	private static final Logger logger = LoggerFactory.getLogger(ProductSaleAnalysisService.class);

	private static final String PRODUCT_WISE_PROFIT = "SELECT ITEM_NUMBER ,SUM(ITEM_QTY) AS TOTAL_QTY FROM BILL_ITEM_DETAILS WHERE BILL_NUMBER IN (SELECT BILL_NUMBER FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?) GROUP BY ITEM_NUMBER ORDER BY SUM(ITEM_QTY) DESC";

	private static final String PRODUCT_WISE_SALES = "SELECT BID.ITEM_NUMBER , PD.PRODUCT_NAME,BID.ITEM_MRP,SUM(BID.ITEM_QTY) AS TOTAL_QTY FROM BILL_ITEM_DETAILS BID,PRODUCT_DETAILS PD WHERE BID.BILL_NUMBER IN (SELECT BILL_NUMBER FROM CUSTOMER_BILL_DETAILS WHERE DATE(BILL_DATE_TIME) BETWEEN ? AND ?) AND BID.ITEM_NUMBER = PD.PRODUCT_ID GROUP BY BID.ITEM_NUMBER ORDER BY SUM(BID.ITEM_QTY) DESC";

	// Get Product total Quantity between date
	public List<ProductAnalysis> getProductTotalQuantity(Date fromDate, Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductAnalysis product = null;
		List<ProductAnalysis> productAnalysisList = new ArrayList<ProductAnalysis>();
		try {
			if (fromDate == null) {
				fromDate = new Date(1947 / 01 / 01);
			}
			if (toDate == null) {
				toDate = new Date(System.currentTimeMillis());
			}
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_WISE_PROFIT);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				product = new ProductAnalysis();
				product.setProductCode(rs.getInt("ITEM_NUMBER"));
				product.setTotalQty(rs.getInt("TOTAL_QTY"));

				productAnalysisList.add(product);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return productAnalysisList;
	}

	// Get Product Wise Profit for the given period
	public List<ProductAnalysis> getProductWiseProfit(Date fromDate, Date toDate) {

		List<ProductAnalysis> productAnalysisList = getProductTotalQuantity(fromDate, toDate);
		HashMap<Integer, Product> productMap = productService.getProductMap();
		for (ProductAnalysis p : productAnalysisList) {
			if (productMap.containsKey(p.getProductCode())) {
				p.setProductName(productMap.get(p.getProductCode()).getProductName());
				p.setProductMRP(productMap.get(p.getProductCode()).getProductMRP());
				p.setPurcasePrice(productMap.get(p.getProductCode()).getPurcasePrice());
			}
		}
		Collections.sort(productAnalysisList);
		return productAnalysisList;

	}

	// Product Wise Sales Analysis
	public List<ProductAnalysis> getProductWiseSales(Date fromDate, Date toDate) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductAnalysis product = null;
		List<ProductAnalysis> productAnalysisList = new ArrayList<ProductAnalysis>();
		try {
			if (fromDate == null) {
				fromDate = new Date(1947 / 01 / 01);
			}
			if (toDate == null) {
				toDate = new Date(System.currentTimeMillis());
			}
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(PRODUCT_WISE_SALES);
			stmt.setDate(1, fromDate);
			stmt.setDate(2, toDate);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				product = new ProductAnalysis();
				product.setProductCode(rs.getInt("ITEM_NUMBER"));
				product.setProductName(rs.getString("PRODUCT_NAME"));
				product.setProductMRP(rs.getDouble("ITEM_MRP"));
				product.setTotalQty(rs.getInt("TOTAL_QTY"));

				productAnalysisList.add(product);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception : ",e);
		} finally {
			DBUtils.closeConnection(stmt, conn);
		}
		return productAnalysisList;
	}

}
