package com.billing.dto;

public class ItemDetails {

	private int itemNo;

	private int billNumber;

	private String itemName;

	private String unit;

	private double MRP;

	private double rate;

	private double quantity;

	private double amount;

	private double purchasePrice;

	private GSTDetails gstDetails;

	private double discountPercent;

	private double discountAmount;
	
	private String stockInOutFlag;
	
	private String hsn;
	
	//Fields for Purchase Entry
	
	private int purchaseEntryNo;

	public int getItemNo() {
		return itemNo;
	}

	public void setItemNo(int itemNo) {
		this.itemNo = itemNo;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public double getMRP() {
		return MRP;
	}

	public void setMRP(double mRP) {
		MRP = mRP;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getAmount() {
		return getGstDetails().getTaxableAmount();
	}

	public double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public int getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(int billNumber) {
		this.billNumber = billNumber;
	}

	public GSTDetails getGstDetails() {
		return gstDetails;
	}

	public void setGstDetails(GSTDetails gstDetails) {
		this.gstDetails = gstDetails;
	}

	public double getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(double discountPercent) {
		this.discountPercent = discountPercent;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getStockInOutFlag() {
		return stockInOutFlag;
	}

	public void setStockInOutFlag(String stockInOutFlag) {
		this.stockInOutFlag = stockInOutFlag;
	}

	@Override
	public String toString() {
		return "ItemDetails [itemNo=" + itemNo + ", billNumber=" + billNumber + ", itemName=" + itemName + ", unit="
				+ unit + ", MRP=" + MRP + ", rate=" + rate + ", quantity=" + quantity + ", amount=" + amount
				+ ", purchasePrice=" + purchasePrice + ", gstDetails=" + gstDetails + ", discountPercent="
				+ discountPercent + ", discountAmount=" + discountAmount + ", stockInOutFlag=" + stockInOutFlag + "]";
	}

	public String getHsn() {
		return hsn;
	}

	public void setHsn(String hsn) {
		this.hsn = hsn;
	}

	public int getPurchaseEntryNo() {
		return purchaseEntryNo;
	}

	public void setPurchaseEntryNo(int purchaseEntryNo) {
		this.purchaseEntryNo = purchaseEntryNo;
	}

}
