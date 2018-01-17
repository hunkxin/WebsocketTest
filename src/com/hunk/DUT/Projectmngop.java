package com.hunk.DUT;

import java.util.ArrayList;

import com.hunk.bean.CallList;
import com.hunk.bean.Pjjobbase;
import com.hunk.bean.Projectbase;

public class Projectmngop extends DoObbase<Projectbase>{

	protected Projectmngdo op;
	
	public Projectmngop(){
		this.op = new Projectmngdo();
	}
	
	public int GetProfile(String pjid, int cmdtype, ArrayList<Projectbase> contents){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		resmsg += op.selectdb(contents,op.rowselsql(pjid),"");
		//System.out.println("1:"+resmsg);
		if(resmsg.equals("")&&contents.size()>0){
			CallListmngdo clop = new CallListmngdo();
			//如果为重新开始的任务，则将符合条件的号码的retry重置为项目设定值
			if(cmdtype!=CTIEnum.OBCMDTYPE_START){
				resmsg += clop.updatedb(clop.getupdatesql(cmdtype, contents.get(0)));
			}
			if(!resmsg.equals("")){
				System.out.println("GetProfile"+":"+resmsg);
				rescode = CTIEnum.PBXINNERERR;
			}
		}else{
			rescode = CTIEnum.OBCALLPJ_ISNOT_EXIST;
			if(!resmsg.equals("")){
				System.out.println("GetProfile"+":"+resmsg);
				rescode = CTIEnum.PBXINNERERR;
			}
		}
		return rescode;
	}
	
	public int GetLastpjjob(String pjid, ArrayList<Pjjobbase> contents){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		Pjjobmngdo pjjop = new Pjjobmngdo();
		resmsg += pjjop.selectdb(contents,pjjop.lastrowselsql(pjid),"");
		//System.out.println("1:"+resmsg);
		if(resmsg.equals("")&&contents.size()>0){
			
		}else{
			System.out.println("GetLastpjjob"+":"+resmsg);
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int GetCallList(String pjid, String retry, int CmdType, int listnum, String lastnumid, ArrayList<CallList> contents){
		String resmsg = "";
		int rescode = 0;
		CallListmngdo clop = new CallListmngdo();
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		resmsg += clop.selectdb(contents, clop.rowsselsql(pjid, retry, CmdType, listnum, lastnumid), "");
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")){
			if(contents.size()>0){
				
			}else{
				rescode = CTIEnum.OBCALLPJ_ISALREADY_END;
			}
		}else{
			System.out.println("GetCallList"+":"+resmsg);
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
			rescode = CTIEnum.OBCALLPJ_NOIDLE_AGENT;
			if(!resmsg.equals("")){
				System.out.println("GetAgentCount"+":"+resmsg);
				rescode = CTIEnum.PBXINNERERR;
			}
		}
		return rescode;
	}
	
	public int UpdateCallList(ArrayList<CallList> contents){
		String resmsg = "";
		int rescode = 0;
		CallListmngdo clop = new CallListmngdo();
		resmsg += clop.updatedb(clop.getretryupdatesql(contents));
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")){
			
		}else{
			System.out.println("UpdateCallList"+":"+resmsg);
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int UpdatePauseinfo(String pjid, CallList lastnum){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		Pjjobmngdo pjjop = new Pjjobmngdo();
		resmsg += pjjop.updatedb(pjjop.getpauseupdatesql(pjid, lastnum==null?"0":lastnum.getAutoid()));
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")){
			
		}else{
			System.out.println("Updatepauseinfo"+":"+resmsg);
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int UpdateEndinfo(String pjid){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		Pjjobmngdo pjjop = new Pjjobmngdo();
		resmsg += pjjop.updatedb(pjjop.getendupdatesql(pjid));
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")){
			
		}else{
			System.out.println("Updatepauseinfo"+":"+resmsg);
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int UpdateExeinfo(String pjid, long executed){
		String resmsg = "";
		int rescode = 0;
		if(pjid==null||"".equals(pjid)){
			return CTIEnum.OBCALLPJ_ISNOT_EXIST;
		}
		Pjjobmngdo pjjop = new Pjjobmngdo();
		resmsg += pjjop.updatedb(pjjop.getexeupdatesql(pjid, executed));
		//System.out.println("21:"+resmsg);
		if(resmsg.equals("")){
			
		}else{
			System.out.println("Updatepauseinfo"+":"+resmsg);
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
}