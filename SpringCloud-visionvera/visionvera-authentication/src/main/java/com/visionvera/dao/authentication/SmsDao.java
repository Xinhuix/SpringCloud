package com.visionvera.dao.authentication;

import com.visionvera.bean.cms.SmsVO;

public interface SmsDao {

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
}
