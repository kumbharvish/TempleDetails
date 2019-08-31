package com.billing.dto;

import java.io.InputStream;

public class MyStoreDetails {
	
	private int myStoreId;
	
	private String storeName;
	
	private String address;

	private String address2;
	
	private String city;
	
	private String district;
	
	private String state;
	
	private long phone;
	
	private long cstNo;
	
	private String panNo;
	
	private long vatNo;
	
	private long electricityNo;
	
	private String ownerName;
	
	private long mobileNo;
	
	private String gstNo;
	
	private InputStream imagePath;

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public long getCstNo() {
		return cstNo;
	}

	public void setCstNo(long cstNo) {
		this.cstNo = cstNo;
	}

	public String getPanNo() {
		return panNo;
	}

	public void setPanNo(String panNo) {
		this.panNo = panNo;
	}

	public long getVatNo() {
		return vatNo;
	}

	public void setVatNo(long vatNo) {
		this.vatNo = vatNo;
	}

	public long getElectricityNo() {
		return electricityNo;
	}

	public void setElectricityNo(long electricityNo) {
		this.electricityNo = electricityNo;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public long getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(long mobileNo) {
		this.mobileNo = mobileNo;
	}

	public int getMyStoreId() {
		return myStoreId;
	}

	public void setMyStoreId(int myStoreId) {
		this.myStoreId = myStoreId;
	}

	public InputStream getImagePath() {
		return imagePath;
	}

	public void setImagePath(InputStream imagePath) {
		this.imagePath = imagePath;
	}

	public String getGstNo() {
		return gstNo;
	}

	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}
	
}
