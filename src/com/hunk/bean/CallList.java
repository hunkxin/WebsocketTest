package com.hunk.bean;

public class CallList {
	private String telnum;
	private String retry;
	public String getTelnum() {
		return telnum;
	}
	public void setTelnum(String telnum) {
		this.telnum = telnum;
	}
	public String getRetry() {
		return retry;
	}
	public void setRetry(String retry) {
		this.retry = retry;
	}
	public CallList(String telnum, String retry) {
		super();
		this.telnum = telnum;
		this.retry = retry;
	}
	public CallList(){}
}
