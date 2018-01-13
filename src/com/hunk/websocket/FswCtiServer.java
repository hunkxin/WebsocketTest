package com.hunk.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import com.hunk.DUT.Agentsdo;
import com.hunk.DUT.Agentsop;
import com.hunk.DUT.CTIEnum;
import com.hunk.DUT.Projectmngop;
import com.hunk.DUT.UT;
import com.hunk.bean.CallList;
import com.hunk.bean.CtiClient;
import com.hunk.bean.CtiServer;
import com.hunk.bean.Projectbase;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/FswCtiServer")
public class FswCtiServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    
    private static int onlineSvrCount = 0;
    
    private static int onlineAdmCount = 0;
    //private static int testCount = 0;
    
    //CopyOnWriteArrayList/Set是ArrayList/Set 的一个线程安全的变体，其中所有可变操作（add、set等等）都是通过对底层数组进行一次新的复制来实现的。
    //当迭代线程数大于修改线程数时，CopyOnWriteArrayList的性能优于同步的ArrayList，反之，同步的ArrayList优于CopyOnWriteArrayList。CopyOnWriteArraySet同理。
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<FswCtiServer> webSocketSet = new CopyOnWriteArraySet<FswCtiServer>();
    private static CopyOnWriteArraySet<FswCtiServer> webSocketSet_s = new CopyOnWriteArraySet<FswCtiServer>();
    private static CopyOnWriteArraySet<FswCtiServer> webSocketSet_a = new CopyOnWriteArraySet<FswCtiServer>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    //客户端对象
    private CtiClient client;
    
    //服务器对象
    private CtiServer server;
    
    //判断是否是服务器
    private boolean isServer;
    
  //判断是否是admin
    private boolean isAdmin;
    
  //判断是否已连接上cti
    private boolean isConnected;
    
    //连接开始时间
    private Date starttime;
        
    public Session getSession() {
		return session;
	}

	public CtiClient getClient() {
		return client;
	}

	public CtiServer getServer() {
		return server;
	}

	private final class CmdExeThread implements Runnable{
		private boolean isPause;
		private boolean isEnd;
		private String pjid;
		private int cmdtype;
		private Projectbase pjbase;
		private int interal;
//		private long starttime;
//		private long pausetime;
		public boolean isPause() {
			return isPause;
		}
		public void setPause(boolean isPause) {
			this.isPause = isPause;
		}
		public boolean isEnd() {
			return isEnd;
		}
		public void setEnd(boolean isEnd) {
			this.isEnd = isEnd;
		}
		public String getPjid() {
			return pjid;
		}
		public void setPjid(String pjid) {
			this.pjid = pjid;
		}
		public int getCmdtype() {
			return cmdtype;
		}
		public void setCmdtype(int cmdtype) {
			this.cmdtype = cmdtype;
		}
		public CmdExeThread(String pjid, int cmdtype) {
			super();
			this.isPause = false;
			this.isEnd = false;
			this.pjid = pjid;
			this.cmdtype = cmdtype;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				if(isEnd)
					break;
				if(isPause){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO: handle exception
					}
				}else{
					if(this.pjbase == null){//为空说明是重新开始的线程
						int rescode = 200;
						ArrayList<Projectbase> pjcontents = new ArrayList<Projectbase>();
						ArrayList<CallList> calllist = new ArrayList<CallList>();
						ArrayList<String> ALagentcount = new ArrayList<String>();
						int listnum = 1;
						Projectmngop op = new Projectmngop();
						rescode = op.GetProfile(pjid, pjcontents);
						if(rescode!=0){//发送错误消息
							
							break;
						}
						this.pjbase = pjcontents.get(0);
						try {
							this.interal = Integer.parseInt(pjbase.getCallinterval());
						} catch (NumberFormatException e) {
							// TODO: handle exception
							this.interal = 0;
						}
//						try {
//							this.starttime = Long.parseLong(pjbase.getStarttime());
//						} catch (NumberFormatException e) {
//							// TODO: handle exception
//							this.starttime = 0;
//						}
//						try {
//							this.pausetime = Long.parseLong(pjbase.getPausetime());
//						} catch (NumberFormatException e) {
//							// TODO: handle exception
//							this.pausetime = 0;
//						}
//						if(UT.isPastT(starttime)){//启动项目
//							
//						}else//
//							break;
						rescode = op.GetAgentCount(this.pjbase.getQueueno(), ALagentcount);
						if(rescode==0){
							try {
								listnum = (Integer.parseInt(ALagentcount.get(0)))*(Integer.parseInt(this.pjbase.getConcurrencyrate()))/100;
							} catch (NumberFormatException e) {
								// TODO: handle exception
								listnum = 1;
							}
						}
						if(listnum<1) listnum = 1;
						rescode = op.GetCallList(listnum, pjid, this.cmdtype, calllist);
					}else{//继续执行线程
						
					}
				}
			}
		}
	}
	
	private static int CmdExeThreadsCount = 0;
	private static CopyOnWriteArraySet<CmdExeThread> cmdExeThreads = new CopyOnWriteArraySet<CmdExeThread>();

	/**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
    	this.starttime = new Date();
        this.session = session;
        String usertype = session.getRequestParameterMap().get("type").get(0);
        if("server".equals(usertype)){
        	webSocketSet_s.add(this);
        	addOnlineSvrCount();
        	this.isServer = true;
        	this.server = new CtiServer();
        	//System.out.println("有新连接加入！当前在线节点数为" + getOnlineSvrCount());
        }else if("client".equals(usertype)){
        	//testSocketSet.add(this);
        	webSocketSet.add(this);     //加入set中
            addOnlineCount();           //在线数加1
            //addTestCount();
            this.isServer = false;
            this.isAdmin = false;
            this.client = new CtiClient();
            //System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
            //System.out.println("client对象总数："+getTestCount());
//            int i = 0;
//            for(xnWebSocket_M item: testSocketSet){
//            	System.out.println(i+":");
//            	if(item==null){
//            		System.out.println("null");
//            	}else
//            		System.out.println(item.toString());
//            	i++;
//            }
        }else if("admin".equals(usertype)){
        	webSocketSet_a.add(this);     //加入set中
        	addOnlineAdmCount();           //在线数加1
            //addTestCount();
            this.isServer = false;
            this.isAdmin = true;
            this.client = new CtiClient();
        }
        //System.out.println(usertype);
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
    	//System.out.println("closed!");
    	if(this.isServer == false){
    		if(this.isAdmin==true){
    			webSocketSet_a.remove(this);
        		subOnlineAdmCount();  
    		}else{
    			webSocketSet.remove(this);  //从set中删除
                subOnlineCount();           //在线数减1
              //还需要将已经登录的坐席分机登出
        		String agentid = this.client.getAgentid();
        		if(!UT.zstr(agentid)){
        			Agentsdo op = new Agentsdo();
        			op.updatedb(op.getupdatesql(this.client.getLoginext(), CTIEnum.logout, CTIEnum.Waiting));
        		}
        		//System.out.println("还未销毁前，client对象总数："+getTestCount());
//        		if(getTestCount()>1){
//        			System.out.println("destroy it"+this.client.getAgentid());
//            		try {
//            			this.finalize();
//            			subTestCount();
//            		} catch (Throwable e) {
//            			// TODO Auto-generated catch block
//            			e.printStackTrace();
//            		}
//            		System.out.println("销毁后，client对象总数："+getTestCount());
//            	}
    		}
    	}else{
			webSocketSet_s.remove(this);  //从set中删除
    		subOnlineSvrCount();          //在线数减1
//    		if(getOnlineSvrCount()>1){
//        		try {
//        			this.finalize();
//        		} catch (Throwable e) {
//        			// TODO Auto-generated catch block
//        			e.printStackTrace();
//        		}
//        	}
    	}
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
    	if(this.isConnected){
        //System.out.println("来自客户端的消息:" + message);
    		if(this.isServer){
    			//System.out.println(message);
    			String msgtype = this.checkmsgheader(message,"MSGTYPE");
    			//System.out.println(msgtype);
    			if("1".equals(msgtype)){//event群发
    				for(FswCtiServer item: webSocketSet){
    	                try {
    	                	if (session.isOpen()){
    	                		if(!UT.zstr(item.getClient().getAgentid()))
    	                			item.sendMessage(message);
    	                	}
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                }
    	            }
    			}else if("3".equals(msgtype)){//命令返回
    				String agentid = GetBodyItem(message,"userid");
    				//System.out.println(agentid);
    				for(FswCtiServer item: webSocketSet){
						try {
    	                	if (session.isOpen()){
    	                		//System.out.println(item.client.getAgentid());
    	                		if(agentid.equals(item.getClient().getAgentid())){
    	                			item.sendMessage(message);
    	                			//判断是否是登出消息，如果是，还要将坐席登出
    	                			int msgclass = Integer.parseInt(checkmsgheader(message, "MSG"));
    	                			if(msgclass == CTIEnum.CMD_Agentloginoff){
    	                				item.getClient().setAgentid("");
        	                			item.getClient().setLoginext("");
    	                			}
    	                			break;
    	                		}
    	                	}
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                } catch(NullPointerException e1){}
    	            }
    			}
    		}else{//是客户端发来的命令或者是心跳消息
    			if(checkCliWSMsg(message,"ping")){
    				return;
    			}
    			int msgclass = 1;
    			if(this.isAdmin==true){
    				try {
        				msgclass = Integer.parseInt(this.checkmsgclass(message));
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
    				int rescode = 200;
    				ArrayList<CtiClient> contents = new ArrayList<CtiClient>();
    				String pjid = "";
    				String cmdtype = "";
    				if(CTIEnum.CMD_ObCall == msgclass){//如果为有效执行命令，则开始执行相应任务，每个任务应采用子线程的方式执行；读取发送参数，从数据库中查询消息交由节点处理
    					Agentsop op = new Agentsop();
    					pjid = this.GetBodyItem(message,"pjid");
    					cmdtype = this.GetBodyItem(message,"type");
    					System.out.println(pjid);
    					System.out.println(cmdtype);
//    					rescode = op.agentlogin(name, agentpwd, loginext, contents);
    				}else{//发送拒绝执行消息
    					rescode = CTIEnum.PBX_ISNOT_HAVE_THISFUN;
    				}
    				String resmsg = getresmsg(msgclass, rescode);
    				for(FswCtiServer item: webSocketSet_a){//向前端admin发送命令执行回馈消息
						try {
    	                	if (session.isOpen()){//是否要全部发送？
    	                		if(item==this){
    	                			item.sendMessage(resmsg);
    	                		}
    	                	}
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                }catch(NullPointerException e1){}
    	            }
        		}else{
        			//首先判断是否已登录，如果未登录，则只接收登录命令，其它命令一律拒绝执行
        			if(this.client.getAgentid()==null||"".equals(this.client.getAgentid())){
        				try {
            				msgclass = Integer.parseInt(this.checkmsgclass(message));
        				} catch (Exception e) {
        					// TODO: handle exception
        				}
        				int rescode = 200;
        				ArrayList<CtiClient> contents = new ArrayList<CtiClient>();
        				String name = "";
        				String loginext = "";
        				if(CTIEnum.CMD_Agentlogin == msgclass){
        					Agentsop op = new Agentsop();
            				name = this.GetBodyItem(message,"agentid");
        					String agentpwd = this.GetBodyItem(message,"agentpwd");
        					loginext = this.GetBodyItem(message,"loginext");
        					name = "".equals(name)?this.client.getAgentid():name;
        					rescode = op.agentlogin(name, agentpwd, loginext, contents);
        				}else{//发送拒绝执行消息
        					rescode = CTIEnum.AGENT_CANNOT_BE_ACTIONED_NOLOGIN;
        				}
        				if(rescode == CTIEnum.AGENTLOGINOK){
        					this.client.setAgentid(name);//保存坐席id和坐席登录分机信息
        					this.client.setLoginext(contents.get(0).getLoginext());
        				}
        				String resmsg = getresmsg(msgclass, rescode);
        				for(FswCtiServer item: webSocketSet){
    						try {
        	                	if (session.isOpen()){
        	                		if(item==this){
        	                			item.sendMessage(resmsg);
        	                		}else if(this.client.getAgentid().equals(item.getClient().getAgentid())){
        	                			item.sendMessage(getresmsg(msgclass, CTIEnum.AGENTLOGIN_FORCELOGOFF));
        	                			item.getClient().setAgentid("");//使前面重复账号自动登出
        	                			item.getClient().setLoginext("");
        	                		}
        	                	}
        	                } catch (IOException e) {
        	                    e.printStackTrace();
        	                }catch(NullPointerException e1){}
        	            }
        				
        				boolean isomit = false;
        				String e_status = "";
        				String laststate = "";
        				if(rescode == CTIEnum.AGENTLOGINOK){
        					laststate = contents.get(0).getAgentlaststate();
        					if(!"".equals(laststate)){
        						isomit = true;
        						e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_IDLE, this.getagentstate(laststate));
        					}
        				}
        				
        				if(isomit){
            				for(FswCtiServer item: webSocketSet){
        						try {
            	                	if (session.isOpen()){
            	                		if(!UT.zstr(item.getClient().getAgentid())){
            	                			item.sendMessage(e_status);
            	                		}
            	                	}
            	                } catch (IOException e) {
            	                    e.printStackTrace();
            	                }catch(NullPointerException e1){}
            	            }
        				}
        			}else{
        				try {
            				msgclass = Integer.parseInt(this.checkmsgclass(message));
        				} catch (Exception e) {
        					// TODO: handle exception
        				}
            			if(CTIEnum.CMD_Agentlogin == msgclass||CTIEnum.CMD_Agentloginoff == msgclass||CTIEnum.CMD_Makebusy == msgclass){//cti服务器直接处理
            				Agentsop op = new Agentsop();
            				ArrayList<CtiClient> contents = new ArrayList<CtiClient>();
            				int rescode = 200;
            				String name = "";
            				String loginext = "";
            				String action = "";
            				if(CTIEnum.CMD_Agentlogin == msgclass){
            					name = this.GetBodyItem(message,"agentid");
            					String agentpwd = this.GetBodyItem(message,"agentpwd");
            					loginext = this.GetBodyItem(message,"loginext");
            					name = "".equals(name)?this.client.getAgentid():name;
            					rescode = op.agentlogin(name, agentpwd, loginext, contents);
            				}else{
            					if(CTIEnum.CMD_Makebusy == msgclass){
                					action = "0".equals(this.GetBodyItem(message,"ifbusy"))?CTIEnum.available:CTIEnum.onbreak;
                					name = this.GetBodyItem(message,"deviceid");
                				}else{
                					action = CTIEnum.logout;
                					name = this.GetBodyItem(message,"agentid");
                				}
            					name = "".equals(name)?this.client.getLoginext():name;//注意，是将坐席登录的分机登出
            					name = "".equals(name)?this.client.getAgentid():name;//如果登录分机为空，则认为登录分机和坐席id同名
            					rescode = op.agentaction(name, action, contents);
            				}
            				if(rescode == CTIEnum.AGENTLOGINOK){
            					this.client.setAgentid(name);
            					this.client.setLoginext(contents.get(0).getLoginext());
            				}
            				//发送命令执行结果消息
            				String resmsg = getresmsg(msgclass, rescode);
            				for(FswCtiServer item: webSocketSet){
            					if(msgclass==CTIEnum.CMD_Agentlogin){
            						try {
                	                	if (session.isOpen()){
                	                		if(item==this){
                	                			item.sendMessage(resmsg);
                	                		}else if(this.client.getAgentid().equals(item.getClient().getAgentid())){
                	                			item.sendMessage(getresmsg(msgclass, CTIEnum.AGENTLOGIN_FORCELOGOFF));
                	                			item.getClient().setAgentid("");
                	                			item.getClient().setLoginext("");
                	                		}
                	                	}
                	                } catch (IOException e) {
                	                    e.printStackTrace();
                	                }catch(NullPointerException e1){}
            					}else{
            						if(item==this){
                						try {
                    	                	if (session.isOpen()){
                    	                		item.sendMessage(resmsg);
                    	                	}
                    	                } catch (IOException e) {
                    	                    e.printStackTrace();
                    	                }
            							break;
            						}
            					}
            	            }
            				//发送状态改变事件消息
            				boolean isomit = false;
            				String e_status = "";
            				String laststate = "";
            				if(rescode == CTIEnum.AGENTLOGINOK){
            					laststate = contents.get(0).getAgentlaststate();
            					if(!"".equals(laststate)){
            						isomit = true;
            						e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_IDLE, this.getagentstate(laststate));
            					}
            				}else if(rescode == CTIEnum.AGENTLOGOFF_OK){
            					laststate = contents.get(0).getAgentlaststate();
            					isomit = true;
            					e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_NOLOGIN, this.getagentstate(laststate));
            				}else if(rescode == CTIEnum.AGENT_MAKEBUSY_OK){
            					laststate = contents.get(0).getAgentlaststate();
            					isomit = true;
            					e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, this.getagentstate(action), this.getagentstate(laststate));
            				}
            				if(isomit){
                				for(FswCtiServer item: webSocketSet){
            						try {
                	                	if (session.isOpen()){
                	                		if(!UT.zstr(item.getClient().getAgentid())){
                	                			item.sendMessage(e_status);
                	                		}
                	                	}
                	                } catch (IOException e) {
                	                    e.printStackTrace();
                	                }catch(NullPointerException e1){}
                	            }
                				if(rescode == CTIEnum.AGENTLOGOFF_OK){
                					this.client.setAgentid("");
                					this.client.setLoginext("");
                				}
            				}
            			}else{//群发给ctilink
            				for(FswCtiServer item: webSocketSet_s){
            	                try {
            	                	if (session.isOpen()){
            	                		if(!UT.zstr(item.getServer().getPbxid()))
            	                			item.sendMessage(message);
            	                	}
            	                } catch (IOException e) {
            	                    e.printStackTrace();
            	                }catch(NullPointerException e1){}
            	            }
            			}
        			}
        		}
    		}
        }else{
        	//确认cti登陆信息
        	if(this.isServer){
        		//System.out.println(message);
        		if(checkservermsg(message,"login")){
        			this.isConnected = true;
        		}
        	}else{
        		//System.out.println(message);
        		if(checkCliWSMsg(message,"login")){
        			this.isConnected = true;
        			if(this.isAdmin){
        				try{
        					JSONObject loginmsg = JSONObject.parseObject(message);
            				this.client.setAgentid(loginmsg.getString("thisDN"));
        				}catch (Exception e) {
        					// TODO: handle exception
        				}	
        			}
        		}else{
//        			try {
//						session.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
        		}
        	}
        }
        //System.out.println((new ObjectMapper()).writeValueAsString(this.user));
    } 
     
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        //System.out.println("发生错误");
        //error.printStackTrace();
    }
     
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }
        
	private boolean checkCliWSMsg(String message, String typemsg){
		boolean status = false;
		String type, thisDN, request;
		type = thisDN = request = "";
		try {
			JSONObject loginmsg = JSONObject.parseObject(message);
			if(loginmsg == null){
				return status;
			}
			type = loginmsg.getString("type");
			if(!typemsg.equals(type)){
				return status;
			}
			thisDN = loginmsg.getString("thisDN");
			if(thisDN == null){
				return status;
			}
			if("login".equals(typemsg)){
				JSONObject msg = loginmsg.getJSONObject("message");
				if(msg == null){
					return status;
				}
				request = msg.getString("request");
				if(!"CtiConnect".equals(request)){
					return status;
				}
				String DN = msg.getString("thisDN");
				if(!thisDN.equals(DN)){
					return status;
				}
			}else{
				String msg = loginmsg.getString("message");
				if(msg == null){
					return status;
				}
			}
			status = true;
			//this.client.setAgentid(thisDN);
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return status;
    }
	
	private boolean checkservermsg(String message, String typemsg){
		boolean status = false;
		String type, thisDN, request;
		type = thisDN = request = "";
		try {
			JSONObject loginmsg = JSONObject.parseObject(message);
			if(loginmsg == null){
				return status;
			}
			type = loginmsg.getString("type");
			if(!typemsg.equals(type)){
				return status;
			}
			thisDN = loginmsg.getString("thisDN");
			if(thisDN == null){
				return status;
			}
			if("login".equals(typemsg)){
				JSONObject msg = loginmsg.getJSONObject("message");
				if(msg == null){
					return status;
				}
				request = msg.getString("request");
				if(!"CtiConnect".equals(request)){
					return status;
				}
				String ipv4 = msg.getString("ipv4");
				if(ipv4 == null){
					return status;
				}
			}else{
				String msg = loginmsg.getString("message");
				if(msg == null){
					return status;
				}
			}
			this.server.setPbxid(thisDN);
			status = true;
			//this.client.setAgentid(thisDN);
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return status;
	}
	
	private String GetMsgPara(String src, String key, String s, String e, int sdelenum){
		String res = "";
        if("".equals(src))return "";
        int f1,f2,f3;

        if(src.indexOf(key+s)<0) return "";

        f1=src.indexOf(key+s,0);
        
        f2=src.indexOf(s,f1);
        
        f3=src.indexOf(e,f1);
        
        res=src.substring(f2+1+sdelenum,f3).replace("\r","").replace("\n","");/* 对于没有\r\n的字符串也不会有影响 */
        
        return res+"";
	}

	private String GetBodyItem(String src,String key)/* 获取key=val&中的val值 */
	{
		return GetMsgPara(src,key,"=","&",0);
	}
	
	private String checkmsgclass(String message){
		return GetBodyItem(message,"actionid");
	}
	
	private String checkmsgheader(String message,String header){
		return GetMsgPara(message,header,":","|",0);
	}
	
	private String getresmsg(int msgclass, int rescode){
		String resmsg = "MSGTYPE:3|MSG:"
				+ msgclass + "|MSGBODY:actionid="
				+ msgclass + "&imsg=&rescode="
				+ rescode + "&pbxrescode=&res=&@";
		return resmsg;
	}
	
	private String getscemsg(int msg, String agentid, int state, int laststate){
		String resmsg = "MSGTYPE:1|MSG:"
				+ msg + "|MSGBODY:agentid="
				+ agentid + "&agentstate="
				+ state + "&pbxid=&laststate="
				+ laststate + "&floatdata=&@";
		return resmsg;
	}
	
	private int getagentstate(String state){
		int st = 0;
		switch (state) {
		case "Logged Out":
			st = CTIEnum.AGENT_NOLOGIN;
			break;
		case "Available":
			st = CTIEnum.AGENT_IDLE;
			break;
		case "On Break":
			st = CTIEnum.AGENT_NOREADY;
			break;
		case "Available (On Demand)":
			st = CTIEnum.AGENT_NOREADY;
			break;
		default:
			st = CTIEnum.AGENT_NOLOGIN;
			break;
		}
		return st;
	}
	 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
    public static synchronized void addOnlineCount() {
        FswCtiServer.onlineCount++;
    }    
    public static synchronized void subOnlineCount() {
        FswCtiServer.onlineCount--;
    }
    
    public static synchronized int getOnlineSvrCount() {
        return onlineSvrCount;
    }
    public static synchronized void addOnlineSvrCount() {
    	FswCtiServer.onlineSvrCount++;
    }   
    public static synchronized void subOnlineSvrCount() {
    	FswCtiServer.onlineSvrCount--;
    }
    
    public static synchronized int getOnlineAdmCount() {
        return onlineAdmCount;
    }
    public static synchronized void addOnlineAdmCount() {
    	FswCtiServer.onlineAdmCount++;
    }
     
    public static synchronized void subOnlineAdmCount() {
    	FswCtiServer.onlineAdmCount--;
    }
    
    public static synchronized int getCmdExeThreadsCount() {
        return CmdExeThreadsCount;
    }
    public static synchronized void addCmdExeThreadsCount() {
    	FswCtiServer.CmdExeThreadsCount++;
    }
     
    public static synchronized void subCmdExeThreadsCount() {
    	FswCtiServer.CmdExeThreadsCount--;
    }
}
