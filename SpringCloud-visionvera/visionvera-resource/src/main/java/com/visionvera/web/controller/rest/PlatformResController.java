package com.visionvera.web.controller.rest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.github.pagehelper.Page;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.PlatformResourceVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;
import com.visionvera.common.api.resource.PlatformResAPI;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.UserService;
import com.visionvera.service.PlatformResService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.StringUtil;


/**
 * 平台资源信息Controller
 * @author dql
 *
 */
@RestController
public class PlatformResController extends BaseReturn implements PlatformResAPI {
	
	private static final Logger logger = LogManager.getLogger(PlatformResController.class);
	
	@Autowired
	private PlatformResService platformResService;
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/addPlatformResource", method = RequestMethod.POST)
	public ReturnData addPlatformResource(HttpServletRequest request, HttpServletResponse response, 
			@RequestBody PlatformResourceVO platformResource, @RequestParam("access_token")String access_token) {
		try {
			if (StringUtil.isNull(platformResource.getDevName())) {
				return super.returnError("平台名称不能为空");
			}
			
			return this.platformResService.addPlatformResource(platformResource, access_token);
		} catch (BusinessException e) {
			logger.error("PlatformResController ===== addPlatformResource ===== 添加平台资源异常!!!", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("PlatformResController ===== addPlatformResource ===== 添加平台资源异常!!!", e);
			return super.returnError("添加平台资源异常");
		}
	}
	
	/**
	 * 平台资源添加
	 * @param request
	 * @param response
	 * @param appDeviceVO
	 * @return
	 */
	/*@RequestMapping(value="/addPlatformResource",method=RequestMethod.POST)
	public ResponseInfo<?> addPlatformResource(HttpServletRequest request,HttpServletResponse response,
			@RequestBody PlatformResourceVO platformResVO, @RequestParam("access_token")String access_token) {
		ResponseInfo<Object> responseInfo = new ResponseInfo<Object>();
		try {
			String devName = platformResVO.getDevName();
			if(StringUtil.isNull(devName)) {
				//平台名称不能为空
				responseInfo.setErrcode(1);
				responseInfo.setErrmsg("平台名称不能为空");
				return responseInfo;
			}
			platformResVO.setId(UUID.randomUUID().toString().replace("-", ""));
			
			platformResVO.setCreateid("admin");
			platformResVO.setCreateName("admin");
			platformResVO.setCreatetime(new Date());
			
			ReturnData user = userService.getUser(access_token);
			if(user.getErrcode() == 0) {
				
				Map<String,Object> userMap = (Map<String,Object>)((Map<String,Object>)user.getData()).get("extra");
				platformResVO.setCreateid((String)userMap.get("uuid"));
				platformResVO.setCreateName((String)userMap.get("loginName"));
				platformResVO.setCreatetime(DateUtil.date2String(new Date()));
			} else {
				//返回获取用户错误信息
				responseInfo.setErrcode(1);
				responseInfo.setErrmsg(user.getErrmsg());
				return responseInfo;
			}
						
			platformResService.insertPlatformResource(platformResVO);
			responseInfo.setErrcode(0);
			responseInfo.setErrmsg("添加平台资源成功");
		} catch(RuntimeException e) {
			logger.error("添加平台资源异常", e);
			responseInfo.setErrcode(1);
			responseInfo.setErrmsg(e.getMessage());
		} catch(Exception e) {
			logger.error("添加平台资源异常", e);
			responseInfo.setErrcode(1);
			responseInfo.setErrmsg("添加平台资源异常");
		}
		return responseInfo;
	}*/
	
	/**
	 * 通过主键ID获取平台信息
	 */
	@Override
	public ResponseInfo<DataInfo<PlatformResourceVO>> getPlatformResource(@PathVariable("id") String id) {
		ResponseInfo<DataInfo<PlatformResourceVO>> resInfo = new ResponseInfo<DataInfo<PlatformResourceVO>>();
		DataInfo<PlatformResourceVO> dataInfo = new DataInfo<PlatformResourceVO>();
		List<PlatformResourceVO> platformResourceList = new ArrayList<>();
		try {
			PlatformResourceVO platformResource = this.platformResService.getPlatformResourceById(id);
			
			if (platformResource != null) {
				platformResourceList.add(platformResource);
			}
			
			dataInfo.setItems(platformResourceList);
			resInfo.setErrcode(0);
			resInfo.setErrmsg("查询平台信息成功");
			resInfo.setData(dataInfo);
		} catch (Exception e) {
			logger.error("通过主键ID获取平台信息失败 =====>", e);
			resInfo.setErrcode(1);
			resInfo.setErrmsg("查询平台信息异常");
		}
		
		
		return resInfo;
	}   
	
	/**
	 * 查询平台资源
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/getPlatformResource",method=RequestMethod.GET)
	public ResponseInfo<DataInfo<PlatformResourceVO>> getPlatformResource(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(value="devName",required=false) String devName,
			@RequestParam(value="devType",required=false) String devType,
			@RequestParam(value="startTime",required=false) String startTime,
			@RequestParam(value="endTime",required=false) String endTime,
			@RequestParam(value="pageSize",defaultValue="15") Integer pageSize,
			@RequestParam(value="pageNum",defaultValue="1") Integer pageNum
		) {
		ResponseInfo<DataInfo<PlatformResourceVO>> resInfo = new ResponseInfo<DataInfo<PlatformResourceVO>>();
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("devName", devName);
			paramMap.put("devType", devType==null?"":devType.split(","));
			paramMap.put("startTime",startTime);
			paramMap.put("endTime", endTime);
			paramMap.put("pageSize", pageSize);
			paramMap.put("pageNum", pageNum);
			List<PlatformResourceVO> platformResourceList = platformResService.getPlatformResource(paramMap);
			
			if(platformResourceList.size() > 0) {
				
				DataInfo<PlatformResourceVO> dataInfo = new DataInfo<PlatformResourceVO>();
				dataInfo.setItems(platformResourceList);
				
				if(pageNum != -1) {
					//分页的属性
					Page<PlatformResourceVO> page = (Page<PlatformResourceVO>)platformResourceList;
					HashMap<String,Object> pageMap = new HashMap<String,Object>();
					pageMap.put("pageNum", page.getPageNum());
					pageMap.put("pageSize", page.getPageSize());
					pageMap.put("total", page.getTotal());
					pageMap.put("pages", page.getPages());
					dataInfo.setExtra(pageMap);
				}
				
				resInfo.setErrcode(0);
				resInfo.setErrmsg("查询平台信息成功");
				resInfo.setData(dataInfo);
			} else {
				resInfo.setErrcode(1);
				resInfo.setErrmsg("没有符合条件的平台信息");
			}
		}catch(Exception e) {
			logger.error("查询平台信息失败", e);
			resInfo.setErrcode(1);
			resInfo.setErrmsg("查询平台信息异常");
		}
		return resInfo;
	}
	
	/**
	 * 修改平台资源信息
	 * @param request
	 * @param response
	 * @param appDeviceVO
	 * @return
	 */
	@RequestMapping(value="/updatePlatformResource")
	public Map<String,Object> updatePlatformResource(HttpServletRequest request,HttpServletResponse response,
			@RequestBody PlatformResourceVO platformResourceVO,@RequestParam("access_token")String access_token) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			
			ReturnData user = userService.getUser(access_token);
			if(user.getErrcode() == 0) {
				Map<String,Object> userMap = (Map<String,Object>)((Map<String,Object>)user.getData()).get("extra");
				platformResourceVO.setUpdateid((String)userMap.get("uuid"));
				platformResourceVO.setUpdateName((String)userMap.get("loginName"));
				platformResourceVO.setUpdatetime(DateUtil.date2String(new Date()));
			} else {
				//返回获取用户错误信息
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", user.getErrmsg());
				return resultMap;
			}
			
			resultMap = platformResService.updatePlatformResource(platformResourceVO);
		} catch(RuntimeException e) {
			logger.error("添加平台资源异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", e.getMessage());
		} catch(Exception e) {
			logger.error("修改平台信息异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "修改平台资源异常");
		}
		return resultMap;
	}
	
	@RequestMapping(value="/{id}/deletePlatformResource")
	public Map<String,Object> deletePlatformResource(HttpServletRequest request,HttpServletResponse response,
			@PathVariable String id) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			platformResService.deletePlatformResource(id);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "删除平台成功");
		} catch(Exception e) {
			logger.error("删除平台异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "删除平台异常");
		}
		return resultMap;
	}
	
	@RequestMapping(value="/connect.do")
	public Map<String,Object> connect(HttpServletRequest request,HttpServletResponse resposne,
			@RequestParam(value="url",required = true) String url) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		HttpURLConnection conn = null;
		try {
			if ("https".equals(url.substring(0, 5))) {
				try {
					TrustManager[] tms = {new X509TrustManager() {
						@Override
						public void checkClientTrusted(X509Certificate certificates[], String authType) {
						}

						@Override
						public void checkServerTrusted(X509Certificate[] ax509certificate, String s) {
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}
					}};
					SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
					sslContext.init(null, tms, new SecureRandom());
					//从上述SSLContext对象中得到SSLSocketFactory对象

					HostnameVerifier ignoreHostnameVerifier = (s, sslsession) -> {
						System.out.println("WARNING: Hostname is not matched for cert.");
						logger.error("访问HTTPS远端服务器失败: 主机名验证失败");
						return true;
					};
					HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
					HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

				} catch (Exception e) {
					logger.error("访问HTTPS远端服务器失败: ", e);
				}
			}
			URL urlObj = new URL(url);
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setUseCaches(false);
			conn.setConnectTimeout(3000); // 设置超时时间
			int status = conn.getResponseCode();// 请求状态
		    if(status == 200){ 
		    	resultMap.put("errcode", 0);
    			resultMap.put("errmsg", "连接成功");
		    }else{  
		    	resultMap.put("errcode", 1);
    			resultMap.put("errmsg", "连接失败");
		    } 
		}catch(Exception e) {
			logger.error("连接异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "连接失败");
		}finally {
			conn.disconnect();
		}
		return resultMap;
	}
	
	
	//只需要加上下面这段即可，注意不能忘记注解  
    @InitBinder  
    public void initBinder(WebDataBinder binder, WebRequest request) {  
        //转换日期  
        DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");  
        // CustomDateEditor为自定义日期编辑器
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));  
    }
}
