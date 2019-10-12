package com.billing.dto;

import java.util.List;

public class SalesReport {

	private List<BillDetails> billList;

	private String fromDate;

	private String toDate;

	private Double totalPendingAmt;

	private Double totalCashAmt;

	private Double totalAmt;

	private Double totalQty;

	private Integer totalNoOfItems;

	public List<BillDetails> getBillList() {
		return billList;
	}

	public void setBillList(List<BillDetails> billList) {
		this.billList = billList;
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

	public Double getTotalPendingAmt() {
		return totalPendingAmt;
	}

	public void setTotalPendingAmt(Double totalPendingAmt) {
		this.totalPendingAmt = totalPendingAmt;
	}

	public Double getTotalCashAmt() {
		return totalCashAmt;
	}

	public void setTotalCashAmt(Double totalCashAmt) {
		this.totalCashAmt = totalCashAmt;
	}

	public Double getTotalAmt() {
		return totalAmt;
	}

	public void setTotalAmt(Double totalAmt) {
		this.totalAmt = totalAmt;
	}

	public Double getTotalQty() {
		return totalQty;
	}

	public void setTotalQty(Double totalQty) {
		this.totalQty = totalQty;
	}

	public Integer getTotalNoOfItems() {
		return totalNoOfItems;
	}

	public void setTotalNoOfItems(Integer totalNoOfItems) {
		this.totalNoOfItems = totalNoOfItems;
	}

}
