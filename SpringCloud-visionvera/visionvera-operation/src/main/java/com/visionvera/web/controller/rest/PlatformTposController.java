package com.visionvera.web.controller.rest;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.slweoms.PlatformVO;
import com.visionvera.service.PlatformService;
import com.visionvera.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台Controller
 * @author dql
 *
 */
@RestController
public class PlatformTposController extends BaseReturn {
	
	private Logger logger = LoggerFactory.getLogger(PlatformTposController.class);
	
	@Autowired
	private PlatformService platformService;
	
	/**
	 * 根据服务器唯一标识查询平台列表
	 * @param serverUnique
	 * @return
	 */
	@RequestMapping(value="/platform/{serverUnique}/list",method=RequestMethod.GET)
	public ReturnData getPlatformByServerUnique(@PathVariable String serverUnique) {
		try {
			List<PlatformVO> platformList = platformService.getPlatformListByServerUnique(serverUnique);
			return super.returnResult(0, "查询服务器列表信息成功", null, platformList);
		} catch(Exception e) {
			logger.error("查询服务器平台列表失败", e);
			return super.returnError("查询服务器平台列表失败");
		}
	} 
	
	/**
	 * 添加或者修改平台
	 * @param platformVO
	 * @return
	 */
	@RequestMapping(value="/platform/insertOrUpdate",method=RequestMethod.POST)
	public ReturnData insertOrUpdatePlatform(@RequestBody PlatformVO platformVO) {
		try {
			String tposRegisterid = platformVO.getTposRegisterid();
			if(StringUtil.isNull(tposRegisterid)) {
				platformVO.setTposState(0);
				platformService.insertPlatform(platformVO, null);
				return super.returnSuccess("添加平台成功");
			}else {
				//修改平台
				platformService.updatePlatform(platformVO);
				return super.returnSuccess("修改平台成功");
			}
		} catch(Exception e) {
			logger.error("平台操作失败", e);
			return super.returnError("平台操作失败");
		}
	}
	
	/**
	 * 删除平台
	 * @param tposRegisterid
	 * @return
	 */
	@RequestMapping(value="/platform/{tposRegisterid}/delete")
	public ReturnData deletePlatform(@PathVariable String tposRegisterid) {
		try {
			platformService.deletePlatform(tposRegisterid);
			return super.returnSuccess("删除平台成功");
		}catch(Exception e) {
			logger.error("删除平台失败", e);
			return super.returnError("删除平台失败");
		}
	}


}
