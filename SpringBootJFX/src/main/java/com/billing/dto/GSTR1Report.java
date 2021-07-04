package com.billing.dto;

import java.util.List;

public class GSTR1Report {

	private List<GSTR1Data> invoiceList;

	private List<GSTR1Data> saleReturnList;

	private String fromDate;

	private String toDate;
	
	private String leagleName;
	
	private String gstin;

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
