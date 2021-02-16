package com.billing.dto;

import java.util.List;

public class CustomersReport {

	private List<Customer> customerList;

	private String totalPendingAmount;

	public List<Customer> getCustomerList() {
		return customerList;
	}

	public void setCustomerList(List<Customer> customerList) {
		this.customerList = customerList;
	}

	public String getTotalPendingAmount() {
		return totalPendingAmount;
	}

	public void setTotalPendingAmount(String totalPendingAmount) {
		this.totalPendingAmount = totalPendingAmount;
	}

}
