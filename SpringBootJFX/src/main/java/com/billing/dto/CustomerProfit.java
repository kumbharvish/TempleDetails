package com.billing.dto;


public class CustomerProfit implements Comparable<CustomerProfit>{

	private long custMobileNumber;
	private String custName;
	private double sumOfBillAmt;
	private double sumOfBillPurAmt;
	private int totalNoOfItems;
	private int totalQty;
	private double profit;
	
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public long getCustMobileNumber() {
		return custMobileNumber;
	}
	public void setCustMobileNumber(long custMobileNumber) {
		this.custMobileNumber = custMobileNumber;
	}
	public double getSumOfBillAmt() {
		return sumOfBillAmt;
	}
	public void setSumOfBillAmt(double sumOfBillAmt) {
		this.sumOfBillAmt = sumOfBillAmt;
	}
	public double getSumOfBillPurAmt() {
		return sumOfBillPurAmt;
	}
	public void setSumOfBillPurAmt(double sumOfBillPurAmt) {
		this.sumOfBillPurAmt = sumOfBillPurAmt;
	}
	public double getProfit() {
		return sumOfBillAmt - sumOfBillPurAmt;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}
	
	public int getTotalNoOfItems() {
		return totalNoOfItems;
	}
	public void setTotalNoOfItems(int totalNoOfItems) {
		this.totalNoOfItems = totalNoOfItems;
	}
	public int getTotalQty() {
		return totalQty;
	}
	public void setTotalQty(int totalQty) {
		this.totalQty = totalQty;
	}
	@Override
	public int compareTo(CustomerProfit o) {

		if (this.getProfit() < o.getProfit()) {
	        return 1;
	    }
	    else if(this.getProfit() > o.getProfit()){
	        return -1;
	    }

		return 0;
	}
	
}
