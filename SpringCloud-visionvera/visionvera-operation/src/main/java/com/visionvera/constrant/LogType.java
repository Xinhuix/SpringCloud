package com.visionvera.constrant;

public class LogType {

	private LogType(){
	}
	
	public static final int OPERATE_OK = 1;//操作成功
	public static final int OPERATE_ERROR = 0;//操作失败

	public static final int LOGIN = 1;//登录
	public static final int LOGOUT = 2;//退出
	public static final int ADD_FUNCTION = 3;//新增功能
	public static final int EDIT_FUNCTION = 4;//修改功能
	public static final int DEL_FUNCTION = 1;//删除功能
	public static final int ADD_USER = 6;//新增用户
	public static final int EDIT_USER = 7;//修改用户
	public static final int DEL_USER = 8;//删除用户
	public static final int ADD_ROLE = 9;//新增角色
	public static final int EDIT_ROLE = 10;//修改角色
	public static final int DEL_ROLE = 11;//删除角色
	public static final int ADD_SCHEDULE = 12;//新增预约
	public static final int EDIT_SCHEDULE = 13;//修改预约
	public static final int DEL_SCHEDULE = 14;//删除预约
	public static final int AUDIT_SCHEDULE = 15;//审批预约
	public static final int ADD_DEVICE = 16;//新增设备
	public static final int EDIT_DEVICE = 17;//修改设备
	public static final int DEL_DEVICE = 18;//删除设备
	public static final int ADD_DEVICES = 19;//批量新增设备
	public static final int ADD_USER_GROUP = 20;//新增用户组
	public static final int EDIT_USER_GROUP = 21;//修改用户组
	public static final int DEL_USER_GROUP = 22;//删除用户组
	public static final int ADD_DEV_GROUP = 23;//新增设备组
	public static final int EDIT_DEV_GROUP = 24;//修改设备组
	public static final int DEL_DEV_GROUP = 25;//删除设备组
	public static final int START_MEETING = 26;//开启会议
	public static final int STOP_MEETING = 27;//停止会议

	public static final int SYN_DEVICE = 34;//同步设备
	public static final int ADD_AUTH_DEVICE = 35;//授权
	public static final int DEL_AUTH_DEVICE = 36;//取消授权
	public static final int GEN_DEV_GROUPS = 37;//快速生成会场
	public static final int MV_DEVICE = 38;//移动设备
	public static final int LOCK_USER_GROUP = 39;//锁定用户组
	public static final int UNLOCK_USER_GROUP = 40;//解锁用户组
	public static final int EDIT_SYS_CONFIG = 41;//修改系统配置
	
	public static final int SYNC_SYS_REGISTER = 50;//分级系统注册
	public static final int SYNC_SYS_UNREGISTER = 51;//分级系统解注册
	public static final int SYNC_SYS_SYNCDATA = 52;//分级系统同步数据
	public static final int SYNC_SYS_DEL = 53;//分级系统删除服务
	public static final int SYNC_SYS_EDIT = 54;//分级系统编辑服务
	
	public static final int UOGRADE_DOWNLOAD = 55;//下载升级包
	public static final int DEVICE_MEETING = 56;//下载升级包
}
