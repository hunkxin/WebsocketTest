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
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	<script type="text/javascript">
		var ws = {};
		function init()
		{
			ws = new WebSocket("ws://localhost:8383/WebsocketTest/sendImage");
			
			ws.onopen=function(event){
				console.log("open...");
			};
			 ws.onmessage = function(event){
			
					 var reader = new FileReader();
					
					
					 reader.onload=function(eve){
						 if(eve.target.readyState==FileReader.DONE)
						 {
							 
							var img = document.createElement("img");
							img.src=this.result;
							document.getElementById("show").appendChild(img);
						 }
					 };
					 reader.readAsDataURL(event.data);
					
			 };
		}
		
		function sendMsg()
		{
			ws.send("asdfaasdf");
		}
	</script>
  </head>
  
  <body onload="init()">
  	<input type="button" value="send" onclick="sendMsg()"/>
  	<div id="show" style="width:500px; height:300px; border:1px solid"></div>
</body>
</html>