package com.visionvera.service;

import java.util.Map;

/**
 * 会管数据的业务
 *
 */
public interface SenseService {

	/**
     * @Description: 获取某视联网服务器的流量趋势接口
     * @param @param mac
     * @param @param minute
     * @param @return   
     * @return Map<String,Object>  
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
	public Map<String, Object> getServerRateTrend(String mac, Integer minute,String ip);
	/**
	 * 获取某视联网服务器的流量趋势接口(通过小时)
	 * @date 2018年9月11日 下午6:41:07
	 * @author wangqiubao
	 * @param hour
	 * @return
	 */
	public Map<String, Object> getServerRateTrendByProcedure(Integer hour,String ip,String mac);
	/**
     * @Description: 获取某省的流量趋势接口
     * @param @param regionId
     * @param @param ip
     * @param @param minute
     * @param @return   
     * @return Map<String,Object>  
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
	public Map<String, Object> getProvinceRateTrend(String regionId, String ip, Integer minute);
	/**
	 * 获取某省的流量趋势接口(通过小时)
	 * @date 2018年9月11日 下午6:28:48
	 * @author wangqiubao
	 * @param hour
	 * @return
	 */
	public Map<String, Object> getProvinceRateTrendByProcedure(Integer hour,String ip,String regionId);
	
}
