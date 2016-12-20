package com.hunk.websocket;

import java.io.IOException;  
import java.nio.ByteBuffer;  
import java.util.Random;  
import java.util.Timer;  
import java.util.TimerTask;  
  
import javax.websocket.OnClose;  
import javax.websocket.OnMessage;  
import javax.websocket.OnOpen;  
import javax.websocket.PongMessage;  
import javax.websocket.Session;  
import javax.websocket.server.ServerEndpoint;  
  
@ServerEndpoint("/mywebsocket")  
public class MyWebSocket2 {  
      
    private Session session;  
    private static final Random random = new Random();  
    private Timer timer = null;  
    //停止信息信息指令  
    private static final ByteBuffer stopbuffer  = ByteBuffer.wrap(new byte[]{1, 9, 2, 0, 1, 5, 1, 6});  
      
    /**  
     * 打开连接时执行  
     * @param session  
     */  
    @OnOpen  
    public void start(Session session) {  
        this.session = session;  
        try {  
            System.out.println("open");  
            if (session.isOpen()) {  
                //设置心跳发送信息。每2秒发送一次信息。  
                timer = new Timer(true);  
                timer.schedule(task, 1000, 2000);  
            }  
        } catch (Exception e) {  
            try {  
                session.close();  
            } catch (IOException e1) {}  
        }  
    }  
  
    /**  
     * 接收信息时执行  
     * @param session  
     * @param msg 字符串信息  
     * @param last  
     */  
    @OnMessage  
    public void echoTextMessage(Session session, String msg, boolean last) {  
        try {  
            if (session.isOpen()) {  
                System.out.println("string:" + msg);  
                session.getBasicRemote().sendText(msg, last);  
            }  
        } catch (IOException e) {  
            try {  
                session.close();  
            } catch (IOException e1) {  
                // Ignore  
            }  
        }  
    }  
  
    /**  
     * 接收信息时执行  
     * @param session  
     * @param bb 二进制数组  
     * @param last  
     */  
    @OnMessage  
    public void echoBinaryMessage(Session session, ByteBuffer bb, boolean last) {  
        try {  
            if (session.isOpen()) {  
                //如果是停止心跳指令，则停止心跳信息  
                if (bb.compareTo(stopbuffer) == 0) {  
                    if (timer != null) {  
                        timer.cancel();  
                    }  
                } else {  
                    session.getBasicRemote().sendBinary(bb, last);  
                }  
            }  
        } catch (IOException e) {  
            try {  
                session.close();  
            } catch (IOException e1) {  
                // Ignore  
            }  
        }  
    }  
      
    /**  
     * 接收pong指令时执行。  
     *  
     * @param pm    Ignored.  
     */  
    @OnMessage  
    public void echoPongMessage(PongMessage pm) {  
        // 无处理  
    }  
      
    @OnClose  
    public void end(Session session) {  
        try {  
            System.out.println("close");  
            if (timer != null) {  
                timer.cancel();  
            }  
        } catch(Exception e) {  
        }  
    }  
      
    /*  
     * 发送心跳信息  
     */  
    public void sendLong(long param) {  
        try {  
            if (session.isOpen()) {  
                this.session.getBasicRemote().sendText(String.valueOf(param));  
            }  
        } catch (IOException e) {  
            try {  
                this.session.close();  
            } catch (IOException e1) {}  
        }  
    }  
      
    /**  
     * 心跳任务。发送随机数。  
     */  
    TimerTask task = new TimerTask() {  
        public void run() {     
            long param = random.nextInt(100);  
            sendLong(param);  
        }     
    };  
  
}
