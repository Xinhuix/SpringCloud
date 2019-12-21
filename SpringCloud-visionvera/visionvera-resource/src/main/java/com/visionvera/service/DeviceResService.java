package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.datacore.TTerminalInfoVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;


public interface DeviceResService {
	/**
	 * 获取用户的区域和设备信息
	 * @date 2018年7月9日 下午5:50:07
	 * @author wangqiubao
	 * @param regionId
	 * @param userId
	 * @param isDevnum
	 * @param dv
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public ResponseInfo<DataInfo<RegionVO>> getUserRegionDevices(String regionId,String userId,Integer isDevnum, DeviceVO dv,Integer pageNum, Integer pageSize);
	
	/**
	 * 获取设备信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	public PageInfo<TTerminalInfoVO> getDevices(boolean isPage, Integer pageNum, Integer pageSize);
}
