package com.hunk.bean;

public class CtiExt {
	private int exist;
	private String name;
	private int ifreg;
	private String agentname;
	private String status;
	private String laststatuschange;
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
	public String getLaststatuschange() {
		return laststatuschange;
	}
	public void setLaststatuschange(String laststatuschange) {
		this.laststatuschange = laststatuschange;
	}
	public CtiExt(int exist, String name, int ifreg, String agentname, String status, String laststatuschange) {
		super();
		this.exist = exist;
		this.name = name;
		this.ifreg = ifreg;
		this.agentname = agentname;
		this.status = status;
		this.laststatuschange = laststatuschange;
	}
	public CtiExt(){}
}
