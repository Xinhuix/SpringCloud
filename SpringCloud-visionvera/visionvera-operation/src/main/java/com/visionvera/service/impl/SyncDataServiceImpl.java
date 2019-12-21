package com.visionvera.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.visionvera.bean.cms.DevDetail;
import com.visionvera.bean.cms.Device;
import com.visionvera.bean.cms.ScheduleStateVO;
import com.visionvera.bean.cms.ServerInfo;
import com.visionvera.bean.cms.SummaryForm;
import com.visionvera.bean.cms.User;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.operation.SyncDataDao;
import com.visionvera.service.SyncDataService;
import com.visionvera.util.RestClient;
import com.visionvera.websocket.WebSocketPushMessage;
import com.visionvera.bean.cms.Schedule;
import com.visionvera.bean.cms.ScheduleDev;
import com.visionvera.bean.cms.Meeting;
import com.visionvera.bean.cms.ServerSyncVO;



/**
 * 
 * @ClassName: SyncDataServiceImpl
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author 谢程算
 * @date 2017年10月10日 下午3:22:54
 * 
 */
@Service
@Transactional(value = "transactionManager_operation", rollbackFor = Exception.class)
public class SyncDataServiceImpl implements SyncDataService {

	
	@Resource
	private SyncDataDao syncDataDao;
	
	private static boolean isFinished = true;
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<ServerInfo> getServerInfos(List<String> list) {
		return syncDataDao.getServerInfos(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addServerInfos(List<Map<String, Object>> list) {
		return syncDataDao.addServerInfos(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的设备所属服务器列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delServerInfos(List<String> list) {
		return syncDataDao.delServerInfos(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<Device> getDevices(List<String> list) {
		return syncDataDao.getDevices(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addDevices(List<Map<String, Object>> list) {
		return syncDataDao.addDevices(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的设备列表
	 * @author 谢程算
	 * @date 2017年10月12日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delDevices(List<String> list) {
		return syncDataDao.delDevices(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<Schedule> getSchedules(List<String> list) {
		return syncDataDao.getSchedules(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addSchedules(List<Map<String, Object>> list) {
		return syncDataDao.addSchedules(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的预约列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delSchedules(List<String> list) {
		return syncDataDao.delSchedules(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<ScheduleDev> getScheduleDevs(List<String> list) {
		return syncDataDao.getScheduleDevs(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addScheduleDevs(List<Map<String, Object>> list) {
		return syncDataDao.addScheduleDevs(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的预约设备列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delScheduleDevs(List<String> list) {
		return syncDataDao.delScheduleDevsReport(list);
	}

	/**
	 * 
	 * TODO webservice分级系统-获取子系统总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<SummaryForm> getSummarys(List<String> list) {
		return syncDataDao.getSummarys(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addSummarys(List<Map<String, Object>> list) {
		return syncDataDao.addSummarys(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的总结表列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delSummarys(List<String> list) {
		return syncDataDao.delSummarys(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<Meeting> getMeetings(List<String> list) {
		return syncDataDao.getMeetings(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/删除的会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addMeetings(List<Map<String, Object>> list) {
		return syncDataDao.addMeetings(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的会议列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delMeetings(List<String> list) {
		return syncDataDao.delMeetings(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<User> getUsers(List<String> list) {
		return syncDataDao.getUsers(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addUsers(List<Map<String, Object>> list) {
		return syncDataDao.addUsers(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报删除的用户列表
	 * @author 谢程算
	 * @date 2017年10月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delUsers(List<String> list) {
		return syncDataDao.delUsers(list);
	}
	
	/**
	 * 
	 * TODO webservice从会管子系统同步数据
	 * @author 谢程算
	 * @date 2017年10月9日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public Map<String, Object> syncData(ServerSyncVO sv) {
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			
			if(!isFinished){
				ret.put("result", false);
				ret.put("msg", "后台同步中，请稍后重试！");
				//logger.info("后台同步中，请稍后重试！");
				return ret;
			}
			isFinished = false;
			//logger.info("子系统"+sv.getDstIp()+"同步开始，时间：" + System.currentTimeMillis());
			//获取基础url
			String baseUrl = String.format(WsConstants.CMS_HOST_URL, sv.getDstIp(),sv.getDstPort());
			//获得token
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(WsConstants.KEY_CMS_USER, sv.getDstAccount());//账号
			args.put(WsConstants.KEY_CMS_PWD, sv.getDstPassword());//密码
			String token = null;
			try{
				Map<String, Object> data = RestClient.post(baseUrl + WsConstants.URL_CMS_LOGIN, null, args);
				if(data.get(WsConstants.KEY_TOKEN) == null){
					ret.put("result", false);
					ret.put("msg", data.get("errmsg"));
					//logger.error("从"+ sv.getDstIp() + "子系统同步失败：" + data.get("errmsg"));
					isFinished = true;
					return ret;
				}
				token = data.get(WsConstants.KEY_TOKEN).toString();
			}catch(Exception e){
				ret.put("result", false);
				ret.put("msg", "同步失败：获取token失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：获取token失败");
				isFinished = true;
				return ret;
			}
//			//获取子系统的行政区域ID
//			map.put("ip", sv.getDstIp());
//			String svrRegionId = syncDataDao.getSvrRegionId(map);
////			//禁用数据库外键约束
////			map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_OFF);
////			syncDataDao.setFKChk(map);
//			
//			ExecutorService executor = Executors.newCachedThreadPool();
//			//同步设备所属服务器列表
//	        MultiSyncData addServerInfos = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SERVER_INFO_GET, token, "addServerInfos");
//	        FutureTask<Map<String, Object>> futureTask1 = new FutureTask<Map<String, Object>>(addServerInfos);
//	        //同步设备列表
//	        MultiSyncData addDevices = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_DEVICE_GET, token, "addDevices");
//	        FutureTask<Map<String, Object>> futureTask2 = new FutureTask<Map<String, Object>>(addDevices);
//	        //同步预约列表
//	        MultiSyncData addSchedules = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_GET, token, "addSchedules");
//	        FutureTask<Map<String, Object>> futureTask3 = new FutureTask<Map<String, Object>>(addSchedules);
//	        //同步预约设备列表
//	        MultiSyncData addScheduleDevs = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_DEV_GET, token, "addScheduleDevs");
//	        FutureTask<Map<String, Object>> futureTask4 = new FutureTask<Map<String, Object>>(addScheduleDevs);
//	        //同步总结表列表
//	        MultiSyncData addSummarys = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SUMMARY_GET, token, "addSummarys");
//	        FutureTask<Map<String, Object>> futureTask5 = new FutureTask<Map<String, Object>>(addSummarys);
//	        //同步会议列表
//	        MultiSyncData addMeetings = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_MEETING_GET, token, "addMeetings");
//	        FutureTask<Map<String, Object>> futureTask6 = new FutureTask<Map<String, Object>>(addMeetings);
//	        //同步用户列表
//	        MultiSyncData task = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_USER_GET, token, "addUsers");
//	        FutureTask<Map<String, Object>> futureTask7 = new FutureTask<Map<String, Object>>(task);
//	        //同步预约主席，发一，发二列表
//	        MultiSyncData addScheduleState = new MultiSyncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_STATE_GET, token, "addScheduleState");
//	        FutureTask<Map<String, Object>> futureTask8 = new FutureTask<Map<String, Object>>(addScheduleState);
//	        
//	        executor.submit(futureTask1);
//	        executor.submit(futureTask2);
//	        executor.submit(futureTask3);
//	        executor.submit(futureTask4);
//	        executor.submit(futureTask5);
//	        executor.submit(futureTask6);
//	        executor.submit(futureTask7);
//	        executor.submit(futureTask8);
//	        executor.shutdown();
//	        while(true){
//		        if(executor.isTerminated()){
//					//启用数据库外键约束
//					map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_ON);
//					syncDataDao.setFKChk(map);
//					isFinished = true;
//					break;
//		        }
//		        Thread.sleep(1000);
//	        }
	        //获取子系统的行政区域ID
			map.put("ip", sv.getDstIp());
			String svrRegionId = syncDataDao.getSvrRegionId(map);
			//禁用数据库外键约束
			map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_OFF);
			syncDataDao.setFKChk(map);
			//同步设备所属服务器列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SERVER_INFO_GET, token, "addServerInfos");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步设备所属服务器列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步设备所属服务器列表失败");
				isFinished = true;
				return ret;
			}
			//同步设备列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_DEVICE_GET, token, "addDevices");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步设备列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步设备列表失败");
				isFinished = true;
				return ret;
			}
			//同步预约列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_GET, token, "addSchedules");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步预约列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步预约列表失败");
				isFinished = true;
				return ret;
			}
			//同步预约设备列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_DEV_GET, token, "addScheduleDevs");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步预约设备列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步预约设备列表失败");
				isFinished = true;
				return ret;
			}
			//同步总结表列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SUMMARY_GET, token, "addSummarys");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步总结表列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步总结表列表失败");
				isFinished = true;
				return ret;
			}
			//同步会议列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_MEETING_GET, token, "addMeetings");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步会议列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步会议列表失败");
				isFinished = true;
				return ret;
			}
			//同步用户列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_USER_GET, token, "addUsers");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步用户列表失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步用户列表失败");
				isFinished = true;
				return ret;
			}
			//同步预约主席，发一，发二列表
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_SCHEDULE_STATE_GET, token, "addScheduleState");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步预约主席，发一，发二列表失败");
				////logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步预约主席，发一，发二列表失败");
				isFinished = true;
				return ret;
			}
			//同步设备联系人信息
			ret = syncData(svrRegionId, baseUrl + WsConstants.URL_CMS_DEVICE_DETAIL_GET, token, "addDevDetail");
			if(!(Boolean) ret.get("result")){
				ret.put("msg", "同步设备联系人失败");
				//logger.error("从"+ sv.getDstIp() + "子系统同步失败：同步设备联系人失败");
				isFinished = true;
				return ret;
			}
			//启用数据库外键约束
			map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_ON);
			syncDataDao.setFKChk(map);
			isFinished = true;
	        ret.put("result", true);
			ret.put("msg", "同步" + sv.getDstIp() + "成功");
			//logger.info("子系统"+sv.getDstIp()+"同步结束，时间：" + System.currentTimeMillis());
			WebSocketPushMessage.sendToAllUser("{\"type\":\"meetStateChange\"}");// 会议状态改变
		} catch (Exception e) {
			ret.put("result", false);
			ret.put("msg", "同步失败，内部异常");
			//logger.error("从子系统" + sv.getDstIp() + "同步数据失败", e);
			isFinished = true;
			//启用数据库外键约束
			map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_ON);
			syncDataDao.setFKChk(map);
			throw new RuntimeException("运行时出错！");//为了使事务回滚
		}
		return ret;
	}

	/**
	 * 多线程同步子系统数据
	 * @author 谢程算
	 *
	 */
	class MultiSyncData implements Callable<Map<String, Object>>{
		String svrRegionId = null;
		String url = null;
		String token = null;
		String methodName = null;
		MultiSyncData(String svrRegionId, String url, String token, String methodName){
			this.svrRegionId = svrRegionId;
			this.url = url;
			this.token = token;
			this.methodName = methodName;
		}
		public Map<String, Object> call() throws Exception {
			Map<String, Object> ret = new HashMap<String, Object>();
			try{
				//禁用数据库外键约束
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("chkFlag", GlobalConstants.FOREIGN_KEY_CHECKS_OFF);
				syncDataDao.setFKChk(map);
				//同步预约主席，发一，发二列表
				ret = syncData(svrRegionId, url, token, methodName);
			} catch (Exception e) {
				ret.put("result", false);
				ret.put("msg", "同步失败，内部异常");
				//logger.error("从子系统同步数据失败", e);
				throw new RuntimeException("运行时出错！");//为了使事务回滚
			}
			return ret;
		}
	}
	
	//同步数据
	@SuppressWarnings("unchecked")
	private Map<String, Object> syncData(String svrRegionId, String url, String token, String methodName) throws Exception{
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> args = new HashMap<String, Object>();
		
		//获取子系统数据
		Map<String, Object> data = RestClient.get(url, token, args);
//		if (methodName.equals("addDevDetail") && Integer.parseInt(data.get("code").toString())== 404) {
			if (methodName.equals("addDevDetail") && (data.get("code") != null) && Integer.parseInt(data.get("code").toString())== 404) {
			data.put("result", true);
			return data;
		}
		if(data.get("errmsg") != null){
			data.put("result", false);
			return data;
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("items");
		if(list == null || list.size() == 0){
			ret.put("result", true);
			return ret;
		}
		// 剔除重复数据
		if(methodName.equals("addServerInfos")){
			ServerInfo s = new ServerInfo();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getServerIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
			    Object o = it.next().get("id");
			    if(myList.contains(o)){
			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delServerInfos(myList);
			}
		}else if(methodName.equals("addDevices")){
			Device s = new Device();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getDevIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){ 
				Map<String, Object> dev = it.next();
			    Object o = dev.get("id");
			    if(myList.contains(o)){
			    	myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			        //it.remove();//2018/12/6修改，因为sql中会有更新所以数据不能移除
			    }
			    //兼容2.15.3版本
			   /* if(dev.get("devno") == null ||dev.get("devno") == "" ||
			    		dev.get("devno").toString().equals("0") || dev.get("zoneno") == null
			    		|| dev.get("zoneno") == ""){
			    	it.remove();
			    	continue;
			    }
				if(dev.get("zonedevno") == null || dev.get("zonedevno") == ""){
					String devNo = "00000" + dev.get("devno");
					dev.put("zonedevno", "1" + dev.get("zoneno") + devNo.substring(devNo.length() - 5));
				}*/
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delDevices(myList);
			}
//			//将子系统的新数据存入本系统数据库
//			if(list != null && list.size() > 0){
//				int l = list.size();
//				int total = Math.round(l/WsConstants.MULTI_SIZE);
//				for(int k=0;k<total;k++){
//                	//将子系统的数据保存到本系统数据库
//					if(k == total - 1){
//						new MultiSyncDev(list.subList(k*WsConstants.MULTI_SIZE, l), methodName, svrRegionId).call();
//					}else{
//						new MultiSyncDev(list.subList(k*WsConstants.MULTI_SIZE, (k+1)*WsConstants.MULTI_SIZE), methodName, svrRegionId).call();
//					}
//		        }
//			}
//			ret.put("result", true);
//			return ret;
		}else if(methodName.equals("addSchedules")){
			Schedule s = new Schedule();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getScheduleIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
				Map<String, Object> schedule = it.next();
				if (schedule.get("stop_status") == null ) {
					schedule.put("stop_status", 1);
				}
				if(schedule.get("creator_name") == null ){
				   schedule.put("creator_name", "");
				}
				if(schedule.get("login_mode") == null ){
				   schedule.put("login_mode", null);
				}
			    Object o = schedule.get("uuid");
			    if(myList.contains(o)){
//			        it.remove(); //不剔除，重复时做更新
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delSchedules(myList);
			}
		}else if(methodName.equals("addScheduleDevs")){
			ScheduleDev s = new ScheduleDev();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getScheduleDevIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
				Object o = it.next().get("uuid");
			    if(myList.contains(o)){
			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delScheduleDevs(myList);
			}
		}else if(methodName.equals("addSummarys")){
			SummaryForm s = new SummaryForm();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getSummaryIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
				Map<String, Object> map = it.next();
				if(map.get("process_id") != null && map.get("process_id").toString().matches("[0-9]*")){//纯数字的process_id可能与父系统重复，去除
					it.remove();
				}
			    Object o = map.get("process_id");
			    if(myList.contains(o)){
//			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delSummarys(myList);
			}
		}else if(methodName.equals("addMeetings")){
			Meeting s = new Meeting();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getMeetingIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
			    Object o = it.next().get("uuid");
			    if(myList.contains(o)){
//			        it.remove();//不剔除，重复时做更新
			    	myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delMeetings(myList);
			}
		}else if(methodName.equals("addUsers")){
			User s = new User();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getUserIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
			    Object o = it.next().get("uuid");
			    if(myList.contains(o)){
//			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delUsers(myList);
			}
		}else if(methodName.equals("addScheduleState")){
			Schedule s = new Schedule();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getScheduleStateIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
			    Object o = it.next().get("schedule_id");
			    if(myList.contains(o)){
//			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delScheduleState(myList);
			}
		}else if(methodName.equals("addDevDetail")){
			DevDetail s = new DevDetail();
			s.setSvr_region_id(svrRegionId);
			List<String> myList = syncDataDao.getDevDetailIds(s);
			Iterator<Map<String, Object>> it = list.iterator();
			while(it.hasNext()){
			    Object o = it.next().get("devno");
			    if(myList.contains(o)){
//			        it.remove();
			        myList.remove(o);//从本系统的集合中删除本次同步子系统还有的数据，剩下的就是本系统有而子系统没有的数据，即需要本系统删除的数据
			    }
			}
			if(StringUtils.isNotBlank(svrRegionId) && myList.size() > 0){//从本系统中删除子系统中已经删除的数据
				syncDataDao.delDevDetail(myList);
			}
		}
		//将子系统的新数据存入本系统数据库
		if(list != null && list.size() > 0){
			//将子系统的数据保存到本系统数据库
			batchOp(list, methodName, svrRegionId);
		}
		ret.put("result", true);
		return ret;
	}
	
	/**
	 * 多线程同步子系统设备列表（数据量大时采用多线程入库）
	 * @author 谢程算
	 *
	 */
	class MultiSyncDev implements Callable<Integer>{
		List<Map<String, Object>> list= null;
		String methodName = null;
		String svrRegionId = null;
		MultiSyncDev(List<Map<String, Object>> list, String methodName, String svrRegionId){
			this.list = list;
			this.methodName = methodName;
			this.svrRegionId = svrRegionId;
		}
		public Integer call() throws Exception {
			int ret = 0;
			try{
				//批量入库
				ret = batchOp(list, methodName, svrRegionId);
			} catch (Exception e) {
				//logger.error("多线程保存设备列表失败", e);
				throw new RuntimeException("运行时出错！");//为了使事务回滚
			}
			return ret;
		}
	}

	private int batchOp(List<Map<String, Object>> args, String methodName, String svrRegionId) throws Exception{
		int ret = 0;
		if(args.size() > WsConstants.BATCH_SIZE){//数量太大采用分批入库
			List<Map<String, Object>> batchData = new ArrayList<Map<String, Object>>();
			int i = args.size();
			int count = (i % WsConstants.BATCH_SIZE == 0) ? (i / WsConstants.BATCH_SIZE) : (i / WsConstants.BATCH_SIZE + 1);
			for(int s = 1; s <= count; s ++){
				for(int j = (s-1) * WsConstants.BATCH_SIZE; j < s * WsConstants.BATCH_SIZE && j < i; j ++){
					args.get(j).put("svr_region_id", svrRegionId);
					batchData.add(args.get(j));
				}
				ret = reflectCall(syncDataDao, methodName, batchData);
				batchData.clear();//清空以便给下个循环用
			}
		}else{
			for(int i = 0; i < args.size(); i ++){
				args.get(i).put("svr_region_id", svrRegionId);
			}
			ret = reflectCall(syncDataDao, methodName, args);
		}
		return ret;
	}
	
	/**
	 * 通过反射调用Dao方法
	 */
	private int reflectCall(Object instance, String methodName,
			List<Map<String, Object>> args) throws Exception {
		java.lang.reflect.Method m = instance.getClass().getDeclaredMethod(
				methodName, List.class);
		return (Integer) m.invoke(instance, args);
	}

	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约主席，发一,发二列表
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public List<ScheduleStateVO> getScheduleState(List<String> list) {
		return syncDataDao.getScheduleState(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-子系统上报新增/修改的预约主席，发一,发二列表
	 * @author 谢程算
	 * @date 2018年01月04日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int addScheduleState(List<Map<String, Object>> list) {
		return syncDataDao.addScheduleState(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-获取子系统预约主席，发一,发二列表
	 * @author 周逸芳
	 * @date 2017年12月11日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int delScheduleState(List<String> list) {
		return syncDataDao.delScheduleState(list);
	}
	
	/**
	 * 
	 * TODO webservice分级系统-启用/禁用数据库外键约束
	 * @author 谢程算
	 * @date 2017年10月16日  
	 * @version 1.0.0 
	 * @param sv
	 * @return
	 */
	public int setFKChk(Map<String, Object> map) {
		return syncDataDao.setFKChk(map);
	}
	
	/**
	 * TODO webservice根据uuid或ip获取子系统的行政区域id
	 * @author 谢程算
	 * @param map
	 * @return
	 */
	public String getSvrRegionId(Map<String, Object> map) {
		return syncDataDao.getSvrRegionId(map);
	}

	/**
	 * TODO 获取设备联系人数据
	 * @author 周逸芳
	 * @param map
	 * @return
	 */
	public List<DevDetail> getDevDetail(List<String> list) {
		return syncDataDao.getDevDetail(list);
	}
}
