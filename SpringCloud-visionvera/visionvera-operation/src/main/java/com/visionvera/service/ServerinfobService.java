package com.visionvera.service;

import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Serverinfob;

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
public interface ServerinfobService extends BaseService<Serverinfob> {
	
	/**
	 * 获取服务器列表-分页
	 * @author zhanghui
	 * @param params
	 * @return
	 * ReturnData
	 */
	public ReturnData getServers(Map<String,Object> params); 

}
