package com.visionvera.dao.ywcore;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;

/**
 * 平台dao
 * @author dql714099655
 *
 */
public interface PlatformDao {
	
	/**
	 * 根据平台id查询进程异常的数量
	 * @param registerid
	 * @return
	 */
	int getProcessExceptionCount(String registerid);
	
	/**
	 * 根据服务器信息查询平台列表
	 * @param serverUnique
	 * @return
	 */
	List<PlatformVO> getPlatformListByServerUnique(String serverUnique);
	
	/**
	 * 添加平台
	 * @param platformVO
	 * @return
	 */
	int insertPlatform(PlatformVO platformVO);
	
	/**
	 * 修改平台
	 * @param platformVO
	 * @return
	 */
	int updatePlatform(PlatformVO platformVO);
	
	/**
	 * 删除平台
	 * @param tposRegisterid
	 * @return
	 */
	int deletePlatform(String tposRegisterid);

	/**
	 * 根据平台唯一标识获取平台
	 * @param tposRegisterid
	 * @return
	 */
    PlatformVO getPlatformByTposRegisterid(String tposRegisterid);

	/**
	 * 根据平台唯一标识查询服务器信息
	 * @param tposRegisterid
	 * @return
	 */
	ServerBasics getServerBasicsByTposRegisterid(String tposRegisterid);

	/**
	 * 根据平台id恢复平台
	 * @param tposRegisterid
	 * @return
	 */
    int recoverPlatform(String tposRegisterid);

	/**
	 * 根据平台类型id更新平台版本
	 * @param platformType
	 * @return
	 */
	int updatePlatfomVersionByPlatformType(@Param("platformType") int platformType, @Param("version")String version);
	/**
	 * 根据进程ID查询平台和进程信息
	 * @param platformType
	 * @return
	 */
	Map<String,Object> getPlatformProcessByProcessId(String processId);
	
	
	
}
