package com.visionvera.service;

import java.util.List;

import com.visionvera.bean.slweoms.ServerHardwareVO;

/**
 * 服务器硬件信息Service
 * @author dql714099655
 *
 */
public interface ServersHardwareService {
	
	/**
	 * 查询单台服务器reserverDays天前的硬件数据id
	 * @param reserverDays
	 * @param serverUnique
	 * @return
	 */
	List<Long> getServersHardwareListBeforeDay(Integer reserverDays, String serverUnique);
	
	/**
	 * 根据硬件信息id删除服务器硬件信息
	 * @param serversHardwareIds
	 * @return
	 */
	int deleteServersHardwareList(List<Long> serversHardwareIds);
	
	/**
	 * 添加应用服务器硬件信息
	 * @param serverHardwareVO
	 */
	int insertServerHardwareVO(ServerHardwareVO serverHardwareVO);
	
	/**
	 * 获取服务器最早的硬件信息
	 * @param serverUnique 服务器唯一标识
	 * @param size  获取数量
	 * @return
	 */
	List<Long> getServersHardwareListEarliest(String serverUnique, Integer size);
}
