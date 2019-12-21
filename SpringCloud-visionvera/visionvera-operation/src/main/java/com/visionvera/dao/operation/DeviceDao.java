package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.CloudVO;
import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceRoleVO;
import com.visionvera.bean.cms.DeviceTreeVO;
import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerInfoVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;

public interface DeviceDao {

	/**
	 * 
	 * @Title: getDeviceList 
	 * @Description: TODO 获取设备列表 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDeviceList(Map<String,Object> map, RowBounds rowBounds);

	/**
	 * 
	 * @Title: getDevsInGroup 
	 * @Description: TODO 获取群内已添加设备列表 
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDevsInGroup(Map<String,Object> map, RowBounds rowBounds);
	
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
	 * @Title: getDevList 
	 * @Description: TODO 获取设备列表 （无权限要求）
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDevList(Map<String,Object> map, RowBounds rowBounds);
	
	
	/**
	 * 
	 * @Title: getDeviceListCount 
	 * @Description: 获取设备列表总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDeviceListCount(Map<String,Object> map);

	/**
	 * 
	 * @Title: getDevListCount 
	 * @Description: 获取设备列表总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDevListCount(Map<String,Object> map);
	
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
	 * @Title: devicesInGroup 
	 * @Description: TODO 根据会场ID获取设备列表
	 * @param @return  参数说明 
	 * @return List<DeviceTypeVO>    返回类型 
	 * @throws
	 */
	List<DeviceVO> devicesInGroup(Map<String,Object> map);

	/**
	 * 
	 * @Title: getEditDeviceList 
	 * @Description: TODO 获取设备列表（供新增、修改页面的选择页使用）
	 * @param @return  参数说明 
	 * @return List<Device>    返回类型 
	 * @throws
	 */
	List<DeviceVO> getEditDeviceList(Map<String,Object> map);

	/**
	 * 
	 * @Title: addDevice 
	 * @Description: TODO 新增设备
	 * @param @param device
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevice(DeviceVO device);
	
	/**
	 * 
	 * @Title: updateDevice 
	 * @Description: TODO 更新设备信息 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateDevice(DeviceVO device);
	
	/**
	 * 
	 * @Title: updateDeviceGis 
	 * @Description: TODO 更新设备gis信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateDeviceGis(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: deleteDevice 
	 * @Description: TODO 删除设备 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDevice(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: onlineDeviceCount 
	 * @Description: 在线设备统计 
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int onlineDeviceCount();
	
	/**
	 * @param rowBounds 
	 * 
	 * @Title: getDeviceGroupList 
	 * @Description: 获取会场列表 
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getDeviceGroupList(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * 
	 * @Title: addDeviceGroup 
	 * @Description: 新增会场信息
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDeviceGroup(DeviceGroupVO deviceGroup);

	/**
	 * 
	 * @Title: addDevGroup2UserGroup 
	 * @Description: 创建用户组和会场的关联关系
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevGroup2UserGroup(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: deleteDeviceGroup 
	 * @Description: 删除会场信息
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDeviceGroup(Map<String, Object> map);

	/**
	 * 
	 * @Title: isGroupEditable 
	 * @Description: 判断会场是否允许编辑-使用该会场的用户是否处于登录状态
	 * @param @param map
	 * @param @return  参数说明 
	 * @return UserVO    返回类型 
	 * @throws
	 */
	List<UserVO> isGroupEditable(Map<String, Object> map);

	/**
	 * 
	 * @Title: isDeviceDeletable 
	 * @Description: 判断会场是否允许编辑-要删除的设备是否与有效预约相关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return DeviceVO    返回类型 
	 * @throws
	 */
	List<DeviceVO> isDeviceDeletable(Map<String, Object> map);

	/**
	 * 
	 * @Title: updateDeviceGroup 
	 * @Description: 更新会场信息 
	 * @param @param userGroup
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateDeviceGroup(DeviceGroupVO deviceGroup);

	/**
	 * 
	 * @Title: getDeviceGroupListCount 
	 * @Description: 获取会场列表总数
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDeviceGroupListCount(Map<String, Object> map);

	/**
	 * 
	 * @Title: addDevice2Group 
	 * @Description: 添加会场和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevice2Group(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: addDevices2Group 
	 * @Description: 添加群组和联系人的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int addDevices2Group(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: deleteDevice2Group 
	 * @Description: 删除会场和设备的关联
	 * @param @param map
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int deleteDevice2Group(Map<String, Object> paramsMap);

	/**
	 * @param map 
	 * 
	 * @Title: getAllGroups 
	 * @Description: 获取所有会场
	 * @return int    返回类型 
	 * @throws
	 */
	List<DeviceGroupVO> getAllGroups(Map<String, Object> map);

	/**
	 * @return 
	 * 
	 * @Title: upLoadExcel
	 * @Description: 解析上传的excel文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	void upLoadExcel(List<DeviceVO> devList);
	/**
	 * @return 
	 * 
	 * @Title: batchAdd
	 * @Description: 从手机端获取设备列表批量入库
	 * @param @param list
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	int batchAdd(List<DeviceVO> devList);
	/**
	 * @param rowBounds 
	 * 
	 * @Title: avaliableDevices
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> avaliableDevices(Map<String, Object> map, RowBounds rowBounds);

	/**
	 * @param  
	 * 
	 * @Title: avaliableDevicesCount
	 * @Description: 获取所有未分组用户
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int avaliableDevicesCount(Map<String, Object> map);
	
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
	 * @Title: getDeviceByGroupIds
	 * @Description: 获取设备组内的设备ID列表
	 * @param  map
	 * @param 
	 * @return DeviceVO 返回类型
	 * @throws
	 */
	List<DeviceVO> getDeviceByGroupIds(Map<String, Object> paramsMap);
	
	/**
	 * @Title：getDeviceListByUser 
	 * @Description：根据用户获取设备列表
	 * @param  map RowBounds
	 * @param 
	 * @return List<DeviceVO>    
	 * @throws
	 */
	List<DeviceVO> getDeviceListByUser(Map<String, Object> map, RowBounds rowBounds);
	
	/**
	 * 
	 * @Title: getDeviceCountByUser 
	 * @Description: 获取设备列表总条目
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int getDeviceCountByUser(Map<String,Object> map);
	
	/**
	 * 
	 * @Title: getDevice 
	 * @Description: 获取设备信息
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return DeviceVO    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDevice(Map<String, Object> map);

	/**
	 * 
	 * @Title: getOrgDevGroups
	 * @Description: 根据工作单位获取会场列表
	 * @param  Map
	 * @param 
	 * @return List<DeviceGroupVO> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> getOrgDevGroups(Map<String, Object> map);

	/**
	 * 
	 * @Title: getRegion
	 * @Description: 获取行政节点
	 * @param  Map
	 * @param 
	 * @return list 返回类型
	 * @throws
	 */
	List<RegionVO> getRegion();

	/**
	 * 
	 * @Title: delRegion
	 * @Description: 删除行政节点
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int delRegion(List<String> regionIds);

	/**
	 * 
	 * @Title: addRegion
	 * @Description: 新增行政节点
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addRegion(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: updateRegion
	 * @Description: 更新行政节点
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int updateRegion(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: getServerInfo
	 * @Description: 获取服务器
	 * @param  Map
	 * @param 
	 * @return list 返回类型
	 * @throws
	 */
	List<ServerInfoVO> getServerInfo();

	/**
	 * 
	 * @Title: delServerInfo
	 * @Description: 删除服务器信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int delServerInfo(List<String> list);


	/**
	 * 
	 * @Title: addServerInfo
	 * @Description: 新增服务器信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addServerInfo(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: updateServerInfo
	 * @Description: 更新设备信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int updateServerInfo(List<Map<String, Object>> list);
	
	/**
	 * 
	 * @Title: getCloud
	 * @Description: 获取云
	 * @param  Map
	 * @param 
	 * @return list 返回类型
	 * @throws
	 */
	List<CloudVO> getCloud();

	
	/**
	 * 
	 * @Title: delCloud
	 * @Description: 删除云
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int delCloud(List<String> list);
	
	/**
	 * 
	 * @Title: addCloud
	 * @Description: 新增云
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addCloud(List<Map<String, Object>> list);
	
	/**
	 * 
	 * @Title: updateCloud
	 * @Description: 更新云
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int updateCloud(List<Map<String, Object>> list);
	/**
	 * 
	 * @Title: getDevIds
	 * @Description: 获取所有设备的ID集合
	 * @param  Map
	 * @param 
	 * @return list 返回类型
	 * @throws
	 */
	List<Integer> getDevIds();

	/**
	 * 
	 * @Title: getDeviceInfo
	 * @Description: 获取设备
	 * @param  Map
	 * @param 
	 * @return list 返回类型
	 * @throws
	 */
	List<DeviceVO> getDeviceInfo();

	/**
	 * 
	 * @Title: delDeviceInfo
	 * @Description: 删除设备信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int delDeviceInfo(List<String> list);

	/**
	 * 
	 * @Title: addDeviceInfo
	 * @Description: 新增设备信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addDeviceInfo(List<Map<String, Object>> list);

	
	/**
	 * 
	 * @Title: addDeviceInfo64
	 * @Description: 新增设备信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addDeviceInfo64(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: addUnitInfo
	 * @Description: 新增客户成员单位信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addUnitInfo(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: addDeviceDetail
	 * @Description: 新增设备详细信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int addDeviceDetail(List<Map<String, Object>> list);

	/**
	 * 
	 * @Title: updateDeviceInfo
	 * @Description: 更新设备信息
	 * @param  Map
	 * @param 
	 * @return int 返回类型
	 * @throws
	 */
	int updateDeviceInfo(List<Map<String, Object>> list);

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
	 * @param rowBounds 
	 * 
	 * @Title: getDevices
	 * @Description: 获取设备信息
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<DeviceVO> getDevices(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * @param rowBounds 
	 * 
	 * @Title: getDevicesOfRegion
	 * @Description: 获取设备信息（获取区域节点下的所有设备，包含该区域所有子节点下的所有设备）
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	List<DeviceVO> getDevicesOfRegion(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * @Title: getServersCountOfRegion
	 * @Description: 统计获取区域节点下的所有服务器数量 
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int getServersCountOfRegion(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getDevicesCountOfRegion
	 * @Description: 统计获取区域节点下的所有设备数量 
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int getDevicesCountOfRegion(Map<String, Object> paramsMap);
	
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
	 * @Title: getUnitChild
	 * @Description: 获取成员单位的子节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> getUnitChild(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: auth
	 * @Description: 给用户组授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int auth(Map<String, Object> paramsMap);

	/**
	 * @param rowBounds 
	 * 
	 * @Title: getAuthedOfRegion
	 * @Description: 获取某一节点下户组内已授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getAuthedOfRegion(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * @param rowBounds 
	 * 
	 * @Title: getAuthedOfRegions
	 * @Description: 获取某一节点下户组内已授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getAuthedOfRegions(Map<String, Object> paramsMap, RowBounds rowBounds);
	
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
	 * @param rowBounds 
	 * 
	 * @Title: getNotAuthedOfRegion
	 * @Description: 获取某一节点下户组内未授权设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getNotAuthedOfRegion(Map<String, Object> paramsMap, RowBounds rowBounds);
	
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
	 * @param rowBounds 
	 * 
	 * @Title: getNotAuthedOfRegions
	 * @Description: 获取某一节点下户组内未授权的所有设备 （含子节点）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceVO> getNotAuthedOfRegions(Map<String, Object> paramsMap, RowBounds rowBounds);
	
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
	int delAuthed(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: rmFromGroup
	 * @Description: 移除会场内的设备
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int rmFromGroup(Map<String, Object> paramsMap);

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
	 * @Title: add2Groups
	 * @Description: 往会场添加设备（快速生成会场使用）
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int add2Groups(List<Map<String, Object>> list);
	
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
	 * 通过多个会场id获取设备列表
	 * @param map
	 * @return
	 */
	List<DeviceVO> getDevicesByDevGroups(Map<String, Object> map);

	/**
	 * @param rowBounds 
	 * 
	 * @Title: userGroupList
	 * @Description: 获取用户组列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<UserGroupVO> getUserGroupList(Map<String, Object> paramsMap, RowBounds rowBounds);
	
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
	 * @Title: searchGroupByName
	 * @Description: 根据会场名称查找会场
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> searchGroupByName(Map<String, Object> paramsMap);

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
	 * @param rowBounds 
	 * 
	 * @Title: groupList
	 * @Description: 获取用户组下的会场列表（会场视图）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<DeviceGroupVO> getGroupList(Map<String, Object> paramsMap, RowBounds rowBounds);
	
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
	int generateGroups(Map<String, Object> paramsMap);
	
	/**
	 * 获取设备的树结构根据设备号码,只获取设备和设备的上一次组织节点
	 * @param paramsMap
	 * @return
	 */
	List<DeviceTreeVO> getDevTreeData(Map<String, Object> paramsMap);
	
	/**
	 * 获取被删除的设备从授权组织节点中
	 * @return
	 */
	List<DeviceTreeVO> getDelDevTreeData(Map<String, Object> paramsMap);

	/**
	 * 通讯录资源管理获取设备列表
	 * @return
	 */
	List<DeviceVO> getDeviceListBooks(Map<String, Object> map,
			RowBounds rowBounds);

	/**
	 * 通讯录资源管理获取设备列表总数目
	 * @return
	 */
	int getDeviceListBooksCount(Map<String, Object> paramsMap);

	/**
	 * 用户管理页面初始群授权获取
	 * @return
	 */
	List<DeviceGroupVO> getDeviceGroupBooks(Map<String, Object> map,
			RowBounds rowBounds);
	/**
	 * 用户管理页面初始群授权获取总条数
	 * @return
	 */
	int getDeviceGroupBooksCount(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * TODO 获取联系人（设备）列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @param rowBounds 
	 * @return
	 */
	List<DeviceVO> getUserDevices(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * @param rowBounds 
	 * @return
	 */
	List<DeviceVO> getUserDevicesInfo(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	List<DeviceGroupVO> getUserDevGroups(Map<String, Object> paramsMap, RowBounds rowBounds);

	/**
	 * 
	 * TODO 获取联系人（设备）群组列表总数
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getUserDevGroupsCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: getGroupDevs
	 * @Description: 根据群组id获取联系人（设备）列表
	 * @param @return 参数说明
	 * @return ResponseInfo<?> 返回类型
	 * @throws
	 */
	List<DeviceVO> getGroupDevs(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * TODO 添加联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int addUserDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceGroupVO> editUserDevGroups(Map<String, Object> paramsMap);

	/**
	 * 
	 * TODO 修改联系人（设备）群组列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	List<DeviceGroupVO> delUserDevGroups(Map<String, Object> paramsMap);

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
	 * 群组管理获取群信息
	 * @return
	 */
	List<UserVO> getDevgroup(Map<String, Object> map, RowBounds rowBounds);
	/**
	 * 群组管理获取群信息总条数
	 * @return
	 */
	int getDevgroupCount(Map<String, Object> paramsMap);

	/**
	 * 群组管理新增群组
	 * @return
	 */
	int addDevGroupBooks(Map<String, Object> paramsMap);


	/**
	 * 群组管理删除群与设备关联
	 * @return
	 */
	int deleteDevGroup(DeviceGroupVO devGroup);

	/**
	 * @param  
	 * 
	 * @Title: getUserDev
	 * @Description: 获取已选设备列表
	 * @param @return 参数说明
	 * @return List 返回类型
	 * @throws
	 */
	List<DeviceVO> getUserDev(Map<String, Object> paramsMap, RowBounds rowBounds);

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
	 * @param  
	 * 
	 * @Title: deleteUserDeviceGroup
	 * @Description: 删除用户和群的关联关系（单个）
	 * @param @return 参数说明
	 * @return int 返回类型
	 * @throws
	 */
	int deleteUserDeviceGroup(Map<String, Object> paramsMap);

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
	 * @return list
	 */
	List<DeviceVO> getDevInfo(Map<String, Object> paramsMap,RowBounds rowBounds);
	
	/**
	 * @Title: distributeDevToUser
	 * @Description: 给用户分配设备
	 * @param list
	 * @return int
	 */
	int distributeDevToUser(List<Object> list);
	
	/**
	 * @Title: getDitributeDev
	 * @Description: 查询用户是否已经分配某设备
	 * 
	 */
	List<UserVO> getDitributeDev(Map<String, Object> map);
	
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

	/** <pre>getMasterBySchedule(以会议主席设备为单位查询所有以此设备为主席的会议)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年4月28日 上午11:00:36    
	 * 修改人：周逸芳        
	 * 修改时间：2018年4月28日 上午11:00:36    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<ScheduleVO> getMasterBySchedule(Map<String, Object> map,
			RowBounds rowBounds);
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

	/**
	 * 
	 * @Title: getDeviceId 
	 * @Description: 获取设备ID信息
	 * @param @param map selct条件集合
	 * @param @return  参数说明 
	 * @return DeviceVO    返回类型 
	 * @throws
	 */
	List<DeviceVO> getDeviceId(Map<String, Object> map);
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
	/**
	 * 删除用户设备
	 * @date 2018年9月25日 下午5:07:58
	 * @author wangqiubao
	 * @param map
	 * @return
	 */
	int deleteUserDev(Map<String, Object> map);
	
	/**
	 * 通过会议ID查询设备信息
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> selectDeviceByScheduleId(Map<String, Object> paramsMap);
	
	/** <pre>getMeetingDevicesCount(获取会议中设备数量)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月19日 下午2:27:42    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月19日 下午2:27:42    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	int getMeetingDevicesCount(Map<String, Object> paramsMap);
	
	/** <pre>getRegionSvrName(获取设备号码及区域)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月23日 下午5:16:58    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月23日 下午5:16:58    
	 * 修改备注： 
	 * @param devRegion
	 * @return</pre>    
	 */
	List<DeviceVO> getRegionSvrName(List<DeviceVO> devRegion);
	
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
	
	String getSingleRegionSvrName(Map<String, Object> paramsMap);
	
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
	/** <pre>getDevicesMeetingDetail(根据设备id获取设备正在参会的会议详情)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年6月20日 上午9:36:49    
	 * 修改人：周逸芳        
	 * 修改时间：2018年6月20日 上午9:36:49    
	 * 修改备注： 
	 * @param paramsMap
	 * @return</pre>    
	 */
	List<ScheduleVO> getDevicesMeetingDetail(Map<String, Object> paramsMap);
    
	/**
	 * 根据设备id查询设备
	 * @param deviceId
	 * @return
	 */
	DeviceVO getDeviceById(String deviceId);
	
	/**
	 * 根据区域id查询行政区域信息
	 * @param regionId
	 * @return
	 */
	RegionVO getRegionById(String regionId);
	/**
	 * @Title: deleteDevOfUser
	 * @Description: 根据用户id和设备id删除设备
	 * @param 
	 * @return 
	 */
	int deleteDevOfUser(@Param("userId") String userId,@Param("devId") List<String> devId);

	int updateById(Map<String, Object> paramsMap);
	/**
	 * 获取设备列表（掌上通）
	 */
	List<Map<String, Object>> getDevDetail(Map<String, Object> paramsMap);
	
	/**
	 * 获取所有成员单位列表（掌上通）
	 */
	List<Map<String, Object>> getAllUnits(Map<String, Object> paramsMap);
	/***
	 * @Title: getThisDevInfo
	 */
	List<DeviceVO> getThisDevInfo(Map<String, Object> map, RowBounds rowBounds);
	/**
	 * 获取设备别名
	 * @param paramsMap
	 * @return
	 */
	List<DeviceVO> getDevAlias(Map<String, Object> paramsMap);
	/****
	 * 根据终端号码，查询号码和对应的MAC
	 * @date 2018年11月24日
	 * @auth wangrz
	 * @param list 号码集合
	 * @return
	 */
	List<DeviceVO> getNumberAndMacByNumbers(List list);
	/**
	 * 获取终端业务列表
	 * @author 褚英奇
	 */
	List<DeviceVO> getDevOperationStatus(Map<String, Object> paramsMap);
	/**
	 * 根据主席号码获取会议信息
	 * @author 周逸芳
	 */
	List<ScheduleFormVO> getScheduleByMaster(String devId);
	
	List<DeviceVO> getDevMessage(Map<String, Object> paramsMap);

	List<DeviceVO> getDevInfo64(Map<String, Object> paramsMap);

	int getUserDevicesCount64(Map<String, Object> paramsMap);
}
