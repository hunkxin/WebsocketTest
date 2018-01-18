package com.hunk.DUT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.hunk.bean.Pjjobbase;

public class Pjjobmngdo extends DoObbase<Pjjobbase>{

	public String rowselsql(String autoid) {
		String sqlcmd;
		sqlcmd = "select * from `fs_ob_pjjob` where `autoid`="+autoid;
		return sqlcmd;
	}
		
	public String lastrowselsql(String autoid) {
		String sqlcmd;
		sqlcmd = "select * from `fs_ob_pjjob` where "
		+"`endtime` = 0 and "
		+"`autoid`= (select a.autoid from (select MAX(`autoid`) as `autoid` from `fs_ob_pjjob` where `pjid` = "+autoid+") as a)";
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<Pjjobbase> contents,String cdt) throws SQLException{
		while(res.next())
		{
			Pjjobbase content = new Pjjobbase(
					res.getString("autoid"),
					res.getString("pjclassid"),
					res.getString("pjid"),
					res.getString("cmdtype"),
					res.getString("callpool"),
					res.getString("executed"),
					res.getString("lastnumid"),
					res.getString("starttime"),
					res.getString("lastpausetime"),
					res.getString("endtime"));
			contents.add(content);
		}
	}
			
	public ArrayList<String> getendupdatesql(String cdt){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcmd = "update `fs_ob_pjjob` set "
				+"`endtime` = '"+(new Date()).getTime()/1000+"'";
		//String sqlcdt = " where `autoid` = (select a.autoid from (select MAX(`autoid`)  as `autoid` from `fs_ob_pjjob` where `pjid` = "+cdt+") as a)";
		String sqlcdt = " where `autoid`= "+cdt;
		sqlcmd += sqlcdt;
		sqls.add(sqlcmd);
		System.out.println(sqlcmd);
		return sqls;
	}
	
	public ArrayList<String> getpauseupdatesql(String cdt, String lastnumid){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcmd = "update `fs_ob_pjjob` set "
						+"`lastnumid` = '"+lastnumid+"',"
						+"`lastpausetime` = '"+(new Date()).getTime()/1000+"'";
		//String sqlcdt = " where `autoid` = (select a.autoid from (select MAX(`autoid`)  as `autoid` from `fs_ob_pjjob` where `pjid` = "+cdt+") as a)";
		String sqlcdt = " where `autoid`= "+cdt;
		sqlcmd += sqlcdt;
		sqls.add(sqlcmd);
		System.out.println(sqlcmd);
		return sqls;
	}
	
	public ArrayList<String> getexeupdatesql(String cdt, long executed){
		ArrayList<String> sqls = new ArrayList<>();
		String sqlcmd = "update `fs_ob_pjjob` set "
						+"`executed` = (`executed`+"+executed+")";
		//String sqlcdt = " where `autoid` = (select a.autoid from (select MAX(`autoid`)  as `autoid` from `fs_ob_pjjob` where `pjid` = "+cdt+") as a)";
		String sqlcdt = " where `autoid`= "+cdt;
		sqlcmd += sqlcdt;
		sqls.add(sqlcmd);
		System.out.println(sqlcmd);
		return sqls;
	}
}