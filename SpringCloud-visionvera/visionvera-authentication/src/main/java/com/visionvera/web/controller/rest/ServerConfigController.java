package com.visionvera.web.controller.rest;

import java.util.Date;

import com.visionvera.common.api.authentication.ServerConfigAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.service.ServerConfigService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.StringUtil;

@RestController
public class ServerConfigController extends BaseReturn implements ServerConfigAPI {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private ServerConfigService serverConfigService;


	/**
	 * 添加服务配置信息
	 * @param config 服务配置信息
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ReturnData configAdd(@RequestBody ServerConfig config) {
		try {
			/** 数据校验 Start */
			ReturnData dataReturn = this.checkServerConfigInfo(config);//校验用户的必填信息
			if (!dataReturn.getErrcode().equals(0)) {//用户必填信息校验失败
				return dataReturn;
			}
			int count =0;
			/*判断配置信息是否存在，不存在添加，存在则修改*/
			ServerConfig param = new ServerConfig();
			param.setOtherPlatformId(config.getOtherPlatformId());
			ServerConfig serverConfig =this.serverConfigService.get(param);
			config.setUpdateTime(DateUtil.date2String(new Date()));
			if(serverConfig==null){
				  count = this.serverConfigService.save(config);
			}else{
				config.setId(serverConfig.getId());
				count = this.serverConfigService.update(config);
			}
			if(count>0){
				return super.returnSuccess("添加服务配置信息成功");
			}

		} catch (Exception e) {
			this.LOGGER.error("添加服务配置信息失败,=====> ", e);
			return super.returnError("添加服务配置信息失败");
		}
		return super.returnError("添加服务配置信息失败");
	}

	/**
	 * 获取服务配置信息
	 * @param config 服务配置信息
	 * @return
	 */
	@Override
	public ReturnData getServerConfig(@RequestParam(value="otherPlatformId") String otherPlatformId ) {
		try {
			/** 数据校验 Start */
			if (StringUtil.isNull(otherPlatformId)) {
				return super.returnError("平台ID不能为空");
			}
			ServerConfig config =new ServerConfig();
			config.setOtherPlatformId(otherPlatformId);
			ServerConfig serverConfig =this.serverConfigService.get(config);
			if(serverConfig == null){
				return super.returnError("请先配置服务信息");
			}
			return super.returnResult(0,"获取服务配置信息成功",null,null,serverConfig);
		} catch (Exception e) {
			this.LOGGER.error("获取服务配置信息失败,=====> ", e);
			return super.returnError("获取服务配置信息失败");
		}
	}

	/**
	 * 校验用户的必填字段：登录名、密码、手机号
	 * @param config
	 * @return
	 */
	private ReturnData checkServerConfigInfo(ServerConfig config) {
		if (null == config) {
			return super.returnError("服务配置信息不能为空");
		}
		/*if (StringUtil.isNull(config.getLoginName())) {
			return super.returnError("账号不能为空");
		}
		if (StringUtil.isNull(config.getLoginPwd())) {
			return super.returnError("密码不能为空");
		}*/
		if (StringUtil.isNull(config.getIp())) {
			return super.returnError("IP不能为空");
		}
		if (StringUtil.isNull(String.valueOf(config.getPort()))) {
			return super.returnError("PORT不能为空");
		}
		/*if (StringUtil.isNull(config.getToken())) {
			return super.returnError("token不能为空");
		}
		if (StringUtil.isNull(config.getPlatformId())) {
			return super.returnError("平台ID不能为空");
		}*/
		return super.returnResult(0, "校验成功");
	}
}
