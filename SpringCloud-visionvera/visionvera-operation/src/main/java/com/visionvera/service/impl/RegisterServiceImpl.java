package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.cms.ServerSyncVO;
import com.visionvera.dao.operation.RegisterDao;
import com.visionvera.dao.operation.SyncDataDao;
import com.visionvera.service.RegisterService;



/**
 * 
 * @ClassName: RegisterServiceImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 谢程算
 * @date 2017年10月10日 下午3:22:54
 * 
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class RegisterServiceImpl implements RegisterService {

	
	@Resource
	private RegisterDao registerDao;
	@Resource
	private SyncDataDao syncDataDao;
//	private static final Log logger = LogManager.getLogger(RegisterServiceImpl.class);
	
	/**
	 * 
	 * TODO webservice系统注册（使自己成为目标系统的子系统）
	 * @author 谢程算
	 * @date 2017年9月29日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int register(ServerSyncVO sv) {
		return registerDao.register(sv);
	}

	/**
	 * 
	 * TODO webservice系统解注册（使自己与目标系统解除父子关系）
	 * @author 谢程算
	 * @date 2017年9月29日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int unregister(ServerSyncVO sv) {
		return registerDao.unregister(sv);
	}

	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 多级系统-获取系统列表（子系统、父系统）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<ServerSyncVO> getSyncServers(ServerSyncVO sv, Map<String, Object> map) {
		return registerDao.getSyncServers(sv, new RowBounds((Integer)map.get("pageNum"),(Integer)map.get("pageSize")));
	}

	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 多级系统-获取系统列表（子系统、父系统）的总数
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int getSyncServersCount(Map<String, Object> paramsMap) {
		return registerDao.getSyncServersCount(paramsMap);
	}
	
	/**
	 * 
	 * @Title: editRegister
	 * @Description: 多级系统-编辑系统信息
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public int editRegister(ServerSyncVO sv) {
		return registerDao.editRegister(sv);
	}
	
	/**
	 * 
	 * @Title: getRouteFlag
	 * @Description: 分级系统-获取调度路由启用状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public String getRouteFlag() {
		return registerDao.getRouteFlag();
	}
	
	/**
	 * 
	 * @Title: getAvailableServers
	 * @Description: 分级系统-获取会议可用的服务器列表（根据主席所在区域查询）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public List<ServerSyncVO> getAvailableServers(Map<String, Object> paramsMap) {
		return registerDao.getAvailableServers(paramsMap);
	}

	/**
	 * 
	 * @Title: updateRouteFlag
	 * @Description: 分级系统-获取调度路由启用状态
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	public void updateRouteFlag(Integer status) {
		registerDao.updateRouteFlag(status);
	}

	/**
	 * 
	 * @Title: getSyncServers
	 * @Description: 获取所有子系统
	 * @param @return 参数说明
	 * @return List<ServerSyncVO> 返回类型
	 * @throws
	 */
	public List<ServerSyncVO> getSyncServers(ServerSyncVO sv) {
		return registerDao.getSyncServers(sv, new RowBounds());
	}
}
