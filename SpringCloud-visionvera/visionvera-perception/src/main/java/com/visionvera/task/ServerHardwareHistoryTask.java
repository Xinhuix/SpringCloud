package com.visionvera.task;

import com.visionvera.service.ServerHardwareHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class ServerHardwareHistoryTask {
    private Logger logger = LoggerFactory.getLogger(ServerHardwareHistoryTask.class);
    @Autowired
    private ServerHardwareHistoryService serverHardwareHistoryService;
    @Scheduled(cron = "0 5 0 1/1 * ?")
//    @Scheduled(cron = "0 0/2 * * * ?")
    public void generateHistory(){
        try {
            logger.info("服务器硬件历史信息开始生成");
            serverHardwareHistoryService.generateHistory();
            logger.info("服务器硬件历史信息生成成功");
        }catch (Exception e){
            logger.error("服务器硬件历史信息生成失败",e);
        }
    }
    @Scheduled(cron = "0 15 0 1/1 * ?")
    public void deleteHistory(){
        try {
            logger.info("服务器硬件历史信息开始清除历史");
            serverHardwareHistoryService.deleteHistory();
            logger.info("服务器硬件历史信息清除历史成功");
        }catch (Exception e){
            logger.error("服务器硬件历史信息清除历史失败",e);
        }
    }
}
