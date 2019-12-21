package com.visionvera.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class, 
		HibernateJpaAutoConfiguration.class, 
		DataSourceTransactionManagerAutoConfiguration.class
})//禁用SpringBoot自带自动数据源的配置
@SpringBootApplication
@EnableDiscoveryClient//开启Eureka注册发现服务
@EnableFeignClients(basePackages = {"com.visionvera.feign"})//开启Feign支持
@ComponentScan(basePackages = {"com.visionvera"})//扫描其他包的注解
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)//开启AOP方法内部拦截
@EnableAsync(proxyTargetClass = true)//开启异步调用,使用CGLIB代理
public class OperationApplication {
	public static void main(String[] args) {
		SpringApplication.run(OperationApplication.class, args);
	}
}
