package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.ServerHardwareRelation;

/**
 * 应用服务器与最新硬件信息的关系
 * @author dql
 *
 */
public interface ServerHardwareRelationDao {
	
	/**
	 * 根据服务器唯一标识查询服务器硬件信息
	 * @param serverUnique
	 * @return
	 */
	ServerHardwareRelation getServerHardwareRelationByUnique(String serverUnique);
	
	/**
	 * 修改服务器最新硬件关系
	 * @param shRelation
	 */
	void insertServerHardwareRelation(ServerHardwareRelation shRelation);

	/**
	 * 修改服务器最新硬件信息关系
	 * @param shRelation
	 */
	void updateServerHardwareRelation(ServerHardwareRelation shRelation);

}
