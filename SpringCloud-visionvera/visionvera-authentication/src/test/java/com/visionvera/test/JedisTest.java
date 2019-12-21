package com.visionvera.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.visionvera.application.AuthenticationApplication;
import com.visionvera.dao.JRedisDao;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthenticationApplication.class)
public class JedisTest {
	@Autowired
	private JRedisDao jedisDao;
	
	@Test
	public void getTest() {
		String value = this.jedisDao.get("bian");
		System.out.println("结果：" + value);
	}
	
	@Test
	public void setTest() {
		this.jedisDao.del("bian");
	}
}
