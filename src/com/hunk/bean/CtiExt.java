package com.hunk.bean;

public class CtiExt {
	private int exist;
	private String name;
	private int ifreg;
	private String agentname;
	private String status;
	public int getExist() {
		return exist;
	}
	public void setExist(int exist) {
		this.exist = exist;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIfreg() {
		return ifreg;
	}
	public void setIfreg(int ifreg) {
		this.ifreg = ifreg;
	}
	public String getAgentname() {
		return agentname;
	}
	public void setAgentname(String agentname) {
		this.agentname = agentname;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public CtiExt(int exist, String name, int ifreg, String agentname, String status) {
		super();
		this.exist = exist;
		this.name = name;
		this.ifreg = ifreg;
		this.agentname = agentname;
		this.status = status;
	}
	public CtiExt(){}
}
