package com.hunk.DUT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.hunk.DAO.JDBCMysql;

public class DoObbase<T>{
	protected void getcontents(ResultSet res,ArrayList<T> contents,String cdt) throws Exception{
		
	}
	public String selectdb(ArrayList<T> contents,String sql,String cdt){
		String errormsg = "";
		Connection con;
		try {
			con = new JDBCMysql().getconnect();
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
	
	public String updatedb(ArrayList<String> sqls){
		String errormsg = "";
		Connection con;
		try {
			con = new JDBCMysql().getconnect();
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
				stmt.addBatch(sqls.get(i));
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
	
	public String deletedb(String sql,String[] ids,ArrayList<String> sqls){
		String errormsg = "";
		Connection con;
		try {
			con = new JDBCMysql().getconnect();
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
			if(sqls!=null){
				for(int i=0;i < sqls.size(); i++){
					stmt.addBatch(sqls.get(i));
				}
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
