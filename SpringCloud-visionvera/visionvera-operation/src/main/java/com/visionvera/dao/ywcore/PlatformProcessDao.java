package com.visionvera.dao.ywcore;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;

/**
 * 平台进程Dao
 * @author dql
 *
 */
public interface PlatformProcessDao {

	List<PlatformProcess> getAllProcesses(String serverUnique);
	
	/**
	 * 添加平台进程
	 * @param platformProcess
	 */
	void insertPlatformProcess(PlatformProcess platformProcess);
	
	/**
	 * 更新平台进程
	 * @param platformProcess
	 * @return
	 */
	Integer updatePlatformProcess(PlatformProcess platformProcess);
	
	/**
	 * 根据id删除进程
	 * @param id
	 * @return
	 */
	Integer deleteProcessById(Integer id);
	
	/**
	 * 根据进程id查询平台列表
	 * @param id
	 * @return
	 */
	List<PlatformVO> getPlatformListByProcessId(Integer id);
	
	/**
	 * 根据平台id查询进程信息
	 * @param tposRegisterid
	 * @return
	 */
	List<PlatformProcess> getProcessByTposRegisterid(String tposRegisterid);
	
	/**
	 * 根据进程id查询进程
	 * @param id
	 * @return
	 */
	Map<String,Object> getProcessById(Integer id);
	/**
	 * 批量更改进程状态
	 * @param processList
	 */
	void recoverProcessStatusBatch(List<PlatformProcess> processList);

	void recoverProcessStatusByIds(String processIds);
	
	/**
	 * 设置进程id为正常状态
	 * @param processIds
	 */
	void recoverProcessStateByIds(String processIds);
	
	/**
	 * 根据进程id删除进程
	 * @param processIds
	 */
	void deleteProcessByIds(String processIds);
	
	/**
	 * 根据服务器ID查询平台数量
	 * @param id
	 * @return
	 */
	Map<String,Object> getTopsNumByServerUnique(String serverUnique);

}
