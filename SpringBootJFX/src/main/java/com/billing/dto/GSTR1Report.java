package com.billing.dto;

import java.util.List;

public class GSTR1Report {

	private List<GSTR1Data> invoiceList;

	private List<GSTR1Data> saleReturnList;

	private String fromDate;

	private String toDate;

	private String leagleName;

	private String gstin;

	private Double totalTaxableValue;
	
	private Double totalInvoiceValue;
	
	private Double totalCGST;
	
	private Double totalSGST;

	public Double getTotalTaxableValue() {
		return totalTaxableValue;
	}

	public void setTotalTaxableValue(Double totalTaxableValue) {
		this.totalTaxableValue = totalTaxableValue;
	}

	public Double getTotalInvoiceValue() {
		return totalInvoiceValue;
	}

	public void setTotalInvoiceValue(Double totalInvoiceValue) {
		this.totalInvoiceValue = totalInvoiceValue;
	}

	public Double getTotalCGST() {
		return totalCGST;
	}

	public void setTotalCGST(Double totalCGST) {
		this.totalCGST = totalCGST;
	}

	public Double getTotalSGST() {
		return totalSGST;
	}

	public void setTotalSGST(Double totalSGST) {
		this.totalSGST = totalSGST;
	}

	public List<GSTR1Data> getInvoiceList() {
		return invoiceList;
	}

	public void setInvoiceList(List<GSTR1Data> invoiceList) {
		this.invoiceList = invoiceList;
	}

	public List<GSTR1Data> getSaleReturnList() {
		return saleReturnList;
	}

	public void setSaleReturnList(List<GSTR1Data> saleReturnList) {
		this.saleReturnList = saleReturnList;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getLeagleName() {
		return leagleName;
	}

	public void setLeagleName(String leagleName) {
		this.leagleName = leagleName;
	}

	public String getGstin() {
		return gstin;
	}

	public void setGstin(String gstin) {
		this.gstin = gstin;
	}

}
