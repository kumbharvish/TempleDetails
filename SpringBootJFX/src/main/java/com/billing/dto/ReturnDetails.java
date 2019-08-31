package com.billing.dto;

import java.util.List;

public class ReturnDetails {
	
	private int returnNumber;
	
	private String timestamp;
	
	private String comments;
	
	private int billNumber;
	
	private long customerMobileNo;
	
	private String billDate;
	
	private List<ItemDetails> itemDetails;
	
	private String billPaymentMode;
	
	private int noOfItems;
	
	private double totalQuantity;
	
	private double totalAmount;
	
	private String 	returnpaymentMode;
	
	private double billNetSalesAmt;
	
	private double newBillnetSalesAmt;
	
	private String customerName ;
	
	private double tax;
	
	private double returnPurchaseAmt;
	
	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(double taxAmount) {
		this.taxAmount = taxAmount;
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

	public double getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}

	private double discount;
	
	private double taxAmount;
	
	private double discountAmount;
	
	private double subTotal;
	
	private double grandTotal;
	
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

	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public long getCustomerMobileNo() {
		return customerMobileNo;
	}

	public void setCustomerMobileNo(long customerMobileNo) {
		this.customerMobileNo = customerMobileNo;
	}

	public String getBillDate() {
		return billDate;
	}

	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}

	public List<ItemDetails> getItemDetails() {
		return itemDetails;
	}

	public void setItemDetails(List<ItemDetails> itemDetails) {
		this.itemDetails = itemDetails;
	}

	public String getBillPaymentMode() {
		return billPaymentMode;
	}

	public void setBillPaymentMode(String billPaymentMode) {
		this.billPaymentMode = billPaymentMode;
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

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getReturnpaymentMode() {
		return returnpaymentMode;
	}

	public void setReturnpaymentMode(String returnpaymentMode) {
		this.returnpaymentMode = returnpaymentMode;
	}

	public double getBillNetSalesAmt() {
		return billNetSalesAmt;
	}

	public void setBillNetSalesAmt(double billNetSalesAmt) {
		this.billNetSalesAmt = billNetSalesAmt;
	}

	public double getNewBillnetSalesAmt() {
		return newBillnetSalesAmt;
	}

	public void setNewBillnetSalesAmt(double newBillnetSalesAmt) {
		this.newBillnetSalesAmt = newBillnetSalesAmt;
	}

	@Override
	public String toString() {
		return "ReturnDetails [returnNumber=" + returnNumber + ", timestamp="
				+ timestamp + ", comments=" + comments + ", billNumber="
				+ billNumber + ", customerMobileNo=" + customerMobileNo
				+ ", billDate=" + billDate + ", itemDetails=" + itemDetails
				+ ", billPaymentMode=" + billPaymentMode + ", noOfItems="
				+ noOfItems + ", totalQuanity=" + totalQuantity
				+ ", totalAmount=" + totalAmount + ", returnpaymentMode="
				+ returnpaymentMode + ", billNetSalesAmt=" + billNetSalesAmt
				+ ", newBillnetSalesAmt=" + newBillnetSalesAmt + "]";
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
	
}
