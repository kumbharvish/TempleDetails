package com.billing.dto;

public class PrintTemplate {
	
	private int id;
	
	private String name;
	
	private String jasperName;
	
	private String isDefault;

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

	public String getJasperName() {
		return jasperName;
	}

	public void setJasperName(String jasperName) {
		this.jasperName = jasperName;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

}
