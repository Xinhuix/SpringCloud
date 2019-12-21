package com.visionvera.service.impl;

import com.visionvera.dao.datacore.TerminalInfoDao;
import com.visionvera.service.TerminalInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "transactionManager_dataCore", rollbackFor = Exception.class)
public class TerminalInfoServiceImpl implements TerminalInfoService {

    @Autowired
    private TerminalInfoDao terminalInfoDao;

    @Override
    public Integer selectTerminalInfo(String v2vNetMac, String terminalCode) {
        return terminalInfoDao.selectTerminalInfo(v2vNetMac, terminalCode);
    }
}