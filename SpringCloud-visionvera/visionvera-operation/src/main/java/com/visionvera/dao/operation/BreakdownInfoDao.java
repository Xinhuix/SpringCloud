package com.visionvera.dao.operation;

import java.util.Map;

import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.operation.BreakdownInfo;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
public interface BreakdownInfoDao extends CrudDao<BreakdownInfo> {
	/**
	 * 获取故障信息的统计数据
	 * @date 2018年7月9日 上午9:10:50
	 * @author wangqiubao
	 * @param info
	 * @return
	 */
	public Map<String,Object> getBreakdownInfoCount(BreakdownInfo info) ;
}
