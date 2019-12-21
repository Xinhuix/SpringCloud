package com.visionvera.service.impl;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.cms.CloudVO;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceRoleVO;
import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerInfoVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.VSMeet64;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.operation.DeviceDao;
import com.visionvera.dao.operation.ScheduleDao;
import com.visionvera.dao.operation.SysConfigDao;
import com.visionvera.dao.operation.UserDao;
import com.visionvera.service.DeviceService;
import com.visionvera.util.ChangeNumberUtils;
import com.visionvera.util.ExcelUtil;
import com.visionvera.util.RegionUtil;
import com.visionvera.util.RestClient;
import com.visionvera.util.SocketClient;
import com.visionvera.util.TimeUtil;
import com.visionvera.util.UUIDGenerator;
import com.visionvera.util.Util;
import com.visionvera.util.VSMeet;

/**
 * 
 * @ClassName: DeviceServiceImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author zhaolei
 * @date 2016年8月12日 下午3:22:54
 * 
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class DeviceServiceImpl implements DeviceService {

	
	@Resource
	private DeviceDao deviceDao;
	@Resource
	private SysConfigDao sysConfigDao;
	@Resource
	private ScheduleDao scheduleDao;
	@Resource
	private UserDao userDao;
	@Value("${sys_bit}")
	private Integer sysBit;
	
	private static final Logger logger = LogManager.getLogger(DeviceServiceImpl.class);
	//会管设备本地区号
	private final Integer LOCAL_ZONENO = 9 ;
	
	/**
	 * 
	 * Title: getDeviceList Description: 获取设备列表
	 * 
	 * @return
	 * @see com.visionvera.cms.service.DeviceService#getDeviceList()
	 */
	public List<DeviceVO> getDeviceList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceList(map, new RowBounds());
		}
		return deviceDao.getDeviceList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * Title: getDevList Description: 获取设备列表（无权限要求）
	 * 
	 * @return
	 * @see com.visionvera.cms.service.DeviceService#getDevList()
	 */
	public List<DeviceVO> getDevList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevList(map, new RowBounds());
		}
		return deviceDao.getDevList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getDeviceTypeList 
	 * @Description: TODO 获取设备类型列表 
	 * @param @return  参数说明 
	 * @return List<DeviceTypeVO>    返回类型 
	 * @throws
	 */
	public List<DeviceTypeVO> getDeviceTypeList(){
		return deviceDao.getDeviceTypeList();
	}

	/**
	 * 
	 * @Title: getDeviceRoleList 
	 * @Description: TODO 获取设备角色列表 
	 * @param @return  参数说明 
	 * @return List<DeviceTypeVO>    返回类型 
	 * @throws
	 */
	public List<DeviceRoleVO> getDeviceRoleList(){
		return deviceDao.getDeviceRoleList();
	}

	/**
	 * 
	 * @Title: avaliableDevices
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> avaliableDevices(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.avaliableDevices(map, new RowBounds());
		}
		return deviceDao.avaliableDevices(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * @param  
	 * 
	 * @Title: avaliableDevicesCount
	 * @Description: 获取所有未分组（含本组）设备总数
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int avaliableDevicesCount(Map<String, Object> map){
		return deviceDao.avaliableDevicesCount(map);
	}

	/**
	 * 
	 * @Title: devicesInGroup
	 * @Description: 获取组内已添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> devicesInGroup(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceList(map, new RowBounds());
		}
		return deviceDao.getDeviceList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * Title: addDevice Description: 新增设备
	 * 
	 * @param map
	 * @return
	 * @see com.visionvera.cms.service.DeviceService#addDevice(java.util.Map)
	 */
	public int addDevice(DeviceVO device) {
		int rlt = 1;
		if (device == null)
			return 0;
		try {
			rlt = deviceDao.addDevice(device);
		} catch (Exception e) {
			logger.error("新增设备失败", e);
			rlt = 0;
		}
		return rlt;
	}

	/**
	 * 
	 * Title: updateDevice Description: 更新设备
	 * 
	 * @param map
	 * @return
	 * @see com.visionvera.cms.service.DeviceService#updateDevice(java.util.Map)
	 */
	public int updateDevice(DeviceVO device) {
		int rlt = 0;
		if(null == device)
			return 0;
		try {
			rlt = deviceDao.updateDevice(device);
		} catch (Exception e) {
			logger.error("更新设备失败", e);
			rlt = 0;
		}
		return rlt;
	}

	public int updateDevice(Map<String, Object> map) {
		return deviceDao.updateDeviceGis(map);
	}
	/**
	 * 
	 * Title: deleteDevice Description: 删除设备
	 * 
	 * @param map
	 * @return
	 * @see com.visionvera.cms.service.DeviceService#deleteDevice(java.util.Map)
	 */
	public int deleteDevice(Map<String, Object> map) {
		return deviceDao.deleteDevice(map);
	}

	
	/**
	 * 
	 * Title: getDeviceListCount
	 * Description: 根据删选条件获取设备列表条目数
	 * @param map
	 * @return 
	 * @see com.visionvera.cms.service.DeviceService#getDeviceListCount(java.util.Map)
	 */
	public int getDeviceListCount(Map<String, Object> map) {
		return deviceDao.getDeviceListCount(map);
	}

	/**
	 * 
	 * Title: getDevListCount
	 * Description: 根据删选条件获取设备列表条目数
	 * @param map
	 * @return 
	 * @see com.visionvera.cms.service.DeviceService#getDeviceListCount(java.util.Map)
	 */
	public int getDevListCount(Map<String, Object> map) {
		return deviceDao.getDevListCount(map);
	}

	/**
	 * 
	 * @Title: getDeviceGroupList 
	 * @Description: 获取设备分组列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public List<DeviceGroupVO> getDeviceGroupList(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceGroupList(map, new RowBounds());
		}
		return deviceDao.getDeviceGroupList(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getDeviceGroupListCount 
	 * @Description: 获取设备分组列表总数
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int getDeviceGroupListCount(Map<String, Object> map) {
		return deviceDao.getDeviceGroupListCount(map);
	}

	/**
	 * 
	 * @Title: addDeviceGroup 
	 * @Description: 新增设备分组信息
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int addDeviceGroup(DeviceGroupVO deviceGroup) {
		int rlt = 0;
		if(null == deviceGroup)
			return 0;
		try {
			//新增会场
			rlt = deviceDao.addDeviceGroup(deviceGroup);
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			
			if(StringUtils.isNotBlank(deviceGroup.getDevices()))
			{
				String[] uuids = deviceGroup.getDevices().split(",");
				if (uuids.length > 0) {
					paramsMap.put("groupuuid", deviceGroup.getUuid());
					paramsMap.put("deviceuuids", uuids);
					rlt = deviceDao.addDevice2Group(paramsMap);
				}
			}
			//创建会场和用户组的关联关系
			paramsMap.put("groupId", deviceGroup.getUserGroupId());
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("uuid", deviceGroup.getUuid());
			items.add(item);
			paramsMap.put("list", items);
			deviceDao.addDevGroup2UserGroup(paramsMap);
		} catch (Exception e) {
			logger.error("新增设备分组失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return rlt;
	}

	/**
	 * 
	 * @Title: addDevice2Group 
	 * @Description: 添加设备分组和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */

	public int addDevice2Group(Map<String, Object> map){
		return deviceDao.addDevice2Group(map);
	}

	/**
	 * 
	 * @Title: deleteDevice2Group 
	 * @Description: 删除设备分组和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int deleteDevice2Group(Map<String, Object> map){
		return deviceDao.deleteDevice2Group(map);
	}
	/**
	 * 
	 * @Title: deleteDeviceGroup 
	 * @Description: 删除设备分组信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return map    返回类型 
	 * @throws
	 */
	public Map<String, Object> deleteDeviceGroup(Map<String, Object> paramsMap) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("msg", "删除会场成功");
		result.put("result", true);
		try {
			List<DeviceGroupVO> devGroups = deviceDao.searchGroupByUuid(paramsMap);
			if(devGroups.size() > 0){//默认会场不允许删除
				for(DeviceGroupVO devGroup : devGroups){
					if(devGroup.getName().equals(GlobalConstants.DEFAULT_GROUP_NAME)){
						result.put("msg", "默认会场不允许删除");
						result.put("result", false);
						return result;
					}
				}
			}
			//判断会场中是否有设备被占用
			List<DeviceVO> devices = deviceDao.isDeviceDeletable(paramsMap);
			if(devices.size() > 0){//只要有有效预约在使用组内设备，就不允许删除该设备
				result.put("result", false);
				result.put("msg", "当前有预约使用设备"+devices.get(0).getId()+"，不允许删除");
				return result;
			}
			//获取默认会场ID
			String uuid = null;
			paramsMap.put("name", GlobalConstants.DEFAULT_GROUP_NAME);
			List<DeviceGroupVO> defGroup = deviceDao.searchGroup(paramsMap);//查找用户分组下是否有默认会场
			DeviceGroupVO devGroup = new DeviceGroupVO();
			if(defGroup.size() < 1){
				devGroup.setName(GlobalConstants.DEFAULT_GROUP_NAME);
				deviceDao.addDeviceGroup(devGroup);
				uuid = devGroup.getUuid();
			}else{
				uuid = defGroup.get(0).getUuid();
			}
			//获取要删除的会场下的所有设备
			devices = deviceDao.devicesInGroup(paramsMap);
			List<String> devIds = new ArrayList<String>();
			for(DeviceVO device : devices){
				devIds.add(device.getId());
			}
			//删除会场
			deviceDao.deleteDeviceGroup(paramsMap);
			//将被删除会场下的所有设备添加到默认会场
			if(devIds.size() > 0){
				paramsMap.put("groupId", uuid);
				paramsMap.put("devIds", devIds);
				deviceDao.add2Group(paramsMap);
			}
		} catch (Exception e) {
			result.put("msg", "删除会场失败");
			result.put("result", false);
			logger.error("删除会场失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return result;
	}

	/**
	 * 
	 * @Title: updateDeviceGroup 
	 * @Description: 更新设备分组信息 
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public Map<String, Object> updateDeviceGroup(DeviceGroupVO deviceGroup) {
		Map<String, Object> rtMap = new HashMap<String, Object>();
		int rlt = 0;
		if(null == deviceGroup){
			rtMap.put("result", 0);
			return rtMap;
		}
		try {
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			
			paramsMap.put("groupuuid", deviceGroup.getUuid());
			List<UserVO> users = deviceDao.isGroupEditable(paramsMap);
			if(users.size() > 0){//只要关联该设备组任何用户组中有一个以上的用户在线，就不允许编辑该设备组
				rtMap.put("result", -1);
				rtMap.put("errMsg", users.get(0).getLoginName()+"正使用该设备组，不允许编辑");
				return rtMap;
			}
			if(StringUtils.isNotBlank(deviceGroup.getDevices()))
			{
				String[] uuids = deviceGroup.getDevices().split(",");
				if (uuids.length > 0) {
					paramsMap.put("deviceuuids", uuids);//新增设备和用户组关联使用
					paramsMap.put("devids", uuids);//判断设备是否可删除使用
				}
			}else{
				String[] uuids = {"-1"};
				paramsMap.put("devids", uuids);//删除组内所有设备的情况，构造一个假数据，供 not in 使用
			}
			List<DeviceVO> devices = deviceDao.isDeviceDeletable(paramsMap);
			if(devices.size() > 0){//只要有有效预约在使用组内设备，就不允许删除该设备
				rtMap.put("result", -1);
				rtMap.put("errMsg", "当前有预约使用设备"+devices.get(0).getId()+"，不允许删除");
				return rtMap;
			}
			rlt = deviceDao.updateDeviceGroup(deviceGroup);
			deviceDao.deleteDevice2Group(paramsMap);
			if(StringUtils.isNotBlank(deviceGroup.getDevices()))
			{
				rlt = deviceDao.addDevice2Group(paramsMap);
			}
		} catch (Exception e) {
			logger.error("更新设备分组失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		rtMap.put("result", rlt);
		return rtMap;
	}

	/**
	 * 
	 * @Title: updateDeviceGroupName 
	 * @Description: 更新会场名称
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public Map<String, Object> updateDeviceGroupName(DeviceGroupVO deviceGroup) {
		Map<String, Object> rtMap = new HashMap<String, Object>();
		int rlt = 0;
		if(null == deviceGroup){
			rtMap.put("result", 0);
			return rtMap;
		}
		try {
			rlt = deviceDao.updateDeviceGroup(deviceGroup);
		} catch (Exception e) {
			logger.error("更新设备分组失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		rtMap.put("result", rlt);
		return rtMap;
	}

	/**
	 * 
	 * @Title: getAllGroups 
	 * @Description: 获取所有设备分组
	 * @return int    返回类型 
	 * @throws
	 */
	public List<DeviceGroupVO> getAllGroups(Map<String, Object> map) {
		return deviceDao.getAllGroups(map);
	}

	/**
	 * 
	 * @Title: upLoadExcel
	 * @Description: 解析上传的excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> upLoadExcel(InputStream in, String fileName) {
		Map<String, Object> rlt = new HashMap<String, Object>();
		Map<String, Object> result = null;
		boolean flag = true;
		try {
			result = ExcelUtil.getListByExcel(in, fileName);
			rlt.put("successCount", ((List<DeviceVO>) result.get("list")).size());//成功导入设备的数量
			rlt.put("errList", result.get("errList"));//导入错误的设备列表
			rlt.put("failCount", ((List<Map<String, Object>>) result.get("errList")).size());//导入错误设备的数量
			deviceDao.upLoadExcel((List<DeviceVO>) result.get("list"));
		} catch (Exception e) {
			logger.error("从excel批量导入设备失败", e);
			flag = false;
		}
		rlt.put("result", flag);
		return rlt;
	}

	/**
	 * 
	 * @Title: downLoadExcel
	 * @Description: 下载excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public boolean downLoadExcel(InputStream in, String fileName) {
		try {
//			Map<String, Object> result = ExcelUtil.getListByExcel(in, fileName);
//			deviceDao.upLoadExcel((List<DeviceVO>) result.get("list"));
		} catch (Exception e) {
			logger.error("从excel批量导入设备失败", e);
			return false;
		}
		return true;
	}

	public List<Map<String, Object>> createExcelRecord() {
        List<DeviceVO> devices = getDeviceList(new HashMap<String, Object>());
        List<Map<String, Object>> listmap = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sheetName", "设备列表");
        listmap.add(map);
        DeviceVO device=null;
        for (int j = 0; j < devices.size(); j++) {
        	device=devices.get(j);
            Map<String, Object> mapValue = new HashMap<String, Object>();
            mapValue.put("id", device.getId());
            mapValue.put("name", device.getName()); 
            mapValue.put("mac", device.getMac()); 
            mapValue.put("ip", device.getIp()); 
            mapValue.put("type", device.getType()); 
            mapValue.put("description", device.getDescription()); 
            listmap.add(mapValue);
        }
        return listmap;
    }
	/**
	 * 
	 * @Title: checkRepeatGroup 
	 * @Description: 验证设备组名组称是否重复
	 * @param @param name
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int checkRepeatGroup(Map<String, Object> map) {
		return deviceDao.checkRepeatGroup(map);
	}
	
	/**
	 * 
	 * @Title: checkRepeatId
	 * @Description: 验证设备号码重复
	 * @param @param uuid
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int checkRepeatId(Map<String, Object> map){
		return deviceDao.checkRepeatId(map);
	}

	/**
	 * 
	 * @Title: checkRepeatName
	 * @Description: 验证设备名称重复
	 * @param @param uuid
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int checkRepeatName(Map<String, Object> map){
		return deviceDao.checkRepeatName(map);
	}

	/**
	 * 
	 * @Title: getDeviceCountByUserId
	 * @Description: 根据用户id获取设备数量
	 * @param @param map
	 * @param @param 
	 * @param @return
	 * @return int 设备数量
	 * @throws
	 */
	public int getDeviceCountByUserId(Map<String, Object> paramsMap) {
		return deviceDao.getDeviceCountByUser(paramsMap);
	}

	/**
	 * 
	 * @Title: GetDeviceListByUserId
	 * @Description: 根据用户id获取设备列表
	 * @param @param map,page
	 * @param @param 
	 * @param @return
	 * @return List<DeviceVO> 
	 * @throws
	 */
	public List<DeviceVO> GetDeviceListByUserId(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceListByUser(map, new RowBounds());
		}
		return deviceDao.getDeviceListByUser(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: GetDevice
	 * @Description: 根据设备id获取设备信息
	 * @param @param map,page
	 * @param @param 
	 * @param @return
	 * @return List<DeviceVO> 
	 * @throws
	 */
	public List<DeviceVO> GetDevice(Map<String, Object> map) {
		return deviceDao.getDevice(map);
	}

	/**
	 * 
	 * @Title: getOrgDevGroups
	 * @Description: 根据工作单位获取会场列表
	 * @param  Map
	 * @param 
	 * @return List<DeviceGroupVO> 返回类型
	 * @throws
	 */
	public List<DeviceGroupVO> getOrgDevGroups(Map<String, Object> map) {
		return deviceDao.getOrgDevGroups(map);
	}

	/**
	 * 
	 * @Title: synchroDevices
	 * @Description: 从网管同步设备
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public Map<String, Object> synchroDevices(Map<String, Object> paramsMap) {
		Map<String, Object> ret = new HashMap<String, Object>();
		try{
			//获取基础url
			paramsMap.put("type", 2);
			ServerVO svr = sysConfigDao.getServer(paramsMap);
			
			if(svr == null){
				ret.put("result", false);
				ret.put("msg", "网管服务器未配置");
				return ret;
			}
			if(StringUtils.isBlank(svr.getIp())){
				ret.put("result", false);
				ret.put("msg", "网管服务器IP未配置");
				return ret;
			}
			if(StringUtils.isBlank(svr.getPort())){
				ret.put("result", false);
				ret.put("msg", "网管服务器端口未配置");
				return ret;
			}
			if(StringUtils.isBlank(svr.getAccount())){
				ret.put("result", false);
				ret.put("msg", "网管服务器账号未配置");
				return ret;
			}
			if(StringUtils.isBlank(svr.getPassword())){
				ret.put("result", false);
				ret.put("msg", "网管服务器密码未配置");
				return ret;
			}
			String baseUrl = null;
			//获取当前会管系统类别 16或64位
			//String systemType = Util.getSysProp("cmsweb.systemType");
			baseUrl = String.format(WsConstants.NET_HOST_URL, svr.getIp(),svr.getPort());
			//获得token
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_USER, svr.getAccount());//账号
			args.put(WsConstants.KEY_PWD, svr.getPassword());//密码
			String token = null;
			try{
				Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_LOGIN, null, args);
				if(data.get("extra") == null){
					ret.put("result", false);
					ret.put("msg", data.get("errmsg"));
					return ret;
				}
				Map<String, Object> extra = (Map<String, Object>) data.get("extra");
				token = extra.get(WsConstants.KEY_TOKEN).toString();
//				token = WsAgent.getToken(baseUrl + WsConstants.URL_LOGIN, args);
			}catch(Exception e){
				ret.put("result", false);
				ret.put("msg", "同步失败：请检查网管服务配置");
				return ret;
			}
			//同步行政结构信息
			/*ret = synchroRegionInfo(baseUrl, token);
			if(!(Boolean) ret.get("result")){
				return ret;
			}*/
			//同步服务器信息
			ret = synchroServerInfo(baseUrl, token);
			if(!(Boolean) ret.get("result")){
				return ret;
			}
			//同步设备信息
			ret = synchroDeviceInfo(baseUrl, token,sysBit.toString());
			//同步设备详细信息
			ret = synchroDeviceDetail(baseUrl, token);
			//同步客户信息
			ret = synchroUnitInfo(baseUrl, token);
			if (sysBit == 64) {
				//同步自治云信息
				ret = synchroCloudsInfo(baseUrl, token);
			}
		} catch (Exception e) {
			ret.put("result", false);
			ret.put("msg", "同步失败，内部异常");
			logger.error("从网管服务器同步设备失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return ret;
	
	}
	
	//同步行政结构信息
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroRegionInfo(String baseUrl, String token) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页

		Map<String, Object> data = RestClient.get(baseUrl + WsConstants.URL_REGION_GET, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> regionData = new ArrayList<Map<String, Object>>();
		if(data != null){
			regionData = (List<Map<String, Object>>) data.get("items");
		}
		List<RegionVO> regionInfo = deviceDao.getRegion();
		Map<String, Object> serverInfoMap = new HashMap<String, Object>();
		List<String> regionIds = new ArrayList<String>();
		for(RegionVO region : regionInfo){//先对会管查询出的数据进行分解
			regionIds.add(region.getId());
			serverInfoMap.put(region.getId().toString(), region.getUpdateTime());
		}
		List<Map<String, Object>> addRegion = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateRegion = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> map : regionData){//遍历网管数据
			map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
			if(regionIds.contains(map.get("id"))){//会管数据库已经有该数据，则判断是否更新
				if(serverInfoMap.get(map.get("id").toString()) == null || !serverInfoMap.get(map.get("id").toString()).equals(map.get("updatetime"))){
					updateRegion.add(map);
				}
				regionIds.remove(map.get("id"));//从会管集合里移除会管和网管都有的数据，剩下的就是会管有而网管没有的数据，即需要会管删除数据
			} else {//会管里没有数据，则添加到“新增集合”
				addRegion.add(map);
			}
		}
		if(regionIds.size() > 0){
			deviceDao.delRegion(regionIds);
		}
		if(addRegion.size() > 0){
			batchOp(addRegion, "addRegion");
		}
		if(updateRegion.size() > 0){
//			batchOp(updateRegion, "updateRegion");
			batchOp(updateRegion, "addRegion");
		}
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	
	//同步服务器信息
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroServerInfo(String baseUrl, String token) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页

		Map<String, Object> data = RestClient.get(baseUrl + WsConstants.URL_SERVER_GET, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> serverData = new ArrayList<Map<String, Object>>();
		if(data != null){
			serverData = (List<Map<String, Object>>) data.get("items");
		}
		List<ServerInfoVO> serverInfo = deviceDao.getServerInfo();
		Map<String, Object> serverInfoMap = new HashMap<String, Object>();
		List<String> serverIds = new ArrayList<String>();
		for(ServerInfoVO server : serverInfo){//先对会管查询出的数据进行分解
			serverIds.add(server.getId().toString());
			serverInfoMap.put(server.getId().toString(), server.getUpdateTime());
		}
		List<Map<String, Object>> addServer = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateServer = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> map : serverData){//遍历网管数据
			map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
			if(serverIds.contains(map.get("id"))){//会管数据库已经有该数据，则判断是否更新
				if(serverInfoMap.get(map.get("id").toString()) == null || !serverInfoMap.get(map.get("id").toString()).equals(map.get("updatetime"))){
					updateServer.add(map);
				}
				serverIds.remove(map.get("id"));//从会管集合里移除会管和网管都有的数据，剩下的就是会管有而网管没有的数据，即需要会管删除数据
			} else {//会管里没有数据，则添加到“新增集合”
				addServer.add(map);
			}
		}
		if(serverIds.size() > 0){
			deviceDao.delServerInfo(serverIds);
		}
		if(addServer.size() > 0){
			batchOp(addServer, "addServerInfo");
		}
		if(updateServer.size() > 0){
//			batchOp(updateServer, "updateServerInfo");
			batchOp(updateServer, "addServerInfo");
		}
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	
	//同步设备信息
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroDeviceInfo(String baseUrl, String token,String systemType) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页

		paramsMap.put("type", 2);
		ServerVO svr = sysConfigDao.getServer(paramsMap);
		String wlgtUrl = String.format(WsConstants.DEVICE_URL, svr.getIp(),svr.getPort());
		Map<String, Object> data = RestClient.get(wlgtUrl, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> deviceData = new ArrayList<Map<String, Object>>();
		if(data != null){
			deviceData = (List<Map<String, Object>>) data.get("items");
		}
		List<Map<String, Object>> addDevice = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateDevice = new ArrayList<Map<String, Object>>();
		if (sysBit == 64) {
			for(Map<String, Object> map : deviceData){//遍历网管数据
				if(map.get("xxdz") == null){//兼容没有xxdz字段的网管版本
					map.put("xxdz", "");
				}
				map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
				addDevice.add(map);
			}
			if(addDevice.size() > 0){
				batchOp(addDevice, "addDeviceInfo64");
			}
			if(updateDevice.size() > 0){
//				batchOp(updateDevice, "updateDeviceInfo");
				batchOp(updateDevice, "addDeviceInfo64");
			}

		}else if(sysBit == 16){
			List<DeviceVO> deviceInfo = deviceDao.getDeviceInfo();
			Map<String, Object> deviceInfoMap = new HashMap<String, Object>();
			Map<String, Object> regionInfoMap = new HashMap<String, Object>();
			List<String> devIds = new ArrayList<String>();
			for(DeviceVO device : deviceInfo){//先对会管查询出的数据进行分解
				devIds.add(device.getId());
				deviceInfoMap.put(device.getId().toString(), device.getUpdateTime());
				regionInfoMap.put(device.getId().toString(), device.getRegionId());
			}
			ConstDataVO dataVO = new ConstDataVO();
			for(Map<String, Object> map : deviceData){//遍历网管数据
				//获取网管区号，放入会管常量库
				if (dataVO.getDisplay() == null) {
					dataVO.setValue(map.get("zoneno").toString());
				}
				if(map.get("xxdz") == null){//兼容没有xxdz字段的网管版本
					map.put("xxdz", "");
				}
				map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
				if(devIds.contains(map.get("devkey"))){//会管数据库已经有该数据，则判断是否更新
					if(deviceInfoMap.get(map.get("devkey").toString()) == null || !deviceInfoMap.get(map.get("devkey").toString()).equals(map.get("updatetime"))
						|| regionInfoMap.get(map.get("devkey").toString()) == null || regionInfoMap.get(map.get("devkey").toString()) == "" || !regionInfoMap.get(map.get("devkey").toString()).equals(map.get("regionId"))){
						updateDevice.add(map);
					}
					devIds.remove(map.get("devkey"));//从会管集合里移除会管和网管都有的数据，剩下的就是会管有而网管没有的数据，即需要会管删除数据
				} else {//会管里没有数据，则添加到“新增集合”
					addDevice.add(map);
				}
			}
			if(devIds.size() > 0){
//				deviceDao.delDeviceInfo(devIds);//暂时屏蔽，手机控会需要上传设备列表--2017-08-17
			}
			if(addDevice.size() > 0){
				batchOp(addDevice, "addDeviceInfo");
			}
			if(updateDevice.size() > 0){
//				batchOp(updateDevice, "updateDeviceInfo");
				batchOp(updateDevice, "addDeviceInfo");
			}
			//数据同步成功后向常量数据库插入网管数据区号
			dataVO.setDisplay("会管设备本地区号");
			sysConfigDao.updateSes(dataVO);
		}
		
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	//同步设备详细信息
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroDeviceDetail(String baseUrl, String token) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("type", 2);
		ServerVO svr = sysConfigDao.getServer(paramsMap);
		String wlgtUrl = String.format(WsConstants.URL_DEVDETAIL, svr.getIp(),svr.getPort());
		Map<String, Object> data = RestClient.get(wlgtUrl, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> deviceData = new ArrayList<Map<String, Object>>();
		if(data != null){
			deviceData = (List<Map<String, Object>>) data.get("items");
		}
		//获取当前会管系统类别 16或64位
		//String systemType = Util.getSysProp("cmsweb.systemType");
		if (sysBit == 64) {
			if(deviceData.size() > 0){
				batchOp(deviceData, "addDeviceDetail64");
			}
		}else{
			if(deviceData.size() > 0){
				batchOp(deviceData, "addDeviceDetail");
			}
		}
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	
	//同步自治云信息  2019/2/20 周逸芳合并16位与64位代码时增加  原16位没有
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroCloudsInfo(String baseUrl, String token) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页
		
		Map<String, Object> data = RestClient.get(baseUrl + WsConstants.URL_CLOUD_GET, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> cloudData = new ArrayList<Map<String, Object>>();
		if(data != null){
			cloudData = (List<Map<String, Object>>) data.get("items");
		}
		List<CloudVO> cloudInfo = deviceDao.getCloud();
		Map<String, Object> deviceInfoMap = new HashMap<String, Object>();
		List<String> cloudIds = new ArrayList<String>();
		for(CloudVO cloud : cloudInfo){//先对会管查询出的数据进行分解
			cloudIds.add(cloud.getId());
			deviceInfoMap.put(cloud.getId().toString(), cloud.getUpdateTime());
		}
		List<Map<String, Object>> addCloud = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateCloud = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> map : cloudData){//遍历网管数据
			map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
			if(cloudIds.contains(map.get("id"))){//会管数据库已经有该数据，则判断是否更新
				if(deviceInfoMap.get(map.get("id").toString()) == null || !deviceInfoMap.get(map.get("id").toString()).equals(map.get("updatetime"))){
					updateCloud.add(map);
				}
				cloudIds.remove(map.get("id"));//从会管集合里移除会管和网管都有的数据，剩下的就是会管有而网管没有的数据，即需要会管删除数据
			} else {//会管里没有数据，则添加到“新增集合”
				addCloud.add(map);
			}
		}
		if(cloudIds.size() > 0){
			deviceDao.delCloud(cloudIds);//暂时屏蔽，手机控会需要上传设备列表--2017-08-17
		}
		if(addCloud.size() > 0){
			batchOp(addCloud, "addCloud");
		}
		if(updateCloud.size() > 0){
//			batchOp(updateCloud, "updateCloud");
			batchOp(updateCloud, "addCloud");
		}
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	//同步客户 详细信息
	@SuppressWarnings("unchecked")
	private Map<String, Object> synchroUnitInfo(String baseUrl, String token) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("getcount", -1);//不分页

		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("type", 2);
		ServerVO svr = sysConfigDao.getServer(paramsMap);
		String wlgtUrl = String.format(WsConstants.UNIT_URL, svr.getIp(),svr.getPort());
		Map<String, Object> data = RestClient.get(wlgtUrl, token, args);
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> unitData = new ArrayList<Map<String, Object>>();
		if(data != null){
			unitData = (List<Map<String, Object>>) data.get("items");
		}
		if(unitData.size() > 0){
			for(Map<String, Object> map : unitData){//遍历网管数据
				map.put("createtime", TimeUtil.stampToDate((Long) map.get("createtime")));//时间戳转换成时间
				map.put("updatetime", TimeUtil.stampToDate((Long) map.get("updatetime")));//时间戳转换成时间
			}
			batchOp(unitData, "addUnitInfo");
		}
		ret.put("result", true);
		ret.put("msg", "同步成功");
		return ret;
	}
	
	private int batchOp(List<Map<String, Object>> args, String methodName) throws Exception{
		int ret = 0;
		logger.info("会管同步网管"+methodName+"一共有： "+args.size()+" 条数据");
		if(args.size() > 0){
			logger.info("网管返回的("+methodName+")一条数据样例："+args.get(0));
			//logger.info("网管返回的("+methodName+")一条数据样例："+JSONObject.toJSONString(args));
		}
		if(args.size() > WsConstants.BATCH_SIZE){//数量太大采用分批入库
			List<Map<String, Object>> batchData = new ArrayList<Map<String, Object>>();
			int i = args.size();
			int count = (i % WsConstants.BATCH_SIZE == 0) ? (i / WsConstants.BATCH_SIZE) : (i / WsConstants.BATCH_SIZE + 1);
			for(int s = 1; s <= count; s ++){
				for(int j = (s-1) * WsConstants.BATCH_SIZE; j < s * WsConstants.BATCH_SIZE && j < i; j ++){
					batchData.add(args.get(j));
				}
				ret = reflectCall(deviceDao, methodName, batchData);
				batchData.clear();//清空以便给下个循环用
			}
		}else{
			ret = reflectCall(deviceDao, methodName, args);
		}
		return ret;
	}
	
	/**
	 * 通过反射调用Dao方法
	 */
	private int reflectCall(Object instance, String methodName,
			List<Map<String, Object>> args) throws Exception {
		java.lang.reflect.Method m = instance.getClass().getDeclaredMethod(
				methodName, List.class);
		return (Integer) m.invoke(instance, args);
	}

	/**
	 * 
	 * @Title: getRegions
	 * @Description: 获取行政区域信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public List<RegionVO> getRegions(Map<String, Object> paramsMap) {
		List<RegionVO> list = deviceDao.getRegions(paramsMap);
		List<RegionVO> tempList = new ArrayList<RegionVO>();
		tempList.addAll(list);
		String regionId = null;
		for(RegionVO region : tempList){//去除没有设备的节点
			regionId = region.getId();
			String base = regionId;
			for(int i=regionId.length(); i>=0; i--){
				if(regionId.substring(i-1, i).equals("0")){
					base = base.substring(0,i-1);
				}else{
					break;
				}
			}
			if(base.length() % 2 != 0 && base.length() != 9){//长度为奇数时补零
				if (base.length() == 7) {
					base += "00";
				}else{
					base += "0";
				}
			}else if(base.length() == 10){
				base += "00";
			}
			else if(base.length() == 8){
				base += "0";
			}
			paramsMap.put("regionId", base);
			if(deviceDao.getDevicesCountOfRegion(paramsMap) < 1){
				list.remove(region);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @Title: getUnits
	 * @Description: 获取成员单位信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public List<RegionVO> getUnits(Map<String, Object> paramsMap) {
		return deviceDao.getUnits(paramsMap);
	}

	/**
	 * 
	 * @Title: getDevices
	 * @Description: 获取设备信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public List<DeviceVO> getDevices(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevices(map, new RowBounds());
		}
		return deviceDao.getDevices(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getDevicesOfRegion
	 * @Description: 获取设备信息（获取区域节点下的所有设备，包含该区域所有子节点下的所有设备）
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public List<DeviceVO> getDevicesOfRegion(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevicesOfRegion(map, new RowBounds());
		}
		return deviceDao.getDevicesOfRegion(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getDevicesOfRegionCount
	 * @Description: 获取设备信息（获取区域节点下的所有设备，包含该区域所有子节点下的所有设备）
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getDevicesOfRegionCount(Map<String, Object> map) {
		return deviceDao.getDevicesOfRegionCount(map);
	}

	/**
	 * 
	 * @Title: getRoot
	 * @Description: 获取行政区域信息的第一级节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<RegionVO> getRoot(Map<String, Object> paramsMap) {
		List<RegionVO> list = deviceDao.getRoot(paramsMap);
		List<RegionVO> tempList = new ArrayList<RegionVO>();
		tempList.addAll(list);
		String regionId = null;
		for(RegionVO region : tempList){//去除没有设备的节点
			regionId = region.getId();
			String base = regionId;
			for(int i=regionId.length(); i>=0; i--){
				if(regionId.substring(i-1, i).equals("0")){
					base = base.substring(0,i-1);
				}else{
					break;
				}
			}
			if(base.length() % 2 != 0){//长度为奇数时补零
				base += "0";
			}
			paramsMap.put("regionId", base);
			if(deviceDao.getDevicesCountOfRegion(paramsMap) < 1){
				list.remove(region);
			}
		}
		return list;
	} 

	/**
	 * 
	 * @Title: getChild
	 * @Description: 获取行政区域信息的子节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<RegionVO> getChild(Map<String, Object> paramsMap) {
		List<RegionVO> list = new ArrayList<RegionVO>();
		// 获取成员单位
		paramsMap.put("unitPid", "-1");// 获取行政区域下成员单位的根节点
		paramsMap.put("regionid", paramsMap.get("pId"));
		if(paramsMap.get("pId") != null && paramsMap.get("pId").toString().length() < 12){// 行政区域ID长度为12位，<12说明是成员单位的pif
			paramsMap.put("unitPid", paramsMap.get("pId"));
			if(!paramsMap.get("pId").equals("-1")){
				paramsMap.remove("regionid");
			}
		}else{
			// 获取行政区域子节点
			list = deviceDao.getChild(paramsMap);
			List<RegionVO> tempList = new ArrayList<RegionVO>();
			tempList.addAll(list);
			String regionId = null;
			for(RegionVO region : tempList){//去除没有设备的节点
				regionId = region.getId();
				String base = regionId;
				for(int i=regionId.length(); i>=0; i--){
					if(regionId.substring(i-1, i).equals("0")){
						base = base.substring(0,i-1);
					}else{
						break;
					}
				}
				if(base.length() % 2 != 0 && base.length() != 9){//长度为奇数时补零
					if (base.length() == 7) {
						base += "00";
					}else{
						base += "0";
					}
				}else if(base.length() == 10){
					base += "00";
				}
				else if(base.length() == 8){
					base += "0";
				}
				paramsMap.put("regionId", base);
				if(deviceDao.getDevicesCountOfRegion(paramsMap) < 1){
					list.remove(region);
				}
			}
		}
		List<RegionVO> unitList = deviceDao.getUnitChild(paramsMap);
		
		// 合并成员单位列表和行政区域列表
		List<RegionVO> allList = new ArrayList<RegionVO>();
		allList.addAll(unitList);
		allList.addAll(list);
		return allList;
	}
	
	/**
	 * 
	 * @Title: auth
	 * @Description: 给用户组授权设备（增减合一）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> auth(Map<String, Object> paramsMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("msg", "授权失败");
		resultMap.put("result", false);
		try {
			String devIds = paramsMap.get("devIds").toString();
			//根据RegionId取对应的设备
			String regionIds = paramsMap.get("regionIds").toString();
			if(StringUtils.isNotBlank(regionIds)){
				List<String> searchedIds = new ArrayList<String>();
				boolean isSkip = false;
				String[] regionIdArr = regionIds.split(",");//参数传入的ID数组
				for(String regionId : regionIdArr){
					String base = regionId;
					for(int i=regionId.length(); i>=0; i--){
						if(regionId.substring(i-1, i).equals("0")){
							base = base.substring(0,i-1);
						}else{
							break;
						}
					}
					
					for(String searchedId : searchedIds){//判断当前节点的父节点是否已经被搜索过
						if(base.startsWith(searchedId)){
							isSkip = true;
							break;
						}
					}
					if(isSkip){//如果是，则不必再搜索
						continue;
					}
					paramsMap.put("regionId", base);
					List<DeviceVO> list = deviceDao.getDevicesOfRegion(paramsMap, new RowBounds());
					for(DeviceVO device : list){//将节点下的所有设备都拼接到devIds字符串中
						if(("," + devIds + ",").indexOf("," + device.getId() + ",") < 0){//去除重复的设备（防错处理）
							devIds+=","+device.getId();
						}
					}
					searchedIds.add(base);
				}
			}
			if(devIds.length() > 0 && devIds.indexOf(",") == 0){
				devIds = devIds.substring(1, devIds.length());
			}
			//获取用户组内已授权的设备
			List<DeviceVO> authed = deviceDao.getAuthed(paramsMap);
			List<String> authedIds = new ArrayList<String>();//已授权的设备号集合
			for(DeviceVO device : authed){
				authedIds.add(device.getId());
			}
			//2019/2/20 周逸芳合并16位与64位代码时增加  原16位为Integer 64位为String
			List<String> addIds = new ArrayList<String>();//需要授权的设备
			if(StringUtils.isNotBlank(devIds)){
				String[] deviceuuids = devIds.split(",");//参数传入的ID数组
				for(int i = 0 ; i < deviceuuids.length; i ++){
					if(deviceuuids[i].length() > 10){
						continue;
					}
					if(!authedIds.contains(Integer.parseInt(deviceuuids[i]))){
						//2019/2/20 周逸芳合并16位与64位代码时增加  原16位为Integer 64位为String
						addIds.add(deviceuuids[i]);
					}else{
						authedIds.remove(Integer.valueOf(deviceuuids[i]));//从已授权的集合中移除和参数传入的ID相同的数据，剩下的就是需要解除授权的设备
					}
				}
			}
			//处理需要授权的设备
			if(addIds.size() > 0){
				//查找用户组下默认会场的uuid
				String uuid = null;
				paramsMap.put("name", GlobalConstants.DEFAULT_GROUP_NAME);
				List<DeviceGroupVO> defGroup = deviceDao.searchGroup(paramsMap);//查找用户分组下是否有默认会场
				DeviceGroupVO devGroup = new DeviceGroupVO();
				if(defGroup.size() < 1){
					devGroup.setName(GlobalConstants.DEFAULT_GROUP_NAME);
					deviceDao.addDeviceGroup(devGroup);
					uuid = devGroup.getUuid();
					//添加默认会场和用户组的关联关系
					List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("uuid", uuid);
					items.add(item);
					paramsMap.put("list", items);
					deviceDao.addDevGroup2UserGroup(paramsMap);
				}else{
					uuid = defGroup.get(0).getUuid();
				}
				paramsMap.put("groupuuid", uuid);//会场ID
				if(addIds.size() > WsConstants.BATCH_SIZE){//数量太大采用分批入库
					List<String> batchData = new ArrayList<String>();
					int i = addIds.size();
					int count = (i % WsConstants.BATCH_SIZE == 0) ? (i / WsConstants.BATCH_SIZE) : (i / WsConstants.BATCH_SIZE + 1);
					for(int s = 1; s <= count; s ++){
						for(int j = (s-1) * WsConstants.BATCH_SIZE; j < s * WsConstants.BATCH_SIZE && j < i; j ++){
							batchData.add(addIds.get(j));
						}
						paramsMap.put("devIds", batchData);
						//授权设备
						deviceDao.auth(paramsMap);
						//将新授权（未分组的）设备添加到默认会场
						deviceDao.addDevice2Group(paramsMap);
						batchData.clear();
					}
				}else{
					paramsMap.put("devIds", addIds);
					//授权设备
					deviceDao.auth(paramsMap);
					//将新授权（未分组的）设备添加到默认会场
					deviceDao.addDevice2Group(paramsMap);
				}
//				paramsMap.put("devids", addIds);
//				List<DeviceTreeVO> addList = deviceDao.getDevTreeData(paramsMap);
//				paramsMap.put("addlist", addList);
			}
			//处理需要解除授权的设备
			if(authedIds.size() > 0){
//				paramsMap.put("devids", authedIds);
//				List<DeviceTreeVO> delList = deviceDao.getDevTreeData(paramsMap);
//				paramsMap.put("removelist", delList);
				//查找用户组下的会场列表
				paramsMap.remove("name");//防止对查找造成干扰
				List<DeviceGroupVO> groups = deviceDao.getGroupList(paramsMap, new RowBounds());
				List<String> uuids = new ArrayList<String>();
				for(DeviceGroupVO group : groups){
					uuids.add(group.getUuid());
				}
				paramsMap.put("devIds", authedIds);
				deviceDao.delAuthed(paramsMap);//解除授权
				paramsMap.put("uuids", uuids);
				deviceDao.rmFromGroup(paramsMap);//删除用会场-设备对应关系
			}
//			new SendData(paramsMap, addIds, authedIds).start();//异步推送消息
			resultMap.put("result", true);
			resultMap.put("msg", "授权成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: addAuth
	 * @Description: 给用户组授权设备（增加）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> addAuth(Map<String, Object> paramsMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("msg", "授权失败");
		resultMap.put("result", false);
		try {
			String devIds = paramsMap.get("devIds").toString();
			//根据RegionId取对应的设备
			String regionIds = paramsMap.get("regionIds").toString();
			if(StringUtils.isNotBlank(regionIds)){
				List<String> searchedIds = new ArrayList<String>();
				boolean isSkip = false;
				String[] regionIdArr = regionIds.split(",");//参数传入的ID数组
				for(String regionId : regionIdArr){
					String base = regionId;
					for(int i=regionId.length(); i>=0; i--){
						if(regionId.substring(i-1, i).equals("0")){
							base = base.substring(0,i-1);
						}else{
							break;
						}
					}
					
					for(String searchedId : searchedIds){//判断当前节点的父节点是否已经被搜索过
						if(base.startsWith(searchedId)){
							isSkip = true;
							break;
						}
					}
					if(isSkip){//如果是，则不必再搜索
						continue;
					}
					paramsMap.put("regionId", base);
					List<DeviceVO> list = deviceDao.getDevicesOfRegion(paramsMap, new RowBounds());
					for(DeviceVO device : list){//将节点下的所有设备都拼接到devIds字符串中
						if(("," + devIds + ",").indexOf("," + device.getId() + ",") < 0){//去除重复的设备（防错处理）
							devIds+=","+device.getId();
						}
					}
					searchedIds.add(base);
				}
			}
			if(devIds.length() > 0 && devIds.indexOf(",") == 0){
				devIds = devIds.substring(1, devIds.length());
			}
			
			List<String> addIds = new ArrayList<String>();//需要授权的设备
			if(StringUtils.isNotBlank(devIds)){
				String[] deviceuuids = devIds.split(",");//参数传入的ID数组
				for(int i = 0 ; i < deviceuuids.length; i ++){
					addIds.add(deviceuuids[i]);
				}
			}
			//处理需要授权的设备
			if(addIds.size() > 0){
				//查找用户组下默认会场的uuid
				String uuid = null;
				paramsMap.put("name", GlobalConstants.DEFAULT_GROUP_NAME);
				List<DeviceGroupVO> defGroup = deviceDao.searchGroup(paramsMap);//查找用户分组下是否有默认会场
				DeviceGroupVO devGroup = new DeviceGroupVO();
				if(defGroup.size() < 1){
					devGroup.setName(GlobalConstants.DEFAULT_GROUP_NAME);
					deviceDao.addDeviceGroup(devGroup);
					uuid = devGroup.getUuid();
					//添加默认会场和用户组的关联关系
					List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("uuid", uuid);
					items.add(item);
					paramsMap.put("list", items);
					deviceDao.addDevGroup2UserGroup(paramsMap);
				}else{
					uuid = defGroup.get(0).getUuid();
				}
				paramsMap.put("groupuuid", uuid);//会场ID
				if(addIds.size() > WsConstants.BATCH_SIZE){//数量太大采用分批入库
					List<String> batchData = new ArrayList<String>();
					int i = addIds.size();
					int count = (i % WsConstants.BATCH_SIZE == 0) ? (i / WsConstants.BATCH_SIZE) : (i / WsConstants.BATCH_SIZE + 1);
					for(int s = 1; s <= count; s ++){
						for(int j = (s-1) * WsConstants.BATCH_SIZE; j < s * WsConstants.BATCH_SIZE && j < i; j ++){
							batchData.add(addIds.get(j));
						}
						paramsMap.put("devIds", batchData);
						//授权设备
						deviceDao.auth(paramsMap);
						//将新授权（未分组的）设备添加到默认会场
						deviceDao.addDevice2Group(paramsMap);
						batchData.clear();
					}
				}else{
					paramsMap.put("devIds", addIds);
					//授权设备
					deviceDao.auth(paramsMap);
					//将新授权（未分组的）设备添加到默认会场
					deviceDao.addDevice2Group(paramsMap);
				}
//				paramsMap.put("devids", addIds);
//				List<DeviceTreeVO> addList = deviceDao.getDevTreeData(paramsMap);
//				paramsMap.put("addlist", addList);
			}
//			new SendData(paramsMap, addIds, new ArrayList<String>()).start();//异步推送消息
			resultMap.put("result", true);
			resultMap.put("msg", "授权成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: delAuth
	 * @Description: 给用户组授权设备（减少）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> delAuth(Map<String, Object> paramsMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("msg", "授权失败");
		resultMap.put("result", false);
		try {
			String devIds = paramsMap.get("devIds").toString();
			//根据RegionId取对应的设备
			String regionIds = paramsMap.get("regionIds").toString();
			if(StringUtils.isNotBlank(regionIds)){
				List<String> searchedIds = new ArrayList<String>();
				boolean isSkip = false;
				String[] regionIdArr = regionIds.split(",");//参数传入的ID数组
				for(String regionId : regionIdArr){
					String base = regionId;
					for(int i=regionId.length(); i>=0; i--){
						if(regionId.substring(i-1, i).equals("0")){
							base = base.substring(0,i-1);
						}else{
							break;
						}
					}
					
					for(String searchedId : searchedIds){//判断当前节点的父节点是否已经被搜索过
						if(base.startsWith(searchedId)){
							isSkip = true;
							break;
						}
					}
					if(isSkip){//如果是，则不必再搜索
						continue;
					}
					paramsMap.put("regionId", base);
					List<DeviceVO> list = deviceDao.getDevicesOfRegion(paramsMap, new RowBounds());
					for(DeviceVO device : list){//将节点下的所有设备都拼接到devIds字符串中
						if(("," + devIds + ",").indexOf("," + device.getId() + ",") < 0){//去除重复的设备（防错处理）
							devIds+=","+device.getId();
						}
					}
					searchedIds.add(base);
				}
			}
			if(devIds.length() > 0 && devIds.indexOf(",") == 0){
				devIds = devIds.substring(1, devIds.length());
			}
			List<String> delIds = new ArrayList<String>();//需要解除授权的设备
			if(StringUtils.isNotBlank(devIds)){
				String[] deviceuuids = devIds.split(",");//参数传入的ID数组
				for(int i = 0 ; i < deviceuuids.length; i ++){
					delIds.add(deviceuuids[i]);
				}
			}
			//处理需要解除授权的设备
			if(delIds.size() > 0){
//				paramsMap.put("devids", delIds);
//				List<DeviceTreeVO> delList = deviceDao.getDevTreeData(paramsMap);
//				paramsMap.put("removelist", delList);
				//查找用户组下的会场列表
				paramsMap.remove("name");//防止对查找造成干扰
				List<DeviceGroupVO> groups = deviceDao.getGroupList(paramsMap, new RowBounds());
				List<String> uuids = new ArrayList<String>();
				for(DeviceGroupVO group : groups){
					uuids.add(group.getUuid());
				}
				paramsMap.put("devIds", delIds);
				deviceDao.delAuthed(paramsMap);//解除授权
				paramsMap.put("uuids", uuids);
				deviceDao.rmFromGroup(paramsMap);//删除用会场-设备对应关系
			}
//			new SendData(paramsMap, new ArrayList<String>(), delIds).start();//异步推送消息
			resultMap.put("result", true);
			resultMap.put("msg", "授权成功");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			logger.error("授权设备失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	
	class SendData extends Thread{
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		List<String> addIds = null;
		List<String> delIds = null;
		SendData(Map<String, Object> paramsMap, List<String> addIds, List<String> delIds){
			this.paramsMap = paramsMap;
			this.addIds = addIds;
			this.delIds = delIds;
		}
		public void run(){
			paramsMap.put("type", 1);
			ServerVO ser = sysConfigDao.getServer(paramsMap);
			if (null != ser) {
				
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonRoot = new HashMap<String, Object>();
				jsonRoot.put("cmd", VSMeet.MSG_USERGROUP_RESCHANGE);
				jsonRoot.put("groupid", paramsMap.get("groupId"));
				Map<String, Object> jsonContent = new HashMap<String, Object>();
				//此处查询所有被移除的设备推送到会议管理服务器
				//添加新授权设备的树结构
				jsonContent.put("addlist", paramsMap.get("addlist"));
				//添加被移除授权的设备树结构
				jsonContent.put("removelist", paramsMap.get("removelist"));
				jsonRoot.put("content", jsonContent);
				String json;
				try {
					json = objectMapper
							.writeValueAsString(jsonRoot);
					SocketClient client = new SocketClient(ser.getIp(),
							Integer.parseInt(ser.getPort()));
					if(0 == client.start())
						client.sendData(VSMeet.MSG_USERGROUP_RESCHANGE, json.getBytes("utf-8"));
					else
						logger.error("推送资源改变消息失败：连接会议管理服务器失败");
					Map<String, Object> map = client.recvData(Map.class);
					if (null != map) {
						// 处理返回数据
						if (null == map.get("cmd")
								|| Integer.parseInt(map.get("cmd").toString()) != VSMeet.MSG_USERGROUP_RESCHANGE+1) {
							throw new Exception("推送资源改变消息失败：未收到服务器反馈数据接收标识!");
						}
						else
						{
							if (null == map.get("result")
									|| Integer
											.parseInt(map.get("result").toString()) != 0) {
								logger.error("推送资源改变消息失败：连接会议管理服务器失败");
							} 
						}
					} else {
						logger.error("推送资源改变消息失败");
					}
					if(0 != client.close())
					{
						logger.error("推送资源改变消息失败：关闭与会议管理服务器连接通道失败");
					}
				} catch (Exception e) {
					logger.error("推送资源改变消息失败：未知会议管理服务器");
				}
			}
			else{
				logger.error("推送资源改变消息失败：未知会议管理服务器");
			}
		}
	}

	/**
	 * 
	 * @Title: getAuthed
	 * @Description: 获取已经授权给用户组的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> getAuthed(Map<String, Object> paramsMap){
		return deviceDao.getAuthed(paramsMap);
	}
	
	/**
	 * 
	 * @Title: getAuthedOfRegion
	 * @Description: 获取某一节点下户组内已授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> getAuthedOfRegion(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getAuthedOfRegion(map, new RowBounds());
		}
		return deviceDao.getAuthedOfRegion(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getAuthedOfRegions
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> getAuthedOfRegions(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getAuthedOfRegions(map, new RowBounds());
		}
		return deviceDao.getAuthedOfRegions(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getNotAuthedOfRegion
	 * @Description: 获取某一节点下户组内未授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> getNotAuthedOfRegion(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getNotAuthedOfRegion(map, new RowBounds());
		}
		return deviceDao.getNotAuthedOfRegion(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getNotAuthedOfRegions
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceVO> getNotAuthedOfRegions(Map<String, Object> map){
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getNotAuthedOfRegions(map, new RowBounds());
		}
		return deviceDao.getNotAuthedOfRegions(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: delAuthed
	 * @Description: 删除用户组内已授权的设备（devIds为空时，删除组内所有已授权设备）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> delAuthed(Map<String, Object> paramsMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			//从用户组中移除授权的设备
			deviceDao.delAuthed(paramsMap);
			//从用户组的会场中移除授权的设备
			deviceDao.rmFromGroup(paramsMap);
			resultMap.put("result", true);
			resultMap.put("msg", "删除用户组内已授权设备成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "删除用户组内已授权设备失败");
			logger.error("删除用户组内已授权设备失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: rmFromGroup
	 * @Description: 移除会场内的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> rmFromGroup(Map<String, Object> paramsMap){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("msg", "移动设备成功");
		result.put("result", true);
		try {
			//判断会场中是否有设备被占用
			List<DeviceVO> devices = deviceDao.isDeviceDeletable(paramsMap);
			if(devices.size() > 0){//只要有有效预约在使用组内设备，就不允许删除该设备
				result.put("result", false);
				result.put("msg", "当前有预约使用设备"+devices.get(0).getId()+"，不允许移动");
				return result;
			}
			deviceDao.rmFromGroup(paramsMap);
		} catch (Exception e) {
			logger.error("移动设备失败", e);
			result.put("msg", "移除设备失败");
			result.put("result", false);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return result;
	}
	
	/**
	 * 
	 * @Title: rmFromGroup
	 * @Description: 移除会场内的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> mvDev2Group(Map<String, Object> paramsMap){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("msg", "移动设备成功");
		result.put("result", true);
		try {
			//判断会场中是否有设备被占用
			List<DeviceVO> devices = deviceDao.isDeviceDeletable(paramsMap);
			if(devices.size() > 0){//只要有有效预约在使用组内设备，就不允许删除该设备
				result.put("result", false);
				result.put("msg", "当前有预约使用设备"+devices.get(0).getId()+"，不允许移动");
				return result;
			}
			deviceDao.rmFromGroup(paramsMap);
			deviceDao.add2Group(paramsMap);
			/*paramsMap.put("type", 1);
			ServerVO ser = sysConfigDao.getServer(paramsMap);
			if (null != ser) {
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonRoot = new HashMap<String, Object>();
				jsonRoot.put("cmd", VSMeet.MSG_USERGROUP_ADDDEV);
				Map<String, Object> jsonContent = new HashMap<String, Object>();
				//此处查询所有被移除的设备推送到会议管理服务器
				
				List<DeviceVO> list = deviceService.getDevicesByDevGroups(paramsMap);
				jsonContent.put("data", list);
				jsonContent.put("destgroupid", paramsMap.get("destGroupId").toString());
				jsonRoot.put("content", jsonContent);
				
				String json = objectMapper
						.writeValueAsString(jsonRoot);
				SocketClient client = new SocketClient(ser.getIp(),
						Integer.parseInt(ser.getPort()));
				client.start();
				client.sendData(VSMeet.MSG_USERGROUP_ADDDEV, json.getBytes(), VSMeet.MSG_PUSH);
				client.close();
			}*/
		} catch (Exception e) {
			logger.error("移动设备失败", e);
			result.put("msg", "移动设备失败");
			result.put("result", false);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return result;
	}
	
	/**
	 * 
	 * @Title: add2Group
	 * @Description: 往会场添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int add2Group(Map<String, Object> paramsMap){
		return deviceDao.add2Group(paramsMap);
	}
	
	/**
	 * 
	 * @Title: searchRegion
	 * @Description: 模糊查询行政结构
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public List<RegionVO> searchRegion(Map<String, Object> paramsMap){
		List<String> regionIds = (List<String>) paramsMap.get("regionIds");
		List<String> searchIds = new ArrayList<String>();//根据regionIds拆分，查找其对应的上级行政节点
		for(String str : regionIds){
			RegionUtil.getAllPids(str, searchIds);
		}
		paramsMap.put("searchIds", searchIds);
		return deviceDao.searchRegion(paramsMap);
	}
	
	/**
	 * 
	 * @Title: searchUnit
	 * @Description: 模糊查询行政结构
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public List<RegionVO> searchUnit(Map<String, Object> paramsMap){
		List<RegionVO> searchList = new ArrayList<RegionVO>();
		List<String> unitIds = (List<String>) paramsMap.get("unitIds");
		for(String str : unitIds){
			String pid = str;
			while(!pid.equals("-1")){
				paramsMap.put("unitId", pid);
				List<RegionVO> unit = deviceDao.getUnitChild(paramsMap);
				if(unit.size() < 1)
					break;
				pid = unit.get(0).getPid();
				if(pid.equals("-1"))
					unit.get(0).setPid(unit.get(0).getRegionId());
				searchList.addAll(unit);
				unit.clear();
			}
		}
		return searchList;
	}

	/**
	 * 
	 * @Title: getLock
	 * @Description: 获取用户组锁定状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<UserVO> getLock(Map<String, Object> paramsMap) {
		return deviceDao.getLock(paramsMap);
	}

	/**
	 * 
	 * @Title: lock
	 * @Description: 锁定用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int lock(Map<String, Object> paramsMap) {
		return deviceDao.lock(paramsMap);
	}

	/**
	 * 
	 * @Title: unlock
	 * @Description: 解锁用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int unlock(Map<String, Object> paramsMap) {
		return deviceDao.unlock(paramsMap);
	}

	public List<DeviceVO> getDevicesByDevGroups(Map<String, Object> paramsMap) {
		return deviceDao.getDevicesByDevGroups(paramsMap);
	}
	
	/**
	 * 
	 * @Title: userGroupList
	 * @Description: 获取用户组列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<UserGroupVO> getUserGroupList(Map<String, Object> paramsMap){
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getUserGroupList(paramsMap, new RowBounds());
		}
		return deviceDao.getUserGroupList(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}

	/**
	 * 
	 * @Title: userGroupList
	 * @Description: 获取用户组列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int getUserGroupCount(Map<String, Object> paramsMap){
		return deviceDao.getUserGroupCount(paramsMap);
	}
	
	/**
	 * 
	 * @Title: searchGroup
	 * @Description: 精确查找用户组下的会场（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceGroupVO> searchGroup(Map<String, Object> paramsMap){
		return deviceDao.searchGroup(paramsMap);
	}
	
	/**
	 * 
	 * @Title: groupList
	 * @Description: 获取用户组下的会场列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceGroupVO> getGroupList(Map<String, Object> paramsMap){
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getGroupList(paramsMap, new RowBounds());
		}
		return deviceDao.getGroupList(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getGroupCount
	 * @Description: 获取用户组下的会场总数
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int getGroupCount(Map<String, Object> paramsMap) {
		return deviceDao.getGroupCount(paramsMap);
	}

	/**
	 * 
	 * @Title: generateGroups
	 * @Description: 快速生成会场列表（一个设备一个会场）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public Map<String, Object> generateGroups(Map<String, Object> paramsMap) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try{
			//获取用户组下默认会场内的设备
			paramsMap.put("devGroupName", GlobalConstants.DEFAULT_GROUP_NAME);
			List<DeviceVO> devsInDefGroup = deviceDao.getDeviceList(paramsMap, new RowBounds());//获取默认会场下的设备列表
			if(devsInDefGroup.size() == 0){
				resultMap.put("msg", "默认会场中没有设备");
				resultMap.put("result", false);
				return resultMap;
			}
			String uuid = devsInDefGroup.get(0).getGroupId();//获取默认会场ID
			List<String> oldGroupNames = new ArrayList<String>();//存放用户组内已存在的会场列表
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();//存放需要添加到用户组-设备表、用户组-会场表的数据列表
			Map<String, Object> item = null;
			List<String> devIds = new ArrayList<String>();//存放需要从默认会场移除的设备列表
			List<DeviceGroupVO> devGroups = deviceDao.searchGroup(paramsMap);//查询用户组内已存在的会场列表
			Map<String, String> name2uuid = new HashMap<String, String>();//存放会场名称和会场ID的映射集合
			//判断是否有重名的会场（未分组的设备名称和会场名称重复）
			for(DeviceGroupVO devGroup : devGroups){
				oldGroupNames.add(devGroup.getName());
				name2uuid.put(devGroup.getName(), devGroup.getUuid());
			}
			for(DeviceVO device : devsInDefGroup){
				if(!oldGroupNames.contains(device.getName())){
					//获取UUID，构造要插入的数据
					item = new HashMap<String, Object>();
					item.put("uuid", getUuidByName(name2uuid,device.getName()));//先查询UUID（多个设备同名的，归到同一会场）
					item.put("name", device.getName());
					item.put("devId", device.getId());//设备ID
					items.add(item);
				}else{//与已有的会场重名的设备不再创建新会场
					item = new HashMap<String, Object>();
					item.put("uuid", name2uuid.get(device.getName()));//先查询UUID
					item.put("name", device.getName());
					item.put("devId", device.getId());//设备ID
					items.add(item);
				}
				devIds.add(device.getId());
			}
			
			if(items.size() > WsConstants.BATCH_SIZE){//数量太大采用分批入库
				List<Map<String, Object>> batchData = new ArrayList<Map<String, Object>>();
				int i = items.size();
				int count = (i % WsConstants.BATCH_SIZE == 0) ? (i / WsConstants.BATCH_SIZE) : (i / WsConstants.BATCH_SIZE + 1);
				for(int s = 1; s <= count; s ++){
					for(int j = (s-1) * WsConstants.BATCH_SIZE; j < s * WsConstants.BATCH_SIZE && j < i; j ++){
						batchData.add(items.get(j));
					}
					//根据设备名批量创建会场
					paramsMap.put("list", batchData);
					deviceDao.generateGroups(paramsMap);
					//创建用户组和会场的关联
					deviceDao.addDevGroup2UserGroup(paramsMap);
					//将设备划分到其它会场，会场和设备的一对一关系
					deviceDao.add2Groups(batchData);
					batchData.clear();
				}
			}else{
				//根据设备名批量创建会场
				paramsMap.put("list", items);
				deviceDao.generateGroups(paramsMap);
				//创建用户组和会场的关联
				deviceDao.addDevGroup2UserGroup(paramsMap);
				//将设备划分到其它会场，会场和设备的一对一关系
				deviceDao.add2Groups(items);
			}
			
			//从默认会场中删除需要划分到其它会场的设备
			paramsMap.put("uuids", uuid.split(","));
			paramsMap.put("devIds", devIds);
			deviceDao.rmFromGroup(paramsMap);
			resultMap.put("msg", "自动生成会场成功");
			resultMap.put("result", true);
		} catch (Exception e) {
			logger.error("快速生成会场列表失败", e);
			resultMap.put("msg", "自动生成会场失败");
			resultMap.put("result", false);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	
	private String getUuidByName(Map<String,String> name2uuid, String name){
		if(!name2uuid.containsKey(name)){
			name2uuid.put(name, UUIDGenerator.getUuid());
		}
		return name2uuid.get(name);
	}
	/**
	 * 
	 * @Title: searchGroupByUuid
	 * @Description: 根据会场ID会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<DeviceGroupVO> searchGroupByUuid(Map<String, Object> paramsMap) {
		return deviceDao.searchGroupByUuid(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getDevicesCount
	 * @Description: 获取设备总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getDevicesCount(Map<String, Object> paramsMap) {
		return deviceDao.getDevicesCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getAuthedOfRegionCount
	 * @Description: 获取某一节点下户组内已授权设备总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getAuthedOfRegionCount(Map<String, Object> paramsMap) {
		return deviceDao.getAuthedOfRegionCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getAuthedOfRegionsCount
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getAuthedOfRegionsCount(Map<String, Object> paramsMap) {
		return deviceDao.getAuthedOfRegionsCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getNotAuthedOfRegionCount
	 * @Description: 获取某一节点下户组内未授权设备的总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getNotAuthedOfRegionCount(Map<String, Object> paramsMap) {
		return deviceDao.getNotAuthedOfRegionCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getNotAuthedOfRegionsCount
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）的总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public int getNotAuthedOfRegionsCount(Map<String, Object> paramsMap) {
		return deviceDao.getNotAuthedOfRegionsCount(paramsMap);
	}
	
	public int addDeviceFromPhone(List<DeviceVO> devList){
		return deviceDao.batchAdd(devList);
	}
	/**
	 * @param  
	 * 
	 * @Title: getDeviceListBooks
	 * @Description: 通讯录资源管理获取设备列表
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	public List<DeviceVO> getDeviceListBooks(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceListBooks(map, new RowBounds());
		}
		return deviceDao.getDeviceListBooks(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * @param  
	 * 
	 * @Title: getDeviceListBooksCount
	 * @Description: 通讯录资源管理获取设备列表总数目
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int getDeviceListBooksCount(Map<String, Object> paramsMap) {
		return deviceDao.getDeviceListBooksCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: getDeviceGroupBooks
	 * @Description: 用户管理页面初始群授权获取
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	public List<DeviceGroupVO> getDeviceGroupBooks(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDeviceGroupBooks(map, new RowBounds());
		}
		return deviceDao.getDeviceGroupBooks(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * @param  
	 * 
	 * @Title: getDeviceGroupBooksCount
	 * @Description: 用户管理页面初始群授权获取总条数
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	public int getDeviceGroupBooksCount(Map<String, Object> paramsMap) {
		return deviceDao.getDeviceGroupBooksCount(paramsMap);
	}
	
	/**
	 * 
	 * TODO 获取联系人（设备）列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getUserDevices(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getUserDevices(paramsMap, new RowBounds());
		}
		return deviceDao.getUserDevices(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}
	
	/**
	 * 
	 * TODO 获取联系人（设备）列表总数
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int getUserDevicesCount(Map<String, Object> paramsMap) {
		return deviceDao.getUserDevicesCount(paramsMap);
	}

	/**
	 * 
	 * TODO 获取联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceGroupVO> getUserDevGroups(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getUserDevGroups(paramsMap, new RowBounds());
		}
		return deviceDao.getUserDevGroups(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getUserDevGroups
	 * @Description: 根据用户id获取联系人（设备）群组列表总数
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	public int getUserDevGroupsCount(Map<String, Object> paramsMap) {
		return deviceDao.getUserDevGroupsCount(paramsMap);
	}

	/**
	 * 
	 * TODO 根据群组id获取联系人（设备）列表
	 * @author 谢程算
	 * @date 2017年10月18日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getGroupDevs(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getGroupDevs(paramsMap, new RowBounds());
		}
		return deviceDao.getGroupDevs(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getGroupDevsCount
	 * @Description: 根据群组id获取联系人（设备）列表总数
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	public int getGroupDevsCount(Map<String, Object> paramsMap) {
		return deviceDao.getGroupDevsCount(paramsMap);
	}

	/**
	 * 
	 * TODO 添加联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> addUserDevGroups(DeviceGroupVO dgv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try{
			//创建群
//			dgv.setDataType(2);//私有群组
			deviceDao.addDeviceGroup(dgv);
			//建立群和联系人（设备）的关联关系
			String[] uuids = dgv.getDevices().split(";");
			if (uuids.length > 0) {
				String[] props = null;
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = null;
				Map<String, Object> params = null ;
				for(String uuid : uuids){ //2019/2/20 周逸芳合并16位与64位代码时
					props = uuid.split(":");
					if(props.length < 2){
						continue;
					}
					params = new HashMap<String, Object>();
					map = new HashMap<String, Object>();
					if (props[0].length() == 11) {//64位
						String devId = ChangeNumberUtils.getDeviceTo20(props[0]); 
						map.put("devId", devId);
					} else {//16位
						params.put("devno", props[0]) ;
						if(props.length < 3 ){
							params.put("zoneno", this.getLocalZoneno()) ;
						}else{
							params.put("zoneno", props[2]) ;
						}
						//通过5位设备号和区号，查询该设备
						List<DeviceVO> devInfo = this.getThisDevInfo(params) ;
						if(null == devInfo || devInfo.isEmpty()){
							continue ;
						}
						map.put("devId", devInfo.get(0).getZonedevno());
					}
					map.put("groupuuid", dgv.getUuid());
					map.put("devFunc", props[1]);
					list.add(map);
				}
				paramsMap.put("list", list);
				deviceDao.addDevices2Group(paramsMap);
			}
			//建立用户和群的关联关系
			paramsMap.put("userId", dgv.getUserId());
			paramsMap.put("groupuuid", dgv.getUuid());
			deviceDao.addUserDevGroups(paramsMap);
			resultMap.put("msg", "操作成功");
			resultMap.put("result", WsConstants.OK);
		}catch(Exception e){
			logger.error("添加联系人群组失败", e);
			resultMap.put("msg", "添加联系人群组失败，内部异常");
			resultMap.put("result", WsConstants.ERROR);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> editUserDevGroups(DeviceGroupVO dgv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try{
			//获取会管设备本地区号
/*			String zoneno = this.getLocalZoneno() ;
			//通过5位设备号和区号，查询该设备
			//masterno
			paramsMap.put("devno", dgv.getMasterNo());
			paramsMap.put("zoneno", StringUtils.isBlank(dgv.getMasterZonenoNo()) ? zoneno : dgv.getMasterZonenoNo());
			List<DeviceVO> devInfo1 = this.getDevInfo(paramsMap) ;
			if(null == devInfo1 || devInfo1.isEmpty()){
				logger.error("editUserDevGroups| 该设备不存在 | param="+paramsMap);
				resultMap.put("msg", "该设备不存在");
				resultMap.put("result", WsConstants.ERROR);
				return resultMap ;
			}
			dgv.setMasterNo(devInfo1.get(0).getZonedevno()) ;
			//spearker1
			paramsMap.put("devno", dgv.getSpeakerOne());
			paramsMap.put("zoneno", StringUtils.isBlank(dgv.getSpeakerZonenoOne()) ? zoneno : dgv.getSpeakerZonenoOne());
			List<DeviceVO> devInfo2 = this.getDevInfo(paramsMap) ;
			if(null == devInfo2 || devInfo2.isEmpty()){
				logger.error("editUserDevGroups| 该设备不存在 | param="+paramsMap);
				resultMap.put("msg", "该设备不存在");
				resultMap.put("result", WsConstants.ERROR);
				return resultMap ;
			}
			dgv.setSpeakerOne(devInfo2.get(0).getZonedevno());
			//spearker2
			paramsMap.put("devno", dgv.getSpeakerTwo());
			paramsMap.put("zoneno", StringUtils.isBlank(dgv.getSpeakerZonenoTwo()) ? zoneno : dgv.getSpeakerZonenoTwo());
			List<DeviceVO> devInfo3 = this.getDevInfo(paramsMap) ;
			if(null == devInfo3 || devInfo3.isEmpty()){
				logger.error("editUserDevGroups| 该设备不存在 | param="+paramsMap);
				resultMap.put("msg", "该设备不存在");
				resultMap.put("result", WsConstants.ERROR);
				return resultMap ;
			}
			dgv.setSpeakerTwo(devInfo3.get(0).getZonedevno()) ;
			paramsMap.remove("devno") ;
			paramsMap.remove("zoneno") ;*/
			
			//修改群
			deviceDao.updateDeviceGroup(dgv);
			//删除群和联系人（设备）的关联关系
			paramsMap.put("groupuuid", dgv.getUuid());
			deviceDao.deleteDevice2Group(paramsMap);
			//更新群和联系人（设备）的关联关系
			String[] uuids = dgv.getDevices().split(";");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if (uuids.length > 0) {
				String[] props = null;
				Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> params = new HashMap<String, Object>();
				for(String uuid : uuids){ //2019/2/21 周逸芳合并16位与64位代码时
					props = uuid.split(":");
					if(props.length < 2){
						continue;
					}
					params = new HashMap<String, Object>();
					if (props[0].length() == 11) {//64位
						//TODO wqb 11位转20位
						String devId = ChangeNumberUtils.getDeviceTo20(props[0]) ;
						map = new HashMap<String, Object>();
						map.put("devId", devId);
					} else {//16位
						params.put("devno", props[0]) ;
						if(props.length < 3 ){
							params.put("zoneno", this.getLocalZoneno()) ;
						}else{
							params.put("zoneno", props[2]) ;
						}
						//通过5位设备号和区号，查询该设备
						List<DeviceVO> devInfo = this.getThisDevInfo(params) ;
						if(null == devInfo || devInfo.isEmpty()){
							continue ;
						}
						map = new HashMap<String, Object>();
						map.put("devId", devInfo.get(0).getZonedevno());
					}
					map.put("groupuuid", dgv.getUuid());
					map.put("devFunc", props[1]);
					list.add(map);
				}
		
				paramsMap.put("list", list);
				deviceDao.addDevices2Group(paramsMap);
		}
			resultMap.put("msg", "操作成功");
			resultMap.put("result", WsConstants.OK);
		}catch(Exception e){
			logger.error("修改联系人群组失败", e);
			resultMap.put("msg", "修改联系人群组失败，内部异常");
			resultMap.put("result", WsConstants.ERROR);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int delUserDevGroups(Map<String, Object> paramsMap) {
		return deviceDao.deleteDeviceGroup(paramsMap);
	}
	
	/**
	 * 
	 * TODO 修改联系人（设备）群组（默认）
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public Map<String, Object> editDftUserDevGroups(DeviceGroupVO dgv) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try{
			//删除用户和基础群的关联关系
			String[] uuids = dgv.getUuid().split(",");
			if (uuids.length > 0) {
				paramsMap.put("uuids", uuids);
			}
			paramsMap.put("userId", dgv.getUserId());
			deviceDao.deleteUserDeviceGroup(paramsMap);
			//创建私有群（相当于把基础群的信息copy一份到私有群）
			dgv.setDataType(2);//群类型：1基础；2私有
			deviceDao.addDeviceGroup(dgv);
			
			//建立群和联系人（设备）的关联关系
			uuids = dgv.getDevices().split(";");
			if (uuids.length > 0) {
				String[] props = null;
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = null;
				Map<String, Object> params = null ;
				for(String uuid : uuids){ //2019/2/21 周逸芳合并16位与64位代码时
					props = uuid.split(":");
					if(props.length < 2){
						continue;
					}
					params = new HashMap<String, Object>();
					if (props[0].length() == 11) {//64位
						//TODO wqb 11位转20位
						String devId = ChangeNumberUtils.getDeviceTo20(props[0]) ;
						map.put("devId", devId);
					} else {//16位
						map.put("groupuuid", dgv.getUuid());
						params = new HashMap<String, Object>();
						params.put("devno", props[0]) ;
						if(props.length < 3 ){
							params.put("zoneno", this.getLocalZoneno()) ;
						}else{
							params.put("zoneno", props[2]) ;
						}
					
						//通过5位设备号和区号，查询该设备
						List<DeviceVO> devInfo = this.getDevInfo(params) ;
						if(null == devInfo || devInfo.isEmpty()){
							continue ;
						}
						map.put("devId", devInfo.get(0).getZonedevno());
						map.put("devFunc", props[1]);
						list.add(map);
				}
				
				paramsMap.put("list", list);
				deviceDao.addDevices2Group(paramsMap);
			}
			//建立用户和群的关联关系
//			paramsMap.put("userId", dgv.getUserId());
			paramsMap.put("groupuuid", dgv.getUuid());
			deviceDao.addUserDevGroups(paramsMap);
			resultMap.put("msg", "操作成功");
			resultMap.put("result", WsConstants.OK);
			}
		}catch(Exception e){
			logger.error("修改联系人群组失败", e);
			resultMap.put("msg", "修改联系人群组失败，内部异常");
			resultMap.put("result", WsConstants.ERROR);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return resultMap;
	}
	
	/**
	 * 
	 * TODO 修改联系人（设备）群组（默认）
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int delDftUserDevGroups(Map<String, Object> paramsMap) {
		return deviceDao.deleteUserDeviceGroup(paramsMap);
	}

	/**
	 * 
	 * TODO 根据会议ID获取会议主席、发一、发二
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceGroupVO> getScheduleState(Map<String, Object> paramsMap) {
		return deviceDao.getScheduleState(paramsMap);
	}

	/**
	 * 
	 * TODO 根据会议ID设置会议主席、发一、发二
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public int updateScheduleState(Map<String, Object> paramsMap) {
		return deviceDao.updateScheduleState(paramsMap);
	}
	/**
	 * @param  
	 * 
	 * @Title: getDevgroup
	 * @Description: 群组管理获取群信息
	 * @param @return 参数说明
	 * @return List<UserVO> 返回类型
	 * @throws
	 */
	public List<UserVO> getDevgroup(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevgroup(map, new RowBounds());
		}
		return deviceDao.getDevgroup(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}


	/**
	 * @param  
	 * 
	 * @Title: getDevgroupCount
	 * @Description: 群组管理获取群信息总条数
	 * @param @return 参数说明
	 * @return List<UserVO> 返回类型
	 * @throws
	 */
	public int getDevgroupCount(Map<String, Object> paramsMap) {
		return deviceDao.getDevgroupCount(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: addDevGroupBooks
	 * @Description: 通讯录群组管理添加群组
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int addDevGroupBooks(Map<String, Object> paramsMap) {
		int rlt = 0;
		if(null == paramsMap)
			return 0;
		try {
			rlt = deviceDao.addDevGroupBooks(paramsMap);
			//更新vc_devgroup_dev表中设备与群组的关联
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			String[] uuids = paramsMap.get("devices").toString().split(",");
			if (uuids.length > 0) {
				//将所有设备与主席设备，发1，发2进行对比，并附上type属性值
				Map<String, Object> map =null;
				for (String uuid : uuids) {
					if (paramsMap.get("speakerOne")!=null && paramsMap.get("speakerOne")!="") {
						if (uuid.equals( paramsMap.get("speakerOne").toString())){
							map = new HashMap<String, Object>();
							map.put("groupuuid", paramsMap.get("uuid"));
							map.put("devId", uuid);
							map.put("devFunc", 2);
						}
					}else if (paramsMap.get("speakerTwo")!=null && paramsMap.get("speakerTwo")!="") {
						if(uuid.equals( paramsMap.get("speakerTwo").toString())){
							map = new HashMap<String, Object>();
							map.put("groupuuid", paramsMap.get("uuid"));
							map.put("devId", uuid);
							map.put("devFunc", 3);
						}
					}else{
						if (uuid.equals( paramsMap.get("masterNo").toString())) {
							map = new HashMap<String, Object>();
							map.put("groupuuid", paramsMap.get("uuid"));
							map.put("devId", uuid);
							map.put("devFunc", 1);
						}else{
							map = new HashMap<String, Object>();
							map.put("groupuuid", paramsMap.get("uuid"));
							map.put("devId", uuid);
							map.put("devFunc", 0);
						}
					}
					list.add(map);
				}
				paramsMap.put("list", list);
				rlt = deviceDao.addDevices2Group(paramsMap);
			}
		} catch (Exception e) {
			logger.error("更新失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return rlt;
	}

	/**
	 * @param  
	 * 
	 * @Title: deleteDevGroup
	 * @Description: 通讯录群组管理删除群组
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int deleteDevGroup(Map<String, Object> paramsMap) {
		return deviceDao.deleteDeviceGroup(paramsMap);
	}

	/**
	 * @param  
	 * 
	 * @Title: updateDevGroupBooks
	 * @Description: 通讯录群组管理修改群组基本信息
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int updateDevGroupBooks(DeviceGroupVO devGroup) {
		int rlt = 0;
		if(null == devGroup)
			return 0;
		try {
			//修改群与设备信息
			rlt = deviceDao.updateDeviceGroup(devGroup);
			if (StringUtils.isNotBlank(devGroup.getDevices())) {
				//删除设备与群组关系
				rlt = deviceDao.deleteDevGroup(devGroup);
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Map<String, Object> paramsMap =new HashMap<String, Object>();
				String[] uuids = devGroup.getDevices().split(",");
				if (uuids.length > 0) {
					//将所有设备与主席设备，发1，发2进行对比，并附上type属性值
					Map<String, Object> map =null;
					for (String uuid : uuids) {
						if (devGroup.getSpeakerOne()!=null && devGroup.getSpeakerOne()!="") {
							if (uuid.equals(devGroup.getSpeakerOne())){
								map = new HashMap<String, Object>();
								map.put("groupuuid", devGroup.getUuid());
								map.put("devId", uuid);
								map.put("devFunc", 2);
							}
						}else if (devGroup.getSpeakerTwo()!=null && devGroup.getSpeakerTwo()!="") {
							if(uuid.equals(devGroup.getSpeakerTwo())){
								map = new HashMap<String, Object>();
								map.put("groupuuid", devGroup.getUuid());
								map.put("devId", uuid);
								map.put("devFunc", 3);
							}
						}else{
							if (uuid.equals(devGroup.getMasterNo())) {
								map = new HashMap<String, Object>();
								map.put("groupuuid", devGroup.getUuid());
								map.put("devId", uuid);
								map.put("devFunc", 1);
							}else{
								map = new HashMap<String, Object>();
								map.put("groupuuid", devGroup.getUuid());
								map.put("devId", uuid);
								map.put("devFunc", 0);
							}
						}
						list.add(map);
					}
					paramsMap.put("list", list);
					rlt = deviceDao.addDevices2Group(paramsMap);
				}
			}
		} catch (Exception e) {
			logger.error("更新失败", e);
			rlt = 0;
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return rlt;
	}

	/**
	 * @param  
	 * 
	 * @Title: getUserDev
	 * @Description: 获取已选设备列表
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	public List<DeviceVO> getUserDev(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getUserDev(paramsMap, new RowBounds());
		}
		return deviceDao.getUserDev(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}

	/**
	 * @param  
	 * 
	 * @Title: getUserDevCount
	 * @Description: 获取已选设备列表总条数
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int getUserDevCount(Map<String, Object> paramsMap) {
		return deviceDao.getUserDevCount(paramsMap);
	}

	/**
	 * 
	 * @Title: getDevsInGroup 
	 * @Description: TODO 获取群内已添加设备列表 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	public List<DeviceVO> getDevsInGroup(Map<String,Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevsInGroup(map, new RowBounds());
		}
		return deviceDao.getDevsInGroup(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	
	/**
	 * 
	 * @Title: getDevsInGroupCount 
	 * @Description: 获取群内已添加设备列表 总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int getDevsInGroupCount(Map<String,Object> map) {
		return deviceDao.getDevsInGroupCount(map);
	}

	/**
	 * @param  
	 * 
	 * @Title: updateDevGroup
	 * @Description: 置顶（取消置顶）群组
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int updateDevGroup(Map<String, Object> paramsMap) {
		return deviceDao.updateDevGroup(paramsMap);
	}
	
	/***
	 * @Title: getDevInfo
	 * @Description: 获取设备列表
	 * @param paramsMap
	 * @return List<DeviceVO>
	 */
	public List<DeviceVO> getDevInfo(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getDevInfo(map, new RowBounds());
		}
		return deviceDao.getDevInfo(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * @Title: distributeDevToUser
	 * @Description: 给用户分配设备
	 * @param paramsMap
	 * @return int
	 */
	public int distributeDevToUser(List<Object> list) {
		return deviceDao.distributeDevToUser(list);
	}
	
	
	/**
	 * @Title: getDitributeDev
	 * @Description: 查询用户是否已经分配某设备
	 * 
	 */
	public List<UserVO> getDitributeDev(Map<String, Object> map) {
		return deviceDao.getDitributeDev(map);
	}
	
	/**
	 * @param  
	 * 
	 * @Title: updateUserDev
	 * @Description: 置顶（取消置顶）用户下设备
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	public int updateUserDev(Map<String, Object> map) {
		return deviceDao.updateUserDev(map);
	}

	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:00:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:00:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<ScheduleVO> getMasterBySchedule(Map<String, Object> map) {
		List<ScheduleVO> list = new ArrayList<ScheduleVO>();
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			list  =  deviceDao.getMasterBySchedule(map, new RowBounds());
		}else{
			list  =  deviceDao.getMasterBySchedule(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
		}
		for (ScheduleVO scheduleVO : list) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long starttime=0;
			long stoptime=0;
			try {
				if(StringUtils.isNotBlank(scheduleVO.getStartTime())){
					starttime = sdf.parse(scheduleVO.getStartTime()).getTime();
				}
				if(StringUtils.isNotBlank(scheduleVO.getStopTime())){
					stoptime = sdf.parse(scheduleVO.getStopTime()).getTime();
					//毫秒
					long date3 = stoptime-starttime;
					String timeDiff = "";
					//计算出相差天数  
				    int days = (int)Math.floor(date3/(24*3600*1000));
				    //计算出小时数  
				    long leave1=date3%(24*3600*1000);    //计算天数后剩余的毫秒数  
				    int hours=(int)Math.floor(leave1/(3600*1000));
				    //计算相差分钟数  
				    long leave2=leave1%(3600*1000);        //计算小时数后剩余的毫秒数  
				    int minutes=(int)Math.floor(leave2/(60*1000));  
				    //计算相差秒数  
				    long leave3=leave2%(60*1000);      //计算分钟数后剩余的毫秒数  
				    int seconds=(int)Math.round(leave3/1000);
				    if(days > 0){
				    	timeDiff += days + "天";
				    }
				    if(hours > 0){
				    	timeDiff += hours + "时";
				    }
				    if(minutes > 0){
				    	timeDiff += minutes + "分";
				    }
				    if(seconds > 0){
				    	timeDiff += seconds + "秒";
				    }
				    scheduleVO.setScheduleTime(timeDiff);
				}
			} catch (ParseException e) {
				logger.error("时间转换失败：", e);
			}  
		}	
		return list;
		
	}

	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议总条数)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:00:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:00:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public int getMasterByScheduleCount(Map<String, Object> paramsMap) {
		return deviceDao.getMasterByScheduleCount(paramsMap);
	}

	/** <pre>getMasterDetail(获取会议详情)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:44:32    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:44:32    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<ScheduleVO> getMasterDetail(Map<String, Object> map) {
		List<ScheduleVO> list = new ArrayList<ScheduleVO>();
		list = deviceDao.getMasterDetail(map);
		for (ScheduleVO scheduleVO : list) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long starttime=0;
			long stoptime=0;
			try {
				if(StringUtils.isNotBlank(scheduleVO.getStartTime())){
					starttime = sdf.parse(scheduleVO.getStartTime()).getTime();
				}
				if(StringUtils.isNotBlank(scheduleVO.getStopTime())){
					stoptime = sdf.parse(scheduleVO.getStopTime()).getTime();
					//毫秒
					long date3 = stoptime-starttime;
					String timeDiff = "";
					//计算出相差天数  
				    int days = (int)Math.floor(date3/(24*3600*1000));
				    //计算出小时数  
				    long leave1=date3%(24*3600*1000);    //计算天数后剩余的毫秒数  
				    int hours=(int)Math.floor(leave1/(3600*1000));
				    //计算相差分钟数  
				    long leave2=leave1%(3600*1000);        //计算小时数后剩余的毫秒数  
				    int minutes=(int)Math.floor(leave2/(60*1000));  
				    //计算相差秒数  
				    long leave3=leave2%(60*1000);      //计算分钟数后剩余的毫秒数  
				    int seconds=(int)Math.round(leave3/1000);
				    if(days > 0){
				    	timeDiff += days + "天";
				    }
				    if(hours > 0){
				    	timeDiff += hours + "时";
				    }
				    if(minutes > 0){
				    	timeDiff += minutes + "分";
				    }
				    if(seconds > 0){
				    	timeDiff += seconds + "秒";
				    }
				    scheduleVO.setScheduleTime(timeDiff);
				}
			} catch (ParseException e) {
				logger.error("时间转换失败：", e);
			}  
		}	
		return  list;
	}

	/** <pre>getScheduleDevice(获取会议相关设备信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 下午12:56:32    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 下午12:56:32    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<DeviceVO> getScheduleDevice(Map<String, Object> paramsMap) {
		return deviceDao.getScheduleDevice(paramsMap);
	}
	
	/** <pre>getChildByUserId(视联汇根据用户获取用户所拥有设备的行政区域)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月26日 下午4:03:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月26日 下午4:03:36    
	 * 修改备注： 

	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<RegionVO> getChildByUserId(Map<String, Object> paramsMap) {
		List<RegionVO> list = deviceDao.getChild(paramsMap);
		List<RegionVO> tempList = new ArrayList<RegionVO>();
		tempList.addAll(list);
		String regionId = null;
		// 获取成员单位
		paramsMap.put("unitPid", "-1");// 获取行政区域下成员单位的根节点
		paramsMap.put("regionid", paramsMap.get("pId"));
		if(paramsMap.get("pId") != null && paramsMap.get("pId").toString().length() < 12){// 行政区域ID长度为12位，<12说明是成员单位的pif
			paramsMap.put("unitPid", paramsMap.get("pId"));
			if(!paramsMap.get("pId").equals("-1")){
				paramsMap.remove("regionid");
			}
		}else{
			for(RegionVO region : tempList){//去除没有设备的节点
				regionId = region.getId();
				String base = regionId;
				for(int i=regionId.length(); i>=0; i--){
					if(regionId.substring(i-1, i).equals("0")){
						base = base.substring(0,i-1);
					}else{
						break;
					}
				}
				if(base.length() % 2 != 0 && base.length() != 9){//长度为奇数时补零
					if (base.length() == 7) {
						base += "00";
					}else{
						base += "0";
					}
				}else if(base.length() == 10){
					base += "00";
				}else if(base.length() == 8){
					base += "0";
				}
				paramsMap.put("regionId", base);
				//获取用户所拥有的设备所在的行政区域，若该行政区域没有设备则剔除掉
				int count = deviceDao.getUserDevicesCount(paramsMap);
				if(count < 1){
					list.remove(region);
				}else{
					region.setDevNum(count);
				}
			}
		}
		List<RegionVO> unitList = deviceDao.getUnitChild(paramsMap);
		
		// 合并成员单位列表和行政区域列表
		List<RegionVO> allList = new ArrayList<RegionVO>();
		allList.addAll(unitList);
		allList.addAll(list);
		return allList;
	}
	
/**
	 * 
	 * @Description: TODO
	 * @param @param 获取参会终端详细信息
	 * @param @return   
	 * @return List<DeviceVO>  
	 * @throws
	 * @author 谢程算
	 * @date 2018年6月13日
	 */
	public List<Map<String, Object>> getMeetingDevList(Map<String, Object> paramsMap) {
		return deviceDao.getMeetingDevList(paramsMap);
	}
	
	/**
	 * 
	 * @Description: TODO
	 * @param @param 获取参会终端总数
	 * @param @return   
	 * @return List<DeviceVO>  
	 * @throws
	 * @author 谢程算
	 * @date 2018年6月13日
	 */
	public int getMeetingDevListCount(Map<String, Object> paramsMap) {
		return deviceDao.getMeetingDevListCount(paramsMap);
	}
	
	
	/** <pre>getMeetingDevicesCount(获取所有正在会议中的设备数量)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月19日 下午2:37:47    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月19日 下午2:37:47    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public int getMeetingDevicesCount(Map<String, Object> paramsMap) {
		return deviceDao.getMeetingDevicesCount(paramsMap);
	}
	
	/** <pre>getDevicesMeeting(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月19日 下午2:54:20    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月19日 下午2:54:20    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<DeviceVO> getDevicesMeeting(Map<String, Object> paramsMap) {
		return deviceDao.getDevicesMeeting(paramsMap);
	}
	
	/** <pre>getDevRegion(根据设备号码或设备名称查询设备及设备所属行政区域拼接返回)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月20日 下午2:02:17    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月20日 下午2:02:17    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	public List<DeviceVO> getDevRegion(Map<String, Object> paramsMap) {
		List<DeviceVO> devRegion = deviceDao.getDevRegion(paramsMap);
		List<String> regionIdList = new ArrayList<String>();
		Map<String, String> regionNameRel = new HashMap<String, String>();
		if (devRegion == null || devRegion.size() == 0) {
			return devRegion;
		}
		for (DeviceVO deviceVO : devRegion) {
			if (!regionIdList.contains(deviceVO.getRegionId())) {
				regionIdList.add(deviceVO.getRegionId());
				paramsMap.put("regionId", deviceVO.getRegionId());
				regionNameRel.put(deviceVO.getRegionId(), deviceDao.getSingleRegionSvrName(paramsMap));
			}
		}
		for (DeviceVO deviceVO : devRegion) {
			deviceVO.setSvrName(regionNameRel.get(deviceVO.getRegionId()));
		}
		return devRegion;
		
		//此方法效率太低了，数据量稍大时，就会导致内存溢出。xcs和wqb优化了该方法。
		/*long start = System.currentTimeMillis() ;
		List<DeviceVO> devRegion = deviceDao.getDevRegion(paramsMap);
		for (DeviceVO deviceVO : devRegion) {
			if (deviceVO.getRegionId().indexOf("000") == 2) {
				deviceVO.setGradeid(1);
			}else if (deviceVO.getRegionId().indexOf("000") == 4) {
				deviceVO.setGradeid(2);
			}else if (deviceVO.getRegionId().indexOf("000") == 6) {
				deviceVO.setGradeid(3);
			}else if (deviceVO.getRegionId().indexOf("000") == 9) {
				deviceVO.setGradeid(4);
			}else if (deviceVO.getRegionId().indexOf("000") == 12) {
				deviceVO.setGradeid(5);
			}
		}
		if (devRegion.size() > 0 && devRegion != null) {
			List<DeviceVO> svrList = deviceDao.getRegionSvrName(devRegion);
			for (DeviceVO deviceVO : devRegion) {
				for (DeviceVO device : svrList) {
					if (device.getId().equals(deviceVO.getId())) {
						deviceVO.setSvrName(device.getSvrName());
					}
				}
			}
		}
		long end = System.currentTimeMillis() ;
		System.err.println((end-start));
		System.err.println("number is "+devRegion.size());
		return devRegion;*/
	}
	

	/**
	 * @Title: deleteDevOfUser
	 * @Description: 根据用户id和设备id删除设备
	 * @param 
	 * @return 
	 */
	public int deleteDevOfUser(String userId,List<String> devIdList) {
		
		return deviceDao.deleteDevOfUser(userId,devIdList);
	}

	public int updateById(Map<String, Object> paramsMap) {
		return deviceDao.updateById(paramsMap);
	}
	//会管设备本地区号
	public String getLocalZoneno(){
		ConstDataVO dataVO = new ConstDataVO() ;
		dataVO.setValueType(9) ;
		List<ConstDataVO> constList = sysConfigDao.getAprTime(dataVO) ;
		return constList.get(0).getValue() ;
	}
	
		/**
	 * 获取设备列表（掌上通）
	 * @param paramsMap
	 * @return
	 */
	public List<Map<String, Object>> getDevDetail(Map<String, Object> paramsMap) {
		return deviceDao.getDevDetail(paramsMap);
	}
	
	/**
	 * 获取所有成员单位列表（掌上通）
	 * @param paramsMap
	 * @return
	 */
	public List<Map<String, Object>> getAllUnits(Map<String, Object> paramsMap) {
		return deviceDao.getAllUnits(paramsMap);
	}
	/***
	 * @Title: getThisDevInfo
	 * @Description: 获取某个设备列表
	 * @param paramsMap
	 * @return List<DeviceVO>
	 */
	public List<DeviceVO> getThisDevInfo(Map<String, Object> map) {
		if(map.get("pageSize") == null || Integer.parseInt(map.get("pageSize").toString()) == -1){
			return deviceDao.getThisDevInfo(map, new RowBounds());
		}
		return deviceDao.getThisDevInfo(map, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}
	/**
	 * 获取联系人（设备）列表(为了兼容视联管家获取昵称，该昵称是从vc_user_dev表获取)
	 * @date 2018年9月12日 下午4:07:26
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getUserDevicesInfo(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceDao.getUserDevicesInfo(paramsMap, new RowBounds());
		}
		return deviceDao.getUserDevicesInfo(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}

	public int getUserDevicesInfoCount(Map<String, Object> paramsMap) {
		return deviceDao.getUserDevicesInfoCount(paramsMap);
	}

	/**
	 * 获取设备别名
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getDevAlias(Map<String, Object> paramsMap) {
		return deviceDao.getDevAlias(paramsMap);
	}
	
	/****
	 * 根据终端号码，查询号码和对应的MAC
	 * @date 2018年11月24日
	 * @auth wangrz
	 * @param list 号码集合
	 * @return
	 */
	@Override
	public List<DeviceVO> getNumberAndMacByNumbers(List list) {
		return deviceDao.getNumberAndMacByNumbers(list);
	}

	@Override
	public void telecontrollerStop(Map<String, Object> map) {/*
		Map<String ,Object> params = new HashMap<String, Object>();
		List<VphoneVO> list = new ArrayList<>();
		Map<String, Object> args = new HashMap<String, Object>();
		List<UserVO> userList = new ArrayList<>();
		List<Object> items = new ArrayList<Object>();
		Map<String , Object> param = new HashMap<String,Object>();
		Map<String , Object> paramsMap = new HashMap<String,Object>();
		Map<String , Object> resultMap = new HashMap<String,Object>();
		resultMap = (Map<String, Object>) map.get("content");
		params.put("uuid", resultMap.get("meetingId"));
		list = scheduleDao.getPhoneMeet(params);
		if(list.size() > 0 || list != null){
			args.put("srcNo", list.get(0).getDevId1());
			args.put("srcMac", list.get(0).getDevMac1());
			args.put("dstNo", list.get(0).getDevId2());
			args.put("dstMac", list.get(0).getDevMac2());
			args.put("businessType",0);
			args.put("businessState", 1);//停止
			args.put("platformType", 601);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
			String now = sdf.format(new Date());
			args.put("time",now);
			userList = userDao.getUserByUuid(list.get(0).getUserId());
			if(null != userList && userList.size() > 0){
				args.put("userName",userList.get(0).getName());
			}
		}
		items.add(args);
		param.put("items",items );
		paramsMap.put("type", 11);
		ServerVO server = sysConfigDao.getServer(paramsMap);
		String ip = server.getIp();
		String port = server.getPort();
		String baseUrl = String.format("http://%s:%s/dispatchment/",
				ip, port);
		resultMap = RestClient.postTo(baseUrl+"vphone/report?access_token=22222222222222222222222222222222", null, param);
		logger.info(resultMap);
	*/}

	@Override
	public int deleteUserDev(Map<String, Object> map) {
		return deviceDao.deleteUserDev(map) ;
	}

	@Override
	public List<RegionVO> getChildMeeting(Map<String, Object> paramsMap) {
		List<RegionVO> list = deviceDao.getChild(paramsMap);
		List<RegionVO> tempList = new ArrayList<RegionVO>();
		tempList.addAll(list);
		String regionId = null;
		for(RegionVO region : tempList){//去除没有设备的节点
			regionId = region.getId();
			String base = regionId;
			for(int i=regionId.length(); i>=0; i--){
				if(regionId.substring(i-1, i).equals("0")){
					base = base.substring(0,i-1);
				}else{
					break;
				}
			}
			if(base.length() % 2 != 0 && base.length() != 9){//长度为奇数时补零
				if (base.length() == 7) {
					base += "00";
				}else{
					base += "0";
				}
			}else if(base.length() == 10){
				base += "00";
			}
			else if(base.length() == 8){
				base += "0";
			}
			paramsMap.put("regionId", base);
			if(deviceDao.getMeetingDevicesCount(paramsMap) < 1){
				list.remove(region);
			}
		}
		return list;
	}

	/**
	 * 获取网管webservice接口的主路径
	 * @return
	 */
	public Map<String, Object> getNmBaseUrl(ServerVO svr){
		Map<String, Object> ret = new HashMap<String, Object>();
		//获取基础url
		if(svr == null){
			ret.put("result", false);
			ret.put("msg", "网管服务器未配置");
			logger.error("网管服务器未配置");
			return ret;
		}
		if(StringUtils.isBlank(svr.getIp())){
			ret.put("result", false);
			ret.put("msg", "网管服务器IP未配置");
			return ret;
		}
		if(StringUtils.isBlank(svr.getPort())){
			ret.put("result", false);
			ret.put("msg", "网管服务器端口未配置");
			return ret;
		}
		if(StringUtils.isBlank(svr.getAccount())){
			ret.put("result", false);
			ret.put("msg", "网管服务器账号未配置");
			return ret;
		}
		if(StringUtils.isBlank(svr.getPassword())){
			ret.put("result", false);
			ret.put("msg", "网管服务器密码未配置");
			return ret;
		}
		String baseUrl = String.format(WsConstants.NET_HOST_URL, svr.getIp(),svr.getPort());
		ret.put("result", true);
		ret.put("baseUrl", baseUrl);
		return ret;
	}
	
	/**
	 * 获取网管服务器接口调用token
	 * @param baseUrl
	 * @param svr
	 * @return
	 */
	public Map<String, Object> getNmToken(String baseUrl, ServerVO svr){
		Map<String, Object> ret = new HashMap<String, Object>();
		//获得token
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(WsConstants.KEY_USER, svr.getAccount());//账号
		args.put(WsConstants.KEY_PWD, svr.getPassword());//密码
		String token = null;
		try{
			Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_LOGIN, null, args);
			if(data.get("extra") == null){
				logger.error("登录网管失败：" + data.get("errmsg"));
				ret.put("result", false);
				ret.put("msg", "登录网管失败：" + data.get("errmsg"));
				return ret;
			}
			Map<String, Object> extra = (Map<String, Object>) data.get("extra");
			token = extra.get(WsConstants.KEY_TOKEN).toString();
		}catch(Exception e){
			logger.error("登录网管失败：请检查网管服务配置", e);
			ret.put("result", false);
			ret.put("msg", "登录网管失败：系统内部异常");
			return ret;
		}
		ret.put("result", true);
		ret.put("token", token);
		return ret;
	}
	
	@Override
	public Map<String, Object> getAllAlarm(Map<String, Object> paramsMap) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> alarmData = null;
		Map<String, Object> cpuMemoryData = null;
		List<String> devnoList = new ArrayList<String>();
		
		if(null == paramsMap || null == paramsMap.get("id")){
			result.put("description", "设备号为空") ;
			result.put("result", VSMeet64.ERR_SCHEDULE_DEVICE_NULL) ;
			return result;
		}
		String[] devnoArray = paramsMap.get("id").toString().split(",") ;
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>() ;
		for (int i = 0; i < devnoArray.length; i++) {
			// wqb 11位转20位
			String devno20 = ChangeNumberUtils.getDeviceTo20(devnoArray[i]) ;
			devnoList.add(devno20) ;
		}
		
		paramsMap.put("type", 2);
		ServerVO svr = sysConfigDao.getServer(paramsMap);
		//获取基础url
		ret = this.getNmBaseUrl(svr);
		if(!(Boolean) ret.get("result")){
			logger.error("从网管获取终端告警信息失败：" + ret.get("msg").toString());
			result.put("description", ret.get("msg").toString()) ;
			result.put("result", VSMeet64.ERR_SCHEDULE_LOGIN_WG) ;
			return result;
		}
		String baseUrl = ret.get("baseUrl").toString();
		//获得token
		ret = this.getNmToken(baseUrl, svr);
		if(!(Boolean) ret.get("result")){
			logger.error("从网管获取token失败：" + ret.get("msg").toString());
			result.put("description", ret.get("msg").toString()) ;
			result.put("result", VSMeet64.ERR_SCHEDULE_TOKEN_WG) ;
			return result;
		}
		String token = ret.get("token").toString();
		//1：获取视频丢包率、音频丢包率、视频流、音频流
		alarmData = RestClient.post(baseUrl + WsConstants.URL_ALARM_GET, token, devnoList);
		if(alarmData.get("errmsg") != null){
			logger.error("从网管获取终端视频丢包率、音频丢包率、视频流、音频流失败：" + alarmData.get("errmsg"));
			result.put("description", ret.get("msg").toString()) ;
			result.put("result", VSMeet64.ERR_SCHEDULE_DATA_WG) ;
			return result;
		}
		logger.info("getAllAlarm|网管返回视频丢包率、音频丢包率、视频流、音频流|result="+alarmData) ;
		
		//2：获取CPU使用率、内存使用率
		cpuMemoryData = RestClient.post(baseUrl + WsConstants.URL_DEVICE_CPUMEMORY, token, devnoList);
		if(cpuMemoryData.get("errmsg") != null){
			logger.error("从网管获取CPU使用率、内存使用率失败：" + cpuMemoryData.get("errmsg"));
			result.put("description", ret.get("msg").toString()) ;
			result.put("result", VSMeet64.ERR_SCHEDULE_DATA_WG) ;
			return result;
		}
		logger.info("getAllAlarm|网管返回CPU使用率、内存使用率|result="+cpuMemoryData) ;
		
		List<Map<String, Object>> items = (List<Map<String, Object>>) alarmData.get("items") ;
		List<Map<String, Object>> cpumemoryItemsList = (List<Map<String, Object>>) cpuMemoryData.get("items") ;
		items.addAll(cpumemoryItemsList) ;//把两个list合并
		
		Map<String, Object> dataMap = new HashMap<String, Object>() ;
		Map<String,Map<String, Object>> itemsMap = new HashMap<String, Map<String, Object>>() ;
		//把结果数据整理为key值是11位号码+":"+告警code的hashmap
		for(int i = 0 ; i<items.size() ; i++){
			Map<String, Object> m = items.get(i) ;
			String v2vno = m.get("v2vno").toString() ;
			Object almcode = m.get("almcode") ;
			if(null == almcode){
				almcode = "" ;
			}
			//是否有音频流、视频流特殊处理(有流丢失就会有一条数据，所以要进行累加计算)
			if(almcode.equals("14") || almcode.equals("15")){
				Map<String, Object> map = itemsMap.get(v2vno+":"+almcode) ;
				int num = (map != null && map.get("value") != null) ? Integer.parseInt(map.get("value").toString())+1 : 1 ;
				m.put("value", num) ;
			}
			itemsMap.put(v2vno+":"+almcode, m) ;
		}
		for (int i = 0; i < devnoArray.length; i++) {
			String devno = devnoArray[i] ;
			Map<String,Object> resultMap = new HashMap<String, Object>() ;
			Map<String,Object> quotaMap = new HashMap<String, Object>() ;
			//视频丢包率(16)
			Map<String, Object> videoMap = itemsMap.get(devno+":"+"16") ;
			String videoLost = "" ;
			if(null != videoMap){
				videoLost = videoMap.get("almcontent").toString() ;
				videoLost = videoLost.contains(":") ? videoLost.substring(videoLost.lastIndexOf(":")+1).trim() : "" ;
				videoLost = videoLost.replace("%", "").replace("‰", "") ;
			}
			
			//音频丢包率(17)
			Map<String, Object> audioMap = itemsMap.get(devno+":"+"17") ;
			String audioLost = "" ;
			if(null != audioMap){
				audioLost = audioMap.get("almcontent").toString() ;
				audioLost = audioLost.contains(":") ? audioLost.substring(audioLost.lastIndexOf(":")+1).trim() : "" ;
				audioLost = audioLost.replace("%", "").replace("‰", "") ;
			}
			
			//有无视频流(14)
			Map<String, Object> hasVideoMap = itemsMap.get(devno+":"+"14") ;
			String hasVideo = hasVideoMap == null ? "" : hasVideoMap.get("value").toString() ;
			
			//有无音频流(15)
			Map<String, Object> hasAudioMap = itemsMap.get(devno+":"+"15") ;
			String hasAudio = hasAudioMap == null ? "" : hasAudioMap.get("value").toString() ;
			
			//CPU使用率、内存使用率
			Map<String, Object> cpuMemoryMap = itemsMap.get(devno+":") ;
			String cpu = "" ;
			String memory = "" ;
			if(null != cpuMemoryMap){
				cpu = cpuMemoryMap.get("cpuvalue") == null ? "" : cpuMemoryMap.get("cpuvalue").toString() ;
				memory = cpuMemoryMap.get("memoryvalue") == null ? "" : cpuMemoryMap.get("memoryvalue").toString() ;
				cpu = cpu.replace("%", "").replace("‰", "") ;
				memory = memory.replace("%", "").replace("‰", "") ;
			}
			
			quotaMap.put("videoPacketLossRate", videoLost) ;//视频丢包率
			quotaMap.put("audioPacketLossRate", audioLost) ;//音频丢包率
			quotaMap.put("videoFlow", hasVideo) ;//videoFlow
			quotaMap.put("audioFlow", hasAudio) ;//audioFlow
			quotaMap.put("cpuUsedRate", cpu) ;//cpuUsedRate
			quotaMap.put("memUsedRate", memory) ;//memUsedRate
			resultMap.put(devno, quotaMap) ;
			resultList.add(resultMap) ;
		}
		
		dataMap.put("items", resultList) ;
		result.put("content", dataMap) ;
		result.put("result", VSMeet.ERR_SUCCESS) ;
		result.put("description", "查询成功") ;
		return result;
	
	}

	@Override
	public List<DeviceVO> getDevInfo64(Map<String, Object> paramsMap) {
		return deviceDao.getDevInfo64(paramsMap);
	}

	@Override
	public int getUserDevicesCount64(Map<String, Object> paramsMap) {
		return deviceDao.getUserDevicesCount64(paramsMap);
	}

	
}
