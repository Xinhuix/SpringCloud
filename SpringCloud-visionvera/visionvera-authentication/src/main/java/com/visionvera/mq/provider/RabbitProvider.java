package com.visionvera.mq.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.TLog;
import com.visionvera.constrant.CommonConstrant;

@Component
public class RabbitProvider {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	/**
	 * 向会管队列发送用户消息
	 * @param user 用户消息
	 * @param operationType 操作类型:1表示添加；2表示修改；3表示删除
	 */
	public void sendHuiguanUserMessage(UserVO user, Integer operationType) {
		JSONObject userMessageJson = new JSONObject();
		try {
			if (operationType == null || operationType.equals(0)) {//验证操作类型
				this.LOGGER.error("RabbitProvider ===== sendHuiguanUserMessage ===== 向队列中发送用户数据失败, 操作类型为空");
				return;
			}
			user.setRole(user.getRoleIds());
			userMessageJson.put("user", user);
			userMessageJson.put("operationType", operationType);
			this.rabbitTemplate.convertAndSend(CommonConstrant.RABBITMQ_BUSINESS_EXCHANGE_NAME, CommonConstrant.RABBIT_USER_EXCHANGE_HUIGUAN_QUEUE_BINDING_NAME, 
					userMessageJson.toJSONString());
		} catch (Exception e) {
			this.LOGGER.error("RabbitProvider ===== sendHuiguanUserMessage ===== 向队列中发送用户数据失败, 操作类型:{} + " + operationType + " =====> ", e);
		}
	}
	
	/**
	 * 向队列中发送日志数据。全字段
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param createName 创建人，关联用户登录名
	 * @param result 操作结果
	 * @param platformId 平台ID
	 * @param errMsg 错误信息
	 * @param token 访问令牌
	 */
	public void sendLogMessage(Integer logType, String description, String createName, Boolean result, String platformId, String errMsg, String token) {
		TLog log = new TLog();
		try {
			log = this.getLog(logType, description, createName, result, platformId, errMsg, token);
			this.rabbitTemplate.convertAndSend(CommonConstrant.RABBITMQ_LOG_EXCHANGE_NAME, "logRoute", JSONObject.toJSONString(log));
		} catch (Exception e) {//不处理异常
			this.LOGGER.error("RabbitProvider ===== sendLogMessage ===== 向队列中发送日志数据(全字段)失败 =====> ", e);
		}
	}
	
	/**
	 * 向队列中发送日志数据。平台ID为空, 默认消费端会自动添加为当前登录用户的平台信息
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param createName 创建人，关联用户登录名
	 * @param result 操作结果
	 * @param errMsg 错误信息
	 * @param token 访问令牌。此时token为必传
	 */
	public void sendLogMessage(Integer logType, String description, String createName, Boolean result, String errMsg, String token) {
		try {
			this.sendLogMessage(logType, description, createName, result, null, errMsg, token);
		} catch (Exception e) {//不处理异常
			this.LOGGER.error("RabbitProvider ===== sendLogMessage ===== 向队列中发送日志数据(平台ID)失败 =====> ", e);
		}
	}
	
	/**
	 * 向队列中发送日志数据。平台ID为空, 默认消费端会自动添加为当前登录用户的登录名
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param result 操作结果
	 * @param errMsg 错误信息
	 * @param token 访问令牌。此时token为必传
	 */
	public void sendLogMessage(Integer logType, String description, Boolean result, String errMsg, String token) {
		try {
			this.sendLogMessage(logType, description, null, result, null, errMsg, token);
		} catch (Exception e) {//不处理异常
			this.LOGGER.error("RabbitProvider ===== sendLogMessage ===== 向队列中发送日志数据(平台ID和创建人为空)失败 =====> ", e);
		}
	}
	
	
	/**
	 * 获取Log对象
	 * @param logType Log类型
	 * @param description 描述信息
	 * @param createName 创建人
	 * @param result 操作结果。
	 * @param platformId 平台ID
	 * @param errMsg 错误原因
	 * @param token 访问令牌
	 * @return
	 */
	private TLog getLog(Integer logType, String description, String createName, Boolean result, String platformId, String errMsg, String token) {
		TLog log = new TLog();
		log.setLogType(logType);//日志类型
		log.setDescription(description);//描述信息
		log.setCreateName(createName);//创建人，关联登录名
		log.setResult(result);//操作结果
		log.setPlatformId(platformId);//平台ID
		log.setErrMsg(errMsg);//错误信息
		log.setToken(token);//访问令牌
		return log;
	}
}
