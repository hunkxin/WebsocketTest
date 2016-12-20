<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<base href="<%=basePath%>">
<title>My WebSocket</title>
<title>Insert title here</title>
</head>
<body>
	Welcome<br/>
    <input id="text" type="text" /><button onclick="send()">Send</button><button onclick="closeWebSocket()">Close</button><button onclick="connectWebSocket()">connect</button>
    <div id="message">
</body>
<script type="text/javascript">
      var websocket = null;
       
      //判断当前浏览器是否支持WebSocket
      if('WebSocket' in window){
          websocket = new WebSocket("ws://localhost:8383/WebsocketTest/websocket");
      }
      else{
          alert('Not support websocket')
      }
       
      //连接发生错误的回调方法
      websocket.onerror = function(){
          setMessageInnerHTML("error");
      };
       
      //连接成功建立的回调方法
      websocket.onopen = function(event){
          setMessageInnerHTML("open");
      }
       
      //接收到消息的回调方法
      websocket.onmessage = function(event){
          setMessageInnerHTML(event.data);
      }
       
      //连接关闭的回调方法
      websocket.onclose = function(){
          setMessageInnerHTML("close");
      }
       
      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function(){
          websocket.close();
      }
       
      //将消息显示在网页上
      function setMessageInnerHTML(innerHTML){
          document.getElementById('message').innerHTML += innerHTML + '<br/>';
      }
       
      //关闭连接
      function closeWebSocket(){
          websocket.close();
          websocket=null;
          //alert(websocket==null?"null":websocket);
      }
      
      //重新连接
      function connectWebSocket(){
    	  if(websocket==null){
    		  websocket = new WebSocket("ws://localhost:8383/WebsocketTest/websocket");
    		  //return false;
    		  //连接发生错误的回调方法
    	      websocket.onerror = function(){
    	          setMessageInnerHTML("error");
    	      };
    	       
    	      //连接成功建立的回调方法
    	      websocket.onopen = function(event){
    	          setMessageInnerHTML("open");
    	      }
    	       
    	      //接收到消息的回调方法
    	      websocket.onmessage = function(event){
    	          setMessageInnerHTML(event.data);
    	      }
    	       
    	      //连接关闭的回调方法
    	      websocket.onclose = function(){
    	          setMessageInnerHTML("close");
    	      }
    	  }else
          	alert("当前已有连接！");
          //return false;
      }
       
      //发送消息
      function send(){
          var message = document.getElementById('text').value;
          websocket.send(message);
      }
  </script>
</html>