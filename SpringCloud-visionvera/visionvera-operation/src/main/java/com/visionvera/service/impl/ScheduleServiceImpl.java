package com.visionvera.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ConstDataVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleFormVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.operation.DeviceDao;
import com.visionvera.dao.operation.ScheduleDao;
import com.visionvera.dao.operation.SysConfigDao;
import com.visionvera.service.ScheduleService;
import com.visionvera.util.StringUtil;

@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class ScheduleServiceImpl implements ScheduleService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ScheduleDao scheduleDao;
	
	@Autowired
	private SysConfigDao sysConfigDao;
	
	@Autowired
	private DeviceDao deviceDao;
	
	/**
	 * Title: getScheduleList Description:
	 * 
	 * @param map
	 * @return
	 * @see com.visionvera.cms.service.ScheduleService#getScheduleList(java.util.Map)
	 */
	public PageInfo<ScheduleVO> getScheduleList(Map<String, Object> map) {
		PageHelper.startPage((Integer) map.get("pageNum"), (Integer) map.get("pageSize"));
		
		List<ScheduleVO> scheduleList = this.scheduleDao.getScheduleList(map);
		
		PageInfo<ScheduleVO> scheduleInfo = new PageInfo<ScheduleVO>(scheduleList);
		return scheduleInfo;
	}

	public int getScheduleListCount(Map<String, Object> map) {
		return scheduleDao.getScheduleListCount(map);
	}
	
	/**
	 * 
	 * TODO 获取会议审批详情
	 * @author 谢程算
	 * @date 2017年11月1日  
	 * @version 1.0.0 
	 * @param map
	 * @return
	 */
	public PageInfo<ScheduleVO> getAccessorDetail(Map<String, Object> map) {
		PageHelper.startPage((Integer) map.get("pageNum"), (Integer) map.get("pageSize"));
		
		List<ScheduleVO> scheduleList = this.scheduleDao.getAccessorDetail(map);
		PageInfo<ScheduleVO> scheduleInfo = new PageInfo<ScheduleVO>(scheduleList);
		
		ConstDataVO dataVO = new ConstDataVO();
	    dataVO.setValueType(4);
		//将每条数据的处理时间毫秒值转换
		for (ScheduleVO scheduleVO : scheduleList) {
			/*long date3 = new Date(scheduleVO.getAccessorTime()).getTime() -
					new Date(scheduleVO.getCreateTime()).getTime();   *///时间差的毫秒数 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			long createTime=0;
			long accssorTime=0;
			try {
				if(StringUtils.isNotBlank(scheduleVO.getCreateTime())){
					createTime = sdf.parse(scheduleVO.getCreateTime()).getTime();
				}
				if(StringUtils.isNotBlank(scheduleVO.getAccessorTime())){
					accssorTime = sdf.parse(scheduleVO.getAccessorTime()).getTime();
				}
				
			} catch (ParseException e) {
				LOGGER.error("时间转换失败：", e);
			}//毫秒
			long date3 = accssorTime-createTime;
			String timeDiff = "";
			//计算出相差天数  
		    int days = (int)Math.floor(date3/(24*3600*1000));
		    //计算出小时数  
		    long leave1=date3%(24*3600*1000);    //计算天数后剩余的毫秒数  
		    int hours=(int)Math.floor(leave1/(3600*1000));
		    //计算相差分钟数  
		    long leave2=leave1%(3600*1000);        //计算小时数后剩余的毫秒数  
		    int minutes=(int)Math.floor(leave2/(60*1000));  
		    //计算相差秒数  
		    long leave3=leave2%(60*1000);      //计算分钟数后剩余的毫秒数  
		    int seconds=(int)Math.round(leave3/1000);
		    if(days > 0){
		    	timeDiff += days + "天";
		    }
		    if(hours > 0){
		    	timeDiff += hours + "时";
		    }
		    if(minutes > 0){
		    	timeDiff += minutes + "分";
		    }
		    if(seconds > 0){
		    	timeDiff += seconds + "秒";
		    }
		    //获取系统审批超时时间的毫秒值，与每条数据的处理时间相对比，返回是否超时标志位
		    List<ConstDataVO> listApr= sysConfigDao.getAprTime(dataVO);
		    String aprTime = "";
		    String aprExcellent  = "";
		    for (ConstDataVO constDataVO : listApr) {
				if (constDataVO.getConstId().equals("approve_time_out")) {
					aprTime = constDataVO.getValue();
				}else if (constDataVO.getConstId().equals("approve_time_excellent")){
					aprExcellent = constDataVO.getValue();
				}
			}
		    //系统设置超时时间毫秒值
		    long date4  = Integer.valueOf(aprTime)*60*1000;//合格时间
		    long date5  = Integer.valueOf(aprExcellent)*60*1000;//优秀时间
		    if (date5>=date3) {//小于优秀时间
				scheduleVO.setTimeOut(0);
			}else if (date5<date3 && date3<=date4) {//大于优秀时间，小于合格时间
				scheduleVO.setTimeOut(1);
			}else if (date3>date4) {
				scheduleVO.setTimeOut(2);
			}
		    scheduleVO.setApproveTime(timeDiff);//超时时间
		    scheduleVO.setApproveExc(aprExcellent);
		    scheduleVO.setApproveQua(aprTime);
		}
		return scheduleInfo;
	}
	
	/**
	 * 获取数据所属平台列表
	 * @param isPage 是否分页
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	public PageInfo<RegionVO> getScheduleServers(boolean isPage, Integer pageNum, Integer pageSize) {
		if (isPage) {
			PageHelper.startPage(pageNum, pageSize);
		}
		
		List<RegionVO> regionList = this.scheduleDao.getScheduleServers();
		PageInfo<RegionVO> regionInfo = new PageInfo<RegionVO>(regionList);
		return regionInfo;
	}
	
	/**
	 * 删除预约会议
	 * @param uuids 预约会议ID，多个ID使用英文逗号","分隔
	 * @return
	 */
	public ReturnData delSchedule(String uuid) {
		BaseReturn returnData = new BaseReturn();
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		
		String[] uuids = uuid.split(",");
		
		paramsMap.put("uuids", uuids);
		int count = scheduleDao.isScheduleDeletable(paramsMap);// 执行中的预约数量
		if (count > 0) {// 执行中的预约不让删除
			return returnData.returnError("正在开会, 不允许删除");
		}
		
		int deleteScheduleCount = this.scheduleDao.deleteSchedule(paramsMap);//删除会议
		
		if (deleteScheduleCount <= 0) {
			this.LOGGER.error("删除会议失败, 操作结果数: " + deleteScheduleCount);
			return returnData.returnError("删除失败");
		}
		return returnData.returnResult(0, "删除成功");
	}
	
	/**
	 * 判断是否可以删除预约会议
	 * @param uuids 预约会议ID，多个ID使用英文逗号","分隔
	 * @return
	 */
	public int isOK2Delete(String uuid) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		String[] uuids = uuid.split(",");
		paramsMap.put("uuids", uuids);
		return scheduleDao.isScheduleDeletable(paramsMap);// 执行中的预约数量
	}

	@Override
	public List<ScheduleFormVO> getScheduleListByDate(Map<String, Object> paramsMap) {

		List<ScheduleFormVO> scheduleVoList = scheduleDao.getScheduleListByDate(paramsMap);

		if (scheduleVoList != null && scheduleVoList.size() > 0) {

			System.out.println(scheduleVoList);
			for (ScheduleFormVO scheduleVO : scheduleVoList) {
				String masterAddr = scheduleVO.getMasterAddr();
				if(StringUtil.isNull(masterAddr)) {
					String masterNo = scheduleVO.getChairman();//主席号码

					DeviceVO deviceVO = deviceDao.getDeviceById(masterNo);
					String regionId = deviceVO.getRegionId();
					//将行政区域编码后10位修改为0
					regionId = regionId.replaceAll("(\\d{2})\\d{10}", "$10000000000");
					RegionVO regionVO = deviceDao.getRegionById(regionId);
					masterAddr = GlobalConstants.REGION_MASTERNO_MAP.get(regionVO.getName());
					scheduleVO.setMasterAddr(masterAddr);
				}
			}
		}
		return scheduleVoList;
	}
	
	/**
	 * 获取会议详情（含通知）
	 * 
	 * @Title: getSchedule
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @return 参数说明
	 * @return map 返回类型
	 * @throws
	 */
	public List<ScheduleVO> getSchedule(Map<String, Object> map) {
		return scheduleDao.getSchedule(map);
	}
	
	/**
	 * 
	 * TODO 获取数据所属区域
	 * @author 周逸芳
	 * @date 2017年12月25日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	public RegionVO selectRegionId(ScheduleVO scheduleVO) {
		return scheduleDao.selectRegionId(scheduleVO);
	}



}
