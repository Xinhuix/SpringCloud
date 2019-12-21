package com.visionvera.util;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPostFileUtils {
	/**
	 * @param filePostUrl 请求接口URL
	 * @param params	  除文件外其它参数
	 * @param filesMap    Map<文件名,文件二进制byte[]>
	 * @return
	 */
	
	private static final Logger logger = LoggerFactory.getLogger(HttpPostFileUtils.class);
	
	public static String postFiles(String filePostUrl,
			Map<String, String> params, Map<String, byte[]> filesMap) {

		HttpPost httppost = new HttpPost(filePostUrl);
		HttpClient client = new DefaultHttpClient();
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		// 是否多个文件
		for (String key : filesMap.keySet()) {
			entityBuilder.addBinaryBody("file", filesMap.get(key),
					ContentType.DEFAULT_BINARY, key);
		}
		// 是否有表单参数
		if(params != null) {
			for (String key : params.keySet()) {
				entityBuilder.addTextBody(key, params.get(key),ContentType.TEXT_PLAIN.withCharset("UTF-8"));
			}
		}
		httppost.setEntity(entityBuilder.build());
		try {
			HttpResponse response = client.execute(httppost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(response.getEntity(), "UTF-8");
				return result;
			}
		} catch (Exception e) {
			logger.error("HttpPostFileUtils===postFiles===发送文件数据流异常",e);
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}
	
}
