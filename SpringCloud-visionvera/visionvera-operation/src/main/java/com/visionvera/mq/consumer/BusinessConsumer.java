package com.visionvera.mq.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.visionvera.constrant.CommonConstrant;
import com.visionvera.exception.BusinessException;
import com.visionvera.websocket.WebSocketPushMessage;

/**
 * 监听会管预约消息队列的消费者
 *
 */
@Component
public class BusinessConsumer {
	private final static Logger LOGGER = LoggerFactory.getLogger(BusinessConsumer.class);
	
	@RabbitHandler
	@RabbitListener(queues = {CommonConstrant.RABBITMQ_HUIGUAN_2_MICRO_QUEUE_NAME})
	public void onMessage(byte[] message) {
		try {
			if (message == null) {
				LOGGER.error("消息体为空");
				return;
			}
			String str= new String (message);
			String[] strs = str.split("_");//消息格式为“消息_用户ID”
			WebSocketPushMessage.sendToUser(strs[1], strs[0]);
		} catch (BusinessException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("消息处理异常!!!", e);
		}
	}
}
