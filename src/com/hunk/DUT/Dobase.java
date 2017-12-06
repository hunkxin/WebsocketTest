package com.hunk.DUT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.hunk.DAO.JDBCMysql;
import com.hunk.bean.Cbbean;

public class Dobase<T>{
	protected void getcontents(ResultSet res,ArrayList<T> contents,String cdt) throws Exception{
		
	}
	public String selectdb(ArrayList<T> contents,String sql,String cdt){
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
//			while(res.next())
//			{
//				T content = getcontent(res,contents,cdt);
//				contents.add(content);
//			}
		} catch (Exception e) {
			//e.printStackTrace();
			errormsg += UT.errormsg(e);
			contents = null;
		}finally{
			errormsg = errormsg + (UT.sfcloseDb(res) + UT.sfcloseDb(stmt) + UT.sfcloseDb(con));
		}
		
		return errormsg;
	}
	
	public String updatedb(String sql){
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
			stmt = con.createStatement();
			stmt.executeUpdate(sql);		
		} catch (SQLException e) {
			//e.printStackTrace();
			errormsg += UT.errormsg(e);
		}finally{
			errormsg = errormsg + (UT.sfcloseDb(stmt) + UT.sfcloseDb(con));
		}
		return errormsg;
	}
	
	public String deletedb(String sql,String[] ids){
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
			for(int i=0;i < (ids.length); i++){
				String sqlcmd = sql+ids[i];
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
	
	public String selectcb(ArrayList<Cbbean> contents,String combox,String groupid) {
		String errormsg = "";
		Connection con;
		try {
			con = new JDBCMysql().getconnect(false,false);
		} catch (SQLException e1) {
			//e1.printStackTrace();
			errormsg += UT.errormsg(e1);
			return errormsg;
		}
		String sqlcmd;
		Statement stmt = null;
		ResultSet res = null;
		try {
			stmt = con.createStatement();
			sqlcmd = "select `cntid`,`cntvalue` from `fs_pbxcomboxcnt` where `combox` = '"+combox+"' and `groupid` = "+groupid+" order by `index`";
			res = stmt.executeQuery(sqlcmd);
			while(res.next())
			{
				Cbbean content = new Cbbean(res.getString("cntid"),res.getString("cntvalue"));
				contents.add(content);
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			errormsg += UT.errormsg(e);
			contents = null;
		}finally{
			errormsg = errormsg + (UT.sfcloseDb(res) + UT.sfcloseDb(stmt) + UT.sfcloseDb(con));
		}
		return errormsg;
	}
}
