package com.billing.dto;

import java.util.List;

public class Dashboard {
	
	private double toCollectAmount;
	
	private double toPayAmount;
	
	private double todaysCashAmount;
	
	private double stockValue;
	
	private	int lowStock;
	
	private List<GraphDTO> salesReport;

	public double getToCollectAmount() {
		return toCollectAmount;
	}

	public void setToCollectAmount(double toCollectAmount) {
		this.toCollectAmount = toCollectAmount;
	}

	public double getToPayAmount() {
		return toPayAmount;
	}

	public void setToPayAmount(double toPayAmount) {
		this.toPayAmount = toPayAmount;
	}

	public double getTodaysCashAmount() {
		return todaysCashAmount;
	}

	public void setTodaysCashAmount(double todaysCashAmount) {
		this.todaysCashAmount = todaysCashAmount;
	}

	public double getStockValue() {
		return stockValue;
	}

	public void setStockValue(double stockValue) {
		this.stockValue = stockValue;
	}

	public int getLowStock() {
		return lowStock;
	}

	public void setLowStock(int lowStock) {
		this.lowStock = lowStock;
	}

	public List<GraphDTO> getSalesReport() {
		return salesReport;
	}

	public void setSalesReport(List<GraphDTO> salesReport) {
		this.salesReport = salesReport;
	}

}
