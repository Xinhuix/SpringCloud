package com.visionvera.dao.ywcore;

import com.visionvera.bean.ywcore.SetTerminalRateVO;

import java.util.List;

/**
 * 旧的终端(实际上是路由的意思)的流量数据库操作层
 * @author Bianjf
 *
 */
public interface SetTerminalRateDao {
	/**
	 * 通过条件查询链路的数据流量
	 * @param setTerminalRateVO 查询条件
	 * @return
	 */
	public List<SetTerminalRateVO> selectSetTerminalRateByCondition(SetTerminalRateVO setTerminalRateVO);
}
