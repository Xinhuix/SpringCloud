package com.visionvera.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.Hospital;
import com.visionvera.dao.datacore.HospitalMapper;
import com.visionvera.service.HospitalService;
import com.visionvera.util.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Administrator
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    @Resource
    private HospitalMapper hospitalMapper;

    /**
     * 根据对象查询列表
     *
     * @param hospital
     * @return
     */
    @Override
    public PageInfo<Hospital> selectByObject(Integer pageNum, Integer pageSize, Hospital hospital) {
        PageHelper.startPage(pageNum, pageSize);
        List<Hospital> hospitals = hospitalMapper.selectByObject(hospital);
        return new PageInfo<>(hospitals);
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    @Override
    public int delete(Long id) {
        return hospitalMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据id更新
     *
     * @param record
     * @return
     */
    @Override
    public int update(Hospital record) {
        return hospitalMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 添加
     *
     * @param record
     * @return
     */
    @Override
    public int insert(Hospital record) {
        record.setUuid(UUID.randomUUID().toString().replace("-",""));
        String[] regionIds = record.getRegions().split(",");
        if(regionIds.length>0) {
            record.setLastRegion(regionIds[regionIds.length - 1]);
        }
        return hospitalMapper.insertSelective(record);
    }

    /**
     * 获取医院级别
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> level() {
        return hospitalMapper.level();
    }

    /**
     * 导出
     *
     * @param response
     * @param hospital
     */
    @Override
    public Workbook export(Hospital hospital) {
        String[] titleName = new String[]{"id","name","grade"};
        String[] keys = new String[]{"id","name","grade"};
        List<Hospital> hospitals = hospitalMapper.selectByObject(hospital);
        List<Map<String, Object>> excelDataList = new ArrayList<Map<String, Object>>();
        Map<String,Object> sheetNameMap = new HashMap<String, Object>();
        sheetNameMap.put("sheetName", "sheet");
        excelDataList.add(sheetNameMap);
        for (Hospital hospital1 : hospitals) {
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("id",hospital1.getUuid());
            dataMap.put("name",hospital1.getHospitalName());
            dataMap.put("grade",hospital1.getLevelName());
            excelDataList.add(dataMap);
        }
        return ExcelUtil.createWorkBook(excelDataList, keys, titleName);

    }

    /**
     * 获取区域信息
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> area() {
        return hospitalMapper.area();
    }

    /**
     * 区域关联行政区域
     *
     * @param areaId
     * @return
     */
    @Override
    public List<Map<String, Object>> areaRegion(Integer areaId) {
        return hospitalMapper.areaRegion(areaId);
    }

    /**
     * 行政区域id 级联
     *
     * @param regionId
     * @return
     */
    @Override
    public List<Map<String, Object>> region(Long regionId) {
        return hospitalMapper.region(regionId);
    }
}
