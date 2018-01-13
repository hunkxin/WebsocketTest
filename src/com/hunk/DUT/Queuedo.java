package com.hunk.DUT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.hunk.DAO.JDBCMysql;

public class Queuedo {

	public String queuequerysql(String queue) {
		String sqlcmd;
		sqlcmd = "select COUNT(`autoid`) as `count` from `agents` where `status` = 'Available' and `state`= 'Waiting' and  `name` in (select `name` from `tiers` where `queue`='"+queue+"')";
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<String> contents,String cdt) throws SQLException{
		while(res.next())
		{
			contents.add(res.getString("count"));
		}
	}
	
	public String selectdb(ArrayList<String> contents,String sql,String cdt){
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
		ResultSet res = null;
		//System.out.println(sql);
		try {
			stmt = con.createStatement();
			res = stmt.executeQuery(sql);
			getcontents(res,contents,cdt);
		} catch (Exception e) {
			//e.printStackTrace();
			errormsg += UT.errormsg(e);
			contents = null;
		}finally{
			errormsg = errormsg + (UT.sfcloseDb(res) + UT.sfcloseDb(stmt) + UT.sfcloseDb(con));
		}
		
		return errormsg;
	}
}
