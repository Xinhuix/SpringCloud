package com.visionvera.service.impl;

import cn.afterturn.easypoi.entity.vo.TemplateExcelConstants;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.view.PoiBaseView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.OperationalDevice;
import com.visionvera.bean.log.SysLog;
import com.visionvera.bean.restful.client.RestClient;
import com.visionvera.bean.ywcore.SetProvRateVO;
import com.visionvera.common.api.authentication.UserAPI;
import com.visionvera.common.api.dispatchment.RestTemplateUtil;
import com.visionvera.common.api.log.SysLogApi;
import com.visionvera.config.base.SysConfig;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.cms.DeviceDao;
import com.visionvera.dao.cms.ScheduleDao;
import com.visionvera.exception.BusinessException;
import com.visionvera.feign.SysConfigService;
import com.visionvera.service.*;
import com.visionvera.util.ReturnDataUtil;
import com.visionvera.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 预约会议相关的业务接口实现类
 */
@Service
@Transactional(value = "transactionManager_cms", rollbackFor = Exception.class)
public class MeetServiceImpl implements MeetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeetServiceImpl.class);

    @Autowired
    private ScheduleDao scheduleDao;
    @Autowired
    private SysLogApi sysLogApi;
    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private MonitorVedioReportService monitorVedioReportService;

    @Autowired
    private SerachService serachService;

    @Autowired
    private SenseService senseService;

    @Autowired
    private SysConfig sysConfig;

    @Autowired
    private VphoneReportService vphoneReportService;
    @Autowired
    private UserAPI userAPI;


    /**
     * 获取正在进行中的会议列表
     *
     * @param paramsMap
     * @param serverRegionIdLength 想看服务器行政区域ID的长度
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public ReturnData getMeetingInfo(Map<String, Object> paramsMap, Integer serverRegionIdLength) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> extraMap = new HashMap<String, Object>();
        if (!paramsMap.get("pageSize").equals(-1)) {//pageSize为-1表示不分页
            PageHelper.startPage(paramsMap);
        }
        String regionId = StringUtil.null2Empty(paramsMap.get("regionId"));
        if(StringUtil.isNotNull(regionId)&&!"000000000000".equals(regionId)){
            RegionVO region = this.serachService.getRegionByRegionId(regionId);
            Integer gradeId = region.getGradeId();
            if(gradeId==2) {
                paramsMap.put("regionId", regionId.substring(0, 4));
            }else{
                paramsMap.put("regionId", regionId.substring(0, 2));
            }
        }

        List<Map<String, Object>> scheduleList = this.scheduleDao.selectMeetingInfo(paramsMap);//查询正在进行中的会议数据

        if (scheduleList == null || scheduleList.size() <= 0) {
            return dataReturn.returnResult(0, "获取正在进行的会议列表成功", null, scheduleList, extraMap);
        }

        PageInfo<Map<String, Object>> meetInfo = new PageInfo<Map<String, Object>>(scheduleList);
        extraMap.put("totalPage", meetInfo.getPages());//总页数
        extraMap.put("totalCount", meetInfo.getTotal());//总条数

        Map<Integer, Integer> alarmThresholdMap = this.getAlarmThresholdList();//告警阈值（从数据库获取）
        //调用网管查询设备离线的接口 Start
        for (Map<String, Object> scheduleMap : scheduleList) {
            Integer deviceStatus = 0;
            String devIdsStr = "";
            Map<String, Object> scheduleDev = scheduleDao.selectScheduleDev(scheduleMap.get("uuid").toString());
            if(scheduleDev.containsKey("zonedevno")) {
                scheduleMap.put("zonedevno",scheduleDev.get("zonedevno"));
                scheduleMap.put("devIds",scheduleDev.get("devIds"));
                devIdsStr = StringUtil.null2Empty(scheduleDev.get("zonedevno"));//会议对应的设备ID(8位设备号)，多个以英文逗号","分隔
            }
            if (devIdsStr != null && devIdsStr.length() > 0) {//会议对应的设备不为空才会调用网管接口
                List<String> deviceIdsList = Arrays.asList(devIdsStr.split(","));//转换成List
                //计算循环操作的次数 End

                //从网管获取终端、服务器状态（一个终端只调一次接口即可返回该终端的所有故障信息）-- start
                List<Map<String, Object>> devSvrList = this.getAllInfo(new ArrayList<>(deviceIdsList));
                if (devSvrList != null && devSvrList.size() > 0) {
                    for (Map<String, Object> deviceSvr : devSvrList) {
                        //终端在线离线
                        if (GlobalConstants.MEET_DEVICE_STATUS_OFF.equals(StringUtil.null2Empty(deviceSvr.get("terstate")))) {//0表示不在线,1表示在线
                            deviceStatus = GlobalConstants.DEVICE_STATUS_ERROR;//1表示离线
                            continue;
                        }
                        //服务器在线离线
                        if (GlobalConstants.MEET_DEVICE_SERVER_OFFLINE.equals(StringUtil.null2Empty(deviceSvr.get("svrstate")))) {//0表示服务器离线
                            deviceStatus = GlobalConstants.DEVICE_STATUS_ERROR;//1表示离线
                            continue;
                        }
                        //终端有流无流
                        if ((GlobalConstants.MEET_DEVICE_VIDEO_FLOW_ERROR.equals(StringUtil.null2Empty(deviceSvr.get("videoret"))) || //0表示有流，1表示无流.终端视频无流
                                GlobalConstants.MEET_DEVICE_AUDIO_FLOW_ERROR.equals(StringUtil.null2Empty(deviceSvr.get("audioret")))) && deviceStatus !=1) {//0表示有流，1表示无流.终端音频无流
                            deviceStatus = GlobalConstants.DEVICE_STATUS_WARNING;//4表示警告
                            continue;
                        }
                        String serverRegionId = deviceSvr.containsKey("svrregionid")?StringUtil.null2Empty(deviceSvr.get("svrregionid")):"";
                        String forwardServerRegionId = deviceSvr.containsKey("zfregionid")?StringUtil.null2Empty(deviceSvr.get("zfregionid")):"";//转发服务器行政区域ID

                        serverRegionId =serverRegionId.length()>=serverRegionIdLength? serverRegionId.substring(0, serverRegionIdLength):serverRegionId;//客户想看几位行政区域
                        forwardServerRegionId = forwardServerRegionId.length()>=serverRegionIdLength? forwardServerRegionId.substring(0, serverRegionIdLength):forwardServerRegionId;//客户想看几位行政区域
                        //服务器行政区域ID和转发服务器行政区域ID不匹配
                        if (!serverRegionId.equals(forwardServerRegionId) && deviceStatus !=1) {//0表示服务器转发配置不相同
                            deviceStatus = GlobalConstants.DEVICE_STATUS_WARNING;//4表示警告
                            continue;
                        }
                        //设备丢包率
                        Integer deviceAudioLostRate = Integer.valueOf(deviceSvr.containsKey("alostpacket")?StringUtil.isNull(StringUtil.null2Empty(deviceSvr.get("alostpacket")))?"0":StringUtil.null2Empty(deviceSvr.get("alostpacket")):"0");//设备的音频丢包率
                        Integer deviceVideoLostRate = Integer.valueOf(deviceSvr.containsKey("vlostpacket")?StringUtil.isNull(StringUtil.null2Empty(deviceSvr.get("vlostpacket")))?"0":StringUtil.null2Empty(deviceSvr.get("vlostpacket")):"0");//设备的视频丢包率
                        //音频丢包率和视频丢包率都超过阈值的
                        if ((deviceAudioLostRate >= alarmThresholdMap.get(7)) || (deviceVideoLostRate >= alarmThresholdMap.get(8)) && deviceStatus !=1) {
                            deviceStatus = GlobalConstants.DEVICE_STATUS_WARNING;//4表示警告
                            continue;
                        }
                        //终端版本是否相同
                        if (GlobalConstants.DEVICE_VERSION_STATUS_DIFFERENCE.equals(StringUtil.null2Empty(deviceSvr.get("version"))) && deviceStatus !=4 && deviceStatus !=1) {//0表示版本不相同，1表示版本相同
                            deviceStatus = GlobalConstants.DEVICE_STATUS_NOTICE;//3表示 注意
                        }
                    }
                } else {
                    //2表示设备状态未知
                    deviceStatus = GlobalConstants.DEVICE_STATUS_UNKNOWN;
                }
            }
            scheduleMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, deviceStatus);//0表示设备正常
        }
        //调用网管查询设备离线的接口 End
        return dataReturn.returnResult(0, "获取成功", null, meetInfo.getList(), extraMap);
    }

    /**
     * @param @param               获取参会终端详细信息
     * @param serverRegionIdLength 想看服务器行政区域ID的长度
     * @param @return
     * @return List<DeviceVO>
     * @throws
     * @Description: TODO
     * @author 谢程算
     * @date 2018年6月13日
     */
    @SuppressWarnings("unchecked")
    @Override
    public ReturnData getMeetingDevList(Map<String, Object> paramsMap, Integer serverRegionIdLength) {
        BaseReturn dataReturn = new BaseReturn();
        Map<String, Object> extraMap = new HashMap<>();
        //接口传递数据的设备List
        List<String> deviceIdList = new ArrayList<>();
        List<Map<String, Object>> breakDownList = new ArrayList<>();
        //循环的次数
        //设备终端状态信息
        StringBuffer deviceStatusInfoStringBuffer = new StringBuffer();
        //服务器状态
        StringBuffer serverStatusInfoStringBuffer = new StringBuffer();
        //获取MeetService对象，为了让AlarmInDataBaseInterceptor拦截器连接
        //pageSize为-1表示不分页
        if (!paramsMap.get("pageSize").equals(-1)) {
            PageHelper.startPage(paramsMap);
        }
        //根据会议ID查询对应的设备列表
        List<Map<String, Object>> deviceList = this.deviceDao.selectDeviceListByMeetingId(paramsMap);
        if (deviceList == null || deviceList.size() <= 0) {
            return dataReturn.returnResult(0, "获取会议终端列表成功", null, deviceList, extraMap);
        }
        // 服务器区号和服务器号的集合
        for (Map<String, Object> deviceMap : deviceList) {
            if(deviceMap.containsKey("zonedevno")) {
                deviceIdList.add(deviceMap.get("zonedevno").toString());
            }else{
                deviceMap.put("zonedevno",deviceMap.get("devNo"));
            }
        }

        PageInfo<Map<String, Object>> deviceInfo = new PageInfo<Map<String, Object>>(deviceList);
        extraMap.put("totalPage", deviceInfo.getPages());
        extraMap.put("totalCount", deviceInfo.getTotal());
        List<Map<String, Object>> zoneServers = serachService.getZoneServers(deviceList);

        //调用网管的丢包率接口 Start
        //从会管获取终端音视频丢包率阈值
        Map<Integer, Integer> alarmThresholdMap = this.getAlarmThresholdList();
        List<Map<String, Object>> devSvrAllList = getAllInfo(deviceIdList);
        //遍历会议下的设备List
        for (Map<String, Object> deviceMap : deviceList) {
            deviceStatusInfoStringBuffer.setLength(0);
            serverStatusInfoStringBuffer.setLength(0);
            String deviceId = String.valueOf(deviceMap.get("devNo"));//设备ID
            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_UNKNOWN);//2表示设备状态未知（默认未知）
            deviceMap.put(GlobalConstants.DEVICE_STATUS_INFO_KEY, "终端状态未知");//设备终端状态提示信息
            deviceMap.put(GlobalConstants.SERVER_STATUS_INFO_KEY, "服务器状态未知");//服务器状态提示信息
            for (Map<String, Object> zoneServer : zoneServers) {
                if (deviceMap.get("zonedevno").equals(zoneServer.get("v2vno"))) {
                    deviceMap.put("svrName", zoneServer.get("svrname"));
                }
            }
            if (devSvrAllList.size() > 0) {
                for (Map<String, Object> devSvr : devSvrAllList) {//遍历音频或视频丢包率超过告警阈值的List
                    String deviceFaultId = String.valueOf(devSvr.get("devno"));//失败(即音频或视频丢包率超过告警阈值的设备ID)
                    if (deviceId.equals(deviceFaultId)) {
                        //0表示设备正常
                        deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_OK);
                        deviceMap.put("devStatus",devSvr);
                        Integer deviceAudioLostRate = Integer.valueOf((devSvr.containsKey("alostpacket") ? StringUtil.isNull(devSvr.get("alostpacket").toString())?"0":StringUtil.null2Empty(devSvr.get("alostpacket")) : "0"));//设备的音频丢包率
                        Integer deviceVideoLostRate = Integer.valueOf((devSvr.containsKey("vlostpacket") ? StringUtil.isNull(devSvr.get("vlostpacket").toString())?"0":StringUtil.null2Empty(devSvr.get("vlostpacket")) : "0"));//设备的视频丢包率
                        //服务器是否在线
                        if (StringUtil.null2Empty(devSvr.get("svrstate")).equals(GlobalConstants.MEET_DEVICE_SERVER_OFFLINE) && //0表示服务器离线
                                StringUtil.null2Empty(devSvr.get("devno")).equals(deviceId)) {
//								serverStatusInfoStringBuffer.append(GlobalConstants.PERCEPTION_DEVICE_SERVER_OFFLINE_INFO).append("、");
                            deviceMap.put(GlobalConstants.DEVICE_STATUS_INFO_KEY, GlobalConstants.PERCEPTION_DEVICE_SERVER_OFFLINE_INFO);//设备终端状态提示信息
                            deviceMap.put(GlobalConstants.SERVER_STATUS_INFO_KEY, GlobalConstants.PERCEPTION_DEVICE_OFFLINE_INFO);//服务器状态提示信息
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_ERROR);//1表示设备不正常
                            breakDownList.add(deviceMap);
                            continue;
                        }

                        //终端是否在线
                        if (StringUtil.null2Empty(devSvr.get("terstate")).equals(GlobalConstants.MEET_DEVICE_STATUS_OFF) && devSvr.get("devno").equals(deviceId)) {//0表示不在线,1表示在线
                            deviceStatusInfoStringBuffer.append(GlobalConstants.PERCEPTION_DEVICE_OFFLINE_INFO).append("、");
                            String deviceStatusInfo = this.getStrWithoutLastDawn(deviceStatusInfoStringBuffer);//设备状态提示信息，去掉最后一个顿号
                            String serverStatusInfo = this.getStrWithoutLastDawn(serverStatusInfoStringBuffer);//服务器状态提示信息，去掉最后一个顿号
                            deviceMap.put(GlobalConstants.DEVICE_STATUS_INFO_KEY, deviceStatusInfo);//设备终端状态提示信息
                            deviceMap.put(GlobalConstants.SERVER_STATUS_INFO_KEY, serverStatusInfo);//服务器状态提示信息
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_ERROR);//1表示设备不正常
                            breakDownList.add(deviceMap);
                            continue;
                        }

                        //服务器转发配置是否正确
                        String serverRegionId = StringUtil.null2Empty(devSvr.get("svrregionid"));//服务器行政区域ID
                        String forwardServerRegionId = StringUtil.null2Empty(devSvr.get("zfregionid"));//转发服务器行政区域ID
                        serverRegionId =serverRegionId.length()>=serverRegionIdLength? serverRegionId.substring(0, serverRegionIdLength):serverRegionId;//客户想看几位行政区域
                        forwardServerRegionId = forwardServerRegionId.length()>=serverRegionIdLength? forwardServerRegionId.substring(0, serverRegionIdLength):forwardServerRegionId;//客户想看几位行政区域
                        //服务器行政区域ID和转发服务器行政区域ID不匹配
                        if (!serverRegionId.equals(forwardServerRegionId)) {//0表示服务器转发配置不相同
                            serverStatusInfoStringBuffer.append(GlobalConstants.PERCEPTION_DEVICE_SERVER_CONFIG_DIFFERENCE_INFO).append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_WARNING);//4表示警告
                        }

                        //音频丢包率超过告警阈值
                        if (deviceAudioLostRate >= alarmThresholdMap.get(7)) {
                            deviceStatusInfoStringBuffer.append(String.format(GlobalConstants.PERCEPTION_DEVICE_LOST_RATE_AUDIO_INFO, deviceAudioLostRate))
                                    .append("%").append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_WARNING);//0表示设备不正常
                        }

                        //视频丢包率超过告警阈值
                        if (deviceVideoLostRate >= alarmThresholdMap.get(8)) {
                            deviceStatusInfoStringBuffer.append(String.format(GlobalConstants.PERCEPTION_DEVICE_LOST_RATE_VIDEO_INFO, deviceVideoLostRate))
                                    .append("%").append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_WARNING);//0表示设备不正常
                        }

                        //终端版本号是否一致
                        if (StringUtil.null2Empty(devSvr.get("version")).equals(GlobalConstants.DEVICE_VERSION_STATUS_DIFFERENCE)
                                && StringUtil.null2Empty(devSvr.get("devno")).equals(deviceId)) {//0表示不同，1表示相同
                            deviceStatusInfoStringBuffer.append(GlobalConstants.PERCEPTION_VERSION_STATUS_INFO).append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_NOTICE);//3表示 注意
                        }

                        //终端有流无流
                        if (StringUtil.null2Empty(devSvr.get("videoret")).equals(GlobalConstants.MEET_DEVICE_VIDEO_FLOW_ERROR)) {//终端视频无流
                            deviceStatusInfoStringBuffer.append("终端视频无流").append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_WARNING);//4表示警告
                        }
                        if (StringUtil.null2Empty(devSvr.get("audioret")).equals(GlobalConstants.MEET_DEVICE_AUDIO_FLOW_ERROR)) {//终端音频无流
                            deviceStatusInfoStringBuffer.append("终端音频无流").append("、");
                            deviceMap.put(GlobalConstants.MEET_DEVICE_STATUS_KEY, GlobalConstants.DEVICE_STATUS_WARNING);//4表示警告
                        }

                        //设备状态提示信息，去掉最后一个顿号
                        String deviceStatusInfo = this.getStrWithoutLastDawn(deviceStatusInfoStringBuffer);
                        //服务器状态提示信息，去掉最后一个顿号
                        String serverStatusInfo = this.getStrWithoutLastDawn(serverStatusInfoStringBuffer);
                        //设备终端状态提示信息
                        deviceMap.put(GlobalConstants.DEVICE_STATUS_INFO_KEY, deviceStatusInfo);
                        //服务器状态提示信息
                        deviceMap.put(GlobalConstants.SERVER_STATUS_INFO_KEY, serverStatusInfo);
                        breakDownList.add(deviceMap);
                    }
                }
            }
        }
        //分批操作 End
        //0所有，1只看有问题的
        if (paramsMap.get("showType").toString().equals("0")) {
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> list = deviceInfo.getList();
            int index =0;
            Boolean isMaster = false;
            Boolean speaker1 = false;
            Boolean speaker2 = false;

            for (Map<String, Object> map : list) {
                if (paramsMap.get("masterNo").toString().equals(map.get("devNo"))) {
                    dataList.add(0, map);
                    isMaster = true;
                    index++;
                    continue;
                }
                if (map.containsKey("speaker_1")&&map.get("speaker_1").toString().equals(map.get("zonedevno"))&&!map.get("speaker_1").toString().equals(paramsMap.get("masterNo").toString())) {
                    if (dataList.size() > 0) {
                        dataList.add(speaker2 ? 1 : 0, map);
                    } else {
                        dataList.add(map);
                    }
                    speaker1 = true;
                    index++;
                    continue;
                }
                if (map.containsKey("speaker_2")&&map.get("speaker_2").toString().equals(map.get("zonedevno"))) {
                    if (dataList.size() > 0) {
                        dataList.add(isMaster ? speaker1 ? 2 : 1 : speaker1 ? 1 : 0, map);
                    } else if (dataList.size() > 1) {
                        dataList.add(isMaster ? speaker1 ? 2 : 1 : speaker1 ? 1 : 0, map);
                    } else {
                        dataList.add(map);
                    }
                    speaker2 = true;
                    index++;
                    continue;
                }
                if (Integer.parseInt(map.get(GlobalConstants.MEET_DEVICE_STATUS_KEY).toString()) == 0) {
                    if (dataList.size() > 0) {
                        dataList.add(index, map);
                        continue;
                    }
                }
                dataList.add(map);
            }
            return dataReturn.returnResult(0, "获取会议的参会终端列表成功", null, dataList, extraMap);
        }
        return dataReturn.returnResult(0, "获取会议的参会终端列表成功", null, breakDownList, extraMap);
    }



    /**
     * 获取网管服务器配置信息
     *
     * @return
     */
    private JSONObject getServerConfig() {
        //获取网管服务器配置信息,serverType为2
        ReturnData resultData = this.sysConfigService.getServer("2");
        if (!resultData.getErrcode().equals(0)) {
            LOGGER.error("MeetServiceImpl ===== getServerConfig ===== 调用运维服务器配置接口失败");
            throw new BusinessException("运维服务不可用, 无法查询丢包率, 请稍后再试");
        }
        JSONObject serverJsonObject = JSONObject.parseObject(JSONObject.toJSONString(resultData), JSONObject.class);
        JSONObject extraJsonObject = serverJsonObject.getJSONObject("data").getJSONObject("extra");
        return extraJsonObject;
    }

    /**
     * 去掉StringBuilder类型的最后一个顿号，并返回字符串
     *
     * @param stringBuffer
     * @return
     */
    private String getStrWithoutLastDawn(StringBuffer stringBuffer) {
        String deviceStatusInfo = stringBuffer.toString();
        if (StringUtil.isNotNull(deviceStatusInfo)) {
            deviceStatusInfo = deviceStatusInfo.substring(0, deviceStatusInfo.length() - 1);
        }

        return deviceStatusInfo;
    }


    /**
     * 调用运维服务获取音视频告警阈值
     *
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public Map<Integer, Integer> getAlarmThresholdList() {
        //调用运维服务获取告警阈值接口 Start
        ReturnData alarmThresholdData = this.sysConfigService.getAlarmMeetCount();
        if (!alarmThresholdData.getErrcode().equals(0)) {
            LOGGER.error("MeetServiceImpl ===== getAlarmThresholdList ===== 调用运维服务器获取告警阈值接口失败");
            throw new BusinessException("运维服务不可用, 请稍后再试");
        }
        JSONObject alarmThresholdJson = JSONObject.parseObject(JSONObject.toJSONString(alarmThresholdData), JSONObject.class);

        JSONArray alarmThresholdJsonArray = alarmThresholdJson.getJSONObject("data").getJSONArray("items");
        List<Map<String, Object>> alarmThresholdList = JSONObject.parseObject(alarmThresholdJsonArray.toJSONString(), List.class);
        //调用运维服务获取告警阈值接口 End

        if (alarmThresholdList == null || alarmThresholdList.size() <= 0) {
            LOGGER.error("MeetServiceImpl ===== getAlarmThresholdList ===== 告警阈值为空");
            throw new BusinessException("告警阈值不能为空, 请先配置告警阈值");
        }
        //告警阈值List
        Map<Integer, Integer> map = new HashMap<>();
        for (Map<String, Object> alarmThresholdMap : alarmThresholdList) {
            //音频丢包率 			//视频丢包率
            map.put(Integer.parseInt(alarmThresholdMap.get("valueType").toString()), Integer.valueOf(alarmThresholdMap.get("value") + ""));
        }
        return map;
    }

    /**
     * @param @param  获取参会终端区域信息
     * @param @return
     * @return List<DeviceVO>
     * @throws
     * @Description: TODO
     * @author 谢程算
     * @date 2018年6月13日
     */
    @Override
    public ReturnData getMeetingDevRegionList(Map<String, Object> paramsMap) {
        BaseReturn dataReturn = new BaseReturn();
        List<Map<String, Object>> deviceList = this.deviceDao.selectDeviceRegionListByMeetingId(paramsMap);//根据会议ID查询对应的设备列表
        return dataReturn.returnResult(0, "获取会议的参会终端区域列表成功", null, deviceList, null);
    }

    /**
     * 重启/关闭终端
     *
     * @param operationalDevice
     * @return
     */
    @Override
    public ReturnData operateDevice(OperationalDevice operationalDevice) {
        SysLog sysLog = new SysLog();
        sysLog.setLoginName(getUsername());
        sysLog.setOperateType("1".equals(operationalDevice.getType())?3003:3002);
        sysLog.setDescription("1".equals(operationalDevice.getType())?"重启终端终端号[":"关闭终端终端号["+operationalDevice.getDevno()+"]成功");
        sysLogApi.addLog(sysLog);
        BaseReturn dataReturn = new BaseReturn();
        JSONObject resultJsonObject = new JSONObject();

        //获取网管服务器配置信息
        JSONObject extraJsonObject = this.getServerConfig();

        //网管重启/关闭终端业务接口地址
        String operateDeviceUrl = String.format(WsConstants.URL_OPERATE_DEVICE, extraJsonObject.getString("ip"), extraJsonObject.getString("port"));
        //调用网管重启/关闭终端业务接口

        //通过8位设备号查询设备信息
        String[] zonedevnoArr = new String[]{operationalDevice.getZonedevno()};
        List<DeviceVO> deviceList = this.deviceDao.selectDeviceByZonedevnoBatch(zonedevnoArr);//唯一
        if (deviceList == null || deviceList.size() <= 0) {
            LOGGER.error("MeetServiceImpl ===== operateDevice ===== 找不到该设备信息，设备8位号: " + operationalDevice.getZonedevno());
            return dataReturn.returnError("没有该设备信息");
        }
        operationalDevice.setZonedevno(deviceList.get(0).getZonedevno());//5位设备号
        operationalDevice.setDevno(deviceList.get(0).getDevno());//5位设备号
        operationalDevice.setZoneno(deviceList.get(0).getZoneno());//区号

        try {
            resultJsonObject = RestTemplateUtil.postForObject(operateDeviceUrl, operationalDevice, JSONObject.class);
        } catch (Exception e) {
            LOGGER.error("MeetServiceImpl ===== operateDevice ===== 调用网管重启/关闭终端业务接口失败", e);
            throw new BusinessException("连接网管第三方接口服务失败");
        }

        if (resultJsonObject == null || resultJsonObject.size() <= 0) {
            LOGGER.error("MeetServiceImpl ===== operateDevice ===== 网管没有任何数据返回");
            throw new BusinessException("连接网管第三方接口服务失败");
        }

        if (!resultJsonObject.getString("ret").equals("0")) {
            return dataReturn.returnError(resultJsonObject.getString("msg"));
        }

        return dataReturn.returnResult(0, "操作成功");
    }

    /**
     * 获取终端、终端所属服务器情况
     *
     * @param deviceIdList 设备ID，多个使用英文逗号","分隔。最多支持20个
     * @return
     */
    @Override
    public ReturnData getDevAndSvrInfo(List<String> deviceIdList) {
        BaseReturn dataReturn = new BaseReturn();
        JSONObject deviceVersionResultData;
        //获取网管服务器配置信息
        JSONObject extraJsonObject = this.getServerConfig();

        //网管查询设备版本号情况的接口地址
        String deviceVersionUrl = String.format(WsConstants.URL_DEVICE_AND_SERVER_INFO, extraJsonObject.getString("ip"), extraJsonObject.getString("port"));
        //调用网管设备版本号情况接口
        try {
            deviceVersionResultData = RestTemplateUtil.postForObject(deviceVersionUrl, deviceIdList, JSONObject.class);
        } catch (Exception e) {
            LOGGER.error("MeetServiceImpl ===== getDeviceVersion ===== 从网管获取终端、服务器状态信息失败", e);
            throw new BusinessException("从网管获取终端、服务器状态信息失败");
        }

        if (deviceVersionResultData == null || deviceVersionResultData.size() <= 0) {
            LOGGER.error("MeetServiceImpl ===== getDeviceVersion ===== 网管没有任何数据返回");
            throw new BusinessException("从网管获取终端、服务器状态信息失败");
        }

        if (!deviceVersionResultData.getString("ret").equals("0")) {
            LOGGER.error("MeetServiceImpl ===== getDeviceVersion ===== 网管异常。异常信息{}: " + deviceVersionResultData.getString("msg"));
            throw new BusinessException("从网管获取终端、服务器状态信息失败, 网管返回信息: " + deviceVersionResultData.getString("msg"));
        }

        List<Map<String, Object>> deviceVersionList = (List<Map<String, Object>>) deviceVersionResultData.getJSONObject("data").get("items");//获取数据
        return dataReturn.returnResult(0, "获取终端、服务器状态信息成功", null, deviceVersionList);
    }

    /**
     * 业务上报的Excel的工作簿对象
     *
     * @param businessId           业务ID
     * @param businessType         业务类型。1表示会议列表，2表示可视电话列表，3表示实时直播列表，4表示实时录制列表，5表示实时点播列表，6表示实时升级列表，7表示实时监控列表
     * @param serverRegionIdLength 想看服务器行政区域ID的长度
     * @param request
     * @param response
     * @param devNo
     * @param order
     * @param sidx
     * @return
     */
    @Override
    public void getBusinessReportingWorkbook(String businessId, String businessType, Integer serverRegionIdLength, Boolean isException, HttpServletRequest request, HttpServletResponse response,
                                             String devNo, String order, String sidx) {
        SysLog sysLog = new SysLog();
        sysLog.setOperateType(3004);
        sysLog.setDescription("报告导出");
        sysLog.setLoginName(getUsername());
        sysLogApi.addLog(sysLog);
        List<String> deviceIdList = new ArrayList<String>();
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("scheduleId", businessId);
        List<Map<String, Object>> deviceList = new ArrayList<Map<String, Object>>();//业务的设备列表
        if ("1".equals(businessType)) {//会议信息
            deviceList = this.deviceDao.selectDeviceListByMeetingId(paramsMap);//根据会议ID查询对应的设备列表
        } else if ("2".equals(businessType)) {//可视电话信息
            deviceList = this.vphoneReportService.queryListBySearchText(order,sidx);//可视电话的终端列表
        } else {
            getMonitorVedioReport(businessId, devNo, deviceList);
        }
        List<Map<String,Object>> list =new ArrayList<>();
        Map<String, Object> maps = new HashMap<>();
        String templateUrl;
        String fileName;
        String name = "";
        switch (businessType) {
            case "1":
                name="业务名称";
                fileName = "会议信息导出";
                break;
            case "2":
                fileName = "可视电话信息导出";
                break;
            case "3":
                fileName = "实时直播信息导出";
                name="发起直播终端";
                break;
            case "4":
                fileName = "实时录制信息导出";
                break;
            case "5":
                fileName = "实时点播信息导出";
                break;
            case "6":
                fileName = "实时升级信息导出";
                break;
            case "7":
                fileName = "实时监控信息导出";
                name="监控名称";
                break;
            default:
                throw new RuntimeException("业务类型异常");
        }
        if("2".equals(businessType)){
            if (deviceList != null && deviceList.size() > 0) {
                for (Map<String, Object> deviceIdMap : deviceList) {
                    deviceIdList.add(deviceIdMap.get("srcZonedevno") + "");
                    deviceIdList.add(deviceIdMap.get("dstZonedevno") + "");
                }
            }
            //从网管获取终端、服务器状态（一个终端只调一次接口即可返回该终端的所有故障信息）
            List<Map<String, Object>> allInfoMapList = this.getAllInfo(deviceIdList);
            if (deviceList != null && deviceList.size() > 0) {
                for (int i = 0; i < deviceList.size(); i++) {
                    Map<String,Object> map = new HashMap<>();
                    initMap(map, "src");
                    initMap(map, "dst");
                    if (allInfoMapList != null && allInfoMapList.size() > 0) {
                        for (Map<String, Object> allInfoMap : allInfoMapList) {
                            if (deviceList.get(i).get("srcZonedevno").equals(allInfoMap.get("zonedevno"))) {//同一个终端
                                device(serverRegionIdLength, map,allInfoMap,"src");
                            }
                            if(deviceList.get(i).get("dstZonedevno").equals(allInfoMap.get("zonedevno"))){
                                device(serverRegionIdLength, map,allInfoMap,"dst");
                            }
                        }
                    }
                    map.put("srcZoneDevno", deviceList.get(i).get("srcZonedevno"));
                    map.put("srcDevName",deviceList.get(i).get("srcDevName"));
                    map.put("srcSvrname",deviceList.get(i).get("srcSvrname"));

                    map.put("dstZoneDevno", deviceList.get(i).get("dstZonedevno"));
                    map.put("dstDevName",deviceList.get(i).get("dstDevName"));
                    map.put("dstSvrname",deviceList.get(i).get("dstSvrname"));
                    //真实行政区域ID
                    region(deviceList, i, map, "src");
                    region(deviceList, i, map, "dst");
                    list.add(map);
               }
            }
            templateUrl = "doc/phone.xlsx";

        }else {
            if (deviceList != null && deviceList.size() > 0) {
                for (Map<String, Object> deviceIdMap : deviceList) {
                    deviceIdList.add(deviceIdMap.get("zonedevno") + "");
                }
            }
            List<Map<String, Object>> allInfoMapList = this.getAllInfo(deviceIdList);//从网管获取终端、服务器状态（一个终端只调一次接口即可返回该终端的所有故障信息）
            //获取服务器名称
            List<Map<String, Object>> zoneServers = this.serachService.getZoneServers(deviceList);
            //将服务器名称整理成map形式
            Map<String, String> zoneIdNameRel = new HashMap<String, String>();
            for (Map<String, Object> zoneServer : zoneServers) {
                zoneIdNameRel.put((String)zoneServer.get("v2vno"), (String)zoneServer.get("svrname"));
            }
            maps.put("logName", "");
            maps.put("log", "");
            maps.put("businessName", "");
            maps.put("name",name);
            if (deviceList != null && deviceList.size() > 0) {
                for (int i = 0; i < deviceList.size(); i++) {
                    Map<String,Object> map = new HashMap<>();
                    initMap(map, "");
                    Object zonedevno = deviceList.get(i).get("zonedevno");
                    Boolean phone =true;
                    if (allInfoMapList != null && allInfoMapList.size() > 0) {
                        for (Map<String, Object> allInfoMap : allInfoMapList) {
                            if (zonedevno.equals(allInfoMap.get("zonedevno"))) {//同一个终端
                                phone = device(serverRegionIdLength, map, allInfoMap, "");
                            }
                        }
                    }
                    map.put("zoneDevno", zonedevno);
                    map.put("devName",deviceList.get(i).get("devName"));
                    map.put("svrname",zoneIdNameRel.get(zonedevno.toString()));


                    //真实行政区域ID
                    region(deviceList, i, map, "");
                    if(phone&&isException) {
                        return;
                    }
                    list.add(map);

                }
                Map<String, Object> stringObjectMap = deviceList.get(0);
                String businessName = stringObjectMap.containsKey("scheduleName") ? StringUtil.null2Empty(stringObjectMap.get("scheduleName")): "";
                maps.put("businessName", businessName);

                meetingLog(businessId, businessType, maps);
            }
            templateUrl = "doc/meet.xlsx";
        }
        TemplateExportParams params = new TemplateExportParams(templateUrl);
        params.setColForEach(true);
        Map<String,Object> modelMap =new HashMap<>();
        maps.put("list", list);
        modelMap.put(TemplateExcelConstants.MAP_DATA, maps);
        modelMap.put(TemplateExcelConstants.PARAMS, params);
        modelMap.put(TemplateExcelConstants.FILE_NAME, fileName);
        PoiBaseView.render(modelMap, request, response, TemplateExcelConstants.EASYPOI_TEMPLATE_EXCEL_VIEW);
    }

    private void getMonitorVedioReport(String businessId, String devNo, List<Map<String, Object>> deviceList) {
        /**
         * 			分别是实时直播、录制、点播、升级和监控终端列表
         */
        List<Map<String, Object>> monitorVideoDeviceList = monitorVedioReportService.getDeviceListByBusinessId(businessId);
        if (monitorVideoDeviceList != null && monitorVideoDeviceList.size() > 0) {
            for (Map<String, Object> monitorVideoDeviceMap : monitorVideoDeviceList) {
                if(StringUtil.isNotNull(devNo)&&!StringUtil.null2Empty(monitorVideoDeviceMap.get("zonedevno")).contains(devNo)){
                    continue;
                }
                Map<String, Object> deviceMap = new HashMap<String, Object>();
                deviceMap.put("msgNo", monitorVideoDeviceMap.get("msgNo"));//终端8位设备号
                deviceMap.put("zonedevno", monitorVideoDeviceMap.get("zonedevno"));//终端8位设备号
                deviceMap.put("scheduleName", monitorVideoDeviceList.get(0).get("scheduleName"));//终端名称
                deviceMap.put("devName", monitorVideoDeviceMap.get("devName"));//终端名称
                deviceMap.put("regionId", monitorVideoDeviceMap.get("regionid"));//终端行政区域
                deviceMap.put("svrId", monitorVideoDeviceMap.get("svrNo"));//终端所在的服务器ID
                deviceMap.put("dstZoneno", monitorVideoDeviceMap.get("zoneno"));//终端所在的区域
                deviceList.add(deviceMap);
            }
        }
    }

    private void meetingLog(String businessId, String businessType, Map<String, Object> maps) {
        if ("1".equals(businessType)) {//只有会议有日志
            StringBuilder logStringBuilder = new StringBuilder();
            Map<String, Object> result = RestClient.postUrl(sysConfig.getHgUrl() + String.format(WsConstants.URL_CMS_MEET_LOGLIST, businessId), null, null);
            boolean state = (boolean) result.get("result");
            if (state == true) {
                List<Map<String, Object>> logList = (List<Map<String, Object>>) result.get("list");
                if (logList != null && logList.size() > 0) {
                    for (Map<String, Object> logMap : logList) {
                        logStringBuilder.append(logMap.get("login_name")).append("-").append(logMap.get("create_time"))
                                .append("-").append(logMap.get("operate")).append("\n\r");
                    }
                }
            }
            maps.put("logName", "日志追溯");
            maps.put("log",logStringBuilder.toString());
        }
    }

    private void initMap(Map<String, Object> map, String source) {
        map.put(source+"terstate","");
        map.put(source+"audioret","");
        map.put(source+"videoret","");
        map.put(source+"alostpacket","");
        map.put(source+"version","");
        map.put(source+"vlostpacket","");
        map.put(source+"svrstate","");
        map.put(source+"forwarding","");
    }

    private void region(List<Map<String, Object>> deviceList, int i, Map<String, Object> map, String source) {
        String regionId = StringUtil.null2Empty(deviceList.get(i).get(StringUtil.isNotNull(source)?source+"Regionid":"regionId"));
        if (StringUtil.isNotNull(regionId)) {
            //省级行政区域ID
            regionId = regionId.substring(0, 2) + "0000000000";
            RegionVO region = this.serachService.getRegionByRegionId(regionId);
            if (region != null) {
                map.put(source+"regionName",region.getName());
            }
            ReturnData linkDetails = this.senseService.getLinkDetails(regionId);
            SetProvRateVO provRate = ReturnDataUtil.getExtraJsonObject(linkDetails, SetProvRateVO.class);//获取省服务器带宽
            map.put(source+"upAllRate",provRate.getUpAllRate());
            map.put(source+"downAllRate",provRate.getDownAllRate());
        }else{
            map.put(source+"regionName","");
            map.put(source+"upAllRate","");
            map.put(source+"downAllRate","");
        }
    }

    private Boolean device(Integer serverRegionIdLength, Map<String, Object> map, Map<String, Object> allInfoMap, String source) {
        Boolean isFlag = true;
        String terstate = StringUtil.null2Empty(allInfoMap.get("terstate"));
        if (!allInfoMap.containsKey("svrstate")) {
//                                elevenCellValue = "服务器监测异常:未知";
        } else {
            String svrstate = StringUtil.null2Empty(allInfoMap.get("svrstate"));
            if (svrstate.equals("0")) {//服务器离线
                map.put(source+"svrstate","服务器离线");
                isFlag = false;
            } else if (svrstate.equals("1")) {
                map.put(source+"svrstate","服务器在线");
            } else {
//                                    elevenCellValue = "服务器在线检测失败:未知";
            }
        }
        if (!allInfoMap.containsKey("svrregionid") || !allInfoMap.containsKey("zfregionid")) {
//                                twelveCellValue = "服务器转发配置检测失败:未知";
        } else {
            String serverRegionId = StringUtil.null2Empty(allInfoMap.get("svrregionid"));//服务器行政区域ID
            String forwardServerRegionId = StringUtil.null2Empty(allInfoMap.get("zfregionid"));//转发服务器行政区域ID
            serverRegionId = serverRegionId.substring(0, serverRegionIdLength);//客户想看几位行政区域
            forwardServerRegionId = forwardServerRegionId.substring(0, serverRegionIdLength);//客户想看几位行政区域
            //服务器行政区域ID和转发服务器行政区域ID不匹配
            if (!serverRegionId.equals(forwardServerRegionId)) {
                map.put(source+"forwarding","服务器转发配置错误");
                isFlag = false;
            } else {
                map.put(source+"forwarding","服务器转发配置正常");
            }
        }
        if (terstate.equals("0")) {//终端离线
            map.put(source+"terstate","终端离线");
            isFlag = false;
            return isFlag;
        } else if (terstate.equals("1")) {//终端在线
            map.put(source+"terstate","终端在线");
        } else {
//                                fourCellValue = "终端在线检测失败:未知";
        }

        //音频无流
        if (!allInfoMap.containsKey("audioret")) {
//                                fiveCellValue = "终端音频流监测失败:未知";
        } else if (StringUtil.null2Empty(allInfoMap.get("audioret")).equals("1")) {
            map.put(source+"audioret","终端音频无流:第" + StringUtils.join(((JSONObject) allInfoMap).getJSONArray("audiolist"), "/") + "路无流");
            isFlag = false;
        } else if (StringUtil.null2Empty(allInfoMap.get("audioret")).equals("0")) {
            map.put(source+"audioret","终端音频流正常");
        } else if (StringUtil.null2Empty(allInfoMap.get("audioret")).equals("-1")) {
            map.put(source+"audioret","");
        }
        //视频无流
        if (!allInfoMap.containsKey("videoret")) {
//                                sixCellValue = "终端视频流监测失败:未知";
        } else if (StringUtil.null2Empty(allInfoMap.get("videoret")).equals("1")) {
            map.put(source+"videoret","终端视频无流:第" + StringUtils.join(((JSONObject) allInfoMap).getJSONArray("videolist"), "/") + "路无流");
            isFlag = false;
        } else if (StringUtil.null2Empty(allInfoMap.get("videoret")).equals("0")) {
            map.put(source+"videoret","终端视频流正常");
        } else if (StringUtil.null2Empty(allInfoMap.get("videoret")).equals("-1")) {
            map.put(source+"videoret","");
        }
        if (!allInfoMap.containsKey("alostpacket")) {
//                                sevenCellValue = "终端音频流监测失败:未知";
        } else if (StringUtil.null2Empty(allInfoMap.get("alostpacket")).equals("-1")) {
            map.put(source+"alostpacket","");
        } else {
            map.put(source+"alostpacket","终端音频丢包率" + allInfoMap.get("alostpacket") + "%");
        }
        if (!allInfoMap.containsKey("vlostpacket")) {
//                                eightCellValue = "终端视频丢包率监测失败:未知";
        } else if (StringUtil.null2Empty(allInfoMap.get("vlostpacket")).equals("-1")) {
            map.put(source+"vlostpacket","");
        } else {
            map.put(source+"vlostpacket","终端视频丢包率" + allInfoMap.get("alostpacket") + "%");
        }
        if (!allInfoMap.containsKey("version")) {
//                                nineCellValue = "终端版本监测失败:未知";
        } else if (StringUtil.null2Empty(allInfoMap.get("version")).equals("1")) {//版本相同
            map.put(source+"version","终端版本正常");
        } else if (StringUtil.null2Empty(allInfoMap.get("version")).equals("0")) {
            map.put(source+"version","终端版本不一致：当前版本为" + (allInfoMap.containsKey("errversion")?StringUtil.null2Empty(allInfoMap.get("errversion")):"") +
                    "，推荐版本为" + (allInfoMap.containsKey("sucversion")?StringUtil.null2Empty(allInfoMap.get("sucversion")):""));
            isFlag = false;
        } else if (allInfoMap.get("version").toString().equals("-1")) {
//                                nineCellValue = "终端版本监测失败:未知";
        }
        return isFlag;
    }

    /**
     * 调用网管接口，返回所有的信息（超过20个）
     *
     * @param deviceIdList 8位设备号的集合
     * @return
     */
    @Override
    public List<Map<String, Object>> getAllInfo(List<String> deviceIdList) {
        int cycleTotal = 0;//循环的次数
        List<Map<String, Object>> allDevSvrInfoMapList = new ArrayList<>();
        if (deviceIdList != null && deviceIdList.size() > 0) {
            //计算循环操作的次数 Start
            int deviceIdsSize = deviceIdList.size();//设备ID数量
            deviceIdsSize = deviceIdsSize > WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE ? deviceIdsSize : WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE;
            cycleTotal = deviceIdsSize / WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE;//循环的次数
            if (deviceIdsSize % WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE != 0) {//不能被整除
                cycleTotal += 1;
            }
            //计算循环操作的次数 End

            //分批次操作, 默认是20条 Start
            for (int i = 0; i < cycleTotal; i++) {//循环
                List<String> childDeviceIdsParamsList = new ArrayList<String>();//分批次调用网管设备ID接口参数集合

                if (deviceIdList.size() > WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE) {
                    childDeviceIdsParamsList = deviceIdList.subList(0, WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE);//封装分批次ID集合数据
                    childDeviceIdsParamsList = new ArrayList<String>(childDeviceIdsParamsList);
                    deviceIdList.subList(0, WsConstants.WANGGUAN_OFF_LINE_BATCH_SIZE).clear();//剔除原数据
                } else {
                    childDeviceIdsParamsList = deviceIdList.subList(0, deviceIdList.size());//封装分批次ID集合数据
                    childDeviceIdsParamsList = new ArrayList<String>(childDeviceIdsParamsList);
                }

                //从网管获取终端、服务器状态（一个终端只调一次接口即可返回该终端的所有故障信息）-- start
                ReturnData devSvrResultData = this.getDevAndSvrInfo(childDeviceIdsParamsList);
                JSONObject devSvrJsonObject = JSONObject.parseObject(JSONObject.toJSONString(devSvrResultData), JSONObject.class);
                List<Map<String, Object>> devSvrList = (List<Map<String, Object>>) devSvrJsonObject.getJSONObject("data").get("items");
                allDevSvrInfoMapList.addAll(devSvrList);
            }
        }
        return allDevSvrInfoMapList;
    }

    @Override
    public ScheduleVO queryByScheduleUuid(ScheduleVO schedule) {
        return scheduleDao.qureyByScheduleUuid(schedule);
    }

    @Override
    public int updateMeetings(Map<String, Object> params) {
        return scheduleDao.updateMeetings(params);
    }

    @Override
    public int updateScheduleByUuid(ScheduleVO schedule) {
        ScheduleVO result = scheduleDao.queryByUuid(schedule.getUuid());
        SysLog sysLog = new SysLog();
        sysLog.setOperateType(3001);
        sysLog.setDescription("一键停会会议名称【"+result.getName()+"】");
        sysLog.setLoginName(getUsername());
        sysLog.setBusinessId(schedule.getUuid());
        sysLogApi.addLog(sysLog);
        return scheduleDao.updateByPrimaryKeySelective(schedule);
    }
    private String getUsername(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes!=null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

            //设置IP地址
//            sysLog.setIp(IpUtils.getIpAddr(request));
            //用户名
            String accessToken = request.getParameter("access_token");
            if(StringUtils.isNotEmpty(accessToken)) {
                ReturnData returnData = userAPI.getUser(accessToken);
                if(returnData.getErrcode()==0) {
                    return ((LinkedHashMap)((LinkedHashMap) returnData.getData()).get("extra")).get("loginName").toString();
                }
            }
        }
        return null;
    }
}
