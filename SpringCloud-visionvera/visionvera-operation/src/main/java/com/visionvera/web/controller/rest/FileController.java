package com.visionvera.web.controller.rest;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.FileVO;
import com.visionvera.bean.cms.ServerVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.config.SysConfig;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import com.visionvera.service.FileService;
import com.visionvera.service.SysConfigService;
import com.visionvera.service.UserService;
import com.visionvera.util.TimeUtil;
import com.visionvera.util.Util;
import com.visionvera.util.bandwidth.BandwidthLimiter;
import com.visionvera.util.bandwidth.BandwidthUtils;
import com.visionvera.util.bandwidth.InputStreamLimiter;
import com.visionvera.util.bandwidth.OutputStreamLimiter;


/**
 * 
 * @ClassName: FileController
 * @Description: 文件管理（pamir升级）
 * @author xiechs
 * @date 2017年4月28日 下午5:45:27
 * 
 */
@RestController
@RequestMapping("/rest/file")
public class FileController {

	@Resource
	private FileService fileService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private com.visionvera.feign.UserService authUserService;
	
	@Resource
	private SysConfigService sysConfigService;
	
	@Autowired
	private SysConfig sysConfig;
	
	private static final Logger logger = LogManager.getLogger(FileController.class);

	
	/**
	 * 
	 * TODO 获取文件列表
	 * @author 谢程算
	 * @date 2017年4月28日  
	 * @version 1.0.0 
	 * @param pageNum
	 * @param pageSize
	 * @param name
	 * @return
	 */
	@RequestMapping("fileList")
	@ResponseBody
	public Map<String, Object> getFileList(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			String name, String version, Integer spVersion, Integer applyFlag) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			paramsMap.put("spVersion", spVersion);
			paramsMap.put("applyFlag", applyFlag);
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			if (StringUtils.isNotBlank(version)) {
				paramsMap.put("version", version);
			}
			List<FileVO> list = fileService.getFileList(paramsMap);
			int total = fileService.getFileListCount(paramsMap);
			total = total % pageSize == 0 ? total / pageSize
					: (total / pageSize) + 1;
			resultMap.put("pageNum", pageNum);
			resultMap.put("pageTotal", total);
			resultMap.put("list", list);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取设备列表失败", e);
		}
		return resultMap;
	}

	
	/** <pre>getFileDetail(这里用一句话描述这个方法的作用)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 上午10:45:30    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 上午10:45:30    
	 * 修改备注： 
	 * @param name
	 * @return</pre>    
	 */
	@RequestMapping("getFileDetail")
	@ResponseBody
	public Map<String, Object> getFileDetail(String name) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		try {
			if (StringUtils.isNotBlank(name)) {
				paramsMap.put("name", URLDecoder.decode(name, "utf-8"));
			}
			FileVO file = fileService.getFileDetail(paramsMap);
			resultMap.put("file", file);
			resultMap.put("result", true);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取设备列表失败", e);
		}
		return resultMap;
	}
	
	
	/** <pre>checkUser(校验当前登录人员是否有权限上传文件)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月26日 上午11:23:35    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月26日 上午11:23:35    
	 * 修改备注： 
	 * @param name
	 * @return</pre>    
	 */
	@RequestMapping("checkUser")
	public Map<String, Object> checkUser(@RequestParam(value = "access_token") String token,HttpSession session) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		try{
			ReturnData data = authUserService.getUser(token);
			if (!data.getErrcode().equals(0)) {
				resultMap.put("result", false);
				resultMap.put("errmsg", data.getErrmsg());
				return resultMap;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>)data.getData();
			userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
			//上传文件权限为可配置人员
			String loginName = userData.getLoginName();
			//根据登录名获取真实名称
			UserVO user = userService.getNameByLoginName(loginName);
			//获取可上传文件人员信息
			List<ConstDataVO> list = sysConfigService.getConfigUpdate();
			boolean auth = false;
			for (ConstDataVO constDataVO : list) {
				if (constDataVO.getDisplay().equals(user.getName())) {
					auth = true;
					break;
				}
			}	
			if (!auth) {
				resultMap.put("result", false);
			}else{
				resultMap.put("result", true);
			}
		} catch (Exception e) {
			logger.error("校验当前登录人员是否有权限上传文件失败", e);
		}
		return resultMap;
	}
	
	/**
	 * 
	 * @Title: upLoadFile
	 * 全量命名：PamirAll_xx.xx.xx.xx.zip
	 * 增量命名：PamirPart_xx.xx.xx.xx.zip
	 * @Description: 上传文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("uploadFile")
	public Map<String, Object> uploadFile(@RequestParam(value = "access_token") String token,HttpServletRequest request, HttpSession session){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		resultMap.put("result", true);
		InputStream in = null;
		FileOutputStream fos = null;
		try {
				ReturnData data = authUserService.getUser(token);
				if (!data.getErrcode().equals(0)) {
					resultMap.put("result", false);
					resultMap.put("errmsg", data.getErrmsg());
					return resultMap;
				}
				@SuppressWarnings("unchecked")
				Map<String, Object> dataMap = (Map<String, Object>)data.getData();
				userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
				CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());    
				//判断 request 是否有文件上传,即多部分请求    
				if(multipartResolver.isMultipart(request)){    
				    //转换成多部分request      
				    MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;  
				    // 取得request中的所有文件名  
				    Iterator<String> iter = multiRequest.getFileNames();  
				    MultipartFile file = null;
				    String destFileName = null;
				    while (iter.hasNext()) {  
				        // 取得上传文件  
				        file = multiRequest.getFile(iter.next());  
				        // 数据封装操作 MultipartFile reqeust  
				        // 取得当前上传文件的文 件名称  
				        destFileName = file.getOriginalFilename(); //这里需要对文件进行处理 
				        if(!destFileName.matches(GlobalConstants.FILE_NAME_REG)){//检查文件命名格式
				        	resultMap.put("result", false);
				        	resultMap.put("msg", "文件名格式错误。正确格式：PAMIR_Vxx.xx.xx或ME_Vxx.xx.xx");
				        	break;
				        }
				        FileVO fileInfo = new FileVO();
				        fileInfo.setName(destFileName); //文件名
				        if(fileService.checkName(fileInfo) > 0){
				        	resultMap.put("result", false);
				        	resultMap.put("msg", "已存在同名文件！");
				        	break;
				        }
				        File file_root = new File(Util.getServerPath() + GlobalConstants.FILE_DIR_ROOT);  
				        //判断目录是否存在  
				        if(!file_root.exists()) {
				        	//如果目标文件所在的目录不存在，则创建父目录  
				        	if(!file_root.mkdirs()) {  
				        		logger.error("创建目标文件所在目录失败！");
				        	}  
				        }
				        //这是你要保存之后的文件，是自定义的，本身不存在
				        File afterfile = new File(file_root + File.separator + destFileName);
				        //先删除文件（如果已经存在）
				        if(afterfile.exists()){
				        	afterfile.delete();
				        }
				        in = file.getInputStream();
				        logger.info("文件大小:" + file.getSize());
						//定义文件输出流，用来把信息写入afterfile文件中
						fos = new FileOutputStream(afterfile);
						//文件缓存区
						byte[] b = new byte[1024 * 10];
						int i;
						//将文件流信息读取文件缓存区，如果读取结果不为-1就代表文件没有读取完毕，反之已经读取完毕
						while((i=in.read(b))>0){
							//将缓存区中的内容写到afterfile文件中
						    fos.write(b, 0, i);  
						    fos.flush();
						}
						if(destFileName.toLowerCase().startsWith("pamir")){
							fileInfo.setApplyFlag(1);
						}else{
							fileInfo.setApplyFlag(2);
						}
						fileInfo.setSize(file.getSize()); //文件大小，单位byte
						fileInfo.setForce(0);
						//fileInfo.setSpVersion(0);为无效版本时不可选择升级策略
						fileInfo.setVersion(destFileName.substring(destFileName.indexOf("_") + 2, destFileName.lastIndexOf(".")));//版本
						fileInfo.setCreator(userData.getLoginName());//创建人
						fileInfo.setAppMd5(getMd5(afterfile));
						fileInfo.setCreateTime(TimeUtil.stampToDate(System.currentTimeMillis()));
						fileService.addFile(fileInfo);
						resultMap.put("uuid", fileInfo.getUuid());
						resultMap.put("createTime", fileInfo.getCreateTime().substring(0,fileInfo.getCreateTime().length()-3));
						resultMap.put("version", fileInfo.getVersion());
				    }  
				}
			//}
		
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "上传文件失败，系统内部异常");
			logger.error("上传文件失败，系统内部异常", e);
		} finally {
			try {
				if(fos != null){
					fos.close();
				}
				if(in != null){
					in.close();
				}
				System.gc();//立即释放流，防止占用文件
			} catch (IOException e) {
				logger.error("关闭输入流失败", e);
			}
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: checkName
	 * 全量命名：PamirAll_xx.xx.xx.xx.zip
	 * 增量命名：PamirPart_xx.xx.xx.xx.zip
	 * @Description: 上传文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("checkName")
	public Map<String, Object> checkName(String fileName) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", true);
		FileVO fileInfo = new FileVO();
		fileInfo.setName(fileName); // 文件名
		resultMap.put("count", fileService.checkName(fileInfo));
		return resultMap;
	}
	
	/** <pre>qrUpdate(获取会控App当前可使用版本)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月25日 下午7:22:00    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月25日 下午7:22:00    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("qrUpdate")
	@ResponseBody
	public Map<String, Object> qrUpdate() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			List<FileVO> list = fileService.qrUpdate();
			if (list != null && list.size() > 0 ) {
				String downLoadUrl = "http://"+getServer(5).get("ip")+":"+getServer(5).get("port")+"/cmsweb/file/downloadFile?name="+list.get(0).getName();
				list.get(0).setDownLoadUrl(downLoadUrl);
			}
			resultMap.put("result", true);
			resultMap.put("list", list);
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取会控App当前可使用版本失败", e);
		}
		return resultMap;
	}


	/**
	 * 
	 * @Title: setFileInfo
	 * @Description: 设置文件属性
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/setFileInfo", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> setFileInfo(@RequestBody FileVO file){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		resultMap.put("result", true);
		try {
			//获取更新状态，有效无效
			Integer force = file.getForce();
			if (StringUtils.isBlank(file.getLastUpdateTime())) {
				file.setLastUpdateTime(null);
			}
			int result = fileService.updateFile(file);
			if(result > 0) {
				 //判断更改状态
				 if(force != null && force > 0){
					 paramsMap.put("applyFlag", file.getApplyFlag());
					 paramsMap.put("uuid", file.getUuid());
					 paramsMap.put("force", 0);
					 paramsMap.put("spVersion", null);
					 paramsMap.put("lastUpdateTime", null);
					 int updateTypeResult = fileService.updateForceByPackage(paramsMap);
					 if(updateTypeResult > 0){
						 logger.info("更新状态成功");
					 }
				 }
				 resultMap.put("result", true);
				 resultMap.put("msg", "操作成功");
				 logger.info("操作成功");
				return resultMap;
			}
			//TODO 操作失败要删除已上传的文件
			deleteFile(file);
			resultMap.put("result", false);
			resultMap.put("msg", "操作失败");
			logger.info("操作失败");
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("更新文件信息失败：", e);
		}
		return resultMap;
	}

	/**
	 * 
	 * @Title: downloadFile
	 * @Description: 下载文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping("downloadFile")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, String name, Integer flag){
		String fileName = null;
        InputStream is = null;
        OutputStream os = null;
        String ip = null;
        String threadName = Thread.currentThread().getName();
        try {
        	//获取服务端ip，用来统计同时下载数量
        	StringBuffer requestURL = request.getRequestURL();
        	String substring = requestURL.substring(7);
        	int indexOf = substring.indexOf(":");
        	ip = substring.substring(0, indexOf);
        	BandwidthUtils.setDownLoadLimit(BandwidthUtils.MAX_COUNT, (Integer)getPublicIp().get("maxcount"));
        	BandwidthUtils.setDownLoadLimit(BandwidthUtils.MAX_RATE, (Integer)getPublicIp().get("maxrate"));
        	if (flag == null) {
        		//当前正在进行的下载进程
    			int downloadCount = BandwidthUtils.getDownloadCount(ip);
    			//获取最大可下载进程数
    			int maxDownloadCount = this.getMaxDownloadCount();
    			if(maxDownloadCount > 0 && downloadCount >= maxDownloadCount){
    				String str = "目前已达到同时下载最大任务数"+downloadCount+"个";
    				response.setHeader("msg",new String(str.getBytes("UTF-8"), "ISO-8859-1"));
    				return;
    			}
			}
	        fileName = URLDecoder.decode(name, "utf-8");
			String downfileName = fileName.substring(
					fileName.lastIndexOf(File.separator) + 1, fileName.length());
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition", "attachment;fileName="
					+ new String(downfileName.getBytes("UTF-8"), "ISO-8859-1"));
			File file = new File(Util.getServerPath() + GlobalConstants.FILE_DIR_ROOT + File.separator + fileName);
			if (!file.exists()) {
				String str = "文件已被删除";
				response.setHeader("msg",new String(str.getBytes("UTF-8"), "ISO-8859-1"));
				return;
			}
			logger.info("文件路径:" + fileName + "===文件大小:" + file.length());
	        // 设置response参数，可以打开下载页面
	        response.reset();
	        response.setContentLength((int) file.length());
	        response.setContentType("application/vnd.ms-excel;charset=utf-8");
	        response.setHeader("Content-Disposition", "attachment;filename="+ new String((fileName).getBytes(), "iso-8859-1"));
	        response.setHeader("msg", new String("downloadSuccess".getBytes("UTF-8"), "ISO-8859-1"));
	        logger.info("download client ip is " + ip);
			BandwidthLimiter readLimiter = new BandwidthLimiter();
			readLimiter.setCurrentThreadIp(ip);
			is = new InputStreamLimiter(new FileInputStream(file), readLimiter);
			os = new OutputStreamLimiter(response.getOutputStream(), readLimiter);
			//添加下载进程进入缓存中
			BandwidthUtils.addDownLoadThread(threadName, ip);
            byte[] buf = new byte[1024*10]; 
            int read; 
			while ((read = is.read(buf,0,buf.length)) != -1) {
				os.write(buf, 0, read);
			}
			os.flush();
			logger.info("下载文件：" + fileName + "成功");
        } catch (final Exception e) {
        	logger.error("下载文件出现异常：", e);
        } finally {
        	try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				logger.info("下载文件错误：", e);
			}
			if(StringUtils.isNotBlank(ip)){
				BandwidthUtils.removeDownLoadThread(threadName, ip);
			}
        }
	}
	
	/** <pre>getMaxDownloadCount(获取下载最大数)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月17日 下午7:20:12    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月17日 下午7:20:12    
	 * 修改备注： 
	 * @return</pre>    
	 */
	private int getMaxDownloadCount(){  
		try {
			Integer maxCount1 = BandwidthUtils.getDownLoadLimit(BandwidthUtils.MAX_COUNT);
			if (maxCount1 != null) {
				return maxCount1.intValue();
			}
			int count = 3;
			int maxRate = this.getSystemConfigByFunc(count, BandwidthUtils.MAX_RATE);
			int maxCount = this.getSystemConfigByFunc(count, BandwidthUtils.MAX_COUNT);
			maxRate = maxRate <= 0 ? 1 : maxRate;
			maxCount = maxCount <= 0 ? 5 : maxCount;
			BandwidthUtils.setDownLoadLimit(BandwidthUtils.MAX_RATE, maxRate);
			BandwidthUtils.setDownLoadLimit(BandwidthUtils.MAX_COUNT, maxCount);
			logger.info("DownloadController============getMaxCount==========maxRate=====["+maxRate+"]=======maxCount=====["+maxCount+"]");
			return maxCount;
		} catch (Exception e) {
			logger.error("获取最大下载任务数失败", e);
		}
		return 0;
    }
	
	private int getSystemConfigByFunc(int count, String func) throws Exception{
		return count;
	}
	/**
	 * @return 
	 * 
	 * @Title: deleteFile
	 * @Description: 删除文件
	 * @param @param map
	 * @param @return 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	@RequestMapping(value = "/deleteFile", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public Map<String, Object> deleteFile(@RequestBody FileVO f){
		Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	if(StringUtils.isBlank(f.getName())){
        		resultMap.put("result", false);
        		resultMap.put("msg", "文件名不能为空");
        		return resultMap;
        	}
        	fileService.deleteFile(f);
        } catch (final Exception e) {
            resultMap.put("result", false);
            resultMap.put("msg", "删除文件失败：系统内部异常");
            logger.error("删除文件失败：", e);
        }
        resultMap.put("result", true);
        resultMap.put("msg", "删除文件成功");
        return resultMap;
	}
	
	/** <pre>getPublicIp(读取properties文件公网ip地址)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月10日 下午5:20:13    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月10日 下午5:20:13    
	 * 修改备注： 
	 * @return</pre>    
	 */
	@RequestMapping("getPublicIp")
	private Map<String, Object> getPublicIp(){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String maxrate = Util.getSysProp("cmsweb.maxrate");
			String maxcount = Util.getSysProp("cmsweb.maxcount");
			resultMap.put("maxrate",Integer.parseInt(maxrate));
			resultMap.put("maxcount", Integer.parseInt(maxcount));
			resultMap.put("result", true);
		} catch (Exception e) {
			logger.error("获取版本信息失败：", e);
		}
		return resultMap;
	}
	
	
	/**
	 * 获取文件MD5值
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String getMd5(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
        	logger.error("获取文件md5失败：", e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }
	
	/** <pre>getServer(获取会管服务器ip端口号)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月23日 上午11:58:20    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月23日 上午11:58:20    
	 * 修改备注： 
	 * @param type
	 * @return</pre>    
	 */
	@RequestMapping("getServer")
	public Map<String,Object> getServer(Integer type){//1调度服务器；2网管服务器
		Map<String, Object> paramsMap = new HashMap<String,Object>();
		Map<String, Object> resultMap = new HashMap<String,Object>();
		paramsMap.put("type", type);
		try {
			ServerVO server = sysConfigService.getServer(paramsMap);
			resultMap.put("result", true);
			resultMap.put("ip", server.getIp());
			resultMap.put("port", server.getPort());
		} catch (Exception e) {
			resultMap.put("result", false);
			logger.error("获取服务器ip端口号失败", e);
		}
		return resultMap;
	}
	
	/** <pre>dodownload(获取正在下载的线程数量)   
	 * 创建人：周逸芳       
	 * 创建时间：2018年1月29日 上午11:01:57    
	 * 修改人：周逸芳        
	 * 修改时间：2018年1月29日 上午11:01:57    
	 * 修改备注： 
	 * @param request
	 * @return</pre>    
	 */
	@RequestMapping("dodownload")
	public Map<String,Object> dodownload(HttpServletRequest request,String name){
		//获取服务端ip，用来统计同时下载数量
    	StringBuffer requestURL = request.getRequestURL();
    	String substring = requestURL.substring(7);
    	int indexOf = substring.indexOf(":");
    	String clientIP = substring.substring(0, indexOf);
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String fileName = "";
		try {
			//当前正在进行的下载进程
			int downloadCount = BandwidthUtils.getDownloadCount(clientIP);
			//获取最大可下载进程数
			int maxDownloadCount = this.getMaxDownloadCount();
			if(maxDownloadCount > 0 && downloadCount >= maxDownloadCount){
				resultMap.put("result",false);
				resultMap.put("msg","目前已达到同时下载最大任务数"+downloadCount+"个,请稍后再试");
				return resultMap;
			}
			fileName = URLDecoder.decode(name, "utf-8");
			File file = new File(Util.getServerPath() + GlobalConstants.FILE_DIR_ROOT + File.separator + fileName);
			if (!file.exists()) {
				resultMap.put("result",false);
				resultMap.put("msg","文件不存在");
				return resultMap;
			}
			resultMap.put("result",true);
		} catch (Exception e) {
			logger.error("判断设备是否具体下载资格报错", e);
			resultMap.put("result",false);
			resultMap.put("msg","下载出错");
		}
		return resultMap;
	}
	
	
	@RequestMapping("uploadFileByOpertion")
	public Map<String, Object> uploadFileByOpertion(HttpServletRequest request, HttpSession session){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", true);
		try {
			String hgUrl = this.sysConfig.getHgUrl();
			String url = hgUrl+WsConstants.UPLOAD_FILE;
			resultMap.put("url", url);
			resultMap.put("result", true);
			resultMap.put("msg", "获取上传文件路径成功");
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "获取上传文件路径失败");
			logger.error("获取上传文件路径失败，系统内部异常", e);
		}
		return resultMap;
	}
	
	
	@RequestMapping("uploadUpgradeFile")
	public Map<String, Object> uploadUpgradeFile(@RequestParam(value = "access_token") String token,HttpServletRequest request, 
			HttpServletResponse response){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		UserVO userData = new UserVO();
		resultMap.put("result", true);
		InputStream in = null;
		OutputStream os = null;
		try {
				ReturnData data = authUserService.getUser(token);
				if (!data.getErrcode().equals(0)) {
					resultMap.put("result", false);
					resultMap.put("errmsg", data.getErrmsg());
					return resultMap;
				}
				@SuppressWarnings("unchecked")
				Map<String, Object> dataMap = (Map<String, Object>)data.getData();
				userData = JSONObject.parseObject(JSONObject.toJSONString(dataMap.get("extra")), UserVO.class);
				CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());    
				//判断 request 是否有文件上传,即多部分请求    
				if(multipartResolver.isMultipart(request)){    
				    //转换成多部分request      
				    MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;  
				    // 取得request中的所有文件名  
				    Iterator<String> iter = multiRequest.getFileNames();  
				    MultipartFile file = null;
				    String destFileName = null;
				    while (iter.hasNext()) {  
				        // 取得上传文件  
				        file = multiRequest.getFile(iter.next());  
				        // 数据封装操作 MultipartFile reqeust  
				        // 取得当前上传文件的文 件名称  
				        destFileName = file.getOriginalFilename(); //这里需要对文件进行处理 
				        if(!destFileName.matches(GlobalConstants.FILE_NAME_REG)){//检查文件命名格式
				        	resultMap.put("result", false);
				        	resultMap.put("msg", "文件名格式错误。正确格式：PAMIR_Vxx.xx.xx或ME_Vxx.xx.xx");
				        	break;
				        }
				        FileVO fileInfo = new FileVO();
				        fileInfo.setName(destFileName); //文件名
				        if(fileService.checkName(fileInfo) > 0){
				        	resultMap.put("result", false);
				        	resultMap.put("msg", "已存在同名文件！");
				        	break;
				        }
				        in = file.getInputStream();
				        logger.info("文件大小:" + file.getSize());
						//定义文件输出流，用来把信息写入response中
				      //调用会管上传接口上传该升级包
						String hgUrl = this.sysConfig.getHgUrl();
					    //校验文件是否存在
					    URL url = new URL(hgUrl+WsConstants.UPLOAD_FILE);
					    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					    conn.setDoOutput(true);//需要用到输出流
				        conn.setDoInput(true);//需要用到输入流
				        conn.setRequestMethod("POST");
				        conn.setRequestProperty("content-type", "text/html");//设置内容类型
						os = new BufferedOutputStream(conn.getOutputStream());
						DataOutputStream out = new DataOutputStream(conn.getOutputStream());
						String fileName = "destFileName=" + URLEncoder.encode(destFileName, "UTF-8");
						out.writeBytes(fileName);
						//文件缓存区
						byte[] b = new byte[1024 * 10];
						int i;
						//将文件流信息读取文件缓存区，如果读取结果不为-1就代表文件没有读取完毕，反之已经读取完毕
						while((i=in.read(b))>0){
							//将缓存区中的内容写到afterfile文件中
						    os.write(b, 0, i);  
						    os.flush();
						}
				    }  
				}
		} catch (Exception e) {
			resultMap.put("result", false);
			resultMap.put("msg", "上传文件失败，系统内部异常");
			logger.error("上传文件失败，系统内部异常", e);
		} finally {
			try {
				if(os != null){
					os.close();
				}
				if(in != null){
					in.close();
				}
				System.gc();//立即释放流，防止占用文件
			} catch (IOException e) {
				logger.error("关闭输入流失败", e);
			}
		}
		return resultMap;
	}
}
	
