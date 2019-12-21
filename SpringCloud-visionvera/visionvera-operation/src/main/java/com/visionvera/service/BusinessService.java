package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.AlarmInfoVO;

/**
 *  Class Name: BusinessService.java
 *  Description: 告警推送配置
 *  @author ==zyf==
 *  @date 2019年11月12日 下午5:34:08  
 *  @version 
 */
public interface BusinessService {

	/**
	 *  Description:获取推送配置
	 *  @author  ==zyf==
	 * @date 2019年11月12日 下午5:54:18  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getBusinessConfig(Map<String, Object> paramMap);

	/**
	 *  Description:获取推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午8:48:39  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getPushUserConfig(Map<String, Object> paramMap);

	/**
	 *  Description:删除配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:13:22  
	 *  @param paramMap
	 *  @return
	 */
	int deleteBusinessConfig(Map<String, Object> paramMap);

	/**
	 *  Description:获取预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:28:39  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getDefaultUser(Map<String, Object> paramMap);
	
	/**
	 *  Description:更新预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:45:15  
	 *  @param list
	 *  @return
	 */
	int updateDefaultUser(List<Map<String, Object>> list);

	/**
	 *  Description:获取告警等级树结构
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午11:17:50  
	 *  @return
	 */
	List<Map<String, Object>> getLevelRoot();

	/**
	 *  Description:添加告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午2:18:02  
	 *  @param list
	 *  @return
	 */
	int addAlarmUserConfig(List<AlarmInfoVO> list);

	/**
	 *  Description:获取告警类别
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:28:16  
	 *  @return
	 */
	List<Map<String, Object>> getTypeRoot();

	/**
	 *  Description:获取告警类型
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:37:46  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getSubTypeRoot(Map<String, Object> paramMap);

	/**
	 *  Description:获取拥有视联汇--告警推送权限的用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午4:39:07  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getUser(Map<String, Object> paramMap);

	/**
	 *  Description:根据告警等级和类型更新推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:25:15  
	 *  @param list
	 *  @return
	 */
	int updatePushUserConfig(List<AlarmInfoVO> list);

	/**
	 *  Description:根据告警等级、类型删除告警推送信息
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:51:25  
	 *  @param paramMap
	 *  @return
	 */
	int deletePushUserConfig(Map<String, Object> paramMap);
	
}
