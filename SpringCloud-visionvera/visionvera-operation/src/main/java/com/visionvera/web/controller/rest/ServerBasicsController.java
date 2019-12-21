package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.service.ServerBasicsService;
import com.visionvera.service.SyncProbeManageService;
import com.visionvera.util.ExcelUtil;
import com.visionvera.util.ProbeManagerMsgUtil;

/**
 * 服务器基本信息Controller
 * @author dql
 *
 */
@RestController
public class ServerBasicsController extends BaseReturn{
	
	private Logger logger = LoggerFactory.getLogger(ServerBasicsController.class);
	
	@Autowired
	private ServerBasicsService serverBasicsService;
	
	@Autowired
	private SyncProbeManageService syncProbeManageService;
	
	/**
	 * 添加服务器信息
	 * @return
	 */
	@RequestMapping(value="/server/insertServer",method=RequestMethod.POST)
	public ReturnData insertServer(@RequestBody ServerBasics serverBasics) {
		try {
			if(serverBasics == null) {
				return super.returnError("服务器信息不能为空");
			}
			ReturnData returnData = serverBasicsService.insertServerBasics(serverBasics);
			return returnData;
		} catch(Exception e) {
			logger.error("添加服务器异常", e);
			return super.returnError("添加服务器异常");
		}
	}
	
	/**
	 * 编辑服务器信息
	 * @param serverBasics
	 * @return
	 */
	@RequestMapping(value="/server/updateServer",method=RequestMethod.POST)
	public ReturnData updateServer(@RequestBody ServerBasics serverBasics) {
		try {
			if(serverBasics == null) {
				return super.returnError("服务器信息不能为空");
			}
			return serverBasicsService.updateServerBasics(serverBasics);
		} catch(Exception e) {
			logger.error("修改服务器异常", e);
			return super.returnError("修改服务器异常");
		}
		
	}
		
	/**
	 * 根据服务器id查询服务器信息
	 */
	@RequestMapping(value="/server/{id}/getServer")
	public ReturnData getServerById(@PathVariable Integer id){
		try {
			ServerBasics serverBasics = serverBasicsService.getServerBasicsById(id);
			if(serverBasics == null) {
				return super.returnError("没有查询到服务器信息");
			}
			return super.returnResult(0, "查询服务器信息成功", null, null, serverBasics);
		} catch(Exception e) {
			logger.error("查询服务器异常", e);
			return super.returnError("查询服务器异常");
		}
	}
	
	
	/**
	 * 获取探针管理IP地址
	 * @return
	 */
	@RequestMapping(value="/server/getProbeManageIp",method=RequestMethod.GET)
	public ReturnData getProbeManageIp() {
		try {
			Map<String,Object> ipMap = new HashMap<String, Object>();
			ipMap.put("probeManageIp", ProbeManagerMsgUtil.probeManageIp);
			return super.returnResult(0, "获取探针管理IP地址成功", null,null,ipMap);
		} catch(Exception e) {
			logger.error("获取探针管理IP地址异常", e);
			return super.returnError("获取探针管理IP地址异常");
		}
	}
	
	/**
	 * 根据条件分页查询服务器信息
	 * @param serverName   服务器名称
	 * @param ip	ip地址
	 * @param serverHostname 服务器主机名	
	 * @param state	监测探针状态
	 * @param serverDistrict 三级行政区域编码，编码之间用逗号分隔
	 * @param serverSite	详细地址
	 * @param openState	开启状态
	 * @param version	版本号
	 * @param serverOs  操作系统 Linux|Windows
	 * @param transferType 传输协议 IP|V2V
	 * @param pageNum	分页参数，第几页，默认为1
	 * @param pageSize	分页参数，每页包含多少条记录，默认为10
	 * @return
	 */
	@RequestMapping(value="/server/getServerList",method=RequestMethod.GET)
	public ReturnData getServerList(@RequestParam(name="serverName",required=false) String serverName,
			@RequestParam(name="ip",required=false) String ip,
			@RequestParam(name="serverHostname",required=false)String serverHostname,
			@RequestParam(name="state",required=false) Integer state,
			@RequestParam(name="serverDistrict",required=false)String serverDistrict,
			@RequestParam(name="serverSite",required=false) String serverSite,
			@RequestParam(name="openState",required=false) Integer openState,
			@RequestParam(name="version",required=false) String version,
			@RequestParam(name="serverOs",required=false) String serverOs,
			@RequestParam(name="transferType",required=false) String transferType,
			@RequestParam(name="pageNum",defaultValue="1") Integer pageNum,
			@RequestParam(name="pageSize",defaultValue="10") Integer pageSize) {
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("serverName", serverName);
			paramMap.put("ip", ip);
			paramMap.put("serverHostname", serverHostname);
			paramMap.put("state", state);
			paramMap.put("serverDistrict", serverDistrict);
			paramMap.put("serverSite", serverSite);
			paramMap.put("openState", openState);
			paramMap.put("version", version);
			paramMap.put("serverOs", serverOs);
			paramMap.put("transferType", transferType);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", pageSize);
			
			List<ServerBasics> serverList = serverBasicsService.getServerBasicByPage(paramMap);
			Page<ServerBasics> page = (Page<ServerBasics>)serverList;
			Map<String,Object> pageMap = new HashMap<String, Object>();
			pageMap.put("pageNum", page.getPageNum());
			pageMap.put("pageSize", page.getPageSize());
			pageMap.put("total", page.getTotal());
			pageMap.put("pages", page.getPages());
			
			return super.returnResult(0, "查询服务器列表成功", null, serverList, pageMap);
		} catch(Exception e) {
			logger.error("查询服务器列表异常", e);
			return super.returnError("查询服务器列表异常");
		}
	}
	
	/**
	 * 获取服务器报警阈值
	 * @return
	 */
	@RequestMapping(value="/server/{serverUnique}/getThreshold",method=RequestMethod.GET)
	public ReturnData getServerThreshold(@PathVariable String serverUnique) {
		try {
			ServerBasics serverBasics = serverBasicsService.getServerThreshold(serverUnique);
			return super.returnResult(0, "查询服务器报警阈值成功", null, null, serverBasics);
		} catch(Exception e) {
			logger.error("查询服务器报警阈值失败", e);
			return super.returnError("查询服务器报警阈值失败");
		}
	}
	
	
	/**
	 * 更新服务器报警阈值
	 * @return
	 */
	@RequestMapping(value="/server/updateThreshold",method=RequestMethod.POST)
	public ReturnData updateServerThreshold(@RequestBody ServerBasics serverBasics) {
		if(serverBasics == null || serverBasics.getServerUnique() == null) {
			return super.returnError("服务器信息不能为空或者服务器唯一标识不能为空");
		}
		try {
			serverBasicsService.updateServerThreshold(serverBasics);
			return super.returnSuccess("修改服务器报警阈值成功");
		}catch(Exception e) {
			logger.error("修改服务器报警阈值失败", e);
			return super.returnError("修改服务器报警阈值失败");
		}
	}
	
	/**
	 * 批量添加服务器信息
	 * @return
	 */
	@RequestMapping(value="/server/batchAdd",method=RequestMethod.POST)
	public ReturnData addServerBasicsBatch(@RequestParam("file") MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			Workbook workbook = ExcelUtil.getWorkbook(file.getInputStream(), fileName);
			ReturnData returnData= serverBasicsService.addServerBasicsOfExcel(workbook);
			if(returnData.getErrcode()==0){
				syncProbeManageService.addProbeBatch();
			}
			return returnData;
		}catch(Exception e) {
			logger.error("批量添加服务器失败",e);
			return super.returnError("批量添加服务器失败");
		}
	}
	
	/**
	 * 批量导出服务器信息
	 * @param transferType	传输协议 IP或者V2V
	 * @param response
	 */
	@RequestMapping(value="/server/exportExcel",method=RequestMethod.GET)
	public void exportServerBasics(@RequestParam(name="serverName",required=false) String serverName,
			@RequestParam(name="ip",required=false) String ip,
			@RequestParam(name="serverHostname",required=false)String serverHostname,
			@RequestParam(name="state",required=false) Integer state,
			@RequestParam(name="serverDistrict",required=false)String serverDistrict,
			@RequestParam(name="serverSite",required=false) String serverSite,
			@RequestParam(name="openState",required=false) Integer openState,
			@RequestParam(name="version",required=false) String version,
			@RequestParam(name="serverOs",required=false) String serverOs,
			@RequestParam(name="transferType",required=false) String transferType,HttpServletResponse response) {
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("serverName", serverName);
			paramMap.put("ip", ip);
			paramMap.put("serverHostname", serverHostname);
			paramMap.put("state", state);
			paramMap.put("serverDistrict", serverDistrict);
			paramMap.put("serverSite", serverSite);
			paramMap.put("openState", openState);
			paramMap.put("version", version);
			paramMap.put("serverOs", serverOs);
			paramMap.put("transferType", transferType);
			
			serverBasicsService.exportServerBasics(paramMap,response);
		} catch(Exception e) {
			logger.error("批量导出服务器信息失败",e);
		}
	}
	
	/**
	 * 根据服务器id列表查询服务器信息
	 * @param serverIds
	 * @return
	 */
	@RequestMapping(value="/server/getServersByIds",method=RequestMethod.POST)
	public ReturnData getServerBasicsByIds(@RequestBody List<Integer> serverIds) {
		try {
			List<ServerBasics> serverBasicsList = serverBasicsService.getServerBasicsListByIds(serverIds);
			return super.returnResult(0, "查询服务器列表成功", null, serverBasicsList);
		} catch(Exception e) {
			logger.error("查询服务器列表信息失败",e);
			return super.returnError("查询服务器列表信息失败");
		}
	}
}
