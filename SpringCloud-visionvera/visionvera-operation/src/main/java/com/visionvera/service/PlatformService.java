package com.visionvera.service;

import com.visionvera.bean.slweoms.PlatformVO;

import java.util.List;

/**
 * 平台service
 * @author dql
 *
 */
public interface PlatformService {
	
	/**
	 * 根据平台唯一标识查询异常进程的数量
	 * @param string 
	 * @return
	 */
	int getProcessExceptionCount(String registerid);
	
	/**
	 * 根据服务器唯一标识查询平台列表
	 * @param serverUnique
	 * @return
	 */
	List<PlatformVO> getPlatformListByServerUnique(String serverUnique);
	
	/**
	 * 添加平台
	 * @param platformVO
	 * @param userId
	 * @return
	 */
	int insertPlatform(PlatformVO platformVO, String userId) throws Exception;
	
	/**
	 * 修改平台
	 * @param platformVO
	 * @return
	 */
	int updatePlatform(PlatformVO platformVO) throws Exception;
	
	/**
	 * 删除平台
	 * @param tposRegisterid
	 * @return
	 */
	int deletePlatform(String tposRegisterid) throws Exception;

	/**
	 * 根据平台唯一标识查询平台信息
	 * @param tposRegisterid  平台唯一标识
	 * @return
	 */
    PlatformVO getPlatformByTposRegisterid(String tposRegisterid);

	/**
	 * 恢复平台
	 * @param platid
	 * @return
	 */
	int recoverPlatform(String platid);

	/**
	 * 根据平台类型id更新版本
	 * @param platformType
	 * @return
	 */
    int updatePlatformVersionByPlatformType(int platformType,String version);
}
