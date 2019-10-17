package com.billing.dto;

import java.util.Comparator;

public class Product {

	private int productCode;

	private Long productBarCode;

	private String productName;

	private String measure;

	private double quantity;

	private double purcaseRate;

	private double productTax;

	private double cgst;

	private double sgst;

	private double purcasePrice;

	private double sellPrice;

	private double productMRP;

	private double discount;
	
	private double discountAmount;

	private String entryDate;

	private String lastUpdateDate;

	private String description;

	private String enterBy;

	private String productCategory;

	private int sellQuantity;

	private double stockValueAmount;

	private double profit;

	private double stockPurchaseAmount;

	private int categoryCode;

	private String supplierName;

	private int supplierId;

	private String timeStamp;
	
	private double tableDispRate;
	
	private double tableDispQuantity;
	
	private double tableDispAmount;
	
	private GSTDetails gstDetails;
	
	private double tableAmountShowValue;
	
	private double cgstPercent;
	
	private double sgstPercent;
	
	private double orignalDiscount;
	
	private String hsn;

	public int getProductCode() {
		return productCode;
	}

	public void setProductCode(int productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public double getPurcasePrice() {
		return purcasePrice;
	}

	public void setPurcasePrice(double purcasePrice) {
		this.purcasePrice = purcasePrice;
	}

	public double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public double getProductMRP() {
		return productMRP;
	}

	public void setProductMRP(double productMRP) {
		this.productMRP = productMRP;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public String getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(String entryDate) {
		this.entryDate = entryDate;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnterBy() {
		return enterBy;
	}

	public void setEnterBy(String enterBy) {
		this.enterBy = enterBy;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public int getSellQuantity() {
		return sellQuantity;
	}

	public void setSellQuantity(int sellQuantity) {
		this.sellQuantity = sellQuantity;
	}

	public double getItemPurchasePrice() {

		return sellQuantity * purcasePrice;
	}

	public double getPurcaseRate() {
		return purcaseRate;
	}

	public void setPurcaseRate(double purcaseRate) {
		this.purcaseRate = purcaseRate;
	}

	public double getProductTax() {
		return productTax;
	}

	public void setProductTax(double productTax) {
		this.productTax = productTax;
	}

	public double getProfit() {
		return productMRP - purcasePrice;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public static Comparator<Product> getComparator(SortParameter... sortParameters) {
		return new ProductComparator(sortParameters);
	}

	public double getStockValueAmount() {
		return productMRP * quantity;
	}

	public void setStockValueAmount(double stockValueAmount) {
		this.stockValueAmount = stockValueAmount;
	}

	public Long getProductBarCode() {
		return productBarCode;
	}

	public void setProductBarCode(Long productBarCode) {
		this.productBarCode = productBarCode;
	}

	public double getStockPurchaseAmount() {
		return purcasePrice * quantity;
	}

	public void setStockPurchaseAmount(double stockPurchaseAmount) {
		this.stockPurchaseAmount = stockPurchaseAmount;
	}

	public int getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(int categoryCode) {
		this.categoryCode = categoryCode;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public double getCgst() {
		return gstDetails.getCgst();
	}

	public double getSgst() {
		return gstDetails.getSgst();
	}

	public double getTableDispRate() {
		return tableDispRate;
	}

	public void setTableDispRate(double tableDispRate) {
		this.tableDispRate = tableDispRate;
	}

	public double getTableDispQuantity() {
		return tableDispQuantity;
	}

	public void setTableDispQuantity(double tableDispQuantity) {
		this.tableDispQuantity = tableDispQuantity;
	}

	public double getTableDispAmount() {
		//Amount - Discount
		return tableDispAmount-getDiscountAmount();
	}

	public void setTableDispAmount(double tableDispAmount) {
		this.tableDispAmount = tableDispAmount;
	}
	
	public double getDiscountAmount() {
		return ((tableDispQuantity*tableDispRate)*(discount/100));
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}


	@Override
	public String toString() {
		return "Product [productCode=" + productCode + ", productBarCode=" + productBarCode + ", productName="
				+ productName + ", measure=" + measure + ", quanity=" + quantity + ", purcaseRate=" + purcaseRate
				+ ", productTax=" + productTax + ", purcasePrice=" + purcasePrice + ", sellPrice=" + sellPrice
				+ ", productMRP=" + productMRP + ", discount=" + discount + ", entryDate=" + entryDate
				+ ", lastUpdateDate=" + lastUpdateDate + ", Description=" + description + ", enterBy=" + enterBy
				+ ", productCategory=" + productCategory + ", sellQuantity=" + sellQuantity + ", stockValueAmount="
				+ stockValueAmount + ", profit=" + profit + ", stockPurchaseAmount=" + stockPurchaseAmount
				+ ", categoryCode=" + categoryCode + ", supplierName=" + supplierName + ", supplierId=" + supplierId
				+ ", timeStamp=" + timeStamp + "]";
	}

	public GSTDetails getGstDetails() {
		return gstDetails;
	}

	public void setGstDetails(GSTDetails gstDetails) {
		this.gstDetails = gstDetails;
	}

	public double getTableAmountShowValue() {
		if("Y".equalsIgnoreCase(gstDetails.getInclusiveFlag())) {
			return getTableDispAmount() - gstDetails.getGstAmount();
		}
		return getTableDispAmount();
	}

	public void setTableAmountShowValue(double tableAmountShowValue) {
		this.tableAmountShowValue = tableAmountShowValue;
	}

	public double getCgstPercent() {
		return productTax/2;
	}

	public double getSgstPercent() {
		return productTax/2;
	}

	public double getOrignalDiscount() {
		return orignalDiscount;
	}

	public void setOrignalDiscount(double orignalDiscount) {
		this.orignalDiscount = orignalDiscount;
	}

	public String getHsn() {
		return hsn;
	}

	public void setHsn(String hsn) {
		this.hsn = hsn;
	}

	public enum SortParameter {
		CATEGORY_NAME_ASCENDING, PROFIT_ASCENDING, PRODUCT_NAME_ASCENDING, STOCK_VALUE_AMT_ASC, STOCK_QUANTITY_ASC
	}

	private static class ProductComparator implements Comparator<Product> {
		private SortParameter[] parameters;

		private ProductComparator(SortParameter[] parameters) {
			this.parameters = parameters;
		}

		public int compare(Product o1, Product o2) {
			int comparison;
			for (SortParameter parameter : parameters) {
				switch (parameter) {
				case CATEGORY_NAME_ASCENDING:
					comparison = o1.getProductCategory().compareTo(o2.getProductCategory());
					if (comparison != 0)
						return comparison;
					break;
				case PROFIT_ASCENDING:
					if (o1.getProfit() < o2.getProfit()) {
						return 1;
					} else if (o1.getProfit() > o2.getProfit()) {
						return -1;
					}
					break;
				case PRODUCT_NAME_ASCENDING:
					comparison = o1.getProductName().compareTo(o2.getProductName());
					if (comparison != 0)
						return comparison;
					break;
				case STOCK_VALUE_AMT_ASC:
					if (o1.getStockValueAmount() < o2.getStockValueAmount()) {
						return 1;
					} else if (o1.getStockValueAmount() > o2.getStockValueAmount()) {
						return -1;
					}
					break;
				case STOCK_QUANTITY_ASC:
					if (o1.getQuantity() < o2.getQuantity()) {
						return 1;
					} else if (o1.getQuantity() > o2.getQuantity()) {
						return -1;
					}
					break;
				}
			}
			return 0;
		}
	}

}
