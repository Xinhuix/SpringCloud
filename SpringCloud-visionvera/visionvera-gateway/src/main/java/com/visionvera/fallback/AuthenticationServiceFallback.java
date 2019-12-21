package com.visionvera.fallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.constrant.CommonConstrant;

/**
 * 认证/用户中心断路器
 *
 */
@Component
public class AuthenticationServiceFallback implements ZuulFallbackProvider {

	@Override
	public String getRoute() {
		return CommonConstrant.AUTHENTICATION_SERVICE_NAME;//用户认证服务
	}

	@Override
	public ClientHttpResponse fallbackResponse() {
		return new ClientHttpResponse() {
			
			/**
			 * 设置响应头
			 */
			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				MediaType mediaType = new MediaType("application", "JSON", Charset.forName("UTF-8"));
				headers.setContentType(mediaType);
				return headers;
			}
			
			/**
			 * 设置响应体
			 */
			@Override
			public InputStream getBody() throws IOException {
				JSONObject resultJson = new JSONObject();
				resultJson.put("errcode", 1);
				resultJson.put("errmsg", "用户认证服务不可用, 请稍后再试");
				return new ByteArrayInputStream(resultJson.toJSONString().getBytes());
			}
			
			@Override
			public String getStatusText() throws IOException {
				return this.getStatusCode().getReasonPhrase();
			}
			
			/**
			 * 设置响应状态码
			 */
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.OK;
			}
			
			@Override
			public int getRawStatusCode() throws IOException {
				return this.getStatusCode().value();
			}
			
			@Override
			public void close() {
				
			}
		};
	}

}
