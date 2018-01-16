package com.hunk.DUT;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.hunk.bean.Projectbase;

public class Projectmngdo extends DoObbase<Projectbase>{

	public String rowselsql(String autoid) {
		String sqlcmd;
		sqlcmd = "select * from `fs_ob_pj` where `autoid`="+autoid;
		return sqlcmd;
	}
	
	public String lastnumselsql(String autoid) {
		String sqlcmd;
		sqlcmd = "select `lastnumid` from `fs_ob_pjjob` where `autoid`= (select MAX(`autoid`) from `fs_ob_pjjob` where `pjid` = "+autoid+")";
		return sqlcmd;
	}
		
	protected void getcontents(ResultSet res,ArrayList<Projectbase> contents,String cdt) throws SQLException{
		while(res.next())
		{
			Projectbase content = new Projectbase(
					res.getString("autoid"),
					res.getString("pjid"),
					res.getString("pjclassid"),
					res.getString("pjname"),
					res.getString("trunkid"),
					res.getString("caller_id_number"),
					res.getString("ifrecord"),
					res.getString("concurrencyrate"),
					res.getString("callinterval"),
					res.getString("obtype"),
					res.getString("queueno"),
					res.getString("ivrid"),
					res.getString("extid"),
					res.getString("ringbackid"),
					res.getString("pn_imptime"),
					res.getString("callpool"),
					res.getString("telbookpath"),
					res.getString("blacklist"),
					res.getString("iffilter"),
					res.getString("starttime"),
					res.getString("pausetime"),
					res.getString("lastpn_imptime"),
					res.getString("lastcallpool"),
					res.getString("laststarttime"),
					res.getString("lastpausetime"),
					res.getString("lastcpremain"),
					res.getString("exestate"),
					res.getString("call_overtime"),
					res.getString("call_retry"),
					res.getString("creator"),
					res.getString("lastmodifyer"),
					res.getString("creatortime"),
					res.getString("modifytime"),
					res.getString("iflock"),
					res.getString("owner"),
					res.getString("pbxid"));
			contents.add(content);
		}
	}
			
	public String getupdatesql(Projectbase content,String cdt){
		String sqlcdt = "";
		String sqlcmd = "update `fs_ob_pj` set "
				+"`starttime` = "+UT.NSinsert(content.getStarttime(), "0")+","
				+"`pausetime` = "+UT.NSinsert(content.getPausetime(), "0")+","
				+"`lastpn_imptime` = "+content.getLastpn_imptime()+","
				+"`lastcallpool` = "+content.getLastcallpool()+","
				+"`laststarttime` = "+content.getLaststarttime()+","
				+"`lastpausetime` = "+content.getLastpausetime()+","
				+"`lastcpremain` = "+content.getLastcpremain()+","
				+"`exestate` = "+content.getExestate();
		sqlcdt = " where `autoid` = "+cdt;
		sqlcmd += sqlcdt;
		return sqlcmd;
	}
}
