package com.billing.dto;

import java.util.List;

public class ExpenseReport {

	private List<TransactionDetails> expenseList;

	private String fromDate;

	private String toDate;

	private String category;
	
	private String totalExpenaseAmount;

	public List<TransactionDetails> getExpenseList() {
		return expenseList;
	}

	public void setExpenseList(List<TransactionDetails> expenseList) {
		this.expenseList = expenseList;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTotalExpenaseAmount() {
		return totalExpenaseAmount;
	}

	public void setTotalExpenaseAmount(String totalExpenaseAmount) {
		this.totalExpenaseAmount = totalExpenaseAmount;
	}

}
