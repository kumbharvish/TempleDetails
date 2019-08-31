package com.billing.dto;

import java.util.List;

public class SupplierInvoiceDetails {
	
private int stockNumber;
	
	private int supplierId;
	
	private String supplierName;
	
	private int invoiceNumber;
	
	private String invoiceDate;
	
	private String comments;
	
	private List<StockItemDetails> itemDetails;
	
	private int noOfItems;
	
	private double totalQuanity;
	
	private double totalAmtWOTax;
	
	private double totalTax;
	
	private double totalMRPAmt;
	
	private String 	paymentMode;
	
	private double extraCharges;
	
	private double supplierInvoiceAmt;
	
	private String timeStamp;

	public int getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(int stockNumber) {
		this.stockNumber = stockNumber;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public List<StockItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<StockItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public int getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(int noOfItems) {
		this.noOfItems = noOfItems;
	}

	public double getTotalQuanity() {
		return totalQuanity;
	}

	public void setTotalQuanity(double totalQuanity) {
		this.totalQuanity = totalQuanity;
	}

	public double getTotalAmtWOTax() {
		return totalAmtWOTax;
	}

	public void setTotalAmtWOTax(double totalAmtWOTax) {
		this.totalAmtWOTax = totalAmtWOTax;
	}

	public double getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(double totalTax) {
		this.totalTax = totalTax;
	}

	public double getTotalMRPAmt() {
		return totalMRPAmt;
	}

	public void setTotalMRPAmt(double totalMRPAmt) {
		this.totalMRPAmt = totalMRPAmt;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public double getExtraCharges() {
		return extraCharges;
	}

	public void setExtraCharges(double extraCharges) {
		this.extraCharges = extraCharges;
	}

	public double getSupplierInvoiceAmt() {
		return supplierInvoiceAmt;
	}

	public void setSupplierInvoiceAmt(double supplierInvoiceAmt) {
		this.supplierInvoiceAmt = supplierInvoiceAmt;
	}

	public int getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "SupplierInvoiceDetails [stockNumber=" + stockNumber
				+ ", supplierName=" + supplierName + ", invoiceNumber="
				+ invoiceNumber + ", invoiceDate=" + invoiceDate
				+ ", comments=" + comments + ", itemDetails=" + itemDetails
				+ ", noOfItems=" + noOfItems + ", totalQuanity=" + totalQuanity
				+ ", totalAmtWOTax=" + totalAmtWOTax + ", totalTax=" + totalTax
				+ ", totalMRPAmt=" + totalMRPAmt + ", paymentMode="
				+ paymentMode + ", extraCharges=" + extraCharges
				+ ", supplierInvoiceAmt=" + supplierInvoiceAmt + ", timeStamp="
				+ timeStamp + "]";
	}

	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}
	
}
