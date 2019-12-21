package com.visionvera.service.impl;

import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.datacore.MonitorVedioReport;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.perception.MonitorVedioReportDao;
import com.visionvera.service.MonitorVedioReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-11-21
 */
@Service
@Transactional(value = "transactionManager_perception", rollbackFor = Exception.class)
public class MonitorVedioReportServiceImpl extends CrudService<MonitorVedioReportDao, MonitorVedioReport>
		implements MonitorVedioReportService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MonitorVedioReportDao monitorVedioReportDao;




	/**
	 * 根据业务ID获取收看直播、监控、升级、点播终端信息
	 * @param businessId 发布直播、监控、升级、点播的业务ID
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getDeviceListByBusinessId(String businessId) {
		Map<String, Object> params = new HashMap<String, Object>();
		List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();;
		MonitorVedioReport ret = super.get(businessId);
		if(ret!=null){
			Integer businessType =ret.getBusinessType();
			String  msgNo = ret.getMsgNo();
			String  monitorId = ret.getMonitorId();
			Integer subBusinessType =this.addBusinessTypeRel(businessType);
			params.put("businessType", subBusinessType);
			params.put("msgNo", msgNo);
			params.put("monitorId", monitorId);
			params.put("virtualNo", ret.getVirtualNo());
			params.put("order", "desc");
			params.put("sidx", "start_time");
		    list = monitorVedioReportDao.getSubscribeList(params);
		    if(list.size()>0){
		        if(businessType!=1) {
                    list.get(0).put("scheduleName", ret.getDevName());
                }else{
					String devName = monitorVedioReportDao.selectDevNameByDevNoAndZoneNo(ret.getZoneno(), ret.getDevNo());
					list.get(0).put("scheduleName", devName);
				}
			}
		}
		return list;
	}



	/**
	 * 获取收看类型
	 * @param bussinessType
	 * @return
	 */
	private Integer addBusinessTypeRel(Integer bussinessType){
		if(bussinessType==GlobalConstants.BUSINESSTYPE_FBZB_PT){
			return GlobalConstants.BUSINESSTYPE_SKZB_PT;
		}
		if(bussinessType==GlobalConstants.BUSINESSTYPE_FBZB_JK){
			return GlobalConstants.BUSINESSTYPE_SKZB_JK;
		}
		if(bussinessType==GlobalConstants.BUSINESSTYPE_FBZB_SJ){
			return GlobalConstants.BUSINESSTYPE_SKZB_SJ;
		}
		return bussinessType;
	}
}
