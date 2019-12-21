package com.visionvera.dao.authentication;

import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.datacore.ServerConfig;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-10-24
 */
public interface ServerConfigDao extends CrudDao<ServerConfig> {
	public List<ServerConfig> queryLmtConfigList();

	/**
	 * 通过条件查询服务配置信息
	 * @param serverConfig 查询条件.
	 * @return 服务配置信息
	 */
	List<ServerConfig> selectServerConfigByCondition(ServerConfig serverConfig);

	/**
	 * 通过平台ID查询服务器配置信息
	 * @param platformId 平台ID
	 * @return 服务配置信息
	 */
	ServerConfig selectServerConfigByOtherPlatformId(String platformId);

	/**
	 * 通过平台类别批量查询服务配置信息
	 * @param platformTypeArr 平台类别数组
	 * @return 服务配置信息
	 */
	List<ServerConfig> selectServerConfigByPlatformType(String[] platformTypeArr);

	/**
	 * 通过主键ID查询服务配置信息
	 * @param serverConfigId 主键ID
	 * @return 服务配置信息
	 */
	ServerConfig selectServerConfigById(String serverConfigId);
}
