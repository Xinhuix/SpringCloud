package com.visionvera.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface ServerHardwareHistoryService {
    /**
     * 历史列表查询
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param field 字段名
     * @param serverUnique 服务器唯一标识
     * @param num 数量
     * @return
     */
    Map<String, Object> selectServerHardwareHistory(Date startTime, Date endTime, String field, String serverUnique, Integer num);

    /**
     * 删除历史数据
     */
    void generateHistory();

    /**
     * 清理历史数据
     */
    void deleteHistory();
}
