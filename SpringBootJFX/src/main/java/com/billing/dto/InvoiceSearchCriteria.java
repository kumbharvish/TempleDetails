package com.billing.dto;

import java.time.LocalDate;

public class InvoiceSearchCriteria {
	private String invoiceNumber;
	private LocalDate startDate;
	private LocalDate endDate;
	private String startAmount;
	private String endAmount;
	private String pendingInvoice;

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

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

	public String getPendingInvoice() {
		return pendingInvoice;
	}

	public void setPendingInvoice(String pendingInvoice) {
		this.pendingInvoice = pendingInvoice;
	}

}
