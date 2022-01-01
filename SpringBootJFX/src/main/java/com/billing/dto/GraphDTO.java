package com.billing.dto;

public class GraphDTO {
	
	private String paymentMode;
	private Double totalAmount;
	
	//Daily Sales Amount & Profit Amount Report Fields
	private String date;
	private Integer totalCollection;
	private Integer totalPurchaseAmt;
	private Integer totalProfit;
	private Integer noOfInvoicesMade;
	
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Integer getTotalCollection() {
		return totalCollection;
	}
	public void setTotalCollection(Integer totalCollection) {
		this.totalCollection = totalCollection;
	}
	public Integer getTotalProfit() {
		return totalCollection-totalPurchaseAmt;
	}
	public void setTotalProfit(Integer totalProfit) {
		this.totalProfit = totalProfit;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Integer getTotalPurchaseAmt() {
		return totalPurchaseAmt;
	}
	public void setTotalPurchaseAmt(Integer totalPurchaseAmt) {
		this.totalPurchaseAmt = totalPurchaseAmt;
	}
	@Override
	public String toString() {
		return "GraphDTO [paymentMode=" + paymentMode + ", totalAmount="
				+ totalAmount + ", date=" + date + ", totalCollection="
				+ totalCollection + ", totalPurchaseAmt=" + totalPurchaseAmt
				+ ", totalProfit=" + getTotalProfit() + "]";
	}
	public Integer getNoOfInvoicesMade() {
		return noOfInvoicesMade;
	}
	public void setNoOfInvoicesMade(Integer noOfInvoicesMade) {
		this.noOfInvoicesMade = noOfInvoicesMade;
	}
	
}
