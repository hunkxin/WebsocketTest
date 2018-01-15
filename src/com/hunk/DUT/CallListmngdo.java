package com.hunk.DUT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.hunk.bean.CallList;
import com.hunk.bean.Projectbase;

public class CallListmngdo extends DoObbase<CallList>{

	public String rowsselsql(String autoid, String retry, int CmdType, int listnum, String lastnumid) {
		String sqlcdt = "";
		String sqlcmd = "";
		switch (CmdType) {
		case CTIEnum.OBCMDTYPE_RESTART:
			sqlcmd = "select * from `fs_ob_pjnumlist` where `pjid`="+autoid
					+" and `isinjob` = 1"
					+" and `retry` = "+retry
					+" order by `autoid`";
			break;
		default:
			sqlcmd = "select * from `fs_ob_pjnumlist` where `pjid`="+autoid
			+" and `isinjob` = 1"
			+" and `retry` > 0"
			+" and `autoid` > "+lastnumid
			+" order by `autoid`";
			break;
		}
		sqlcmd += sqlcdt;
		sqlcmd += " limit "+listnum;
		System.out.println(sqlcmd);
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<CallList> contents,String cdt) throws SQLException{
		while(res.next())
		{
			CallList content = new CallList(
					res.getString("autoid"),
					res.getString("telnum"),
					res.getString("trycount"),
					res.getString("retry"));
			contents.add(content);
		}
	}
	
	public ArrayList<String> getpauseupdatesql(String cdt, String lastnumid){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcmd = "update `fs_ob_pjjob` set "
						+"`lastnumid` = '"+lastnumid+"',"
						+"`lastpausetime` = '"+(new Date()).getTime()/1000+"'";
		String sqlcdt = " where `autoid` = (select MAX(`autoid`) from `fs_ob_pjjob` where `pjid` = "+cdt+")";
		sqlcmd += sqlcdt;
		sqls.add(sqlcmd);
		System.out.println(sqlcmd);
		return sqls;
	}
		
	public ArrayList<String> getretryupdatesql(ArrayList<CallList> contents){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcmd = "update `fs_ob_pjnumlist` set "
		+"`retry` = (`retry`-1) where ";
		for(int i=0;i<contents.size();i++){
			if(i>0)
				sqlcmd += " or ";
			String sqlcdt = "`autoid` = "+contents.get(i).getAutoid();
			sqlcmd += sqlcdt;
		}
		sqls.add(sqlcmd);
		System.out.println(sqlcmd);
		return sqls;
	}
	
	public ArrayList<String> getupdatesql(int cmdtype,Projectbase content){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcdt = "";
		String sqlcmd = "";
		switch (cmdtype) {
		case CTIEnum.OBCMDTYPE_RESTART:
			sqlcmd = "update `fs_ob_pjnumlist` set "
					//+"`trycount` = 0,"
					+"`retry` = "+content.getCall_retry()+","
					+"`isinjob` = 1,"
					+"`isanswer` = 0,"
					+"`isbridge` = 0";
			sqlcdt = " where `pjid` = "+content.getAutoid();
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			sqlcmd = "insert into `fs_ob_pjjob`("
					//+"`autoid`,"
					+"`pjclassid`,"
					+"`pjid`,"
					+"`callpool`,"
					//+"`executed`,"
					//+"`lastnumid`,"
					+"`starttime`,"
					//+"`lastpausetime`,"
					//+"`endtime`"
					+") values("
					//+"'"+content.getAutoid()+"',"
					+"'"+content.getPjclassid()+"',"
					+"'"+content.getPjid()+"',"
					+"'"+content.getCallpool()+"',"
					//+"'"+content.getExecuted()+"',"
					//+"'"+content.getLastnumid()+"',"
					+"'"+(new Date()).getTime()/1000+"',"
					//+"'"+content.getLastpausetime()+"',"
					//+"'"+content.getEndtime()+"'"
					+")";
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_START_REMAINED:
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`,"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getPjid()+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(`autoid`) from `fs_ob_pjnumlist` where `pjid` = "+content.getPjid()+" and `retry` = "+content.getCall_retry()+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"',"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0"
					+" where `pjid` = "+content.getPjid()+" and not `retry` = "+content.getCall_retry();
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_CUS_FAILED:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 0 and `isbridge` = 0";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`,"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getPjid()+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(`autoid`) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"',"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set "
					+"`retry` = "+content.getCall_retry();
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0"
					+" where `pjid` = "+content.getPjid()+" and not (`retry` < "+content.getCall_retry()+" and `isanswer` = 0 and `isbridge` = 0)";
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_AGT_FAILED:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 0";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`,"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getPjid()+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(`autoid`) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"',"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set "
					+"`retry` = "+content.getCall_retry();
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0"
					+" where `pjid` = "+content.getPjid()+" and not (`retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 0)";
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_CUS_SUCCESS:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 1";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`,"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getPjid()+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(`autoid`) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"',"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set "
					+"`retry` = "+content.getCall_retry();
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0"
					+" where `pjid` = "+content.getPjid()+" and not (`retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 1)";
			sqls.add(sqlcmd);
			break;
		default:
			break;
		}
		return sqls;
	}
}
