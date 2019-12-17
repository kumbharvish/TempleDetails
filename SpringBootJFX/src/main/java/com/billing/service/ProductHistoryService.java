package com.billing.service;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Product;
import com.billing.dto.ProductAnalysis;
import com.billing.dto.StatusDTO;
import com.billing.dto.StockLedger;
import com.billing.repository.ProductHistoryRepository;

@Service
public class ProductHistoryService {

	@Autowired
	ProductHistoryRepository productHistoryRepository;

	@Autowired
	ProductService productService;

	public StatusDTO addProductPurchasePriceHistory(List<Product> productList, Connection conn) {
		return productHistoryRepository.addProductPurchasePriceHistory(productList, conn);
	}

	public List<Product> getProductPurchasePriceHist(int productCode) {
		return productHistoryRepository.getProductPurchasePriceHist(productCode);
	}

	public List<StockLedger> getProductStockLedger(int productCode, String fromDate, String toDate) {
		return productHistoryRepository.getProductStockLedger(productCode, fromDate, toDate);
	}

	public List<ProductAnalysis> getProductTotalQuantity(String fromDate, String toDate) {
		return productHistoryRepository.getProductTotalQuantity(fromDate, toDate);
	}

	public List<ProductAnalysis> getProductWiseSales(String fromDate, String toDate) {
		return productHistoryRepository.getProductWiseSales(fromDate, toDate);
	}

	// Get Product Wise Profit for the given period
	public List<ProductAnalysis> getProductWiseProfit(String fromDate, String toDate) {

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

}
