package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;

/**
 * 平台进程Service
 * @author dql
 *
 */
public interface PlatformProcessService {

	List<PlatformProcess> getAllProcesses(String serverUnique);

	void insertPlatformProcess(PlatformProcess platformProcess, String userId) throws Exception;
	
	/**
	 * 更新进程
	 * @param platformProcess
	 * @throws Exception 
	 */
	Integer updatePlatformProcess(PlatformProcess platformProcess) throws Exception;
	
	/**
	 * 更新平台和进程
	 * @param platformProcess
	 * @throws Exception 
	 */
	ReturnData updatePlatformAndProcess(PlatformVO platformVO) throws Exception;
	
	/**
	 * 根据id删除平台进程
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	Integer deleteProcessById(Integer id) throws Exception;
	
	/**
	 * 根据进程id查询平台列表
	 * @param id
	 * @return
	 */
	List<PlatformVO> getPlatformVOByProcessId(Integer id);
	
	/**
	 * 根据进程id查询平台进程
	 * @param id
	 * @return
	 */
	ReturnData getProcessByid(Integer id);
	
	/**
	 * 根据平台id查询进程列表
	 * @param registerid
	 * @return
	 */
	List<PlatformProcess> getPlatformProcessListByTposId(String registerid);
	
	/**
	 * 批量修改进程状态
	 * @param processList
	 */
	void recoverProcessStatusBatch(List<PlatformProcess> processList);
	
	/**
	 * 设置进程状态为正常状态
	 * @param processIds 进程id,多个逗号分隔
	 */
	void recoverProcessStateByIds(String processIds);
	
	/**
	 * 根据进程id删除进程
	 * @param processIds
	 */
	void deleteProcessByIds(String processIds);

	/**
	 * 根据进程Id恢复进程状态为正常状态
	 * @param procIdStr
	 */
    void recoverProcessStatusByIds(String procIdStr);
    
    /**
	 * 根据服务器ID查询平台数量
	 * @param id
	 * @return
	 */
	Map<String,Object> getTopsNumByServerUnique(String serverUnique);
}
