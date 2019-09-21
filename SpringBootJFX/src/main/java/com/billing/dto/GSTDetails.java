package com.billing.dto;

public class GSTDetails {
	
	private String name;
	
	private double rate;
	
	private double cgst;
	
	private double sgst;
	
	private double gstAmount;
	
	private String inclusiveFlag;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public double getCgst() {
		return cgst;
	}

	public void setCgst(double cgst) {
		this.cgst = cgst;
	}

	public double getSgst() {
		return sgst;
	}

	public void setSgst(double sgst) {
		this.sgst = sgst;
	}

	public double getGstAmount() {
		return gstAmount;
	}

	public void setGstAmount(double gstAmount) {
		this.gstAmount = gstAmount;
	}

	public String getInclusiveFlag() {
		return inclusiveFlag;
	}

	public void setInclusiveFlag(String inclusiveFlag) {
		this.inclusiveFlag = inclusiveFlag;
	}

}
