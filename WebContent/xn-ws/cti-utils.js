
 
var MSGTYPE={
   EVENT:1,//事件
   CMD:2,//命令
   CMDRES:3//命令执行返回
};

var CONFTYPE={
    CONFSTART:0,//开始三方会议
    ADDNEWPART:1,//增加会议新成员
    CONF_BRIDGE:2//会议桥
};
var CONFACTION={
    KICKALL:0,//踢除所有会议成员
    MUTEALL:1,//静音所有会议成员
    MUTE:2,//静间会议成员
    UNMUTEALL:3,//取消所有会议成员静音
    UNMUTE:4,//取消会议成员静音
    KICK:5,//踢除
    LOCK:6,//加锁
    UNLOCK:7//取消锁
};
var MSGCLASS={
    CMD_MakeCall:500,//外呼
    CMD_Agentlogin:501,//座席登录
    CMD_Agentloginoff:502,//座席登出
    CMD_Hangup:503,//挂机/强拆
    CMD_Hold:504,//呼叫操持
    CMD_ConferenceCall:505,//会议
    CMD_PickupCall:506,//代接
    CMD_TransferCall:507,//转接
    CMD_Insert:508,//强插
    CMD_RecordControl:509,//录音控制
    CMD_Monitor:510,//监听
    CMD_Makebusy:511,//置忙
    CMD_ConfAction:512,//会议控制操作
    CMD_PbxCmd:513,//PBXCLI命令
    CMD_PbxAmiLogin:514//登录到PBX
};
var MSGRESCLASS={
    CMDRES_MakeCall:600,
    CMDRES_Agentlogin:601,
    CMDRES_Agentloginoff:602,
    CMDRES_Hangup:603,
    CMDRES_Hold:604,
    CMDRES_ConferenceCall:605,
    CMDRES_PickupCall :606,
    CMDRES_TransferCall:607,
    CMDRES_Insert:608,
    CMDRES_Listen:609,
    CMDRES_Monitor:610,
    CMDRES_Makebusy:611,
    CMDRES_ConfAction:612,
    CMDRES_PbxCmd:613,
    CMDRES_PbxAmiLogin:614
};
var EVENTCLASS={
    EVENT_ExtStateChanged:100,
    EVENT_AgentStateChanged:101,
    EVENT_ChannelStateChanged:102,
    EVENT_AgentCallined:103,
    EVENT_MeEventHanpend:105,
    EVENT_NewQueueEvent:106,
    EVENT_HangupEvent:107,
	EVENT_WXChatIn:201,
	EVENT_WXChatTransfer:202
};
var CMDRESCODE=
{
    AGENTLOGINOK: 0,//登录成功
    AGENTLOGINFAIL_ERR_AGENTID: 1,//登录失败，无效座席工号/调度员工号
    AGENTLOGINFAIL_ERR_PWD: 2,//登录失败，无效密码
    AGENTLOGIN_NO_LOGINEXT: 3,//登录失败，登录的分机，没有被系统监控
    AGENTLOGIN_LOGINEXT_ISNOTONLINE: 31,//登录失败,该座席的分机没有在线
    AGENTLOGIN_NO_QUEUE: 4,//无效登录队列
    AGENTLOGIN_NO_QUEUEMEMBER: 5,//座席不是队列的成员
    AGENTLOGIN_HAVELOGIN: 6,//座席已经登录
    AGENTLOGIN_FORCELOGOFF: 61,//座席被强制登出
    AGENTLOGOFF_NOLOGIN: 7,//登出失败,因为座席没有登录
    AGENT_NOEXIST: 8,//座席不存在
    AGENTLOGOFF_OK: 9,//登出成功
    AGENTLOGOFF_NO_QUEUE: 10,//无效登出队列
    AGENT_HAVENO_BASE_ACTION: 11,//座席没有基本控制命令权限
    AGENT_HAVENO_ADMIN_ACTION: 12,//没有管理员权限
    AGENT_HAVENO_ADVANCE_CALL_ACTION: 14,//没有高级呼叫权限
    AGENT_OBJ_NOFOUND: 15,//座席对象没有找到
    AGENT_CANNOT_BE_ACTIONED_NOLOGIN: 16,//座席操作无效，座席没有登录
    AGENT_CANNOT_BE_MAKEIDLE_ISIDLE: 17,//座席示闲状态下，不能示闲操作
    AGENT_CANNOT_BE_MAKEBUSY_ISBUSY: 18,//座席置忙状态下,不能做置忙操作
    AGENT_CANNOT_BE_MAKEBUSY_NOLOGINQUEUE: 19,//座席不能置忙,座席没有队列中
    AGENT_MAKEBUSY_OK: 20,//置忙示闲成功
    EXT_OBJ_NOFOUND: 21,//分机对象没有找到
    CHANNEL_OBJ_NOFOUND: 22,//通道对象没有找到
    RELATE_CHANNEL_ISNOTFOUND: 23,//关连通道没有找到
    EXT_ISNOIDLE: 24,//分机不空闲
    PBX_ISNOT_HAVE_THISFUN: 25,//PBX没有这个命令
    EXT_ISNOLINKSTATE: 26,//分机不是通话状态
    EXT_ISNO_MEETING_STATE: 27,//分机不是在会议状态
    NOT_FOUND_MEETROOM: 28,//没有找到会议室号码
    CONF_NOTFOUND_MEMBER: 40,//会议没有该成员
    CONF_NOTFOUND_MEETROOM_OBJ: 41,//没有找到会议对象
    CONF_CANNOTLOCK_MEETISNOTINMEETING: 42,//不能锁定会议室，因为会议室没有在使用状态
    AGENT_CANNOTHOLD_ISNOLINK: 43,//不能呼叫保持因为没有在通话状态
    AGENT_CANNOTUNHOLD_ISNOHOLD: 44,//不能取消保持因为当前状态不为HOLD
    PBXOBJISNULL: 100,//PBX对象无效
    PBXINNERERR: 200//PBX内部错误


};
var AGENTSTATE = {
	    AGENT_NOLOGIN: 0,
        AGENT_IDLE:1,
        AGENT_NOREADY:2,
        AGENT_CALL_INITIALIZE:3,
        AGENT_CALL_RINGBACK:4,
        AGENT_CALL_RINGING:5,
        AGENT_CALL_UP:6,
        AGENT_CALL_DISCONNECT:7,
        AGENT_CALL_LINK:8,
        AGENT_CALL_MEETING:9,
        AGENT_CALL_INSERT:10,
        AGENT_CALL_HOLD:11,
        AGENT_CALL_MONITOR:12
};


var CHANNELSTATE= {
    CHANNEL_INITIALIZE:3,

    CHANNEL_RINGBACK:4,
    CHANNEL_RINGING:5,
    CHANNEL_UP:6,
    CHANNEL_DISCONNECT:7,
    CHANNEL_LINK:8,
    CHANNEL_HANGUP:1,
    CHANNEL_MEETING:9,
    CHANNEL_INSERT:10,
    CHANNEL_HOLD:11,
    CHANNEL_MONITOR:12


};
var  EXTSTATE={
    EXT_INITIALIZE:3,
    EXT_IDLE:1,
    EXT_RINGBACK:4,
    EXT_RINGING:5,
    EXT_UP:6,
    EXT_DISCONNECT:7,
    EXT_LINK:8,
    EXT_MEETING:9,
    EXT_INSERT:10,
    EXT_DISABLE:100,
    EXT_HOLD:11,
    EXT_MONITOR:12
};

 var ACTIONTYPE={
	SELF:0,
    SPECIFY_EXT:1,
    SPECIFY_AGENT:2
};
var  TRANSFERTYPE={
    BLIND:1,
    ATTENDED:2,
	ATTENDEDOTHER:3
};

Map = function(){
	this.elements = new Array();
 
	this.size = function() {
		return this.elements.length;
	}
 
	this.isEmpty = function() {
		return (this.elements.length < 1);
	}

	this.clear = function() {
		this.elements = new Array();
	}

	this.put = function(_key, _value) {
		this.elements.push({key:_key, value:_value});
	}
 
	this.remove = function(_key) {
		var bln = false;
		try{   
			for (i = 0; i < this.elements.length; i++){  
				if (this.elements[i].key == _key){
					this.elements.splice(i, 1);
					return true;
				}
			}
		}catch(e){
			bln = false;    
		}
		return bln;
	}

	this.get = function(_key) {
		try{   
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					return this.elements[i].value;
				}
			}
		}catch(e) {
			return null;   
		}
		return null;
	}
 
	this.element = function(_key) {
		if (_key < 0 || _key >= this.elements.length){
			return null;
		}
		return this.elements[_key];
	}

	this.containsKey = function(_key) {
		var bln = false;
		try{
			for (i = 0; i < this.elements.length; i++){  
				if (this.elements[i].key == _key){
					bln = true;
				}
			}
		}catch(e) {
			bln = false;    
		}
		return bln;
	}

	this.containsValue = function(_value){
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {  
				if (this.elements[i].value == _value){
					bln = true;
				}
			}
		}catch(e) {
			bln = false;    
		}
		return bln;
	}
 
	this.values = function() {
		var arr = new Array();
			for (i = 0; i < this.elements.length; i++) {  
				arr.push(this.elements[i].value);
			}
		return arr;
	}
 
	this.keys = function() {
		var arr = new Array();
		for (i = 0; i < this.elements.length; i++) {  
			arr.push(this.elements[i].key);
		}
		return arr;
	}
};

var showMessage = function(msg){
	if(typeof art != 'undefined'){
		art.dialog({
			lock: true,
		    width: '24em',
		    time: 5000,
		    window: 'top',
		    content: msg
		});
	}else{
		alert(msg);
	}
}

var GetMsgPara= function(src, key, s, e, sdelenum){
        if(src=="")return "";
        var f1,f2,f3;

       //alert(src.indexOf(key+s));
        if(src.indexOf(key+s)<0) return "";

        f1=src.indexOf(key+s,0);
        
        //if(f1==-1) return "";
        f2=src.indexOf(s,f1);
        
        //if(f2==-1) return "";
        f3=src.indexOf(e,f1);
         
        //if(f3==-1) return "";

        //if((f3-f2-1)<=0)return "";
        //if((f3-f2-1)>src.size())return "";

         var res=src.substr(f2+1+sdelenum,f3-f2-1).replace("\r","").replace("\n","");/* 对于没有\r\n的字符串也不会有影响 */
        
         //res=res.trimmed();
        return res+"";
}

var GetBodyItem =function(src,key)/* 获取key=val&中的val值 */
{
	return GetMsgPara(src,key,'=','&',0);
}
var showDiv = function(divId){
	$("#"+divId).fadeIn('fast');
}

var hideDiv = function(divId){
	$("#"+divId).fadeOut('fast');
}

var checkLineId = function(lineId) {
	if (lineId == null) {
		return;
	}
	if (lineId != 0 && lineId != 1) {
		showMessage("无效的线路号");
		return;
	}
}

var checkPhoneNumber = function(phoneNumber){
	if(phoneNumber==null || phoneNumber.length==0)
		return false;
	else{
		var validNumber = "*#0123456789";
		var phoneNumber = $("#phoneNumber").val();
		phoneNumber = $.trim(phoneNumber);
		for (var i = 0; i < phoneNumber.length; i++) {
			var c = phoneNumber.charAt(i);
			if (validNumber.indexOf(c) == -1) {
				showMessage("输入的电话号码含有不符合规范,请检查是否含有空格或者其他非数字字符.");
				return false;
			}
		}
		return true;
	}
}

var getTenantIdByThisDN = function(thisDN) {
	if (thisDN == null || thisDN.length == 0) 
	  return null;
	else{
	  return thisDN.substring(0,thisDN.indexOf('_'));
	}
}