package com.billing.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.ItemDetails;
import com.billing.dto.MeasurementUnit;
import com.billing.dto.Product;
import com.billing.dto.ProductCategory;
import com.billing.dto.StatusDTO;
import com.billing.dto.Tax;
import com.billing.repository.ProductRepository;
import com.billing.utils.AppUtils;
import com.billing.utils.DBUtils;

@Service
public class ProductService implements AppService<Product> {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	AppUtils appUtils;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	ProductHistoryService productHistoryService;

	@Autowired
	ProductCategoryService productCategoryService;

	@Autowired
	MeasurementUnitsService measurementUnitsService;

	@Autowired
	TaxesService taxesService;

	@Override
	public List<Product> getAll() {
		return productRepository.getAllProducts();
	}

	public List<Product> getProductsWithNoBarcode() {
		return productRepository.getProductsWithNoBarcode();
	}

	public StatusDTO saveBarcode(Product product) {
		return productRepository.saveBarcode(product);
	}

	public List<Product> getZeroStockProducts(Integer lowStockQtyLimit) {
		return productRepository.getZeroStockProducts(lowStockQtyLimit);
	}

	public StatusDTO updateProductPurchasePrice(List<Product> productList, Connection conn) {
		return productRepository.updateProductPurchasePrice(productList, conn);
	}

	public Product getProduct(int productCode) {
		return productRepository.getProduct(productCode);
	}

	public StatusDTO delete(Product product) {
		return productRepository.deleteProduct(product.getProductCode());
	}

	public StatusDTO add(Product product) {
		return productRepository.addProduct(product);
	}

	public StatusDTO update(Product product) {
		return productRepository.updateProduct(product);
	}

	public boolean updateProductStock(List<ItemDetails> itemList, String stockInOutFlag, Connection conn) {
		return productRepository.updateProductStock(itemList, stockInOutFlag, conn);
	}

	public boolean addProductStockLedger(List<Product> productList, String stockInOutFlag, String transactionType,
			Connection conn) {
		return productRepository.addProductStockLedger(productList, stockInOutFlag, transactionType, conn);
	}

	// Update Product Stock and Add Stock ledger entry
	public boolean updateStockAndLedger(List<ItemDetails> itemList, String stockInOutFlag, String transactionType,
			Connection conn) {
		boolean isStockUpdated = false;
		boolean isStockLedgerUpdated = false;

		isStockUpdated = updateProductStock(itemList, stockInOutFlag, conn);
		List<Product> productList = getProductListForStockLedger(itemList, transactionType);
		isStockLedgerUpdated = addProductStockLedger(productList, stockInOutFlag, transactionType, conn);

		if (isStockUpdated && isStockLedgerUpdated) {
			return true;
		}

		return false;
	}

	// Returns Products map with Product Code as key
	public HashMap<Integer, Product> getProductMap() {
		HashMap<Integer, Product> productMap = new HashMap<Integer, Product>();

		for (Product p : getAll()) {
			productMap.put(p.getProductCode(), p);
		}
		return productMap;
	}

	// Returns Products map with Barcode as key
	public HashMap<Long, Product> getProductBarCodeMap() {
		HashMap<Long, Product> productMap = new HashMap<Long, Product>();
		for (Product p : getAll()) {
			if (p.getProductBarCode() != 0)
				productMap.put(p.getProductBarCode(), p);
		}
		return productMap;
	}

	public List<Product> getProductListForStockLedger(List<ItemDetails> itemList, String transactionType) {
		List<Product> productList = new ArrayList<>();
		for (ItemDetails p : itemList) {
			Product product = new Product();
			product.setProductCode(p.getItemNo());
			product.setQuantity(p.getQuantity());
			if (AppConstants.SALES.equals(transactionType)) {
				product.setDescription("Sales based on Invoice No.: " + p.getBillNumber());
			} else if (AppConstants.DELETE_INVOICE.equals(transactionType)) {
				product.setDescription("Delete Invoice based on Invoice No.: " + p.getBillNumber());
			} else if (AppConstants.SALES_RETURN.equals(transactionType)) {
				product.setDescription("Sales Return based on Return No.: " + p.getBillNumber());
			} else if (AppConstants.PURCHASE.equals(transactionType)) {
				product.setDescription("Purchase based on P.E. No.: " + p.getPurchaseEntryNo());
			}else if (AppConstants.DELETE_PURCHASE_ENTRY.equals(transactionType)) {
				product.setDescription("Delete purchase entry based on P.E. No.: " + p.getPurchaseEntryNo());
			}
			productList.add(product);
		}
		return productList;
	}

	public HashMap<String, List> getComboboxData() {
		HashMap<String, List> dataMap = new HashMap<>();
		Connection conn = dbUtils.getConnection();
		List<ProductCategory> categoryList = productCategoryService.getAll(conn);
		List<MeasurementUnit> uomList = measurementUnitsService.getAll(conn);
		List<Tax> taxList = taxesService.getAll(conn);
		DBUtils.closeConnection(null, conn);

		dataMap.put("CATEGORIES", categoryList);
		dataMap.put("UOMS", uomList);
		dataMap.put("TAXES", taxList);

		return dataMap;
	}

	// Do Quick Stock Correction
	public boolean doQuickStockCorrection(Product product, double quantity) {
		List<Product> productList = new ArrayList<Product>();
		boolean result = false;
		Product p = getProduct(product.getProductCode());
		Connection conn = dbUtils.getConnection();
		product.setDescription("Existing Stock: " + appUtils.getDecimalFormat(p.getQuantity()) + " Correction Qty: "
				+ appUtils.getDecimalFormat(product.getQuantity()));
		if (product.getQuantity() < p.getQuantity()) {
			product.setQuantity(p.getQuantity() - product.getQuantity());
			productList.add(product);
			addProductStockLedger(productList, AppConstants.STOCK_OUT, AppConstants.QUICK_STOCK_CORR, conn);
		} else {
			product.setQuantity(product.getQuantity() - p.getQuantity());
			productList.add(product);
			addProductStockLedger(productList, AppConstants.STOCK_IN, AppConstants.QUICK_STOCK_CORR, conn);
		}
		// Close Connection
		DBUtils.closeConnection(null, conn);
		product.setQuantity(quantity);
		result = productRepository.updateProductQuantity(product);
		return result;
	}

}
