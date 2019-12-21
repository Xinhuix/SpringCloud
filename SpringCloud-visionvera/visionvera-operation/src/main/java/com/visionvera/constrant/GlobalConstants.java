package com.visionvera.constrant;

import java.util.HashMap;
import java.util.Map;


public class GlobalConstants {
	
	private GlobalConstants(){
	}
	
	public static final int CREATE_TYPE_PAMIRE = 1;//pamir创建的预约
	public static final int CREATE_TYPE_HYT = 2;//会易通创建的预约
	public static final int CREATE_TYPE_GIS = 3;//GIS创建的预约
	public static final int CREATE_TYPE_WS = 4;//通过webservice接口创建的预约

	public static final int MODIFY_STATUS_YES = 1;//预约是否有变更：有
	public static final int MODIFY_STATUS_NO = 0;//预约是否有变更：无

	public static final String ADMIN_LOGIN_NAME = "admin";//默认会场名称
	public static final String DEFAULT_GROUP_NAME = "默认会场";//默认会场名称
	public static final String COMPONENT_CMSWEB = "cmsweb";//会管服务名称
	public static final String FILE_DIR_ROOT = "/cmsweb_file_manager";//会管文件存放根路径
	public static final String FILE_DIR_ATTACHMENT = "/cmsweb_file_manager/attachment";//会管附件存放路径
	public static final String FILE_NAME_REG = "^[A-Z]{1,}_V[0-9]{1,}(.([0-9]{1,})){2,3}.[a-zA-Z]{1,}$";//pamir升级包文件命名规范
	public static final Integer COMPLETE_START = 0;//发起
	public static final Integer COMPLETE_AGREE = 2;//审批中
	public static final Integer COMPLETE_REJECT = 1;//被拒绝
	public static final Integer COMPLETE_WAIT = 3;//待审批
	public static final Integer COMPLETE_END = 4;//已完成
	public static final Integer COMPLETE_SUMMARY = 5;//待总结
	
	public static final Integer CHOOSE_NEXT_YES = 2;//需要选择下一节点审批人
	public static final Integer CHOOSE_NEXT_NO = 1;//不需要选择下一节点审批人
	public static final Integer EDIT_FORM_NO = 0;//是否有表单的编辑权限。0只能查看，1可以编辑申请表的“网络部填写”部分，2可以编辑总结表
	public static final Integer EDIT_FORM_APPLY = 1;//可以编辑申请表的“网络部填写”部分
	public static final Integer EDIT_FORM_SUMMARY = 2;//可以编辑总结表
	
	public static final Integer FOREIGN_KEY_CHECKS_ON = 1;//数据库外键约束启用
	public static final Integer FOREIGN_KEY_CHECKS_OFF = 0;//数据库外键约束禁用
	
	//系统配置文件路径
	//public static final String SYS_BATH_PATH = GlobalConstants.class.getClassLoader().getResource("/").getPath();
	public static final String SYS_BATH_PATH = GlobalConstants.class.getClassLoader().getResource("/").getPath();
	public static final String SYS_CONFIG_FILE_PATH = "/sysConfig.properties";
	public static final String ERRDESC_CONFIG_FILE_PATH = SYS_BATH_PATH + "properties/errorDesc.properties";
	public static final String SERVER_ONLINE_STATUS_PREFIX = "serveronline_";  //redis服务器在线/离线状态key前缀
	public static final String REDIS_HEARTBEAT_PREFIX = "heartbeat_"; //redis保存心跳时间key前缀
	
	public static final String SERVERWATCH_NETTY_CLIENT_URL = "/serverwatch/nettyClient/start.do";
	
	public static final Integer SERVER_ONLINE_STATE = 1; //服务器在线状态
	public static final Integer SERVER_OFFLINE_STATE = 2;	//服务器离线状态
	
	public static final String ALARM_PROBLEM = "PROBLEM";
	public static final String ALARM_OK = "OK";
	
	public static final String PROCESS_STATUS_OK = "OK";	//进程状态正常
	public static final String PROCESS_STATUS_PROBLEM = "PROBLEM";		//进程状态异常
	
	public static final Integer PROCESS_STATE_DELETE = -1;		//进程状态是已删除
	public static final Integer PROCESS_STATE_FORMAL = 0;		//进程状态是正常
	
	public static final Integer SERVER_UNDISPLOY_STATE = 0;		//探针未部署
	public static final Integer SERVER_DISPLOY_STATE = 1;		//探针已部署
	public static final Integer SERVER_CONFIG_STATE = 2;		//探针已配置
	public static final Integer SERVER_DEL_STATE = 3;		//探针已删除

	public static final Map<String,String> REGION_MASTERNO_MAP = new HashMap<String, String>();
	static {
		REGION_MASTERNO_MAP.put("北京市", "1"); REGION_MASTERNO_MAP.put("天津市", "2");
		REGION_MASTERNO_MAP.put("上海市", "3"); REGION_MASTERNO_MAP.put("重庆市", "4");
		REGION_MASTERNO_MAP.put("内蒙古自治区", "5"); REGION_MASTERNO_MAP.put("新疆维吾尔自治区", "6");
		REGION_MASTERNO_MAP.put("西藏自治区", "7"); REGION_MASTERNO_MAP.put("宁夏回族自治区", "8");
		REGION_MASTERNO_MAP.put("广西壮族自治区", "9"); REGION_MASTERNO_MAP.put("香港特别行政区", "10");
		REGION_MASTERNO_MAP.put("澳门特别行政区", "11"); REGION_MASTERNO_MAP.put("黑龙江省", "12");
		REGION_MASTERNO_MAP.put("吉林省", "13"); REGION_MASTERNO_MAP.put("辽宁省", "14");
		REGION_MASTERNO_MAP.put("河北省", "15"); REGION_MASTERNO_MAP.put("山西省", "16");
		REGION_MASTERNO_MAP.put("青海省", "17"); REGION_MASTERNO_MAP.put("山东省", "18");
		REGION_MASTERNO_MAP.put("河南省", "19"); REGION_MASTERNO_MAP.put("江苏省", "20");
		REGION_MASTERNO_MAP.put("安徽省", "21"); REGION_MASTERNO_MAP.put("浙江省", "22");
		REGION_MASTERNO_MAP.put("福建省", "23"); REGION_MASTERNO_MAP.put("江西省", "24");
		REGION_MASTERNO_MAP.put("湖南省", "25"); REGION_MASTERNO_MAP.put("湖北省", "26");
		REGION_MASTERNO_MAP.put("广东省", "27"); REGION_MASTERNO_MAP.put("台湾省", "28"); 
		REGION_MASTERNO_MAP.put("海南省", "29"); REGION_MASTERNO_MAP.put("甘肃省", "30");
		REGION_MASTERNO_MAP.put("陕西省", "31"); REGION_MASTERNO_MAP.put("四川省", "32");
		REGION_MASTERNO_MAP.put("贵州省", "33"); REGION_MASTERNO_MAP.put("云南省", "34");
	}
	
	//TCP通信相关常量
	public static final String TCP_START_DELIMITER = "##**";	//包开始标志
	public static final String TCP_END_DELIMITER = "**##";		//包结束标志
	public static final String TCP_HEARTBEAT_PACKAGE = "##**00000000HETP";   //根据包结束标志获取到的心跳包内容
	public static final String TCP_DEVICE_FLAG = "DevInfo";		//服务器信息标志
	public static final String TCP_OFFLINE_FLAG = "alarm_offLine";			//服务器离线报警标志
	public static final String TCP_TGL_CHECK_INFO = "TglCheckInfo";			//唐古拉配置告警
	public static final String TCP_TGL_ALARM = "TglAlarm";			//唐古拉告警
	public static final String TCP_PLATFORM_FLAG = "alarm_platform";		//平台报警
	public static final String TCP_SUCESS_RET = "0";
	
	//redis 报警前缀
	public static final String ALARM_REDIS_PREFIX = "alarm_flag_";

	public static final String V2V_FUNC_REDIS_PREFIX = "user_func_";

	public static final String IP_V2V_PROBE_REDIS_PREFIX = "IP_V2V_PROBE:";

	public static final String V2V_PROBE_UPGRADE_PREFIX = "probeUpgradeList";
	//防止重复提交前缀
	public static final String RESUBMIT = "RESUBMIT_";


	//V2V探针是否复用协转终端号
	public static final Integer REUSE_XZ_TERNO_YES = 1;   	//复用协转终端号
	public static final Integer REUSE_XZ_TERNO_NO = 0;		//不复用协转终端号

	//各平台缩写
	public static final String PLATFORM_GIS_SIGN = "GIS";  //Gis天眼平台
	public static final String PLATFORM_TGL_SIGN = "TGL";  //TGL监控联网系统

	//IP探针websocket标识
	public static final String IP_SERVER_LOGIN_BATCH = "testLoginBatch";		//测试登录
	public static final String IP_PROBE_DISPLAY = "displayProbe";		//部署探针
	public static final String IP_PROBE_UPGRADE = "upgradeProbe";					//升级探针
	public static final String IP_PROBE_REMOVE = "removeProbe";			//移除探针

	//WebSocket标识
	public static final String GET_SUGGESTED_PLATFORM_VERSION = "getSuggestedPlatformVersion";		//获取推荐的平台版本
	public static final String PROBE_PLATFORM_ALARM = "ProbePlatformAlarm";		//探针平台告警

	//平台/进程重启操作
	public static final String PLATFORM_HANDLE = "platformHandle";
	public static final String PROCESS_HANDLE = "processHandle";

	/**
	 * 监测探针关于监测平台服务配置的url
	 */
	public static final String PROBE_ADD_PLATFORM_URL = "http://%s:%s/serverwatch/config/addPlatformConf";
	public static final String PROBE_MODIFY_PLATFORM_URL = "http://%s:%s/serverwatch/config/modifyPlatformConf";
	public static final String PROBE_REMOVE_PLATFORM_URL = "http://%s:%s/serverwatch/config/%s/removePlatformConf";

	public static final String PROBE_PLATFORM_CONF_CHECK = "http://%s:%s/serverwatch//pfConfig/%s/configModify";	//通知探针检测平台配置文件
	public static final String UPDATE_OPERATION_URL = "http://%s:%s/serverwatch/config/operation/update";		//更新探针微服务operation服务IP
	public static final String START_PROBE_CHECK_URL = "http://%s:%s/serverwatch/config/startProbeCheck";		//启动探针检测
	public static final String STOP_PROBE_CHECK_URL = "http://%s:%s/serverwatch/config/stopProbeCheck";	//停止探针检测
	public static final String REMOVE_PROBE_URL = "http://%s:%s/serverwatch/probe/del";		//删除探针
	public static final String HANDLE_PROCESS = "http://%s:%s/serverwatch/process/%s/handleProcess";		//操作进程

	public static final String PROBE_CURRENT_VERSION = "http://%s:%s/serverwatch/probe/getCurrentVersion";	//获取探针最新版本
	public static final String UPGRADE_PROBE_URL = "http://%s:%s/serverwatch/probe/upGradeProbe";		//windows，IP环境自动升级url
	public static final String ADD_PROCESS_URL = "http://%s:%s/serverwatch/process/addProcess";			//添加进程
	public static final String MODIFY_PROCESS_URL = "http://%s:%s/serverwatch/process/modifyProcess";		//修改进程
	public static final String DEL_PROCESS_URL = "http://%s:%s/serverwatch/process/%s/delete";		//删除进程
	
	public static final Integer JAVA_PROBE_PORT =10009;//java探针端口号

}