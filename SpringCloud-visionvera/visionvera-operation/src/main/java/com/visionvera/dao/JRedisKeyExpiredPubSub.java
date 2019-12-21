package com.visionvera.dao;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.ywcore.PlatformDao;
import com.visionvera.util.ProbeManagerMsgUtil;
import com.visionvera.util.StringUtil;
import com.visionvera.websocket.WebSocketPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPubSub;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Set;

/**
 * ClassName: JRedisKeyExpiredPubSub
 * @author Administrator
 * @Description:  Redis Key过期发布者
 * 使用该功能时，需要在redis.conf中配置，设置" notify-keyspace-events = EX "， 该功能在redis2.8版本之后开始支持
 */
@Component
public class JRedisKeyExpiredPubSub extends JedisPubSub {

	private static final Logger logger = LoggerFactory.getLogger(JRedisKeyExpiredPubSub.class);
	@Autowired
	private JRedisDao jRedisDao;
	@Autowired
	private PlatformDao platformDao;
	/**
	 * 初始化按表达式的方式订阅时候的处理
	 */
	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.info("onPSubscribe ---- pattern ----- " + pattern + " ---- subscribedChannels ----- " + subscribedChannels);
	}

	/**
	 * 取得按表达式的方式订阅的消息后的处理
	 */
	@Override
	public void onPMessage(String pattern, String channel, String message) {
		logger.info("onPMessage ---- pattern ---- " + pattern + " -----  channel ---- " + channel + " ---- message ---- " + message);

		//如果key即不是以规定开头的 ,那么，则不再进行下面的操作，因为没有意义
		if (message == null || !message.startsWith(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX)) {
			return;
		}

		/*if (jRedisDao.get(message) == null) {
			return;
		}*/
		
		JSONObject resultJson = new JSONObject();
		resultJson.put("errcode",1);
		resultJson.put("msg","请求超时");
		String funcName = message.split(":")[1];
		resultJson.put("func",funcName);

		if(message.contains(":sub")) {
			message = message.replace(":sub", "");
			//服务器升级超时需要返回服务器唯一标识
			if(message.contains(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "upGrade:")) {
				String server = message.split(":")[2];
				resultJson.put("server",server);
			}
			Set<Object> set = jRedisDao.getSet(message);
			for (Object o : set) {
				try {
					WebSocketPushMessage.sendMessage(WebSocketPushMessage.getWebSocketMap().get(o),resultJson.toJSONString());
				} catch (IOException e) {
					logger.error("JRedisKeyExpiredPubSub===onPMessage===订阅消息表达式处理异常",e);
				}
			}
			jRedisDao.del(message);
			jRedisDao.delSet(message);
		} else {
			String[] redisStrArr = message.split(":");
			try {
				if(StringUtil.isNotNull(redisStrArr[2])){
					resultJson.put("server",redisStrArr[2]);
				}
				if(StringUtil.isNotNull(redisStrArr[3])) {
					if(ProbeManagerMsgUtil.DEL_PLATFORM.equals(funcName)) {
						PlatformVO platform = platformDao.getPlatformByTposRegisterid(redisStrArr[3]);
						if(platform == null) {
							return;
						}
					}
					resultJson.put("registerId",redisStrArr[3]);
				}
				if(StringUtil.isNotNull(redisStrArr[4])) {
					resultJson.put("processName",redisStrArr[4]);
				}
				
			} catch (Exception e) {
				logger.error("JRedisKeyExpiredPubSub===onPMessage===订阅消息表达式处理异常",e);
			}
			try {
				logger.info("redisStrArr[5]==="+redisStrArr[5]);
			    Session session = WebSocketPushMessage.getWebSocketMap().get(redisStrArr[5]);
			    logger.info("session==="+session);
			    if(session != null) {
                    WebSocketPushMessage.sendMessage(session,resultJson.toJSONString());
                }
			} catch(Exception e) {
				logger.error("JRedisKeyExpiredPubSub===onPMessage===订阅消息表达式处理异常",e);
			}
		}
	}
}
