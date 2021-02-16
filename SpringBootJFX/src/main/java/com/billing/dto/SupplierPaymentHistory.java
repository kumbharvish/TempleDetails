package com.billing.dto;


public class SupplierPaymentHistory {
	
	private int supplierId;
	
	private String supplierName;
	
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

	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	
	

}
