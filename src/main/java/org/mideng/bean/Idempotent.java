package org.mideng.bean;

import java.util.Map;

public class Idempotent {

	public static final Integer STATUS_START = 100;
	public static final Integer STATUS_REDIRECT = 101;
	public static final Integer STATUS_FINISIED = 200;
	
	private String key;

	private Integer status;
	
	private Integer statusCode;
	
	private String statusMessage;
	
	private String result;
	
	private Map<String, String> headers;


	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}


	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	
}
