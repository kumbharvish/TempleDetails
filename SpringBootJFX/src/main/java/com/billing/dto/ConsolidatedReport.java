package com.billing.dto;

public class ConsolidatedReport {
	
	private double totalSalesPendingAmt;
	
	private double totalSalesCashAmt;
	
	private double totalSalesReturnAmt;
	
	private double totalExpensesAmt;
	
	private double totalCustSettlementAmt;
	
	private double totalPurchaseAmt;
	
	private double totalQtySold;

	public double getTotalSalesPendingAmt() {
		return totalSalesPendingAmt;
	}

	public void setTotalSalesPendingAmt(double totalSalesPendingAmt) {
		this.totalSalesPendingAmt = totalSalesPendingAmt;
	}

	public double getTotalSalesCashAmt() {
		return totalSalesCashAmt;
	}

	public void setTotalSalesCashAmt(double totalSalesCashAmt) {
		this.totalSalesCashAmt = totalSalesCashAmt;
	}

	public double getTotalSalesReturnAmt() {
		return totalSalesReturnAmt;
	}

	public void setTotalSalesReturnAmt(double totalSalesReturnAmt) {
		this.totalSalesReturnAmt = totalSalesReturnAmt;
	}

	public double getTotalExpensesAmt() {
		return totalExpensesAmt;
	}

	public void setTotalExpensesAmt(double totalExpensesAmt) {
		this.totalExpensesAmt = totalExpensesAmt;
	}

	public double getTotalCustSettlementAmt() {
		return totalCustSettlementAmt;
	}

	public void setTotalCustSettlementAmt(double totalCustSettlementAmt) {
		this.totalCustSettlementAmt = totalCustSettlementAmt;
	}

	public double getTotalPurchaseAmt() {
		return totalPurchaseAmt;
	}

	public void setTotalPurchaseAmt(double totalPurchaseAmt) {
		this.totalPurchaseAmt = totalPurchaseAmt;
	}

	public double getTotalQtySold() {
		return totalQtySold;
	}

	public void setTotalQtySold(double totalQtySold) {
		this.totalQtySold = totalQtySold;
	}

	@Override
	public String toString() {
		return "MonthlyReport [totalSalesPendingAmt=" + totalSalesPendingAmt
				+ ", totalSalesCashAmt=" + totalSalesCashAmt
				+ ", totalSalesReturnAmt=" + totalSalesReturnAmt
				+ ", totalExpensesAmt=" + totalExpensesAmt
				+ ", totalCustSettlementAmt=" + totalCustSettlementAmt
				+ ", totalPurchaseAmt=" + totalPurchaseAmt + ", totalQtySold="
				+ totalQtySold + "]";
	}
	
}
