package com.visionvera.dao.authentication;

import com.visionvera.bean.datacore.TRegionb;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作t_regionb表
 * @author Bianjf
 *
 */
public interface RegionDao {
    /**
     * 通过主键ID批量查询区域信息
     * @param idArray ID的数组
     * @return
     */
    public List<TRegionb> selectRegionByIds(@Param("idArray") String[] idArray);

    /**
     * 通过主键ID查询行政区域信息
     * @param id 主键ID
     * @return
     */
    public TRegionb selectRegionById(@Param("id") String id);
}
