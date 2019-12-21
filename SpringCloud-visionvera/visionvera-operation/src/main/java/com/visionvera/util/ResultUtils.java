package com.visionvera.util;

import java.util.HashMap;
import java.util.Map;

import com.github.pagehelper.PageInfo;

/**
 * 返回json
 * @author wangrz
 * 2018年6月13日
 * @param <T>
 */
public class ResultUtils{
	
	public static <T> Map<String, Object> getResult(Integer errcode, String errmsg){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errcode", errcode);
		resultMap.put("errmsg", errmsg);
		return resultMap;
	}
	
	public static <T> Map<String, Object> getResult(Integer errcode, String errmsg, PageInfo<T> pageInfo) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errcode", errcode);
		resultMap.put("errmsg", errmsg);
		resultMap.put("resultList", pageInfo);
		return resultMap;
	}
	public static Map<String, Object>  getResult(Integer errcode, String errmsg, Object object) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("errcode", errcode);
		resultMap.put("errmsg", errmsg);
		resultMap.put("data", object);
		return resultMap;
	}
	
	public static Map<String, Object> getResult(boolean result, String msg){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", result);
		resultMap.put("msg", msg);
		return resultMap;
	}
}
