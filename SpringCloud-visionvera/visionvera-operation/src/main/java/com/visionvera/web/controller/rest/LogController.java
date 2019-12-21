package com.visionvera.web.controller.rest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.DeviceLogVO;
import com.visionvera.bean.cms.LogTypeVO;
import com.visionvera.bean.cms.LogVO;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.service.LogService;
import com.visionvera.service.SysConfigService;
import com.visionvera.util.RestClient;
import com.visionvera.util.ResultMapUtil;


@RestController
@RequestMapping("/rest/log")
public class LogController {
private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private SysConfigService sysConfigService;
	
	@Autowired
	private SysConfig sysConfig;
	
	/**
	 * 查询日志
	 * @param log 查询条件
	 * @param isPage 是否分页。1表示分页，0表示不分页
	 * @return
	 */
	@RequestMapping(value = "/{isPageType}/getLogList", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> getLogList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestBody(required = false) LogVO log, @PathVariable("isPageType") String isPageType) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> extraMap = new HashMap<String, Object>();
		boolean isPage = true;
		
		try {
			if (isPageType.equals("0")) {
				isPage = false;
			}
			PageInfo<LogVO> logInfo = this.logService.getLogList(isPage, pageNum, pageSize, log);
			extraMap.put("totalPage", logInfo.getPages());
			extraMap.put("pageNum", logInfo.getPageNum());
			
			resultMap = ResultMapUtil.getResultMap(0, "查询成功", logInfo.getList(), extraMap);
		} catch (Exception e) {
			this.LOGGER.error("获取日志列表失败 ===== LogController ===== getLogList =====> ", e);
			resultMap = ResultMapUtil.getResultMapError("获取日志列表失败");
		}
		
		return resultMap;
	}
	
	/**
	 * 获取所有类型
	 * @return
	 */
	@RequestMapping(value = "/getLogType", method = RequestMethod.GET)
	public Map<String, Object> getLogType() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			List<LogTypeVO> logTypeList = this.logService.getLogTypeList();
			resultMap = ResultMapUtil.getResultMap(0, "查询成功", logTypeList);
		} catch (Exception e) {
			this.LOGGER.error("获取日志类型 ===== LogController ===== getLogType =====> ", e);
			resultMap = ResultMapUtil.getResultMapError("获取日志类型失败");
		}
		
		return resultMap;
	}	

	/**
	 * 
	 * @Title: getDeviceLog
	 * @Description: TODO 获取终端日志
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/getDeviceLog", method = RequestMethod.GET)
	public Map<String, Object> getDeviceLog(String devno,String userId,Integer filetype) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("devid", devno);
			paramsMap.put("filetype", filetype);
			//获取会管的ip端口
			String hgUrl = this.sysConfig.getHgUrl();
			String url =String.format(WsConstants.URL_DEVICELOG_POST,userId)+"?microServerFlag=1"; 
			Map<String, Object> map = RestClient.post(hgUrl+url, null, paramsMap);
			if(map != null &&  (Integer)map.get("errcode") == 0){
				//将文件的uuid返回前端
				List<DeviceLogVO> list = sysConfigService.getDevLog(paramsMap);
				if(list != null && list.size() > 0){
					resultMap.put("uuid", list.get(0).getUuid());
				}
				resultMap.put("errmsg", "获取文件成功");
				resultMap.put("result", true);
	    	}else{
	    		resultMap.put("errmsg", "获取文件失败");
				resultMap.put("result", false);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取文件失败");
		}
		return resultMap;
	}
	

	/**
	 * 
	 * @Title: deleteDevLog
	 * @Description: 删除终端日志
	 * @param @param uuid
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("deleteDevLog")
	public Map<String,Object> deleteDevLog(String userId,String uuid,String filePath){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			//paramsMap.put("microServerFlag", 1);
			paramsMap.put("uuid", uuid);
			paramsMap.put("savepath", filePath);
			//获取会管的ip端口
			String hgUrl = this.sysConfig.getHgUrl();
			String url = String.format(WsConstants.URL_DEVICELOG_DELETE,userId)+"?microServerFlag=1";
			Map<String, Object>  map = RestClient.post(hgUrl+url,null, paramsMap);
			if(map != null &&  (Integer)map.get("errcode") == 0){
				resultMap.put("errmsg", "删除文件成功");
				resultMap.put("result", true);
	    	}else{
	    		resultMap.put("errmsg", "删除文件失败");
				resultMap.put("result", false);
	    	}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("errmsg", "删除终端日志失败");
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: getAalyzsisLog
	 * @Description: TODO 获取终端日志
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping("getAalyzsisLog")
	public Map<String, Object> getAalyzsisLog(String filename,String devno,String userId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			//paramsMap.put("microServerFlag", 1);
			paramsMap.put("filename", filename);
			paramsMap.put("devid", devno);
			//paramsMap.put("userId", userId);
			//获取会管的ip端口
			String hgUrl = this.sysConfig.getHgUrl();
			String url = String.format(WsConstants.URL_DEVICELOG_AALYZSISLOG,userId)+"?microServerFlag=1";
			Map<String, Object> map = RestClient.post(hgUrl+url,null, paramsMap);
			if(map != null &&  (Integer)map.get("errcode") == 0){
				resultMap.put("errmsg", "获取文件成功");
				resultMap.put("result", true);
	    	}else{
	    		resultMap.put("errmsg", "获取文件失败");
				resultMap.put("result", false);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", false);
			resultMap.put("errmsg", "获取文件失败");
			//logger.error("获取文件失败", e);
		}
		return resultMap;
	}
	
	
	/**
	 * 
	 * @Title: downloadDevLog
	 * @Description: TODO 
	 * @param 
	 * @return Map<String,Object> 返回类型
	 */
	@RequestMapping("downloadDevLog")
	public Map<String,Object> downloadDevLog(String filePath,HttpServletResponse response){
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			InputStream is = null;
		    OutputStream os = null;
	        try {
	        	String param = URLEncoder.encode(filePath, "utf-8");
	        	String fileName = URLDecoder.decode(filePath, "utf-8");
	        	String downfileName = fileName.substring(
						fileName.lastIndexOf("/") + 1, fileName.length());
				paramsMap.put("filePath", param);
			    //获取会管的ip端口
				String hgUrl = this.sysConfig.getHgUrl();
			    //校验文件是否存在
				//Map<String, Object> map = RestClient.downloadGet(hgUrl+WsConstants.CHECK_FILE, paramsMap);
				//if(map != null &&  (boolean)map.get("result") == true){
					response.setContentType("application/x-download");
				    response.setHeader("Content-Disposition", "attachment;filename="+ new String((downfileName).getBytes(), "iso-8859-1"));
			        URL url = new URL(hgUrl+WsConstants.URL_DEVICELOG_DOWNLOAD+"?filePath="+filePath);
			        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
				    urlCon.setConnectTimeout(6000);
				    urlCon.setReadTimeout(600000);
				    int code = urlCon.getResponseCode();
				    if (code != HttpURLConnection.HTTP_OK) {
				        throw new Exception("文件读取失败");
				    }
				    // 读文件流
					is = new BufferedInputStream(urlCon.getInputStream());
		            os = new BufferedOutputStream(response.getOutputStream());
					byte[] buff = new byte[2048];
				    int bytesRead;
				    while (-1 != (bytesRead = is.read(buff, 0, buff.length))) {
				    	os.write(buff, 0, bytesRead);
				    }
					os.flush();
		    	/*}else{
		    		resultMap.put("errmsg", map.get("msg").toString());
					resultMap.put("result", false);
					return resultMap;
		    	}*/
				resultMap.put("errmsg", "获取文件成功");
				resultMap.put("result", true);
	        } catch (final Exception e) {
	        	resultMap.put("result", false);
	        	resultMap.put("errmsg", "下载文件出现异常");
	        } finally {
	        	try {
					if (is != null) {
						is.close();
					}
					if (os != null) {
						os.close();
					}
				} catch (IOException e) {
					resultMap.put("result", false);
					resultMap.put("errmsg", "下载文件出现异常");
				}
	        }
	        return resultMap;
		}
	
	
	/**
	 * 
	 * @Title: checkFile
	 * @Description: TODO 
	 * @param 
	 * @return Map<String,Object> 返回类型
	 */
	@RequestMapping("checkFile")
	public Map<String,Object> checkFile(String filePath,HttpServletResponse response){
			Map<String, Object> resultMap = new HashMap<String, Object>();
			Map<String, Object> paramsMap = new HashMap<String, Object>();
	        try {
	        	String param = URLEncoder.encode(filePath, "utf-8");
				paramsMap.put("filePath", param);
			    //获取会管的ip端口
				String hgUrl = this.sysConfig.getHgUrl();
			    //校验文件是否存在
				Map<String, Object> map = RestClient.downloadGet(hgUrl+WsConstants.CHECK_FILE, paramsMap);
				if(map != null &&  (boolean)map.get("result") == true){
					resultMap.put("errmsg", "获取文件成功");
					resultMap.put("result", true);
					return resultMap;
		    	}else{
		    		resultMap.put("errmsg", map.get("msg").toString());
					resultMap.put("result", false);
					return resultMap;
		    	}
	        } catch (final Exception e) {
	        	resultMap.put("result", false);
	        	resultMap.put("errmsg", "下载文件出现异常");
	        } 
	        return resultMap;
		}
	
	
	
}
