package com.visionvera.controller;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @date 2018年11月26日 14:33
 */
@RequestMapping("control")
@RestController
public class ControlController extends BaseReturn {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlController.class);


    @Autowired
    private ControlService controlService;


    /**
     * 业务控制接口 不包含可视电话
     * @param platformId
     * @param uniqId
     * @param msg
     * @return
     */
    @RequestMapping("control")
    public ReturnData control(@RequestParam(value ="platformId" )String platformId,
    		@RequestParam(value ="uniqId" )String uniqId,
    		@RequestParam(value ="msg" )String msg,
    		@RequestParam(value ="loginName" )String loginName,
    		@RequestParam(value ="loginPwd" )String loginPwd){
        try {
//            return controlService.control(platformId, uniqId, msg, loginName, loginPwd);
            return null;
        } catch (Exception e) {
        	LOGGER.error("ControlController--control--", e);
    		return super.returnError("停止失败");
        }
    }

    /**
     * 控制可视电话
     * @param platformId
     * @param uniqId
     * @param msg
     * @return
     */
    @RequestMapping("controlPhone")
    public ReturnData controlPhone(@RequestParam(value ="platformId" )String platformId,
    		@RequestParam(value ="uniqId" )String uniqId,
    		@RequestParam(value ="msg" )String msg,
    		@RequestParam(value ="loginName" )String loginName,
    		@RequestParam(value ="loginPwd" )String loginPwd){
        try {
            return controlService.controlPhone(platformId, uniqId, msg, loginName, loginPwd);
        } catch (Exception e) {
        	LOGGER.error("ControlController--controlPhone--", e);
    		return super.returnError("停止失败");
        }
    }
    /**
     * 终端、流媒体停止会议
     * @param platformId
     * @param uuid
     * @param msg
     * @return
     */
    @RequestMapping(value="stopMeeting",method =RequestMethod.POST)
    public ReturnData stopMeeting(@RequestParam(value ="platformId")String platformId,
    		@RequestParam(value ="uuid")String uuid,
    		@RequestParam(value ="loginName")String loginName,
    		@RequestParam(value ="loginPwd")String loginPwd,
    		@RequestParam(value ="msg",required=false)String msg){
    	try {
    		return controlService.stopMeeting(platformId, uuid, loginName, loginPwd, msg);
    	} catch (Exception e) {
    		LOGGER.error("ControlController--stopMeeting--", e);
    		return super.returnError("停会失败");
    	}
    }
}
