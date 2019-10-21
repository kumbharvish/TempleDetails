package com.billing.dto;

import java.util.List;

public class SalesReturnReport {
	
	private List<ReturnDetails> returnList;

	private String fromDate;

	private String toDate;

	private Double totalReturnAmount;

	public List<ReturnDetails> getReturnList() {
		return returnList;
	}

	public void setReturnList(List<ReturnDetails> returnList) {
		this.returnList = returnList;
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

	public Double getTotalReturnAmount() {
		return totalReturnAmount;
	}

	public void setTotalReturnAmount(Double totalReturnAmount) {
		this.totalReturnAmount = totalReturnAmount;
	}

}
