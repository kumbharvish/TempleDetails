package com.billing.dto;


public class CustomerPaymentHistory {
	
	private long custMobNo;
	
	private String custName;
	
	private double closingBlanace;
	
	private double credit;
	
	private double debit;
	
	private String narration;
	
	private String status;
	
	private String entryDate;
	

	public String getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(String entryDate) {
		this.entryDate = entryDate;
	}

	public long getCustMobNo() {
		return custMobNo;
	}

	public void setCustMobNo(long custMobNo) {
		this.custMobNo = custMobNo;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public double getClosingBlanace() {
		return closingBlanace;
	}

	public void setClosingBlanace(double closingBlanace) {
		this.closingBlanace = closingBlanace;
	}

	public double getCredit() {
		return credit;
	}

	public void setCredit(double credit) {
		this.credit = credit;
	}

	public double getDebit() {
		return debit;
	}

	public void setDebit(double debit) {
		this.debit = debit;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
