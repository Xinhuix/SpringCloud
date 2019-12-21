package com.visionvera.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,//禁用SpringBoot自带的数据源自动配置
		HibernateJpaAutoConfiguration.class,//禁用Hibernate的JPA自动配置
		DataSourceTransactionManagerAutoConfiguration.class,//禁用SpringBoot的事务自动配置。
		JpaRepositoriesAutoConfiguration.class//禁用SpringBoot的JPA自动配置,避免At least one JPA metamodel must be present错误
})//禁用SpringBoot自带自动数据源的配置
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.visionvera.feign"})//开启Feign支持
@EnableDiscoveryClient//开启Eureka注册发现服务
@ComponentScan(basePackages = {"com.visionvera"})//扫描其他包的注解
@EnableAsync(proxyTargetClass = true)//开启异步调用,使用CGLIB代理
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class AuthenticationApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthenticationApplication.class, args);
	}
}
