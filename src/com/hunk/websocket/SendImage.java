package com.hunk.websocket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * 
 * @author ghpan
 * ����:2014-09-10
 *
 */
@ServerEndpoint("/sendImage")
public class SendImage {
	@OnMessage
	public void getMessage(Session session ,String message){
		try {
			FileInputStream fs = new FileInputStream("E:\\PM\\asd.jpg");
			byte[] content = new byte[fs.available()];
			fs.read(content);
			ByteBuffer byteBuffer = ByteBuffer.wrap(content);
			Basic basic = session.getBasicRemote();
			basic.sendBinary(byteBuffer);
			
			fs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	@OnOpen
	public void start(Session session){
		System.out.println("connected...");
	}
}
