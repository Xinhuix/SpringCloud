package com.visionvera.web.controller.rest;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.AlarmInfoVO;
import com.visionvera.service.BusinessService;
import com.visionvera.util.ResultUtils;
import com.visionvera.util.StringUtil;

/**
 *  Class Name: BusinessConfig.java
 *  Description: 告警推送配置
 *  @author ==zyf==
 *  @date 2019年11月12日 下午5:01:41  
 *  @version 
 */
@RestController
@RequestMapping("/rest/businessConfig")
public class BusinessConfigController extends BaseReturn{
	
	
	@Autowired
	private BusinessService businessService;

	/**
	 *  Description:获取告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午5:28:21  
	 *  @return
	 */
	@RequestMapping("getBusinessConfig")
	public ReturnData getBusinessConfig(
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize,
            String level,String type,String subType,String subTypeName,String realName){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Map<String,Object>> businessConfigList= new ArrayList<Map<String,Object>>(); 
		Map<String, Object> extraMap = new HashMap<String, Object>();
		try {
			paramMap.put("level", level);
			paramMap.put("type", type);
			paramMap.put("subType", subType);
			paramMap.put("typeName", subTypeName == null ? subTypeName
					: URLDecoder.decode(subTypeName, "utf-8"));
			paramMap.put("realName", realName == null ? realName
					: URLDecoder.decode(realName, "utf-8"));
			//获取告警推送配置
			businessConfigList = businessService.getBusinessConfig(paramMap);
			PageInfo<Map<String,Object>> deviceInfo = new PageInfo<Map<String,Object>>(businessConfigList);
			extraMap.put("totalPage", deviceInfo.getPages());//总页数
			extraMap.put("totalCount", deviceInfo.getTotal());//总条数
			extraMap.put("totalPage", deviceInfo.getPages());
			extraMap.put("pageSize", deviceInfo.getPageSize());
			extraMap.put("pageNum", deviceInfo.getPageNum());
			return super.returnResult(0, "获取列表成功", null, businessConfigList, extraMap);		
		} catch (Exception e) {
			return super.returnResult(1,"查询失败");
		}
	}
	
	
	
	/**
	 *  Description:根据告警等级和告警类型获取推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月12日 下午8:41:16  
	 *  @param level
	 *  @param type
	 *  @return
	 */
	@RequestMapping("getPushUserConfig")
	public ReturnData getPushUserConfig(String level,String type,String subType){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			paramMap.put("level", level);
			paramMap.put("type", type);
			paramMap.put("subType", subType);
			//获取告警推送配置
			List<Map<String,Object>> list = businessService.getPushUserConfig(paramMap);
			return super.returnResult(0, "查询成功", null, list);
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	
	
	/**
	 *  Description:根据告警等级和类型更新推送人员
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:06:23  
	 *  @param level
	 *  @param type
	 *  @param subType
	 *  @return
	 */
	@RequestMapping("updatePushUserConfig")
	public ReturnData updatePushUserConfig(@RequestBody AlarmInfoVO alarmInfoVO){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<AlarmInfoVO> list = new ArrayList<AlarmInfoVO>();
		try {
			if(alarmInfoVO == null){
				return super.returnResult(1, "参数错误，参数不能为空");
			}
			if(StringUtil.isNull(alarmInfoVO.getLevel())){
				return super.returnResult(1, "参数错误，等级不能为空");
			}
			if(StringUtil.isNull(alarmInfoVO.getType())){
				return super.returnResult(1, "参数错误，类别不能为空");
			}
			if(StringUtil.isNull(alarmInfoVO.getSubType())){
				return super.returnResult(1, "参数错误，类型不能为空");
			}
			if(StringUtil.isNull(alarmInfoVO.getUserId())){
				return super.returnResult(1, "参数错误，用户唯一标识不能为空");
			}
			String[] userId = alarmInfoVO.getUserId().split(",");
			AlarmInfoVO alVo;
			//循环人员id
			for (int i = 0; i < userId.length; i++) {
				alVo = new AlarmInfoVO();
				alVo.setUserId(userId[i]);
				alVo.setLevel(alarmInfoVO.getLevel());
				alVo.setType(alarmInfoVO.getType());
				alVo.setSubType(alarmInfoVO.getSubType());
				alVo.setAlarmId(StringUtil.get32UUID());
				list.add(alVo);
			}
			paramMap.put("level", alarmInfoVO.getLevel());
			paramMap.put("type", alarmInfoVO.getType());
			paramMap.put("subType", alarmInfoVO.getSubType());
			//删除原有告警推送人员
			int i = businessService.deletePushUserConfig(paramMap);
			//更新告警推送人员
			int j = businessService.addAlarmUserConfig(list);
			return super.returnResult(0, "更新成功");
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	
	/**
	 *  Description:删除配置（支持批量删除）
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:08:21  
	 *  @param uuid
	 *  @return
	 */
	@RequestMapping("deleteBusinessConfig")
	public ReturnData deleteBusinessConfig(String uuid){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			if(StringUtil.isNull(uuid)){
				return super.returnError("参数错误，用户ID为空");
			}
			String[] uuids = uuid.split(",");
			paramMap.put("uuids", uuids);
			//获取告警推送配置
			int i = businessService.deleteBusinessConfig(paramMap);
			if(i > 0){
				return super.returnResult(0, "删除成功");
			}else{
				return super.returnResult(1, "删除失败");
			}
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	
	/**
	 *  Description:获取预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:27:57  
	 *  @return
	 */
	@RequestMapping("getDefaultUser")
	public ReturnData getDefaultUser(String userId,String realName){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			paramMap.put("uuid", userId);
			paramMap.put("realName", realName == null ? realName
					: URLDecoder.decode(realName, "utf-8"));
			//获取告警推送配置
			List<Map<String,Object>> list = businessService.getDefaultUser(paramMap);
			return super.returnResult(0, "查询成功", null, list);
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	
	
	/**
	 *  Description:更新预设用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:38:48  
	 *  @param uuids
	 *  @param loginNames
	 *  @return
	 */
	@RequestMapping("updateDefaultUser")
	public ReturnData updateDefaultUser(@RequestBody AlarmInfoVO alarmInfoVO){
		Map<String, Object> paramMap = null;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			if (!StringUtil.isNull(alarmInfoVO.getUserId())) {
				//为审批人赋valueType属性值
				String [] uuidsArray = alarmInfoVO.getUserId().split(",");
				for (int i = 0; i < uuidsArray.length; i++) {
					paramMap = new HashMap<String, Object>();
					paramMap.put("uuid", uuidsArray[i]);
					list.add(paramMap);
				}
				int i = businessService.updateDefaultUser(list);
			}
			//获取告警推送配置
			return super.returnResult(0, "更新成功");
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	
	/**
	 *  Description:获取告警等级、类别、类型树结构
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 上午10:38:48  
	 *  @param root 父级id
	 *  @param level 告警等级
	 *  @return
	 */
	@RequestMapping("getRoot")
	public ReturnData getRoot(String level,String type){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		try {
			//等级为空，默认获取第一级告警级别
			if(StringUtil.isNull(level)){
				list = businessService.getLevelRoot();
				return super.returnResult(0, "查询成功",null,list);
			}
			if(StringUtil.isNull(type)){
				list = businessService.getTypeRoot();
				return super.returnResult(0, "查询成功",null,list);
			}
			paramMap.put("type", type);
			paramMap.put("level", level);
			//根据父级ID和等级level获取告警类型(每个级别的告警，网络和硬件下都有需要阈值定义级别的告警类型)
			list = businessService.getSubTypeRoot(paramMap);
			return super.returnResult(0, "查询成功",null,list);
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	/**
	 *  Description:新增告警推送配置
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午1:59:39  
	 *  @param alarmInfo
	 *  @return
	 */
	@RequestMapping("addAlarmUserConfig")
	public ReturnData addAlarmUserConfig(@RequestBody AlarmInfoVO alarmInfo){
		try {
			if(alarmInfo == null){
				return super.returnResult(1, "参数错误，参数不能为空");
			}
			if(StringUtil.isNull(alarmInfo.getLevel())){
				return super.returnResult(1, "参数错误，等级不能为空");
			}
			if(StringUtil.isNull(alarmInfo.getType())){
				return super.returnResult(1, "参数错误，类别不能为空");
			}
			if(StringUtil.isNull(alarmInfo.getSubType())){
				return super.returnResult(1, "参数错误，类型不能为空");
			}
			if(StringUtil.isNull(alarmInfo.getUserId())){
				return super.returnResult(1, "参数错误，用户唯一标识不能为空");
			}
			//处理数据，方便入库
			String[] subType = alarmInfo.getSubType().split(",");
			String[] userId = alarmInfo.getUserId().split(",");
			AlarmInfoVO alInfoVO;
			List<AlarmInfoVO> list = new ArrayList<AlarmInfoVO>();
			//先循环告警类型
			for (int j = 0; j < subType.length; j++) {
				//再循环人员id
				for (int i = 0; i < userId.length; i++) {
					alInfoVO = new AlarmInfoVO();
					alInfoVO.setLevel(alarmInfo.getLevel());
					alInfoVO.setType(alarmInfo.getType());
					alInfoVO.setUserId(userId[i]);
					alInfoVO.setSubType(subType[j]);
					alInfoVO.setAlarmId(StringUtil.get32UUID());
					list.add(alInfoVO);
				}
			}
			businessService.addAlarmUserConfig(list);
			return super.returnResult(0, "添加成功");
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
	/**
	 *  Description:获取拥有视联汇--告警推送权限的用户
	 *  @author  ==zyf==
	 *  @date 2019年11月13日 下午4:10:42  
	 *  @param level
	 *  @param type
	 *  @return
	 */
	@RequestMapping("getUser")
	public ReturnData getUser(String userId,String realName,String loginName){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			paramMap.put("uuid", userId);
			paramMap.put("realName", realName == null ? realName
					: URLDecoder.decode(realName, "utf-8"));
			paramMap.put("loginName", loginName == null ? loginName
					: URLDecoder.decode(loginName, "utf-8"));
			//获取视联汇用户
			List<Map<String,Object>> list = businessService.getUser(paramMap);
			return super.returnResult(0, "查询成功",null,list);
		} catch (Exception e) {
			return super.returnError("系统内部异常");
		}
		
	}
	
}
