package com.billing.dto;

public class AccountDetails {

	private String name;

	private double balance;

	private String timestamp;

	private long lastTxnId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getLastTxnId() {
		return lastTxnId;
	}

	public void setLastTxnId(long lastTxnId) {
		this.lastTxnId = lastTxnId;
	}

}
