package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.ServerSyncVO;



/**
 * 
 * @ClassName: ScheduleDao 
 * @Description: TODO 预约Dao接口
 * @author zhaolei
 * @date 2016年8月12日 下午3:15:44 
 *
 */
public interface RegisterDao {
	
	/**
	 * 
	 * TODO webservice系统注册（使自己成为目标系统的子系统）
	 * @author 谢程算
	 * @date 2017年9月29日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int register(ServerSyncVO sv);

	/**
	 * 
	 * TODO webservice系统解注册（使自己与上级系统解除父子关系）
	 * @author 谢程算
	 * @date 2017年9月29日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	int unregister(ServerSyncVO sv);
	
	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 多级系统-获取系统列表（子系统、父系统）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<ServerSyncVO> getSyncServers(ServerSyncVO sv, RowBounds rowBounds);

	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 多级系统-获取系统列表（子系统、父系统）的总数
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int getSyncServersCount(Map<String, Object> paramsMap);

	/**
	 * 
	 * @Title: editRegister
	 * @Description: 多级系统-编辑系统信息
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	int editRegister(ServerSyncVO sv);

	/**
	 * 
	 * @Title: getRouteFlag
	 * @Description: 分级系统-获取调度路由启用状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	String getRouteFlag();
	
	/**
	 * 
	 * @Title: getAvailableServers
	 * @Description: 分级系统-获取会议可用的服务器列表（根据主席所在区域查询）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<ServerSyncVO> getAvailableServers(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: updateRouteFlag
	 * @Description: 分级系统-更新调度路由启用状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	void updateRouteFlag(Integer status);
}
