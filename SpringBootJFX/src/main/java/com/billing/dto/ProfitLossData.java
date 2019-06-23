package com.billing.dto;

public class ProfitLossData {

	private String description;
	
	private double amount;

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
		return "ProfitLossData [description=" + description + ", amount="
				+ amount + "]";
	}
	
	
}
