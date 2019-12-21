package com.visionvera.dao.ywcore;

import com.visionvera.bean.slweoms.PlatformVO;

import java.util.List;

/**
 * 平台dao
 * @author dql714099655
 *
 */
public interface PlatformDao {

	/**
	 * 根据平台id查询进程异常的数量
	 * @param registerid
	 * @return
	 */
	int getProcessExceptionCount(String registerid);


}
