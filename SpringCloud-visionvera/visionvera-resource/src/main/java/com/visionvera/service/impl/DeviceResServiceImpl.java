package com.visionvera.service.impl;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.datacore.TTerminalInfoVO;
import com.visionvera.bean.restful.DataInfo;
import com.visionvera.bean.restful.ResponseInfo;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.resource.DeviceResDao;
import com.visionvera.service.DeviceResService;

@Service
@Transactional(value = "transactionManager_resource", rollbackFor = Exception.class)
public class DeviceResServiceImpl implements DeviceResService {
	@Autowired
	private DeviceResDao deviceResDao;
	private static final Logger logger = LogManager.getLogger(DeviceResServiceImpl.class);
	@Override
	public ResponseInfo<DataInfo<RegionVO>> getUserRegionDevices(String regionId, String userId,Integer isDevnum,
			DeviceVO dv, Integer pageNum, Integer pageSize) {
		ResponseInfo<DataInfo<RegionVO>> result = new ResponseInfo<DataInfo<RegionVO>>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		HashMap<String, Object> extra = new HashMap<String, Object>();
		DataInfo<RegionVO> data = new DataInfo<RegionVO>();
		try {
			if(StringUtils.isBlank(regionId)){
				result.setErrmsg("行政区域为空");
				result.setErrcode(WsConstants.ERROR);
				return result;
			}
			if(StringUtils.isBlank(userId)){
				result.setErrmsg("用户为空");
				return result;
			}
			/********拼接参数********/
			paramsMap.put("userId", userId);//用户ID
			if(dv != null){
				paramsMap.put("id", dv.getId());//设备ID
				paramsMap.put("typeId", dv.getTypeId());//设备类型ID
				paramsMap.put("dataType", dv.getDataType());//数据类型：1默认，2私有
				paramsMap.put("isDevnum", isDevnum) ;//是否需要设备数量(0-否；1-是)
				if(StringUtils.isNotBlank(dv.getName())){
					paramsMap.put("name", URLDecoder.decode(dv.getName(), "utf-8"));//设备名称
				}
			}
			paramsMap.put("pId", regionId);
			
			/********获取行政区域下所有有设备的行政区域********/
			logger.debug("获取行政区域下所有有设备的行政区域|param="+paramsMap);
			List<RegionVO> regionList = this.getChildByRegionId(paramsMap);
			paramsMap.remove("pId");
			
			/********获取行政区域节点的设备（跟用户关联）********/
			paramsMap.put("regionId", regionId);
			pageNum = (pageNum == null ? 1 : pageNum);
			pageSize = (pageSize == null ? 5 : pageSize);
			paramsMap.put("pageNum", pageSize * (pageNum - 1));
			paramsMap.put("pageSize", pageSize);
			List<DeviceVO> devlist = this.getUserDevices(paramsMap);
			if(pageSize != -1){//分页
				//获取当前用户下本区域下的设备数
				int total = deviceResDao.getDevicesCount(paramsMap);
				total = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;
				extra.put("pageTotal", total);
			}
			result.setErrcode(WsConstants.OK);
			result.setErrmsg("查询成功");
			extra.put("regionList", regionList);
			extra.put("devlist", devlist);
			data.setExtra(extra);
			result.setData(data);
		} catch (Exception e) {
			result.setErrcode(WsConstants.ERROR);
			result.setErrmsg("查询失败");
			logger.error("根据行政区域获取子行政区域及设备列表失败", e);
		}
		return result;
	
	}
	/**
	 * 获取regionID下的子节点
	 * @date 2018年7月5日 下午1:20:53
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	public List<RegionVO> getChildByRegionId(Map<String, Object> paramsMap) {
		List<RegionVO> list = deviceResDao.getChild(paramsMap);
		List<RegionVO> tempList = new ArrayList<RegionVO>();
		tempList.addAll(list);
		String regionId = null;
		int total = 0 ;
		for(RegionVO region : tempList){//去除没有设备的节点
			regionId = region.getId();
			String base = regionId;
			for(int i=regionId.length(); i>=0; i--){
				if(regionId.substring(i-1, i).equals("0")){
					base = base.substring(0,i-1);
				}else{
					break;
				}
			}
			if(base.length() % 2 != 0 && base.length() != 9){//长度为奇数时补零
				if (base.length() == 7) {
					base += "00";
				}else{
					base += "0";
				}
			}else if(base.length() == 10){
				base += "00";
			}else if(base.length() == 8){
				base += "0";
			}
			paramsMap.put("regionId", base);
			if (paramsMap.get("isDevnum")!=null && Integer.valueOf(paramsMap.get("isDevnum").toString())==1) {
				total = deviceResDao.getDevicesCount(paramsMap) ;
				region.setDevNum(total);
			}
			//获取指定区域的设备数
			/*total = deviceResDao.getDevicesCount(paramsMap) ;
			//获取用户所拥有的设备所在的行政区域，若该行政区域没有设备则剔除掉
			if(total < 1){
				list.remove(region);
			}else{
				//该区域下的设备数
				region.setDevNum(total);
			}*/
		}
		return list;
	}
	/**
	 * 获取用户下设备信息
	 * @date 2018年7月5日 下午1:23:06
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	public List<DeviceVO> getUserDevices(Map<String, Object> paramsMap) {
		if(paramsMap.get("pageSize") == null || Integer.parseInt(paramsMap.get("pageSize").toString()) == -1){
			return deviceResDao.getUserDevices(paramsMap, new RowBounds());
		}
		return deviceResDao.getUserDevices(paramsMap, new RowBounds((Integer)paramsMap.get("pageNum"),(Integer)paramsMap.get("pageSize")));
	}
	
	/**
	 * 获取设备信息
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	@Override
	public PageInfo<TTerminalInfoVO> getDevices(boolean isPage, Integer pageNum, Integer pageSize) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<TTerminalInfoVO> terminalInfoList = this.deviceResDao.selectTerminalInfo();
		PageInfo<TTerminalInfoVO> terminalInfo = new PageInfo<TTerminalInfoVO>(terminalInfoList);
		return terminalInfo;
	}
}
