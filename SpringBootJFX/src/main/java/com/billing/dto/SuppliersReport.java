package com.billing.dto;

import java.util.List;

public class SuppliersReport {
	
	private List<Supplier> suppliersList;
	
	private String totalBalanceAmount;

	public List<Supplier> getSuppliersList() {
		return suppliersList;
	}

	public void setSuppliersList(List<Supplier> suppliersList) {
		this.suppliersList = suppliersList;
	}

	public String getTotalBalanceAmount() {
		return totalBalanceAmount;
	}

	public void setTotalBalanceAmount(String totalBalanceAmount) {
		this.totalBalanceAmount = totalBalanceAmount;
	}

}
