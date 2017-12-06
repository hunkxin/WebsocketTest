package com.hunk.bean;

public class CtiServer {
	private String pbxid;
	private String pbxhost;
	private String pbxuser;
	private String pbxpwd;
	public String getPbxid() {
		return pbxid;
	}
	public void setPbxid(String pbxid) {
		this.pbxid = pbxid;
	}
	public String getPbxhost() {
		return pbxhost;
	}
	public void setPbxhost(String pbxhost) {
		this.pbxhost = pbxhost;
	}
	public String getPbxuser() {
		return pbxuser;
	}
	public void setPbxuser(String pbxuser) {
		this.pbxuser = pbxuser;
	}
	public String getPbxpwd() {
		return pbxpwd;
	}
	public void setPbxpwd(String pbxpwd) {
		this.pbxpwd = pbxpwd;
	}
	public CtiServer(String pbxid, String pbxhost, String pbxuser, String pbxpwd) {
		super();
		this.pbxid = pbxid;
		this.pbxhost = pbxhost;
		this.pbxuser = pbxuser;
		this.pbxpwd = pbxpwd;
	}
	public CtiServer(){}
}
