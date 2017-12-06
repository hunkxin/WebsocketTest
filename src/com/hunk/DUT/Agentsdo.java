package com.hunk.DUT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.hunk.DAO.JDBCMysql;
import com.hunk.bean.CtiClient;

public class Agentsdo extends Dobase<CtiClient> {
	
	public String rowselsql(String id) {
		String sqlcmd = "select count(`agentid`) as `exist`, `guid`, `agentid`, `agentmd5pwd`, `loginext`, `agentrole`, `agentext`, `agentlevel`, "
				+ "`agentstate`, `ifrevcallin` from `fs_pbxnode_ctiagent` where `agentid`="+id;
		return sqlcmd;
	}
	
	public String statusselsql(String name){
		String sqlcmd = "select count(`agentid`) as `exist`, `name` as `agentid`, `status` as `agentstate` from `agents` where `name`="+name;
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<CtiClient> contents,String cdt) throws SQLException{
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
	}
	
	public String getupdatesql(String agentname, String status, String state) {
		String sqlcmd = "update `agents` set "
				+"`status` =  '"+status+"',"
				+"`state` =  '"+state+"' "
				+" where `name` = '"+agentname+"'";
		return sqlcmd;
	}
	
	public ArrayList<String> getupdatesql(String ext, String status, String state, String agentname) {
		ArrayList<String> sqls = new ArrayList<String>();
		String sqlcmd = "update `fs_pbxnode_ctiagent` set `loginext` = '"+ext+"'";
		sqls.add(sqlcmd);
		sqlcmd = "update `agents` set "
				+"`status` =  '"+status+"',"
				+"`state` =  '"+state+"' "
				+" where `name` = '"+agentname+"'";
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
