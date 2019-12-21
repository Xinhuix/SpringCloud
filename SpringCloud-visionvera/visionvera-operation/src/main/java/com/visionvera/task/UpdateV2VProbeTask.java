package com.visionvera.task;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.netty.NettyTcpClient;
import com.visionvera.service.PlatformService;
import com.visionvera.util.ProbeManagerMsgUtil;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时更新V2V监测探针版本
 * @author dql
 *
 */
@Component
public class UpdateV2VProbeTask {
	
	private Logger logger = LoggerFactory.getLogger(UpdateV2VProbeTask.class);

	@Autowired
	private PlatformService platformService;
	
	/**
	 * 发送信息给探针管理服务，获取最新探针版本
	 */
	@Scheduled(cron = "0 0 22 * * ?")
	public void updateV2vProbeInstallPac() {
		try {
			logger.info("定时获取V2V探针版本信息=======执行");
			
			boolean retFlag = ProbeManagerMsgUtil.getProbeRecentVersion("back", "");
			if(retFlag) {
				logger.info("向探针管理服务发送获取V2v探针版本信息成功");
			}else {
				logger.error("向探针管理服务发送获取V2V探针版本信息失败");
			}
			
		}catch(Exception e) {
			logger.error("定时查询V2v监测探针版本异常", e);
		}
	}

	@Scheduled(cron = "0 0 1 * * ?")
	public void synchroPlatformSuggestedVersion() {
		try {
			logger.info("定时更新平台推荐版本号=============");
			//当前只更新协转的版本号
			JSONObject dataJson = new JSONObject();
			dataJson.put("funcName","get_xz_version");
			dataJson.put("param","");
			//System.out.println(dataJson.toJSONString());
			String result = NettyTcpClient.sendMsg(ProbeManagerMsgUtil.probeManageIp,dataJson.toJSONString());
			if(ProbeManagerMsgUtil.getTcpResult(result)) {
				JSONObject resultJson = JSONObject.parseObject(result);
				String version = resultJson.getString("version");
				logger.info("==========获取协转最新版本号为=============" + version);
				if(StringUtil.isNotNull(version)) {
					int num = platformService.updatePlatformVersionByPlatformType(9,version);
					logger.info("更新协转版本号的平台数量=====" + num);
				}

			}else {
				logger.info("==========获取协转版本号结果=========" + result);
			}
		} catch(Exception e) {
			logger.error("定时更新平台推荐版本号异常",e);
		}
	}

}
