package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Nmsserver;

import java.util.Map;

import com.visionvera.basecrud.BaseService;

/**
 * <p>
 *  服务接口
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
public interface NmsserverService extends BaseService<Nmsserver> {
	
	/**
	 * 获取网管下行政区域列表
	 * @Description TODO
	 * @author zhanghui
	 * @return
	 * ReturnData
	 */
	public ReturnData getNmsServersRegionList(Map<String,Object> params); 
	
	/**
	 * 获取网管下行政区域和服务器列表
	 * @Description TODO
	 * @author zhanghui
	 * @return
	 * ReturnData
	 */
	public ReturnData getNmsServersAndRegionList(Map<String,Object> params);

}
