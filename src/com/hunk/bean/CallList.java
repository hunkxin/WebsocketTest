package com.hunk.bean;

public class CallList {
	private String autoid;
	private String telnum;
	private String trycount;
	private String retry;
	public String getAutoid() {
		return autoid;
	}
	public void setAutoid(String autoid) {
		this.autoid = autoid;
	}
	public String getTelnum() {
		return telnum;
	}
	public void setTelnum(String telnum) {
		this.telnum = telnum;
	}
	public String getTrycount() {
		return trycount;
	}
	public void setTrycount(String trycount) {
		this.trycount = trycount;
	}
	public String getRetry() {
		return retry;
	}
	public void setRetry(String retry) {
		this.retry = retry;
	}
	public CallList(String autoid, String telnum, String trycount, String retry) {
		super();
		this.autoid = autoid;
		this.telnum = telnum;
		this.trycount = trycount;
		this.retry = retry;
	}
	public CallList(){}
}
