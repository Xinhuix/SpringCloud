package com.visionvera.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.BackupChannel;
import com.visionvera.bean.datacore.Serverinfob;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.datacore.BackupChannelDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.MeetService;
import com.visionvera.service.BackupChannelService;
import com.visionvera.service.ServerinfobService;
import com.visionvera.util.AesUtils;
import com.visionvera.util.StringUtil;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@Service("backupChannelService")
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class BackupChannelServiceImpl extends CrudService<BackupChannelDao, BackupChannel>
		implements BackupChannelService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ServerinfobService serverinfobService;
	@Autowired
	private MeetService meetService;

	@Override
	public ReturnData addBatch(List<BackupChannel> list) {
		BaseReturn baseReturn = new BaseReturn();
		int num = 0;
		try {
			if (list != null) {
				for (BackupChannel backupChannel : list) {
					
					String omcid =backupChannel.getOmcid();
					Integer id = backupChannel.getId();
					if(StringUtil.isNull(omcid)){
						return baseReturn.returnError("omcid不能为空");
					}
					if(id==null){
						return baseReturn.returnError("id不能为空");
					}
					BackupChannel params = new BackupChannel();
					params.setOmcid(omcid);
					params.setId(id);
					BackupChannel result =super.dao.get(params);
					int count =0;
					if(result==null){
						count =super.dao.insertSelective(backupChannel);
					}else{
						count = super.dao.updateByPrimaryKeySelective(backupChannel);
					}
					if (count > 0) {
						num++;
					}
				}
				if (num == list.size()) {
					return baseReturn.returnSuccess("设置成功");
				}
			}

		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl--addBatch", e);
		}
		return baseReturn.returnError("设置失败");
	}

	@Override
	public ReturnData getServerChannel(BackupChannel params) {
		BaseReturn baseReturn = new BaseReturn();
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			String omcid =params.getOmcid();
			Integer serverId =params.getId();
			resultMap.put("omcid", omcid);
			resultMap.put("serverId", serverId);
			BackupChannel BackupChannelResult =super.dao.get(params);
			Serverinfob entity = new Serverinfob();
			entity.setOmcid(omcid);
			entity.setId(serverId);
			Serverinfob serverinfobResult =serverinfobService.get(entity);
			resultMap.put("serverName", serverinfobResult==null?null:serverinfobResult.getName());
			resultMap.put("serverStatus", serverinfobResult==null?null:serverinfobResult.getOnline());
			resultMap.put("channelUrl", BackupChannelResult==null?null:BackupChannelResult.getUrl());
			return baseReturn.returnResult(0, "获取成功", null, null, resultMap);
			
		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl--getServerChannel",e);
		}
		return  baseReturn.returnError("获取失败");
	}

	@Override
	@SuppressWarnings("unchecked")
	public ReturnData getServerChannelBatch(List<BackupChannel> list) {
		BaseReturn baseReturn = new BaseReturn();
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		try {
			if(list!=null&&list.size()>0){
				for(BackupChannel backupChannel:list){
					
					ReturnData returnData =this.getServerChannel(backupChannel);
					resultList.add((Map)((Map)returnData.getData()).get("extra"));
				}
				
				return baseReturn.returnResult(0, "获取成功", null, resultList, null);
			}
			
			
		} catch (Exception e) {
		}
		
		return  baseReturn.returnError("获取失败");
	}

	@Override
	public ReturnData operationter(Map<String, Object> params) {
		BaseReturn resultData = new BaseReturn();
		String resultJsonObject = "";
		String operateDeviceUrl = "";
		String encryptParams = "";
		JSONObject extraJsonObject = null;
		try {
			String paramsString = JSONObject.toJSONString(params);
			logger.info(paramsString);
			encryptParams = AesUtils.encrypt(paramsString);
			logger.info(encryptParams);
		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl ===== operationter ===== ", e);
			throw new BusinessException("数据加密失败");
		}
		try {
			extraJsonObject = meetService.getServerConfig();
			if (extraJsonObject == null) {
				return resultData.returnError("获取网管配置信息失败");
			}
		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl ===== operationter ===== 获取网管配置信息失败", e);
			throw new BusinessException("获取网管配置信息失败");
		}

		try {
			// 网管重启/关闭终端业务接口地址
			operateDeviceUrl = String.format(WsConstants.URL_BACKUPCHANNEL_OPERATIONTER,
					extraJsonObject.getString("ip"), extraJsonObject.getString("port"));
			resultJsonObject = RestTemplateUtil.postForObject(operateDeviceUrl, encryptParams, String.class);
		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl ===== operationter ===== 调用网管备用通道重启/关闭终端业务接口失败", e);
			throw new BusinessException("连接网管第三方接口服务失败");
		}
		try {
			String resultString = AesUtils.decrypt(resultJsonObject);
			logger.debug("resultString===" + resultString);
			JSONObject resultJson = JSONObject.parseObject(resultString);
			Integer ret = (Integer) resultJson.get("ret");
			if (ret == 0) {
				return resultData.returnSuccess(resultJson.getString("msg"));
			} else {
				return resultData.returnError(resultJson.getString("msg"));
			}
		} catch (Exception e) {
			logger.info("BackupChannelServiceImpl ===== operationter =====");
		}

		return resultData.returnError("操作失败");
	}

	@Override
	public ReturnData getBackupChannelUrl(Map<String, Object> params) {
		BaseReturn resultData = new BaseReturn();
		try {
			BackupChannel backupChannel = super.dao.getBackupChannelBySelective(params);
			return resultData.returnResult(0, "获取成功",null,null,backupChannel);
			
		} catch (Exception e) {
			logger.error("BackupChannelServiceImpl === getBackupChannelUrl===",e);
		}
		return resultData.returnError("获取失败");
	}

}
