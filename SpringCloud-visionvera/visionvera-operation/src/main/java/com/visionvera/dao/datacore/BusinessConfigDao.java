package com.visionvera.dao.datacore;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.AlarmInfoVO;

public interface BusinessConfigDao {

	/**
	 *  Description:获取告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午6:05:38  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getBusinessConfig(Map<String, Object> paramMap);

	/**
	 *  Description:获取推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午8:49:45  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getPushUserConfig(Map<String, Object> paramMap);

	/**
	 *  Description:删除配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:14:35  
	 *  @param paramMap
	 *  @return
	 */
	int deleteBusinessConfig(Map<String, Object> paramMap);

	/**
	 *  Description:获取预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:29:40  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getDefaultUser(Map<String, Object> paramMap);

	/**
	 *  Description:更新预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:46:14  
	 *  @param list
	 *  @return
	 */
	int updateDefaultUser(List<Map<String, Object>> list);

	/**
	 *  Description:删除预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午11:03:57  
	 *  @return
	 */
	int deleteDefaultUser();

	/**
	 *  Description:获取告警等级树结构
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午11:18:43  
	 *  @return
	 */
	List<Map<String, Object>> getLevelRoot();

	/**
	 *  Description:添加告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午2:26:07  
	 *  @param list
	 *  @return
	 */
	int addAlarmUserConfig(List<AlarmInfoVO> list);

	/**
	 *  Description:获取告警类别
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:30:45  
	 *  @return
	 */
	List<Map<String, Object>> getTypeRoot();

	/**
	 *  Description:获取告警类型
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午3:38:37  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getSubTypeRoot(Map<String, Object> paramMap);

	/**
	 *  Description:获取拥有视联汇--告警推送权限的用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午4:39:52  
	 *  @param paramMap
	 *  @return
	 */
	List<Map<String, Object>> getUser(Map<String, Object> paramMap);

	/**
	 *  Description:根据告警等级、类型更新告警推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:27:09  
	 *  @param list
	 *  @return
	 */
	int updatePushUserConfig(List<AlarmInfoVO> list);

	/**
	 *  Description:根据告警等级、类型删除告警推送信息
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午5:52:37  
	 *  @param paramMap
	 *  @return
	 */
	int deletePushUserConfig(Map<String, Object> paramMap);
	
}
