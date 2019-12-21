package com.visionvera.dao.ywcore;

import java.util.List;
import java.util.Map;

/**
 * 会管DAO
 *
 */
public interface SenseDao {

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
	List<Map<String, String>> getServerRateTrend(Map<String, Object> paramsMap);
	/**
     * @Description: 获取某视联网服务器的流量趋势接口(通过小时)
     * @param @param mac
     * @param @param minute
     * @param @return   
     * @return Map<String,Object>  
     * @throws
     * @author 谢程算
     * @date 2018年6月14日
     */
	List<Map<String, String>> getServerRateTrendByHour(Map<String, Object> paramsMap);
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
	List<Map<String, String>> getProvinceRateTrend(Map<String, Object> paramsMap);
	
	/**
	 * 获取某省的流量趋势接口(通过小时)
	 * @date 2018年9月11日 下午6:32:00
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	List<Map<String, String>> getProvinceRateTrendByHour(Map<String, Object> paramsMap);
	
}
