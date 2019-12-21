package com.visionvera.service.impl;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.analysis.SlwYwMaxVO;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.ywcore.RemoteReportVO;
import com.visionvera.bean.ywcore.SetProvRateVO;
import com.visionvera.bean.ywcore.SetTerminalRateVO;
import com.visionvera.dao.ywcore.*;
import com.visionvera.exception.BusinessException;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.visionvera.constrant.WsConstants;
import com.visionvera.service.SenseService;

/**
 * 感知中心的数据业务
 *
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class SenseServiceImpl implements SenseService {
	private final Logger logger= LoggerFactory.getLogger(SenseServiceImpl.class);
	@Autowired
	private SenseDao senseDao;
	@Autowired
	private SetProvRateDao setProvRateDao;
	@Autowired
	private SetTerminalRateDao setTerminalRateDao;


	@Autowired
	private RemoteReportDao remoteReportDao;
	/** 最新速率的标志: now */
	private static final String CURRENT_RATE_FLAG = "now";
	/**
     * @Description: 获取某视联网服务器的流量趋势接口
     * @param @param mac
     * @param @param minute
     * @param @return
     * @return Map<String,Object>
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
	@Override
	public Map<String, Object> getServerRateTrend(String mac, Integer minute,String ip) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("minute", minute);
			paramsMap.put("mac", mac);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getServerRateTrend(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	/**
     * @Description: 获取某省的流量趋势接口
     * @param @param regionId
     * @param @param ip
     * @param @param minute
     * @param @return
     * @return Map<String,Object>
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
	@Override
	public Map<String, Object> getProvinceRateTrend(String regionId, String ip, Integer minute) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("minute", minute);
			paramsMap.put("regionId", regionId);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getProvinceRateTrend(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getProvinceRateTrendByProcedure(Integer hour,String ip,String regionId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("hour", hour);
			paramsMap.put("regionId", regionId);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getProvinceRateTrendByHour(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	/**
	 * 感知中心获取链路详情接口
	 *
	 * @param regionId 行政区域ID
	 * @return
	 */
	/**
	 * 感知中心获取链路详情接口
	 * @param regionId 行政区域ID
	 * @return
	 */
	@Override
	@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
	public ReturnData getLinkDetails(String regionId) {
		BaseReturn returnData = new BaseReturn();
		SetProvRateVO setProvRate = new SetProvRateVO();
		SetTerminalRateVO setTerminalRateVO = new SetTerminalRateVO();
		setProvRate.setRegionId(regionId);//通过行政区域查询
		setProvRate.setLife(CURRENT_RATE_FLAG);//查询最新的数据

		setTerminalRateVO.setRegionId(regionId);//通过源省份或目的省份行政区域ID查询
		setTerminalRateVO.setLife(CURRENT_RATE_FLAG);//查询最新的数据
		List<SetTerminalRateVO> terminalRateList = this.setTerminalRateDao.selectSetTerminalRateByCondition(setTerminalRateVO);
		if (terminalRateList != null && terminalRateList.size() > 0) {
			for (SetTerminalRateVO terminalRate : terminalRateList) {
				if("4".equals(terminalRate.getBusinessType())) {
					if (regionId.substring(0,2).equals(terminalRate.getFromprovid())) {//上行
						terminalRate.setUpAllRate(terminalRate.getAllRate());
						terminalRate.setDownAllRate("0");
					}else if(regionId.substring(0,2).equals(terminalRate.getToprovid())){
						terminalRate.setUpAllRate("0");
						terminalRate.setDownAllRate(terminalRate.getAllRate());
					}
				}else{
					if ("up".equals(terminalRate.getDirection())) {//上行
						terminalRate.setUpAllRate(terminalRate.getAllRate());
						terminalRate.setDownAllRate("0");
					}
					if ("down".equals(terminalRate.getDirection())) {//下行
						terminalRate.setUpAllRate("0");
						terminalRate.setDownAllRate(terminalRate.getAllRate());
					}
				}
			}
		}

		List<SetProvRateVO> currentRateList  = this.setProvRateDao.selectRateByRegionId(setProvRate);//通过行政区域ID查询最新的上下行带宽
		if (currentRateList == null || currentRateList.size() <= 0) {//空值处理
			setProvRate.setRegionId("0");
			setProvRate.setLife(null);
			setProvRate.setProv("");
			setProvRate.setDownAllRate("0");
			setProvRate.setUpAllRate("0");
			currentRateList.add(setProvRate);
		}
		return returnData.returnResult(0, "获取成功", null, terminalRateList, currentRateList.get(0));
	}

	@Override
	public Map<String, Object> getServerRateTrendByProcedure(Integer hour,String ip,String mac) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try{
			paramsMap.put("hour", hour);
			paramsMap.put("mac", mac);
			paramsMap.put("ip", ip);
			//根据开始时间去数据库查询对应的数据
			List<Map<String, String>> datas = senseDao.getServerRateTrendByHour(paramsMap);
			resultMap.put("items", datas);
			resultMap.put("errcode", WsConstants.OK);
			resultMap.put("errmsg", "获取数据成功");
		}catch(Exception e){
			resultMap.put("errcode", WsConstants.ERROR);
			resultMap.put("errmsg", "获取数据失败");
		}
		return resultMap;
	}

	/**
	 * 获取抓包机列表
	 * @param remoteReport 抓包机设备状态查询条件
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	@Override
	public PageInfo<RemoteReportVO> getGrapMachineList(RemoteReportVO remoteReport, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		if (StringUtil.isNull(remoteReport.getCapture())) {//不是通过抓包机的唯一标识查询
			if ("000000000000".equals(remoteReport.getRealRegionId())) {//全国数据
				remoteReport.setRealRegionId(null);
				remoteReport.setRow(null);
			} else {
				if ("1".equals(remoteReport.getGradeId())) {//一级行政区域
					remoteReport.setRealRegionId(remoteReport.getRealRegionId().substring(0, 2));
				} else if ("2".equals(remoteReport.getGradeId())) {//二级行政区域
					remoteReport.setRealRegionId(remoteReport.getRealRegionId().substring(0, 4));
				} else {
					throw new BusinessException("请输入正确的行政区域级别");
				}
			}
		} else {//通过抓包机的唯一标识查询
			remoteReport.setRealRegionId(null);
		}
		List<RemoteReportVO> remoteReportList = new ArrayList<RemoteReportVO>();//抓包机列表
		remoteReportList = this.remoteReportDao.selectRemoteReportByCondition(remoteReport);

		if (remoteReportList != null && remoteReportList.size() > 0) {
			for (RemoteReportVO remoteReportVO : remoteReportList) {
				String report = remoteReportVO.getReport();
				if (StringUtil.isNotNull(report)) {
					JSONObject reportJsonObject = JSONObject.parseObject(report, JSONObject.class);
					remoteReportVO.setReportJsonObject(reportJsonObject);
					remoteReportVO.setReport(null);
				}
			}
		}
		PageInfo<RemoteReportVO> remoteReportInfo = new PageInfo<RemoteReportVO>(remoteReportList);
		return remoteReportInfo;
	}

	/**
	 * 获取抓包机树的节点：包含行政区域节点和抓包机节点
	 * @param regionId 行政区域ID
	 * @param gradeId 级别
	 * @return
	 */
	@Override
	public ReturnData getGrapMachineTree(String regionId, RemoteReportVO remoteReport,String gradeId) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> totalMap = new HashMap<String, Object>();//包含行政区域List和抓包机节点List
		Set<String> distinctRegionSet = new TreeSet<String>();//保存去重后的一级行政区域的ID
		List<String> regionParamsList = new ArrayList<String>();//通过一级行政区域ID查询行政区域信息的List
		Set<RegionVO> existRegioSet = new LinkedHashSet<>();// 有序的Set集合,存放左侧行政区域树
		List<RemoteReportVO> remoteReportNodeList =  new ArrayList<>();//树的抓包机列表
		List<RegionVO> regionRootList = new ArrayList<>();//树的根行政区域节点List
		List<RemoteReportVO> machineList = new ArrayList<RemoteReportVO>();
		totalMap.put("regionList", regionRootList);
		totalMap.put("machineList", machineList);

		//查询树行政区域列表 Start
		//只查询省级行政区域 Start
		if (gradeId.equals("0")) {
			//查询所有一级行政区域，即树的根节点
			//查询已经存在的行政区域 Start
			List<RemoteReportVO> exitGrapMachineRegionList = this.remoteReportDao.selectExistGrapMachineRegionList(remoteReport);//查询已经存在的抓包机对应的行政区域ID
			if (exitGrapMachineRegionList == null || exitGrapMachineRegionList.size() <= 0) {
				logger.info("StatisticsServiceImpl ===== getGrapMachineTreeRoot ===== 没有经存在的抓包机对应的行政区域ID，即根节点为空");
				return returnData.returnResult(0, "获取成功", null, null, totalMap);
			}
			//查询已经存在的行政区域 End
			for (RemoteReportVO remoteReportvo : exitGrapMachineRegionList) {
				String regionIdParam = remoteReportvo.getRealRegionId().substring(0, 2) + "0000000000";//拼成一级行政区域
				distinctRegionSet.add(regionIdParam);
			}
			regionParamsList.addAll(distinctRegionSet);
			regionRootList = this.remoteReportDao.selectRegionBatch(regionParamsList);//批量查询行政区域信息

			if (regionRootList != null && regionRootList.size() > 0) {//根据行政区域ID查询抓包机的在线数和离线数
				for (RegionVO region : regionRootList) {
					if(remoteReport==null){
						remoteReport = new RemoteReportVO();
					}
					remoteReport.setRealRegionId(region.getId().substring(0, 2));
					Map<String, Long> countMap = remoteReportDao.selectGrapMachineCountByCondition(remoteReport);//查询该行政区域下所有的抓包机数量
					region.setServerCount(Long.parseLong(countMap.get("count")+""));//抓包机数量
					region.setServerOnlineCount(Long.parseLong(countMap.get("online")+""));//所有在线的抓包机数量
				}
			}

			totalMap.put("regionList", regionRootList);
			totalMap.put("machineList", machineList);
			return returnData.returnResult(0, "获取成功", null, null, totalMap);
		}
		//只查询省级行政区域 End

		//查询二级节点行政区域 Start
		String regionIdSecond = regionId.substring(0, 2);//Like查询前两位
		List<RegionVO> existRegioList = this.remoteReportDao.selectExistRegionByRegionIdWithoutOneLevel(regionIdSecond);//查询已经存在的抓包机所在的行政区域列表

		if(existRegioList != null && existRegioList.size() > 0) {//如果存在抓包机对应的行政区域,那么查询其对应的二级行政区域
			for (RegionVO region : existRegioList) {//查询其对应的二级行政区域列表
				//查询对应的二级行政区域 Start
				region.setId(region.getId().substring(0, 4));
				region.setGradeId(2);
				List<RegionVO> secondRegionList = this.remoteReportDao.selectRegionLikeRegionId(region);
				existRegioSet.addAll(secondRegionList);
				//查询对应的二级行政区域 End
			}
		}

		//查询抓包机对应的总数和在线数 Start
		if (existRegioSet != null && existRegioSet.size() > 0) {
			for (RegionVO region : existRegioSet) {//查询抓包机对应的数量
				if(remoteReport==null){
					remoteReport = new RemoteReportVO();
				}
				remoteReport.setRealRegionId(region.getId().substring(0, 4));
				Map<String, Long> countMap = remoteReportDao.selectGrapMachineCountByCondition(remoteReport);//查询该行政区域下所有的抓包机数量
				region.setServerCount(Long.parseLong(countMap.get("count")+""));//抓包机数量
				region.setServerOnlineCount(Long.parseLong(countMap.get("online")==null?"0":countMap.get("online")+""));//所有在线的抓包机数量
			}
		}
		//查询抓包机对应的总数和在线数 End

		totalMap.put("regionList", existRegioSet);
		//查询二级节点行政区域 End
		//查询树行政区域列表 End


		//查询树的抓包机列表 Start
		String regionIdGrapMachine = regionId.substring(0, 4);//Like查询前四位,即到市级
		remoteReportNodeList = this.remoteReportDao.selectRemoteReportNodeByRegionIdWithoutBlob(regionIdGrapMachine);
		totalMap.put("machineList", remoteReportNodeList);
		//查询树的抓包机列表 End

		//只查询到2级 Start
		//2级树下没有行政区域，只有抓包机
		if (gradeId.equals("2")) {
			totalMap.put("regionList", new ArrayList<>());
		}
		//只查询到2级 End
		return returnData.returnResult(0, "获取成功", null, null, totalMap);
	}

    @Override
    public ReturnData grapMachine(String regionId, String gradeId, RemoteReportVO remoteReport, Integer pageNum, Integer pageSize) {
		RemoteReportVO target = new RemoteReportVO();
		BeanUtils.copyProperties(remoteReport, target);
		if (StringUtil.isNull(target.getCapture())) {//不是通过抓包机的唯一标识查询
			if ("000000000000".equals(target.getRealRegionId())) {//全国数据
				target.setRealRegionId(null);
				target.setRow(null);
			} else {
				if ("1".equals(target.getGradeId())) {//一级行政区域
					target.setRealRegionId(target.getRealRegionId().substring(0, 2));
				} else if ("2".equals(target.getGradeId())) {//二级行政区域
					target.setRealRegionId(target.getRealRegionId().substring(0, 4));
				} else {
					throw new BusinessException("请输入正确的行政区域级别");
				}
			}
		} else {//通过抓包机的唯一标识查询
			target.setRealRegionId(null);
		}
		ReturnData grapMachineTree = getGrapMachineTree(regionId, target, gradeId);
		if(grapMachineTree.getErrcode()!=0){
			return grapMachineTree;
		}
		PageInfo<RemoteReportVO> grapMachineList = getGrapMachineList(remoteReport, pageNum, pageSize);
		JSONObject dataMap = new JSONObject();
		JSONObject resultMap =new JSONObject();
		Map<String,Object> extra = ((HashMap)((HashMap) grapMachineTree.getData()).get("extra"));
		resultMap.put("regions", extra.get("regionList"));
		resultMap.put("regionServer", extra.get("machineList"));
		resultMap.put("serverHardware",grapMachineList.getList());

		JSONObject extraMap = new JSONObject();
		extraMap.put("totalPage", grapMachineList.getPages());
		extraMap.put("pageNum", grapMachineList.getPageNum());
		extraMap.put("total", grapMachineList.getTotal());
		extraMap.put("pageSize", grapMachineList.getPageSize());
		dataMap.put("items", resultMap);
		dataMap.put("extra", extraMap);
		grapMachineTree.setData(dataMap);
		return grapMachineTree;
    }
}
