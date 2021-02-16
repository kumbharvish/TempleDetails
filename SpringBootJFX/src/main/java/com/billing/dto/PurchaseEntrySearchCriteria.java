package com.billing.dto;

import java.time.LocalDate;

public class PurchaseEntrySearchCriteria {
	private String purchaseEntryNo;
	private LocalDate startDate;
	private LocalDate endDate;
	private String startAmount;
	private String endAmount;
	private String pendingPurchaseEntry;
	private Integer supplierId;

	public String getStartAmount() {
		return startAmount;
	}

	public void setStartAmount(String startAmount) {
		this.startAmount = startAmount;
	}

	public String getEndAmount() {
		return endAmount;
	}

	public void setEndAmount(String endAmount) {
		this.endAmount = endAmount;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Integer getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public String getPurchaseEntryNo() {
		return purchaseEntryNo;
	}

	public void setPurchaseEntryNo(String purchaseEntryNo) {
		this.purchaseEntryNo = purchaseEntryNo;
	}

	public String getPendingPurchaseEntry() {
		return pendingPurchaseEntry;
	}

	public void setPendingPurchaseEntry(String pendingPurchaseEntry) {
		this.pendingPurchaseEntry = pendingPurchaseEntry;
	}

}
