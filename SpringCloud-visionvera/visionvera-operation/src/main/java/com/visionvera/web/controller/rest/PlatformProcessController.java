package com.visionvera.web.controller.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.util.StringUtil;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformProcess;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.service.PlatformProcessService;
import com.visionvera.service.ServerBasicsService;

/**
 * 平台进程controller
 * @author dql
 *
 */
@RestController
public class PlatformProcessController extends BaseReturn {
	
	private Logger logger = LoggerFactory.getLogger(PlatformProcessController.class); 
	
	@Autowired
	private PlatformProcessService processService;
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	
	@Value("${probe_manage.ip}")
	private String probeManageIp;
	
	/**
	 * 查询进程列表
	 * @return
	 */
	@RequestMapping(value="/process/{serverUnique}/getAllProcess",method=RequestMethod.GET)
	public ReturnData getAllProcess(@PathVariable String serverUnique) {
		try {
			List<PlatformProcess> processList = processService.getAllProcesses(serverUnique);
			Map<String,Object> map = processService.getTopsNumByServerUnique(serverUnique);
			return super.returnResult(0, "查询进程信息成功", null, processList,map);
		} catch(Exception e) {
			logger.error("查询进程信息异常", e);
			return super.returnError("查询进程信息异常");
		}
	}
	
	/**
	 * 添加进程
	 * @param platformProcess
	 * @return
	
	@RequestMapping(value="/process/addProcess",method=RequestMethod.POST)
	public ReturnData addProcess(@RequestBody PlatformProcess platformProcess) {
		try {
			platformProcess.setCreateTime(DateUtil.date2String(new Date()));
			processService.insertPlatformProcess(platformProcess, null);
			return super.returnSuccess("添加平台进程成功");
		} catch(Exception e) {
			logger.error("添加进程异常", e);
			return super.returnError("添加平台进程异常");
		}
	} */
	
	/**
	 * 更新平台和进程
	 * @param platformProcess
	 * @return
	 */
	@RequestMapping(value="/process/updateProcess",method=RequestMethod.POST)
	public ReturnData updatePlatformProcess(@RequestBody PlatformVO platformVO) {
		try {
			if(StringUtil.isEmpty(platformVO.getProcessIp())){
				return super.returnError("进程IP不能为空");
			}
			ReturnData	returnData = processService.updatePlatformAndProcess(platformVO);
			return returnData;
		} catch(Exception e) {
			logger.error("更新平台进程失败", e);
			return super.returnError("更新平台进程失败");
		}
	}
	
	/**
	 * 删除进程
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/process/{id}/deleteProcess",method=RequestMethod.GET)
	public ReturnData deleteProcess(@PathVariable Integer id) {
		try {
			processService.deleteProcessById(id);
			return super.returnSuccess("删除平台进程成功");
		} catch(Exception e) {
			logger.error("删除平台进程异常", e);
			return super.returnError("删除平台进程异常");
		}
	}
	
	/**
	 * 获取进程及平台信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/process/{id}/getProcessInfo",method=RequestMethod.GET)
	public ReturnData getProcessInfo(@PathVariable Integer id) {
		try {
			ReturnData returnData = processService.getProcessByid(id);
			return returnData;
		} catch(Exception e) {
			logger.error("获取平台进程异常", e);
			return super.returnError("获取平台进程异常");
		}
	}
	
	/**
	 * 启动或者停止进程
	 * @param id
	 * @param method
	 * @return
	 */
/*	@RequestMapping(value="/process/{id}/{method}/handleProcess")
	public ReturnData handleProcess(@PathVariable Integer id,@PathVariable String method) {
		try {
			PlatformProcess process = processService.getProcessByid(id);
			ServerBasics serverBasics = serverBasicsService.getServerBasicsByProcessId(id);
			
			if(TransferType.V2V.getTransferType().equals(serverBasics.getTransferType())) {
				boolean resultFlag = transferData2ProbeManager(serverBasics,process,method);
				if(resultFlag) {
					return super.returnSuccess("操作进程成功");
				}
			}else {
				boolean retFlag = IPProbeReqUtil.handleProcess(serverBasics, method, process);
				if(retFlag) {
					return super.returnSuccess("操作平台进程成功");
				}
			}
			return super.returnError("操作进程失败");
		} catch(Exception e) {
			logger.error("操作进程失败", e);
			return super.returnError("操作进程失败");
		}
	}*/
	
	/**
	 * 向探针管理传输数据
	 * @param serverBasics
	 * @param process
	 * @param method
	 * @return
	 */
	private boolean transferData2ProbeManager(ServerBasics serverBasics,PlatformProcess process,String method) {
	/*	String startCommand = StringUtil.isNotNull(process.getStartScript())? process.getStartScript(): process.getStartScriptPath();
		String stopCommand = StringUtil.isNotNull(process.getShutdownScript())? process.getShutdownScript() : process.getShutdownScriptPath();
		
		JSONArray cmdArr = new JSONArray();
		switch(method) {
		case "start": cmdArr.add(startCommand); break;
		case "stop": cmdArr.add(stopCommand); break;
		case "restart": cmdArr.add(stopCommand); cmdArr.add(startCommand); break;
		}
		
		JSONObject sendData = new JSONObject();
		sendData.put("funcName", "cmd");
		sendData.put("uuid", serverBasics.getServerUnique());
		JSONObject paramData = new JSONObject();
		paramData.put("cmd", cmdArr);
		sendData.put("param", paramData);
		String resultStr = NettyTcpClient.sendMsg(probeManageIp, sendData.toJSONString());
		if(!"error".equals(resultStr)) {
			JSONObject resultObj =  JSONObject.parseObject(resultStr);
			if(ProbeManagerMsgUtil.TCP_SUCESS_RET.equals(resultObj.getString("ret"))) {
				return true;
			}
		}*/
		return false;
	}
}
