package com.visionvera.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import org.springframework.web.multipart.MultipartFile;

import com.visionvera.bean.base.ReturnData;

/**
 * 检测探针Service
 * @author dql
 */
public interface WatchProbeService {
	
	/**
	 * 获取已部署监测探针的版本号
	 * @param paramMap
	 */
	ReturnData getDeployedVersion(Map<String, Object> paramMap);
	
	/**
	 * 部署检测端服务
	 * @param id 服务器id
	 * @param version 版本号
	 */
	Map<String,Object> displayWatchProbe(Integer id, String version) throws Exception;
	
	/**
	 * 测试服务器远程登录
	 * @param id 服务器id
	 * @return
	 */
	ReturnData testRemoteLogin(Integer id) throws Exception;
	
	/**
	 * 启动检测探针
	 * @param id 服务器id
	 * @return
	 */
	ReturnData startWatchProbe(Integer id) throws Exception;
	
	/**
	 * 关闭检测端
	 * @param id 服务器id
	 * @return
	 */
	ReturnData stopWatchProbe(Integer id) throws Exception;
	
	/**
	 * 批量测试服务器登录
	 * @param serverIdList
	 */
	List<Map<String,Object>> testRemoteLoginBatch(List<Integer> serverIdList) throws Exception;
	
	/**
	 * 移除检测探针
	 * @param id
	 * @return
	 */
	Map<String,Object> removeWatchProbe(Integer id,String userId) throws Exception;
	
	/**
	 * 下载监测探针安装包版本
	 * @param serverId
	 * @param version
	 * @throws Exception 
	 */
	void downLoadInstallPackage(Integer serverId, String version,HttpServletResponse response) throws Exception;
	
	/**
	 * 升级监测探针
	 * @param id  服务器id
	 * @param version  探针版本
	 * @return
	 * @throws Exception 
	 */
	Map<String,Object> upGradeWatchProbe(Integer id, String version) throws Exception;
	
	/**
	 * 获取最新的监测探针版本
	 * @param transferType 传输协议，IP或者V2V
	 * @return
	 */
	String getLocalRecentVersion(String transferType);
	
	/**
	 * 检测监测探针状态
	 * @param serverId 服务器Id
	 * @return
	 */
	ReturnData checkProbeStatus(Integer serverId);
	
	/**
	 * 升级V2V监测探针
	 * @param idList  应用服务器Id列表
	 * @param version	版本号
	 * @param session 
	 */
	void upV2VGradeProbe(List<Integer> idList, String version, Session session);
	
	/**
	 * 处理V2v探针升级结果信息
	 * @param message
	 */
	void handleUpGradeMsg(String message);
	
	/**
	 * 保存监测探针安装包
	 * @param filename 文件名
	 * @param type 下载监测探针
	 * @param object
	 * @param version
	 */
	void saveV2vProbeInstallPack(MultipartFile file, String filename, String type) throws Exception;
	
}
