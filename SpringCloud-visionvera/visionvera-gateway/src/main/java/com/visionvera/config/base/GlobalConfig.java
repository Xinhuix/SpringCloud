package com.visionvera.config.base;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.visionvera.filter.LoginFilter;
import com.visionvera.filter.WrapperFilter;

@Configuration
public class GlobalConfig {

	/**
	 * 配置登陆过滤器
	 * 
	 * @return
	 */
	@Bean
	public LoginFilter getLoginFilter() {
		return new LoginFilter();
	}

	/**
	 * 配置跨域请求的Filter
	 * 
	 * @return
	 */
	@Bean
	public FilterRegistrationBean getCorsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();//URL源
		CorsConfiguration config = new CorsConfiguration();//Cors配置
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		
		config.addAllowedMethod(HttpMethod.GET);//允许的请求
		config.addAllowedMethod(HttpMethod.POST);//允许的请求
		config.addAllowedMethod(HttpMethod.HEAD);//允许的请求
		config.addAllowedMethod(HttpMethod.OPTIONS);//允许的请求
		config.addAllowedMethod(HttpMethod.PUT);//允许的请求
		config.addAllowedMethod(HttpMethod.DELETE);//允许的请求

		config.addAllowedHeader("Content-Type");//允许的头
		config.addAllowedHeader("X-Requested-With");//允许的头
		config.addAllowedHeader("accept");//允许的头
		config.addAllowedHeader("Origin");//允许的头
		config.addAllowedHeader("Access-Control-Request-Method");//允许的头
		config.addAllowedHeader("Access-Control-Request-Headers");//允许的头
		
		config.addExposedHeader("Access-Control-Allow-Origin");//响应头
		config.addExposedHeader("Access-Control-Allow-Credentials");//响应头
		config.setMaxAge(10L);
		source.registerCorsConfiguration("/**", config); // CORS 配置对所有接口都有效
		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
	}
	
	@Bean
	public FilterRegistrationBean getSaveFilter() {
		FilterRegistrationBean bean = new FilterRegistrationBean(new WrapperFilter());
		bean.setOrder(1);
		return bean;
	}
}
