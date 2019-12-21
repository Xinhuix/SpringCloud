package com.visionvera.service.impl;

import com.visionvera.basecrud.CrudService;
import com.visionvera.bean.datacore.VphoneReport;
import com.visionvera.dao.perception.VphoneReportDao;
import com.visionvera.service.VphoneReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2018-11-21
 */
@Service
@Transactional(value = "transactionManager_perception", rollbackFor = Exception.class)
public class VphoneReportServiceImpl extends CrudService<VphoneReportDao, VphoneReport> implements VphoneReportService {

    @Override
    public List<Map<String, Object>> queryListBySearchText(String order, String sidx) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("order", order);
		params.put("sidx", sidx);
        return this.dao.queryListBySearchText(params);
    }


}
