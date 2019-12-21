package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.datacore.TRegionb;
import com.visionvera.bean.datacore.TTerminalInfoVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;
import com.visionvera.common.api.resource.DeviceResAPI;
import com.visionvera.service.DeviceResService;
import com.visionvera.service.RegionService;

/**
 * 平台资源信息Controller
 * @author dql
 *
 */
@RestController
public class DeviceResController extends BaseReturn implements DeviceResAPI{
	
	private static final Logger logger = LogManager.getLogger(DeviceResController.class);
	
	@Resource
	private DeviceResService deviceResService;
	
	@Autowired
	private RegionService regionService;
	
	/**
	 * 获取用户的区域和设备信息
	 * @date 2018年7月2日 下午5:38:34
	 * @author wangqiubao
	 * @param pageNum 页码（从1开始，默认1）
	 * @param pageSize 页大小（默认5），值为-1时不分页
	 * @return
	 */
	@RequestMapping(value="/{regionId}/{userId}/getUserRegionDevices",method=RequestMethod.GET)
	public ResponseInfo<DataInfo<RegionVO>> getUserRegionDevices(
			DeviceVO dv,
			@PathVariable("regionId") String regionId,@PathVariable("userId") String userId, 
			@RequestParam(value="isDevnum",defaultValue="0") Integer isDevnum,//是否需要设备数量(0-否；1-是)
			@RequestParam(value="pageNum",defaultValue="1") Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="5") Integer pageSize){
		long startTime = System.currentTimeMillis() ;
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		ResponseInfo<DataInfo<RegionVO>> result = null ;
		try {
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			
			result = deviceResService.getUserRegionDevices(regionId, userId,isDevnum, dv, pageNum, pageSize) ;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取地区设备信息出错getUserRegionDevices",e);
		}
		long endTime = System.currentTimeMillis() ;
		logger.info("getUserRegionDevices|方法花费时间"+(endTime-startTime)/1000+" s|result="+JSONObject.toJSONString(result));
		return result ;
	}
	
	/**
	 * 获取行政区域。默认获取省级区域
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/getRegions", method = RequestMethod.GET)
	public ReturnData getRegions(@RequestParam(required = false, name = "pid") String pid) {
		try {
			TRegionb region = new TRegionb();
			region.setPid(pid == null ? "000000000000" : pid);;
			PageInfo<TRegionb> regionInfo = this.regionService.getRegionList(false, null, null, region);
			return super.returnResult(0, "获取成功", null, regionInfo.getList());
		} catch (Exception e) {
			logger.error("获取行政区域失败 ===== DeviceResController ===== getRegions =====>", e);
			return super.returnError("获取行政区域列表失败");
		}
	}
	
	/**
	 * 查询所有设备信息
	 * @param pageNum 页大小
	 * @param pageSize 页码。为-1时表示不分页
	 * @return
	 */
	@RequestMapping(value = "/getAllDevice", method = RequestMethod.GET)
	public ReturnData getAllDevice(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize) {
		PageInfo<TTerminalInfoVO> terminalInfo = new PageInfo<>();
		try {
			if (pageSize.equals(-1)) {
				terminalInfo = this.deviceResService.getDevices(false, pageNum, pageSize);
				return super.returnResult(0, "获取成功", null, terminalInfo.getList());
			}
			
			terminalInfo = this.deviceResService.getDevices(true, pageNum, pageSize);
			return super.returnResult(0, "获取成功", null, terminalInfo.getList());
		} catch (Exception e) {
			logger.error("获取所有设备信息失败 ===== DeviceResController ===== getAllDevice =====>", e);
			return super.returnError("获取所有设备信息失败");
		}
	}
}
