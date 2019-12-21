package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.BandWidthVO;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceLogVO;
import com.visionvera.bean.cms.LogKeyValueVO;
import com.visionvera.bean.cms.ServerTypeVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.SmsVO;

/**
 * 系统配置业务层接口
 *
 */
public interface SysConfigService {
	
	
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
	 * @Title: getServerType 
	 * @Description: 获取服务器类型列表
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	List<ServerTypeVO> getServerType();
	
	/** <pre>getShold(获取阈值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月16日 下午4:47:52    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月16日 下午4:47:52    
	 * 修改备注： 
	 * @param list2
	 * @return</pre>    
	 */
	public List<ConstDataVO> getShold(List<Integer> list);
	
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
	 * @Title: updateAprTime 
	 * @Description: 修改当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return     返回类型 
	 * @throws
	 */
	public void updateAprTime(List<ConstDataVO> list);
	
	/**
	 * 根据类型获取服务器配置信息
	 * @param serverType。 类型.1会管中心服务 2网管服务 3调度服务 4消息转发服务器
	 * @return
	 */
	public ServerVO getServerByType(String serverType);
	
	/**
	 * 更新审批流程配置数据
	 * @param list
	 * @return
	 */
	void updateApr(List<ConstDataVO> list);
	
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
	 * @param sms 
	 * 
	 * @Title: updateSms 
	 * @Description: 更新短信配置
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	int updateSms(SmsVO sms);
	
	List<ConstDataVO> getApproval();
	
	/**
	 * 
	 * @Title: getAprTime 
	 * @Description: 获取当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return List    返回类型 
	 * @throws
	 */
	List<ConstDataVO> getAprTime(ConstDataVO dataVO);
	
	/**
	 * 
	 * @Title: getPerServer 
	 * @Description: 获取视联汇配置
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	public List<ServerVO> getPerServer();
	
	/**
	 * 获取各省份的带宽信息
	 * @param bandWidth 查询条件
	 * @return
	 */
	public List<BandWidthVO> getBandwidth(BandWidthVO bandWidth);
	
	/**
	 * 批量更新国干带宽
	 * @param bandWidthList
	 * @return
	 */
	public ReturnData updateBandwidthBatch(List<BandWidthVO> bandWidthList);
	
	/**
	 * 根据类型获取相关数据
	 * @param type 类别。11表示智能审批
	 * @return
	 */
	public String getConstDataByType(String type);
	
	/**
	 * @param 获取终端日志列表
	 * @return
	 */
	List<DeviceLogVO> getDevLog(Map<String, Object> paramsMap);
	/**
	 * @param 获取终端日志列表总条数
	 * @return
	 */
	int getDevLogCount(Map<String, Object> paramsMap);
	/**
	 * @param 获取终端日志分析参数列表
	 * @return
	 */
	List<LogKeyValueVO> getDeviceLogKeyList(Map<String, Object> paramsMap);
	/**
	 * @param 获取终端日志分析参数总条数
	 * @return
	 */
	int getDeviceLogKeyListCount(Map<String, Object> paramsMap);
	/**
	 * @param 新增终端日志分析参数
	 * @return
	 */
	int addLogKey(LogKeyValueVO log);
	/**
	 * @param 更新终端日志分析参数
	 * @return
	 */
	int updateLogKey(LogKeyValueVO log);
	/**
	 * @param 删除终端日志分析参数
	 * @return
	 */
	int deleteLogKey(Map<String, Object> paramsMap);
	/**
	 * @param 判断日志分析参数是否有相同key值
	 * @return
	 */
	int checkKey(LogKeyValueVO log);
	
	void updateSes(ConstDataVO dataVO);

}
