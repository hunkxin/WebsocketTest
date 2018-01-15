package com.hunk.bean;

public class Pjjobbase{
	private String autoid;
	private String pjclassid;
	private String pjid;
	private String callpool;
	private String executed;
	private String lastnumid;
	private String starttime;
	private String lastpausetime;
	private String endtime;
	public void setAutoid(String autoid){
		this.autoid=autoid;
	}
	public String getAutoid(){
		return autoid;
	}
	public void setPjclassid(String pjclassid){
		this.pjclassid=pjclassid;
	}
	public String getPjclassid(){
		return pjclassid;
	}
	public void setPjid(String pjid){
		this.pjid=pjid;
	}
	public String getPjid(){
		return pjid;
	}
	public void setCallpool(String callpool){
		this.callpool=callpool;
	}
	public String getCallpool(){
		return callpool;
	}
	public void setExecuted(String executed){
		this.executed=executed;
	}
	public String getExecuted(){
		return executed;
	}
	public void setLastnumid(String lastnumid){
		this.lastnumid=lastnumid;
	}
	public String getLastnumid(){
		return lastnumid;
	}
	public void setStarttime(String starttime){
		this.starttime=starttime;
	}
	public String getStarttime(){
		return starttime;
	}
	public void setLastpausetime(String lastpausetime){
		this.lastpausetime=lastpausetime;
	}
	public String getLastpausetime(){
		return lastpausetime;
	}
	public void setEndtime(String endtime){
		this.endtime=endtime;
	}
	public String getEndtime(){
		return endtime;
	}
	public Pjjobbase(String autoid, String pjclassid, String pjid, String callpool, String executed, String lastnumid,
			String starttime, String lastpausetime, String endtime) {
		super();
		this.autoid = autoid;
		this.pjclassid = pjclassid;
		this.pjid = pjid;
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
		this.callpool = "";
		this.executed = "";
		this.lastnumid = "";
		this.starttime = "";
		this.lastpausetime = "";
		this.endtime = "";
	}
}
