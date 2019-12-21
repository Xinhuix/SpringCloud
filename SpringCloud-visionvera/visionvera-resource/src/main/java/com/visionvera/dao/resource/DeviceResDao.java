package com.visionvera.dao.resource;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.visionvera.bean.cms.DeviceGroupVO;
import com.visionvera.bean.cms.DeviceRoleVO;
import com.visionvera.bean.cms.DeviceTreeVO;
import com.visionvera.bean.cms.DeviceTypeVO;
import com.visionvera.bean.cms.DeviceVO;
import com.visionvera.bean.cms.RegionVO;
import com.visionvera.bean.cms.ScheduleVO;
import com.visionvera.bean.cms.ServerInfoVO;
import com.visionvera.bean.cms.UserGroupVO;
import com.visionvera.bean.cms.UserVO;
import com.visionvera.bean.datacore.TTerminalInfoVO;


public interface DeviceResDao {
	/**
	 * 
	 * TODO 获取联系人（设备）列表
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @param rowBounds 
	 * @return
	 */
	List<DeviceVO> getUserDevices(Map<String, Object> paramsMap, RowBounds rowBounds);
	/**
	 * 
	 * TODO 获取联系人（设备）列表总数
	 * @author 谢程算
	 * @date 2017年10月17日  
	 * @version 1.0.0 
	 * @param paramsMap
	 * @return
	 */
	int getUserDevicesCount(Map<String, Object> paramsMap);
	/**
	 * 
	 * @Title: getChild
	 * @Description: 获取行政区域信息的子节点
	 * @param @return 参数说明
	 * @return Map<String,Object> 返回类型
	 * @throws
	 */
	List<RegionVO> getChild(Map<String, Object> paramsMap);
	/**
	 * 获取指定区域的设备数
	 * @date 2018年7月5日 下午2:42:05
	 * @author wangqiubao
	 * @param paramsMap
	 * @return
	 */
	int getDevicesCount(Map<String, Object> paramsMap);
	
	/**
	 * 查询终端信息，携带出项目名称和详细地址
	 * @return
	 */
	public List<TTerminalInfoVO> selectTerminalInfo();
}
