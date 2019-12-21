/**
 * @Title: TestController.java
 * @Package com.visionvera.union.restController
 * @Description: TODO
 * @author 谢程算
 * @date 2018年5月30日
 */
package com.visionvera.controller;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.datacore.OperationalDevice;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.config.base.SysConfig;
import com.visionvera.constrant.WsConstants;
import com.visionvera.exception.BusinessException;
import com.visionvera.service.MeetService;
import com.visionvera.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * ClassName: TestController
 * @author xiechengsuan
 * @date 2018年5月30日
 */
@RequestMapping("/rest/meet")
@RestController
public class MeetController extends BaseReturn{
	private static final Logger logger = LogManager.getLogger(MeetController.class);
	@Autowired
	SysConfig sysConfig;

	@Autowired
	private MeetService meetService;

	/**
	 *
	 * 搜索设备列表(支持分页)
	 * @param token
	 * @param id 20位设备号码
	 * @param devNo 5位设备号码
	 * @param name 设备名称
	 * @param alias 别名
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @date 2018年6月1日
	 */
	@RequestMapping(value = "getDevInfo", method = RequestMethod.POST)
	public Map<String, Object> getDevInfo(@RequestParam(value = "access_token") String token,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "devNo", required = false) String devNo,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "alias", required = false) String alias,
			@RequestParam(value = "pageNum", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			if(id != null) {
				paramMap.put("id", id);
			}
			if(devNo != null) {
				paramMap.put("devNo", devNo);
			}
			if(name != null) {
				paramMap.put("name",URLEncoder.encode(name, "utf-8"));
			}
			if(alias != null) {
				paramMap.put("alias",URLEncoder.encode(alias, "utf-8"));
			}
			if(pageNum != null) {
				paramMap.put("pageNum", pageNum);
			}
	    	if(pageSize != null) {
				paramMap.put("pageSize", pageSize);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return RestClient.postUrl(sysConfig.getHgUrl()+String.format(WsConstants.URL_CMS_PSOT_DEVICE_GETDEVINFO),token, paramMap);

	}

	/**
	 * 获取正在进行的会议列表
	 * @param orderBy 根据那个字段排序。默认根据级别排序
	 * @param orderType 排序规则，默认为正向排序
	 * @param pageNum 页码
	 * @param pageSize 页大小，为-1时表示不分页
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @return
	 */
	@RequestMapping(value = "/{userId}/meetingList", method = RequestMethod.GET)
	public ReturnData getMeetingList(
			@RequestParam(value = "orderBy", defaultValue = "level") String orderBy,
			@RequestParam(value = "orderType", defaultValue = "asc") String orderType,
			@RequestParam(value = "keyWord", required = false) String keyWord,
			@RequestParam(value = "regionId", required = false) String regionId,
			@RequestParam(value = "level", required = false) String level,
			@RequestParam(value = "createType", required = false) String createType,
			@RequestParam(value = "bits", required = false) Boolean bits,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
			@RequestParam(value = "serverRegionIdLength", required = false, defaultValue = "2") Integer serverRegionIdLength) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try {
			//校验排序字段
			if ((!"level".equalsIgnoreCase(orderBy)) && (!"start_time".equalsIgnoreCase(orderBy)) && (!"devCount".equalsIgnoreCase(orderBy))) {
				return super.returnError("排序字段错误, 请重新输入");
			}

			paramsMap.put("orderBy", orderBy);//默认根据级别排序
			paramsMap.put("orderType", orderType);//默认升序
			paramsMap.put("keyWord", keyWord);//模糊查找关键字
			paramsMap.put("pageNum", pageNum);//页码
			paramsMap.put("pageSize", pageSize);//页大小
			paramsMap.put("regionId", regionId);//区域id
			paramsMap.put("bits", bits);//区域id
			if(StringUtil.isNotNull(level)) {
				paramsMap.put("level", level);//会议等级
			}
			if(StringUtil.isNotNull(createType)) {
				paramsMap.put("createType", createType);
			}
			paramsMap.put("time", "");

			return this.meetService.getMeetingInfo(paramsMap, serverRegionIdLength);//获取正在进行中的会议数据

		} catch (BusinessException e) {
			logger.error("MeetController ===== getMeetingList ===== 获取正在进行的会议列表失败 =====> ", e);
			return returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("MeetController ===== getMeetingList ===== 获取正在进行的会议列表失败 =====> ", e);
			return returnError("获取正在进行的会议列表失败");
		}
	}


	/**
	 * 获取会议的参会终端列表（给感知中心）
	 * @param scheduleId 会议ID
	 * @param pageNum 页码
	 * @param pageSize 页大小。为-1时表示不分页
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @return
	 */
	@RequestMapping(value = "{userId}/{scheduleId}/devList", method = RequestMethod.GET)
	public ReturnData getMeetingDevList(
			@PathVariable(value = "scheduleId") String scheduleId,
			@RequestParam(value = "keyWord", required = false) String keyWord,
			@RequestParam(value = "showType", defaultValue = "0") String showType,
			@RequestParam(value = "masterNo",defaultValue = "0") String masterNo,
            @RequestParam(value = "bits", required = false) Boolean bits,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(value = "serverRegionIdLength", required = false, defaultValue = "2") Integer serverRegionIdLength) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try {
			paramsMap.put("pageNum", pageNum);//页码
			paramsMap.put("pageSize", pageSize);//页大小
			paramsMap.put("scheduleId", scheduleId);//会议ID
			paramsMap.put("keyWord", keyWord);//模糊查找关键字
			paramsMap.put("showType", showType);//显示范围：0所有设备，1只看故障设备，默认0
			paramsMap.put("masterNo", masterNo);//主席号码
            paramsMap.put("bits", bits);//区域id

			return this.meetService.getMeetingDevList(paramsMap, serverRegionIdLength);//查询数据
		} catch (BusinessException e) {
			logger.error("MeetController ===== getMeetingDevList ===== 获取会议的参会终端列表失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("MeetController ===== getMeetingDevList ===== 获取会议的参会终端列表失败 =====> ", e);
			return super.returnError("获取会议的参会终端列表失败");
		}
	}


	/**
	 *
	 * @Title: getMeetList
	 * @Description: 根据会议ID查询会议操作日志（不分页）
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	@RequestMapping(value = "{scheduleId}/logList", method = RequestMethod.GET)
	public Map<String, Object> getLogList(@PathVariable(value = "scheduleId") String scheduleId) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put("errcode", 1);
		resultMap.put("errmsg", "系统内部异常");
		resultMap.put("access_token",null);
		resultMap.put("data",null);

		try {
			Map<String, Object> result = RestClient.postUrl(sysConfig.getHgUrl()+String.format(WsConstants.URL_CMS_MEET_LOGLIST,scheduleId), null, null);
			boolean state=(boolean)result.get("result");
			if(state==true){
				Map<String, Object> data = new LinkedHashMap<String, Object>();
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询会议操作日志成功");
				resultMap.put("access_token",null);
				data.put("items",result.get("list"));
				data.put("extra",null);
				resultMap.put("data",data);
			}
		} catch (Exception e) {
		}
		return resultMap;
	}

	/**
	 * 一键停会，包含子系统
	 * @param scheduleId 会议ID
	 * @param reason 停会原因
	 * @return
	 */
	@RequestMapping(value = "/{scheduleId}/stopMeetingWithSubSystem", method = RequestMethod.GET)
	public Map<String, Object> stopMeetingWithSubSystem(@PathVariable("scheduleId") String scheduleId,
														String reason,@RequestParam(value="stopStatus",required=false,defaultValue="3") String stopStatus,
														@RequestParam(value ="loginName")String loginName,
														@RequestParam(value ="loginPwd")String loginPwd,
														@RequestParam("access_token") String token) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		Map<String, Object> returnDataMap = new HashMap<String, Object>();

		try {
			if(StringUtil.isContainChinese(loginName)||StringUtil.isContainChinese(loginPwd)){
				returnDataMap.put("errcode", 1);
				returnDataMap.put("errmsg", "用户名或密码错误");
				return returnDataMap;
			}
			paramsMap.put("loginName", loginName);
			paramsMap.put("loginPwd", loginPwd);
			Map<String, Object> resultMap = RestClient.postUrl(this.sysConfig.getHgUrl() + WsConstants.URL_CMS_USER_LOGIN, token, paramsMap);
			if ((Integer)resultMap.get("errcode")!=0) {
				returnDataMap.put("errcode", 1);
				returnDataMap.put("errmsg", resultMap.get("errmsg"));
				return returnDataMap;
			}
			paramsMap.clear();
			paramsMap.put("scheduleId", scheduleId);
			paramsMap.put("reason", reason);
			resultMap = RestClient.postUrl(this.sysConfig.getHgUrl() + WsConstants.URL_CMS_STOP_MEETING_WITH_SUBSYSTEM, token, paramsMap);

//			 resultMap = RestClient.get(this.sysConfig.getHgUrl() + WsConstants.URL_CMS_STOP_MEETING_WITH_SUBSYSTEM, token, paramsMap);//调用会管一键停会接口

			if (!(Boolean)resultMap.get("result")) {
				returnDataMap.put("errcode", 1);
				returnDataMap.put("errmsg", resultMap.get("msg"));
				return returnDataMap;
			}
			ScheduleVO scheduleParams = new ScheduleVO();
			scheduleParams.setUuid(scheduleId);
			scheduleParams.setStopStatus(Integer.valueOf(stopStatus));
			meetService.updateScheduleByUuid(scheduleParams);
			returnDataMap.put("errcode", 0);
			returnDataMap.put("errmsg", "停会成功");
		} catch (Exception e) {
			logger.error("MeetController ===== stopMeetingWithSubSystem ===== 一键停会失败 =====> ", e);
			returnDataMap.put("errcode", 1);
			returnDataMap.put("errmsg", "系统错误");
		}



		return returnDataMap;
	}



    /**
     * 获取RabbitMQ登录配置
     * @return
     */
    @RequestMapping(value = "/schedule/getRabbitMQProrerties", method = RequestMethod.POST)
    public Map<String, Object> getRabbitMQProrerties(@RequestParam(value="access_token") String token) {
    	String url = sysConfig.getHgUrl() + String.format(WsConstants.URL_CMS_RABBITMQPRORERTIES_GET);
    	return RestClient.postUrl(url,null, null);
    }



	/**
	 * 获取会议的参会终端的区域列表（给感知中心）
	 * @param token 访问令牌
	 * @param scheduleId 会议ID
	 * @return
	 */
	@RequestMapping(value = "{userId}/{scheduleId}/devRegionList", method = RequestMethod.GET)
	public ReturnData getMeetingDevRegionList(@RequestParam(value = "access_token") String token,
			@PathVariable(value = "userId") String userId,
			@PathVariable(value = "scheduleId") String scheduleId) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();

		try {
			paramsMap.put("scheduleId", scheduleId);//会议ID

			return this.meetService.getMeetingDevRegionList(paramsMap);//查询数据
		} catch (BusinessException e) {
			logger.error("MeetController ===== getMeetingDevList ===== 获取会议的参会终端列表失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("MeetController ===== getMeetingDevList ===== 获取会议的参会终端列表失败 =====> ", e);
			return super.returnError("获取会议的参会终端列表失败");
		}
	}

    /**
	 * 重启/关闭终端
	 * @param operationalDevice 操作类型。1表示重启终端，2表示关闭终端
	 * @param operationalDevice
	 * @return
	 */
	@RequestMapping(value = "/operateDevice", method = RequestMethod.POST, consumes = "application/json;charset=utf-8")
	public ReturnData operateDevice(@RequestBody OperationalDevice operationalDevice) {
		try {
			if (StringUtil.isNull(operationalDevice.getUserid())) {
				return super.returnError("用户ID不能为空");
			}

			if (StringUtil.isNull(operationalDevice.getPwd())) {
				return super.returnError("用户密码不能为空");
			}

			if (StringUtil.isNull(operationalDevice.getZonedevno())) {
				return super.returnError("终端号码不能为空");
			}

			if (StringUtil.isNull(operationalDevice.getType())) {
				return super.returnError("操作类型不能为空");
			}

			if ((!"1".equals(operationalDevice.getType()) && (!"2".equals(operationalDevice.getType())))) {
				return super.returnError("请输入正确的操作类型");
			}
			return this.meetService.operateDevice(operationalDevice);
		} catch (BusinessException e) {
			logger.error("MeetController ===== operateDevice ===== 重启或关闭终端失败 =====> ", e);
			return super.returnError(e.getMessage());
		} catch (Exception e) {
			logger.error("MeetController ===== operateDevice ===== 重启或关闭终端失败 =====> ", e);
			return super.returnError("重启/关闭终端失败失败");
		}
	}



	/**
	 * 下载业务上报的Excel
	 * @param businessId 业务ID
	 * @param businessType 业务类型。1表示会议列表，2表示可视电话列表，3表示实时直播列表，4表示实时录制列表，5表示实时点播列表，6表示实时升级列表，7表示实时监控列表
	 * @param serverRegionIdLength 想看服务器行政区域ID的长度
	 * @return
	 */
	@RequestMapping(value = "/businessId/{businessId}/businessType/{businessType}/downloadBusinessReportingExcel", method = RequestMethod.GET)
	public ReturnData downloadBusinessReportingExcel(@PathVariable("businessId") String businessId,
													 @RequestParam(name = "serverRegionIdLength", required = false, defaultValue = "2") Integer serverRegionIdLength,
													 @PathVariable("businessType") String businessType, HttpServletRequest request, HttpServletResponse response,
													 @RequestParam(name = "isException", defaultValue = "false")Boolean isException,
													 @RequestParam(name = "devNo", defaultValue = "")String devNo,
													 @RequestParam(name = "order", defaultValue = "")String order,
													 @RequestParam(name = "sidx", defaultValue = "")String sidx) {
		try {
			this.meetService.getBusinessReportingWorkbook(businessId, businessType, serverRegionIdLength,isException,request,response,devNo,order,sidx);

			//获取输入流并进行清空处理

			return super.returnResult(0, "下载成功");
		} catch (BusinessException e) {
			logger.error("MeetController ===== downloadBusinessReportingExcel ===== 下载业务上报Excel失败 =====> ", e);
			return super.returnError(StringUtil.isNotNull(e.getMessage())?e.getMessage():"下载失败");
		} catch(Exception e) {
			logger.error("MeetController ===== downloadBusinessReportingExcel ===== 下载业务上报Excel失败 =====> ", e);
			return super.returnError("下载失败");
		}
	}
	@RequestMapping(value = "/getDeviceInfo", method = RequestMethod.GET)
	public ReturnData getDeviceInfo(String deviceId){
		if(StringUtils.isNotEmpty(deviceId)) {
			List<Map<String, Object>> allInfo = meetService.getAllInfo(Arrays.asList(deviceId.split(",")));
			return super.returnResult(0, "成功", "", allInfo);
		}
		return super.returnResult(0, "成功");
	}
}
