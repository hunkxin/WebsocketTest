package com.hunk.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/websocket")
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
     
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();
     
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    //客户信息
    private String userid;
    private String username;

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        for(MyWebSocket item: webSocketSet){
        	try {
        		item.sendMessage("login:"+String.valueOf(getOnlineCount()));
        		System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        for(MyWebSocket item: webSocketSet){
        	try {
        		item.sendMessage("{logout:"+String.valueOf(getOnlineCount()));
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        checkMessage(message);
        //群发消息
        for(MyWebSocket item: webSocketSet){             
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
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
    
    private void checkMessage(String message){
    	String[] tmp = message.split("\",\"");
    	if(tmp!=null){
    		for(int i=0;i<tmp.length;i++){
    			String[] set = tmp[i].split("\",\"");
    			if(set!=null){
    				String key = "";
    				String value = "";
    				if(i==0){
    					key = set[0].substring(2);
    				}else
    					key = set[0].substring(1);
    				if(key.equals("action")){
    					value=set[1].substring(0,set[1].length()-2);
    					if(value.equals("login")||value.equals("logout")){
    						continue;
    					}else
    						break;
    				}else if(key.equals("userid")){
    					value=set[1].substring(0,set[1].length()-2);
    					this.userid=value;
    				}else if(key.equals("username")){
    					value=set[1].substring(0,set[1].length()-2);
    					this.username=value;
    				}
    			}
    		}	
    	}
    }
    
    private String[] checkJson(String message){
    	String[] tmp = message.split("\",\"");
    	return tmp;
    }
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
}
