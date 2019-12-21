package com.visionvera.web.controller.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.ywcore.PortInfo;
import com.visionvera.service.PortInfoService;
import com.visionvera.service.SyncProbeManageService;
import com.visionvera.util.DateUtil;
import com.visionvera.util.ProbeManagerMsgUtil;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhanghui
 * @since 2019-11-02
 */
@RestController
@RequestMapping("/rest/portInfo")
public class PortInfoController  extends BaseReturn {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	private PortInfoService portInfoService;
	@Autowired
	private SyncProbeManageService syncProbeManageService;
	
	 /**
     * 添加或修改端口
     * @author zhanghui
     * @param portInfo
     * @return
     * ReturnData
     */
	@RequestMapping(value="/saveOrUpdatePortInfo",method=RequestMethod.POST)
	public ReturnData saveOrUpdatePortInfo(@RequestBody PortInfo portInfo){
		try {
			Integer port =portInfo.getPort();
			Integer id =portInfo.getId();
			if(port==null){
				return super.returnError("port不能为空");
			}
			if(id==null){
				PortInfo params = new PortInfo();
				params.setPort(port);
				PortInfo result =portInfoService.get(params);
				int count =0;
				if(result==null){
					portInfo.setModifyTime(DateUtil.date2String(new Date()));
					count = portInfoService.save(portInfo);
					if(count>0){
						syncProbeManageService.portInfoChange();
						return super.returnSuccess("保存成功");
					}
				}else{
					portInfo.setId(result.getId());
					count = portInfoService.update(portInfo);
					if(count>0){
						syncProbeManageService.portInfoChange();
						return super.returnSuccess("保存成功");
					}
				}
			}else{
				int count = portInfoService.update(portInfo);
				if(count>0){
					syncProbeManageService.portInfoChange();
					return super.returnSuccess("修改成功");
				}
			}
			
			
			
		} catch (Exception e) {
			logger.error("PortInfoController===saveOrUpdatePortInfo===添加或修改端口失败：",e);
		}
		return super.returnError("设置失败");
	}
    /**
     * 删除端口
     * @author zhanghui
     * @param portInfo
     * @return
     * ReturnData
     */
	@RequestMapping(value = "/delPortInfo", method = RequestMethod.POST)
	public ReturnData delPortInfo(@RequestBody PortInfo portInfo) {
		try {
			Integer id = portInfo.getId();
			if (id == null) {
				return super.returnError("id不能为空");
			}
			int count = portInfoService.delete(portInfo);
			if (count > 0) {
				syncProbeManageService.portInfoChange();
				return super.returnSuccess("删除成功");
			}

		} catch (Exception e) {
			logger.error("PortInfoController===delPortInfo===添加或修改端口失败：", e);
		}
		return super.returnError("删除失败");
	}
    
    
    /**
     * 查询端口列表
     * @author zhanghui
     * @param portInfo
     * @return
     * ReturnData
     */
    @RequestMapping(value="/getPortInfoList",method=RequestMethod.GET)
	public ReturnData getPortInfoList( 
			@RequestParam(value="port", required=false) Integer port, 
			@RequestParam(value="description", required=false) String description, 
			@RequestParam(value="pageNum", required=false, defaultValue="1") Integer pageNum,
			 @RequestParam(value="pageSize", required=false, defaultValue="10") Integer pageSize){
    	Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			PortInfo params = new PortInfo();
			params.setPort(port);
			params.setDescription(description);
			PageInfo<PortInfo> result =portInfoService.queryListPage(params, pageNum, pageSize);
			PageInfo<PortInfo> deviceInfo = new PageInfo<PortInfo>(result.getList());
			extraMap.put("totalPage", deviceInfo.getPages());//总页数
			extraMap.put("totalCount", deviceInfo.getTotal());//总条数
			extraMap.put("totalPage", deviceInfo.getPages());
			extraMap.put("pageSize", deviceInfo.getPageSize());
			extraMap.put("pageNum", deviceInfo.getPageNum());
			return super.returnResult(0, "获取列表成功", null, result.getList(), extraMap);			
		} catch (Exception e) {
			logger.error("PortInfoController===getPortInfoList===获取列表失败：",e);
		}
		return super.returnError("获取列表失败");
	}

}
