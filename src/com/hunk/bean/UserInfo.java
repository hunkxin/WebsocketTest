package com.hunk.bean;

import java.util.List;

public class UserInfo {
	private String action;
	private String username; 
	private String userid; 
	private String time;
	private String onlineCount;
	private List<Username> onlineUsers;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getOnlineCount() {
		return onlineCount;
	}
	public void setOnlineCount(String onlineCount) {
		this.onlineCount = onlineCount;
	}
	public List<Username> getOnlineUsers() {
		return onlineUsers;
	}
	public void setOnlineUsers(List<Username> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}
	public UserInfo(String action, String username, String userid, String time, String onlineCount,
			List<Username> onlineUsers) {
		super();
		this.action = action;
		this.username = username;
		this.userid = userid;
		this.time = time;
		this.onlineCount = onlineCount;
		this.onlineUsers = onlineUsers;
	}
	public UserInfo(){}
}
