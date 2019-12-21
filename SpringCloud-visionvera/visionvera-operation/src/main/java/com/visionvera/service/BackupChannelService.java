package com.visionvera.service;

import java.util.List;
import java.util.Map;

import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.BackupChannel;

/**
 * <p>
 *  服务接口
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
public interface BackupChannelService extends BaseService<BackupChannel> {
	
	 /**
     * 批量配置备用通道
     * @param list
     * @return
     */
	public ReturnData addBatch(List<BackupChannel> list);
	
	 /**
     * 获取通道信息
     * @param 
     * @return
     */
	public ReturnData getServerChannel(BackupChannel params);
	 /**
     *  批量获取通道信息
     * @param list
     * @return
     */
	public ReturnData getServerChannelBatch(List<BackupChannel> list); 
	 /**
     * 备用通道重启/关闭终端
     * @param list
     * @return
     */
	public ReturnData operationter(Map<String,Object> params); 
	 /**
     * 获取备用通道地址
     * @param list
     * @return
     */
	public ReturnData getBackupChannelUrl(Map<String,Object> params); 
	 
	 

}
