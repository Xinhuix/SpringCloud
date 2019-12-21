package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.service.TlogService;

@RestController
@RequestMapping("/rest/tlog")
public class TlogController extends BaseReturn {
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TlogService tlogService;
	
	 /**
     * 查询端口列表
     * @author zhanghui
     * @param portInfo
     * @return
     * ReturnData
     */
    @RequestMapping(value="/getTlogList",method=RequestMethod.GET)
	public ReturnData getTlogList( 
			@RequestParam(value="createName", required=false) String createName, 
			@RequestParam(value="description", required=false) String description, 
			@RequestParam(value="logType", required=false) String logType, 
			@RequestParam(value="platformId", required=false) String platformId, 
			@RequestParam(value="startTime", required=false) String startTime, 
			@RequestParam(value="endTime", required=false) String endTime, 
			@RequestParam(value="pageNum", required=false, defaultValue="1") Integer pageNum,
			 @RequestParam(value="pageSize", required=false, defaultValue="10") Integer pageSize){
    	Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("createName", createName);
			params.put("description", description);
			params.put("logType", logType);
			params.put("platformId", platformId);
			params.put("startTime", startTime);
			params.put("endTime", endTime);
			params.put("pageNum", pageNum);
			params.put("pageSize", pageSize);
			List<Map<String,Object>> result =tlogService.getTlogListSelective(params);
			PageInfo<Map<String,Object>> deviceInfo = new PageInfo<Map<String,Object>>(result);
			extraMap.put("totalPage", deviceInfo.getPages());//总页数
			extraMap.put("totalCount", deviceInfo.getTotal());//总条数
			extraMap.put("totalPage", deviceInfo.getPages());
			extraMap.put("pageSize", deviceInfo.getPageSize());
			extraMap.put("pageNum", deviceInfo.getPageNum());
			return super.returnResult(0, "获取列表成功", null, result, extraMap);			
		} catch (Exception e) {
			logger.error("TlogController===getTlogList===获取列表失败：",e);
		}
		return super.returnError("获取列表失败");
	}
    /**
     * 查询端口列表
     * @author zhanghui
     * @param portInfo
     * @return
     * ReturnData
     */
    @RequestMapping(value="/getTlogTypeList",method=RequestMethod.GET)
    public ReturnData getTlogTypeList(){
    	try {
    		
    		List<Map<String,Object>> result =tlogService.getTlogTypeList();
    		return super.returnResult(0, "获取列表成功", null, result, null);			
    	} catch (Exception e) {
    		logger.error("TlogController===getTlogTypeList===获取列表失败：",e);
    	}
    	return super.returnError("获取列表失败");
    }

}
