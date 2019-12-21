package com.visionvera.util.bandwidth;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;


public class BandwidthUtils {
	/**
	 * 下载限制配置在redis中的存储标志
	 */
	//private static final String CMS_DOWNLOAD_CONFIG = "cms_download_cfg";
	/**
	 * 下载进程
	 */
	private static final String CMS_DOWNLOAD_THREAD = "cms_download";
	public static final String MAX_RATE = "download_maxrate";
	public static final String MAX_COUNT = "download_maxcount";
	private static Map<String, Set<String>> threadMap = new ConcurrentHashMap<String, Set<String>>();
	private static Map<String, Integer> downloadLimitMap = new ConcurrentHashMap<String, Integer>();
	
	//设置下载限制
	public static synchronized void setDownLoadLimit(String key, Integer value) {
		/*if(JRedisHash.hexists(CMS_DOWNLOAD_CONFIG, key)){
			JRedisHash.hdel(key, key);
		}
		JRedisHash.hset(CMS_DOWNLOAD_CONFIG, key, String.valueOf(value));*/
		if (downloadLimitMap.get(key)!=null) {
			downloadLimitMap.remove(key);
		}
		downloadLimitMap.put(key, value);
    }
	//获取下载限制
	public static synchronized Integer getDownLoadLimit(String key) {
		int value = downloadLimitMap.get(key);
		//String value = JRedisHash.hget(CMS_DOWNLOAD_CONFIG, key);
		return value < 0 ? null : value;
    }
	//设置当前正在下载的进程
	public static synchronized void addDownLoadThread(String currentThreadName, String currentThreadIp) {
		if(StringUtils.isBlank(currentThreadIp)) {
			return;
		}
    	Set<String> threadSet = threadMap.get(currentThreadIp);
		if(threadSet == null) {
    		threadSet = new HashSet<String>();
    	}
    	threadSet.add(currentThreadName);
    	threadMap.put(currentThreadIp, threadSet);
		/*String key = getDownloadThreadKey(currentThreadIp);
		Set<Object> set2 = JRedisObject.getSet(key);
		if(set2 == null){
			set2 = new HashSet<>();
		}else{
			JRedisObject.del(key);
		}
		if(!set2.contains(key)){
			set2.add(currentThreadName);
		}
		JRedisObject.setSet(key, set2, 0);*/
    }
    
    public static synchronized void removeDownLoadThread(String currentThreadName, String currentThreadIp) {
    	Set<String> threadSet = threadMap.get(currentThreadIp);
    	if(threadSet != null) {
    		threadSet.remove(currentThreadName);
    	}
    	if(threadSet != null && threadSet.size() == 0){
    		threadMap.remove(currentThreadIp);
    	}
   /* 	String key = getDownloadThreadKey(currentThreadIp);
    	Set<Object> set = JRedisObject.getSet(key);
    	//如果存在，则删除
    	if(set != null && set.contains(currentThreadName)){
    		set.remove(currentThreadName);
    		//如果set已经为空，则将其删除
        	if(set == null || set.size() == 0){
        		JRedisObject.delSet(key);
        	}else{//重置set
        		JRedisObject.delSet(key);
        		JRedisObject.setSet(key, set, 0);
        	}
    	}*/
    	
    }
    
    public static synchronized int getDownloadCount(String currentThreadIp) {
    	Set<String> threadSet = threadMap.get(currentThreadIp); 
    	int threadCount = threadSet == null ? 0 : threadSet.size();
    	//String key = getDownloadThreadKey(currentThreadIp);
    	//Set<Object> set = JRedisObject.getSet(key);
    	//int threadCount = set == null ? 0 : set.size();
    	return threadCount;
    }
    
    private static synchronized String getDownloadThreadKey(String currentThreadIp) {
    	return CMS_DOWNLOAD_THREAD+"_"+currentThreadIp;
    }
    
    public static void main(String[] args) {
    	//setDownLoadLimit(MAX_COUNT, 6);
    	//System.out.println(getDownLoadLimit(MAX_RATE));
    	//addDownLoadThread("currentThreadName2", "currentThreadIp1");
    	//removeDownLoadThread("currentThreadName1", "currentThreadIp1");
	}
}
