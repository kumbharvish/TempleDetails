package com.billing.dto;

import java.util.List;

public class StockSummaryReport {

	private List<Product> productList;

	private String totalStockQty;

	private String totalStockValue;

	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}

	public String getTotalStockQty() {
		return totalStockQty;
	}

	public void setTotalStockQty(String totalStockQty) {
		this.totalStockQty = totalStockQty;
	}

	public String getTotalStockValue() {
		return totalStockValue;
	}

	public void setTotalStockValue(String totalStockValue) {
		this.totalStockValue = totalStockValue;
	}

}
