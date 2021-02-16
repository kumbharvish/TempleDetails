package com.billing.dto;

import java.util.List;

public class LowStockSummaryReport {

	private List<Product> productList;

	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
}
