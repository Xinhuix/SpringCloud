package com.visionvera.dao.datacore;

import java.util.List;
import java.util.Map;
import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.datacore.Nmsserver;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
public interface NmsserverDao extends CrudDao<Nmsserver> {
	
	/**
	 * 网管树--获取网管下行政区域列表
	 * @param omcid
	 * @return
	 * List<Map<String,Object>>
	 */
	public List<Map<String,Object>> getRegionList(Map<String,Object> params); 
	
	/**
	 * 网管树--根据行政区域获取服务器列表（服务器挂载在此行政区域下的服务器列表）
	 * @param omcid regionid
	 * @return
	 * List<Map<String,Object>>
	 */
	public List<Map<String,Object>> getNmsServerList(Map<String,Object> params); 
	
	/**
	 * 网管树--根据行政区域获取行政区域列表（行政区域下有服务器的行政区域）
	 * @param omcid
	 * @return
	 * List<Map<String,Object>>
	 */
	public List<Map<String,Object>> getRegionListByRegionid(Map<String,Object> params); 
	

}
