package com.billing.dto;

import java.util.Comparator;

public class Tax {

	private int id;

	private String name;

	private double value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public enum SortParameter {
		TAX_VALUE
	}

	public static Comparator<Tax> getComparator(SortParameter... sortParameters) {
		return new TAXComparator(sortParameters);
	}

	private static class TAXComparator implements Comparator<Tax> {
		private SortParameter[] parameters;

		private TAXComparator(SortParameter[] parameters) {
			this.parameters = parameters;
		}

		public int compare(Tax o1, Tax o2) {
			for (SortParameter parameter : parameters) {
				switch (parameter) {
				case TAX_VALUE:
					if (o1.getValue() < o2.getValue()) {
						return -1;
					} else if (o1.getValue() > o2.getValue()) {
						return 1;
					}
				}
			}
			return 0;
		}
	}

}
