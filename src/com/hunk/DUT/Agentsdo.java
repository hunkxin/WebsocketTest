package com.hunk.DUT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.hunk.DAO.JDBCMysql;
import com.hunk.bean.CtiClient;

public class Agentsdo extends Dobase<CtiClient> {
	
	public String rowselsql(String id) {
		String sqlcmd = "select count(`agentid`) as `exist`, `guid`, `agentid`, `agentmd5pwd`, `loginext`, `agentrole`, `agentext`, `agentlevel`, "
				+ "`agentstate`, `ifrevcallin` from `fs_pbxnode_ctiagent` where `agentid`='"+id+"'";
		//System.out.println(sqlcmd);
		return sqlcmd;
	}
	
	public String statusselsql(String name){
		String sqlcmd = "select count(`name`) as `exist`, `name` as `agentid`, `status` as `agentstate`, `last_status_change` as `laststatuschange` from `agents` where `name`='"+name+"'";
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<CtiClient> contents,String cdt) throws SQLException{
		if("".equals(cdt)){
			while(res.next())
			{
				CtiClient content = new CtiClient(
						res.getInt("exist"),
						res.getString("guid"),
						res.getString("agentid"),
						res.getString("agentmd5pwd"),
						res.getString("loginext"),
						res.getString("agentrole"),
						res.getString("agentext"),
						res.getInt("agentlevel"),
						res.getString("agentstate"),
						"",
						res.getInt("ifrevcallin"));
				contents.add(content);
			}
		}else{
			while(res.next())
			{
				CtiClient content = new CtiClient(
						res.getInt("exist"),
						res.getString("agentid"),
						res.getString("agentstate"),
						res.getString("laststatuschange"));
				contents.add(content);
			}
		}
	}
	
	public String getupdatesql(String agentname, String status, String state) {
		String sqlcmd = "update `agents` set "
				+"`status` =  '"+status+"',"
				+"`state` =  '"+state+"' "
				+" where `name` = '"+agentname+"'";
		return sqlcmd;
	}
	
	public ArrayList<String> getupdatesql(String agentid, String status, String state, String agentname, int laststatus, String laststatuschange, int agentstatus, int cdrtype) {
		ArrayList<String> sqls = new ArrayList<String>();
		String sqlcmd = "update `fs_pbxnode_ctiagent` set `loginext` = '"+agentname+"', `agentstate` = '"+agentstatus+"' where `agentid` = '"+agentid+"'";
		sqls.add(sqlcmd);
		sqlcmd = "update `agents` set "
				+"`status` =  '"+status+"',"
				+"`state` =  '"+state+"',"
				+"`last_status_change` =  '"+(new Date()).getTime()/1000+"'"
				+" where `name` = '"+agentname+"'";
		sqls.add(sqlcmd);
		//将坐席状态记录到cdr中
		sqlcmd = "insert into fswcdr.fs_agents_cdr ("
				+"`agentid`,"
				+"`loginext`,"
				+"`cdr_type`,"
				+"`last_status`,"
				+"`status`,"
				+"`status_change`,"
				+"`last_status_change`"
				+") values("
				//+content.getAutoid()+","
				+"'"+agentid+"',"
				+"'"+agentname+"',"
				+"'"+cdrtype+"',"
				+"'"+laststatus+"',"
				+"'"+agentstatus+"',"
				+"'"+(new Date()).getTime()/1000+"',"
				+"'"+laststatuschange+"'"
				+")";
		sqls.add(sqlcmd);
		return sqls;
	}
	
	public String updatedb(ArrayList<String> sqls){
		String errormsg = "";
		Connection con;
		try {
			con = new JDBCMysql().getconnect(false,false);
		} catch (SQLException e1) {
			//e1.printStackTrace();
			errormsg += UT.errormsg(e1);
			return errormsg;
		}
		Statement stmt = null;
		try {
			con.setAutoCommit(false);
			stmt = con.createStatement();
			for(int i=0;i < (sqls.size()); i++){
				String sqlcmd = sqls.get(i);
				stmt.addBatch(sqlcmd);
				//System.out.println(sqlcmd);
			}
			stmt.executeBatch();
			con.commit();	
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				errormsg += UT.errormsg(e1);
			}
			//e.printStackTrace();
			errormsg += UT.errormsg(e);
		}finally{
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				errormsg += UT.errormsg(e);
			}
			errormsg = errormsg + (UT.sfcloseDb(stmt) + UT.sfcloseDb(con));
		}
		return errormsg;
	}
}