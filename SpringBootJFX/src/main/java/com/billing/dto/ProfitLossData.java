package com.billing.dto;

public class ProfitLossData {

	private String description;

	private double amount;
	
	private String displayAmount;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "ProfitLossData [description=" + description + ", amount=" + amount + "]";
	}

	public ProfitLossData(String description, String displayAmount) {
		super();
		this.description = description;
		this.displayAmount = displayAmount;
	}
	public ProfitLossData() {
		
	}

	public String getDisplayAmount() {
		return displayAmount;
	}

	public void setDisplayAmount(String displayAmount) {
		this.displayAmount = displayAmount;
	}
}
