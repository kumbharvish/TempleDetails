package com.billing.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.utils.PDFUtils;

public class ProductCategoryServices {

	private static final String GET_ALL_CATEGORIES = "SELECT * FROM PRODUCT_CATEGORY_DETAILS";
	
	private static final String INS_CATEGORY = "INSERT INTO PRODUCT_CATEGORY_DETAILS " 
												+ "(CATEGORY_NAME,CATEGORY_DESCRIPTION)" 
												+ " VALUES(?,?)";
	
	private static final String DELETE_CATEGORY = "DELETE FROM PRODUCT_CATEGORY_DETAILS WHERE CATEGORY_ID=?";
	
	private static final String UPDATE_CATEGORY = "UPDATE PRODUCT_CATEGORY_DETAILS SET CATEGORY_NAME=?," 
												+"CATEGORY_DESCRIPTION=?, COMMISSION=?" 
												+" WHERE CATEGORY_ID=?";

	private static final String GET_ALL_PRODUCTS_FOR_CATEGORY = "SELECT  PD.PRODUCT_ID,PD.PRODUCT_NAME,PD.MEASURE,PD.QUANTITY,PD.PURCHASE_PRICE,PD.SELL_PRICE," +
			"PD.PRODUCT_MRP,PD.DISCOUNT,PD.ENTRY_DATE,PD.LAST_UPDATE_DATE,PD.DESCRIPTION,PD.ENTER_BY," +
			"PD.PURCHASE_RATE,PD.PRODUCT_TAX,PD.BAR_CODE,PCD.CATEGORY_NAME " +
			"FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND PCD.CATEGORY_ID=?;";
	
	
	private static final String CATEGORY_WISE_STOCK_REPORT = "SELECT PCD.CATEGORY_ID,PCD.CATEGORY_NAME,SUM(PD.QUANTITY) AS CATEGORY_STOCK_QTY,SUM(PD.QUANTITY * PD.PRODUCT_MRP) AS CATEGORY_STOCK_AMOUNT FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PCD.CATEGORY_ID=PD.CATEGORY_ID GROUP BY PD.CATEGORY_ID";

	public static List<ProductCategory> getAllCategories() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductCategory pc = null;
		List<ProductCategory> productCategoryList = new ArrayList<ProductCategory>();
		try {
			conn = PDFUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_CATEGORIES);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new ProductCategory();
				pc.setCategoryName(rs.getString("CATEGORY_NAME"));
				pc.setCategoryCode(Integer.parseInt(rs.getString("CATEGORY_ID")));
				pc.setCategoryDescription(rs.getString("CATEGORY_DESCRIPTION"));
				pc.setComission(Double.parseDouble(rs.getString("COMMISSION")));
				
				productCategoryList.add(pc);
			}
			
			Comparator<ProductCategory> cp = ProductCategory.getComparator(ProductCategory.SortParameter.CATEGORY_NAME_ASCENDING); 
			Collections.sort(productCategoryList,cp);
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		return productCategoryList;
	}
	
	public static StatusDTO addCategory(ProductCategory prodcutCategory) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if(prodcutCategory!=null){
				conn = PDFUtils.getConnection();
				stmt = conn.prepareStatement(INS_CATEGORY);
				stmt.setString(1,prodcutCategory.getCategoryName());
				stmt.setString(2,prodcutCategory.getCategoryDescription());
				//stmt.setDouble(3, 0.0);
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		return status;
	}
	
	public static boolean deleteCategory(int categoryCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag=false;
		try {
				conn = PDFUtils.getConnection();
				stmt = conn.prepareStatement(DELETE_CATEGORY);
				stmt.setInt(1,categoryCode);
				
				int i = stmt.executeUpdate();
				if(i>0){
					flag=true;
				}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		return flag;
	}
	
	public static StatusDTO updateCategory(ProductCategory prodcutCategory) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			if(prodcutCategory!=null){
				conn = PDFUtils.getConnection();
				stmt = conn.prepareStatement(UPDATE_CATEGORY);
				stmt.setString(1,prodcutCategory.getCategoryName());
				stmt.setString(2,prodcutCategory.getCategoryDescription());
				stmt.setDouble(3, 0.0);
				stmt.setInt(4, prodcutCategory.getCategoryCode());
				
				int i = stmt.executeUpdate();
				if(i>0){
					status.setStatusCode(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			status.setException(e.getMessage());
			status.setStatusCode(-1);
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		return status;
	}
	
	public static List<Product> getAllProductsForCategory(int categoryId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = PDFUtils.getConnection();
			stmt = conn.prepareStatement(GET_ALL_PRODUCTS_FOR_CATEGORY);
			stmt.setInt(1, categoryId);
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
				Collections.sort(productList,cp);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		
		return productList;
	}
	//Product Category Wise Sales Stock Report
	public static List<ProductCategory> getCategoryWiseStockReprot() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ProductCategory pc = null;
		List<ProductCategory> productCategoryList = new ArrayList<ProductCategory>();
		try {
			conn = PDFUtils.getConnection();
			stmt = conn.prepareStatement(CATEGORY_WISE_STOCK_REPORT);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new ProductCategory();
				pc.setCategoryName(rs.getString("CATEGORY_NAME"));
				pc.setCategoryCode(rs.getInt("CATEGORY_ID"));
				pc.setCategoryStockQty(rs.getInt("CATEGORY_STOCK_QTY"));
				pc.setCategoryStockAmount(rs.getInt("CATEGORY_STOCK_AMOUNT"));
				productCategoryList.add(pc);
			}
			
			Comparator<ProductCategory> cp = ProductCategory.getComparator(ProductCategory.SortParameter.CATEGORY_NAME_ASCENDING); 
			Collections.sort(productCategoryList,cp);
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PDFUtils.closeConnectionAndStatment(conn, stmt);
		}
		return productCategoryList;
	}

}
