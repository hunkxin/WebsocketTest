package com.hunk.DUT;

import java.util.ArrayList;
import com.hunk.bean.CtiClient;
import com.hunk.bean.CtiExt;

public class Agentsop {
	
	protected Agentsdo op;
	
	public Agentsop(){
		this.op = new Agentsdo();
	}
	
	public int agentlogin(String id, String pwd, String ext, ArrayList<CtiClient> contents){
		String resmsg = "";
		int rescode;
		if(id==null||"".equals(id)){
			return CTIEnum.AGENTLOGINFAIL_ERR_AGENTID;
		}
		if(pwd==null||"".equals(pwd)){
			return CTIEnum.AGENTLOGINFAIL_ERR_PWD;
		}
		resmsg += op.selectdb(contents,op.rowselsql(pwd),"");
		if(resmsg.equals("")&&contents.size()>0){
			//System.out.println(mapper.writeValueAsString(contents));
			int count = contents.get(0).getExist();
			if(count < 1){
				rescode = CTIEnum.AGENTLOGINFAIL_ERR_AGENTID;
			}else{
				String p = contents.get(0).getAgentmd5pwd();
				if(p == null || !p.equals(pwd))
					rescode = CTIEnum.AGENTLOGINFAIL_ERR_PWD;
				else{
					if(ext==null||"".equals(ext))
						ext = id;
					Extsdo extop = new Extsdo();
					ArrayList<CtiExt> extcontents = new ArrayList<CtiExt>();
					resmsg += extop.selectdb(extcontents,extop.rowselsql(ext),"");
					if(resmsg.equals("")&&contents.size()>0){
						if(count < 1){
							rescode = CTIEnum.AGENTLOGIN_NO_LOGINEXT;
						}else{
							String agentname = extcontents.get(0).getAgentname();
							if(agentname == null || "".equals(agentname)){
								rescode = CTIEnum.AGENTLOGIN_NO_LOGINEXT;
							}else{
								String status = extcontents.get(0).getStatus();
								if(!CTIEnum.available.equals(status)){
									resmsg += op.updatedb(op.getupdatesql(ext, CTIEnum.available, CTIEnum.Waiting, agentname));
									if(resmsg.equals("")){
										contents.get(0).setAgentlaststate(status);
										rescode = CTIEnum.AGENTLOGINOK;
									}else{
										rescode = CTIEnum.PBXINNERERR;
									}
								}else{
									rescode = CTIEnum.AGENTLOGINOK;
								}
							}
						}
					}else
						rescode = CTIEnum.PBXINNERERR;
				}	
			}
		}else{
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
	
	public int agentaction(String name, String action, ArrayList<CtiClient> contents){
		String resmsg = "";
		int rescode;
		if(name==null||"".equals(name)){
			return CTIEnum.AGENTLOGINFAIL_ERR_AGENTID;
		}
		if(action==null||(!CTIEnum.logout.equals(action)&&!CTIEnum.onbreak.equals(action)&&!CTIEnum.available.equals(action))){
			return CTIEnum.PBX_ISNOT_HAVE_THISFUN;
		}
		resmsg += op.selectdb(contents,op.statusselsql(name),"");
		if(resmsg.equals("")&&contents.size()>0){
			String status = contents.get(0).getAgentstate();
			if(CTIEnum.unknown.equals(status)||CTIEnum.logout.equals(status)){
				rescode = CTIEnum.AGENT_CANNOT_BE_ACTIONED_NOLOGIN;
			}else{
				if(CTIEnum.logout.equals(action)){
					resmsg += op.updatedb(op.getupdatesql(name, CTIEnum.logout, CTIEnum.Waiting));
					if(resmsg.equals("")){
						contents.get(0).setAgentlaststate(status);
						rescode = CTIEnum.AGENTLOGOFF_OK;
					}else{
						rescode = CTIEnum.PBXINNERERR;
					}
				}else if(CTIEnum.onbreak.equals(action)){
					if(action.equals(status)){
						rescode = CTIEnum.AGENT_CANNOT_BE_MAKEBUSY_ISBUSY;
					}else{
						resmsg += op.updatedb(op.getupdatesql(name, CTIEnum.onbreak, CTIEnum.Waiting));
						if(resmsg.equals("")){
							contents.get(0).setAgentlaststate(status);
							rescode = CTIEnum.AGENT_MAKEBUSY_OK;
						}else{
							rescode = CTIEnum.PBXINNERERR;
						}
					}
				}else{
					if(action.equals(status)){
						rescode = CTIEnum.AGENT_CANNOT_BE_MAKEIDLE_ISIDLE;
					}else{
						resmsg += op.updatedb(op.getupdatesql(name, CTIEnum.available, CTIEnum.Waiting));
						if(resmsg.equals("")){
							contents.get(0).setAgentlaststate(status);
							rescode = CTIEnum.AGENT_MAKEBUSY_OK;
						}else{
							rescode = CTIEnum.PBXINNERERR;
						}
					}
				}
			}
		}else{
			rescode = CTIEnum.PBXINNERERR;
		}
		return rescode;
	}
}
