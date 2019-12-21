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
	public static final String SYS_CONFIG_FILE_PATH = "/sysConfig.properties";

	public static final String SERVER_ONLINE_STATUS_PREFIX = "serveronline_";  //redis服务器在线/离线状态key前缀
	public static final String REDIS_HEARTBEAT_PREFIX = "heartbeat_"; //redis保存心跳时间key前缀

	public static final String SERVERWATCH_NETTY_CLIENT_URL = "/serverwatch/nettyClient/start.do";

	//检测端状态常量
	public static final Integer PROBE_STATE_NOT_DIPLAY = 0;	//未部署
	public static final Integer PROBE_STATE_DIPLAYED = 1;	//已部署
	public static final Integer PROBE_STATE_CONFIGURED = 2;	//已配置
	public static final Integer PROBE_STATE_DELETE = 3;	//探针已删除

	public static final Integer PROBE_STATE_OPEN = 1;  //检测端启动状态
	public static final Integer PROBE_STATE_STOP = 0;	//检测端关闭状态

	public static final Integer SERVER_ONLINE_STATE = 1; //服务器在线状态
	public static final Integer SERVER_OFFLINE_STATE = 2;	//服务器离线状态

	public static final String ALARM_PROBLEM = "PROBLEM";
	public static final String ALARM_OK = "OK";

	public static final String PROCESS_STATUS_OK = "OK";	//进程状态正常
	public static final String PROCESS_STATUS_PROBLEM = "PROBLEM";		//进程状态异常

	//远程部署检测端相关命令
	//public static final String PROBE_COMCONFIG_PATH = "/home/ftp/comconfig.xml";  //配置文件目录
	public static final String TESTLOGIN_SUCESS = "login ok";	//测试登录成功
	public static final String INIT_PROBE_SUCESS ="init ok"; //初试化探针成功
	public static final String SFTP_ZIP_SUCESS = "sftp zip file,ok"; //上传安装包成功
	public static final String UNZIP_SUCESS = "unzip zip file,ok";	//解压安装包成功
	public static final String LINK_RUN_PATH_SUCESS = "link run path,ok";	//建立软连接成功
	public static final String INSTALL_SERVICE_SUCESS = "install service,ok";	//安装服务成功
	public static final String EXPORT_ENV_SUCESS = "export env,ok";	//导入环境变量成功
	public static final String INSTALL_PROBE_SUCESS = "install-finished-with-ok";	//检测探针安装成功
	public static final String CONFIG_PROBE_SUCESS = "put config file,ok";	//配置检测探针
	public static final String START_PROBE_SUCESS = "start ok";	//启动检测探针成功
	public static final String STOP_PROBE_SUCESS = "stop ok";	//关闭检测探针成功
	public static final String REMOVE_PROBE_SUCESS = "remove,ok";	//删除检测探针成功
	public static final String RESTART_PROBE_SUCESS = "restart ok";	//检测探针重启成功

	public static final String TESTLOGIN_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> testlogin |grep UPGRADE-RESULT-LINE";
	public static final String INIT_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> init |grep UPGRADE-RESULT-LINE |awk '{print $2,$3}'";	//初试化检测端
	public static final String INSTALL_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> install /home/ftp/visionvera-serverwatch-<Version>.zip | grep UPGRADE-STATUS-LINE";	//安装部署检测端
	public static final String CONFIG_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> config /home/ftp/comconfig.xml |grep UPGRADE-RESULT-LINE"; 	//配置检测端，上传检测探针到服务器
	public static final String START_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> start |grep UPGRADE-RESULT-LINE";	 //启动检测端
	public static final String STOP_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> stop| grep UPGRADE-RESULT-LINE";	//关闭检测端
	public static final String REMOVE_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> remove |grep UPGRADE-RESULT-LINE"; //删除服务器检测端
	public static final String RESTART_SERVERWATCH = "$SW_UPGRADE <IP> <User> <PassWord> restart | grep UPGRADE-RESULT-LINE"; //重启检测探针
	/** 设备正常 */
	public static final int DEVICE_STATUS_OK = 0;
	/** 设备不正常 */
	public static final int DEVICE_STATUS_ERROR = 1;
	/** 网管返回为空，表示设备状态未知 */
	public static final int DEVICE_STATUS_UNKNOWN = 2;
	/** 网管返回为空，表示设备状态注意 */
	public static final int DEVICE_STATUS_NOTICE = 3;
	/** 网管返回为空，表示设备状态警告 */
	public static final int DEVICE_STATUS_WARNING = 4;

	/** 会议终端是否在线状态的Key */
	public static final String MEET_DEVICE_STATUS_KEY = "meetDevStatus";
	/** 可视电话源终端是否在线状态的Key */
	public static final String SRC_MEET_DEVICE_STATUS_KEY = "srcMeetDevStatus";
	/** 可视电话目的终端是否在线状态的Key */
	public static final String DST_MEET_DEVICE_STATUS_KEY = "dstMeetDevStatus";
	/** 会议终端在线 */
	public static final int MEET_DEVICE_STATUS_ON = 1;
	/** 会议终端离线 */
	public static final String MEET_DEVICE_STATUS_OFF = "0";
	/** 会议终端离线入库信息 */
	public static final String MEET_DEVICE_STATUS_INFO = "会议终端(终端号码为: %s)离线异常";
	/** 设备终端视频有流 */
	public static final String MEET_DEVICE_VIDEO_FLOW_OK = "0";
	/** 设备终端视频无流 */
	public static final String MEET_DEVICE_VIDEO_FLOW_ERROR = "1";
	/** 设备终端视频无流入库信息 */
	public static final String MEET_DEVICE_VIDEO_FLOW_INFO = "会议终端(终端号码为: %s)视频无流异常, 视频无流的路数为: %s";
	/** 设备终端音频有流 */
	public static final int MEET_DEVICE_AUDIO_FLOW_OK = 0;
	/** 设备终端音频无流 */
	public static final String MEET_DEVICE_AUDIO_FLOW_ERROR = "1";
	/** 设备终端音频无流入库信息 */
	public static final String MEET_DEVICE_AUDIO_FLOW_INFO = "会议终端(终端号码为: %s)音频无流异常, 音频无流的路数为: %s";
	/** 服务器在线的状态 */
	public static final int MEET_DEVICE_SERVER_ONLINE = 1;
	/** 服务器离线的状态 */
	public static final String MEET_DEVICE_SERVER_OFFLINE = "0";
	/** 服务器转发配置相同的状态 */
	public static final int MEET_DEVICE_CONFIG_STATUS_OK = 1;
	/** 服务器转发配置不同的状态 */
	public static final int MEET_DEVICE_CONFIG_STATUS_DIFFERENCE = 0;
	/** 设备终端状态信息的Key: deviceStatusInfo */
	public static final String DEVICE_STATUS_INFO_KEY = "deviceStatusInfo";
	/** 服务器状态信息的Key: serverStatusInfo */
	public static final String SERVER_STATUS_INFO_KEY = "serverStatusInfo";
	/** 源设备终端状态信息的Key: deviceStatusInfo */
	public static final String SRC_DEVICE_STATUS_INFO_KEY = "srcDeviceStatusInfo";
	/** 源设备所属服务器状态信息的Key: serverStatusInfo */
	public static final String SRC_SERVER_STATUS_INFO_KEY = "srcServerStatusInfo";

	public static final String DST_DEVICE_STATUS_INFO_KEY = "dstDeviceStatusInfo";
	/** 目的服务器状态信息的Key: serverStatusInfo */
	public static final String DST_SERVER_STATUS_INFO_KEY = "dstServerStatusInfo";
	/** 感知中心：设备离线信息 */
	public static final String PERCEPTION_DEVICE_OFFLINE_INFO = "终端离线";
	/** 感知中心：设备终端音频丢包率信息 */
	public static final String PERCEPTION_DEVICE_LOST_RATE_AUDIO_INFO = "终端音频丢包率为%s";
	/** 感知中心：设备终端视频丢包率信息 */
	public static final String PERCEPTION_DEVICE_LOST_RATE_VIDEO_INFO = "终端视频丢包率为%s";
	/** 感知中心：设备终端版本不一致信息 */
	public static final String PERCEPTION_VERSION_STATUS_INFO = "终端版本不一致";
	/** 感知CS：设备对应的服务器离线信息 */
	public static final String PERCEPTION_DEVICE_SERVER_OFFLINE_INFO = "服务器离线";
	/** 感知CS：设备对应的服务器转发配置异常信息 */
	public static final String PERCEPTION_DEVICE_SERVER_CONFIG_DIFFERENCE_INFO = "服务器流量转发配置异常";
	/** 设备版本状态相同 */
	public static final int DEVICE_VERSION_STATUS_OK = 1;
	/** 设备版本状态不相同 */
	public static final String DEVICE_VERSION_STATUS_DIFFERENCE = "0";
	/**
	 * 业务统一管控 平台类型
	 */
	public static final int PLATFORMTYPE_TGL = 301;//唐古拉

	public static final int PLATFORMTYPE_LMT = 401;//流媒体

	public static final int PLATFORMTYPE_CONTENT = 501;//内容管理平台

	public static final int PLATFORMTYPE_HG = 601;//内容管理平台

	public static final int PLATFORMTYPE_ZD = 701;//终端

	public static final int PLATFORMTYPE_SLHAPP = 801;//视联汇APP

	public static final int PLATFORMTYPE_GIS = 901;//GIS平台
	/**
	 * 业务统一管控 业务类型
	 */
	public static final int BUSINESSTYPE_KSDH = 0;//可视电话

	public static final int BUSINESSTYPE_FBZB_PT = 1;//发布直播（普通）

	public static final int BUSINESSTYPE_FBZB_JK = 2;//发布直播（监控）

	public static final int BUSINESSTYPE_FBZB_SJ = 3;//发布直播（升级）

	public static final int BUSINESSTYPE_SKZB_PT = 4;//收看直播（普通）

	public static final int BUSINESSTYPE_SKZB_JK = 5;//收看直播（监控）

	public static final int BUSINESSTYPE_SKZB_SJ = 6;//收看直播（升级）

	public static final int BUSINESSTYPE_LZ = 7;//录制

	public static final int BUSINESSTYPE_DB = 8;//点播

	public static final int BUSINESSTYPE_LMTHY = 9;//会议
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
}
