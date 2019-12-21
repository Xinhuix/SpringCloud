package com.visionvera.web.controller.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Nmsserver;
import com.visionvera.service.NmsserverService;

/**
 * <p>
 *  前端控制类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@RestController
@RequestMapping("/rest/nmsserver")
public class NmsserverController extends BaseReturn {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Autowired
        private NmsserverService nmsserverService;
		


		/**
		 * 获取网管列表
		 * @Description TODO
		 * @author zhangh
		 * @return
		 * ReturnData
		 */
		@RequestMapping("/getNmsServersList")
		public ReturnData getNmsServersList(@RequestParam(value="name",required=false) String name){
		    try {
		    	Nmsserver nmsserver =new Nmsserver();
		    	nmsserver.setName(name);
		    	List<Nmsserver> serverList = nmsserverService.queryList(nmsserver);
				return super.returnResult(0, "", null,serverList, null);
				
			} catch (Exception e) {
				logger.error("NmsserverController--getNmsServersList",e);
			}
		    return super.returnError("获取失败");
		}
		
		
		
		/**
		 * 获取网管下行政区域列表
		 * @Description TODO
		 * @author zhanghui
		 * @return
		 * ReturnData
		 */
		@RequestMapping("/getRegionList")
		public ReturnData getRegionList(@RequestParam(value="omcid",required=true) String omcid,
				@RequestParam(value="regionid",required=false) String regionid,
				@RequestParam(value="gradeid",defaultValue="0",required=false) String gradeid){
		    try {
		    	Map<String,Object> params = new HashMap<String,Object>();
		    	params.put("omcid", omcid);
		    	params.put("regionid", regionid);
		    	params.put("gradeid", gradeid);
		    	ReturnData returData = nmsserverService.getNmsServersRegionList(params);
				return returData;
				
			} catch (Exception e) {
				logger.error("NmsserverController--getNmsServersList",e);
			}
		    return super.returnError("获取失败");
		}
		/**
		 * 获取网管下行政区域和服务器列表
		 * @Description TODO
		 * @author zhanghui
		 * @return
		 * ReturnData
		 */
		@RequestMapping("/getServersAndRegionList")
		public ReturnData getServersAndRegionList(@RequestParam(value="omcid",required=true) String omcid,
				@RequestParam(value="regionid",required=false) String regionid,
				@RequestParam(value="gradeid",defaultValue="0",required=false) String gradeid){
			try {
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("omcid", omcid);
				params.put("regionid", regionid);
				params.put("gradeid", gradeid);
				ReturnData returData = nmsserverService.getNmsServersAndRegionList(params);
				return returData;
				
			} catch (Exception e) {
				logger.error("NmsserverController--getNmsServersList",e);
			}
			return super.returnError("获取失败");
		}

}

