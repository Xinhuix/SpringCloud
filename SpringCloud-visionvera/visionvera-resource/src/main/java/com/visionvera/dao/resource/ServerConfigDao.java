package com.visionvera.dao.resource;

import com.visionvera.bean.datacore.ServerConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作t_server_config表
 *
 */
public interface ServerConfigDao {
	/**
	 * 查询服务配置，并携带出对应的平台相关信息
	 * @param serverConfig 查询条件
	 * @return
	 */
	List<ServerConfig> selectServerConfigWithPlatform(ServerConfig serverConfig);

	/**
	 * 通过用户ID和平台类别查询服务器信息
	 * @param userId 用户ID
	 * @param platformType 平台类别
	 * @return
	 */
	List<ServerConfig> selectServerConfigByUserIdAndPlatformType(@Param("userId") String userId, @Param("platformType") String platformType);
}
