package com.visionvera.mq.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.visionvera.bean.cms.UserVO;

/**
 * RabbitMQ推送消息的异步推送
 * @author Bianjf
 *
 */
@Component
public class RabbitProviderAsync {
	@Autowired
	private RabbitProvider rabbitProvider;
	
	/**
	 * 异步： 向会管队列发送用户消息
	 * @param user 用户消息
	 * @param operationType 操作类型:1表示添加；2表示修改；3表示删除
	 */
	@Async
	public void sendHuiguanUserMessage(UserVO user, Integer operationType) {
		rabbitProvider.sendHuiguanUserMessage(user, operationType);
	}
	
	/**
	 * 异步：向队列中发送日志数据。全字段
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param createName 创建人，关联用户登录名
	 * @param result 操作结果
	 * @param platformId 平台ID
	 * @param errMsg 错误信息
	 * @param token 访问令牌
	 */
	@Async
	public void sendLogMessage(Integer logType, String description, String createName, Boolean result, String platformId, String errMsg, String token) { 
		rabbitProvider.sendLogMessage(logType, description, createName, result, platformId, errMsg, token);
	}
	
	/**
	 * 异步：向队列中发送日志数据。平台ID为空, 默认消费端会自动添加为当前登录用户的平台信息
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param createName 创建人，关联用户登录名
	 * @param result 操作结果
	 * @param errMsg 错误信息
	 * @param token 访问令牌。此时token为必传
	 */
	@Async
	public void sendLogMessage(Integer logType, String description, String createName, Boolean result, String errMsg, String token) {
		rabbitProvider.sendLogMessage(logType, description, createName, result, errMsg, token);
	}
	
	/**
	 * 异步：向队列中发送日志数据。平台ID为空, 默认消费端会自动添加为当前登录用户的登录名
	 * @param logType 操作类型
	 * @param description 描述信息
	 * @param result 操作结果
	 * @param errMsg 错误信息
	 * @param token 访问令牌。此时token为必传
	 */
	@Async
	public void sendLogMessage(Integer logType, String description, Boolean result, String errMsg, String token) {
		rabbitProvider.sendLogMessage(logType, description, result, errMsg, token);
	}
}
