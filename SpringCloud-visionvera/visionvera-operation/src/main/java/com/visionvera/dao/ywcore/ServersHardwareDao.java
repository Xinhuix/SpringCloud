package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.ServerHardwareVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器硬件信息Dao
 * @author dql
 *
 */
public interface ServersHardwareDao {
	
	/**
	 * 查询过期的服务器硬件信息
	 * @param reserverDays
	 * @return
	 */
	List<Long> getServersHardwareListBeforeDay(@Param("reserverDays")Integer reserverDays,@Param("serverUnique")String serverUnique);
	
	/**
	 * 删除服务器硬件信息根据id
	 * @param serverHardwareIds
	 * @return
	 */
	Integer deleteServersHardwareList(@Param("serverHardwareIds")List<Long> serverHardwareIds);
	
	/**
	 * 添加应用服务器硬件信息
	 * @param serverHardwareVO
	 * @return
	 */
	int insertServersHardware(ServerHardwareVO serverHardwareVO);

	/**
	 * 获取最早的服务器硬件信息
	 * @param serverUnique
	 * @param size
	 * @return
	 */
	List<Long> getServersHardwareListEarlist(@Param("serverUnique")String serverUnique, @Param("size") Integer size);
}
