package com.billing.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.billing.constants.AppConstants;
import com.billing.dto.ItemDetails;
import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.service.MeasurementUnitsService;
import com.billing.service.ProductCategoryService;
import com.billing.service.ProductHistoryService;
import com.billing.service.TaxesService;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Repository
public class ProductRepository {

	private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductCategoryService productCategoryService;

	@Autowired
	MeasurementUnitsService measurementUnitsService;

	@Autowired
	TaxesService taxesService;

	@Autowired
	ProductHistoryService productHistoryService;

	private static final String GET_ALL_PRODUCTS = "SELECT  PD.*,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID;";

	private static final String GET_ALL_PRODUCTS_WITH_NO_BARCODE = "SELECT  PD.*,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND BAR_CODE=0;";

	private static final String INS_PRODUCT = "INSERT INTO PRODUCT_DETAILS (PRODUCT_ID,PRODUCT_NAME,MEASURE,QUANTITY,PURCHASE_PRICE,"
			+ "SELL_PRICE,PRODUCT_MRP,DISCOUNT,ENTRY_DATE,LAST_UPDATE_DATE,DESCRIPTION,ENTER_BY,CATEGORY_ID,PURCHASE_RATE,PRODUCT_TAX,BAR_CODE,HSN)"
			+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String DELETE_PRODUCT = "DELETE FROM PRODUCT_DETAILS WHERE PRODUCT_ID=?";

	private static final String SELECT_PRODUCT = "SELECT  PD.*,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND PRODUCT_ID=?;";

	private static final String UPDATE_PRODUCT = "UPDATE PRODUCT_DETAILS SET PRODUCT_NAME=?,MEASURE=?,QUANTITY=?,PURCHASE_PRICE=?,"
			+ "SELL_PRICE=?,PRODUCT_MRP=?,DISCOUNT=?,LAST_UPDATE_DATE=?,DESCRIPTION=?,"
			+ "ENTER_BY=?,CATEGORY_ID=?,PURCHASE_RATE=?,PRODUCT_TAX=?,BAR_CODE=?,HSN=? WHERE PRODUCT_ID=?";

	private static final String UPDATE_PRODUCT_PURCHASE_HISTORY = "UPDATE PRODUCT_DETAILS SET PURCHASE_PRICE=?,LAST_UPDATE_DATE=?,PURCHASE_RATE=?,PRODUCT_TAX=? WHERE PRODUCT_ID=?";

	private static final String UPDATE_PRODUCT_QUANTITY = "UPDATE PRODUCT_DETAILS SET QUANTITY=? WHERE PRODUCT_ID=?";

	private static final String SAVE_BARCODE = "UPDATE PRODUCT_DETAILS SET BAR_CODE=? WHERE PRODUCT_ID=?";

	private static final String LOW_STOCK_PRODUCTS = "SELECT  PD.*,PCD.CATEGORY_NAME "
			+ "FROM PRODUCT_DETAILS PD,PRODUCT_CATEGORY_DETAILS PCD WHERE PD.CATEGORY_ID = PCD.CATEGORY_ID AND QUANTITY<=?;";

	private static final String INS_PRODUCT_STOCK_IN_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_IN,NARRATION,TRANSACTION_TYPE)"
			+ " VALUES(?,?,?,?,?)";

	private static final String INS_PRODUCT_STOCK_OUT_LEDGER = "INSERT INTO PRODUCT_STOCK_LEDGER (PRODUCT_CODE,TIMESTAMP,STOCK_OUT,NARRATION,TRANSACTION_TYPE)"
			+ " VALUES(?,?,?,?,?)";

	private static final String ADD_PRODUCT_QUANTITY = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY+? WHERE PRODUCT_ID=?";

	private static final String DECREASE_PRODUCT_QUANTITY = "UPDATE PRODUCT_DETAILS SET QUANTITY=QUANTITY-? WHERE PRODUCT_ID=?";

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
				pc.setQuantity(rs.getDouble("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setOrignalDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getString("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getString("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));
				pc.setHsn(rs.getString("HSN"));

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
				pc.setQuantity(rs.getDouble("QUANTITY"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setOrignalDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getString("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getString("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));
				pc.setHsn(rs.getString("HSN"));

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
				stmt.setDouble(4, product.getQuantity());
				stmt.setDouble(5, product.getPurcasePrice());
				stmt.setDouble(6, product.getSellPrice());
				stmt.setDouble(7, product.getSellPrice());// MRP same as sell price
				stmt.setDouble(8, product.getDiscount());
				stmt.setString(9, appUtils.getCurrentTimestamp());
				stmt.setString(10, appUtils.getCurrentTimestamp());
				stmt.setString(11, product.getDescription());
				stmt.setString(12, product.getEnterBy());
				// stmt.setString(13, product.getProductCategory());
				stmt.setInt(13, product.getCategoryCode());
				stmt.setDouble(14, product.getPurcaseRate());
				stmt.setDouble(15, product.getProductTax());
				stmt.setLong(16, product.getProductBarCode());
				stmt.setString(17, product.getHsn());

				int i = stmt.executeUpdate();
				if (i > 0) {
					status.setStatusCode(0);
					// Add Stock Ledger
					List<Product> list = new ArrayList<Product>();
					product.setDescription("Add Product Opening Stock");
					list.add(product);
					addProductStockLedger(list, AppConstants.STOCK_IN, AppConstants.ADD_PRODUCT, conn);
					// Add Product Purchase price history
					List<Product> list2 = new ArrayList<Product>();
					product.setDescription(AppConstants.ADD_PRODUCT);
					product.setSupplierId(1);
					list2.add(product);
					productHistoryService.addProductPurchasePriceHistory(list2, conn);
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

	public StatusDTO deleteProduct(int prdouctCode) {
		Connection conn = null;
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(DELETE_PRODUCT);
			stmt.setInt(1, prdouctCode);

			int i = stmt.executeUpdate();
			if (i > 0) {
				status.setStatusCode(0);
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
				stmt.setDouble(3, product.getQuantity());
				stmt.setDouble(4, product.getPurcasePrice());
				stmt.setDouble(5, product.getSellPrice());
				stmt.setDouble(6, product.getSellPrice());
				stmt.setDouble(7, product.getDiscount());
				stmt.setString(8, appUtils.getCurrentTimestamp());
				stmt.setString(9, product.getDescription());
				stmt.setString(10, product.getEnterBy());
				// stmt.setString(11, product.getProductCategory());
				stmt.setInt(11, product.getCategoryCode());
				stmt.setDouble(12, product.getPurcaseRate());
				stmt.setDouble(13, product.getProductTax());
				stmt.setLong(14, product.getProductBarCode());
				stmt.setString(15, product.getHsn());
				stmt.setInt(16, product.getProductCode());

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

	// Get Low Stock Products
	public List<Product> getZeroStockProducts(Integer lowStockQtyLimit) {
		Connection conn = null;
		PreparedStatement stmt = null;
		Product pc = null;
		List<Product> productList = new ArrayList<Product>();
		try {
			conn = dbUtils.getConnection();
			stmt = conn.prepareStatement(LOW_STOCK_PRODUCTS);
			stmt.setInt(1, lowStockQtyLimit);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				pc = new Product();
				pc.setProductCode(rs.getInt("PRODUCT_ID"));
				pc.setProductName(rs.getString("PRODUCT_NAME"));
				pc.setMeasure(rs.getString("MEASURE"));
				pc.setQuantity(rs.getDouble("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getString("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getString("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));
				pc.setHsn(rs.getString("HSN"));

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
				pc.setQuantity(rs.getDouble("QUANTITY"));
				pc.setPurcaseRate(rs.getDouble("PURCHASE_RATE"));
				pc.setProductTax(rs.getDouble("PRODUCT_TAX"));
				pc.setPurcasePrice(rs.getDouble("PURCHASE_PRICE"));
				pc.setSellPrice(rs.getDouble("SELL_PRICE"));
				pc.setProductMRP(rs.getDouble("PRODUCT_MRP"));
				pc.setDiscount(rs.getDouble("DISCOUNT"));
				pc.setEntryDate(rs.getString("ENTRY_DATE"));
				pc.setLastUpdateDate(rs.getString("LAST_UPDATE_DATE"));
				pc.setDescription(rs.getString("DESCRIPTION"));
				pc.setEnterBy(rs.getString("ENTER_BY"));
				pc.setProductCategory(rs.getString("CATEGORY_NAME"));
				pc.setProductBarCode(rs.getLong("BAR_CODE"));
				pc.setHsn(rs.getString("HSN"));

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
	public boolean updateProductQuantity(Product product) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (product != null) {
				conn = dbUtils.getConnection();

				stmt = conn.prepareStatement(UPDATE_PRODUCT_QUANTITY);
				stmt.setInt(2, product.getProductCode());
				stmt.setDouble(1, product.getQuantity());

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

	// Update Purchase Price,Tax & Rate
	public StatusDTO updateProductPurchasePrice(List<Product> productList, Connection conn) {
		PreparedStatement stmt = null;
		StatusDTO status = new StatusDTO();
		try {
			stmt = conn.prepareStatement(UPDATE_PRODUCT_PURCHASE_HISTORY);
			for (Product product : productList) {
				stmt.setDouble(1, product.getPurcasePrice());
				stmt.setString(2, appUtils.getCurrentTimestamp());
				stmt.setDouble(3, product.getPurcaseRate());
				stmt.setDouble(4, product.getProductTax());
				stmt.setInt(5, product.getProductCode());
				stmt.addBatch();
			}
			int batch[] = stmt.executeBatch();
			if (batch.length == productList.size()) {
				status.setStatusCode(0);
				System.out.println("Product Purchase Price  updated");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
			status.setStatusCode(-1);
			status.setException(e.getMessage());
		}
		return status;
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

	// Update Product Stock
	public boolean updateProductStock(List<ItemDetails> itemList, String stockInOutFlag, Connection conn) {
		PreparedStatement stmt = null;
		boolean flag = false;

		try {
			if (stockInOutFlag.equals(AppConstants.STOCK_IN)) {
				stmt = conn.prepareStatement(ADD_PRODUCT_QUANTITY);
			} else {
				stmt = conn.prepareStatement(DECREASE_PRODUCT_QUANTITY);
			}
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
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception : ", e);
		}
		return flag;
	}

}
