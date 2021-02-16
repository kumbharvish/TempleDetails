package com.billing.dto;

public class CashReport {
	
	private String description;
	
	private double creditAmount;
	
	private double debitAmount;
	
	private double closingBalance;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(double creditAmount) {
		this.creditAmount = creditAmount;
	}

	public double getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(double debitAmount) {
		this.debitAmount = debitAmount;
	}

	public double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(double closingBalance) {
		this.closingBalance = closingBalance;
	}

	@Override
	public String toString() {
		return "CashCounter [description=" + description + ", creditAmount="
				+ creditAmount + ", debitAmount=" + debitAmount
				+ ", closingBalance=" + closingBalance + "]";
	}

}
