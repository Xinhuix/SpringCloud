package com.visionvera.service;

/**
 * 平台service
 * @author dql
 *
 */
public interface PlatformService {

	/**
	 * 根据平台唯一标识查询异常进程的数量
	 * @param string
	 * @return
	 */
	int getProcessExceptionCount(String registerid);


}
