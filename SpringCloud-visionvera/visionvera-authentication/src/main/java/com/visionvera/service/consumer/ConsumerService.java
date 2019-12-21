package com.visionvera.service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.constrant.CommonConstrant;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.UserService;

/**
 * 消费消息业务
 *
 */
@Component

public class ConsumerService {
	private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerService.class);
	
	@Autowired
	private UserService userService;
	
	public void process(JSONObject messageJson) {
		Integer operationType = messageJson.getInteger("operationType");//业务操作类型
		
		if (operationType.equals(CommonConstrant.RABBIT_USER_ADD_OPERATION_TYPE)) {//添加用户
			UserVO user = this.parseJsonToUser(messageJson);//将数据转换成用户实体类
			this.userService.addOrEditUserForHuiguan(user, true);
		} else if (operationType.equals(CommonConstrant.RABBIT_USER_EDIT_OPERATION_TYPE)) {//修改用户
			UserVO user = this.parseJsonToUser(messageJson);//将数据转换成用户实体类
			this.userService.addOrEditUserForHuiguan(user, false);
		} else if (operationType.equals(CommonConstrant.RABBIT_USER_DEL_OPERATION_TYPE)) {//删除用户
			UserVO user = this.parseJsonToUser(messageJson);//将数据转换成用户实体类
			this.userService.delUserBatch(user.getUuid(), null, operationType + "");
		} else {
			LOGGER.error("平台类型错误 ===== 平台类型:{}" + operationType);
		}
	}
	
	/**
	 * 将JSON字符数据转换成用户数据
	 * @param userJson
	 * @return
	 */
	private UserVO parseJsonToUser(JSONObject userJson) {
		UserVO user = new UserVO();
		user = JSONObject.parseObject(userJson.getString("user"), UserVO.class);
		
		if (user == null) {
			throw new BusinessException("用户信息为空");
		}
		
		return user;
	}
}
