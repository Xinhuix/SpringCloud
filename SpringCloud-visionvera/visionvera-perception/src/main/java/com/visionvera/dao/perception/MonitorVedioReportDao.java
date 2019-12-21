package com.visionvera.dao.perception;

import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.datacore.MonitorVedioReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2018-11-21
 */
public interface MonitorVedioReportDao extends CrudDao<MonitorVedioReport> {
	

	/**
	 * 收看列表
	 * @param params
	 * @return
	 */
	List<Map<String,Object>> getSubscribeList(Map<String, Object> params);

	/**
	 * 根据条件查询终端名称
	 * @param zoneNo 区域id
	 * @param devNo 终端号码
	 * @return
	 */
	String selectDevNameByDevNoAndZoneNo(@Param("zoneNo") String zoneNo,@Param("devNo")  String devNo);

	
}
