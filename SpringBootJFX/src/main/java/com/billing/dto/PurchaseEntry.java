package com.billing.dto;

import java.util.List;

public class PurchaseEntry {

	private int purchaseEntryNo;
	
	private String purchaseEntryDate;

	private int supplierId;

	private String supplierName;

	private int billNumber;

	private String billDate;

	private String comments;

	private List<ItemDetails> itemDetails;

	private int noOfItems;

	private double totalQuantity;

	private double totalAmtBeforeTax;

	private double totalGSTAmount;

	private String paymentMode;

	private double extraCharges;

	private double totalAmount;
	
	private String createdBy;
	
	private double discountAmount;

	public int getPurchaseEntryNo() {
		return purchaseEntryNo;
	}

	public void setPurchaseEntryNo(int purchaseEntryNo) {
		this.purchaseEntryNo = purchaseEntryNo;
	}

	public String getPurchaseEntryDate() {
		return purchaseEntryDate;
	}

	public void setPurchaseEntryDate(String purchaseEntryDate) {
		this.purchaseEntryDate = purchaseEntryDate;
	}

	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public String getBillDate() {
		return billDate;
	}

	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public List<ItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<ItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public int getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(int noOfItems) {
		this.noOfItems = noOfItems;
	}

	public double getTotalAmtBeforeTax() {
		return totalAmtBeforeTax;
	}

	public void setTotalAmtBeforeTax(double totalAmtBeforeTax) {
		this.totalAmtBeforeTax = totalAmtBeforeTax;
	}

	public double getTotalGSTAmount() {
		return totalGSTAmount;
	}

	public void setTotalGSTAmount(double totalGSTAmount) {
		this.totalGSTAmount = totalGSTAmount;
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

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}

}
