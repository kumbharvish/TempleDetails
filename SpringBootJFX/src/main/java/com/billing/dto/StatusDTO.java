package com.billing.dto;

public class StatusDTO {
	
	private int statusCode;
	
	private String exception;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public StatusDTO(int statusCode) {
		super();
		this.statusCode = statusCode;
	}
	public StatusDTO() {
		super();
	}
}
