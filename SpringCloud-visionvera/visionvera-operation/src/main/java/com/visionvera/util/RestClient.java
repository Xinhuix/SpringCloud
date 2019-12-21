package com.visionvera.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visionvera.constrant.WsConstants;

public class RestClient {
	
	private static final Logger logger = LogManager.getLogger(RestClient.class);
	/**
	 * 消息转发服务器-可视电话拨打、挂断
	 * @author 谢程算
	 * @date 2017年9月13日  
	 * @version 1.0.0 
	 * @param url
	 * @param token
	 * @param args
	 */
	public static Map<String, Object> get(String url, Map<String, Object> args) {
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			URL restServiceURL = new URL(linkUrl(url, null, args));
			HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL
					.openConnection();
			/*httpConnection.setRequestMethod(WsConstants.METHOD_GET);
			httpConnection.setRequestProperty(WsConstants.PROP_CONTENT_TYPE, WsConstants.PROP_CONTENT_JSON);
			httpConnection.setRequestProperty(WsConstants.PROP_ACCEPT, WsConstants.PROP_ACCEPT_JSON);*/
			
			if (httpConnection.getResponseCode() != 200) {
				resultMap.put("errmsg", "访问消息转发服务器失败，错误码：" + httpConnection.getResponseCode());
				logger.error("访问消息转发服务器失败: errorcode " + httpConnection.getResponseCode());
				return resultMap;
			}
			
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream(),"utf-8"));
			
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				result += output; 
			}
			httpConnection.disconnect();
		} catch (MalformedURLException e) {
			resultMap.put("errmsg", "访问消息转发服务器失败，系统内部异常");
			logger.error("访问消息转发服务器失败: ", e);
			return resultMap;
		} catch (IOException e) {
			resultMap.put("errmsg", "访问消息转发服务器失败，系统内部异常");
			logger.error("访问消息转发服务器失败: ", e);
			return resultMap;
		}
		Map<String, Object> map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(result, Map.class);
		}  catch (Exception e) {
			resultMap.put("errmsg", "json转map失败，系统内部异常");
			logger.error("json转map失败: ", e);
		}
		return map;
	}
 
	/**
	 * 
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param url
	 * @param token
	 * @param args
	 */
	public static Map<String, Object> get(String url, String token, Map<String, Object> args) {
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			URL restServiceURL = new URL(linkUrl(url, token, args)); 
			HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL
					.openConnection();
			httpConnection.setConnectTimeout(6000);//超时时间（毫秒）
			httpConnection.setReadTimeout(6000);//读取超时
			httpConnection.setRequestMethod(WsConstants.METHOD_GET);
			httpConnection.setRequestProperty(WsConstants.PROP_CONTENT_TYPE, WsConstants.PROP_CONTENT_JSON);
			httpConnection.setRequestProperty(WsConstants.PROP_ACCEPT, WsConstants.PROP_ACCEPT_JSON);

			if (httpConnection.getResponseCode() != 200) {
				resultMap.put("errmsg", "访问远端服务器失败，错误码：" + httpConnection.getResponseCode());
				logger.error("访问远端服务器失败: errorcode " + httpConnection.getResponseCode());
				return resultMap;
			}

			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream(),"utf-8"));

			String output;
			while ((output = responseBuffer.readLine()) != null) {
				result += output; 
			}
			httpConnection.disconnect();
//			logger.info("调用远端webservice方法：" + url + "；参数: " + args + "；返回值: " + result);
		} catch (MalformedURLException e) {
			resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		} catch (IOException e) {
			if(e instanceof ConnectException || e instanceof SocketTimeoutException){
				resultMap.put("errmsg", "访问远端服务器失败，连接超时");
			}else{
				resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			}
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		}
		return json2Map(result, resultMap);
	}
	
	/**
	 * 
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param <T>
	 * @param url
	 * @return
	 */
	public static <T> Map<String, Object> post(String url, String token, T args) {
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			URL targetUrl = new URL(linkUrl(url, token, null));
			HttpURLConnection httpConnection = (HttpURLConnection) targetUrl
					.openConnection();
			httpConnection.setConnectTimeout(6000);//连接超时时间（毫秒）
			httpConnection.setReadTimeout(6000);//读取超时
			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod(WsConstants.METHOD_POST);
			httpConnection.setRequestProperty(WsConstants.PROP_CONTENT_TYPE, WsConstants.PROP_CONTENT_JSON);
//			httpConnection.setRequestProperty(WsConstants.PROP_ACCEPT, WsConstants.PROP_ACCEPT_JSON);
			if(args != null){
				String json = JSONObject.toJSONString(args, SerializerFeature.WriteMapNullValue); //保留map中值为null的属性
				OutputStream outputStream = httpConnection.getOutputStream();
				outputStream.write(json.getBytes("utf-8"));
				outputStream.flush();
			}
			
			if (httpConnection.getResponseCode() != 200) {
				resultMap.put("errmsg", "访问远端服务器失败，错误码：" + httpConnection.getResponseCode());
				logger.error("访问远端服务器失败: errorcode " + httpConnection.getResponseCode());
				return resultMap;
			}
			
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream(),"utf-8"));
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				result += output; 
			}
			httpConnection.disconnect();
			logger.info("调用远端webservice方法：" + url + "；参数: " + args + "；返回值: " + result);
		} catch (MalformedURLException e) {
			resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		} catch (IOException e) {
			if(e instanceof ConnectException || e instanceof SocketTimeoutException){
				resultMap.put("errmsg", "访问远端服务器失败，连接超时");
			}else{
				resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			}
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		}
		return json2Map(result, resultMap);
	}

	/**
	 * 
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param url
	 * @return
	 */
	public static Map<String, Object> postEncode(String url, String token, Map<String, Object> args) {
		Set<String> keySet = null;
		if(args != null){
			keySet = args.keySet();
		}
		if(keySet != null){
			for(String key : keySet){
				if(args.get(key) == null || !(args.get(key) instanceof String)){
					continue;
				}
				try {
					args.put(key, URLEncoder.encode(args.get(key).toString(), "utf-8"));
				} catch (UnsupportedEncodingException e) {
					Map<String, Object> resultMap = new HashMap<String, Object>();
					resultMap.put(WsConstants.KEY_RESP_CODE, WsConstants.ERROR);
					resultMap.put(WsConstants.KEY_RESP_MSG, "url参数编码转换时错误");
					return resultMap;
				}
			}
		}
		return post(url, token, args);
	}
	
	/**
	 * 
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param url
	 * @return
	 */
	public static Map<String, Object> post(String url, Map<String, Object> args,String title) {
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String json = JSONObject.toJSONString(args);
			String urlParam=URLEncoder.encode(json,"utf-8");
			URL targetUrl = new URL(url+"?"+title+"="+urlParam);
			HttpURLConnection httpConnection = (HttpURLConnection) targetUrl
					.openConnection();
			httpConnection.setConnectTimeout(6000);//超时时间（毫秒）
			httpConnection.setReadTimeout(6000);//读取超时
			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod(WsConstants.METHOD_POST);
			httpConnection.setRequestProperty(WsConstants.PROP_CONTENT_TYPE, WsConstants.PROP_CONTENT_JSON);
			httpConnection.setRequestProperty(WsConstants.PROP_ACCEPT, WsConstants.PROP_ACCEPT_JSON);

			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(json.getBytes());
			outputStream.flush();
			
			if (httpConnection.getResponseCode() != 200) {
				resultMap.put("errmsg", "访问远端服务器失败，错误码：" + httpConnection.getResponseCode());
				logger.error("访问远端服务器失败: errorcode " + httpConnection.getResponseCode());
				return resultMap;
			}
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream(),"utf-8"));
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				result += output; 
			}
			httpConnection.disconnect();
			logger.info("调用远端webservice方法：" + url + "；参数: " + args + "；返回值: " + result);
		} catch (MalformedURLException e) {
			resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		} catch (IOException e) {
			if(e instanceof ConnectException || e instanceof SocketTimeoutException){
				resultMap.put("errmsg", "访问远端服务器失败，连接超时");
			}else{
				resultMap.put("errmsg", "访问远端服务器失败，系统内部异常");
			}
			logger.error("访问远端服务器失败: ", e);
			return resultMap;
		}
		return json2Map(result, resultMap);
	}

	
	
	/**in
	 * 
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param url
	 * @param token
	 * @param args
	 * @return
	 */
	private static String linkUrl(String url, String token, Map<String, Object> args){
		Set<String> keySet = null;
		if(args != null){
			keySet = args.keySet();
		}
		if(StringUtils.isNotBlank(token)){
			url += "?" + WsConstants.KEY_TOKEN + "=" + token;
		}
		if(keySet != null){
			for(String key : keySet){
				url += "&" + key + "=" + args.get(key);
			}
		}
		if(StringUtils.isBlank(token) && url.indexOf("?") < 0){
			url = url.replaceFirst("&", "?");
		}
		return url;
	}

	/**
	 * json字符串转换成map
	 * @author 谢程算
	 * @date 2017年2月8日  
	 * @version 1.0.0 
	 * @param json
	 * @param resultMap 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> json2Map(String json, Map<String, Object> resultMap){
		Map<String, Object> map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(json, Map.class);
			if(map.get("ret") != null){//网管服务器数据解析
				if(map.get("ret").equals(0)){
					return (Map<String, Object>) map.get("data");
				}else{
					logger.error("远端服务器获取数据失败: " + map);
					resultMap.put("errmsg", map.get("msg"));
					return resultMap;
				}
			}else if(map.get("errcode") != null){//会管服务器数据解析
				if(map.get("errcode").equals(0)){
					if(map.get("access_token") != null){
						Map<String, Object> tempMap = (Map<String, Object>) map.get("data");
						tempMap.put(WsConstants.KEY_TOKEN, map.get("access_token"));
						return tempMap;
					}else if(map.get("data") == null){
						return map;
					}
					return (Map<String, Object>) map.get("data");
				}else{
					logger.error("远端服务器获取数据失败: " + map);
					return map;
				}
			}
		} catch (JsonParseException e) {
			logger.error("远端服务器获取的数据转换成Map时失败: ", e);
		} catch (JsonMappingException e) {
			logger.error("远端服务器获取的数据转换成Map时失败: ", e);
		} catch (IOException e) {
			logger.error("远端服务器获取的数据转换成Map时失败: ", e);
		}
		return map;
	}
	
	
	/**
	 * 消息转发服务器-可视电话拨打、挂断
	 * @author 谢程算
	 * @date 2017年9月13日  
	 * @version 1.0.0 
	 * @param url
	 * @param token
	 * @param args
	 */
	public static Map<String, Object> downloadGet(String url, Map<String, Object> args) {
		String result = "";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			URL restServiceURL = new URL(linkUrl(url, null, args));
			HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL
					.openConnection();
			
			if (httpConnection.getResponseCode() != 200) {
				resultMap.put("errmsg", "访问消息转发服务器失败，错误码：" + httpConnection.getResponseCode());
				logger.error("访问消息转发服务器失败: errorcode " + httpConnection.getResponseCode());
				return resultMap;
			}
			
			BufferedReader responseBuffer = new BufferedReader(
					new InputStreamReader(httpConnection.getInputStream(),"utf-8"));
			
			String output;
			while ((output = responseBuffer.readLine()) != null) {
				result += output; 
			}
			httpConnection.disconnect();
		} catch (MalformedURLException e) {
			resultMap.put("errmsg", "访问消息转发服务器失败，系统内部异常");
			logger.error("访问消息转发服务器失败: ", e);
			return resultMap;
		} catch (IOException e) {
			resultMap.put("errmsg", "访问消息转发服务器失败，系统内部异常");
			logger.error("访问消息转发服务器失败: ", e);
			return resultMap;
		}
		Map<String, Object> map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(result, Map.class);
		}  catch (Exception e) {
			resultMap.put("errmsg", "json转map失败，系统内部异常");
			logger.error("json转map失败: ", e);
		}
		return map;
	}
 

}
