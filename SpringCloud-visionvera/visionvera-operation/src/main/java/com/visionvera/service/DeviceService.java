package com.visionvera.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceRoleVO;
import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;

public interface DeviceService {
	
	int addDeviceFromPhone(List<DeviceVO> devList);

	/**
	 * 
	 * @Title: getDeviceList 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDeviceList(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getDeviceListCount 
	 * @Description: 获取设备列表总条目 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDeviceListCount(Map<String, Object> map);

	/**
	 * 
	 * @Title: getDevList 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDevList(Map<String, Object> map);
	
	/**
	 * 
	 * @Title: getDeviceListCount 
	 * @Description: 获取设备列表总条目 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDevListCount(Map<String, Object> map);

	/**
	 * 
	 * @Title: getDeviceTypeList 
	 * @Description: TODO 获取设备类型列表 
	 * @param @return  参数说明 
	 * @return List<DeviceTypeVO>    返回类型 
	 * @throws
	 */
	List<DeviceTypeVO> getDeviceTypeList();
	
	/**
	 * 
	 * @Title: getDeviceRoleList 
	 * @Description: TODO 获取设备角色列表 
	 * @param @return  参数说明 
	 * @return List<DeviceRoleVO>    返回类型 
	 * @throws
	 */
	List<DeviceRoleVO> getDeviceRoleList();

	/**
	 * 
	 * @Title: addDevice 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevice(DeviceVO user);
	
	/**
	 * 
	 * @Title: updateDevice 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateDevice(DeviceVO user);
	
	/**
	 * 
	 * @Title: updateDevice 
	 * @Description: 修改设备gis信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateDevice(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: deleteDevice 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDevice(Map<String,Object> map);
	
	
	/**
	 * 
	 * @Title: getDeviceGroupList 
	 * @Description: 获取设备分组列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getDeviceGroupList(Map<String, Object> map);

	/**
	 * 
	 * @Title: addDeviceGroup 
	 * @Description: 新增设备分组信息
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDeviceGroup(DeviceGroupVO deviceGroup);

	/**
	 * 
	 * @Title: deleteDeviceGroup 
	 * @Description: 删除设备分组信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	Map<String, Object> deleteDeviceGroup(Map<String, Object> map);

	/**
	 * 
	 * @Title: updateDeviceGroup 
	 * @Description: 更新设备分组信息 
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return map    返回类型 
	 * @throws
	 */
	Map<String, Object> updateDeviceGroup(DeviceGroupVO deviceGroup);

	/**
	 * 
	 * @Title: searchGroupByUuid
	 * @Description: 根据会场ID会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> searchGroupByUuid(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: updateDeviceGroupName 
	 * @Description: 更新会场名称
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return map    返回类型 
	 * @throws
	 */
	Map<String, Object> updateDeviceGroupName(DeviceGroupVO deviceGroup);

	/**
	 * 
	 * @Title: getDeviceGroupListCount 
	 * @Description: 获取设备分组列表总数
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDeviceGroupListCount(Map<String, Object> map);

	/**
	 * @param paramsMap 
	 * 
	 * @Title: getAllGroups 
	 * @Description: 获取所有设备分组
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getAllGroups(Map<String, Object> paramsMap);

	/**
	 * @return 
	 * @param in 
	 * 
	 * @Title: upLoadExcel
	 * @Description: 解析上传的excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	Map<String, Object> upLoadExcel(InputStream in, String fileName);

	/**
	 * 
	 * @Title: addDevice2Group 
	 * @Description: 添加设备分组和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevice2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: deleteDevice2Group 
	 * @Description: 删除设备分组和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDevice2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: avaliableDevices
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> avaliableDevices(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: avaliableDevicesCount
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int avaliableDevicesCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: devicesInGroup
	 * @Description: 获取组内已添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> devicesInGroup(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: checkRepeatGroup
	 * @Description: 验证设备组名称重复
	 * @param @param name
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int checkRepeatGroup(Map<String, Object> paramsMap);

	List<Map<String, Object>> createExcelRecord();

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
	int checkRepeatId(Map<String, Object> paramsMap);

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
	int checkRepeatName(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: GetDeviceListByUserId
	 * @Description: 根据用户id获取设备列表
	 * @param  Map
	 * @param 
	 * @return List<DeviceVO> 返回类型
	 * @throws
	 */
	List<DeviceVO> GetDeviceListByUserId(Map<String, Object> map); 
	
	/**
	 * 
	 * @Title: getDeviceCountByUserId
	 * @Description: 根据用户id获取设备数量
	 * @param Map集合
	 * @param
	 * @return int
	 * @throws
	 */
	int getDeviceCountByUserId(Map<String, Object> paramsMap); 
	
	/**
	 * 
	 * @Title: GetDevice
	 * @Description: 根据设备信息
	 * @param  Map
	 * @param 
	 * @return List<DeviceVO> 返回类型
	 * @throws
	 */
	List<DeviceVO> GetDevice(Map<String, Object> map);

	/**
	 * 
	 * @Title: getOrgDevGroups
	 * @Description: 根据工作单位获取会场列表
	 * @param  Map
	 * @param 
	 * @return List<DeviceGroupVO> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> getOrgDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: synchroDevices
	 * @Description: 从网管同步设备
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	Map<String, Object> synchroDevices(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getRegions
	 * @Description: 获取行政区域信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<RegionVO> getRegions(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: getUnits
	 * @Description: 获取成员单位信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<RegionVO> getUnits(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getDevices
	 * @Description: 获取设备信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<DeviceVO> getDevices(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: getDevicesCount
	 * @Description: 获取设备总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getDevicesCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getDevicesOfRegion
	 * @Description: 获取设备信息（获取区域节点下的所有设备，包含该区域所有子节点下的所有设备）
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<DeviceVO> getDevicesOfRegion(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: getDevicesOfRegionCount
	 * @Description: 获取设备信息（获取区域节点下的所有设备，包含该区域所有子节点下的所有设备）的设备总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getDevicesOfRegionCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getRoot
	 * @Description: 获取行政区域信息的第一级节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> getRoot(Map<String, Object> paramsMap); 

	/**
	 * 
	 * @Title: getChild
	 * @Description: 获取行政区域信息的子节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> getChild(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: auth
	 * @Description: 给用户组授权设备（增减合一）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> auth(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getAuthedOfRegion
	 * @Description: 获取某一节点下户组内已授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getAuthedOfRegion(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: getAuthedOfRegionCount
	 * @Description: 获取某一节点下户组内已授权设备总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getAuthedOfRegionCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getAuthedOfRegions
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getAuthedOfRegions(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: getAuthedOfRegionsCount
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getAuthedOfRegionsCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getNotAuthedOfRegion
	 * @Description: 获取某一节点下户组内未授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getNotAuthedOfRegion(Map<String, Object> paramsMap);

	/**
	 * @param  
	 * 
	 * @Title: getNotAuthedOfRegionCount
	 * @Description: 获取某一节点下户组内未授权设备的总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getNotAuthedOfRegionCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getNotAuthedOfRegions
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getNotAuthedOfRegions(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: getNotAuthedOfRegionsCount
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）的总数
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	int getNotAuthedOfRegionsCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: addAuth
	 * @Description: 给用户组授权设备（增加）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> addAuth(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: delAuth
	 * @Description: 给用户组授权设备（减少）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> delAuth(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getAuthed
	 * @Description: 获取已经授权给用户组的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getAuthed(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: delAuthed
	 * @Description: 删除用户组内已授权的设备（devIds为空时，删除组内所有已授权设备）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> delAuthed(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: rmFromGroup
	 * @Description: 移除会场内的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> rmFromGroup(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: add2Group
	 * @Description: 往会场添加设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int add2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: searchRegion
	 * @Description: 查询行政结构
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> searchRegion(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: searchUnit
	 * @Description: 查询成员单位
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> searchUnit(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getLock
	 * @Description: 获取用户组锁定状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserVO> getLock(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: lock
	 * @Description: 锁定用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int lock(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: unlock
	 * @Description: 解锁用户组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int unlock(Map<String, Object> paramsMap);

	
	/**
	 * 通过会场id查找设备
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getDevicesByDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: userGroupList
	 * @Description: 获取用户组列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserGroupVO> getUserGroupList(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getUserGroupCount
	 * @Description: 获取用户组列表（会场视图）数量
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int getUserGroupCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: searchGroup
	 * @Description: 精确查找用户组下的会场（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> searchGroup(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: groupList
	 * @Description: 获取用户组下的会场列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> getGroupList(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getGroupCount
	 * @Description: 获取用户组下的会场总数
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int getGroupCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: generateGroups
	 * @Description: 快速生成会场列表（一个设备一个会场）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> generateGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: mvDev2Group
	 * @Description: 移动（多个）会场内的设备到其它（一个）会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	Map<String, Object> mvDev2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getDeviceListBooks
	 * @Description:通讯录资源管理获取设备列表
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getDeviceListBooks(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getDeviceListBooksCount
	 * @Description:通讯录资源管理获取设备列表总数目
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int getDeviceListBooksCount(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getDeviceGroupBooks
	 * @Description:通讯录资源管理获取设备列表总数目
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> getDeviceGroupBooks(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getDeviceGroupBooksCount
	 * @Description:通讯录用户管理获取初始群总数目
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int getDeviceGroupBooksCount(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 获取联系人（设备）列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getUserDevices(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取联系人（设备）列表总数
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getUserDevicesCount(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 获取联系人（设备）列表(为了兼容视联管家获取昵称，该昵称是从vc_user_dev表获取)
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getUserDevicesInfo(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取联系人（设备）列表总数
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getUserDevicesInfoCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 获取联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceGroupVO> getUserDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getUserDevGroups
	 * @Description: 根据用户id获取联系人（设备）群组列表总数
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	int getUserDevGroupsCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 添加联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param dgv
	 * @return
	 */
	Map<String, Object> addUserDevGroups(DeviceGroupVO dgv);

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param dgv
	 * @return
	 */
	Map<String, Object> editUserDevGroups(DeviceGroupVO dgv);

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int delUserDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 根据会议ID获取会议主席、发一、发二
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceGroupVO> getScheduleState(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 根据会议ID设置会议主席、发一、发二
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int updateScheduleState(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getGroupDevs
	 * @Description: 根据群组id获取联系人（设备）列表
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	List<DeviceVO> getGroupDevs(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getGroupDevs
	 * @Description: 根据群组id获取联系人（设备）列表总数
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	int getGroupDevsCount(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getDevgroup
	 * @Description:群组管理获取群信息
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserVO> getDevgroup(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getDevgroupCount
	 * @Description:群组管理获取群信息总条数
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int getDevgroupCount(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: addDevGroupBooks
	 * @Description:通讯录群组管理添加群组
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int addDevGroupBooks(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: deleteDevGroup
	 * @Description:通讯录群组管理删除群组
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int deleteDevGroup(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: updateDevGroupBooks
	 * @Description:通讯录群组管理修改群组基本信息
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */

	int updateDevGroupBooks(DeviceGroupVO devGroup);

	/**
	 * @param  
	 * 
	 * @Title: getUserDev
	 * @Description: 获取已选设备列表
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	List<DeviceVO> getUserDev(Map<String, Object> paramsMap);

	/**
	 * @param  
	 * 
	 * @Title: getUserDevCount
	 * @Description: 获取已选设备列表总条数
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int getUserDevCount(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: getDevsInGroup 
	 * @Description: TODO 获取群内已添加设备列表 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDevsInGroup(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: getDevsInGroupCount 
	 * @Description: 获取群内已添加设备列表 总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDevsInGroupCount(Map<String,Object> map);

	/**
	 * 
	 * @Title: editDftUserDevGroups
	 * @Description: 修改联系人（设备）群组（默认）
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	Map<String, Object> editDftUserDevGroups(DeviceGroupVO dgv);

	/**
	 * @return 
	 * 
	 * @Title: delDftUserDevGroups
	 * @Description: 删除联系人（设备）群组列表（默认）
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	int delDftUserDevGroups(Map<String, Object> paramsMap);
	
	/**
	 * @param  
	 * 
	 * @Title: updateDevGroup
	 * @Description: 置顶（取消置顶）群组
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int updateDevGroup(Map<String, Object> paramsMap);
	
	/***
	 * @Title: getDevInfo
	 * @Description: 获取设备列表
	 * @param paramsMap
	 * @return List<DeviceVO>
	 */
	List<DeviceVO> getDevInfo(Map<String, Object> paramsMap);
	
	/**
	 * @Title: distributeDevToUser
	 * @Description: 给用户分配设备
	 * @param list
	 * @return int
	 */
	int distributeDevToUser(List<Object> list);
	
	/**
	 * @Title: distributeDevToUser
	 * @Description: 根据用户id和设备id删除设备
	 * @param list
	 * @return int
	 */
	int deleteDevOfUser(String userId,List<String> devIdList);
	
	/**
	 * @Title: getDitributeDev
	 * @Description: 查询用户是否已经分配某设备
	 * 
	 */
	List<UserVO> getDitributeDev(Map<String, Object> map);
	/**
	 * 删除用户设备  2019/2/20 周逸芳合并16位与64位代码时增加  原16位没有
	 * @date 2018年9月25日 下午5:07:58
	 * @author wangqiubao
	 * @param map
	 * @return
	 */
	int deleteUserDev(Map<String, Object> map);
	
	/**
	 * @param  
	 * 
	 * @Title: updateUserDev
	 * @Description: 置顶（取消置顶）用户下设备
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int updateUserDev(Map<String, Object> map);

	/**
	 * 获取网管webservice接口的主路径  2019/2/20 周逸芳合并16位与64位代码时增加  原16位没有
	 * @param svr
	 * @return
	 */
	Map<String, Object> getNmBaseUrl(ServerVO svr);

	/**
	 * 获取网管webservice接口的token  2019/2/20 周逸芳合并16位与64位代码时增加  原16位没有
	 * @param baseUrl
	 * @param svr
	 * @return
	 */
	Map<String, Object> getNmToken(String baseUrl, ServerVO svr);
	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:00:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:00:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<ScheduleVO> getMasterBySchedule(Map<String, Object> paramsMap);
	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议总条数)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:00:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:00:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	int getMasterByScheduleCount(Map<String, Object> paramsMap);

	/** <pre>getMasterDetail(获取会议详情)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:44:32    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:44:32    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<ScheduleVO> getMasterDetail(Map<String, Object> paramsMap);
	/** <pre>getScheduleDevice(获取会议相关设备信息)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 下午12:56:32    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 下午12:56:32    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<DeviceVO> getScheduleDevice(Map<String, Object> paramsMap);
	/** <pre>getChildByUserId(视联汇根据用户获取用户所拥有设备的行政区域)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月26日 下午4:03:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月26日 下午4:03:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<RegionVO> getChildByUserId(Map<String, Object> paramsMap);
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
	List<Map<String, Object>> getMeetingDevList(Map<String, Object> paramsMap);
	
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
	int getMeetingDevListCount(Map<String, Object> paramsMap);
	
	
	/** <pre>getMeetingDevicesCount(获取所有正在会议中的设备数量)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月19日 下午2:37:47    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月19日 下午2:37:47    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	int getMeetingDevicesCount(Map<String, Object> paramsMap);
	
	/** <pre>getDevicesMeeting(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月19日 下午2:54:20    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月19日 下午2:54:20    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<DeviceVO> getDevicesMeeting(Map<String, Object> paramsMap);
	
	/** <pre>getDevRegion(根据设备号码或设备名称查询设备及设备所属行政区域拼接返回)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月20日 下午2:02:17    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月20日 下午2:02:17    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<DeviceVO> getDevRegion(Map<String, Object> paramsMap);
	
	/** <pre>getChildMeeting(获取会议中的行政结构)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月26日 下午3:41:52    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月26日 下午3:41:52    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<RegionVO> getChildMeeting(Map<String, Object> paramsMap);

	int updateById(Map<String, Object> paramsMap);
	
	/**
	 * 会管设备本地区号
	 * @date 2018年8月22日 上午11:27:18
	 * @author wangqiubao
	 * @return
	 */
	public String getLocalZoneno() ;
	
		/**
	 * 获取设备列表（掌上通）
	 * @param paramsMap
	 * @return
	 */
	List<Map<String, Object>> getDevDetail(Map<String, Object> paramsMap);
	
		/**
	 * 获取终端的告警信息(终端状态)
	 * @date 2018年11月14日 下午4:08:26
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	Map<String, Object> getAllAlarm(Map<String, Object> paramsMap);
	
	/**
	 * 获取所有成员单位列表（掌上通）
	 * @param paramsMap
	 * @return
	 */
	List<Map<String, Object>> getAllUnits(Map<String, Object> paramsMap);
	/**
	 * 获取设备别名
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getDevAlias(Map<String, Object> paramsMap);
	/**
	 * 获取设备精确号码
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getThisDevInfo(Map<String, Object> devMap);
	/****
	 * 根据终端号码，查询号码和对应的MAC
	 * @date 2018年11月24日
	 * @auth wangrz
	 * @param list 号码集合
	 * @return
	 */
	List<DeviceVO> getNumberAndMacByNumbers(List list);

	/**
	 * 推送终端业务状态
	 */
	/*public Map<String,Object> pushDeviceStatusReport(Map<String,Object> map);
	*/
	/**
	 * 手机控会用遥控器停止推送
	 */
	public void telecontrollerStop(Map<String,Object> map);
	/**
	 * 64位全网查询终端设备
	 */
	List<DeviceVO> getDevInfo64(Map<String, Object> paramsMap);
	/**
	 * 64位全网查询终端设备总条数
	 */
	int getUserDevicesCount64(Map<String, Object> paramsMap);
}
