package com.visionvera.service;

import java.util.HashMap;
import java.util.List;
import com.visionvera.basecrud.BaseService;
import com.visionvera.bean.operation.BreakdownHistory;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2018-07-05
 */
public interface BreakdownHistoryService extends BaseService<BreakdownHistory> {
	/**
	 * 获取故障历史记录
	 * @date 2018年7月9日 下午4:28:59
	 * @author wangqiubao
	 * @param history
	 * @return
	 */
	public List<BreakdownHistory> getBreakdownHistoryList(BreakdownHistory history,HashMap<String, Object> paramsMap) ;
}
