package com.visionvera.service;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.Hospital;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public interface HospitalService {
    /**
     * 根据对象查询列表
     * @param pageNum
     * @param pageSize
     * @param hospital
     * @return
     */
    PageInfo<Hospital> selectByObject(Integer pageNum,Integer pageSize,Hospital hospital);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    int delete(Long id);

    /**
     * 根据id更新
     * @param record
     * @return
     */
    int update(Hospital record);

    /**
     * 添加
     * @param record
     * @return
     */
    int insert(Hospital record);

    /**
     * 获取医院级别
     * @return
     */
    List<Map<String, Object>> level();

    /**
     * 导出
     * @param hospital
     * @return
     */
    Workbook export(Hospital hospital);

    /**
     * 获取区域信息
     * @return
     */
    List<Map<String, Object>> area();

    /**
     * 区域关联行政区域
     * @param areaId
     * @return
     */
    List<Map<String, Object>> areaRegion(Integer areaId);

    /**
     * 行政区域id 级联
     * @param regionId
     * @return
     */
    List<Map<String, Object>> region(Long regionId);
}
