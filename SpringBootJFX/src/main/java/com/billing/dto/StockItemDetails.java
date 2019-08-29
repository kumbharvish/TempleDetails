package com.billing.dto;

public class StockItemDetails {
	
	private int itemNo;
	
	private int stockNumber;
	
	private String itemName;
	
	private double MRP;
	
	private double rate;
	
	private double quantity;
	
	private double amount;
	
	private double tax;

	private double purchasePrice;
	
	public int getItemNo() {
		return itemNo;
	}

	public void setItemNo(int itemNo) {
		this.itemNo = itemNo;
	}

	public int getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(int stockNumber) {
		this.stockNumber = stockNumber;
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
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getTax() {
		return tax;
	}

	public void setTax(double tax) {
		this.tax = tax;
	}

	public double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	@Override
	public String toString() {
		return "StockItemDetails [itemNo=" + itemNo + ", stockNumber="
				+ stockNumber + ", itemName=" + itemName + ", MRP=" + MRP
				+ ", rate=" + rate + ", quantity=" + quantity + ", amount="
				+ amount + ", tax=" + tax + ", purchasePrice=" + purchasePrice
				+ "]";
	}
	
}
