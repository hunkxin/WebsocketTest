package com.hunk.DAO;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class JDBCMysql {
	private Connection con;
	private static void loaddriver(){
		//System.out.println("-------- MySQL "
        //+ "JDBC Connection Testing ------------");

		try {

			Class.forName("com.mysql.jdbc.Driver");
		
		} catch (Exception e) {
		
			System.out.println("Where is your Mysql JDBC Driver? "
		            + "Include in your library path!");
			e.printStackTrace();
		
		}
		
		//System.out.println("Mysql JDBC Driver Registered!");
	}
	
	public Connection getconnect(boolean issofia,boolean iscc) throws SQLException {
		
		JDBCMysql.loaddriver();
//	    Connection connection = null;
//		String dbip = "127.0.0.1";
		boolean ishome = true;
		String dbip;
		if(ishome){
			dbip = "192.168.99.178";
		}else
			dbip = "192.168.1.128";
	
		if(issofia){
			con = DriverManager.getConnection(
		   			"jdbc:mysql://"+dbip+":3306/sofia_reg_internal?useSSL=false&" +
                    "user=freeswitch&password=freeswitch");
		}else if(iscc){
			con = DriverManager.getConnection(
		   			"jdbc:mysql://"+dbip+":3306/callcenter?useSSL=false&" +
                    "user=freeswitch&password=freeswitch");
		}else
			con = DriverManager.getConnection(
	   			"jdbc:mysql://"+dbip+":3306/FSWccc?useUnicode=true&characterEncoding=utf-8&useSSL=false&" +
                "user=freeswitch&password=freeswitch");

        //System.out.println("database connected!");
		return con;
	}
	
	public Connection getconnect() throws SQLException {
		
		JDBCMysql.loaddriver();
//	    Connection connection = null;
//		String dbip = "127.0.0.1";
		boolean ishome = true;
		String dbip;
		if(ishome){
			dbip = "192.168.99.178";
		}else
			dbip = "192.168.1.78";
	
		con = DriverManager.getConnection(
   			"jdbc:mysql://"+dbip+":3306/FSWcccob?useUnicode=true&characterEncoding=utf-8&useSSL=false&" +
            "user=freeswitch&password=freeswitch");

        //System.out.println("database connected!");
		return con;
	}
}
