package com.visionvera.util;

import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Util {

	private final static Logger logger = LogManager.getLogger(Util.class);
	private static Map<String, Object> resultMap = new ConcurrentHashMap<String, Object>();;

	public static String[] list() {
		String basePath = Util.class.getResource("/").getPath();
		basePath = basePath.substring(1, basePath.length());
		return new File(basePath + File.separator + "diagrams").list();
	}

	/**
	 * 
	 * TODO 获取服务器IP
	 * @author 谢程算
	 * @date 2017年5月5日  
	 * @version 1.0.0 
	 * @return
	 */
	public static String getLocalIp() {
		String ipAddr = "";
		try{
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface
					.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
						.nextElement();
				System.out.println(netInterface.getName());
				Enumeration<InetAddress> addresses = netInterface
						.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						ipAddr = ip.toString();
						System.out.println("本机的IP = " + ip.getHostAddress());
					}
				}
			}
		} catch (Exception e) {
			logger.debug("获取服务器IP失败：", e);
		}
		return ipAddr;
	}
	
	/**
	 * 
	 * TODO 获取服务器端口号
	 * @author 谢程算
	 * @date 2017年5月5日  
	 * @version 1.0.0 
	 * @param secure
	 * @return
	 */
    public static String getServerPort(boolean secure) {  
        MBeanServer mBeanServer = null;  
        try{
	        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {  
	            mBeanServer = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);  
	        }  
	          
	        if (mBeanServer == null) {  
	        	logger.debug("调用findMBeanServer查询到的结果为null");
	            return "";  
	        }  
	          
	        Set<ObjectName> names = null;  
	        names = mBeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);  
	        Iterator<ObjectName> it = names.iterator();  
	        ObjectName oname = null;  
	        while (it.hasNext()) {  
	            oname = (ObjectName)it.next();  
	            String protocol = (String)mBeanServer.getAttribute(oname, "protocol");  
	            String scheme = (String)mBeanServer.getAttribute(oname, "scheme");  
	            Boolean secureValue = (Boolean)mBeanServer.getAttribute(oname, "secure");  
	            Boolean SSLEnabled = (Boolean)mBeanServer.getAttribute(oname, "SSLEnabled");  
	            if (SSLEnabled != null && SSLEnabled) {// tomcat6开始用SSLEnabled  
	                secureValue = true;// SSLEnabled=true但secure未配置的情况  
	                scheme = "https";  
	            }  
	            if (protocol != null && ("HTTP/1.1".equals(protocol) || protocol.contains("http"))) {  
	                if (secure && "https".equals(scheme) && secureValue) {  
	                    return ((Integer)mBeanServer.getAttribute(oname, "port")).toString();  
	                } else if (!secure && !"https".equals(scheme) && !secureValue) {  
	                    return ((Integer)mBeanServer.getAttribute(oname, "port")).toString();  
	                }  
	            }  
	        }
        } catch (Exception e) {
        	logger.debug("获取服务器端口失败：", e);
        }
        return "";  
    }  
    
    public static void main(String[] args) throws Exception{
    	String str = "应用服务器/cpu：天津92抓包机 （10.1.24.92） 当前使用率为 3.08%，阈值 1%";
    	/*getLocalIp();
    	getServerPort(true);
    	getServerPort(false);*/
		byte[] bytes = str.getBytes("utf-8");
		int loadLen = bytes.length;
    	System.out.println(str.length());
    	System.out.print(loadLen);
    }
    
    /**
     * 获取属性文件指定属性
     * @param filePath
     * @param propName
     * @return
     */
	public static String getProperty(String filePath, String propName) {
		Properties prop = new Properties();
		try {
			// 读取属性文件a.properties
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			prop.load(in); //加载属性列表
			in.close();
			return prop.getProperty(propName);
		} catch (Exception e) {
			logger.error("获取property属性失败：", e);
		}
		return "";
	}

	/**
	 * 获取属性文件所有属性
	 * @param filePath
	 * @param propName
	 * @return
	 */
	public static Properties getProperties(String filePath) {
		Properties prop = new Properties();
		try {
			// 读取属性文件a.properties
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			prop.load(in); //加载属性列表+
			in.close();
			return prop;
		} catch (Exception e) {
			logger.error("获取property属性失败：", e);
		}
		return null;
	}
	
	/**
	 * 
	 * TODO 检测URL是否可以访问
	 * @author 谢程算
	 * @date 2017年11月16日  
	 * @version 1.0.0 
	 * @param strLink
	 * @return
	 */
	public static boolean isUrlValid(String strLink) {
		try {
			URL url = new URL(strLink);
			URLConnection conn = url.openConnection();
			String str = conn.getHeaderField(0);
			if (str != null && str.indexOf("OK") > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			return false;
		}
	}
	
	/***
	 * 获取统一平台路径
	 * @return
	 */
	
	public static Map<String, Object> getBaseUrl() {
		try {
			if(resultMap.get("baseUrl") == null || resultMap.get("tcpState") == null){
				String state= Util.getSysProp("ssoserver.checkopen");
				String ip= Util.getSysProp("ssoserver.ip");
				String port= Util.getSysProp("ssoserver.port");
				String slweomsIp= Util.getSysProp("slweoms.ip");
				String slweomsPort= Util.getSysProp("slweoms.port");
				String registerId= Util.getSysProp("slweoms.registerId");
				if (StringUtils.isBlank(ip)) {
					resultMap.put("result", false);
					resultMap.put("msg", "统一服务器IP未配置,请联系超级管理员");
					return resultMap;
				}
				if (StringUtils.isBlank(port)) {
					resultMap.put("result", false);
					resultMap.put("msg", "统一服务器端口未配置,请联系超级管理员");
					return resultMap;
				}
				if(StringUtils.isBlank(state)){
					resultMap.put("result", false);
					resultMap.put("msg", "没有配置统一用户平台开关,请联系超级管理员");
					return resultMap;
				}
				if (StringUtils.isBlank(ip)) {
					resultMap.put("result", false);
					resultMap.put("msg", "运维平台ip未配置,请联系超级管理员");
					return resultMap;
				}
				if (StringUtils.isBlank(port)) {
					resultMap.put("result", false);
					resultMap.put("msg", "运维平台ip端口未配置,请联系超级管理员");
					return resultMap;
				}
				if(StringUtils.isBlank(registerId)){
					resultMap.put("result", false);
					resultMap.put("msg", "未配置运维平台标识，请联系超级管理员");
					return resultMap;
				}
				String slweomsServerUrl = String.format(WsConstants.SLWEOMS_SERVER_URL,
						slweomsIp, slweomsPort);
				String baseUrl = String.format(WsConstants.SS_SERVER_URL,
						ip, port);
				resultMap.put("result", true);
				resultMap.put("baseUrl", baseUrl);
				resultMap.put("tcpState", state == null ? 0 : Integer.valueOf(state));
				resultMap.put("slweomsServerUrl", slweomsServerUrl);
				resultMap.put("registerId", registerId==null ? "" : registerId);
			}
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取统一平台路径失败", e);
		}
		return resultMap;
	}

	
	public static String getSysProp(String propName){
		try {
			String path = ResourceUtils.getURL("classpath:").getPath();
			String filePath = GlobalConstants.SYS_CONFIG_FILE_PATH;
			filePath = java.net.URLDecoder.decode(path+filePath, "utf-8");
			filePath = filePath.replace("/", File.separator).replace("\\", File.separator);
			return Util.getProperty(filePath, propName);
		} catch (Exception e) {
			logger.error("获取版本信息失败：", e);
		}
		return "";
	}
	
	/*public static String getCatalinaPath(){
		String bathPath = "";
		try{
			bathPath = ResourceUtils.getURL("classpath:").getPath();
			bathPath = java.net.URLDecoder.decode(bathPath, "utf-8");
			bathPath = bathPath.replace("/", File.separator).replace("\\", File.separator);
		}catch(Exception e){
			return bathPath;
		}
		return bathPath;
	}*/
	
	/**
	 * 获取服务同级目录
	 * @return
	 */
	public static String getServerPath(){
		String filePath = "";
		try{
			File path  = new File(ResourceUtils.getURL("classpath:").getPath());
		    filePath= path.getParentFile().getParentFile().getParent()+File.separator;
			filePath = java.net.URLDecoder.decode(filePath, "utf-8");
			filePath = filePath.replace("/", File.separator).replace("\\", File.separator);
		}catch(Exception e){
			return filePath;
		}
		return filePath;
	}
	
	//获取指定host
	public static String getWhiteList(){
		String urlList = "";
		try {
			urlList = Util.getSysProp("cmsweb.whiteList");
//			String[] url = urlList.split(";");
//			List<String> whiteList = new ArrayList<String>();
//			for (String arg : url) {
//				whiteList.add(arg);
//			}
		} catch (Exception e) {
			return urlList;
		}
		return urlList;
	}
	
	//判断是否在白名单内
	public static boolean isWhite(String host){
		String whiteUrl = getWhiteList();
		if(whiteUrl == null){
			return true;
		}
		String[] url = whiteUrl.split(";");
		for(String whiteList : url){
			if (whiteList != null && whiteList.equals(host)) {
				return true;
	        }
		}
		return false;
	}
	
	//读取错误码
			public static String getErrDescProp(String propName){
				try {
					String filePath = GlobalConstants.ERRDESC_CONFIG_FILE_PATH;
					filePath = java.net.URLDecoder.decode(filePath, "utf-8");
					filePath = filePath.replace("/", File.separator).replace("\\", File.separator);
					return Util.getProperty(filePath, propName);
				} catch (UnsupportedEncodingException e) {
					logger.error("获取版本信息失败：", e);
				}
				return "";
			}
}
