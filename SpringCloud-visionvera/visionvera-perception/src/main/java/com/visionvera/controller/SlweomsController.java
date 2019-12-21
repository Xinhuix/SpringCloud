package com.visionvera.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.visionvera.bean.slweoms.PlatformTypeVO;
import com.visionvera.bean.slweoms.ServerBasics;
import com.visionvera.bean.slweoms.ServerHardwareVO;
import com.visionvera.service.ServerHardwareHistoryService;
import com.visionvera.service.SlweomsService;
import com.visionvera.util.StringUtil;
import com.visionvera.vo.Slweoms;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName SlweomsController
 * @description 运维平台转发Controller
 * @author dql
 * @date 2018/06/01
 */
@RestController
@RequestMapping("/rest/slweoms")
public class SlweomsController {
	private static final Logger logger = LogManager.getLogger(SlweomsController.class);
	@Autowired
    private SlweomsService slweomsService;
	@Autowired
    private ServerHardwareHistoryService serverHardwareHistoryService;
	@InitBinder
	public void initBind(WebDataBinder binder){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	@RequestMapping(value = "/history",method=RequestMethod.GET)
	public Map<String, Object> server(Date startTime, Date endTime, String field, String serverUnique,Integer num) {
		Map<String, Object> resultMap = new LinkedHashMap<>();
		try{
			Map<String, Object> result = serverHardwareHistoryService.selectServerHardwareHistory(startTime, endTime, field, serverUnique, num);
			resultMap.put("data", result);
			resultMap.put("errcode", result==null?1:0);
			resultMap.put("errmsg", result==null?"查询失败":"查询成功");
		}catch (Exception e){
			logger.error("服务器硬件信息查询失败",e);
			resultMap.put("errcode",1);
			resultMap.put("errmsg", "查询失败");
		}


		return resultMap;
	}
	/**
	 * 应用服务器接口
	 * @param request
	 * @param response
	 * @param pf_type 接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value = "/server",method=RequestMethod.GET)
	public Map<String,Object>  server(@RequestParam(name="pageSize",defaultValue = "10") Integer pageSize,
									   @RequestParam(name="pageNum",defaultValue = "1") Integer pageNum,
									   @ModelAttribute Slweoms slweoms) {
		Map<String, Object> resultMap = new LinkedHashMap<>();
		try{
			JSONObject server = slweomsService.server(pageSize,pageNum,slweoms);
			resultMap.put("data", server);
			resultMap.put("errcode", 0);
			resultMap.put("errmsg", "查询平台列表成功");
		}catch (Exception e){
			logger.error("查询应用服务器失败",e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询应用服务器失败");
		}
		return resultMap;
	}

	/**
	 * 获取平台类型列表
	 * @param request
	 * @param response
	 * @param pf_type 接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value = "/getPlatFormType",method=RequestMethod.GET)
	public Map<String,Object> getPlatFormTypeList(HttpServletRequest request,
			HttpServletResponse response,@RequestParam(name="pf_type",required = true) String pf_type) {
		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			List<PlatformTypeVO> platformTypeList = slweomsService.getPlatFormTypeList();
			if(platformTypeList != null && platformTypeList.size() > 0) {
				Map<String,Object> dataMap = new HashMap<String, Object>();
				dataMap.put("items", platformTypeList);
				resultMap.put("data", dataMap);
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询平台列表成功");
			}else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "没有查询到平台列表");
			}
		} catch(Exception e) {
			logger.error("查询平台列表异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询平台列表异常");
		}
		return resultMap;
	}


	/**
	 * 根据平台类型和行政区域编码获取下级区域列表和服务器列表
	 * @param request
	 * @param response
	 * @param gradeid 行政级别 0：全国，1：省级，2：市级
	 * @Param district	行政区域编码
	 * @param platFormType  平台类型
	 * @param pf_type 接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value="/{gradeid}/{district}/getRegionAndServer")
	public Map<String,Object> getRegionAndServerList(HttpServletRequest request,HttpServletResponse response,
			@PathVariable Integer gradeid,@PathVariable String district,
			@RequestParam(name="platFormType",required = false) Integer platFormType,
			@RequestParam(name="pf_type",required = true) String pf_type){
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("gradeid",gradeid);
			paramMap.put("district", district);
			paramMap.put("platFormType", platFormType);

			Map<String,Object> regionAndServerMap = slweomsService.getRegionAndServerList(paramMap);
			if(regionAndServerMap != null && !regionAndServerMap.isEmpty()) {
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询服务器和行政区域信息成功");
				Map<String,Object> dataMap = new HashMap<String, Object>();
				dataMap.put("items", regionAndServerMap);
				resultMap.put("data", dataMap);
			}
		} catch(Exception e) {
			logger.info("查询服务器和行政区域信息异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询服务器和行政区域信息异常");
		}
		return resultMap;
	}

	/**
	 * 根据行政区域编码查询服务器信息
	 * @param request
	 * @param response
	 * @param gradeid  行政级别 1：省级；2：市级
	 * @param platFormType 平台类型编码
	 * @param serverName 服务器名称，模糊查询
	 * @param platFormName  平台名称，模糊查询
	 * @param serverNetIp 服务器ip,模糊查询
	 * @param pf_type  接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value="/{gradeid}/{district}/regionServerList")
	public Map<String,Object> regionServerList(HttpServletRequest request,HttpServletResponse response,
			@PathVariable Integer gradeid,@PathVariable String district,
			@RequestParam(name="platFormType",required = false) Integer platFormType,
			@RequestParam(name="serverName",required = false) String serverName,
			@RequestParam(name="platFormName",required = false) String platFormName,
			@RequestParam(name="serverNetIp",required = false) String serverNetIp,
			@RequestParam(name="serverOnLine",required = false) Integer serverOnLine,
			@RequestParam(name="pf_type",required = true) String pf_type,
			@RequestParam(name="pageSize",defaultValue = "10") Integer pageSize,
			@RequestParam(name="pageNum",defaultValue = "1") Integer pageNum,
			@RequestParam(name="sortField",required = false) String sortField,
			@RequestParam(name="sortMode",required = false) String sortMode) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			if(StringUtil.isNotNull(sortField) != StringUtil.isNotNull(sortMode)) {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "排序参数缺失");
				return resultMap;
			}

			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("gradeid", gradeid);
			paramMap.put("district", district);
			paramMap.put("platFormType", platFormType);
			paramMap.put("serverName", serverName);
			paramMap.put("platFormName", platFormName);
			paramMap.put("serverNetIp", serverNetIp);
			paramMap.put("serverOnLine", serverOnLine);
			paramMap.put("pageSize",pageSize);
			paramMap.put("pageNum", pageNum);
			paramMap.put("sortField", sortField);
			paramMap.put("sortMode", sortMode);

			List<ServerHardwareVO> ServerHardwareVOList = slweomsService.getRegionServerList(paramMap);

			if(ServerHardwareVOList != null && ServerHardwareVOList.size() > 0) {
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询应用服务器信息成功");
				Map<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("items", ServerHardwareVOList);
				//分页的属性
				Page<ServerHardwareVO> page = (Page<ServerHardwareVO>)ServerHardwareVOList;
				Map<String,Object> pageMap = new HashMap<String,Object>();
				pageMap.put("pageNum", page.getPageNum());
				pageMap.put("pageSize", page.getPageSize());
				pageMap.put("total", page.getTotal());
				pageMap.put("pages", page.getPages());
				dataMap.put("extra", pageMap);

				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "未查询到应用服务器数据");
			}
		} catch (Exception e) {
			logger.error("查询应用服务器信息异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询应用服务器信息异常");
		}
		return resultMap;
	}

	/**
	 * 根据服务器唯一key查询硬件信息
	 * @param request
	 * @param response
	 * @param serverKey 服务器唯一key
	 * @param pf_type 接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value="/{serverKey}/serverHardwareInfo", method = RequestMethod.GET)
	public Map<String,Object> serverHardwareInfoUptoDate(HttpServletRequest request,HttpServletResponse response,
			@PathVariable String serverKey,@RequestParam(name="pf_type",required = true) String pf_type) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("serverKey", serverKey);
			List<ServerHardwareVO> hardWareVOList = slweomsService.getServerHardwareInfo(paramMap);

			if(hardWareVOList != null && hardWareVOList.size() > 0) {
				resultMap.put("errcode", 0);
				resultMap.put("errmsg", "查询应用服务器信息成功");
				Map<String,Object> dataMap =  new HashMap<String, Object>();
				dataMap.put("items", hardWareVOList);
				resultMap.put("data", dataMap);
			} else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "未查询到应用服务器数据");
			}
		} catch(Exception e) {
			logger.error("查询应用服务器信息异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询应用服务器信息异常");
		}
		return resultMap;
	}

	/**
	 * 根据服务器id查询服务器信息
	 * @param request
	 * @param response
	 * @param id  服务器id
	 * @param pf_type  接口类型（平台来源）。huiguan:会管；tgl_ws唐古拉（websocket）；tgl_rest：唐古拉（webservice）；wg：网管；yw：运维
	 * @return
	 */
	@RequestMapping(value="/{id}/serverBasicsInfo",method=RequestMethod.GET)
	public Map<String,Object> serverBasicsInfoById(HttpServletRequest request,HttpServletResponse response,
			@PathVariable Integer id,@RequestParam(name="pf_type",required = true) String pf_type) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			ServerBasics serverBasics = slweomsService.getServerBasicsById(id);
			List<ServerBasics> serverBasicsList = new ArrayList<ServerBasics>();
			if(serverBasics != null) {
				serverBasicsList.add(serverBasics);

				resultMap.put("errcode",0);
				resultMap.put("errmsg", "查询应用服务器信息成功");
				Map<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("items", serverBasicsList);
				resultMap.put("data", dataMap);
			}else {
				resultMap.put("errcode", 1);
				resultMap.put("errmsg", "未查询到应用服务器数据");
			}

		} catch(Exception e) {
			logger.error("查询应用服务器信息异常", e);
			resultMap.put("errcode", 1);
			resultMap.put("errmsg", "查询应用服务器信息异常");
		}
		return resultMap;
	}

	/**
	 * 获取全国服务器的在线/总数量
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="getAllServerCount")
	public Map<String,Object> getAllServerCount(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>();
		try {
			resultMap = slweomsService.getAllServerCount();
		} catch(Exception e) {
			logger.error("查询全国服务器数量异常", e);
			resultMap.put("errcode",1);
			resultMap.put("errmsg", "查询全国服务器数量异常");
			return resultMap;
		}
		return resultMap;
	}

	/**
	 * 操作平台进程
	 * @param registerid
	 * @param method
	 * @return
	 */
	@RequestMapping("/{registerid}/{method}/handleProcess")
	public Map<String,Object> handleProcess(@PathVariable String registerid,
											@PathVariable String method){
		Map<String,Object> resultMap = new HashMap<String, Object>();
		try {
			resultMap = slweomsService.handleProcess(registerid,method);
			return resultMap;
		} catch(Exception e) {
			resultMap.put("errorcode", 1);
			resultMap.put("errmsg", "操作平台进程异常");
			logger.error("操作平台进程异常",e);
			return resultMap;
		}

	}

}
