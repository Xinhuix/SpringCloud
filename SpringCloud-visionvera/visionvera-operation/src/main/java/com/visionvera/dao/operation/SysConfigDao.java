package com.visionvera.dao.operation;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceLogVO;
import com.visionvera.bean.cms.LogKeyValueVO;
import com.visionvera.bean.cms.ServerTypeVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.bean.cms.VersionVO;


public interface SysConfigDao {
	/**
	 * @param paramsMap 
	 * 
	 * @Title: getServer 
	 * @Description: 获取服务配置
	 * @param @return  参数说明 
	 * @return ServerVO    返回类型 
	 * @throws
	 */
	ServerVO getServer(Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @Title: updateServer 
	 * @Description: 更新服务配置
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateServer(ServerVO server);

	/**
	 * 
	 * @Title: getSms 
	 * @Description: 获取服务配置
	 * @param @return  参数说明 
	 * @return SmsVO    返回类型 
	 * @throws
	 */
	SmsVO getSms();
	
	/**
	 * 
	 * @Title: updateSms 
	 * @Description: 更新短信配置
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateSms(SmsVO sms);

	/**
	 * 
	 * @Title: getServerType 
	 * @Description: 获取服务器类型列表
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ServerTypeVO> getServerType();
	
	/**
	 * 
	 * @Title: getVersion 
	 * @Description: 获取系统版本
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<VersionVO> getVersion();

	List<ConstDataVO> getApproval();

	void updateApr(List<ConstDataVO> list);

	void deleteData();

	List<ConstDataVO> getAprUuid(List<String> list);

	void updateSes(ConstDataVO dataVO);

	void deleteSes(ConstDataVO dataVO);

	String getPTM();

	ServerVO getSSOServer(Map<String, Object> paramsMap);

	int updateOpen(ServerVO ser);
	/**
	 * 
	 * @Title: getAprTime 
	 * @Description: 获取当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return String    返回类型 
	 * @throws
	 */
	List<ConstDataVO> getAprTime(ConstDataVO dataVO);
	/**
	 * 
	 * @Title: updateAprTime 
	 * @Description: 修改当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return     返回类型 
	 * @throws
	 */
	int updateAprTime(List<ConstDataVO> list);
	/**
	 * 
	 * @Title: getConfigUpdate 
	 * @Description: 获取可上传文件升级配置人员
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	List<ConstDataVO> getConfigUpdate();
	/**
	 * 
	 * @Title: updateConfigPerson 
	 * @Description: 修改可上传文件升级配置人员
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	void updateConfigPerson(List<ConstDataVO> list);
	/**
	 * 
	 * @Title: deleteConfigData 
	 * @Description: 删除可上传文件升级配置人员
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	void deleteConfigData();

	/**
	 * 
	 * @Title: getPerServer 
	 * @Description: 获取视联汇配置
	 * @return     返回类型 
	 * @throws
	 */
	List<ServerVO> getPerServer();

	/** <pre>getShold(获取阈值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月16日 下午4:47:52    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月16日 下午4:47:52    
	 * 修改备注： 
	 * @param list2
	 * @return</pre>    
	 */
	List<ConstDataVO> getShold(List<Integer> list2);
	
	/**
	 * @param rowBounds 
	 * @param 获取终端日志列表
	 * @return
	 */
	List<DeviceLogVO> getDevLog(Map<String, Object> paramsMap, RowBounds rowBounds);
	/**
	 * @param 获取终端日志列表总条数
	 * @return
	 */
	int getDevLogCount(Map<String, Object> paramsMap);
	/**
	 * @param 获取终端日志分析参数列表
	 * @return
	 */
	List<LogKeyValueVO> getDeviceLogKeyList(Map<String, Object> map, RowBounds rowBounds);
	/**
	 * @param 获取终端日志分析参数列表总条数
	 * @return
	 */
	int getDeviceLogKeyListCount(Map<String, Object> paramsMap);
	/**
	 * @param 新增日志分析参数
	 * @return
	 */
	int addLogKey(LogKeyValueVO log);
	/**
	 * @param 更新日志分析参数
	 * @return
	 */
	int updateLogKey(LogKeyValueVO log);
	/**
	 * @param 删除日志分析参数
	 * @return
	 */
	int deleteLogKey(Map<String, Object> paramsMap);
	/**
	 * @param 判断日志分析参数是否有相同key值
	 * @return
	 */
	int checkKey(LogKeyValueVO log);
}
