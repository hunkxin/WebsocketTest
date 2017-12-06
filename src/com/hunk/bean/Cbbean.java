package com.hunk.bean;

public class Cbbean {
	private String id;
	private String val;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
	public Cbbean(String id, String val) {
		super();
		this.id = id;
		this.val = val;
	}
	
}
