package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 服务器基本信息dao
 * @author dql
 *
 */
public interface ServerBasicsDao {
	
	/**
	 * 根据服务器唯一标识查询服务器基本信息
	 * @param serverUnique
	 * @return
	 */
	ServerBasics getServerBasicsByServerUnique(String serverUnique);
	
	/**
	 * 更新服务器信息
	 * @param serverBasics
	 * @return
	 */
	int updateServerBasic(ServerBasics serverBasics);
	
	/**
	 * 查询所有的服务器信息，包括已删除服务器
	 * @return
	 */
	List<ServerBasics> getAllServerBasics();
	
	/**
	 * 添加服务器信息
	 * @param serverBasics
	 * @return
	 */
	int insertServerBasics(ServerBasics serverBasics);
	
	/**
	 * 根据服务器id查询服务器信息
	 * @param id
	 * @return
	 */
	ServerBasics getServerBasicsById(Integer id);
	
	/**
	 * 查询服务器列表
	 * @param paramMap
	 * @return
	 */
	List<ServerBasics> getServerBasicsList(Map<String, Object> paramMap);
	
	/**
	 * 修改服务器报警阈值
	 * @param serverBasics
	 * @return
	 */
	int updateServerThreshold(ServerBasics serverBasics);
	
	/**
	 * 根据平台id查询服务器信息
	 * @param tposRegisterid
	 * @return
	 */
	ServerBasics getServerBasicsByRegisterid(String tposRegisterid);
	
	/**
	 * 查询服务器报警阈值
	 * @param serverUnique
	 * @return
	 */
	ServerBasics getServerThreshold(String serverUnique);
	
	/**
	 * 根据服务器唯一标识查询平台列表
	 * @param serverUnique
	 * @return
	 */
	List<PlatformVO> getPlatformVOListByServerUnique(String serverUnique);
	
	/**
	 * 根据服务器id列表查询服务器信息
	 * @param serverIds
	 * @return
	 */
	List<ServerBasics> getServerBasicsByIds(@Param("serverIds") List<Integer> serverIds);
	
	/**
	 * 批量插入服务信息
	 * @param serverBasicsList
	 */
	void insertServerBasicsBatch(@Param("serverBasicsList") List<ServerBasics> serverBasicsList);
	
	/**
	 * 根据服务器名称查询服务器列表
	 * @param serverName
	 * @return
	 */
	List<ServerBasics> checkServerBasicsByName(String serverName);
	
	/**
	 * 根据服务器管理ip查询服务器列表
	 * @param serverManageIp
	 * @return
	 */
	List<ServerBasics> checkServerBasicsByManageIp(String serverManageIp);
	
	/**
	 * 根据服务器名称查询除自身外的服务器
	 * @param serverName
	 * @param serverId 
	 * @return
	 */
	List<ServerBasics> checkServerBasicsByNameExcludeSelf(@Param("serverName")String serverName, @Param("serverId")Integer serverId);
	
	/**
	 * 根据服务器管理ip查询除自身外的服务器
	 * @param serverManageIp
	 * @param serverId
	 * @return
	 */
	List<ServerBasics> checkServerBasicsByManageIpExcludeSelf(@Param("serverManageIp")String serverManageIp, @Param("serverId")Integer serverId);
	
	/**
	 * 修改服务器的在线/离线状态
	 * @param serverBasics
	 */
	void updateServerOnLineState(ServerBasics serverBasics);
	
	/**
	 * 获取服务器的监测探针最大版本
	 * @param serverIdList
	 * @return
	 */
	String getMaxVersionByServerIds(@Param("serverIdList")List<String> serverIdList);
	
	/**
	 * 删除服务器信息
	 * @param serverBasics
	 */
	void deleteServerBasics(Integer id);
	
	/**
	 * 根据进程id查询服务器信息
	 * @param processId 进程信息
	 * @return
	 */
	ServerBasics getServerBasicsByProcessId(Integer processId);
	
	/**
	 * 获取服务器已部署探针版本号
	 * @param paramMap
	 */
	List<String> getDeployedVersion(Map<String, Object> paramMap);
	
	/**
	 * 获取所有的探针管理ip
	 * @return
	 */
	List<String> getAllProbeManagerIp();

	/**
	 * 更新服务器探针版本号
	 * @param serverBasics
	 */
    void updateServerProbeVersion(ServerBasics serverBasics);

	/**
	 * 根据终端号查询服务器数量
	 * @param terminalCode
	 * @return
	 */
	int getServerCountByTerminalCode(String terminalCode);

	/**
	 * 根据终端号获取服务器数量（除自身外）
	 * @param terminalCode
	 * @param serverId
	 * @return
	 */
	int getServerCountByTerminalCodeExcludeSelf(@Param("terminalCode")String terminalCode, @Param("serverId")Integer serverId);
	
	/**
	 * 根据时间获取需要检查配置的平台列表
	 * @param terminalCode
	 * @param serverId
	 * @return
	 */
	List<Map<String,Object>> getConfigCheckList(String time);
}
