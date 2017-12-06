package com.hunk.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunk.bean.UserInfo;
import com.hunk.bean.Username;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/ctiwebsocket")
public class xnWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    
    private static int onlineSvrCount = 0;
     
    //CopyOnWriteArrayList/Set是ArrayList/Set 的一个线程安全的变体，其中所有可变操作（add、set等等）都是通过对底层数组进行一次新的复制来实现的。
    //当迭代线程数大于修改线程数时，CopyOnWriteArrayList的性能优于同步的ArrayList，反之，同步的ArrayList优于CopyOnWriteArrayList。CopyOnWriteArraySet同理。
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<xnWebSocket> webSocketSet = new CopyOnWriteArraySet<xnWebSocket>();
    private static CopyOnWriteArraySet<xnWebSocket> webSocketSet_s = new CopyOnWriteArraySet<xnWebSocket>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    //客户信息
    private UserInfo user;
    
    //判断是否是服务器
    private boolean isServer;
    
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        String usertype = session.getRequestParameterMap().get("type").get(0);
        if("server".equals(usertype)){
        	webSocketSet_s.add(this);
        	addOnlineSvrCount();
        	this.isServer = true;
        	System.out.println("有新连接加入！当前在线节点数为" + getOnlineSvrCount());
        }else{
        	webSocketSet.add(this);     //加入set中
            addOnlineCount();           //在线数加1
            this.isServer = false;
            System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        }
        System.out.println(usertype);
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
    	if(this.isServer == false){
    		webSocketSet.remove(this);  //从set中删除
            subOnlineCount();           //在线数减1
    	}else{
    		webSocketSet_s.remove(this);  //从set中删除
    		subOnlineSvrCount();          //在线数减1
    	}
        if(this.user!=null){
        	this.user.setAction("logout");
        	if(this.isServer == false)
        		this.user.setOnlineCount(String.valueOf(onlineCount));
        	else
        		this.user.setOnlineCount(String.valueOf(onlineSvrCount));
            List<Username> usernames = new ArrayList<Username>();
            for(xnWebSocket item: webSocketSet){
            	usernames.add(new Username(item.user.getUsername()));
            }
            for(xnWebSocket item: webSocketSet_s){
            	usernames.add(new Username(item.user.getUsername()));
            }
            this.user.setOnlineUsers(usernames);
            for(xnWebSocket item: webSocketSet){
            	try {
            		item.sendMessage((new ObjectMapper()).writeValueAsString(this.user));
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
            }
            for(xnWebSocket item: webSocketSet_s){
            	try {
            		item.sendMessage((new ObjectMapper()).writeValueAsString(this.user));
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
            }
         }
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     * @throws JsonProcessingException 
     */
    @OnMessage
    public void onMessage(String message, Session session) throws JsonProcessingException {
        //System.out.println("来自客户端的消息:" + message);
        if(this.user==null){
        	UserInfo tmp = checkMessage(message);
        	if(tmp.getAction().equals("login")){
        		this.user = tmp;
        	}
        	this.user.setOnlineCount(String.valueOf(onlineCount));
            List<Username> usernames = new ArrayList<Username>();
//            for(xnWebSocket item: webSocketSet){
//            	usernames.add(new Username(item.user.getUsername()));
//            }
//            for(xnWebSocket item: webSocketSet_s){
//            	usernames.add(new Username(item.user.getUsername()));
//            }
//            this.user.setOnlineUsers(usernames);
            for(xnWebSocket item: webSocketSet){
            	usernames.add(new Username(item.user.getUsername()));
                try {
                	if (session.isOpen()){
                		item.sendMessage((new ObjectMapper()).writeValueAsString(this.user));
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            for(xnWebSocket item: webSocketSet_s){
            	usernames.add(new Username(item.user.getUsername()));
                try {
                	if (session.isOpen()){
                		item.sendMessage((new ObjectMapper()).writeValueAsString(this.user));
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            this.user.setOnlineUsers(usernames);
        }else{
        	//群发消息，真实cti运用环境中不能群发
            for(xnWebSocket item: webSocketSet){             
                try {
                	if (session.isOpen()){
                		item.sendMessage(message);
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            //在此要判断消息类型是否是json格式，如果是，说明是客户端发来的消息，否则是服务器端发来的消息
            //是否要对服务器的消息进行分类？
            String ctimsg = checkctimsg(message);
            String pbxid = "";
            if(ctimsg != null && !("".equals(ctimsg))){
    			pbxid = GetBodyItem(ctimsg,"pbxid");		
    		}
            System.out.println("pbxid:"+pbxid);
            for(xnWebSocket item: webSocketSet_s){ 
            	if(pbxid.equals(item.user.getUsername())){
            		try {
                    	if (session.isOpen()){
                    		item.sendMessage(ctimsg);
                    	}
                    } catch (IOException e) {
                        e.printStackTrace();
                        //continue;
                    }
            		break;
            	}
            }
        }
        //System.out.println((new ObjectMapper()).writeValueAsString(this.user));
    }
    
    /**  
     * 接收信息时执行  
     * @param session  
     * @param bb 二进制数组  
     * @param last  
     */  
    @OnMessage  
    public void echoBinaryMessage(Session session, ByteBuffer bb, boolean last) {
    	//System.out.println("来自客户端的图片！！！"+last+"bb:"+bb.limit());
        try {  
            if (session.isOpen()) {
            	for(xnWebSocket item: webSocketSet){
            		//item.session.getBasicRemote().sendBinary(bb, last);
            		item.session.getBasicRemote().sendBinary(bb, false);
            		if(last){
            			UserInfo uinfo = new UserInfo(null, this.user.getUsername(), this.user.getUserid(), null, null, null, null);
            			//此处需要设置格式为UTF8，否则接收端无法解析
            			String uinfos = (new ObjectMapper()).writeValueAsString(uinfo);
            			
            			byte[] uidb = uinfos.getBytes("UTF8");
            			ByteBuffer uidbf = ByteBuffer.wrap(uidb);
            			uinfos += setsizetostring(uidbf.remaining());
            			//System.out.println(uidb.length+"uidb"+uidbf.capacity()+"uidbf"+uidbf.remaining());
            			//System.out.println(new String(uidbf.array(),"UTF-8"));
            			item.session.getBasicRemote().sendBinary(ByteBuffer.wrap(uinfos.getBytes("UTF8")), true);
            		}
            	}
                //session.getBasicRemote().sendBinary(bb, last);  
            }  
        } catch (IOException e) {  
            try {  
            	System.out.println("发送错误！！！");
                session.close();  
            } catch (IOException e1) {  
                // Ignore  
            }  
        }  
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
    
//    private void checkMessage(String message){
//    	String[] tmp = message.split("\",\"");
//    	if(tmp!=null){
//    		for(int i=0;i<tmp.length;i++){
//    			String[] set = tmp[i].split("\",\"");
//    			if(set!=null){
//    				String key = "";
//    				String value = "";
//    				if(i==0){
//    					key = set[0].substring(2);
//    				}else
//    					key = set[0].substring(1);
//    				if(key.equals("action")){
//    					value=set[1].substring(0,set[1].length()-2);
//    					if(value.equals("login")||value.equals("logout")){
//    						continue;
//    					}else
//    						break;
//    				}else if(key.equals("userid")){
//    					value=set[1].substring(0,set[1].length()-2);
//    					this.user.setUserid(value);
//    				}else if(key.equals("username")){
//    					value=set[1].substring(0,set[1].length()-2);
//    					this.user.setUsername(value);
//    				}
//    			}
//    		}	
//    	}
//    }
    
	private UserInfo checkMessage(String message){
    	UserInfo uinfo  = new UserInfo();
    	try {
    		uinfo = (new ObjectMapper()).readValue(message, uinfo.getClass());
    		//List<Entity> myObjects = mapper.readValue(message, mapper.getTypeFactory().constructCollectionType(List.class, entity.getClass()));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return uinfo;
    }
    
//    private String[] checkJson(String message){
//    	String[] tmp = message.split("\",\"");
//    	return tmp;
//    }

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
	
	private String checkctimsg(String message){
		String ctimsg = "";
		UserInfo uinfo  = new UserInfo();
		try {
    		uinfo = (new ObjectMapper()).readValue(message, uinfo.getClass());
    		//List<Entity> myObjects = mapper.readValue(message, mapper.getTypeFactory().constructCollectionType(List.class, entity.getClass()));
    		ctimsg = uinfo.getContent();
    		System.out.println(ctimsg);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("JsonParseException");
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("JsonMappingException");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ctimsg;
	}
	
	private String setsizetostring(int size){
		String ssize = String.valueOf(size);
		int a = ssize.length();
		if(a<=8){
			for(int i=0;i<8-a;i++){
				ssize = "0"+ ssize;
			}
			return ssize;
		}
		return "00000000";
	}
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        xnWebSocket.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        xnWebSocket.onlineCount--;
    }
    
    public static synchronized int getOnlineSvrCount() {
        return onlineSvrCount;
    }
 
    public static synchronized void addOnlineSvrCount() {
    	xnWebSocket.onlineSvrCount++;
    }
     
    public static synchronized void subOnlineSvrCount() {
    	xnWebSocket.onlineSvrCount--;
    }
}
