package com.visionvera.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.AlarmInfoVO;
import com.visionvera.bean.datacore.Hospital;
import com.visionvera.dao.datacore.BusinessConfigDao;
import com.visionvera.service.BusinessService;


/**
 *  Class Name: BusinessServiceImpl.java
 *  Description: 告警推送
 *  @author ==zyf==
 *  @date 2019年11月13日 下午5:25:59  
 *  @version 
 */
@Service
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class BusinessServiceImpl implements BusinessService {
	
	@Autowired
	private BusinessConfigDao businessConfigDao;

	/**
	 *  Description:获取推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午5:54:38 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getBusinessConfig(Map<String, Object> paramMap) {
	        return businessConfigDao.getBusinessConfig(paramMap);
	}

	/**
	 *  Description:获取推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午8:49:07 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getPushUserConfig(Map<String, Object> paramMap) {
		return businessConfigDao.getPushUserConfig(paramMap);
	}

	/**
	 *  Description:删除配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:14:03 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public int deleteBusinessConfig(Map<String, Object> paramMap) {
		return businessConfigDao.deleteBusinessConfig(paramMap);
	}

	/**
	 *  Description:获取预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:29:07 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getDefaultUser(Map<String, Object> paramMap) {
		return businessConfigDao.getDefaultUser(paramMap);
	}

	/**
	 *  Description:更新预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:45:36 
	 *  @param list
	 *  @return
	 */
	@Override
	public int updateDefaultUser(List<Map<String, Object>> list) {
		//删除预设用户
		businessConfigDao.deleteDefaultUser();
		return businessConfigDao.updateDefaultUser(list);
	}

	/**
	 *  Description:获取告警等级树结构
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午11:18:17 
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getLevelRoot() {
		return businessConfigDao.getLevelRoot();
	}

	/**
	 *  Description:添加告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午2:18:37 
	 *  @param list
	 *  @return
	 */
	@Override
	public int addAlarmUserConfig(List<AlarmInfoVO> list) {
		return businessConfigDao.addAlarmUserConfig(list);
	}

	/**
	 *  Description:获取告警类别
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:29:52 
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getTypeRoot() {
		return businessConfigDao.getTypeRoot();
	}

	/**
	 *  Description:获取告警类型
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:38:07 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getSubTypeRoot(Map<String, Object> paramMap) {
		return businessConfigDao.getSubTypeRoot(paramMap);
	}

	/**
	 *  Description:获取拥有视联汇--告警推送权限的用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午4:39:27 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public List<Map<String, Object>> getUser(Map<String, Object> paramMap) {
		return businessConfigDao.getUser(paramMap);
	}

	/**
	 *  Description:根据告警等级、类型更新告警推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:26:12 
	 *  @param list
	 *  @return
	 */
	@Override
	public int updatePushUserConfig(List<AlarmInfoVO> list) {
		return businessConfigDao.updatePushUserConfig(list);
	}

	/**
	 *  Description:根据告警等级、类型删除告警推送信息
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:52:20 
	 *  @param paramMap
	 *  @return
	 */
	@Override
	public int deletePushUserConfig(Map<String, Object> paramMap) {
		return businessConfigDao.deletePushUserConfig(paramMap);
	}

	
}
