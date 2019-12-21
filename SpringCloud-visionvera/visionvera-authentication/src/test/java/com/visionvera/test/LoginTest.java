package com.visionvera.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visionvera.mq.consumer.BusinessConsumer;
import com.visionvera.mq.provider.RabbitProviderAsync;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.visionvera.application.AuthenticationApplication;
import com.visionvera.bean.cms.SmsVO;
import com.visionvera.bean.cms.restful.SmsSend;
import com.visionvera.bean.datacore.TRoleVO;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.authentication.RoleDao;
import com.visionvera.dao.authentication.SmsDao;
import com.visionvera.util.SmsAgentUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthenticationApplication.class)
public class LoginTest {
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private SmsDao smsDao;

	@Autowired
	private RabbitProviderAsync rabbitProvider;

	@Test
	public void getRoleInfoTest() {
		
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("userId", "01a41216317211e7ba4ba4bf0101a16e");
		paramsMap.put("platformType", 2);
		
		List<TRoleVO> roleList = this.roleDao.selectRoleForLogin(paramsMap);
		if (roleList != null && roleList.size() > 0) {
			
		}
	}
	
	@Test
	public void sendPhoneMessageTest() {
		SmsSend sms = new SmsSend();
		
		if(StringUtils.isBlank(sms.getSignature())){
			sms.setSignature(WsConstants.SMS_SIGNATURE);
		}else if(sms.getSignature().indexOf("】") < 0){
			sms.setSignature("【" + sms.getSignature() + "】");
		}
		
		SmsAgentUtil.post("18610651187", sms.getSignature() + "短信发送测试");
	}

	@Autowired
	BusinessConsumer businessConsumer;
	@Test
	public void rabbitMQTest(){
		//Integer logType, String description, String createName,
		// Boolean result, String platformId, String errMsg, String token
		rabbitProvider.sendLogMessage(1,"测试","测试名称",true,"123","d","2");
	}


}
