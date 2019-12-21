package com.visionvera.dao.perception;

import com.visionvera.basecrud.CrudDao;
import com.visionvera.bean.datacore.VphoneReport;

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
public interface VphoneReportDao extends CrudDao<VphoneReport> {

    List<Map<String, Object>> queryListBySearchText(Map<String, Object> params);

}
