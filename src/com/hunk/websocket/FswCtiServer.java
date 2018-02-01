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
import com.hunk.DUT.Projectmngop;
import com.hunk.DUT.UT;
import com.hunk.bean.CallList;
import com.hunk.bean.CtiClient;
import com.hunk.bean.CtiServer;
import com.hunk.bean.Pjjobbase;
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

	private final class CmdExeThread extends Thread{
		private boolean isPause;
		private boolean isEnd;
		private String pjid;
		private String pjjobid;
		private int cmdtype;
		private Projectbase pjbase;
		private int dfcallrate;
		private int interal;
		private int interal_temp;
		private CallList lastnum;
		private long executed;
		private Projectmngop op;
//		private long starttime;
//		private long pausetime;
//		public boolean isPause() {
//			return isPause;
//		}
//		public void setPause(boolean isPause) {
//			this.isPause = isPause;
//		}
		public void setEnd(boolean isEnd) {
			this.isEnd = isEnd;
		}
		public String getPjid() {
			return pjid;
		}
		public CmdExeThread(String pjid, int cmdtype) {
			super();
			this.isPause = false;
			this.isEnd = false;
			this.pjid = pjid;
			this.cmdtype = cmdtype;
			this.executed = 0;
			this.op = new Projectmngop();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				if(isEnd){
					cmdExeThreads.remove(this);
					subCmdExeThreadsCount();
					//System.out.println("executed:"+this.executed);
					//将结束的信息记录到数据库中（保存在最后一条pjid的任务记录中），只记录暂停时间
					if(this.pjbase!=null&&this.pjjobid!=null)//如果pjid或者pjjobid不存在，则不更新数据库
						op.UpdatePauseinfo(this.pjjobid, this.lastnum);
					break;
				}
				if(isPause){
					if(--this.interal_temp==0)
						this.isPause = false;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO: handle exception
					}
				}else{
					int rescode = 200;
					ArrayList<CallList> calllist = new ArrayList<CallList>();
					ArrayList<String> ALagentcount = new ArrayList<String>();
					int listnum = 1;
					//Projectmngop op = new Projectmngop();
					if(this.pjbase == null){//为空说明是重新开始的线程，需要获取项目相关配置信息
						//如果cmdtype为OBCMDTYPE_START则还要查询数据库查找lastnum
						if(this.cmdtype==CTIEnum.OBCMDTYPE_START){
							ArrayList<Pjjobbase> pjjcontents = new ArrayList<Pjjobbase>();
							rescode = op.GetLastpjjob(pjid, pjjcontents);
							//如果pjjcontents.size()<0则认为本次操作为OBCMDTYPE_RESTART
							if(rescode!=0){
								this.cmdtype = CTIEnum.OBCMDTYPE_RESTART;
							}else{
								this.lastnum = new CallList(pjjcontents.get(0).getLastnumid(), 
										"", 
										"", 
										"0");
								this.pjjobid = pjjcontents.get(0).getAutoid();
							}
						}
						ArrayList<Projectbase> pjcontents = new ArrayList<Projectbase>();
						rescode = op.GetProfile(pjid, cmdtype, pjcontents);
						if(rescode!=0){//发送错误消息
							String message = getresmsg(CTIEnum.CMD_ObCall, rescode);
							for(FswCtiServer item: webSocketSet_a){//
            	                try {
        	                		if(!UT.zstr(item.getClient().getAgentid())){
    	                				item.sendMessage(message);
        	                		}
            	                } catch (IOException e) {
            	                    e.printStackTrace();
            	                }catch(NullPointerException e1){}
            	            }
							this.isEnd = true;//
							continue;
						}
						this.pjbase = pjcontents.get(0);
						if(this.pjjobid==null){//获取任务id值，以传给子节点作为cdr参数使用
							ArrayList<Pjjobbase> pjjcontents = new ArrayList<Pjjobbase>();
							rescode = op.GetLastpjjob(pjid, pjjcontents);
							if(rescode!=0){//发送错误消息
								String message = getresmsg(CTIEnum.CMD_ObCall, rescode);
								for(FswCtiServer item: webSocketSet_a){//
	            	                try {
	        	                		if(!UT.zstr(item.getClient().getAgentid())){
	    	                				item.sendMessage(message);
	        	                		}
	            	                } catch (IOException e) {
	            	                    e.printStackTrace();
	            	                }catch(NullPointerException e1){}
	            	            }
								this.isEnd = true;//
								continue;
							}else{
								this.pjjobid = pjjcontents.get(0).getAutoid();
							}
						}
						try {
							this.interal = Integer.parseInt(pjbase.getCallinterval());
							if(this.interal<4) this.interal = 4;
						} catch (NumberFormatException e) {
							// TODO: handle exception
							this.interal = 4;
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
					}
					//如果呼叫模式是ivr式外呼和直接转坐席，由于this.pjbase.getQueueno()为空，GetAgentCount将会报48错误，每次只会送一次号
					try {
						this.dfcallrate = Integer.parseInt(this.pjbase.getDfcallrate());
					} catch (NumberFormatException e) {
						// TODO: handle exception
						dfcallrate = 0;
					}
					//System.out.println("dfcallrate:"+dfcallrate);
					if(dfcallrate>0){
						listnum = dfcallrate;
					}else{
						rescode = op.GetAgentCount(this.pjbase.getQueueno(), ALagentcount);
						//System.out.println("GetAgentCount"+":"+rescode);
						if(rescode==0){//根据空闲坐席数和呼叫系数得到这次要发送的号码清单
							try {
								listnum = (Integer.parseInt(ALagentcount.get(0)))*(Integer.parseInt(this.pjbase.getConcurrencyrate()))/100;
							} catch (NumberFormatException e) {
								// TODO: handle exception
								listnum = 1;
							}
						}
					}
					if(listnum<1) listnum = 1;//这里必须要至少选择一个，即便空闲坐席为0也宁可浪费，否则会出现线程不会停止的状况
					//System.out.println("listnum:"+listnum);
					rescode = op.GetCallList(pjid, pjbase.getCall_retry(), this.cmdtype, listnum, this.lastnum==null?"0":this.lastnum.getAutoid(), calllist);//这里要保证接着上一次的记录继续查找
					//System.out.println("GetCallList"+":"+rescode);
					if(rescode==CTIEnum.OBCALLPJ_ISALREADY_END&&"0".equals(this.lastnum==null?"0":this.lastnum.getRetry())){//这里需要判断号码是否已经全部执行完，若只是执行完一轮，retry不为0，则从头开始继续执行，否则结束该线程
						//号码全部执行完，结束该线程，发送消息给前端
						String message = getresmsg(CTIEnum.CMD_ObCall, rescode);
						for(FswCtiServer item: webSocketSet_a){//
        	                try {
    	                		if(!UT.zstr(item.getClient().getAgentid())){
	                				item.sendMessage(message);
    	                		}
        	                } catch (IOException e) {
        	                    e.printStackTrace();
        	                }catch(NullPointerException e1){}
        	            }
						//保存任务结束信息
						op.UpdateEndinfo(pjjobid);
						this.isEnd = true;
						//continue;
					}else{
						if(rescode==CTIEnum.OBCALLPJ_ISALREADY_END){//只是执行完一轮，retry不为0，则从头开始继续执行
							rescode = op.GetCallList(pjid, pjbase.getCall_retry(), this.cmdtype, listnum, "0", calllist);
						}
						if(rescode==0){//发送命令到子节点
							//拼装命令字符串，随机分发给子节点
							String message = "\"";//注意ctilink接受的有效信息格式必须收尾要有引号
							message+=FswCtiServer.SetHeader(CTIEnum.CMD, String.valueOf(CTIEnum.CMD_ObCall), CTIEnum.CMD_ObCall, "obc");//注意agentid不能为空
							
							message+=FswCtiServer.SetBody("pjid",this.pjid);
							message+=FswCtiServer.SetBody("pjclassid",this.pjbase.getPjclassid());
							message+=FswCtiServer.SetBody("pjjobid",this.pjjobid);
							message+=FswCtiServer.SetBody("trunkid",this.pjbase.getTrunkid());
							message+=FswCtiServer.SetBody("ifrd",this.pjbase.getIfrecord());
							message+=FswCtiServer.SetBody("rbid",this.pjbase.getRingbackid());
							message+=FswCtiServer.SetBody("overtm",this.pjbase.getCall_overtime());
							String obtype = this.pjbase.getObtype();
							message+=FswCtiServer.SetBody("obtype",obtype);
							if("0".equals(obtype)){
								message+=FswCtiServer.SetBody("fdata",this.pjbase.getQueueno());
							}else if("1".equals(obtype)){
								message+=FswCtiServer.SetBody("fdata",this.pjbase.getIvrid());
							}else{
								message+=FswCtiServer.SetBody("fdata",this.pjbase.getExtid());
							}
							
							int list_num = calllist.size();
							int svr_count = FswCtiServer.getOnlineSvrCount();
							int t = 0;
							if(svr_count>0){
								int dispc_count = list_num/svr_count;
								//System.out.println("dispc_count"+":"+dispc_count);
								for(FswCtiServer item: webSocketSet_s){//平均分发
	            	                try {
	        	                		if(!UT.zstr(item.getServer().getPbxid())){
	        	                			for(int i=0;i<dispc_count;i++){
	        	                				String sendmsg = message;
	        	                				sendmsg+=FswCtiServer.SetBody("destnum",calllist.get(t*dispc_count+i).getTelnum());
	        	                				sendmsg+=FswCtiServer.msgend;
	        	                				sendmsg+="\"";
	        	                				item.sendMessage(sendmsg);
	        	                			}
	        	                		}
	            	                } catch (IOException e) {
	            	                    e.printStackTrace();
	            	                }catch(NullPointerException e1){}
	            	                t++;
	            	            }
								if(list_num%svr_count>0){
									int random = UT.randomNum(0, svr_count-1);
									int j = 0;
									for(FswCtiServer item: webSocketSet_s){//随机选一个分发剩余的号码
										if(j<svr_count&&(j++ != random)){//防止webSocketSet_s的成员数和svr_count不相等的情况;判断是否被随机选择到
											continue;
										}
		            	                try {
		        	                		if(!UT.zstr(item.getServer().getPbxid())){
		        	                			for(int i=0;i<list_num%svr_count;i++){
		        	                				String sendmsg = message;
		        	                				sendmsg+=FswCtiServer.SetBody("destnum",calllist.get(t*svr_count+i).getTelnum());
		        	                				sendmsg+=FswCtiServer.msgend;
		        	                				sendmsg+="\"";
		        	                				item.sendMessage(sendmsg);
		        	                			}
		        	                			break;
		        	                		}
		            	                } catch (IOException e) {
		            	                    e.printStackTrace();
		            	                }catch(NullPointerException e1){}
		            	            }
								}
								//将所发送的号码retry数减一，统一由CTI来调度，并记录最后一个发送号码的信息；记录已执行的电话总数
								int temp;
								try {
									temp = Integer.parseInt(calllist.get(list_num-1).getRetry())-1;
								} catch (NumberFormatException e) {
									// TODO: handle exception
									temp = 0;
								}
								if(temp<0) temp=0;
								if(this.lastnum ==null){
									this.lastnum = new CallList(calllist.get(list_num-1).getAutoid(), 
											calllist.get(list_num-1).getTelnum(), 
											calllist.get(list_num-1).getTrycount(), 
											String.valueOf(temp));
								}else{
									this.lastnum.setAutoid(calllist.get(list_num-1).getAutoid());
									this.lastnum.setTelnum(calllist.get(list_num-1).getTelnum());;
									this.lastnum.setTrycount(calllist.get(list_num-1).getTrycount());
									this.lastnum.setRetry(String.valueOf(temp));
								}
								rescode = op.UpdateCallList(calllist);
								this.executed += list_num;
								//保存已执行的号码数，统一由cti调度
								op.UpdateExeinfo(pjjobid, list_num);
							}else{//没有可用的节点服务器，终止线程，并发送错误消息
								message = getresmsg(CTIEnum.CMD_ObCall, CTIEnum.PBXOBJISNULL);
								for(FswCtiServer item: webSocketSet_a){//
		        	                try {
		    	                		if(!UT.zstr(item.getClient().getAgentid())){
			                				item.sendMessage(message);
		    	                		}
		        	                } catch (IOException e) {
		        	                    e.printStackTrace();
		        	                }catch(NullPointerException e1){}
		        	            }
								this.isEnd = true;
								continue;
							}
						}
						//开始计时，间隔为1s
						this.interal_temp = this.interal;
						this.isPause = true;
					}
				}
			}
		}
	}
	
	private static int CmdExeThreadsCount = 0;
	public static CopyOnWriteArraySet<CmdExeThread> cmdExeThreads = new CopyOnWriteArraySet<CmdExeThread>();

//	public static synchronized void AddThreads(CmdExeThread thread){
//		cmdExeThreads.add(thread);
//	}
//	public static synchronized void RemoveThreads(CmdExeThread thread){
//		cmdExeThreads.remove(thread);
//	}
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
        			//Agentsdo op = new Agentsdo();
        			//op.updatedb(op.getupdatesql(this.client.getLoginext(), CTIEnum.logout, CTIEnum.Waiting));
        			ArrayList<CtiClient> contents = new ArrayList<CtiClient>();
        			(new Agentsop()).agentaction(this.client.getAgentid(), this.client.getLoginext(), CTIEnum.logout, contents);
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
    			String msgtype = checkmsgheader(message,"MSGTYPE");
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
        				msgclass = Integer.parseInt(checkmsgclass(message));
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
    				String pjid = "";
    				int cmdtype;
    				if(CTIEnum.CMD_ObCall == msgclass){//如果为有效执行命令，则开始执行相应任务，每个任务应采用子线程的方式执行；读取发送参数，从数据库中查询消息交由节点处理
    					pjid = GetBodyItem(message,"pjid");
    					try {
    						cmdtype = Integer.parseInt(GetBodyItem(message,"type"));
						} catch (NumberFormatException e) {
							// TODO: handle exception
							cmdtype = 0;
						}
    					//System.out.println(pjid);
    					//System.out.println(cmdtype);
    					if(cmdtype<1||cmdtype>7){//发送参数错误消息
    						String resmsg = getresmsg(msgclass, CTIEnum.PBX_ISNOT_HAVE_THISFUN);
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
    						//查询pjid的线程是否存在，若不存在，则new一个新的静态线程对象，否则对现有的线程进行关闭操作
    						CmdExeThread thread = null;
        					for(CmdExeThread item: cmdExeThreads){//向前端admin发送命令执行回馈消息
        						try {
        	                		if(item.getPjid().equals(pjid)){
        	                			thread = item;
        	                			break;
            	                	}
            	                }catch(NullPointerException e1){}
            	            }
        					if(thread!=null){
        						if(cmdtype==2){//若是暂停命令，则终止线程
        							thread.setEnd(true);
        						}else{//项目必须要先暂停才能执行其它命令
        							String resmsg = getresmsg(msgclass, CTIEnum.OBCALLPJ_ISALREADY_ON);
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
        						}
        					}else{//线程不存在，若命令不是暂停，则new一个新静态线程
        						if(cmdtype==2){//若是暂停命令，则提示错误
        							String resmsg = getresmsg(msgclass, CTIEnum.OBCALLPJ_ISALREADY_PAUSE);
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
        						}else{//新建静态线程
        							CmdExeThread obthread = new CmdExeThread(pjid,cmdtype);
        							cmdExeThreads.add(obthread);
        							addCmdExeThreadsCount();
        							obthread.start();
        						}
        					}
    					}
    				}else{//发送拒绝执行消息
    					String resmsg = getresmsg(msgclass, CTIEnum.PBX_ISNOT_HAVE_THISFUN);
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
    				}
        		}else{
        			//首先判断是否已登录，如果未登录，则只接收登录命令，其它命令一律拒绝执行
        			if(this.client.getAgentid()==null||"".equals(this.client.getAgentid())){
        				try {
            				msgclass = Integer.parseInt(checkmsgclass(message));
        				} catch (Exception e) {
        					// TODO: handle exception
        				}
        				int rescode = 200;
        				ArrayList<CtiClient> contents = new ArrayList<CtiClient>();
        				String name = "";
        				String loginext = "";
        				if(CTIEnum.CMD_Agentlogin == msgclass){
        					Agentsop op = new Agentsop();
            				name = GetBodyItem(message,"agentid");
        					String agentpwd = GetBodyItem(message,"agentpwd");
        					loginext = GetBodyItem(message,"loginext");
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
        						e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_IDLE, getagentstate(laststate));
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
            				msgclass = Integer.parseInt(checkmsgclass(message));
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
            					name = GetBodyItem(message,"agentid");
            					String agentpwd = GetBodyItem(message,"agentpwd");
            					loginext = GetBodyItem(message,"loginext");
            					name = "".equals(name)?this.client.getAgentid():name;
            					rescode = op.agentlogin(name, agentpwd, loginext, contents);
            				}else{
            					if(CTIEnum.CMD_Makebusy == msgclass){
                					action = "0".equals(GetBodyItem(message,"ifbusy"))?CTIEnum.available:CTIEnum.onbreak;
                					name = GetBodyItem(message,"deviceid");
                				}else{
                					action = CTIEnum.logout;
                					name = GetBodyItem(message,"agentid");
                				}
            					name = "".equals(name)?this.client.getLoginext():name;//注意，是将坐席登录的分机登出
            					name = "".equals(name)?this.client.getAgentid():name;//如果登录分机为空，则认为登录分机和坐席id同名
            					rescode = op.agentaction(this.client.getAgentid(), name, action, contents);
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
            						e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_IDLE, getagentstate(laststate));
            					}
            				}else if(rescode == CTIEnum.AGENTLOGOFF_OK){
            					laststate = contents.get(0).getAgentlaststate();
            					isomit = true;
            					e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, CTIEnum.AGENT_NOLOGIN, getagentstate(laststate));
            				}else if(rescode == CTIEnum.AGENT_MAKEBUSY_OK){
            					laststate = contents.get(0).getAgentlaststate();
            					isomit = true;
            					e_status = getscemsg(CTIEnum.EVENT_AgentStateChanged, "".equals(loginext)?name:loginext, getagentstate(action), getagentstate(laststate));
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
        		if(checkservermsg(message,"login",this.getServer())){
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
        
    public static boolean checkCliWSMsg(String message, String typemsg){
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
	
	public static boolean checkservermsg(String message, String typemsg, CtiServer server){
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
			server.setPbxid(thisDN);
			status = true;
			//this.client.setAgentid(thisDN);
		} catch (Exception e) {
			// TODO: handle exception
		}	
		return status;
	}
	
	public static String GetMsgPara(String src, String key, String s, String e, int sdelenum){
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

	private static final String msgheadend="|";
	private static final String msgheadsplit=":";
	private static final String msgbodyend="&";
	private static final String msgbodysplit="=";
	private static final String msgend="@";
	
	public static String GetBodyItem(String src,String key)/* 获取key=val&中的val值 */
	{
		return GetMsgPara(src,key,msgbodysplit,msgbodyend,0);
	}
	
	public static String checkmsgclass(String message){
		return GetBodyItem(message,"actionid");
	}
	
	public static String checkmsgheader(String message,String header){
		return GetMsgPara(message,header,msgheadsplit,msgheadend,0);
	}
	
	public static String getresmsg(int msgclass, int rescode){
		String resmsg = "MSGTYPE"+msgheadsplit+"3"+msgheadend+"MSG"+msgheadsplit+ msgclass + msgheadend
				+"MSGBODY"+msgheadsplit
				+"actionid"+msgbodysplit+ msgclass + msgbodyend
				+"imsg"+msgbodysplit+ "" + msgbodyend
				+"rescode"+msgbodysplit+ rescode + msgbodyend
				+"pbxrescode"+msgbodysplit+ "" + msgbodyend
				+"res"+msgbodysplit+ "" + msgbodyend
				+msgend;
		return resmsg;
	}
	
	public static String getscemsg(int msg, String agentid, int state, int laststate){
		String resmsg = "MSGTYPE"+msgheadsplit+"1"+msgheadend+"MSG"+msgheadsplit+ msg + msgheadend
				+"MSGBODY"+msgheadsplit
				+"agentid"+msgbodysplit+ agentid + msgbodyend
				+"agentstate"+msgbodysplit+ state + msgbodyend
				+"pbxid"+msgbodysplit+ "" + msgbodyend
				+"laststate"+msgbodysplit+ laststate + msgbodyend
				+"floatdata"+msgbodysplit+ "" + msgbodyend
				+msgend;
		return resmsg;
	}
	
	public static String SetHeader(int msgtype,String msg,int actionid,String agentid){/* MSGTYPE:msgtype|MSG:msg|MSGBODY:actionid=&userid=& */
		String msghead="MSGTYPE"+msgheadsplit+msgtype;
		msghead+=msgheadend;
		msghead+="MSG"+msgheadsplit+msg;
		msghead+=msgheadend;
		msghead+="MSGBODY"+msgheadsplit;
		msghead+="actionid";
		msghead+=msgbodysplit;
		msghead+=actionid;
		msghead+=msgbodyend;
		//add by hunk
		msghead+="userid";
		msghead+=msgbodysplit;
		msghead+=agentid;
		msghead+=msgbodyend;
		
		return msghead;
	}
	
	public static String SetBody(String key,String value){/* key=value&*/
		String msgbodyitem=key;
		msgbodyitem+=msgbodysplit;
		msgbodyitem+=value;
		msgbodyitem+=msgbodyend;

		return msgbodyitem;
	}
	
	private static int getagentstate(String state){
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