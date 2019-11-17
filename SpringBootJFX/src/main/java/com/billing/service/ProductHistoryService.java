package com.billing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.dto.Product;
import com.billing.dto.StatusDTO;
import com.billing.dto.StockLedger;
import com.billing.repository.ProductHistoryRepository;

@Service
public class ProductHistoryService {

	@Autowired
	ProductHistoryRepository productHistoryRepository;

	public StatusDTO addProductPurchasePriceHistory(List<Product> productList) {
		return productHistoryRepository.addProductPurchasePriceHistory(productList);
	}

	public List<Product> getProductPurchasePriceHist(int productCode) {
		return productHistoryRepository.getProductPurchasePriceHist(productCode);
	}

	public List<StockLedger> getProductStockLedger(int productCode, String fromDate, String toDate) {
		return productHistoryRepository.getProductStockLedger(productCode, fromDate, toDate);
	}

}
