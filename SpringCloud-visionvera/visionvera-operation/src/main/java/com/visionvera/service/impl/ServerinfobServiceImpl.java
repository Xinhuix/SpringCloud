package com.visionvera.service.impl;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.Serverinfob;
import com.visionvera.dao.datacore.ServerinfobDao;
import com.visionvera.service.ServerinfobService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.basecrud.CrudService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
@Service("serverinfobService")
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class ServerinfobServiceImpl extends CrudService<ServerinfobDao, Serverinfob> implements ServerinfobService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass()); 
	
	@Override
	public ReturnData getServers(Map<String, Object> params) {
		BaseReturn baseReturn = new BaseReturn();
		try {
			Integer  pageNum =(Integer) params.get("pageNum");
			Integer  pageSize =(Integer) params.get("pageSize");
			if(pageSize!=-1){
				PageHelper.startPage(pageNum, pageSize);
			}
			List<Map<String,Object>> serverList = super.dao.getServers(params);
			PageInfo<Map<String, Object>> serverInfo = new PageInfo<Map<String,Object>>(serverList);
			Map<String, Object> extraMap = new HashMap<String, Object>();
			extraMap.put("totalPage", serverInfo.getPages());//总页数
			extraMap.put("totalCount", serverInfo.getTotal());//总条数
			extraMap.put("totalPage", serverInfo.getPages());
			extraMap.put("pageSize", serverInfo.getPageSize());
			extraMap.put("pageNum", serverInfo.getPageNum());
			return baseReturn.returnResult(0, "获取成功", null, serverInfo.getList(), extraMap);
			
		} catch (Exception e) {
			logger.error("ServerinfobServiceImpl--getServers",e);
		}
		return baseReturn.returnError("获取失败");
	}

}
