
///////////////////////////////////////////////////////////
///构造函数
///////////////////////////////////////////////////////////
function ClassXnCtiClient()
{
	this.TvsUrl="localhost";
	this.UserGuid=null;
    this.IP = null;
    this.Port = null;
    this.UserID = null;
    this.thisDN= null;
    this.UserPWD = null;
    this.UserExt = null;
    this.UserGroup = null;
    this.IsConnect_CTI = 0;
    this.Status = 0;
    this.SeatStatus = 0;
    this.CallID = '';
    this.Caller = '';
    this.Called = '';
    this.ExtDirection = '';
    this.IsDebug = false;   
    this.IsBusy = 0;
    this.IsNotDisturb = 0;
    this.dwFreq = 10;
    this.dwDuration = 5000;
    this.TelMessageSign = 0;
    this.LevelMessageSign = 0;
    this.websocket = null;

    this.msgheadend='|';
    this.msgheadsplit=":";
    this.msgbodyend="&";
    this.msgbodysplit="=";
    this.msgend="@";

  
  
    

    
}



///////////////////////////////////////////////////////////
///检测连接
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.CheckConnect = function()/* object.prototype.att = function(),function中的第一层this指的是object*/
{   
    if(this.IsConnect_CTI == 0)
    {       
        alert("请先连接CTI服务器!");
        return false;
    }
    return true;
}

///////////////////////////////////////////////////////////
///显示测试信息
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ShowTestMessage = function(msg)/* 在html元素集合中查找id为txtOcxInfo的元素并赋值；最好用 document.getElementById代替*/
{
    if(this.IsDebug)
    {
        var obj = document.all.txtOcxInfo;
        if(obj != null)
        {
            obj.value = msg;
        }
    }
}
//通过TVS3000接口得到 CTI 服务信息,跨域ajax
ClassXnCtiClient.prototype.CtiConnectEx = function(tvsrooturl,agentguid,agentpwd,ctiobj)
{ 
   this.TvsUrl=tvsrooturl;
   var url=tvsrooturl+"/tvsapi/tvs_ctiapi_c/getRealCtiHostInfo/1/";
   url=url+agentguid;//并未进行密码验证？
   $.ajax({
     url:url,
     dataType:'jsonp',//虽然数据格式是jsonp，但没有指定'jsonp'参数，所以和'json'格式是一样的
     processData: false, 
     type:'get',
     success:function(data){
		 
	   if(data.state)
	   {
		   window.agentid=data.result.agentid;
		   window.wsport=data.result.wsport;
		   window.ctihost=data.result.ctihost;
		   window.tslport=data.result.tslport;
		   ctiobj.CtiConnect(window.ctihost,window.wsport); 
		   ctiobj.UserID=window.agentid;
		   ctiobj.UserGuid=agentguid;
		   ctiobj.UserPWD=agentpwd;
		   ctiobj.UserExt=data.result.agentext;;
		   ctiobj.UserID=window.agentid;
		   ctiobj.IP=window.ctihost;
		   ctiobj.Port=window.wsport;
	   }else{
		   alert(data.message);
		   return;
	   }
       
     },
     error:function(XMLHttpRequest, textStatus, errorThrown) {
       alert(XMLHttpRequest.status);
       alert(XMLHttpRequest.readyState);
       alert(textStatus);
     }});
	 
	     
   
}
ClassXnCtiClient.prototype.CtiConnect = function(IP, Port)/* 重复嵌套定义？ */
{  
  this.CtiConnect(this.IP,this.Port);
}

///////////////////////////////////////////////////////////
///连接CTI服务器
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.CtiConnect = function(IP, Port)
{   
    this.IP = IP;
    this.Port = Port;
     
    if(this.IP == null)
    {
        alert('服务器IP地址不能为空！');
        return ;
    }
    if(this.Port == null)
    {
        this.Port = '5138';
    }
    if(this.websocket!=null)
    {
        if (this.websocket.isConnected()) {
			alert('CTI服务器已连接不可重复连接');
			return;
		} 
    }
   
    
     
    var xncti=this;
    if (ws.browserSupportsWebSockets()) {//判断浏览器是否自带ws功能
        this.websocket = new ws.webSocketClient();
        
        //websocket.setReliabilityOptions(ws.RO_ON);
    } else {
        var lMsg = ws.MSG_WS_NOT_SUPPORTED;
        showMessage(lMsg);
    }
    var lURL = ws.getServerURL("ws", this.IP, this.Port, "/websocket");
    var thisDN = this.thisDN;
    
    var data = {"request":"CtiConnect","thisDN":thisDN};
    var lRes = this.websocket.logon(lURL, thisDN, $.toJSON(data), {/*连接ws服务器并发送登录消息 {type: "login", thisDN: thisDN, message: message } */
            //onOpen callback /* websocket事务设定 */
            OnOpen: function(aEvent) {
                xncti.websocket.startKeepAlive({ immediate: false,interval :30000 });
                xncti.IsConnect_CTI=true;
                xncti.CTIConnectedEvent();     
            },
            //onMessage callback
            OnMessage: function(aEvent) {
                xncti.parseMessage(aEvent.data);
            },
            //onClose callback
            OnClose: function(aEvent) {
                xncti.websocket.stopKeepAlive();
                xncti.IsConnect_CTI=false;
                xncti.CTIDisConnectedEvent();
            }
        });
    
}
ClassXnCtiClient.prototype.send=function(data) {
       
        if (this.websocket) {
           
             this.websocket.sendToken(data);
/*
              var lToken = {
                thisDN: this.thisDN,
                type: "request",
                message: $.toJSON(data)
            };
            */
        } else {
            showMessage("disconnect from cti server while send message");
        }
}

ClassXnCtiClient.prototype.parseMessage = function(data){
        var msttype,msg,msgbody;
        msttype=parseInt(GetMsgPara(data,"MSGTYPE",':','|',0));
        msg=parseInt(GetMsgPara(data,"MSG",':','|',0));
        msgbody=GetMsgPara(data,"MSGBODY",':','@',0);
        
        if(msttype==""||msg==""||msgbody=="")
          {
                
                alert("ERROR MSG="+data);

          }else
          {
              switch(msttype)
              {
                case MSGTYPE.EVENT:
                {
                    switch(msg)
                    {
                        case EVENTCLASS.EVENT_ExtStateChanged:
                        {
                           var deviceid=GetBodyItem(data,"deviceid");
                           var devicestate=GetBodyItem(data,"devicestate");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var laststate=GetBodyItem(data,"laststate");
                           var floatdata=GetBodyItem(data,"floatdata");
                           this.EVENT_ExtStateChanged(deviceid,devicestate,pbxid,laststate,floatdata);
                        }
                        break;
                        case EVENTCLASS.EVENT_AgentStateChanged:
                        {
                           var agentid=GetBodyItem(data,"agentid");
                           var agentstate=GetBodyItem(data,"agentstate");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var laststate=GetBodyItem(data,"laststate");
                           var floatdata=GetBodyItem(data,"floatdata");
                           this.EVENT_AgentStateChanged(agentid,agentstate,pbxid,laststate,floatdata);
                        }
                        break;
                        case EVENTCLASS.EVENT_ChannelStateChanged:
                        {
                           var channel=GetBodyItem(data,"channel");
                           var channelstate=GetBodyItem(data,"channelstate");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var laststate=GetBodyItem(data,"laststate");
                           var floatdata=GetBodyItem(data,"floatdata");
                           var ext=GetBodyItem(data,"ext");
                           this.EVENT_ChannelStateChanged(channel,channelstate,pbxid,laststate,ext,floatdata);
                        }
                        break;
                        case EVENTCLASS.EVENT_AgentCallined:
                        {
                           var caller=GetBodyItem(data,"caller");
                           var callerchannel=GetBodyItem(data,"callerchannel");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var fromqueue=GetBodyItem(data,"fromqueue");
                           var callid=GetBodyItem(data,"callid");
                           var agentid=GetBodyItem(data,"agentid");
                           var floatinfo=GetBodyItem(data,"floatinfo");
                           var context=GetBodyItem(data,"context");
                           var exten=GetBodyItem(data,"exten");
                           this.EVENT_AgentCallined(caller,pbxid,callerchannel,fromqueue,callid,agentid,context,exten,floatinfo);
                        }
                        break;
                        case EVENTCLASS.EVENT_MeEventHanpend:
                        {
                           var eventtype=GetBodyItem(data,"eventtype");
                           var me=GetBodyItem(data,"me");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var member=GetBodyItem(data,"member");
                           var memidx=GetBodyItem(data,"memidx");
                           var activechannel=GetBodyItem(data,"activechannel");
                           var floatinfo=GetBodyItem(data,"floatinfo");
                           this.EVENT_MeEventHanpend(pbxid,eventtype,me,member,memidx,activechannel,floatinfo);
                        }
                        break;
                        case EVENTCLASS.EVENT_NewQueueEvent:
                        {
                           var queue=GetBodyItem(data,"queue");
                           var join=GetBodyItem(data,"join");
                           var joincaller=GetBodyItem(data,"joincaller");
                           var jcchannel=GetBodyItem(data,"jcchannel");
                           var callid=GetBodyItem(data,"callid");
                           var pbxid=GetBodyItem(data,"pbxid");
                           var floatinfo=GetBodyItem(data,"floatinfo");
                           this.EVENT_NewQueueEvent(queue,join,joincaller,jcchannel,callid,pbxid,floatinfo);
                        }
                        break;
						case EVENTCLASS.EVENT_WXChatIn:
                        {
                           var agentid=GetBodyItem(data,"toagentid");
                           var content=GetBodyItem(data,"content");
                           var msgtype=GetBodyItem(data,"msgtype");
						   var fromwxuserid=GetBodyItem(data,"fromwxuserid");
						   var fromghid=GetBodyItem(data,"fromghid");
						   
                           
                           this.EVENT_WXChatIn(fromghid,fromwxuserid,agentid,content,msgtype);
                        }
                        break;
                    }
                }
                break;
                case MSGTYPE.CMD:
                break;
                case MSGTYPE.CMDRES:
                {
                           var imsg=GetBodyItem(data,"imsg");
                           var rescode=GetBodyItem(data,"rescode");
                           var pbxrescode=GetBodyItem(data,"pbxrescode");
                           var res=GetBodyItem(data,"res");
                           var actionid=GetBodyItem(data,"actionid");
                           this.EVENT_CMDRES(imsg,rescode,pbxrescode,res,actionid);
                }
                break;
              }
          }
    }



///////////////////////////////////////////////////////////
///断开与CTI服务器的连接
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.CtiDisconnect = function()
{   
    if (websocket.isConnected()) {
            websocket.stopKeepAlive();
            websocket.close();
        }
}
ClassXnCtiClient.prototype.CheckWS = function()
{   
    if (!this.websocket.isConnected()) {
			alert('请先连接上CTI服务器！');
			return false; 
        }
        return true;
}
ClassXnCtiClient.prototype.MsgHead = function(msgtype,msg,actionid)/* "MSGTYPE":msgtype|"MSG":msg|"MSGBODY":"actionid"=actionid& */
{
  var msghead="MSGTYPE"+this.msgheadsplit+msgtype;
  msghead+=this.msgheadend;
  msghead+="MSG"+this.msgheadsplit+msg;
  msghead+=this.msgheadend;
  msghead+="MSGBODY"+this.msgheadsplit;
  msghead+="actionid";
  msghead+=this.msgbodysplit;
  msghead+=actionid;
  msghead+=this.msgbodyend;

  return msghead;

}
ClassXnCtiClient.prototype.MsgBodyItem = function(key,value)/* key=value&*/
{
  var msgbodyitem=key;
   
  msgbodyitem+=this.msgbodysplit;
  msgbodyitem+=value;
  msgbodyitem+=this.msgbodyend;

  return msgbodyitem;

}
//通过guid自动获取工号登录,并且传递权限 ，和salt
ClassXnCtiClient.prototype.AgentLoginEx = function(UserExt,Level,Salt)
{
	if(!this.CheckWS())
    {
        return false;
    }
	//alert(this.UserID);
    if(this.UserID == null)
    {
        alert('登录工号不能为空！');
        return false;
    }
    if(this.UserPWD == null)
    {
        alert('登录密码不能为空！');
        return false;
    }
    
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Agentlogin,MSGCLASS.CMD_Agentlogin);
    cmd+=this.MsgBodyItem("agentid",this.UserID);
    cmd+=this.MsgBodyItem("agentpwd",this.UserPWD);
    cmd+=this.MsgBodyItem("loginext",UserExt);
	cmd+=this.MsgBodyItem("level",Level);
	cmd+=this.MsgBodyItem("salt",Salt);
    cmd+=this.msgend;

    this.send(cmd);
}

///////////////////////////////////////////////////////////
///坐席靠工号登录,并且传递权限 ，和salt
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AgentLogin = function(UserID, UserPWD, UserExt,Salt,Level)
{
    this.UserID = UserID;
    this.UserPWD = UserPWD;
    this.UserExt = UserExt;
    
    if(!this.CheckWS())
    {
        return false;
    }
    if(this.UserID == null)
    {
        alert('登录工号不能为空！');
        return false;
    }
    if(this.UserPWD == null)
    {
        alert('登录密码不能为空！');
        return false;
    }
    if(this.UserExt == null)
    {
        alert('分机号码不能为空！');
        return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Agentlogin,MSGCLASS.CMD_Agentlogin);
    cmd+=this.MsgBodyItem("agentid",UserID);
    cmd+=this.MsgBodyItem("agentpwd",UserPWD);
    cmd+=this.MsgBodyItem("loginext",UserExt);
	cmd+=this.MsgBodyItem("level",Level);
	cmd+=this.MsgBodyItem("salt",Salt);
    cmd+=this.msgend;

    this.send(cmd);
}
///////////////////////////////////////////////////////////
///坐席靠工号登录
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AgentLogin = function(UserID, UserPWD,UserExt)/* 重载函数，有几种登陆方式 */
{
    this.UserID = UserID;
    this.UserPWD = UserPWD;
    this.UserExt = UserExt;
    
    if(!this.CheckWS())
    {
        return false;
    }
    if(this.UserID == null)
    {
        alert('登录工号不能为空！');
        return false;
    }
    if(this.UserPWD == null)
    {
        alert('登录密码不能为空！');
        return false;
    }
    if(this.UserExt == null)
    {
        alert('分机号码不能为空！');
        return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Agentlogin,MSGCLASS.CMD_Agentlogin);
    cmd+=this.MsgBodyItem("agentid",UserID);
    cmd+=this.MsgBodyItem("agentpwd",UserPWD);
    cmd+=this.MsgBodyItem("loginext",UserExt);
    cmd+=this.msgend;

    this.send(cmd);
}



///////////////////////////////////////////////////////////
///坐席退出
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AgentLogout = function()
{   
    if(!this.CheckWS())
    {
        return false;
    }

    if(this.UserID == null)
    {
        alert('AgentID is not null!');
        return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Agentloginoff,MSGCLASS.CMD_Agentloginoff);
    cmd+=this.MsgBodyItem("agentid","");
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///代理其它坐席退出
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_AgentLogout = function(UserID)
{   
    if(!this.CheckWS())
    {
        return false;
    }
    if(UserID == null)
    {
        alert('AgentID is not null！');
        return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Agentloginoff,MSGCLASS.CMD_Agentloginoff);
    cmd+=this.MsgBodyItem("agentid",UserID);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///发起呼叫
///floatdata 字母数字组合，不能有特殊字符，floatdata之间以.分隔
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.MakeCall = function(TelNumber,caid,floatdata)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    if(TelNumber == null || TelNumber == '')
    {
        alert('被叫号码不能为空！');
        return false;
    }
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_MakeCall,caid);
    
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("callnum",TelNumber);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("floatdata",floatdata);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///发起呼叫
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_MakeCall = function(deviceid,TelNumber,at)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    if(TelNumber == null || TelNumber == '')
    {
        alert('被叫号码不能为空！');
        return false;
    }
    if(deviceid==null)
    {
       alert('主叫设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_MakeCall,MSGCLASS.CMD_MakeCall);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("callnum",TelNumber);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///保持呼叫，让对方听音乐
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.HoldCall = function()
{
     if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(this.UserID==null)
    {
       alert('主叫设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hold,MSGCLASS.CMD_Hold);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    //cmd+=this.MsgBodyItem("agentid",agentid);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("ifhold",1);
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///保持呼叫，让对方听音乐
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_HoldCall = function(deviceid,at, agentid,ifhold)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    if(agentid == null || agentid == '')
    {
        alert('被保持的座席不能为空！');
        return false;
    }
    if(deviceid==null)
    {
    	alert('主叫设备不能为空！');
    	return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hold,MSGCLASS.CMD_Hold);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("agentid",agentid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("ifhold",ifhold);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///恢复呼叫
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.RetriveCall = function()
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('主叫设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hold,MSGCLASS.CMD_Hold);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    //cmd+=this.MsgBodyItem("agentid",agentid);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("ifhold",0);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///恢复呼叫
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_RetriveCall = function(deviceid,at, agentid,ifhold)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    if(agentid == null || agentid == '')
    {
        alert('被取回呼叫的座席不能为空！');
        return false;
    }
    if(this.UserID==null)
    {
       alert('主叫设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hold,MSGCLASS.CMD_Hold);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("agentid",agentid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("ifhold",ifhold);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///座席置忙，因暂时离开座位或处理别的事情而不想接电话
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.MakeBusy = function(ifbusy)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Makebusy,MSGCLASS.CMD_Makebusy);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("ifbusy",ifbusy);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///座席置忙，因暂时离开座位或处理别的事情而不想接电话
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_MakeBusy = function(deviceid,ifbusy,at)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(deviceid==null)
    {
       alert('座席设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Makebusy,MSGCLASS.CMD_Makebusy);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("ifbusy",ifbusy);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.msgend;
    this.send(cmd);
}


///////////////////////////////////////////////////////////
///离座
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ATSetLeaveSeat = function(cause)
{
     
}

///////////////////////////////////////////////////////////
///取消离座
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ATSetNoLeaveSeat = function()
{
       
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.BlindTranCall = function(DestExt)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.BLIND);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AttendTranCall = function(DestExt)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.ATTENDED);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.BlindTranCallBySelfCallerid = function(DestExt,Callerid)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.BLIND);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
	cmd+=this.MsgBodyItem("callerid",Callerid);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AttendTranCallBySelfCallerid = function(DestExt,Callerid)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.ATTENDED);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
	cmd+=this.MsgBodyItem("callerid",Callerid);
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.AttendTranCallOther = function(DestExt)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.ATTENDEDOTHER);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.TranCall = function(DestExt,transfertype)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
    
    if(this.UserID==null)
    {
       alert('操作座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("transfertype",transfertype);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///转接电话，将电话转到别的坐席
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_TranCall = function(deviceid,DestExt,transfertype,at)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(deviceid==null)
    {
       alert('座席分机不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("transfertype",transfertype);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("destcallnum",DestExt);
    cmd+=this.msgend;
    this.send(cmd);
}

 

///////////////////////////////////////////////////////////
///转ivr，满意度调查的时候用
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ATTranCall_toIVR = function(IvrFlow,IvrNode)
{
    if(!this.CheckWS())
    {
        return false;
    }
    
     var deviceid=this.UserExt;
    if(deviceid==null)
    {
       alert('座席分机不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_TransferCall,MSGCLASS.CMD_TransferCall);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("transfertype",TRANSFERTYPE.BLIND);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE);
    cmd+=this.MsgBodyItem("destcallnum",IvrNode);
	cmd+=this.MsgBodyItem("context",IvrFlow);
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///软摘机
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Answer = function()
{
     
}


///////////////////////////////////////////////////////////
///软挂机
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Hangup = function()
{
   if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(this.UserID==null)
    {
       alert('座席不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hangup,MSGCLASS.CMD_Hangup);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///软挂机
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_Hangup = function(deviceid,at)
{
   if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(deviceid==null)
    {
       alert('被挂断分机不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Hangup,MSGCLASS.CMD_Hangup);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///代接
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.PickupCall = function(pickupdevice,pat)
{
   if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(this.UserID==null)
    {
       alert('座席不能为空！');
       return false;
    }
    if(pickupdevice==null)
    {
       alert('被代接设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_PickupCall,MSGCLASS.CMD_PickupCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserExt);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("pickupdevice",pickupdevice);
    cmd+=this.MsgBodyItem("pickuptype",pat);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///代接
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_PickupCall = function(deviceid,at,pickupdevice,pat)
{
   if(!this.CheckWS())
    {
        return false;
    }
    
     
    if(deviceid==null)
    {
       alert('代接设备不能为空！');
       return false;
    }
    if(pickupdevice==null)
    {
       alert('被代接设备不能为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_PickupCall,MSGCLASS.CMD_PickupCall);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("pickupdevice",pickupdevice);
    cmd+=this.MsgBodyItem("pickuptype",pat);
    cmd+=this.msgend;
    this.send(cmd);
}

///////////////////////////////////////////////////////////
///会议
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.StartConferenceCall = function(confpartlist,meetroom)
{
   if(!this.CheckWS())
    {
        return false;
    }
     
    if(this.UserID==null)
    {
       alert('发起会议的设备不能为空！');
       return false;
    }
    if(confpartlist==null)
    {
       alert('与会成员列表不能为空！');
       return false;
    }
    if(meetroom==null)
    {
       alert('会议室号码不为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_ConferenceCall,MSGCLASS.CMD_ConferenceCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("conftype",CONFTYPE.CONFSTART);
    cmd+=this.MsgBodyItem("confpartlist",confpartlist);
    cmd+=this.MsgBodyItem("meetroom",meetroom);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///会议
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ConferenceCall = function(ct,confpartlist,meetroom)
{
   if(!this.CheckWS())
    {
        return false;
    }
     
    if(this.UserID==null)
    {
       alert('发起会议的设备不能为空！');
       return false;
    }
    if(confpartlist==null)
    {
       alert('与会成员列表不能为空！');
       return false;
    }
    if(meetroom==null)
    {
       alert('会议室号码不为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_ConferenceCall,MSGCLASS.CMD_ConferenceCall);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("conftype",ct);
    cmd+=this.MsgBodyItem("confpartlist",confpartlist);
    cmd+=this.MsgBodyItem("meetroom",meetroom);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///会议
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_ConferenceCall = function(deviceid,at, ct,confpartlist,meetroom)
{
   if(!this.CheckWS())
    {
        return false;
    }
     
    if(deviceid==null)
    {
       alert('发起会议的设备不能为空！');
       return false;
    }
    if(confpartlist==null)
    {
       alert('与会成员列表不能为空！');
       return false;
    }
    if(meetroom==null)
    {
       alert('会议室号码不为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_ConferenceCall,MSGCLASS.CMD_ConferenceCall);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("conftype",ct);
    cmd+=this.MsgBodyItem("confpartlist",confpartlist);
    cmd+=this.MsgBodyItem("meetroom",meetroom);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///会议操作
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ConferenceAction = function(meetroom,ca,membercallnum)
{
   if(!this.CheckWS())
    {
        return false;
    }
     
   
    if(membercallnum==null)
    {
       alert('成员号码不能为空！');
       return false;
    }
    if(meetroom==null)
    {
       alert('会议室号码不为空！');
       return false;
    }
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_ConfAction,MSGCLASS.CMD_ConfAction);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("meetaction",ca);
    cmd+=this.MsgBodyItem("conftype",ct);
    cmd+=this.MsgBodyItem("membercallnum",membercallnum);
    cmd+=this.MsgBodyItem("meetroom",meetroom);
    cmd+=this.msgend;
    this.send(cmd);
}
///////////////////////////////////////////////////////////
///强插
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.InsertCall = function(inserteddevice,insertat)
{
    if(!this.CheckWS())
    {
        return false;
    }
     
   
    if(inserteddevice==null)
    {
       alert('被强插的设备不能为空！');
       return false;
    }
    
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Insert,MSGCLASS.CMD_Insert);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    cmd+=this.MsgBodyItem("conftype",ct);
    cmd+=this.MsgBodyItem("insertedtype",insertat);
    cmd+=this.MsgBodyItem("inserteddevice",inserteddevice);
    cmd+=this.msgend;
    this.send(cmd);  
}

///////////////////////////////////////////////////////////
///强插
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_InsertCall = function(deviceid,at,inserteddevice,insertat)
{
    if(!this.CheckWS())
    {
        return false;
    }
     
   if(deviceid==null)
    {
       alert('强插的设备不能为空！');
       return false;
    }
    
    if(inserteddevice==null)
    {
       alert('被强插的设备不能为空！');
       return false;
    }
    
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Insert,MSGCLASS.CMD_Insert);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("type",at);
    cmd+=this.MsgBodyItem("conftype",ct);
    cmd+=this.MsgBodyItem("insertedtype",insertat);
    cmd+=this.MsgBodyItem("inserteddevice",inserteddevice);
    cmd+=this.msgend;
    this.send(cmd);  
}
 ///////////////////////////////////////////////////////////
///监听
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.ListenCall = function(listeneddeviceid,listeneddevicetype)
{
    if(!this.CheckWS())
    {
        return false;
    }
     
   if(this.UserID==null)
    {
       alert('监听的设备不能为空！');
       return false;
    }
    
    if(inserteddevice==null)
    {
       alert('被监听的设备不能为空！');
       return false;
    }
    
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Monitor,MSGCLASS.CMD_Monitor);
    cmd+=this.MsgBodyItem("deviceid",this.UserID);
    cmd+=this.MsgBodyItem("type",ACTIONTYPE.SELF);
    
    cmd+=this.MsgBodyItem("listeneddevicetype",listeneddevicetype);
    cmd+=this.MsgBodyItem("listeneddeviceid",listeneddeviceid);
    cmd+=this.msgend;
    this.send(cmd);  
}
///////////////////////////////////////////////////////////
///监听
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.Admin_ListenCall = function(deviceid,at,listeneddeviceid,listeneddevicetype)
{
    if(!this.CheckWS())
    {
        return false;
    }
     
   if(deviceid==null)
    {
       alert('监听的设备不能为空！');
       return false;
    }
    
    if(inserteddevice==null)
    {
       alert('被监听的设备不能为空！');
       return false;
    }
    
    var cmd=this.MsgHead(MSGTYPE.CMD,MSGCLASS.CMD_Monitor,MSGCLASS.CMD_Monitor);
    cmd+=this.MsgBodyItem("deviceid",deviceid);
    cmd+=this.MsgBodyItem("type",at);
    
    cmd+=this.MsgBodyItem("listeneddevicetype",listeneddevicetype);
    cmd+=this.MsgBodyItem("listeneddeviceid",listeneddeviceid);
    cmd+=this.msgend;
    this.send(cmd);  
}
 
///////////////////////////////////////////////////////////
///CTI 连接成功
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.CTIConnectedEvent = function()
{

}

///////////////////////////////////////////////////////////
/// CTI 断开成功
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.CTIDisConnectedEvent = function()
{

}
///////////////////////////////////////////////////////////
///注册事件：分机状态 变化 
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_ExtStateChanged = function(deviceid,devicestate,pbxid,laststate,floatdata)
{

}

///////////////////////////////////////////////////////////
///注册事件：座席状态 变化 
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_AgentStateChanged = function(agentid,agentstate,pbxid,laststate,floatdata)
{

}

///////////////////////////////////////////////////////////
///注册事件：通道状态 变化 
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_ChannelStateChanged = function(channel,channelstate,pbxid,laststate,ext,floatdata)
{

}

///////////////////////////////////////////////////////////
///注册事件：会议消息 
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_MeEventHanpend = function(pbxid,eventtype,me,member,memidx,activechannel,floatinfo)
{

}


///////////////////////////////////////////////////////////
///注册事件：呼入选中座席消息
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_AgentCallined = function(caller,pbxid,callerchannel,fromqueue,callid,agentid,context,exten,floatinfo)
{

}
///////////////////////////////////////////////////////////
///注册事件：来电进入队列消息
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_NewQueueEvent = function(queue,join,joincaller,jcchannel,callid,pbxid,floatinfo)
{

}
///////////////////////////////////////////////////////////
///注册事件：有微信 消息
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_WXChatIn = function(fromghid,fromwxuserid,toagentid,content,msgtype)
{

}
///////////////////////////////////////////////////////////
///注册事件：操作结果事件
///////////////////////////////////////////////////////////
ClassXnCtiClient.prototype.EVENT_CMDRES= function(imsg,rescode,pbxrescode,res,actionid)
{
 
}

















