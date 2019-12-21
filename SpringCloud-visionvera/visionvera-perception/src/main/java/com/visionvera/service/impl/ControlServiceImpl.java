package com.visionvera.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.datacore.MonitorVedioReport;
import com.visionvera.bean.datacore.VphoneReport;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import com.visionvera.feign.ServerConfigService;
import com.visionvera.service.ControlService;
import com.visionvera.service.MeetService;
import com.visionvera.service.VphoneReportService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.HttpUtils;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @date 2018年11月23日 16:51
 */
@Service
@Transactional(value = "transactionManager_perception", rollbackFor = Exception.class)
public class ControlServiceImpl implements ControlService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlServiceImpl.class);
    @Autowired
    private VphoneReportService vphoneReportService;
    @Autowired
    private ServerConfigService serverConfigService;
    @Autowired
    private MeetService meetService;

	@Override
	@SuppressWarnings("rawtypes")
    public ReturnData controlPhone(String platformId, String uniqId, String msg, String loginName, String loginPwd) {
        BaseReturn resultData = new BaseReturn();
        ReturnData returnData = serverConfigService.getServerConfig(platformId);
        if (!returnData.getErrcode().equals(0)) {
            LOGGER.error("获取会管服务配置信息失败");
            return resultData.returnResult(1, "获取会管服务配置信息失败");
        }
        Map extra = (HashMap) ((HashMap) returnData.getData()).get("extra");
        String token =extra.get("token")== null?"":extra.get("token").toString();
        String lmtUrl = "http://" + extra.get("ip").toString() + ":" + extra.get("port").toString();
//        String loginName = extra.get("loginName")== null?null:extra.get("loginName").toString();
//        String loginPwd =extra.get("loginPwd")== null?null:extra.get("loginPwd").toString();
        VphoneReport vphoneReport = vphoneReportService.get(uniqId);
        if (vphoneReport == null) {
            return resultData.returnResult(1, "该业务已不存在");
        }
        vphoneReport.setBusinessState(1);
        vphoneReport.setEndTime(new Date());
        String dstNo = vphoneReport.getDstNo();
        Integer businessType = vphoneReport.getBusinessType();
        String srcNo = vphoneReport.getSrcNo();
        Integer businessState = vphoneReport.getBusinessState();
        JSONObject jsonObject = null;
        if (vphoneReport.getPlatformType() == 901) {
            // gis 管控接口
            JSONArray jsonArray = new JSONArray();
            JSONObject json = new JSONObject();
            json.put("srcNo", srcNo);
            json.put("dstNo", dstNo);
            json.put("businessType", businessType);
            json.put("businessState", businessState);
            jsonArray.add(json);
            jsonObject = gisControl(loginName, loginPwd, lmtUrl, jsonArray, businessType);
            if (jsonObject.getInteger("errcode") != 0) {
                return resultData.returnResult(1, "停止失败" + (jsonObject.getString("errmsg") == null ? "" : "。" + jsonObject.getString("errmsg")));
            }
        } else if (vphoneReport.getPlatformType() == 701) {
            // 会管 管控接口
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("status", 5);
            jsonObject1.put("stopReason", msg);
            jsonObject = cmsWebControl(srcNo,loginName, loginPwd, lmtUrl, jsonObject1, 1);
            if (jsonObject.getIntValue("errcode") != 0) {
            	return resultData.returnResult(1, "停止失败" + (jsonObject.getString("errmsg") == null ? "" : "。" + jsonObject.getString("errmsg")));
            }
        } else if (vphoneReport.getPlatformType() == 601) {
            // 会管 管控接口
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("boxnum", vphoneReport.getSrcNo());//主叫方终端号码
            jsonObject1.put("destboxnum", vphoneReport.getDstNo());//被叫方终端号码
            jsonObject1.put("scheduleId", vphoneReport.getExtAttr1());
            jsonObject1.put("userId", vphoneReport.getExtAttr2());
            jsonObject1.put("dev1", vphoneReport.getSrcZonedevno());
            jsonObject1.put("dev2", vphoneReport.getDstZonedevno());
            jsonObject1.put("areano1", vphoneReport.getSrcZoneno());
            jsonObject1.put("areano2", vphoneReport.getDstZoneno());
            jsonObject = cmsWebVphoneControl(loginName, loginPwd, lmtUrl, jsonObject1);
            if (jsonObject.getIntValue("errcode") != 0) {
            	return resultData.returnResult(1, "停止失败" + (jsonObject.getString("errmsg") == null ? "" : "。" + jsonObject.getString("errmsg")));
            }
        } else if (vphoneReport.getPlatformType() == 401) {
            // 流媒体管控接口
            StringBuffer url = new StringBuffer();
            url.append(lmtUrl);
            url.append("/api/rest/business/").append(vphoneReport.getBusinessType()).append("/stop?");
            url.append("devNo=").append(dstNo);
            try {
                url.append("&msg=").append(URLEncoder.encode(msg,"UTF-8"));
                url.append("&reason=").append(URLEncoder.encode(msg,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url.append("&userName=").append(vphoneReport.getUserName());
            if (StringUtil.isNotNull(token) && token.indexOf("&") == -1) {
                url.append("&access_token=").append(token);
            } else if (token.indexOf("&") > -1) {
                url.append(token);
            }
            String result = HttpUtils.sendGet(url.toString(), "");
            jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("errcode") != 0) {
            	return resultData.returnResult(1, "停止失败" + (jsonObject.getString("errmsg") == null ? "" : "。" + jsonObject.getString("errmsg")));
            }
        }
        vphoneReportService.update(vphoneReport);
        return resultData.returnResult(0, "停止成功");
    }
    private JSONObject cmsWebControl(String devNo,String username,String password,String lmtUrl,JSONObject json, Integer businessType) {
        StringBuffer url = new StringBuffer();
        url.append(lmtUrl + "/cmsweb/restful/schedule/")
        .append("4106ffa0a7d111e6802eb82a72db6d4d").append("/").append(devNo).append("/stopDeviceOption.json");
//        url.append("?loginName=" + username + "&loginPwd=" + password);
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Content-type", "application/json");
        headersMap.put("Accept", "application/json");
        json.put("loginName", username);
        json.put("pwd", password);
        String result = HttpUtils.sendPost(url.toString(), json.toJSONString(), headersMap);
        return JSONObject.parseObject(result);
    }
   /**
    * 会管可视电话停止接口
    * @param username
    * @param password
    * @param lmtUrl
    * @param json
    * @param businessType
    * @return
    */
    private JSONObject cmsWebVphoneControl(String username,String password,String lmtUrl,JSONObject json) {
    	StringBuffer url = new StringBuffer();
    	url.append(lmtUrl + "/cmsweb/restful/schedule/vPhone/stop.json");
    	Map<String, String> headersMap = new HashMap<>();
    	headersMap.put("Content-type", "application/json");
    	headersMap.put("Accept", "application/json");
    	String result = HttpUtils.sendPost(url.toString(), json.toJSONString(), headersMap);
    	return JSONObject.parseObject(result);
    }

    private JSONObject gisControl(String username, String password, String lmtUrl, JSONArray json, Integer businessType) {
        String result = HttpUtils.sendGet(lmtUrl + "/gisPlatform/api/user/login.do?userAccount=" + username + "&password=" + password, "");
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject.getBoolean("result")) {
            StringBuffer url = new StringBuffer();
            url.append(lmtUrl);
            Map<String, String> headersMap = new HashMap<>();
            headersMap.put("Content-type", "application/json");
            headersMap.put("Accept", "application/json");
            url.append("/gisPlatform/api/business/").append(businessType).append("/stopBusiness.do").append("?token=").append(jsonObject.getString("token"));
            result = HttpUtils.sendPost(url.toString(), json.toJSONString(), headersMap);
            return JSONObject.parseObject(result);
        }
        return jsonObject;
    }

    /**
     *
     */
	@Override
	@SuppressWarnings("rawtypes")
	public ReturnData stopMeeting(String platformId, String uuid,String loginName,String loginPwd, String msg) {
		BaseReturn resultData = new BaseReturn();
		ReturnData returnData = serverConfigService.getServerConfig(platformId);
		if (!returnData.getErrcode().equals(0)) {
			LOGGER.error("获取服务配置信息失败");
            return resultData.returnError("获取服务配置信息失败");
		}
		ScheduleVO param = new ScheduleVO();
		param.setUuid(uuid);
		ScheduleVO schedule = meetService.queryByScheduleUuid(param);
		if (schedule == null) {
			resultData.returnError("会议不存在，停会失败");
		}
		Map extra = (HashMap) ((HashMap) returnData.getData()).get("extra");
		String lmtUrl = "http://" + extra.get("ip") + ":" + extra.get("port");
		StringBuffer url = new StringBuffer();
		url.append(lmtUrl);
		if(schedule.getPlatformType()==GlobalConstants.PLATFORMTYPE_ZD){
			 Map<String, Object> params = new HashMap<String, Object>();
			 url.append("/cmsweb/restful/schedule/")
		     .append("4106ffa0a7d111e6802eb82a72db6d4d").append("/").append(schedule.getDevno()).append("/stopDeviceOption.json");
			params.put("status", 4);
			params.put("stopReason", msg);
			params.put("loginName", loginName);
			params.put("pwd", loginPwd);
			LOGGER.info("请求URL:"+url+"请求参数:"+JSON.toJSONString(params));
			Map<String, Object> resultMap = RestClient.post(url.toString(), null, params);
			if ((Integer) resultMap.get("errcode") == 0) {
				this.updateSchedule(uuid);
				return resultData.returnResult(0, "停会成功");
			}else{
				String errmsg =(String)resultMap.get("errmsg");
				if(StringUtil.isNull(errmsg)){
					errmsg="停会失败";
				}
				return resultData.returnError(errmsg);
			}
		}else if(schedule.getPlatformType()==GlobalConstants.PLATFORMTYPE_LMT){
			url.append("/api/rest/business/9/stop");
			url.append("?userName=" + schedule.getCustomerName());
			url.append("&msg=" + msg);
			LOGGER.info("请求URL:"+url);
			Map<String, Object> resultMap = RestClient.get(url.toString(), null, null);
			if ((Integer) resultMap.get("errcode") == 0) {
				this.updateSchedule(uuid);
				return resultData.returnResult(0, "停会成功");
			}
		}else{
			resultData.returnError("平台类型不支持，停会失败");
		}
		return resultData.returnError("停会失败");
	}


	private void updateSchedule(String uuid){
		Map<String, Object> meetParams = new HashMap<String, Object>();//meeting表参数
		ScheduleVO scheduleParams = new ScheduleVO();
		try {
			scheduleParams.setUuid(uuid);
			scheduleParams.setStatus("5");//停止会议
			String time =DateUtil.date2String(new Date());
			scheduleParams.setEndTime(time);
			meetService.updateScheduleByUuid(scheduleParams);
			meetParams.put("uuid", uuid);
			meetParams.put("stopTime",time );
			meetParams.put("status",0 );
			meetService.updateMeetings(meetParams);

		} catch (Exception e) {
			LOGGER.error("ControlServiceImpl--stopMeeting--"+e);
		}

	}
}
