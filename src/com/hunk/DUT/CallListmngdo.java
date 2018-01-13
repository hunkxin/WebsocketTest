package com.hunk.DUT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.hunk.bean.CallList;

public class CallListmngdo extends DoObbase<CallList>{

	public String rowselsql(String autoid, int CmdType) {
		String sqlcmd;
		sqlcmd = "select * from `fs_ob_pjnumlist` where `pjid`="+autoid;
		switch (CmdType) {
		case 1:
			
			break;
		case 2:
			
			break;
		case 3:
			
			break;
		case 4:
			
			break;
		case 5:
			
			break;
		case 6:
			
			break;
		case 7:
			
			break;
		case 8:
			
			break;
		default:
			break;
		}
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<CallList> contents,String cdt) throws SQLException{
		while(res.next())
		{
			CallList content = new CallList(
					res.getString("telnum"),
					res.getString("retry"));
			contents.add(content);
		}
	}
	
	public String getupdatesql(String retry,String cdt){
		String sqlcdt = "";
		String sqlcmd = "update `fs_ob_pjnumlist` set "
				+"`retry` = "+UT.NSinsert(retry, "0");
		sqlcdt = " where `pjid` = "+cdt;
		sqlcmd += sqlcdt;
		return sqlcmd;
	}
}
