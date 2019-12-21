package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Serverinfob;
import com.visionvera.service.ServerinfobService;

/**
 * <p>
 * 前端控制类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@RestController
@RequestMapping("/rest/serverinfob")
public class ServerinfobController extends BaseReturn {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ServerinfobService serverinfobService;

	/**
	 * 查询服务器列表-分页
	 * 
	 * @author zhanghui
	 * @param serverName
	 * @param serverId
	 * @param serverMac
	 * @param serverStatus
	 * @param serverType
	 * @param omcid
	 * @param regionid
	 * @param gradeid
	 * @return ReturnData
	 */
	@RequestMapping("/getServers")
	public ReturnData getServers(@RequestParam(value = "serverName", required = false) String serverName,
			@RequestParam(value = "serverId", required = false) Integer serverId,
			@RequestParam(value = "serverMac", required = false) String serverMac,
			@RequestParam(value = "serverStatus", required = false) String serverStatus,
			@RequestParam(value = "serverType", required = false) String serverType,
			@RequestParam(value = "omcid", required = false) String omcid,
			@RequestParam(value = "regionid", required = false) String regionid,
			@RequestParam(value = "gradeid", required = false) String gradeid,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
			) {

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("serverName", serverName);
			params.put("serverId", serverId);
			params.put("serverMac", serverMac);
			params.put("serverStatus", serverStatus);
			params.put("serverType", serverType);
			params.put("omcid", omcid);
			params.put("regionid", regionid);
			params.put("gradeid", gradeid);
			params.put("pageNum", pageNum);
			params.put("pageSize", pageSize);
			ReturnData returnData = serverinfobService.getServers(params);
			return returnData;
	}

	/**
	 * 查询服务器详情
	 * 
	 * @author zhanghui
	 * @param serverId
	 * @param omcid
	 * @return ReturnData
	 */
	@RequestMapping("/getServersDetail")
	public ReturnData getServersDetail(@RequestParam(value = "omcid") String omcid,
			@RequestParam(value = "serverId") Integer serverId) {
		try {
			Serverinfob serverinfob = new Serverinfob();
			serverinfob.setOmcid(omcid);
			serverinfob.setId(serverId);
			Serverinfob result = serverinfobService.get(serverinfob);
			return super.returnResult(0, "获取成功", null, null, result);

		} catch (Exception e) {
			logger.error("ServerinfobController--getServersDetail", e);
		}
		return super.returnError("获取失败");

	}

}
