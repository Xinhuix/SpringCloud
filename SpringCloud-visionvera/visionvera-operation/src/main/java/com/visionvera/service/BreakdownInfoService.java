package com.visionvera.service;

import java.util.List;
import java.util.Map;
import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.operation.BreakdownInfo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
public interface BreakdownInfoService extends BaseService<BreakdownInfo> {
	/**
	 * 新增故障信息
	 * @date 2018年7月7日 下午3:50:43
	 * @author wangqiubao
	 * @param BreakdownInfo
	 */
	public void addBreakdownInfo(BreakdownInfo breakdownInfo) ;
	/**
	 * 修改故障信息
	 * @date 2018年7月9日 下午4:07:01
	 * @author wangqiubao
	 * @param breakdownInfo
	 */
	public void updateBreakdownInfo(BreakdownInfo breakdownInfo) ;
	/**
	 * 删除故障信息
	 * @date 2018年7月9日 下午4:09:08
	 * @author wangqiubao
	 * @param breakdownInfo
	 */
	public void deleteBreakdownInfo(BreakdownInfo breakdownInfo) ;
	
	/**
	 * 获取故障会议列表
	 * @date 2018年7月6日 下午5:31:58
	 * @author wangqiubao
	 * @param paramMap
	 * @return
	 */
	public List<BreakdownInfo> getBreakdownInfoList(BreakdownInfo breakdownInfo,Map<String,Object> paramMap);
}
