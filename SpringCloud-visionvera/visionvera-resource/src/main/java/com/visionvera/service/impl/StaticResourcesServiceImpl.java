package com.visionvera.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.ServerConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.resource.ServerConfigDao;
import com.visionvera.feign.UserService;
import com.visionvera.service.StaticResourcesService;
import com.visionvera.util.ReturnDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 静态资源服务层
 *
 */
@Service
@Transactional(transactionManager = "transactionManager_resource", rollbackFor = Exception.class)
public class StaticResourcesServiceImpl implements StaticResourcesService {
	@Autowired
	private ServerConfigDao serverConfigDao;

	@Autowired
	private UserService userService;
	
	/**
	 * 获取服务配置列表
	 * @param serverConfig 查询条件
	 * @param pageNum 页码
	 * @param pageSize 页大小，为-1表示不分页
	 * @return
	 */
	@Override
	public PageInfo<ServerConfig> getServerConfigList(ServerConfig serverConfig, Integer pageNum, Integer pageSize) {
		if (!pageSize.equals(-1)) {//分页
			PageHelper.startPage(pageNum, pageSize);
		}
		//查询服务配置信息，携带出平台信息
		List<ServerConfig> serverConfigWithPlatformList = this.serverConfigDao.selectServerConfigWithPlatform(serverConfig);
		PageInfo<ServerConfig> serverConfigInfo = new PageInfo<ServerConfig>(serverConfigWithPlatformList);
		return serverConfigInfo;
	}

	/**
	 * 视联汇获取服务列表，获取到服务列表以后下发该服务的token
	 * @param platformType 平台类别。8表示网管，9表示会易通，10表示一机一档
	 * @param token 访问标识
	 * @return
	 */
	@Override
	public ReturnData getServerListForAPP(String platformType, String token) {
		BaseReturn dataReturn = new BaseReturn();
		UserVO user = new UserVO();
		ReturnData userData = userService.getUser(token);//获取缓存用户信息(即登录的用户信息), 走Feign接口
		if (!userData.getErrcode().equals(0)) {
			return dataReturn.returnError(userData.getErrmsg());
		}
		user = ReturnDataUtil.getExtraJsonObject(userData, UserVO.class);
		List<ServerConfig> serverConfigList = this.serverConfigDao.selectServerConfigByUserIdAndPlatformType(user.getUuid(), platformType);
		return dataReturn.returnResult(WsConstants.OK, "获取成功", null, serverConfigList);
	}
}
