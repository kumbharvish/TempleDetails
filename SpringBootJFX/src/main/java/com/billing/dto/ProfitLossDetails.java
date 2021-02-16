package com.billing.dto;

import java.util.List;

public class ProfitLossDetails {

	private List<ProfitLossData> debit;
	
	private List<ProfitLossData> credit;
	
	private double totalCredit;
	
	private double totalDebit;
	
	private double netProfit;
	
	private double netLoss;

	public List<ProfitLossData> getDebit() {
		return debit;
	}

	public void setDebit(List<ProfitLossData> debit) {
		this.debit = debit;
	}

	public List<ProfitLossData> getCredit() {
		return credit;
	}

	public void setCredit(List<ProfitLossData> credit) {
		this.credit = credit;
	}

	public double getTotalCredit() {
		return totalCredit;
	}

	public void setTotalCredit(double totalCredit) {
		this.totalCredit = totalCredit;
	}

	public double getTotalDebit() {
		return totalDebit;
	}

	public void setTotalDebit(double totalDebit) {
		this.totalDebit = totalDebit;
	}

	public double getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(double netProfit) {
		this.netProfit = netProfit;
	}

	public double getNetLoss() {
		return netLoss;
	}

	public void setNetLoss(double netLoss) {
		this.netLoss = netLoss;
	}

	@Override
	public String toString() {
		return "ProfitLossDetails [debit=" + debit + ", credit=" + credit
				+ ", totalCredit=" + totalCredit + ", totalDebit=" + totalDebit
				+ ", netProfit=" + netProfit + ", netLoss=" + netLoss + "]";
	}
	
}
