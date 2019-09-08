package com.billing.dto;

public class Supplier {

	private int supplierID;
	private String supplierName;
	private String emailId;
	private long supplierMobile;
	private String supplierAddress;
	private String city;
	private long phoneNumber;
	private String panNo;
	private String gstNo;
	private String compnyName;
	private String comments;

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public long getSupplierMobile() {
		return supplierMobile;
	}

	public void setSupplierMobile(long supplierMobile) {
		this.supplierMobile = supplierMobile;
	}

	public String getSupplierAddress() {
		return supplierAddress;
	}

	public void setSupplierAddress(String supplierAddress) {
		this.supplierAddress = supplierAddress;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public String getGstNo() {
		return gstNo;
	}

	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}

	public String getCompnyName() {
		return compnyName;
	}

	public void setCompnyName(String compnyName) {
		this.compnyName = compnyName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getSupplierName() {
		return supplierName;
	}
}
