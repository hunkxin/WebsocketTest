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
		//让浏览器滚动条保持在最低部
		scrollToBottom:function(){
			w.scrollTo(0, this.msgObj.clientHeight);
		},
		//退出，本例只是一个简单的刷新
		logout:function(){
			//this.socket.disconnect();
			if(this.socket!=null){
				this.socket.close();
				this.socket=null;
			}
			location.reload();
		},
		//提交聊天消息内容
		submit:function(){
			var content = d.getElementById("content").value;
			if(content != ''){
				var obj = {
					action:"message",
					userid: this.userid,
					username: this.username,
					time:stdTime(),
					content: content
				};
				//this.socket.emit('message', obj);
				this.socket.send(JSON.stringify(obj));
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
			var onlineUsers = o.onlineUsers;
			//当前在线人数
			var onlineCount = o.onlineCount;
			//新加入用户的信息
			var user = o.username;
			var time = o.time;
				
			//更新在线人数
			var userhtml = '';
			var separator = '、';
			for(var i=0;i<onlineUsers.length;i++) {
				userhtml += onlineUsers[i].username;
				if(i==onlineUsers.length-1)
					separator = '';
				userhtml += separator;
		    }
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
				initdrop();
			}
			return false;
		},
		init:function(username){
			/*
			客户端根据时间和随机数生成uid,这样使得聊天室用户名称可以重复。
			实际项目中，如果是需要用户登录，那么直接采用用户的uid来做标识就可以
			*/
			this.userid = this.genUid();
			this.username = username;
			
			d.getElementById("showusername").innerHTML = this.username;
			this.msgObj.style.minHeight = (this.screenheight - db.clientHeight + this.msgObj.clientHeight) + "px";
			this.scrollToBottom();
			
			//连接websocket后端服务器
			this.socket = new WebSocket("ws://localhost:8383/WebsocketTest/websocket");
			
			//告诉服务器端有用户登录
			//this.socket.emit('login', {userid:this.userid, username:this.username});
			
			
			//监听新用户登录
			/*this.socket.on('login', function(o){
				CHAT.updateSysMsg(o, 'login');	
			});*/
			
			//监听用户退出
			/*this.socket.on('logout', function(o){
				CHAT.updateSysMsg(o, 'logout');
			});*/
			
			socketutil(this.socket);
		}
	};
	
	//监听消息发送
	function setMessageInnerHTML(obj){
		if(obj instanceof Blob){
			showtime();
			//alert(obj.size);
			var uinfosize = 0;
			var blobuinfosize = obj.slice(-8);
			var sizereader = new FileReader();
			sizereader.onload = function(evt) {
					uinfosize = parseInt(evt.target.result);
					var buinfo = {};
					var blobuinfo = obj.slice(-uinfosize-8,-8);
					//alert(blobuinfo.size);
					
					var blobct = obj.slice(0,obj.size-uinfosize);
					var contentDiv = document.createElement("img");
					//var contentDiv = document.createElement("video");
					contentDiv.height=100;
					var reader = new FileReader();
				    reader.onload = function(e) {
						  //alert(e.target.result);
						  //aVdo.play();
						  //window.URL.revokeObjectURL(obj_url);
						  contentDiv.src = e.target.result;
						  //contentDiv.play();
				    	};
				    //对于图片或视频流，可以使用readAsBinaryString()方法直接生成DataURL供元素调用
				    reader.readAsDataURL(blobct);
				    //reader.readAsBinaryString(obj);
				    //var isme = (CHAT.imguserid == CHAT.userid) ? true : false;
				    var uinforeader = new FileReader();
				    uinforeader.onload = function(e) {
				    	  var infoarray = new Uint8Array(e.target.result);
				    	  //alert(uintToString(infoarray));
				    	  buinfo = JSON.parse(uintToString(infoarray));
						  var isme = (buinfo.userid == CHAT.userid) ? true : false;
						  
						  var usernameDiv = document.createElement("span");
						  usernameDiv.innerHTML = isme?CHAT.username:buinfo.username;
						  //var usernameDiv = '<span>'+msg.username+'</span>';
						  var section = d.createElement('section');
						  if(isme){
							  section.className = 'user';
							  section.appendChild(contentDiv);
							  section.appendChild(usernameDiv);
						  } else {
							  section.className = 'service';
							  section.appendChild(usernameDiv);
							  section.appendChild(contentDiv);
						  }
						  CHAT.msgObj.appendChild(section);
				    	};
				    //如果Blob中包含字符串且不需要解码，则需要readAsBinaryString()方法解析
					//uinforeader.readAsBinaryString(blobuinfo);
				    //如果Blob中包含的字符串还需要解码，则需要readAsArrayBuffer()获得ArrayBuffer，并使用Uint8Array对象对其进行解析
				    uinforeader.readAsArrayBuffer(blobuinfo);
		    	};
		    sizereader.readAsBinaryString(blobuinfosize);
		}else{
			var msg = JSON.parse(obj);
			if(msg.action!=null&&(msg.action=="login"||msg.action=="logout")){
				CHAT.updateSysMsg(msg, msg.action);
			}else if(msg.action!=null&&msg.action=="message"){
				showtime();
				var isme = (msg.userid == CHAT.userid) ? true : false;
				var contentDiv = '<div>'+msg.content+'</div>';
				var usernameDiv = '<span>'+msg.username+'</span>';
				
				var section = d.createElement('section');
				if(isme){
					section.className = 'user';
					section.innerHTML = contentDiv + usernameDiv;
				} else {
					section.className = 'service';
					section.innerHTML = usernameDiv + contentDiv;
				}
				CHAT.msgObj.appendChild(section);
			}else if(msg.action!=null&&msg.action=="img"){
				CHAT.isimg=true;
				CHAT.imgusername=msg.username;
				CHAT.imguserid=msg.userid;
			}
		}
		
		//alert(CHAT.lasttime);
		CHAT.scrollToBottom();	
	};
	
	function socketutil(websocket){
      	//连接发生错误的回调方法
            websocket.onerror = function(){
            	//var logoutmsg = {action:"logout",userid:CHAT.userid, username:CHAT.username,time:stdTime()};
            	//websocket.send(JSON.stringify(logoutmsg));
            	alert("wrong!!!");
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
            websocket.onclose = function(){
            	//alert("close!!!");
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
	
	function initdrop(){
		var dropbox;

		dropbox = document.getElementById("inputdiv");
		dropbox.addEventListener("dragenter", dragenter, false);
		dropbox.addEventListener("dragover", dragover, false);
		dropbox.addEventListener("drop", drop, false);
	}

	function dragenter(e) {
		e.stopPropagation();
		e.preventDefault();
	}

	function dragover(e) {
		e.stopPropagation();
		e.preventDefault();
	}
		
	function drop(e) {
		e.stopPropagation();
		e.preventDefault();

		var dt = e.dataTransfer;
		var files = dt.files;

		handleFiles(files);
	}

	function handleFiles(files) {
		  for (var i = 0; i < files.length; i++) {
		    var file = files[i];
		    var videoType = /^image\//;
		    //var videoType = /^video\//;
		    
		    if ( !videoType.test(file.type) ) {
		      	continue;
		    }
		    
		   if(file.size>(1024*1024*10)){
			   for(var tmp in files){
				   tmp.close();
			   }
			   alert("图片大小超过10M！");
		   }else{
			   var obj = {
						action:"img",
						userid: CHAT.userid,
						username: CHAT.username,
						time:stdTime(),
						onlineCount: "",
						onlineUsers:null
					};
			   //var filecontent = file.slice();
			   //alert(file.type);
			   CHAT.socket.send(file);
			   //CHAT.socket.send(filecontent);
		   }
			
			//this.socket.emit('message', obj);
			
		    //var video = document.createElement("video");
		    //var video = document.getElementById("video");
		    //video.classList.add("obj");
		    //ideo.id="video";
		    //video.file=file;
		    
		    //video.height=100;
		    //var sc = document.createElement("source");
		    //var obj_url = window.URL.createObjectURL(file);
		    //sc.src = obj_url;
		    //sc.type="video/mp4";
		    //video.appendChild(sc);
		    
		    /*video.onload = function(e) {
		        window.URL.revokeObjectURL(obj_url);
		      }*/
		    
		    // 假设 "preview" 是将要展示图片的 div
		    //preview = document.getElementById("preview");
		    //preview.appendChild(video);
		    //video.src = obj_url;
		    //video.play();
		    //window.URL.revokeObjectURL(obj_url);
		    
		    /*var reader = new FileReader();
		    //alert(file.size);
		    reader.onload = function(e) {
				  var content = e.target.result; 
				  alert(content);
				  //aVdo.play();
				  //window.URL.revokeObjectURL(obj_url);
				  var obj = {
							action:"message",
							userid: this.userid,
							username: this.username,
							time:stdTime(),
							content: content
						};
				  
				  //this.socket.emit('message', obj);
				  //CHAT.socket.send(JSON.stringify(obj));
				  CHAT.socket.send(content);
				  d.getElementById("content").value = '';
		    	};
		    //reader.readAsDataURL(file);
		    reader.readAsBinaryString(file);*/
		  }
	}
})();