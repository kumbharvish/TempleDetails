package com.billing.dto;

import java.util.List;

public class ReturnDetails {

	private int returnNumber;

	private String timestamp;

	private String comments;

	private int invoiceNumber;

	private long customerMobileNo;

	private String invoiceDate;

	private List<ItemDetails> itemDetails;

	private String paymentMode;

	private int noOfItems;

	private double totalQuantity;

	private double totalReturnAmount;

	private double invoiceNetSalesAmt;

	private double newInvoiceNetSalesAmt;

	private String customerName;

	private double returnPurchaseAmt;
	
	private double discount;

	private double discountAmount;

	private double subTotal;

	private String gstType;

	private double gstAmount;

	private String createdBy;

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public int getReturnNumber() {
		return returnNumber;
	}

	public void setReturnNumber(int returnNumber) {
		this.returnNumber = returnNumber;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(int invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public long getCustomerMobileNo() {
		return customerMobileNo;
	}

	public void setCustomerMobileNo(long customerMobileNo) {
		this.customerMobileNo = customerMobileNo;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public List<ItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<ItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public int getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(int noOfItems) {
		this.noOfItems = noOfItems;
	}

	public double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public double getTotalReturnAmount() {
		return totalReturnAmount;
	}

	public void setTotalReturnAmount(double totalReturnAmount) {
		this.totalReturnAmount = totalReturnAmount;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public double getReturnPurchaseAmt() {
		return returnPurchaseAmt;
	}

	public void setReturnPurchaseAmt(double returnPurchaseAmt) {
		this.returnPurchaseAmt = returnPurchaseAmt;
	}

	public String getGstType() {
		return gstType;
	}

	public void setGstType(String gstType) {
		this.gstType = gstType;
	}

	public double getGstAmount() {
		return gstAmount;
	}

	public void setGstAmount(double gstAmount) {
		this.gstAmount = gstAmount;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public double getInvoiceNetSalesAmt() {
		return invoiceNetSalesAmt;
	}

	public void setInvoiceNetSalesAmt(double invoiceNetSalesAmt) {
		this.invoiceNetSalesAmt = invoiceNetSalesAmt;
	}

	public double getNewInvoiceNetSalesAmt() {
		return newInvoiceNetSalesAmt;
	}

	public void setNewInvoiceNetSalesAmt(double newInvoiceNetSalesAmt) {
		this.newInvoiceNetSalesAmt = newInvoiceNetSalesAmt;
	}

}
