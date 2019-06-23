package com.billing.dto;

import java.sql.Timestamp;

public class StockLedger {

	private int productCode;
	
	private Timestamp timeStamp;
	
	private int stockIn;
	
	private int stockOut;
	
	private String narration;
	
	private String transactionType;

	public int getProductCode() {
		return productCode;
	}

	public void setProductCode(int productCode) {
		this.productCode = productCode;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getStockIn() {
		return stockIn;
	}

	public void setStockIn(int stockIn) {
		this.stockIn = stockIn;
	}

	public int getStockOut() {
		return stockOut;
	}

	public void setStockOut(int stockOut) {
		this.stockOut = stockOut;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	
}
