package com.visionvera.web.controller.rest;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.service.WatchProbeService;
import com.visionvera.util.OSUtils;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 检测探针远程操作接口
 * @author dql
 */
@RestController
@RequestMapping(value="/probe")
public class WatchProbeController extends BaseReturn{
	
	private Logger logger = LoggerFactory.getLogger(WatchProbeController.class);
	
	@Autowired
	private WatchProbeService watchProbeService;
	
	/**
	 * 根据条件查询已部署监测探针的所有版本
	 * @param serverOs	操作系统 Windows|Linux
	 * @param transferType  传输协议  IP|V2V
	 * @return
	 */
	@RequestMapping(value = "/{serverOs}/{transferType}/deployedVersion",method=RequestMethod.GET)
	public ReturnData getDeployedVersion(@PathVariable String serverOs,@PathVariable String transferType) {
		try {
			Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put("serverOs", serverOs);
			paramMap.put("transferType", transferType);
			
			return watchProbeService.getDeployedVersion(paramMap);
		}catch(Exception e) {
			logger.error("获取已部署探针版本异常", e);
			return super.returnError("获取已部署探针版本异常");
		}
	}

	@RequestMapping(value = "{transferType}/addvs",method=RequestMethod.GET)
	public ReturnData addvs(@RequestParam(value = "name") String name,@PathVariable String transferType) {
		try {
			Map<String,Object> paramMap = new HashMap<String, Object>();

			return super.returnError("获取已部署探针版本异常");
		}catch(Exception e) {
			logger.error("获取已部署探针版本异常", e);
			return super.returnError("获取已部署探针版本异常");
		}
	}


	/**
	 * 获取监测探针最新的安装版本
	 * @param transferType 传输协议 IP或者V2V
	 * @return
	 */
	@RequestMapping(value="/{transferType}/getRecentVersion",method=RequestMethod.GET)
	public ReturnData getRecentVersion(@PathVariable String transferType,@RequestParam(value="version",required=false)String version){
		try {
			if(OSUtils.isWindows()){
				return super.returnResult(0, "获取监测探针最新版本成功", null, null, "1.3.1");	
			}
			version = watchProbeService.getLocalRecentVersion(transferType);
			if(StringUtil.isNull(version)) {
				return super.returnError("没有获取到最新的监测探针版本");
			}
			return super.returnResult(0, "获取监测探针最新版本成功", null, null, version);
		} catch (Exception e) {
			logger.error("获取监测探针版本异常", e);
			return super.returnError("获取监测探针版本异常");
		}
	}
	
	/**
	 * 测试服务器远程登录
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/{id}/testRemoteLogin",method=RequestMethod.GET)
	public ReturnData testRemoteLogin(@PathVariable Integer id) {
		try {
			return watchProbeService.testRemoteLogin(id);
		} catch (Exception e) {
			logger.error("测试服务器远程登录异常", e);
			return super.returnError("测试服务器远程登录异常");
		}
	}
	
	
	/**
	 * 批量测试服务器远程登录
	 * @return
	 */
	/*@RequestMapping(value="/testLoginBatch",method=RequestMethod.POST)
	public ReturnData testRemoteLoginBatch(@RequestBody List<Integer> serverIdList) {
		try {
			ReturnData returnData = watchProbeService.testRemoteLoginBatch(serverIdList);
			return returnData;
		}catch(Exception e) {
			logger.error("测试服务器远程登录异常", e);
			return super.returnError("测试服务器远程登录异常");
		}
	}*/
	
	/**
	 * 远程部署监测探针
	 * @param id 服务器id
	 * @param version 版本号
	 * @return
	 */
	/*@RequestMapping(value="/{id}/{version}/displayProbe",method=RequestMethod.GET)
	public ReturnData displayWatchProbe(@PathVariable Integer id,@PathVariable String version) {
		try {
			ReturnData returnData =  watchProbeService.displayWatchProbe(id,version);
			return returnData;
		} catch(Exception e) {
			logger.error("部署监测探针异常", e);
			return super.returnError("部署监测探针异常");
		}
	}*/
	
	/**
	 * 开启监测探针
	 * @param id 服务器id
	 * @return
	 */
	@RequestMapping(value="/{id}/start",method=RequestMethod.GET)
	public ReturnData startWatchProbe(@PathVariable Integer id) {
		try {
			return watchProbeService.startWatchProbe(id);
		}catch(Exception e) {
			logger.error("启动监测探针异常", e);
			return super.returnError("启动监测探针异常,请检查探针是否在线和网络状态");
		}
	}
	
	/**
	 * 关闭监测探针
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/{id}/stop",method=RequestMethod.GET)
	public ReturnData stopWatchProbe(@PathVariable Integer id) {
		try {
			return watchProbeService.stopWatchProbe(id);
		}catch(Exception e) {
			logger.error("停止监测探针异常", e);
			return super.returnError("停止监测探针异常,请检查探针是否在线和网络状态");
		}
	}
	
	
	/**
	 * 移除服务器监测探针
	 * @param id
	 * @return
	 */
	/*@RequestMapping(value="/{id}/remove",method=RequestMethod.GET)
	public ReturnData removeWatchProbe(@PathVariable Integer id) {
		try {
			return watchProbeService.removeWatchProbe(id);
		} catch(Exception e) {
			logger.error("移除监测探针异常",e);
			return super.returnError("移除监测探针异常");
		}
	}*/
	
	/**
	 * 升级监测探针
	 * @param id 服务器id
	 * @param version 探针版本号
	 * @return
	 */
	/*@RequestMapping(value="/{serverId}/{version}/upgradeProbe")
	public ReturnData upGradeProbe(@PathVariable Integer serverId,@PathVariable String version) {
		try {
			return watchProbeService.upGradeWatchProbe(serverId,version);
		} catch(Exception e) {
			logger.error("升级监测探针异常", e);
			return super.returnError("升级监测探针异常");
		}
	}*/
	
	/**
	 * 下载监测探针安装包
	 * @param serverId 服务器Id
	 */
	@RequestMapping(value="/{serverId}/{version}/downLoadPackage",method=RequestMethod.GET)
	public void downLoadInstallPackage(@PathVariable Integer serverId,@PathVariable String version,
			HttpServletResponse response) {
		try {
			logger.info("下载检测探针安装包，服务器id"+serverId+"探针版本"+version);
			watchProbeService.downLoadInstallPackage(serverId,version,response);
		}catch(Exception e) {
			logger.error("下载监测探针安装包失败",e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 检测服务器探针安装状态
	 * @param serverId 服务器id
	 * @return
	 */
	@RequestMapping(value="/{serverId}/checkStatus",method=RequestMethod.GET)
	public ReturnData checkProbeStatus(@PathVariable Integer serverId) {
		try {
			return watchProbeService.checkProbeStatus(serverId);
		} catch (Exception e) {
			logger.error("探针状态异常",e);
			return super.returnError("监测探针状态异常");
		}
	}
	
	/**
	 * 接收v2v探针安装包
	 * @param type 接收最新的探针安装包，发起类型 web:前端  back:后台
	 * @return
	 */
	@RequestMapping(value="/uploadV2VProbe/{type}",method=RequestMethod.POST)
	public ReturnData receiveV2VProbe(@RequestParam("file") MultipartFile file, @PathVariable String type) {
		try {
			String filename = file.getOriginalFilename();
			logger.info("接收到的监测探针安装包文件名为：" + filename);
			watchProbeService.saveV2vProbeInstallPack(file,filename,type);
			return super.returnSuccess("保存探针安装包成功");
		} catch(Exception e) {
			logger.error("保存V2V探针安装包异常",e);
			return super.returnError("保存探针安装包异常");
		}
	}

}
