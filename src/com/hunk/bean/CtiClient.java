package com.hunk.bean;

public class CtiClient {
	private int exist;
	private String guid;
	private String agentid;
	private String agentmd5pwd;
	private String loginext;
	private String agentrole;
	private String agentext;
	private int agentlevel;
	private String agentstate;
	private String agentlaststate;
	private int ifrevcallin;
	public int getExist() {
		return exist;
	}
	public void setExist(int exist) {
		this.exist = exist;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getAgentid() {
		return agentid;
	}
	public void setAgentid(String agentid) {
		this.agentid = agentid;
	}
	public String getAgentmd5pwd() {
		return agentmd5pwd;
	}
	public void setAgentmd5pwd(String agentmd5pwd) {
		this.agentmd5pwd = agentmd5pwd;
	}
	public String getLoginext() {
		return loginext;
	}
	public void setLoginext(String loginext) {
		this.loginext = loginext;
	}
	public String getAgentrole() {
		return agentrole;
	}
	public void setAgentrole(String agentrole) {
		this.agentrole = agentrole;
	}
	public String getAgentext() {
		return agentext;
	}
	public void setAgentext(String agentext) {
		this.agentext = agentext;
	}
	public int getAgentlevel() {
		return agentlevel;
	}
	public void setAgentlevel(int agentlevel) {
		this.agentlevel = agentlevel;
	}
	public String getAgentstate() {
		return agentstate;
	}
	public void setAgentstate(String agentstate) {
		this.agentstate = agentstate;
	}
	public String getAgentlaststate() {
		return agentlaststate;
	}
	public void setAgentlaststate(String agentlaststate) {
		this.agentlaststate = agentlaststate;
	}
	public int getIfrevcallin() {
		return ifrevcallin;
	}
	public void setIfrevcallin(int ifrevcallin) {
		this.ifrevcallin = ifrevcallin;
	}
	public CtiClient(int exist, String guid, String agentid, String agentmd5pwd, String loginext, String agentrole,
			String agentext, int agentlevel, String agentstate, String agentlaststate, int ifrevcallin) {
		super();
		this.exist = exist;
		this.guid = guid;
		this.agentid = agentid;
		this.agentmd5pwd = agentmd5pwd;
		this.loginext = loginext;
		this.agentrole = agentrole;
		this.agentext = agentext;
		this.agentlevel = agentlevel;
		this.agentstate = agentstate;
		this.agentlaststate = agentlaststate;
		this.ifrevcallin = ifrevcallin;
	}
	public CtiClient(int exist, String loginext, String agentstate) {
		super();
		this.exist = exist;
		this.loginext = loginext;
		this.agentstate = agentstate;
	}
	public CtiClient(){
		super();
		this.exist = 0;
		this.guid = "";
		this.agentid = "";
		this.agentmd5pwd = "";
		this.loginext = "";
		this.agentrole = "";
		this.agentext = "";
		this.agentlevel = 0;
		this.agentstate = "";
		this.agentlaststate = "";
		this.ifrevcallin = 0;
	}
}
