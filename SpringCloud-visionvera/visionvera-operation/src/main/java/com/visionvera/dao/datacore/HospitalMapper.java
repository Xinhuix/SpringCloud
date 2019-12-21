/*
 * HospitalMapper.java
 * ------------------*
 * 2019-10-23 created
 */
package com.visionvera.dao.datacore;

import com.visionvera.bean.datacore.Hospital;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface HospitalMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Hospital record);

    int insertSelective(Hospital record);

    Hospital selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Hospital record);

    int updateByPrimaryKey(Hospital record);

    void batchInsert(@Param("items") List<Hospital> items);

    List<Hospital> selectByObject(Hospital object);

    List<Map<String, Object>> level();

    List<Map<String, Object>> area();

    List<Map<String, Object>> areaRegion(Integer areaId);

    List<Map<String, Object>> region(Long regionId);
}