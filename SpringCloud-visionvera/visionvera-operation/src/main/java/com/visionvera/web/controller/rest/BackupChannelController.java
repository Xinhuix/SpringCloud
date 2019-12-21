package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.visionvera.bean.datacore.BackupChannel;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.BackupChannelService;
import com.visionvera.util.StringUtil;

/**
 * <p>
 *  前端控制类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@RestController
@RequestMapping("/rest/backupChannel")
public class BackupChannelController  extends BaseReturn {

private final Logger logger = LoggerFactory.getLogger(this.getClass());


  @Autowired
  private BackupChannelService backupChannelService;

  
        /**
         * 配置备用通道
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/addBackupChannel",method=RequestMethod.POST)
		public ReturnData addBackupChannel(@RequestBody BackupChannel backupChannel){
			try {
				String omcid =backupChannel.getOmcid();
				Integer id = backupChannel.getId();
				if(StringUtil.isNull(omcid)){
					return super.returnError("omcid不能为空");
				}
				if(id==null){
					return super.returnError("id不能为空");
				}
				BackupChannel params = new BackupChannel();
				params.setOmcid(omcid);
				params.setId(id);
				BackupChannel result =backupChannelService.get(params);
				int count =0;
				if(result==null){
					count = backupChannelService.save(backupChannel);
				}else{
					count = backupChannelService.update(backupChannel);
				}
				if(count>0){
					return super.returnSuccess("设置成功");
				}
			} catch (Exception e) {
				logger.error("BackupChannelController--addBackupChannel",e);
			}
			return super.returnError("设置失败");
		}
       
        /**
         * 批量配置备用通道
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/addBatch",method=RequestMethod.POST)
        public ReturnData addBatch(@RequestBody List<BackupChannel> list){
        	try {
        		ReturnData returnData =backupChannelService.addBatch(list);
        		return returnData;
        		
        	} catch (Exception e) {
        		logger.error("BackupChannelController--addBackupChannel",e);
        	}
        	return super.returnError("设置失败");
        }

        

        /**
         * 获取通道信息
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/getServerChannel")
        public ReturnData getServerChannel(@RequestParam(value = "omcid") String omcid,
    			@RequestParam(value = "serverId") Integer serverId){
        	try {
        		BackupChannel params = new BackupChannel();
        		params.setOmcid(omcid);
        		params.setId(serverId);
        		ReturnData returnData = backupChannelService.getServerChannel(params);
				return returnData;
			} catch (Exception e) {
				logger.error("BackupChannelController--getServerChannel",e);
			}
        	return super.returnError("获取失败");
        }
        /**
         * 批量获取通道信息
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/getServerChannelBatch",method=RequestMethod.POST)
        public ReturnData getServerChannelBatch(@RequestBody List<BackupChannel> list){
        	try {
        		ReturnData returnData = backupChannelService.getServerChannelBatch(list);
        		return returnData;
        	} catch (Exception e) {
        		logger.error("BackupChannelController--getServerChannel",e);
        	}
        	return super.returnError("获取失败");
        }
       
        /**
         * 通过备用通道重启/关闭终端
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/operationter")
        public ReturnData operationter(@RequestParam(value = "userid") String userid,
        		@RequestParam(value = "pwd") String pwd,
        		@RequestParam(value = "zoneno") String zoneno,
        		@RequestParam(value = "devno") String devno,
        		@RequestParam(value = "zonedevno") String zonedevno,
        		@RequestParam(value = "type") Integer type,
        		@RequestParam(value = "url") String url){
        	try {
        		Map<String,Object> params = new HashMap<String,Object>();
        		params.put("userid", userid);
        		params.put("pwd", pwd);
        		params.put("zoneno", zoneno);
        		params.put("devno", devno);
        		params.put("zonedevno", zonedevno);
        		params.put("type", type);
        		params.put("url", url);
        		try {
        			ReturnData returnData = backupChannelService.operationter(params);
        			return returnData;
				} catch (BusinessException e) {
					return super.returnError(e.getMessage());
				}
        		
        	} catch (Exception e) {
        		logger.error("BackupChannelController--operationter",e);
        	}
        	return super.returnError("操作失败");
        }
        /**
         * 根据终端号码获取备用通道地址
         * @author zhanghui
         * @param backupChannel
         * @return
         * ReturnData
         */
        @RequestMapping(value="/getBackupChannelUrl")
        public ReturnData getBackupChannelUrl(@RequestParam(value = "zonedevno") String zonedevno){
        	try {
        		if(zonedevno.length() !=8){
        			return super.returnError("请输入8位终端号码");
        		}
        		String zoneno = zonedevno.substring(0,2);
        		String devno =zonedevno.substring(3,zonedevno.length());
        		devno = devno.replaceAll("^(0+)", "");//去除设备号码前面补的0
        		
        		Map<String,Object> params = new HashMap<String,Object>();
        		params.put("zoneno", zoneno);
        		params.put("devno", devno);
        		ReturnData returnData = backupChannelService.getBackupChannelUrl(params);
        		return returnData;
        		
        	} catch (Exception e) {
        		logger.error("BackupChannelController--operationter",e);
        	}
        	return super.returnError("操作失败");
        }
        
        
        
        

}

