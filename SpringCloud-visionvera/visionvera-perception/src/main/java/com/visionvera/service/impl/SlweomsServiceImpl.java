package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.slweoms.*;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.perception.ServersDao;
import com.visionvera.dao.ywcore.PlatformProcessDao;
import com.visionvera.dao.ywcore.SlweomsDao;
import com.visionvera.service.PlatformService;
import com.visionvera.service.SlweomsService;
import com.visionvera.util.StringUtil;
import com.visionvera.vo.Slweoms;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class SlweomsServiceImpl implements SlweomsService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Resource
    private SlweomsDao slweomsDao;
    @Autowired
    private PlatformService platformService;

    @Autowired
    private PlatformProcessDao processDao;
    @Autowired
    private ServersDao serversDao;
    @Value("${sys_bit:16}")
    private Integer sysBit;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<ServerHardwareVO> getRegionServerList(
            Map<String, Object> paramMap) {
        Integer pageNum = (Integer) paramMap.get("pageNum");
        Integer pageSize = (Integer) paramMap.get("pageSize");
        List<ServerHardwareVO> serverHardwareVOList = new ArrayList<ServerHardwareVO>();
        if (pageNum != -1) {
            PageHelper.startPage(pageNum, pageSize);
            serverHardwareVOList = slweomsDao.getRegionServerList(paramMap);
            for (ServerHardwareVO serverHardwareVO : serverHardwareVOList) {
                assemblyServer(serverHardwareVO);
            }
        }
        return serverHardwareVOList;
    }

    @Override
    public List<ServerHardwareVO> getServerHardwareInfo(Map<String, Object> paramMap) {
        List<ServerHardwareVO> serverHardwareVOList = slweomsDao.getServerHardwareInfo(paramMap);

        for (ServerHardwareVO serverHardwareVO : serverHardwareVOList) {
            assemblyServer(serverHardwareVO);
        }
        return serverHardwareVOList;
    }

    @Override
    public Map<String, Object> getRegionAndServerList(Map<String, Object> paramMap) {
        //获取行政区域列表
        List<RegionVO> regionVoList = slweomsDao.getRegionList(paramMap);
        Integer gradeid = (Integer) paramMap.get("gradeid");
        if (gradeid == 0 || gradeid == 1) {
            //查询每个区域下有多少服务器（在线数/总数）
            List<Map<String, Object>> regionServerCountList = slweomsDao.getRegionServerCount(paramMap);
            Map<String, Long> regionServerCountMap = new HashMap<String, Long>();
            for (Map<String, Object> regionServerCount : regionServerCountList) {
                String districtCode = (String) regionServerCount.get("districtCode");
                Long count = (Long) regionServerCount.get("count");
                regionServerCountMap.put(districtCode, count);
            }

            List<Map<String, Object>> regionServerOnlineCountList = slweomsDao.getRegionServerOnlineCount(paramMap);
            Map<String, Long> regionServerOnlineCountMap = new HashMap<String, Long>();
            for (Map<String, Object> regionServerOnlineCount : regionServerOnlineCountList) {
                String districtCode = (String) regionServerOnlineCount.get("districtCode");
                Long onLineCount = (Long) regionServerOnlineCount.get("count");
                regionServerOnlineCountMap.put(districtCode, onLineCount);
            }

            for (RegionVO regionVO : regionVoList) {
                String regionVOId = regionVO.getId();
                Long serverCount = regionServerCountMap.get(regionVOId);
                Long serverOnlineCount = regionServerOnlineCountMap.get(regionVOId);
                regionVO.setServerCount(serverCount == null ? 0 : serverCount);
                regionVO.setServerOnlineCount(serverOnlineCount == null ? 0 : serverOnlineCount);
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (regionVoList != null && regionVoList.size() > 0) {
            resultMap.put("regions", regionVoList);
        } else {
            resultMap.put("regions", Collections.EMPTY_LIST);
        }

        List<ServerBasics> serverBasicsList = slweomsDao.getServerBasicsListOfRegion(paramMap);
        if (serverBasicsList != null && serverBasicsList.size() > 0) {
            resultMap.put("servers", serverBasicsList);
        } else {
            resultMap.put("servers", Collections.EMPTY_LIST);
        }
        return resultMap;
    }

    @Override
    public List<PlatformTypeVO> getPlatFormTypeList() {
        List<PlatformTypeVO> platformTypeList = slweomsDao.getPlatFormTypeList();
        return platformTypeList;
    }

    @Override
    public ServerBasics getServerBasicsById(Integer id) {
        if (id == null) {
            return null;
        }
        ServerBasics serverBasics = slweomsDao.getServerBasicsById(id);
        if (serverBasics != null) {
            Integer bits = serversDao.selectYwTreeByUUid(serverBasics.getYwUuid());
            serverBasics.setBits(bits==null?sysBit:bits);
            //拼接服务器平台名称
            List<PlatformVO> platformVOList = serverBasics.getPlatformVOList();

            StringBuilder tposStateBuilder = new StringBuilder();
            Integer serverOnLine = serverBasics.getServerOnLine();

            String tposName = "";
            String tposRegisterid = "";
            String tposState = "";
            StringBuilder tposNameBuilder = new StringBuilder();
            StringBuilder tposRegisteridBuilder = new StringBuilder();
            for (PlatformVO platformVO : platformVOList) {
                tposNameBuilder.append(platformVO.getTposName()).append(",");
                tposRegisteridBuilder.append(platformVO.getTposRegisterid()).append(",");
                //tposStateBuilder.append(platformVO.getTposProcessState()).append(",");
                if (GlobalConstants.SERVER_OFFLINE_STATE.equals(serverOnLine)) {
                    tposStateBuilder.append(1).append(",");
                } else {
                    int num = platformService.getProcessExceptionCount(platformVO.getTposRegisterid());
                    if (num > 0) {
                        tposStateBuilder.append(1).append(",");
                    } else {
                        tposStateBuilder.append(0).append(",");
                    }
                }
            }
            if (tposNameBuilder.length() > 0) {
                tposName = tposNameBuilder.substring(0, tposNameBuilder.length() - 1);   //去掉最后一个逗号
                tposRegisterid = tposRegisteridBuilder.substring(0, tposRegisteridBuilder.length() - 1);
                tposState = tposStateBuilder.substring(0, tposStateBuilder.length() - 1);
            }
            serverBasics.setServerPlatforms(tposName);
            serverBasics.setPlatformsRegisterid(tposRegisterid);
            serverBasics.setPlatformProcessState(tposState);

            //查询服务器所在行政区域位置
            String serverDistrict = serverBasics.getServerDistrict();
            String[] districtCodeArr = serverDistrict.split(",");
            StringBuilder districtBuilder = new StringBuilder();
            //String districtName = "";
            for (String districtCode : districtCodeArr) {
                RegionVO regionVO = slweomsDao.getRegionVOById(districtCode);
                districtBuilder.append(regionVO.getName()).append(",");
            }
			/*if(districtBuilder.length() > 0) {
				districtName = districtBuilder.substring(0, districtBuilder.length()-1);
			}*/
            serverBasics.setServerDistrictName(districtBuilder.toString());

            //修改ip和mac的对应关系
            String netWorkMacAddr = serverBasics.getNetWorkMacAddr();
            String serverNETIP = serverBasics.getServerNETIP();
            Map<String, List<String>> macAndIPMap = new HashMap<String, List<String>>();
            if (netWorkMacAddr != null) {
                String[] macAddrArr = netWorkMacAddr.split(",");
                String[] ipArr = serverNETIP.split(",");
                for (int i = 0; i < macAddrArr.length; i++) {
                    String macAddr = macAddrArr[i];
                    List<String> ipList = macAndIPMap.get(macAddr);
                    if (ipList == null) {
                        ipList = new ArrayList<String>();
                    }
                    ipList.add(ipArr[i]);
                    macAndIPMap.put(macAddr, ipList);
                }
            }
            serverBasics.setMacAndIpMap(macAndIPMap);
        }
        return serverBasics;
    }

    @Override
    public Map<String, Object> getAllServerCount() {
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        try {
            List<Map<String, Object>> onLineMapList = slweomsDao.getAllServerCount();
            Long serverCount = 0L;
            Long serverOnLineCount = 0L;
            for (Map<String, Object> map : onLineMapList) {
                Integer serverOnlineState = (Integer) map.get("serverOnLine");
                Long serverNum = (Long) map.get("num");
                serverCount += serverNum;
                if (serverOnlineState.equals(GlobalConstants.SERVER_ONLINE_STATE)) {
                    serverOnLineCount = serverNum;
                }
            }
            resultMap.put("errcode", 0);
            resultMap.put("errmsg", "查询全部服务器数量成功");

            Map<String, Object> dataMap = new HashMap<String, Object>();

            Map<String, Object> extraMap = new HashMap<String, Object>();
            extraMap.put("serverCount", serverCount);
            extraMap.put("serverOnLineCount", serverOnLineCount);
            dataMap.put("extra", extraMap);

            resultMap.put("data", dataMap);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultMap.put("errcode", 0);
            resultMap.put("errmsg", "查询全部服务器数量异常");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> handleProcess(String registerid, String method) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        ServerBasics serverBasics = slweomsDao.getServerBasicsByRegisterid(registerid);
        List<PlatformProcess> processes = processDao.getProcessByTposRegisterid(registerid);

        List<PlatformProcess> processList = new ArrayList<PlatformProcess>();
        for (PlatformProcess platformProcess : processes) {
            if (GlobalConstants.PROCESS_STATUS_PROBLEM.equals(platformProcess.getProcessStatus())) {
                processList.add(platformProcess);
            }
        }

        if (processList.size() == 0) {
            resultMap.put("errorcode", 0);
            resultMap.put("errmsg", "操作平台进程成功");
            return resultMap;
        }

        processDao.recoverProcessStatusBatch(processList);
        String serverManageIp = serverBasics.getServerManageIp();
        Integer port = serverBasics.getPort();
        String url = "http://" + serverManageIp + ":" + port + "/serverwatch/process/" + method + "/handleProcesses";
        Map resultStr = restTemplate.postForObject(url, processList, Map.class);
        if (resultStr.get("result") != null && (Boolean) resultStr.get("result") == true) {
            resultMap.put("errorcode", 0);
            resultMap.put("errmsg", "操作平台进程成功");
            return resultMap;
        }
        resultMap.put("errorcode", 1);
        resultMap.put("errmsg", "操作平台进程失败");
        return resultMap;
    }

    /**
     * 获取应用服务器
     *
     * @param pageSize
     * @param pageNum
     * @param slweoms
     */
    @Override
    public JSONObject server(Integer pageSize, Integer pageNum, Slweoms slweoms) {
        List<RegionVO> regionVOList = new ArrayList<>();
        if (slweoms.getGradeId() != 2) {
            regionVOList = slweomsDao.selectRegion(slweoms);
        }
        List<PlatformTypeVO> platformTypeList = slweomsDao.getPlatFormTypeList();
        if (pageNum != -1) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<ServerHardwareVO> serverHardwareVOList = slweomsDao.selectRegionServerList(slweoms);
        List<ServerHardwareVO>  regionServerHardwareVOList =new ArrayList<>();
        for (ServerHardwareVO serverHardwareVO : serverHardwareVOList) {
            assemblyServer(serverHardwareVO);
            String[] serverDistricts = serverHardwareVO.getServerDistrict().split(",");
            int length = serverDistricts.length;
            if(slweoms.getDistrict()!=null && (slweoms.getDistrict().equals(serverHardwareVO.getServerDistrict())
                    ||slweoms.getDistrict().equals(serverDistricts[length -1])
                    ||(slweoms.getDistrict().equals(length>1?serverDistricts[length -2]:"")&&slweoms.getGradeId()==2))){
                regionServerHardwareVOList.add(serverHardwareVO);
            }
        }
        JSONObject map = new JSONObject();
        map.put("regions", regionVOList);
        map.put("regionServer", regionServerHardwareVOList);
        map.put("platformType", platformTypeList);
        map.put("serverHardware", serverHardwareVOList);
        PageInfo<ServerHardwareVO> serverHardwareVOPageInfo = new PageInfo<>(serverHardwareVOList);
        JSONObject dataMap = new JSONObject();
        dataMap.put("items", map);
        JSONObject extra = new JSONObject();
        extra.put("pageNum", pageNum);
        extra.put("pageSize", pageSize);
        extra.put("pages", serverHardwareVOPageInfo.getPages());
        extra.put("total", serverHardwareVOPageInfo.getTotal());
        dataMap.put("extra", extra);
        return dataMap;
    }

    private void assemblyServer(ServerHardwareVO serverHardwareVO) {
        StringBuilder tposStateBuilder = new StringBuilder();
        StringBuilder tposNameBuilder = new StringBuilder();
        StringBuilder tposResgisteridBuilder = new StringBuilder();
        List<PlatformVO> platformVOList = slweomsDao.getPlatFormVOListByServerKey(serverHardwareVO.getServerUnique());
        Integer serverOnLine = serverHardwareVO.getServerOnLine();
        for (PlatformVO platformVO : platformVOList) {
            tposNameBuilder.append(platformVO.getTposName()).append(",");
            tposResgisteridBuilder.append(platformVO.getTposRegisterid()).append(",");
            if (GlobalConstants.SERVER_OFFLINE_STATE.equals(serverOnLine)) {
                tposStateBuilder.append(1).append(",");
            } else {
                int num = platformService.getProcessExceptionCount(platformVO.getTposRegisterid());
                if (num > 0) {
                    tposStateBuilder.append(1).append(",");
                } else {
                    tposStateBuilder.append(0).append(",");
                }
            }
        }
        if (tposNameBuilder.length() > 0) {
            serverHardwareVO.setServerPlatforms(tposNameBuilder.substring(0, tposNameBuilder.length() - 1));
            serverHardwareVO.setPlatformsRegisterid(tposResgisteridBuilder.substring(0, tposResgisteridBuilder.length() - 1));
            serverHardwareVO.setPlatformProcessState(tposStateBuilder.substring(0, tposStateBuilder.length() - 1));
        }

        //统计内存总使用量
        String serverHDDUsage = serverHardwareVO.getServerHDDUsage();
        Long serverHDDUsageLong = 0L;
        if (StringUtil.isNotNull(serverHDDUsage)) {
            String[] hddUsages = serverHDDUsage.split(",");
            for (String hddUsage : hddUsages) {
                serverHDDUsageLong += Long.parseLong(hddUsage);
            }
        }
        serverHardwareVO.setServerHDDUsageLong(serverHDDUsageLong);

        //获取行政区域名称
        String serverDistrict = serverHardwareVO.getServerDistrict();
        String districtName = "";
        String[] districtArr = serverDistrict.split(",");
        for (int i = 0; i < districtArr.length; i++) {
            RegionVO regionVO = slweomsDao.getRegionVOById(districtArr[i]);
            districtName += regionVO.getName();
        }
        serverHardwareVO.setServerSite(districtName + serverHardwareVO.getServerSite());
    }
}
