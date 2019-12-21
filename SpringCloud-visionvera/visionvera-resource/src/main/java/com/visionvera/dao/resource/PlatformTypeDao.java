package com.visionvera.dao.resource;

import java.util.List;
import java.util.Map;

import com.visionvera.bean.datacore.TPlatformTypeVO;
/**
 * 平台类型Dao
 * @author dql
 *
 */
public interface PlatformTypeDao {

	List<TPlatformTypeVO> getPlatformTypeList(Map<String,Object> params);

}
