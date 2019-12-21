package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.ServerBasics;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 服务器基本信息Service
 * @author dql
 *
 */
public interface ServerBasicsService {

	/**
	 * 根据服务器唯一标识查询服务器信息
	 * @param serverUnique
	 * @return
	 */
	ServerBasics getServerBasicsByServerUnique(String serverUnique);
	
	/**
	 * 更新服务器信息
	 * @param serverBasics
	 * @return
	 */
	ReturnData updateServerBasics(ServerBasics serverBasics) throws Exception;
	
	/**
	 * 查询所有的服务器信息
	 * @return
	 */
	List<ServerBasics> getAllServerBasics();
	
	/**
	 * 添加服务器信息
	 * @param serverBasics
	 * @return
	 */
	ReturnData insertServerBasics(ServerBasics serverBasics) throws Exception;
	
	/**
	 * 根据服务器id查询服务器信息
	 * @param id
	 * @return
	 */
	ServerBasics getServerBasicsById(Integer id);
	
	/**
	 * 根据参数查询服务器信息列表
	 * @param paramMap
	 * @return
	 */
	List<ServerBasics> getServerBasicByPage(Map<String, Object> paramMap);
	
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
	ServerBasics getServerBasicsByTposRegisterid(String tposRegisterid);
	
	/**
	 * 查询服务器阈值
	 * @param serverUnique
	 * @return
	 */
	ServerBasics getServerThreshold(String serverUnique);
	
	/**
	 * 根据Excel批量添加服务器信息
	 * @param workbook
	 * @return 
	 */
	ReturnData addServerBasicsOfExcel(Workbook workbook) throws Exception;
	
	/**
	 * 根据服务器id列表查询服务器信息
	 * @param serverIds
	 * @return
	 */
	List<ServerBasics> getServerBasicsListByIds(List<Integer> serverIds);
	
	/**
	 * 批量导出服务器信息
	 * @param serverIds
	 * @param transferType 
	 * @param response 
	 */
	void exportServerBasics(Map<String,Object> paramMap, HttpServletResponse response) throws Exception;
	
	/**
	 * 修改服务器的在线/离线状态
	 * @param serverBasics
	 */
	void updateServerOnLine(ServerBasics serverBasics);
	
	/**
	 * 根据进程id查询服务器信息
	 * @param processId
	 * @return
	 */
	ServerBasics getServerBasicsByProcessId(Integer processId);

	/**
	 * 更新服务器探针版本号
	 * @param serverBasics
	 */
    void updateServerProbeVersion(ServerBasics serverBasics);

	/**
	 * 根据终端号获取除自身外服务器数量
	 * @param terminalCode
	 * @param id
	 * @return
	 */
	int getServerCountByTerminalCodeExcludeSelf(String terminalCode, Integer id);
	
	/**
	 * 根据时间获取需要检查配置的平台列表
	 * @param terminalCode
	 * @param serverId
	 * @return
	 */
	List<Map<String,Object>> getConfigCheckList(String time);
}
