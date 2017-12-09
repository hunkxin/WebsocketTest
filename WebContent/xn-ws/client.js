/**
 * 
 */
(function () {
	var d = document,
	w = window,
	p = parseInt,
	dd = d.documentElement,
	db = d.body,
	dc = d.compatMode == 'CSS1Compat',
	dx = dc ? dd: db,
	ec = encodeURIComponent;
	
	
	w.CHAT = {
		msgObj:d.getElementById("message"),
		screenheight:w.innerHeight ? w.innerHeight : dx.clientHeight,
		username:null,
		userid:null,
		lasttime:0,
		socket:null,
		cti:null,
		//让浏览器滚动条保持在最低部
		scrollToBottom:function(){
			w.scrollTo(0, this.msgObj.clientHeight);
		},
		//退出，本例只是一个简单的刷新
		logout:function(){
			//this.socket.disconnect();
			if(this.cti!=null){
				this.cti.CtiDisconnect();
				//this.cti=null;
			}
			if(this.cti.CheckWS())
				logmsg("is still on!!");
			location.reload();
		},
		//提交聊天消息内容
		submit:function(){
			var content = d.getElementById("content").value;
			if(content != ''){
				/*var obj = {
					action:"message",
					userid: this.userid,
					username: this.username,
					time:stdTime(),
					content: content,
					onlineCount:null,
					onlineUsers:null
				};*/
				//this.socket.emit('message', obj);
				//this.socket.send(JSON.stringify(obj));
				this.cti.send(content);
				d.getElementById("content").value = '';
			}
			return false;
		},
		genUid:function(){
			return new Date().getTime()+""+Math.floor(Math.random()*899+100);
		},
		//更新系统消息，本例中在用户加入、退出的时候调用
		updateSysMsg:function(o, action){
			//当前在线用户列表
			//var onlineUsers = o.onlineUsers;
			//当前在线人数
			var onlineCount = o.onlineCount;
			//新加入用户的信息
			var user = o.username;
			var time = o.time;
				
			//更新在线人数
			var userhtml = '';
//			var separator = '、';
//			for(var i=0;i<onlineUsers.length;i++) {
//				userhtml += onlineUsers[i].username;
//				if(i==onlineUsers.length-1)
//					separator = '';
//				userhtml += separator;
//		    }
			d.getElementById("onlinecount").innerHTML = '当前共有 '+onlineCount+' 人在线，在线列表：'+userhtml;
			
			//添加系统消息
			var html = '';
			html += '<div class="msg-system">';
			html += time+"&nbsp;&nbsp";
			html += user;
			html += (action == 'login') ? ' 加入了聊天室' : ' 退出了聊天室';
			html += '</div>';
			var section = d.createElement('section');
			section.className = 'system J-mjrlinkWrap J-cutMsg';
			section.innerHTML = html;
			this.msgObj.appendChild(section);
			this.scrollToBottom();
		},
		//第一个界面用户提交用户名
		usernameSubmit:function(){
			var username = d.getElementById("username").value;
			if(username != ""){
				d.getElementById("username").value = '';
				d.getElementById("loginbox").style.display = 'none';
				d.getElementById("chatbox").style.display = 'block';
				this.init(username);
				//initdrop();
			}
			return false;
		},
		init:function(username){
			/*
			客户端根据时间和随机数生成uid,这样使得聊天室用户名称可以重复。
			实际项目中，如果是需要用户登录，那么直接采用用户的uid来做标识就可以
			*/
			this.userid = this.genUid();
			this.username = "2017120223261455VMNIitgFzK7NJ2cy";
			
			d.getElementById("showusername").innerHTML = this.username;
			this.msgObj.style.minHeight = (this.screenheight - db.clientHeight + this.msgObj.clientHeight) + "px";
			this.scrollToBottom();
			
			this.cti = new ClassXnCtiClient();
			this.cti.CTIConnectedEvent = function()
            {
				var obj = {
						onlineCount : 1,
						username : CHAT.cti.UserID,
						time : stdTime()
				};
				CHAT.updateSysMsg(obj,'login');
				//console.log("CtiConnect");
            };
            var msg = {
            		userid : "",
            		showmsg : ""
            		};
            this.cti.EVENT_ExtStateChanged = function(deviceid,devicestate,pbxid,laststate,floatdata){
            	msg.userid = deviceid;
            	msg.showmsg = deviceid+":"+devicestate+":"+pbxid+":"+laststate+":"+floatdata;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_AgentStateChanged = function(agentid,agentstate,pbxid,laststate,floatdata){
            	msg.userid = agentid;
            	msg.showmsg = agentid+":"+agentstate+":"+pbxid+":"+laststate+":"+floatdata;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_ChannelStateChanged = function(channel,channelstate,pbxid,laststate,ext,floatdata){
            	msg.userid = ext;
            	msg.showmsg = channel+":"+channelstate+":"+pbxid+":"+laststate+":"+ext+":"+floatdata;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_AgentCallined = function(caller,pbxid,callerchannel,fromqueue,callid,agentid,context,exten,floatinfo){
            	msg.userid = agentid;
            	msg.showmsg = caller+":"+pbxid+":"+pbxid+":"+callerchannel+":"+fromqueue+":"+callid+":"+agentid+":"+context+":"+exten+":"+floatinfo;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_MeEventHanpend = function(pbxid,eventtype,me,member,memidx,activechannel,floatinfo){
            	msg.userid = member;
            	msg.showmsg = pbxid+":"+eventtype+":"+me+":"+member+":"+memidx+":"+activechannel+":"+floatinfo;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_NewQueueEvent = function(queue,join,joincaller,jcchannel,callid,pbxid,floatinfo){
            	msg.userid = "";
            	msg.showmsg = queue+":"+join+":"+joincaller+":"+jcchannel+":"+callid+":"+pbxid+":"+floatinfo;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_WXChatIn = function(fromghid,fromwxuserid,agentid,content,msgtype){
            	msg.userid = agentid;
            	msg.showmsg = fromghid+":"+fromwxuserid+":"+agentid+":"+content+":"+msgtype;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.EVENT_CMDRES = function(imsg,rescode,pbxrescode,res,actionid){
            	msg.userid = "CTI Server";
            	msg.showmsg = imsg+":"+rescode+":"+pbxrescode+":"+res+":"+actionid;
            	setMessageInnerHTML(JSON.stringify(msg));
            };
            this.cti.CTIDisConnectedEvent = function(){
            	logmsg("closed!");
            };
			this.cti.CtiConnectEx("http://localhost:8383/CCCMSManage/pbxnode/CTImng/Cticonfigmnggetmsg",CHAT.username,"f379eaf3c831b04de153469d1bec345e",CHAT.cti);
			
			//连接websocket后端服务器
			//this.socket = new WebSocket("ws://192.168.1.128:8080/WebsocketTest/websocket");
			//this.socket = new WebSocket("ws://localhost:8383/WebsocketTest/ctiwebsocket?type=client");
			//this.socket = new WebSocket("ws://192.168.1.128:8080/WebsocketTest/ctiwebsocket?type=client");
			//this.socket = new WebSocket("ws://localhost:8040");
			//socketutil(this.socket);
		}
	};
	
	function logmsg(msg)
	{
		console.log(msg);
	}
	
	//监听消息发送
	function setMessageInnerHTML(obj){
		var msg = JSON.parse(obj);
		showtime();
		var isme = (msg.userid == CHAT.cti.UserID) ? true : false;
		var contentDiv = '<div>'+msg.showmsg+'</div>';
		var usernameDiv = '<span>'+msg.userid+'</span>';
		
		var section = d.createElement('section');
		if(isme){
			section.className = 'user';
			section.innerHTML = contentDiv + usernameDiv;
		} else {
			section.className = 'service';
			section.innerHTML = usernameDiv + contentDiv;
		}
		CHAT.msgObj.appendChild(section);
		
		//alert(CHAT.lasttime);
		CHAT.scrollToBottom();	
	};
	
	function socketutil(websocket){
      	//连接发生错误的回调方法
            websocket.onerror = function(){
            	//var logoutmsg = {action:"logout",userid:CHAT.userid, username:CHAT.username,time:stdTime()};
            	//websocket.send(JSON.stringify(logoutmsg));
            	console.log("wrong!!!");
            };
             
            //连接成功建立的回调方法
            websocket.onopen = function(event){
            	var loginmsg = {action:"login",userid:CHAT.userid, username:CHAT.username,time:stdTime(),onlineCount:"",onlineUsers:[{"username":""}]};
            	//alert(JSON.stringify(loginmsg));
            	websocket.send(JSON.stringify(loginmsg));
            }
             
            //接收到消息的回调方法
            websocket.onmessage = function(event){
                setMessageInnerHTML(event.data);
            }
             
            //连接关闭的回调方法
            websocket.onclose = function(event){
            	//alert("close!!!");
            	console.log("close:"+event.code);
            }
             
            //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
            window.onbeforeunload = function(){
            	if(websocket!=null){
            		var logoutmsg = {action:"logout",userid:CHAT.userid, username:CHAT.username,time:stdTime(),onlineCount:"",onlineUsers:[{"username":""}]};
            		websocket.send(JSON.stringify(logoutmsg));
            		websocket.close();
            		websocket=null;
            	}
            }
        }
	
	function stdTime(){
		var now = new Date();
		var week = ["星期日","星期一","星期二","星期三","星期四","星期五","星期六"]
		var stdtime = now.getFullYear()+
					  "/"+(now.getMonth()+1)+
					  "/"+now.getDate()+
					  "/"+week[now.getDay()]+
					  "/"+now.getHours()+
					  ":"+(now.getMinutes()<10?"0":"")+now.getMinutes()+
					  ":"+(now.getSeconds()<10?"0":"")+now.getSeconds();
		return stdtime;
	}
	
	function showtime(){
		var nowtime = (new Date()).getTime();
		if(Math.abs(nowtime-CHAT.lasttime)>10000){
			var time = stdTime();
			var html = '';
			html += '<div class="msg-system">';
			html += time;
			html += '</div>';
			var section = d.createElement('section');
			section.className = 'system J-mjrlinkWrap J-cutMsg';
			section.innerHTML = html;
			CHAT.msgObj.appendChild(section);
			CHAT.scrollToBottom();
		}
		CHAT.lasttime = nowtime;
	}
	
	function uintToString(uintArray) {
	    var encodedString = String.fromCharCode.apply(null, uintArray),
	    decodedString = decodeURIComponent(escape(encodedString));
	    return decodedString;
	}
	
	//通过“回车”提交用户名
	d.getElementById("username").onkeydown = function(e) {
		e = e || event;
		if (e.keyCode === 13) {
			CHAT.usernameSubmit();
		}
	};
	//通过“回车”提交信息
	d.getElementById("content").onkeydown = function(e) {
		e = e || event;
		if (e.keyCode === 13) {
			CHAT.submit();
		}
	};
	
})();