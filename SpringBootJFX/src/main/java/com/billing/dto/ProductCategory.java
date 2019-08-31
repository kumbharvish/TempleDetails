package com.billing.dto;

import java.util.Comparator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProductCategory {

	private int categoryCode;

	private StringProperty categoryName;

	private StringProperty categoryDescription;

	private double categoryStockQty;

	private double categoryStockAmount;

	// Default Constructor
	public ProductCategory() {
		categoryName = new SimpleStringProperty("");
		categoryDescription = new SimpleStringProperty("");
	}

	public int getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(int categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getCategoryName() {
		return categoryName.get();
	}

	public void setCategoryName(String categoryName) {
		this.categoryName.set(categoryName);
	}

	public String getCategoryDescription() {
		return categoryDescription.get();
	}

	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription.set(categoryDescription);
	}

	// Property Methods
	public StringProperty categoryDescProperty() {
		return categoryDescription;
	}

	public StringProperty categoryNameProperty() {
		return categoryName;
	}

	public enum SortParameter {
		CATEGORY_NAME_ASCENDING, STOCK_VALUE_AMT_ASC, STOCK_QUANTITY_ASC
	}

	public static Comparator<ProductCategory> getComparator(SortParameter... sortParameters) {
		return new ProductCategoryComparator(sortParameters);
	}

	public double getCategoryStockQty() {
		return categoryStockQty;
	}

	public void setCategoryStockQty(double categoryStockQty) {
		this.categoryStockQty = categoryStockQty;
	}

	public double getCategoryStockAmount() {
		return categoryStockAmount;
	}

	public void setCategoryStockAmount(double categoryStockAmount) {
		this.categoryStockAmount = categoryStockAmount;
	}

	private static class ProductCategoryComparator implements Comparator<ProductCategory> {
		private SortParameter[] parameters;

		private ProductCategoryComparator(SortParameter[] parameters) {
			this.parameters = parameters;
		}

		public int compare(ProductCategory o1, ProductCategory o2) {
			int comparison;
			for (SortParameter parameter : parameters) {
				switch (parameter) {
				case CATEGORY_NAME_ASCENDING:
					comparison = o1.getCategoryName().compareTo(o2.getCategoryName());
					if (comparison != 0)
						return comparison;
					break;
				case STOCK_VALUE_AMT_ASC:
					if (o1.getCategoryStockAmount() < o2.getCategoryStockAmount()) {
						return 1;
					} else if (o1.getCategoryStockAmount() > o2.getCategoryStockAmount()) {
						return -1;
					}
					break;
				case STOCK_QUANTITY_ASC:
					if (o1.getCategoryStockQty() < o2.getCategoryStockQty()) {
						return 1;
					} else if (o1.getCategoryStockQty() > o2.getCategoryStockQty()) {
						return -1;
					}
					break;
				}
			}
			return 0;
		}
	}

}
