package com.visionvera.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.constrant.GlobalConstants;
import com.visionvera.dao.JRedisDao;
import com.visionvera.enums.TransferType;
import com.visionvera.netty.NettyTcpClient;
import com.visionvera.service.PlatformProcessService;
import com.visionvera.service.PlatformService;
import com.visionvera.service.PlatformTypeService;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.service.TerminalInfoService;
import com.visionvera.service.WatchProbeService;
import com.visionvera.util.ProbeManagerMsgUtil;
import com.visionvera.util.SpringContextUtil;
import com.visionvera.util.StringUtil;

/**
 * 运维工作站WebSocket
 * author:dql
 */
@ServerEndpoint(value = "/websocket/operation/{userId}")
@Component
public class WebSocketPushMessage {

    private static Logger logger = LoggerFactory.getLogger(WebSocketPushMessage.class);

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<String, Session>();

    /**
     * @return the webSocketMap
     */
    public static ConcurrentHashMap<String, Session> getWebSocketMap() {
        return webSocketMap;
    }

    private ServerBasicsService serverBasicsService;

    private PlatformProcessService processService;

    private PlatformService platformService;

    private JRedisDao jRedisDao;

    private WatchProbeService probeService;

    private PlatformTypeService platformTypeService;

    private TerminalInfoService terminalInfoService;

    private String userId;

    public static final Integer EXPIRE_SECONDS = 60;

    /**
     * 连接建立成功调用的方法
     *
     * @param session
     *            可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     * @throws IOException
     * @throws InterruptedException
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "userId") String userId) throws IOException{
        if (webSocketMap.get(userId) != null) {
            webSocketMap.get(userId).close();// 同一个用户若有多个连接，则关闭之前的连接
        }

        platformService = SpringContextUtil.getBean(PlatformService.class);
        jRedisDao = SpringContextUtil.getBean(JRedisDao.class);
        serverBasicsService = SpringContextUtil.getBean(ServerBasicsService.class);
        processService = SpringContextUtil.getBean(PlatformProcessService.class);
        probeService = SpringContextUtil.getBean(WatchProbeService.class);
        platformTypeService = SpringContextUtil.getBean(PlatformTypeService.class);
        terminalInfoService = SpringContextUtil.getBean(TerminalInfoService.class);
        this.userId = userId;

        webSocketMap.put(userId, session);// 放入新的连接
        logger.info("有新连接加入！当前在线人数为" + webSocketMap.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("用户id为" + userId +"的session关闭");
        webSocketMap.remove(userId);
        logger.info("有一连接关闭！当前在线人数为" + webSocketMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     *            客户端发送过来的消息,json格式
     * @param session
     *            可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("WebSocket服务器接收到来自客户端" + session.getId() + "的消息:" + message);
        try {
            logger.info("接收到用户"+userId+"的信息："+message);
            handleReceiveMsg(message,session);
        } catch (Exception e) {
            logger.error("向客户端返回请求结果时失败", e);
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误", error);
        try {
            sendMessage(session, "发生错误");
        } catch (IOException e) {
        	 logger.error("发生错误", e);
        }
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param session
     * @param message
     * @throws IOException
     */
    public static void sendMessage(Session session, String message)
            throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /***
     *
     * @Description: 推送消息给指定登录用户
     * @param message
     * @author 褚英奇
     * @throws InterruptedException
     * @date 2017年9月5日 13:46:53
     */
    public static void sendToUser(String userId, String message) {
        try {
            if (webSocketMap != null && webSocketMap.get(userId) != null) {
                logger.info("待发送的消息：" + message);
                WebSocketPushMessage.sendMessage(webSocketMap.get(userId),
                        message);
            }
        } catch (Exception e) {
            logger.error("向客户端[" + webSocketMap.get(userId).getId()
                    + "]发送的消息失败", e);
            try {// 关闭之前的连接
                webSocketMap.get(userId).close();
            } catch (IOException e1) {
                logger.error("推送消息失败", e1);
            }
            webSocketMap.remove(userId);
            return;
        }
    }


    /***
     *
     * @Description: 推送消息给所有登录用户
     * @param message
     */
    public static void sendToAllUser(String message) {
        logger.info("待发送的消息：" + message);
        String key = "";
        try {
            logger.debug("开始进入循环:" + webSocketMap.size() + "\t keySet测试: " + webSocketMap.keySet());
            for(String k : webSocketMap.keySet()){
                logger.debug("已经进入循环:" + key);
                key = k;
                WebSocketPushMessage.sendMessage(webSocketMap.get(key),
                        message);
                logger.debug("逻辑代码执行完成:" + key);
            }
            logger.debug("即将退出方法:" + key);
        } catch (Exception e) {
            logger.error("向客户端[" + webSocketMap.get(key).getId()
                    + "]发送的消息失败", e);
            try {// 关闭之前的连接
                webSocketMap.get(key).close();
            } catch (IOException e1) {
                logger.error("关闭连接失败", e1);
            }
            webSocketMap.remove(key);
            return;
        }
    }

    /**
     * 处理websocket请求信息
     * @param message websocket请求信息，json格式 {"func":"xxx",.....}
     */
    private void handleReceiveMsg(String message,Session session) throws Exception {
        JSONObject msgObj = JSONObject.parseObject(message);
        msgObj.put("userId", userId);
        String func = msgObj.getString("func");
        Map<String, Object> resultMap = new HashMap<String,Object>();
        switch (func) {
            //获取存储网关探针最新版本
            case ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC:
                resultMap = getProbeRecnetVersion("web");
                resultMap.put("func", ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC);
                break;
            case ProbeManagerMsgUtil.TCP_DOWNLOAD_PROBE:
                resultMap = syncV2vProbeInstallPac("web");
                resultMap.put("func", ProbeManagerMsgUtil.TCP_DOWNLOAD_PROBE);
                break;
             //开启或关闭探针        
            case ProbeManagerMsgUtil.PROBE_OPEN_STATE:
                resultMap = updateProbeOpenState(msgObj);
                resultMap.put("func", ProbeManagerMsgUtil.PROBE_OPEN_STATE);
                break;
            //v2v探针升级    
            case ProbeManagerMsgUtil.PROBE_UPGRADE:
                JSONArray servers = msgObj.getJSONArray("servers");
                String version = msgObj.getString("version");
                List<String> uuidList = JSONObject.parseArray(servers.toJSONString(), String.class);
                upGradeProbe(uuidList, version);
                break;
            //获取探针版本号(检测探针)   
            case ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION:
                resultMap = getProbeVersion(msgObj.getString("server"));
                resultMap.put("func", ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION);
                break;
            //添加探针    
            case ProbeManagerMsgUtil.ADD_PROC:
                resultMap = addProcess(msgObj.getObject("proc", PlatformProcess.class));
                resultMap.put("func", ProbeManagerMsgUtil.ADD_PROC);
                break;
           /* case ProbeManagerMsgUtil.MODIFY_PROC:
                resultMap = modifyProcess(msgObj.getObject("proc", PlatformProcess.class));
                break;*/
            //删除进程    
            case ProbeManagerMsgUtil.DEL_PROC:
                resultMap = delProcess(msgObj);
                //resultMap.put("func", ProbeManagerMsgUtil.DEL_PROC);
                break;
            //感知中心一键重启进程    
            case GlobalConstants.PLATFORM_HANDLE:
                resultMap = platformHandle(msgObj);
                resultMap.put("func",  GlobalConstants.PLATFORM_HANDLE);
                break;
            /*    
            case GlobalConstants.PROCESS_HANDLE:
                resultMap = processHandle(msgObj.getInteger("procId"), msgObj.getString("method"));
                resultMap.put("func", GlobalConstants.PROCESS_HANDLE);
                break;
            case ProbeManagerMsgUtil.ADD_PLATFORM:
                resultMap = addV2vProbePlatform(msgObj.getObject("platform", PlatformVO.class));
                resultMap.put("func", ProbeManagerMsgUtil.ADD_PLATFORM);
                break;
            case ProbeManagerMsgUtil.MODIFY_PLATFORM:
                resultMap = modifyV2vProbePlatform(msgObj.getObject("platform", PlatformVO.class));
                resultMap.put("func", ProbeManagerMsgUtil.MODIFY_PLATFORM);
                break;
            case ProbeManagerMsgUtil.DEL_PLATFORM:
                resultMap = removeV2vProbePlatform(msgObj.getString("tposRegisterid"));
                resultMap.put("func", ProbeManagerMsgUtil.DEL_PLATFORM);
                break;*/
            //删除探针    
            case ProbeManagerMsgUtil.DEL_PROBE:
                resultMap = delServer(msgObj.getString("serverUnique"));
                resultMap.put("func", ProbeManagerMsgUtil.DEL_PROBE);
                break;
            //测试登录    
            case GlobalConstants.IP_SERVER_LOGIN_BATCH:
                JSONArray idArr = msgObj.getJSONArray("ids");
                List<Integer> idList = JSONObject.parseArray(idArr.toJSONString(), Integer.class);
                List<Map<String, Object>> resultList = probeService.testRemoteLoginBatch(idList);
                resultMap.put("func", GlobalConstants.IP_SERVER_LOGIN_BATCH);
                resultMap.put("list", resultList);
                break;
            //部署探针        
            case GlobalConstants.IP_PROBE_DISPLAY:
                Integer id = msgObj.getInteger("id");
                version = msgObj.getString("version");
                resultMap = probeService.displayWatchProbe(id, version);
                resultMap.put("func", GlobalConstants.IP_PROBE_DISPLAY);
                resultMap.put("id", id);
                break;
            //升级IP探针    
            case GlobalConstants.IP_PROBE_UPGRADE:
                id = msgObj.getInteger("id");
                version = msgObj.getString("version");
                resultMap = probeService.upGradeWatchProbe(id, version);
                resultMap.put("func", GlobalConstants.IP_PROBE_UPGRADE);
                resultMap.put("id", id);
                break;
            //删除探针    
            case GlobalConstants.IP_PROBE_REMOVE:
                id = msgObj.getInteger("id");
                resultMap = probeService.removeWatchProbe(id, userId);
                resultMap.put("func", GlobalConstants.IP_PROBE_REMOVE);
                resultMap.put("id", id);
                break;
            //修改探针
            case ProbeManagerMsgUtil.MODIFY_PROBE:
                resultMap = modifyProbe(msgObj);
                resultMap.put("func", ProbeManagerMsgUtil.MODIFY_PROBE);
                break;
            case GlobalConstants.GET_SUGGESTED_PLATFORM_VERSION:
                resultMap = getSuggestedPlatformVersion(msgObj);
                resultMap.put("func", GlobalConstants.GET_SUGGESTED_PLATFORM_VERSION);
                break;
           //进程状态开关
            case ProbeManagerMsgUtil.PROCESS_STATUS_CHECKINFO:
            	resultMap = processStatusCheckInfo(msgObj);
            	resultMap.put("func", ProbeManagerMsgUtil.PROCESS_STATUS_CHECKINFO);
            	break;    
           //进程重启
            case ProbeManagerMsgUtil.PROCESS_RESTART:
            	resultMap = processRestart(msgObj);
            	resultMap.put("func", ProbeManagerMsgUtil.PROCESS_RESTART);
            	break;    
            default:
        }
        if (!resultMap.isEmpty()) {
        	logger.error("handleReceiveMsg---resultMap--返回结果--:"+JSONObject.toJSONString(resultMap));
            sendMessage(session, JSONObject.toJSONString(resultMap));
        }
    }

    /**
     * 获取平台推荐版本
     * @param msgObj
     * @return
     */
    private Map<String, Object> getSuggestedPlatformVersion(JSONObject msgObj) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            int platformType = msgObj.getInteger("platformType");
            String funcName = "";
            switch(platformType) {
                case 9: funcName = "get_xz_version"; break;
                default: break;
            }
            JSONObject dataJson = new JSONObject();
            dataJson.put("funcName",funcName);
            dataJson.put("param","");
            String result = NettyTcpClient.sendMsg(ProbeManagerMsgUtil.probeManageIp,dataJson.toJSONString());
            if(ProbeManagerMsgUtil.getTcpResult(result)) {
                msgObj = JSONObject.parseObject(result);
                resultMap.put("errcode",0);
                resultMap.put("msg",msgObj.getString("msg"));
                resultMap.put("version",msgObj.getString("version"));
            }else {
                resultMap.put("errcode",1);
                resultMap.put("msg","获取推荐版本失败");
            }
        } catch(Exception e) {
            logger.error("获取平台推荐版本异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","获取平台推荐版本异常");
        }
        return resultMap;
    }

    /**
     * 修改服务器
     * @param msgObj
     */
    private Map<String,Object> modifyProbe(JSONObject msgObj) {
        Map<String,Object> resultMap = new HashMap<String,Object>(4);
        ServerBasics serverBasics = JSONObject.parseObject(msgObj.getString("param"), ServerBasics.class);
        try {
            String uuid = serverBasics.getServerUnique();

            ServerBasics oldServerBasics = serverBasicsService.getServerBasicsByServerUnique(uuid);
            serverBasics.setState(oldServerBasics.getState());

            String transferType = serverBasics.getTransferType();
            if(TransferType.V2V.getTransferType().equals(transferType)) {
                if(isModifyV2VProperties(serverBasics,oldServerBasics)) {
                    int num = serverBasicsService.getServerCountByTerminalCodeExcludeSelf(serverBasics.getTerminalCode(),serverBasics.getId());
                    if(num > 0) {
                        resultMap.put("errcode",1);
                        resultMap.put("msg","填写的虚拟终端号码重复");
                        return resultMap;
                    }else if(GlobalConstants.REUSE_XZ_TERNO_NO == serverBasics.getReuseXzNo()) {
                        //验证终端号是否有效
                        Integer integer = terminalInfoService.selectTerminalInfo(serverBasics.getV2vNetMac().replace(":",""),serverBasics.getTerminalCode());
                        if(integer == 0) {
                            resultMap.put("errcode",1);
                            resultMap.put("msg","填写的虚拟终端号码和视联网虚拟mac无效");
                            return resultMap;
                        }
                    }

                    boolean retFlag = ProbeManagerMsgUtil.modifyConfig(serverBasics,userId);
                    if(retFlag) {
                    	  serverBasicsService.updateServerBasics(serverBasics);
                          resultMap.put("errcode",0);
                          resultMap.put("msg","服务器信息修改成功");
                    }else {
                        jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROBE
                                +":"+uuid+":::"+userId);
                        resultMap.put("errcode",1);
                        resultMap.put("msg","服务器信息修改失败");
                    }
                } else {
                   serverBasicsService.updateServerBasics(serverBasics);
                   resultMap.put("errcode",0);
                   resultMap.put("msg","服务器信息修改成功");
                }
            } else {
                //IP
            	String oldServerManageIp = oldServerBasics.getServerManageIp();
    			String serverManageIp = serverBasics.getServerManageIp();
    			logger.info("operation服务IP地址是否改变"+serverManageIp.equals(oldServerManageIp));
    			if(!serverManageIp.equals(oldServerManageIp)) {
    				 jRedisDao.set(ProbeManagerMsgUtil.MODIFY_PROBE+":"+uuid+":"+userId,JSONObject.toJSONString(serverBasics),
                             60*60);
                     jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROBE
                             +":"+uuid+":::"+userId,"",EXPIRE_SECONDS);
                     boolean retFlag = ProbeManagerMsgUtil.modifyConfig(serverBasics,userId);
                     if(retFlag) {
                    	 serverBasicsService.updateServerBasics(serverBasics);
                         resultMap.put("errcode",0);
                         resultMap.put("msg","服务器信息修改成功");
                     }else {
                         jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROBE
                                 +":"+uuid+":::"+userId);
                         resultMap.put("errcode",1);
                         resultMap.put("msg","服务器信息修改失败");
                     }
    			} else {
                    serverBasicsService.updateServerBasics(serverBasics);
                    resultMap.put("errcode",0);
                    resultMap.put("msg","服务器信息修改成功");
                 }
            }


        } catch (Exception e) {
            logger.error("服务器信息修改异常",e);
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROBE
                    +":"+serverBasics.getServerUnique()+":::"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg", "服务器信息修改异常");
        }

        return resultMap;
    }


    /**
     * 获取探针版本号
     * @param uuid
     */
    private Map<String, Object> getProbeVersion(String uuid) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION
                    +":"+uuid+":::"+userId,"",EXPIRE_SECONDS);
            boolean retFlag = ProbeManagerMsgUtil.getProbeVersion(uuid,userId);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","检测探针状态中...");
            } else {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION
                        +":"+uuid+":::"+userId);
                resultMap.put("errcode",1);
                resultMap.put("msg","检测探针状态失败");
            }
        }catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_GET_PROBE_VERSION
                    +":"+uuid+":::"+userId);
            logger.error("检测探针状态异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","检测探针状态异常");
        }
        return resultMap;
    }

    /**
     * 更新探针监测状态
     * @param msgObj
     * @return
     */
    private Map<String, Object> updateProbeOpenState(JSONObject msgObj) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        String openState = msgObj.getString("openState");
        JSONArray servers = msgObj.getJSONArray("servers");
        List<String> uuidList = JSONObject.parseArray(servers.toJSONString(),String.class);
        try {
            for (String uuid : uuidList) {
                jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.PROBE_OPEN_STATE
                        +":"+uuid+":::"+userId,"",EXPIRE_SECONDS);
            }
            boolean retFlag = ProbeManagerMsgUtil.updateProbeOpenState(uuidList,openState,userId);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","start".equals(openState)?"开启探针监测中....":"关闭探针监测中....");

            }else {
                for (String uuid : uuidList) {
                    jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.PROBE_OPEN_STATE
                            +":"+uuid+":::"+userId);
                }
                resultMap.put("errcode",1);
                resultMap.put("msg","start".equals(openState)?"开启探针监测失败,请检查探针是否在线和网络状态"
                        :"关闭探针监测失败,请检查探针是否在线和网络状态");
            }
        } catch(Exception e) {
            for (String uuid : uuidList) {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.PROBE_OPEN_STATE
                        +":"+uuid+":::"+userId);
            }
            resultMap.put("errcode",1);
            resultMap.put("msg","更新探针监测状态异常");
        }
        return resultMap;
    }

    /**
     * 同步探针安装包
     * @param type
     * @return
     */
    private Map<String,Object> syncV2vProbeInstallPac(String type) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            Set<Object> userIdSet = jRedisDao.getSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "download");
            if(userIdSet != null && userIdSet.size() > 0) {
                userIdSet.add(userId);
                resultMap.put("errcode",2);
                resultMap.put("msg","版本同步中...");
            } else {
               if(ProbeManagerMsgUtil.syncV2vProbeInstallPac(type,userId)) {
                   userIdSet = new HashSet<>();
                   userIdSet.add(userId);
                   resultMap.put("errcode",2);
                   resultMap.put("msg","版本同步中...");
               } else {
                   resultMap.put("errcode",1);
                   resultMap.put("msg","版本同步失败");
                   return resultMap;
               }
            }
            jRedisDao.setSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"download",userIdSet,0);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "download:sub","",15*60);

        } catch(Exception e) {
            logger.error("版本同步异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","版本同步异常");
        }
        return resultMap;
    }

    /**
     * 升级监测探针
     * @param uuidList 升级的服务器唯一标识集合
     * @param version  要升级的版本号
     * @return
     */
    private void upGradeProbe(List<String> uuidList, String version) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("func", ProbeManagerMsgUtil.PROBE_UPGRADE);
        try {
            Map<String,Set<Object>> cacheMap = new HashMap<String,Set<Object>>();
            for (Iterator<String> iterator = uuidList.iterator(); iterator.hasNext();) {
                String uuid =  iterator.next();
                ServerBasics serverBasicsByServerUnique = serverBasicsService.getServerBasicsByServerUnique(uuid);
                if(serverBasicsByServerUnique.getServerOnLine()==2){
                    resultMap.put("errcode",1);
                    resultMap.put("server",uuid);
                    resultMap.put("msg","探针离线无法升级");
                    iterator.remove();
                    sendMessage(getWebSocketMap().get(userId),JSONObject.toJSONString(resultMap));
                    continue;
                }
                Set<Object> userIdSet = jRedisDao.getSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "upGrade:" + uuid);
                if(userIdSet != null && userIdSet.size() > 0) {
                    //正在升级中
                    userIdSet.add(userId);
                    iterator.remove();
                    resultMap.put("server",uuid);
                    resultMap.put("errcode",2);
                    resultMap.put("msg","探针正在升级中");
                    sendMessage(getWebSocketMap().get(userId),JSONObject.toJSONString(resultMap));
                }else {
                    userIdSet = new HashSet<>();
                    userIdSet.add(userId);
                }
                cacheMap.put(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"upGrade:"+uuid,userIdSet);
            }
            if(uuidList.size() > 0) {
                boolean retFlag = ProbeManagerMsgUtil.upGradeProbe(uuidList, version, userId);
                for (String uuid : uuidList) {
                    if(!cacheMap.containsKey(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+"upGrade:"+uuid)){
                        continue;
                    }
                    if(retFlag) {
                        resultMap.put("server",uuid);
                        resultMap.put("errcode",2);
                        resultMap.put("msg","探针正在升级中");
                    }else {
                        resultMap.put("server",uuid);
                        resultMap.put("errcode",1);
                        resultMap.put("msg","探针升级失败");
                    }
                    sendMessage(getWebSocketMap().get(userId),JSONObject.toJSONString(resultMap));
                }
            } 
            for (String key : cacheMap.keySet()) {
                jRedisDao.setSet(key,cacheMap.get(key),60);
                jRedisDao.set(key+":sub","",30*60);
            }
        } catch(Exception e) {
            logger.error("升级探针出现异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","升级探针出现异常");
            try {
                sendMessage(getWebSocketMap().get(userId),JSONObject.toJSONString(resultMap));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * 获取存储网关探针最新版本
     * @param type
     * @return
     */
    private Map<String,Object> getProbeRecnetVersion(String type) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            Set<String> keys = jRedisDao.keys(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC+"*");
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC+"::::"+userId,"",EXPIRE_SECONDS);
            if(keys != null && keys.size() > 0) {
                //当前已经有用户获取版本号
                resultMap.put("errcode",2);
                resultMap.put("msg","版本校验中....");
            }else if(ProbeManagerMsgUtil.getProbeRecentVersion(type,userId)) {
                resultMap.put("errcode",2);
                resultMap.put("msg","版本校验中....");
            }else {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC+"::::"+userId);
                resultMap.put("errcode",1);
                resultMap.put("msg","版本校验失败");
                return resultMap;
            }



            /*Set<Object> userIdSet = jRedisDao.getSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "version");
            if(userIdSet != null && userIdSet.size() > 0) {
                //当前已经有用户获取版本号
                userIdSet.add(userId);
                resultMap.put("errcode",2);
                resultMap.put("msg","版本校验中....");
            }else {
                if( ProbeManagerMsgUtil.getProbeRecentVersion(type,userId) ) {
                    userIdSet = new HashSet<>();
                    userIdSet.add(userId);
                    resultMap.put("errcode",2);
                    resultMap.put("msg","版本校验中....");
                }else {
                    resultMap.put("errcode",1);
                    resultMap.put("msg","版本校验失败");
                    return resultMap;
                }
            }*/
            //jRedisDao.setSet(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "version",userIdSet,0);
            //jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX + "version:sub","",5*60);
        } catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.TCP_RECENT_VERSION_FUNC+"::::"+userId);
            logger.error("版本校验异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","版本校验异常");
        }
        return resultMap;
    }

    /**
     * 删除服务器探针
     * @param serverUnique
     * @return
     */
    private Map<String,Object> delServer(String serverUnique) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
        	if(StringUtil.isNull(serverUnique)){
        		 resultMap.put("errcode",1);
                 resultMap.put("msg","serverUnique不能为空");
                 return resultMap;
        	}
            ServerBasics serverBasics = serverBasicsService.getServerBasicsByServerUnique(serverUnique);
            if(serverBasics==null){
            	 resultMap.put("errcode",1);
                 resultMap.put("msg","服务器信息不存在，删除失败");
                 return resultMap;
            }
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PROBE
                    +":"+serverUnique+":::"+userId,"",EXPIRE_SECONDS);
            ProbeManagerMsgUtil.deleteV2vProbe(serverBasics,userId);
            resultMap.put("errcode",2);
            resultMap.put("msg","删除服务器探针中...");

        } catch(Exception e) {
            logger.error("删除服务器异常",e);
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PROBE
                    +":"+serverUnique+":::"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","删除服务器探针异常");
        }
        return resultMap;
    }


    /**
     * 修改平台
     * @param platformVO
     * @return
     */
    private Map<String,Object> modifyV2vProbePlatform(PlatformVO platformVO) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            PlatformTypeVO platformType = platformTypeService.getPlatformTypeByTypeId(platformVO.getTposPlatformType());
            platformVO.setAbbreviation(platformType.getAbbreviation());
            jRedisDao.set(GlobalConstants.V2V_FUNC_REDIS_PREFIX+ ProbeManagerMsgUtil.MODIFY_PLATFORM + userId,
                    JSONObject.toJSONString(platformVO),3600);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PLATFORM
                    +"::"+platformVO.getTposRegisterid()+"::"+userId,"",EXPIRE_SECONDS);
            boolean retFlag = ProbeManagerMsgUtil.addAndModifyPlatform(ProbeManagerMsgUtil.MODIFY_PLATFORM,platformVO, userId);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","平台修改中.....");
                return resultMap;
            }
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PLATFORM
                    +"::"+platformVO.getTposRegisterid()+"::"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","平台修改失败");
        } catch(Exception e) {
            logger.error("平台修改异常",e);
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PLATFORM
                    +"::"+platformVO.getTposRegisterid()+"::"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","平台修改异常");
        }
        return resultMap;
    }

    /**
     * 添加v2v探针平台
     * @param platformVO
     */
    private Map<String,Object> addV2vProbePlatform(PlatformVO platformVO) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            platformVO.setTposState(GlobalConstants.PROCESS_STATE_DELETE);
            platformService.insertPlatform(platformVO,userId);
            resultMap.put("errcode",2);
            resultMap.put("msg","平台添加中.....");
        } catch(Exception e) {
            resultMap.put("errcode",1);
            resultMap.put("msg","平台添加出现异常");
        }
        return resultMap;
    }

    /**
     * 删除v2v探针平台
     * @param tposRegisterid
     * @return
     */
    private Map<String,Object> removeV2vProbePlatform(String tposRegisterid) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            PlatformVO platformVO = platformService.getPlatformByTposRegisterid(tposRegisterid);
            boolean retFlag = ProbeManagerMsgUtil.removePlatform(platformVO, userId);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PLATFORM
                    +"::"+tposRegisterid+"::"+userId,"",EXPIRE_SECONDS);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","平台删除中.....");
                return resultMap;
            } else {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PLATFORM
                        +"::"+tposRegisterid+"::"+userId);
                resultMap.put("errcode",1);
                resultMap.put("msg","平台删除失败");
            }
        }catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PLATFORM
                    +"::"+tposRegisterid+"::"+userId);
            logger.error("平台删除异常",e);
            resultMap.put("errcode",1);
            resultMap.put("msg","平台删除异常");
        }
        return resultMap;
    }

    /**
     * 添加平台进程
     * @param process
     * @return
     */
    private Map<String,Object> addProcess(PlatformProcess process) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            process.setState(GlobalConstants.PROCESS_STATE_DELETE);

            processService.insertPlatformProcess(process,userId);
            resultMap.put("errcode",2);
            resultMap.put("msg","进程添加中.....");
        } catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.ADD_PROC
                    +":::"+process.getId()+":"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","进程添加出现异常");
        }
        return resultMap;
    }

    /**
     * 修改平台进程
     * @param process
     * @return
     */
 /*   private Map<String,Object> modifyProcess(PlatformProcess process) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            String[] registerIds = process.getRegisterIds().split(",");
            ServerBasics serverBasics = serverBasicsService.getServerBasicsByTposRegisterid(registerIds[0]);
            jRedisDao.set(GlobalConstants.V2V_FUNC_REDIS_PREFIX + ProbeManagerMsgUtil.MODIFY_PROC + userId,
                    JSONObject.toJSONString(process),3600);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROC
                    +":::"+process.getId()+":"+userId,"",EXPIRE_SECONDS);
            boolean retFlag = ProbeManagerMsgUtil.sendProcMsg(serverBasics,process,ProbeManagerMsgUtil.MODIFY_PROC,userId);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","修改进程中.....");
            }else {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROC
                        +":::"+process.getId()+":"+userId);
                resultMap.put("errcode",1);
                resultMap.put("msg","修改进程失败");
            }
        } catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.MODIFY_PROC
                    +":::"+process.getId()+":"+userId);
            resultMap.put("errcode",1);
            
            resultMap.put("msg","修改进程异常");
        }
        return resultMap;
    }*/

    /**
     * 删除平台进程
     * @param procId
     * @return
     */
    private Map<String,Object> delProcess(JSONObject msgObj) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
        	msgObj.put("userId", userId);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PROC
                    +":::"+":"+userId,"",EXPIRE_SECONDS);
            ProbeManagerMsgUtil.delProc(msgObj);
        } catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+ProbeManagerMsgUtil.DEL_PROC
                    +":::"+":"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","平台进程删除异常");
        }
        return resultMap;
    }

    /**
     * 平台重启进程 （感知中心调用）
     * @param tposRegisterid  
     * @return
     */
    private Map<String,Object> platformHandle(JSONObject msgObj) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        String  tposRegisterid =msgObj.getString("tposRegisterid");
        if(StringUtil.isNull(tposRegisterid)){
        	 resultMap.put("errcode",1);
             resultMap.put("msg","平台ID不能为空");
             return resultMap;
        }
        PlatformVO platformVO = platformService.getPlatformByTposRegisterid(tposRegisterid);
		if(platformVO==null){
			 logger.warn("感知中心平台重启进程平台不存在： "+ tposRegisterid);
			 resultMap.put("errcode",1);
             resultMap.put("msg","平台不存在");
             return resultMap;
		}
        String resubmitRedisKey=GlobalConstants.RESUBMIT+GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PLATFORM_HANDLE
                +"::"+platformVO.getServerUnique()+"::"+userId;
        String overTimeRedisKey =GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PLATFORM_HANDLE
                +"::"+tposRegisterid+"::"+userId;
        try {
        	String value = jRedisDao.get(resubmitRedisKey);
        	if(StringUtil.isNotNull(value)){
        		 resultMap.put("errcode",0);
                 resultMap.put("msg","有其他重启操作正在进行中，请稍后再试");
                 return resultMap;
        	}
            jRedisDao.set(overTimeRedisKey,"",EXPIRE_SECONDS);
            jRedisDao.set(resubmitRedisKey,"123456",EXPIRE_SECONDS*2);
            boolean retFlag =ProbeManagerMsgUtil.platRestart(msgObj);
            if(retFlag) {
            	jRedisDao.del(overTimeRedisKey);
                resultMap.put("errcode",0);
                resultMap.put("msg","操作平台成功");
            } else {
            	jRedisDao.del(overTimeRedisKey);
                resultMap.put("errcode",1);
                resultMap.put("msg","操作平台失败");
            }
        } catch(Exception e) {
        	jRedisDao.del(overTimeRedisKey);
            resultMap.put("errcode",1);
            resultMap.put("msg","操作平台异常");
        }
        return resultMap;
    }

    /**
     * 进程操作
     * @param procId
     * @param method
     * @return
     */
   /* private Map<String,Object> processHandle(Integer procId, String method) {
        Map<String,Object> resultMap = new HashMap<String,Object>();
        try {
            //PlatformProcess process = processService.getProcessByid(procId);
            PlatformVO platformVO =  processService.getPlatformVOByProcessId(procId).get(0);
            jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                    +":::"+procId+":"+userId,"",EXPIRE_SECONDS);
            boolean retFlag = false;//ProbeManagerMsgUtil.handProcess(platformVO,process,method,userId);
            if(retFlag) {
                resultMap.put("errcode",2);
                resultMap.put("msg","操作进程进行中....");
            }else {
                jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                        +":::"+procId+":"+userId);
                resultMap.put("errcode",1);
                resultMap.put("msg","操作进程失败");
            }
        } catch(Exception e) {
            jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                    +":::"+procId+":"+userId);
            resultMap.put("errcode",1);
            resultMap.put("msg","操作进程异常");
        }
        return resultMap;
    }*/
    
    /**
     * 进程状态监测开关
     * @param procId
     * @param method
     * @return
     */
    private Map<String,Object> processStatusCheckInfo(JSONObject msgObj) {
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	JSONObject json=  JSONObject.parseObject(msgObj.getString("param"));
    	try {
    		json.put("userId", userId);
    		jRedisDao.set(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                      +":::"+json.getString("processId")+":"+userId,"",EXPIRE_SECONDS);
            boolean retFlag = ProbeManagerMsgUtil.processStatusCheckInfo(json);
            if(retFlag) {
                resultMap.put("errcode",0);
                resultMap.put("msg","操作成功");
                return resultMap;
            }
    		
    	} catch(Exception e) {
    	  logger.error("WebSocketPushMessage--processStatusCheckInfo",e);	
    	}
    	 
    	jRedisDao.del(GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
                 +":::"+json.getString("processId")+":"+userId);
	    resultMap.put("errcode",1);
	    resultMap.put("msg","操作失败");
    	return resultMap;
    }
    /**
     * 进程重启
     * @param procId
     * @param method
     * @return
     */
    private Map<String,Object> processRestart(JSONObject msgObj) {
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	String resubmitRedisKey=GlobalConstants.RESUBMIT+GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
    			+":::"+msgObj.getString("serverUnique").replace(":", "")+":"+userId; // 以整个探针服务器每分钟操作一次
    	String overTimeRedisKey =GlobalConstants.IP_V2V_PROBE_REDIS_PREFIX+GlobalConstants.PROCESS_HANDLE
    			+":::"+msgObj.getString("processName").replace(":", "")+":"+userId;
    	try {
    		msgObj.put("userId", userId);
    		String value =jRedisDao.get(resubmitRedisKey);
    		if(StringUtil.isNotNull(value)){
       		    resultMap.put("errcode",0);
                resultMap.put("msg","有其他重启操作正在进行中，请稍后再试");
                return resultMap;
       	    }
    		jRedisDao.set(overTimeRedisKey,"",EXPIRE_SECONDS);
    		jRedisDao.set(resubmitRedisKey,"123456",EXPIRE_SECONDS);
    		boolean retFlag = ProbeManagerMsgUtil.processRestart(msgObj);
    		   if(retFlag) {
    			   jRedisDao.del(overTimeRedisKey);
                   resultMap.put("errcode",0);
                   resultMap.put("msg","进程重启操作成功，请稍后查看进程状态");
               }else {
                  jRedisDao.del(overTimeRedisKey);
                   resultMap.put("errcode",1);
                   resultMap.put("msg","进程重启操作失败，请检查探针及网络状态");
               }
           } catch(Exception e) {
              jRedisDao.del(overTimeRedisKey);
               resultMap.put("errcode",1);
               resultMap.put("msg","进程重启操作异常");
           }
           return resultMap;
    }


    /**
     * 判断V2v探针相关字段是否修改
     * @param serverBasics
     * @param oldServerBasics
     * @return
     */
    private boolean isModifyV2VProperties(ServerBasics serverBasics, ServerBasics oldServerBasics) {
        Integer reuseXzNo = serverBasics.getReuseXzNo();
        String terminalCode = serverBasics.getTerminalCode();
        String netMac = serverBasics.getNetMac();
        String v2vNetMac = serverBasics.getV2vNetMac();
        if(!reuseXzNo.equals(oldServerBasics.getReuseXzNo())) {
            return true;
        } else if(StringUtil.isNotNull(terminalCode) && !terminalCode.equals(oldServerBasics.getTerminalCode())) {
            return true;
        } else {
            if(StringUtil.isNotNull(netMac) && !netMac.equals(oldServerBasics.getNetMac())) {
                return true;
            }
            if(StringUtil.isNotNull(v2vNetMac) && !v2vNetMac.equals(oldServerBasics.getV2vNetMac())) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
		/*String message ="{\"funcName\":\"del_proc\",\"serverUnique\":\"servers_4c301cb3-daa1-4bd6-b01d-b152be8a7b19\",\"procList\":[{\"processId\":\"9527\",{\"processId\":\"3306\"], \"userId\":\"2313\"}";
		JSON json = (JSON) JSON.parse(message); */
		/*JSONObject msgObj = JSONObject.parseObject(message);
		 String[] list = (String[]) msgObj.get("procList");*/
		 
    	String message ="IP_V2V_PROBE:del_proc:::::7c4b03e31b824e47b6afc85c426f759f";
    	String[] redisStrArr = message.split(":");
    	System.out.println(redisStrArr[0]);
    	System.out.println(redisStrArr[1]);
    	System.out.println(redisStrArr[2]);
    	System.out.println(redisStrArr[3]);
    	System.out.println(redisStrArr[4]);
    	System.out.println(redisStrArr[5]);
    	System.out.println(redisStrArr[6]);
    	
	}
    
}
