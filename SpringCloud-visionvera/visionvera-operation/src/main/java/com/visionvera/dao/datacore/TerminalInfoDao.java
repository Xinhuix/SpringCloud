package com.visionvera.dao.datacore;

import org.apache.ibatis.annotations.Param;

public interface TerminalInfoDao {
    Integer selectTerminalInfo(@Param("mac") String mac, @Param("devno") String devno);
}
