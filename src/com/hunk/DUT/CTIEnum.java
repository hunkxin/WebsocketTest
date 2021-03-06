package com.hunk.DUT;

public class CTIEnum {
	public static final String unknown = "Unknown";
	public static final String logout = "Logged Out";
	public static final String available = "Available";
	public static final String available_on_demand = "Available (On Demand)";
	public static final String onbreak = "On Break";
	public static final String Waiting = "Waiting";
	
	public static final int EVENT = 1;//事件
	public static final int CMD = 2;//命令
	public static final int CMDRES =3;//命令执行返回
	
	public static final int CMD_MakeCall = 500;//外呼
	public static final int CMD_Agentlogin = 501;//座席登录
	public static final int CMD_Agentloginoff = 502;//座席登出
	public static final int CMD_Hangup = 503;//挂机/强拆
	public static final int CMD_Hold = 504;//呼叫操持
	public static final int CMD_ConferenceCall = 505;//会议
	public static final int CMD_PickupCall = 506;//代接
	public static final int CMD_TransferCall = 507;//转接
	public static final int CMD_Insert = 508;//强插
	public static final int CMD_RecordControl = 509;//录音控制
	public static final int CMD_Monitor = 510;//监听
	public static final int CMD_Makebusy = 511;//置忙
	public static final int CMD_ConfAction = 512;//会议控制操作
	public static final int CMD_PbxCmd = 513;//PBXCLI命令
	public static final int CMD_PbxAmiLogin = 514;//登录到PBX
	public static final int CMD_Transfer_CANCEL= 515;
	public static final int CMD_Transfer_GOON= 516;
	public static final int CMD_Monitor_WISPERA= 517;
	public static final int CMD_Monitor_WISPERB= 518;
	public static final int CMD_Monitor_RESET= 519;
	public static final int CMD_THREE_WAY= 520;
	public static final int CMD_ObCall = 599;//自动外呼
	
	public static final int AGENTLOGINOK = 0;//登录成功
	public static final int AGENTLOGINFAIL_ERR_AGENTID = 1;//登录失败，无效座席工号/调度员工号
	public static final int AGENTLOGINFAIL_ERR_PWD = 2;//登录失败，无效密码
	public static final int AGENTLOGIN_NO_LOGINEXT = 3;//登录失败，登录的分机，没有被系统监控
	public static final int AGENTLOGIN_LOGINEXT_ISNOTONLINE = 31;//登录失败,该座席的分机没有在线
	public static final int AGENTLOGIN_NO_QUEUE = 4;//无效登录队列
	public static final int AGENTLOGIN_NO_QUEUEMEMBER = 5;//座席不是队列的成员
	public static final int AGENTLOGIN_HAVELOGIN = 6;//座席已经登录
	public static final int AGENTLOGIN_FORCELOGOFF = 61;//座席被强制登出
	public static final int AGENTLOGOFF_NOLOGIN = 7;//登出失败,因为座席没有登录
	public static final int AGENT_NOEXIST = 8;//座席不存在
	public static final int AGENTLOGOFF_OK = 9;//登出成功
	public static final int AGENTLOGOFF_NO_QUEUE = 10;//无效登出队列
	public static final int AGENT_HAVENO_BASE_ACTION = 11;//座席没有基本控制命令权限
	public static final int AGENT_HAVENO_ADMIN_ACTION = 12;//没有管理员权限
	public static final int AGENT_HAVENO_ADVANCE_CALL_ACTION = 14;//没有高级呼叫权限
	public static final int AGENT_OBJ_NOFOUND = 15;//座席对象没有找到
	public static final int AGENT_CANNOT_BE_ACTIONED_NOLOGIN = 16;//座席操作无效，座席没有登录
	public static final int AGENT_CANNOT_BE_MAKEIDLE_ISIDLE = 17;//座席示闲状态下，不能示闲操作
	public static final int AGENT_CANNOT_BE_MAKEBUSY_ISBUSY = 18;//座席置忙状态下,不能做置忙操作
	public static final int AGENT_CANNOT_BE_MAKEBUSY_NOLOGINQUEUE = 19;//座席不能置忙,座席没有队列中
	public static final int AGENT_MAKEBUSY_OK = 20;//置忙示闲成功
	public static final int EXT_OBJ_NOFOUND = 21;//分机对象没有找到
	public static final int CHANNEL_OBJ_NOFOUND = 22;//通道对象没有找到
	public static final int RELATE_CHANNEL_ISNOTFOUND = 23;//关连通道没有找到
	public static final int EXT_ISNOIDLE = 24;//分机不空闲
	public static final int PBX_ISNOT_HAVE_THISFUN = 25;//PBX没有这个命令
	public static final int EXT_ISNOLINKSTATE = 26;//分机不是通话状态
	public static final int EXT_ISNO_MEETING_STATE = 27;//分机不是在会议状态
	public static final int NOT_FOUND_MEETROOM = 28;//没有找到会议室号码
	public static final int CONF_NOTFOUND_MEMBER = 40;//会议没有该成员
	public static final int CONF_NOTFOUND_MEETROOM_OBJ = 41;//没有找到会议对象
	public static final int CONF_CANNOTLOCK_MEETISNOTINMEETING = 42;//不能锁定会议室，因为会议室没有在使用状态
	public static final int AGENT_CANNOTHOLD_ISNOLINK = 43;//不能呼叫保持因为没有在通话状态
	public static final int AGENT_CANNOTUNHOLD_ISNOHOLD = 44;//不能取消保持因为当前状态不为HOLD
	public static final int OBCALLPJ_ISALREADY_ON = 45;//外呼项目已经在运行中
	public static final int OBCALLPJ_ISALREADY_PAUSE = 46;//外呼项目未运行
	public static final int OBCALLPJ_ISALREADY_END = 47;//外呼项目已完成所有呼叫
	public static final int OBCALLPJ_ISNOT_EXIST = 48;//外呼项目不存在
	public static final int OBCALLPJ_NOIDLE_AGENT = 49;//外呼坐席全忙
	public static final int PBXOBJISNULL = 100;//PBX对象无效
	public static final int PBXINNERERR = 200;//PBX内部错误
	
	public static final int EVENT_ExtStateChanged = 100;
	public static final int EVENT_AgentStateChanged = 101;
	public static final int EVENT_ChannelStateChanged = 102;
	public static final int EVENT_AgentCallined = 103;
	public static final int EVENT_MeEventHanpend = 105;
	public static final int EVENT_NewQueueEvent = 106;
	public static final int EVENT_HangupEvent = 107;
	public static final int EVENT_WXChatIn = 201;
	public static final int EVENT_WXChatTransfer = 202;
	
	public static final int AGENT_UNKNOWN = 0;
	public static final int AGENT_NOLOGIN = 1;
	public static final int AGENT_IDLE = 2;
	public static final int AGENT_IDLE_ONDEMAND = 3;
	public static final int AGENT_NOREADY = 4;
	public static final int AGENT_CALL_INITIALIZE = 5;
	public static final int AGENT_CALL_RINGBACK = 6;
	public static final int AGENT_CALL_RINGING = 7;
	public static final int AGENT_CALL_UP = 8;
	public static final int AGENT_CALL_DISCONNECT = 9;
	public static final int AGENT_CALL_LINK = 10;
	public static final int AGENT_CALL_MEETING = 11;
	public static final int AGENT_CALL_INSERT = 12;
	public static final int AGENT_CALL_HOLD = 13;
	public static final int AGENT_CALL_MONITOR = 14;
	
	public static final int OBCMDTYPE_START = 1;
	public static final int OBCMDTYPE_PAUSE = 2;
	public static final int OBCMDTYPE_RESTART = 3;
	public static final int OBCMDTYPE_START_REMAINED = 4;
	public static final int OBCMDTYPE_RE_CUS_FAILED = 5;
	public static final int OBCMDTYPE_RE_AGT_FAILED = 6;
	public static final int OBCMDTYPE_RE_CUS_SUCCESS = 7;
	
	public static final int AGENT_CDRTYPE_AVAILABLE = 100;
	public static final int AGENT_CDRTYPE_LOGOUT = 101;
	public static final int AGENT_CDRTYPE_ONBREAK = 102;
	
	public static int GetAgentstatus(final String str_status){
		switch (str_status) {
		case unknown:
			return AGENT_UNKNOWN;
		case logout:
			return AGENT_NOLOGIN;
		case available:
			return AGENT_IDLE;
		case available_on_demand:
			return AGENT_IDLE_ONDEMAND;
		case onbreak:
			return AGENT_NOREADY;
		default:
			return AGENT_NOLOGIN;
		}
	}
}
