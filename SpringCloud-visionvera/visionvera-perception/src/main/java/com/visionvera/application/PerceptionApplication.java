package com.visionvera.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * @author Administrator
 */
@EnableAutoConfiguration(exclude = {
		HibernateJpaAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class
})//禁用SpringBoot自带自动数据源的配置
@SpringBootApplication
@EnableDiscoveryClient//开启Eureka注册发现服务
@EnableFeignClients(basePackages = {"com.visionvera.feign"})//开启Feign调用
@ComponentScan(basePackages = {"com.visionvera"})//扫描其他包的注解
//开启AOP方法内部拦截
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class PerceptionApplication {
	private static Logger log = LoggerFactory.getLogger(PerceptionApplication.class);
	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(PerceptionApplication.class);
		Environment env = app.run(args).getEnvironment();
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			log.warn("无法使用`localhost`作为访问路径");
		}
		log.info("\n----------------------------------------------------------\n\t" +
						"应用 '{}'正在运行! 访问路径为:\n\t" +
						"本地: \t\t{}://localhost:{}\n\t" +
						"外部: \t{}://{}:{}\n\t" +
						"环境: \t{}\n----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				protocol,
				env.getProperty("server.port"),
				protocol,
				hostAddress,
				env.getProperty("server.port"),
				env.getActiveProfiles());
	}
}
