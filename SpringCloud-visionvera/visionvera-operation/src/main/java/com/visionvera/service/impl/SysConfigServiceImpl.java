package com.visionvera.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.BandWidthVO;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceLogVO;
import com.visionvera.bean.cms.LogKeyValueVO;
import com.visionvera.bean.cms.ServerTypeVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.config.SysConfig;
import com.visionvera.dao.operation.ConstDataDao;
import com.visionvera.dao.operation.RegionBandwidthDao;
import com.visionvera.dao.operation.SysConfigDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.SysConfigService;

/**
 * 系统配置业务层接口实现类
 *
 */
@Service("sysConfigService")
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class SysConfigServiceImpl implements SysConfigService {
	
	private static final Logger logger = LogManager.getLogger(SysConfigServiceImpl.class);
	@Autowired
	private SysConfigDao sysConfigDao;
	
	@Autowired
	private SysConfig sysConfig;
	
	@Autowired
	private RegionBandwidthDao regionBandwidthDao;
	
	@Autowired
	private ConstDataDao constDataDao;
	
	
	/**
	 * 
	 * @Title: getServer 
	 * @Description: 获取服务配置
	 * @param @return  参数说明 
	 * @return ServerVO    返回类型 
	 * @throws
	 */
	public ServerVO getServer(Map<String, Object> paramsMap) {
		return sysConfigDao.getServer(paramsMap);
	}
	
	/** <pre>getShold(获取阈值)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年5月16日 下午4:47:52    
	 * 修改人：周逸芳        
	 * 修改时间：2018年5月16日 下午4:47:52    
	 * 修改备注： 
	 * @param list2
	 * @return</pre>    
	 */
	@Override
	public List<ConstDataVO> getShold(List<Integer> list) {
		return this.sysConfigDao.getShold(list);
	}
	
	/**
	 * 
	 * @Title: getConfigUpdate 
	 * @Description: 获取可上传文件升级配置人员
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	public List<ConstDataVO> getConfigUpdate() {
		return sysConfigDao.getConfigUpdate();
	}
	
	/**
	 * 
	 * @Title: updateConfigPerson 
	 * @Description: 修改可上传文件升级配置人员
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	public void updateConfigPerson(List<ConstDataVO> list) {
		try {
			//先删除所有人员再新增
			sysConfigDao.deleteConfigData();
			sysConfigDao.updateConfigPerson(list);
		} catch (Exception e) {
			logger.error("修改可上传文件升级配置人员", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		
	}

	
	/**
	 * 
	 * @Title: updateAprTime 
	 * @Description: 修改当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return     返回类型 
	 * @throws
	 */
	public void updateAprTime(List<ConstDataVO> list) {
		this.sysConfigDao.updateAprTime(list);
	}
	
	/**
	 * 根据类型获取服务器配置信息
	 * @param serverType。 类型.1会管中心服务 2网管服务 3调度服务 4消息转发服务器
	 * @return
	 */
	@Override
	public ServerVO getServerByType(String serverType) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("type", serverType);
		return this.sysConfigDao.getServer(paramsMap);
	}
	
	public void updateApr(List<ConstDataVO> list) {
		try {
			//先删除旧数据
			sysConfigDao.deleteData();
			//插入新数据
			sysConfigDao.updateApr(list);
		} catch (Exception e) {
			logger.error("修改审批人配置失败", e);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
	}
	
	public List<ConstDataVO> getApproval() {
		return sysConfigDao.getApproval();
	}
	
	/**
	 * 
	 * @Title: getSms 
	 * @Description: 获取服务配置
	 * @param @return  参数说明 
	 * @return SmsVO    返回类型 
	 * @throws
	 */
	public SmsVO getSms() {
		return sysConfigDao.getSms();
	}
	
	/**
	 * 
	 * @Title: updateSms 
	 * @Description: 更新短信配置
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int updateSms(SmsVO sms) {
		return sysConfigDao.updateSms(sms);
	}
	
	/**
	 * 
	 * @Title: getServerType 
	 * @Description: 获取服务器类型列表
	 * @param @return  参数说明 
	 * @return list    返回类型 
	 * @throws
	 */
	public List<ServerTypeVO> getServerType() {
		return sysConfigDao.getServerType();
	}

	/**
	 * 
	 * @Title: updateServer 
	 * @Description: 更新服务配置
	 * @param @return  参数说明 
	 * @return int    返回类型 
	 * @throws
	 */
	public int updateServer(ServerVO server) {
		return sysConfigDao.updateServer(server);
	}
	
	/**
	 * 
	 * @Title: getAprTime 
	 * @Description: 获取当前审批超时时间
	 * @param @return  参数说明 dataVO
	 * @return String    返回类型 
	 * @throws
	 */
	public List<ConstDataVO> getAprTime(ConstDataVO dataVO) {
		return sysConfigDao.getAprTime(dataVO);
	}
	
	/**
	 * 
	 * @Title: getPerServer 
	 * @Description: 获取视联汇配置
	 * @param @return  参数说明 ConstDataVO
	 * @return     返回类型 
	 * @throws
	 */
	@Override
	public List<ServerVO> getPerServer() {
		List<ServerVO> serverList = this.sysConfigDao.getPerServer();
		ServerVO huiguanServer = new ServerVO();//手动给视联汇返回会管的IP和端口，视联汇配置WebSocket使用
		huiguanServer.setIp(this.sysConfig.getHgIp());
		huiguanServer.setPort(this.sysConfig.getHgPort());
		huiguanServer.setType(9999);//配置文件中会管的IP和端口的类型
		serverList.add(huiguanServer);
		return serverList;
	}
	
	/**
	 * 获取各省份的带宽信息
	 * @param bandWidth 查询条件
	 * @return
	 */
	@Override
	public List<BandWidthVO> getBandwidth(BandWidthVO bandWidth) {
		return this.regionBandwidthDao.selectBandWidthByCondition(bandWidth);
	}
	
	/**
	 * 批量更新国干带宽
	 * @param bandWidthList
	 * @return
	 */
	@Override
	public ReturnData updateBandwidthBatch(List<BandWidthVO> bandWidthList) {
		BaseReturn returnData = new BaseReturn();
		int count = this.regionBandwidthDao.updateBandwidthBatch(bandWidthList);
		if (count <= 0) {
			return returnData.returnError("更新失败");
		}
		return returnData.returnResult(0, "更新成功");
	}
	
	/**
	 * 根据类型获取相关数据
	 * @param type 类别。11表示智能审批
	 * @return
	 */
	@Override
	public String getConstDataByType(String type) {
		ConstDataVO constData = new ConstDataVO();
		constData.setValueType(Integer.valueOf(type));
		List<ConstDataVO> constDataList = this.constDataDao.getConstData(constData);//通过type查询常量数据的类型
		if (constDataList != null && constDataList.size() > 1) {
			throw new BusinessException("该常量值存在多个,请联系管理员");
		}
		
		return constDataList.get(0).getValue();
	}
	

	/**
	 * @param 获取终端日志列表
	 * @return
	 */
	@Override
	public List<DeviceLogVO> getDevLog(Map<String, Object> map) {
		if (map.get("pageSize") == null
				|| Integer.parseInt(map.get("pageSize").toString()) == -1) {
			return sysConfigDao.getDevLog(map, new RowBounds());
		}
		return sysConfigDao.getDevLog(
				map,
				new RowBounds((Integer) map.get("pageNum"), (Integer) map
						.get("pageSize")));
	}
	/**
	 * @param 获取终端日志列表总条数
	 * @return
	 */
	@Override
	public int getDevLogCount(Map<String, Object> paramsMap) {
		return sysConfigDao.getDevLogCount(paramsMap);
	}

	@Override
	public List<LogKeyValueVO> getDeviceLogKeyList(Map<String, Object> map) {
		if (map.get("pageSize") == null
				|| Integer.parseInt(map.get("pageSize").toString()) == -1) {
			return sysConfigDao.getDeviceLogKeyList(map, new RowBounds());
		}
		return sysConfigDao.getDeviceLogKeyList(
				map,
				new RowBounds((Integer) map.get("pageNum"), (Integer) map
						.get("pageSize")));
	}

	@Override
	public int getDeviceLogKeyListCount(Map<String, Object> paramsMap) {
		return sysConfigDao.getDeviceLogKeyListCount(paramsMap);
	}

	@Override
	public int addLogKey(LogKeyValueVO log) {
		return sysConfigDao.addLogKey(log);
	}

	@Override
	public int updateLogKey(LogKeyValueVO log) {
		return sysConfigDao.updateLogKey(log);
	}

	@Override
	public int deleteLogKey(Map<String, Object> paramsMap) {
		return sysConfigDao.deleteLogKey(paramsMap);
	}

	@Override
	public int checkKey(LogKeyValueVO log) {
		return  sysConfigDao.checkKey(log);
	}
	
	@Override
	public void updateSes(ConstDataVO dataVO) {
		sysConfigDao.updateSes(dataVO);
	}
}
