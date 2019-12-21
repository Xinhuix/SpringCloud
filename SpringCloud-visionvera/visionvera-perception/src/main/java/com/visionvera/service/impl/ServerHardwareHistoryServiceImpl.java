package com.visionvera.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.visionvera.dao.perception.ServerHardwareHistoryDao;
import com.visionvera.dao.ywcore.SlweomsDao;
import com.visionvera.service.ServerHardwareHistoryService;
import com.visionvera.util.DateUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Administrator
 */
@Service
public class ServerHardwareHistoryServiceImpl implements ServerHardwareHistoryService {
    @Resource
    private SlweomsDao slweomsDao;
    @Resource
    private ServerHardwareHistoryDao serverHardwareHistoryDao;
    private final static String NETWORK = "network";

    @Override
    public Map<String, Object> selectServerHardwareHistory(Date startTime, Date endTime, String field, String serverUnique, Integer num) {
        List<Map<String, Object>> serverHardwareHistories = null;
        endTime = DateUtil.nextDay(endTime);
        if (!DateUtil.isToday(endTime)&&DateUtil.isEffectiveDate(startTime, 14) && DateUtil.isEffectiveDate(endTime, 14)) {
            serverHardwareHistories = serverHardwareHistoryDao.selectServerHardwareHistory(15,serverUnique, startTime, endTime);
        } else if (!DateUtil.isToday(endTime)&&DateUtil.isEffectiveDate(startTime, 60) && DateUtil.isEffectiveDate(endTime, 60)) {
            serverHardwareHistories = serverHardwareHistoryDao.selectServerHardwareHistory(30,serverUnique, startTime, endTime);
        } else if(!DateUtil.isToday(endTime)&&DateUtil.isEffectiveDate(startTime, 200) && DateUtil.isEffectiveDate(endTime, 200)){
            serverHardwareHistories = serverHardwareHistoryDao.selectServerHardwareHistory(60,serverUnique, startTime, endTime);
        }
        if (DateUtil.isToday(endTime)) {
            Map<String, Map<String, Object>> stringMapMap = slweomsDao.selectServerHardwareHistory(1, serverUnique, new Date());
            if (serverHardwareHistories == null) {
                serverHardwareHistories = new ArrayList<>();
            }
            List<String> times = DateUtil.getTimeSegment(new Date(), new Date(), 1);
            for (String time : times) {
                if (!stringMapMap.containsKey(time)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("time", time);
                    result.put("networkInRate", 0);
                    result.put("networkOutRate", 0);
                    serverHardwareHistories.add(result);
                }else{
                    serverHardwareHistories.add(stringMapMap.get(time));
                }
            }
        }
        if(serverHardwareHistories==null){
            return null;
        }
        List<String> numList = new ArrayList<>();
        List<String> times = new ArrayList<>();
        List<String> netWorkList = new ArrayList<>();
        for (Map<String, Object> serverHardwareHistory  : serverHardwareHistories) {
            if (serverHardwareHistory != null) {
                if (num != null && field.equals(NETWORK)) {
                    String networkInRates = serverHardwareHistory.get("networkInRate").toString();
                    String networkOutRates = serverHardwareHistory.get("networkOutRate").toString();
                    String[] networkInRateArray = networkInRates.split(",");
                    String networkInRate = networkInRateArray.length > num ? networkInRateArray[num] : "0";
                    String[] networkOutRateArray = networkOutRates.split(",");
                    String networkOutRate = networkOutRateArray.length > num ? networkOutRateArray[num] : "0";
                    numList.add(networkInRate);
                    netWorkList.add(networkOutRate);
                } else if (serverHardwareHistory.containsKey(field)) {
                    numList.add(serverHardwareHistory.get(field).toString());
                } else {
                    netWorkList.add("0");
                    numList.add("0");
                }
            }
            times.add(serverHardwareHistory.get("time").toString());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("time", times);
        Map<String, Object> maps = slweomsDao.selectThreshold(serverUnique);
        result.put("networkInThreshold", maps.get("networkInRate"));
        result.put("networkOutThreshold", maps.get("networkOutRate"));
        result.put("threshold", maps.get(field));
        if (num != null && field.equals(NETWORK)) {
            result.put("num", new ArrayList<>());
            result.put("networkInRate", numList);
            result.put("networkOutRate", netWorkList.size() == 0 ? numList : netWorkList);
        } else {
            result.put("num", numList);
            result.put("networkInRate", netWorkList);
            result.put("networkOutRate", netWorkList);
        }
        return result;
    }

    @Override
    public void generateHistory() {
        batchInsert(15);
        batchInsert(30);
        batchInsert(60);
    }

    /**
     * 清理历史数据
     */
    @Override
    public void deleteHistory() {
        Calendar calendar = DateUtil.getCalendar(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -15);
        serverHardwareHistoryDao.deleteHistory(calendar.getTime(),15);
        calendar.add(Calendar.DAY_OF_MONTH, -46);
        serverHardwareHistoryDao.deleteHistory(calendar.getTime(),30);
        calendar.add(Calendar.DAY_OF_MONTH, -141);
        serverHardwareHistoryDao.deleteHistory(calendar.getTime(),60);
    }

    private void batchInsert(Integer day) {
        Calendar calendar = DateUtil.getCalendar(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date now = calendar.getTime();
        List<String> times = DateUtil.getTimeSegment(now, DateUtil.nextDay(now), day);
        List<String> serverUniques = slweomsDao.selectServerBasics();
        for (String serverUnique : serverUniques) {
            Map<String, Map<String, Object>> items = slweomsDao.selectServerHardwareHistory(day, serverUnique, now);


            for (String time : times) {
                if (!items.containsKey(time)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("cpuUsed", 0);
                    map.put("diskUsed", 0);
                    map.put("ramUsed", 0);
                    map.put("networkInRate", 0);
                    map.put("networkOutRate", 0);
                    map.put("time", time);
                    map.put("serviceUnique", serverUnique);
                    items.put(time, map);
                }
            }
            for (String time : items.keySet()) {
                calendar = DateUtil.getCalendar(DateUtil.string2Date(time));
                calendar.add(Calendar.MINUTE, -15);
                List<Map<String, Object>> net = slweomsDao.selectServerHardwareHistoryMinute(serverUnique, time, calendar.getTime());
                JSONObject networkInRateJson = new JSONObject();
                JSONObject networkOutRateJson = new JSONObject();
                for (Map<String, Object> stringObjectMap : net) {
                    String[] networkInRate = stringObjectMap.get("networkInRate").toString().split(",");
                    String[] networkOutRate = stringObjectMap.get("networkOutRate").toString().split(",");
                    for (Integer i = 0; i < networkInRate.length; i++) {
                        if (networkInRateJson.containsKey(i.toString())) {
                            networkInRateJson.put(i.toString(), networkInRateJson.getDouble(i.toString()) + Double.parseDouble(networkInRate[i]));
                            networkOutRateJson.put(i.toString(), networkOutRateJson.getDouble(i.toString()) + Double.parseDouble(networkOutRate[i]));
                        } else {
                            networkInRateJson.put(i.toString(), Double.parseDouble(networkInRate[i]));
                            networkOutRateJson.put(i.toString(), Double.parseDouble(networkOutRate[i]));
                        }
                    }
                }
                StringBuilder networkOutRateSb = new StringBuilder();
                StringBuilder networkInRateSb = new StringBuilder();
                for (String key : networkOutRateJson.keySet()) {
                    networkOutRateSb.append(networkOutRateJson.getDouble(key) / net.size()).append(",");
                    networkInRateSb.append(networkOutRateJson.getDouble(key) / net.size()).append(",");
                }
                if(networkInRateSb.length()>0) {
                    items.get(time).put("networkInRate", networkInRateSb.toString());
                }
                if(networkInRateSb.length()>0) {
                    items.get(time).put("networkOutRate", networkInRateSb.toString());
                }
            }

            if (items.size() > 0) {
                serverHardwareHistoryDao.batchInsert(items, day);
            }
        }

    }
}
