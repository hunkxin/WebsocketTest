package com.hunk.DUT;

import java.util.ArrayList;

import com.hunk.bean.CallList;
import com.hunk.bean.Projectbase;

public class Projectmngop extends DoObbase<Projectbase>{

	protected Projectmngdo op;
	
	public Projectmngop(){
		this.op = new Projectmngdo();
	}
	
	public int GetProfile(String pjid, ArrayList<Projectbase> contents){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		resmsg += op.selectdb(contents,op.rowselsql(pjid),"");
		//System.out.println("1:"+resmsg);
		if(resmsg.equals("")&&contents.size()>0){//设置该项目的号码retry数为项目设定值，并获取开始时间和暂停时间
			CallListmngdo clop = new CallListmngdo();
			resmsg += clop.updatedb(clop.getupdatesql(contents.get(0).getCall_retry(), pjid));
			if(!resmsg.equals(""))
				rescode = CTIEnum.PBXINNERERR;
		}else{
			rescode = CTIEnum.OBCALLPJ_ISNOT_EXIST;
			if(!resmsg.equals(""))
				rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int GetCallList(int listnum, String pjid, int CmdType, ArrayList<CallList> contents){
		String resmsg = "";
		int rescode = 0;
		CallListmngdo clop = new CallListmngdo();
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		resmsg += clop.selectdb(contents, clop.rowselsql(pjid, CmdType), "");
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")&&contents.size()>0){
			
		}else{
			rescode = CTIEnum.OBCALLPJ_ISNOT_EXIST;
			if(!resmsg.equals(""))
				rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int GetAgentCount(String queue, ArrayList<String> contents){
		String resmsg = "";
		int rescode = 0;
		Queuedo qdo = new Queuedo();
		if(queue==null||"".equals(queue)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		resmsg += qdo.selectdb(contents, qdo.queuequerysql(queue), "");
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")&&contents.size()>0){
			
		}else{
			rescode = CTIEnum.OBCALLPJ_ISNOT_EXIST;
			if(!resmsg.equals(""))
				rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
}
