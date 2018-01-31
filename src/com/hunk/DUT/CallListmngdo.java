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
		sqlcmd = "select * from `fs_ob_pjnumlist` where `pjid`="+autoid
		+" and `isinjob` = 1"
		+" and `retry` > 0"
		+" and `autoid` > "+lastnumid
		+" order by `autoid`";
		sqlcmd += sqlcdt;
		sqlcmd += " limit "+listnum;
		//System.out.println(sqlcmd);
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
		//System.out.println(sqlcmd);
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
					+"`cmdtype`,"
					+"`callpool`,"
					//+"`executed`,"
					//+"`lastnumid`,"
					+"`starttime`"
					//+"`lastpausetime`,"
					//+"`endtime`"
					+") values("
					//+"'"+content.getAutoid()+"',"
					+"'"+content.getPjclassid()+"',"
					+"'"+content.getAutoid()+"',"
					+"'"+cmdtype+"',"
					+"'"+content.getCallpool()+"',"
					//+"'"+content.getExecuted()+"',"
					//+"'"+content.getLastnumid()+"',"
					+"'"+(new Date()).getTime()/1000+"'"
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
			+"`cmdtype`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getAutoid()+"',"
			+"'"+cmdtype+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(*) from `fs_ob_pjnumlist` where `pjid` = "+content.getAutoid()+" and `retry` = "+content.getCall_retry()+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"'"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0"
					+" where `pjid` = "+content.getAutoid()+" and `autoid` not in (select * from (select `autoid` from `fs_ob_pjnumlist` where `retry` = "+content.getCall_retry()+") temp)";
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_CUS_FAILED:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 0 and `isbridge` = 0";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`cmdtype`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getAutoid()+"',"
			+"'"+cmdtype+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(*) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"'"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			//先将所有号码`isinjob`置0
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0 where `pjid` = "+content.getAutoid();
			sqls.add(sqlcmd);
			//再将满足条件的号码置1
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 1, `retry` = "+content.getCall_retry()+", `isanswer` = 0, `isbridge` = 0";
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_AGT_FAILED:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 0";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`cmdtype`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getAutoid()+"',"
			+"'"+cmdtype+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(*) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"'"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0 where `pjid` = "+content.getAutoid();
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 1, `retry` = "+content.getCall_retry()+", `isanswer` = 0, `isbridge` = 0";
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			break;
		case CTIEnum.OBCMDTYPE_RE_CUS_SUCCESS:
			sqlcdt = " where `pjid` = "+content.getAutoid()+" and `retry` < "+content.getCall_retry()+" and `isanswer` = 1 and `isbridge` = 1";
			sqlcmd = "insert into `fs_ob_pjjob`("
			//+"`autoid`,"
			+"`pjclassid`,"
			+"`pjid`,"
			+"`cmdtype`,"
			+"`callpool`,"
			//+"`executed`,"
			//+"`lastnumid`,"
			+"`starttime`"
			//+"`lastpausetime`,"
			//+"`endtime`"
			+") values("
			//+"'"+content.getAutoid()+"',"
			+"'"+content.getPjclassid()+"',"
			+"'"+content.getAutoid()+"',"
			+"'"+cmdtype+"',"
			//+"'"+content.getCallpool()+"',"
			+"(select COUNT(*) from `fs_ob_pjnumlist`"+sqlcdt+"),"
			//+"'"+content.getExecuted()+"',"
			//+"'"+content.getLastnumid()+"',"
			+"'"+(new Date()).getTime()/1000+"'"
			//+"'"+content.getLastpausetime()+"',"
			//+"'"+content.getEndtime()+"'"
			+")";
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 0 where `pjid` = "+content.getAutoid();
			sqls.add(sqlcmd);
			sqlcmd = "update `fs_ob_pjnumlist` set `isinjob` = 1, `retry` = "+content.getCall_retry()+", `isanswer` = 0, `isbridge` = 0";
			sqlcmd += sqlcdt;
			sqls.add(sqlcmd);
			break;
		default:
			break;
		}
		return sqls;
	}
}