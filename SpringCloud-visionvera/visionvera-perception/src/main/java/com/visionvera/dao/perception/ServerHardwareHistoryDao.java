package com.visionvera.dao.perception;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface ServerHardwareHistoryDao {
    /**
     * 批量插入设备历史信息
     * @param items 历史数据
     * @param times 数据间隔 分钟数 目前支持 15 30 60 分钟
     * @return
     */
    int batchInsert(@Param("items") Map<String,Map<String,Object>> items, @Param("times") Integer times);

    /**
     * 查询设备硬件信息历史
     * @param time 统计间隔 分钟数 目前支持 15 30 60 分钟
     * @param serverUnique
     * @param startTime  查询开始时间
     * @param endTime 查询结束时间
     * @return
     */
    List<Map<String,Object>> selectServerHardwareHistory(@Param("times") Integer time,@Param("serverUnique") String serverUnique, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 清理历史数据
     * @param time
     * @param times
     * @return
     */
    int deleteHistory(@Param("time")Date time, @Param("times") int times);
}
