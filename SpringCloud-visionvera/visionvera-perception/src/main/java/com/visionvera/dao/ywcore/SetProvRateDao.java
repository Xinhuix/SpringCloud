package com.visionvera.dao.ywcore;

import com.visionvera.bean.ywcore.SetProvRateVO;

import java.util.List;

/**
 * 各省/市/县与主服务器之间的流量数据库操作层
 * @author Bianjf
 *
 */
public interface SetProvRateDao {
	/**
	 * 通过regionId查询最新的一条数据，获取其上下行带宽
	 * @param setProvRate
	 * @return
	 */
	public List<SetProvRateVO> selectRateByRegionId(SetProvRateVO setProvRate);
}
