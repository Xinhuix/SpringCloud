package com.visionvera.dao.datacore;

import java.util.List;
import java.util.Map;

import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.datacore.Serverinfob;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghui
 * @since 2019-07-23
 */
public interface ServerinfobDao extends CrudDao<Serverinfob> {
	
	
	public List<Map<String,Object>> getServers(Map<String,Object> params); 

}
