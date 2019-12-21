package com.visionvera.mq.provider;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.alarm.AlarmDomain;
import com.visionvera.constrant.CommonConstrant;

@Component
public class RabbitProvider {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	

	
	/**
	 * 向队列中发送告警数据。
	 * @param AlarmDomain 告警实体信息
	 */
	public Map<String,Object> sendAlarmMessage(AlarmDomain alarmDomain) {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			String alarm = JSONObject.toJSONString(alarmDomain);
			logger.info("发送告警数据："+alarm);
			this.rabbitTemplate.convertAndSend(CommonConstrant.RABBIT_ALARM_EXCHANGE_NAME, CommonConstrant.RABBIT_ALARM_EXCHANGE_QUEUE_ROUTINGKEY_NAME,alarm);
			result.put("errcode", 0);
			result.put("errcode", "发送告警数据成功");
		} catch (Exception e) {//不处理异常
			this.logger.error("RabbitProvider ===== sendAlarmMessage ===== 向队列中发送告警数据失败 =====> ", e);
			result.put("retcode", 1);
			result.put("retmsg", "发送告警数据异常");
		}
		return result;
	}
	
}
