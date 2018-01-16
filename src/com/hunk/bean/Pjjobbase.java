package com.hunk.bean;

public class Pjjobbase{
	private String autoid;
	private String pjclassid;
	private String pjid;
	private String cmdtype;
	private String callpool;
	private String executed;
	private String lastnumid;
	private String starttime;
	private String lastpausetime;
	private String endtime;
	public String getAutoid() {
		return autoid;
	}
	public void setAutoid(String autoid) {
		this.autoid = autoid;
	}
	public String getPjclassid() {
		return pjclassid;
	}
	public void setPjclassid(String pjclassid) {
		this.pjclassid = pjclassid;
	}
	public String getPjid() {
		return pjid;
	}
	public void setPjid(String pjid) {
		this.pjid = pjid;
	}
	public String getCmdtype() {
		return cmdtype;
	}
	public void setCmdtype(String cmdtype) {
		this.cmdtype = cmdtype;
	}
	public String getCallpool() {
		return callpool;
	}
	public void setCallpool(String callpool) {
		this.callpool = callpool;
	}
	public String getExecuted() {
		return executed;
	}
	public void setExecuted(String executed) {
		this.executed = executed;
	}
	public String getLastnumid() {
		return lastnumid;
	}
	public void setLastnumid(String lastnumid) {
		this.lastnumid = lastnumid;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getLastpausetime() {
		return lastpausetime;
	}
	public void setLastpausetime(String lastpausetime) {
		this.lastpausetime = lastpausetime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public Pjjobbase(String autoid, String pjclassid, String pjid, String cmdtype, String callpool, String executed,
			String lastnumid, String starttime, String lastpausetime, String endtime) {
		super();
		this.autoid = autoid;
		this.pjclassid = pjclassid;
		this.pjid = pjid;
		this.cmdtype = cmdtype;
		this.callpool = callpool;
		this.executed = executed;
		this.lastnumid = lastnumid;
		this.starttime = starttime;
		this.lastpausetime = lastpausetime;
		this.endtime = endtime;
	}
	public Pjjobbase() {
		super();
		this.autoid = "";
		this.pjclassid = "";
		this.pjid = "";
		this.cmdtype = "";
		this.callpool = "";
		this.executed = "";
		this.lastnumid = "";
		this.starttime = "";
		this.lastpausetime = "";
		this.endtime = "";
	}
}
