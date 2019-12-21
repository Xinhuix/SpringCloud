package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.analysis.SlwYwMaxVO;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.ywcore.RemoteReportVO;

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
	/**
	 * 感知中心获取链路详情接口
	 * @param regionId 行政区域ID
	 * @return
	 */
	public  ReturnData getLinkDetails(String regionId);

	/**
	 * 获取抓包机列表
	 * @param removeReport 抓包机设备状态查询条件
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return
	 */
	public PageInfo<RemoteReportVO> getGrapMachineList(RemoteReportVO remoteReport, Integer pageNum, Integer pageSize);

	/**
	 * 获取抓包机树的节点：包含行政区域节点和抓包机节点
	 * @param regionId 行政区域ID
	 * @param gradeId 级别
	 * @return
	 */
	public ReturnData getGrapMachineTree(String regionId,RemoteReportVO remoteReport, String gradeId);

    ReturnData grapMachine(String regionId, String gradeId, RemoteReportVO remoteReport, Integer pageNum, Integer pageSize);
}
