package com.billing.dto;

public class GSTR1Data {

	private String gstInNo;
	private String partyName;
	private Integer invoiceNo;
	private String invoiceDate;
	private Double invoiceTotalAmount;
	private Double gstRate;
	private Double taxableValue;
	private Double cgst;
	private Double sgst;
	private String placeOfSupply;
	private Integer returnNo;
	private String returnDate;

	public String getGstInNo() {
		return gstInNo;
	}

	public void setGstInNo(String gstInNo) {
		this.gstInNo = gstInNo;
	}

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public Double getInvoiceTotalAmount() {
		return invoiceTotalAmount;
	}

	public void setInvoiceTotalAmount(Double invoiceTotalAmount) {
		this.invoiceTotalAmount = invoiceTotalAmount;
	}

	public Double getGstRate() {
		return gstRate;
	}

	public void setGstRate(Double gstRate) {
		this.gstRate = gstRate;
	}

	public Double getTaxableValue() {
		return taxableValue;
	}

	public void setTaxableValue(Double taxableValue) {
		this.taxableValue = taxableValue;
	}

	public Double getCgst() {
		return cgst;
	}

	public void setCgst(Double cgst) {
		this.cgst = cgst;
	}

	public Double getSgst() {
		return sgst;
	}

	public void setSgst(Double sgst) {
		this.sgst = sgst;
	}

	public String getPlaceOfSupply() {
		return placeOfSupply;
	}

	public void setPlaceOfSupply(String placeOfSupply) {
		this.placeOfSupply = placeOfSupply;
	}

	public Integer getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(Integer invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public Integer getReturnNo() {
		return returnNo;
	}

	public void setReturnNo(Integer returnNo) {
		this.returnNo = returnNo;
	}

	public String getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(String returnDate) {
		this.returnDate = returnDate;
	}

}
