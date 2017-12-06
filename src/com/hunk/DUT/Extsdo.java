package com.hunk.DUT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.hunk.bean.CtiExt;

public class Extsdo extends Dobase<CtiExt> {
	
	public String rowselsql(String name) {
		String sqlcmd = "select count(sc.name) as `exist`,sc.name,s.contact,c.name as `agentname`,c.status "
				+ "from `fs_pbxext` as sc left join `sip_registrations` as s on s.sip_username = sc.name "
				+ "left join `agents` as c on c.name = sc.name where sc.name = '"+ name +"'";
		return sqlcmd;
	}
			
	protected void getcontents(ResultSet res,ArrayList<CtiExt> contents,String cdt) throws SQLException{
		while(res.next())
		{
			String contact = res.getString("contact");
			int isreg = 0;
			if(contact!=null&&!contact.equals("")){
				isreg = 1;
			}
			CtiExt content = new CtiExt(
								res.getInt("exist"),
								res.getString("name"),
								isreg,
								res.getString("agentname"),
								res.getString("status"));
			contents.add(content);
		}
	}
}
