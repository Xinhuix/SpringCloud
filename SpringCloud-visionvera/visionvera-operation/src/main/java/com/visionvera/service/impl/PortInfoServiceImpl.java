package com.visionvera.service.impl;

import com.visionvera.bean.ywcore.PortInfo;
import com.visionvera.dao.ywcore.PortInfoDao;
import com.visionvera.service.PortInfoService;
import com.visionvera.basecrud.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2019-11-02
 */
@Service
@Transactional(value = "transactionManager_ywcore", rollbackFor = Exception.class)
public class PortInfoServiceImpl extends CrudService<PortInfoDao, PortInfo> implements PortInfoService {

}
