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
import com.hunk.DUT.Agentsop;
import com.hunk.DUT.CTIEnum;
import com.hunk.DUT.UT;
import com.hunk.bean.CtiClient;
import com.hunk.bean.CtiServer;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/ctiwebsocket_m")
public class xnWebSocket_M {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    
    private static int onlineSvrCount = 0;
     
    //CopyOnWriteArrayList/Set是ArrayList/Set 的一个线程安全的变体，其中所有可变操作（add、set等等）都是通过对底层数组进行一次新的复制来实现的。
    //当迭代线程数大于修改线程数时，CopyOnWriteArrayList的性能优于同步的ArrayList，反之，同步的ArrayList优于CopyOnWriteArrayList。CopyOnWriteArraySet同理。
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<xnWebSocket_M> webSocketSet = new CopyOnWriteArraySet<xnWebSocket_M>();
    private static CopyOnWriteArraySet<xnWebSocket_M> webSocketSet_s = new CopyOnWriteArraySet<xnWebSocket_M>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    //客户端对象
    private CtiClient client;
    
    //服务器对象
    private CtiServer server;
    
    //判断是否是服务器
    private boolean isServer;
    
  //判断是否已连接上cti
    private boolean isConnected;
    
    //连接开始时间
    private Date starttime;
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
        	System.out.println("有新连接加入！当前在线节点数为" + getOnlineSvrCount());
        }else{
        	webSocketSet.add(this);     //加入set中
            addOnlineCount();           //在线数加1
            this.isServer = false;
            this.client = new CtiClient();
            System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        }
        System.out.println(usertype);
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
    	System.out.println("closed!");
    	if(this.isServer == false){
    		webSocketSet.remove(this);  //从set中删除
            subOnlineCount();           //在线数减1
    	}else{
    		webSocketSet_s.remove(this);  //从set中删除
    		subOnlineSvrCount();          //在线数减1
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
    			String msgtype = this.checkservermsg(message);
    			if("1".equals(msgtype)){//event群发
    				for(xnWebSocket_M item: webSocketSet){
    	                try {
    	                	if (session.isOpen()){
    	                		if(!UT.zstr(item.client.getAgentid()))
    	                			item.sendMessage(message);
    	                	}
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                }
    	            }
    			}else if("3".equals(msgtype)){//命令返回
    				String agentid = GetBodyItem(message,"userid");
    				for(xnWebSocket_M item: webSocketSet){
						try {
    	                	if (session.isOpen()){
    	                		if(agentid.equals(item.client.getAgentid()))
    	                			item.sendMessage(message);
    	                	}
    	                } catch (IOException e) {
    	                    e.printStackTrace();
    	                } catch(NullPointerException e1){}
						break;
    	            }
    			}
    		}else{//是客户端发来的命令或者是心跳消息
    			if(checkCliWSMsg(message,"ping")){
    				return;
    			}
    			
    			//首先判断是否已登录，如果未登录，则只接收登录命令，其它命令一律拒绝执行
    			int msgclass = 1;
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
    					this.client.setAgentid(name);
    				}
    				String resmsg = getresmsg(msgclass, rescode);
    				for(xnWebSocket_M item: webSocketSet){
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
        				for(xnWebSocket_M item: webSocketSet){
    						try {
        	                	if (session.isOpen()){
        	                		if(!UT.zstr(item.client.getAgentid())){
        	                			item.sendMessage(e_status);
            	                		if(this.client.getAgentid().equals(item.client.getAgentid())&&item!=this)
            	                			webSocketSet.remove(item);//剔除前面重复账号登录的连接
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
        					name = "".equals(name)?this.client.getAgentid():name;
        					rescode = op.agentaction(name, action, contents);
        				}
        				if(rescode == CTIEnum.AGENTLOGINOK){
        					this.client.setAgentid(name);
        				}else if(rescode == CTIEnum.AGENTLOGOFF_OK){
        					this.client.setAgentid("");
        				}
        				//发送命令执行结果消息
        				String resmsg = getresmsg(msgclass, rescode);
        				for(xnWebSocket_M item: webSocketSet){
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
            				for(xnWebSocket_M item: webSocketSet){
        						try {
            	                	if (session.isOpen()){
            	                		if(!UT.zstr(item.client.getAgentid())){
            	                			item.sendMessage(e_status);
                	                		if(rescode == CTIEnum.AGENTLOGOFF_OK&&this.client.getAgentid().equals(item.client.getAgentid())&&item!=this)
                	                			webSocketSet.remove(item);
            	                		}
            	                	}
            	                } catch (IOException e) {
            	                    e.printStackTrace();
            	                }catch(NullPointerException e1){}
            	            }
        				}
        			}else{//群发给ctilink
        				for(xnWebSocket_M item: webSocketSet_s){
        	                try {
        	                	if (session.isOpen()){
        	                		if(!UT.zstr(item.server.getPbxid()))
        	                			item.sendMessage(message);
        	                	}
        	                } catch (IOException e) {
        	                    e.printStackTrace();
        	                }catch(NullPointerException e1){}
        	            }
        			}
    			}
    		}
        }else{
        	//确认cti登陆信息
        	if(this.isServer){
        		
        	}else{
        		System.out.println(message);
        		if(checkCliWSMsg(message,"login")){
        			this.isConnected = true;
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
        System.out.println("发生错误");
        error.printStackTrace();
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
	
	private String checkservermsg(String message){
		String ctimsg = "";
		return ctimsg;
	}
	
	private String getresmsg(int msgclass, int rescode){
		String resmsg = "MSGTYPE:3|MSG:"
				+ msgclass + "|MSGBODY:actionid="
				+ msgclass + "&imsg=&rescode="
				+ rescode + "&pbxrescode=&res=&@";
		return resmsg;
	}
	
	private String getscemsg(int msg, String agentid, int state, int laststate){
		String resmsg = "MSGTYPE:3|MSG:"
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
        xnWebSocket_M.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        xnWebSocket_M.onlineCount--;
    }
    
    public static synchronized int getOnlineSvrCount() {
        return onlineSvrCount;
    }
 
    public static synchronized void addOnlineSvrCount() {
    	xnWebSocket_M.onlineSvrCount++;
    }
     
    public static synchronized void subOnlineSvrCount() {
    	xnWebSocket_M.onlineSvrCount--;
    }
}
