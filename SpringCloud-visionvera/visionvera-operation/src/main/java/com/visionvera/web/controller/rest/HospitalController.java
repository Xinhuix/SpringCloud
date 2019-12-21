package com.visionvera.web.controller.rest;

import com.github.pagehelper.PageInfo;
import com.visionvera.bean.datacore.Hospital;
import com.visionvera.service.HospitalService;
import com.visionvera.util.ResultUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@RequestMapping("hospital")
@RestController
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    /**
     * 医院列表
     * @param pageNum
     * @param pageSize
     * @param hospital
     * @return
     */
    @PostMapping("list")
    public Map<String, Object> list(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "15") Integer pageSize, Hospital hospital){
        PageInfo<Hospital> hospitalPageInfo = hospitalService.selectByObject(pageNum, pageSize, hospital);
        return ResultUtils.getResult(1,"成功",hospitalPageInfo);
    }

    /**
     * 添加医院
     * @param hospital
     * @return
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Hospital hospital) {
        int result = hospitalService.insert(hospital);
        return ResultUtils.getResult(result, result ==0?"添加失败":"添加成功");
    }

    /**
     * 更新医院
     * @param hospital
     * @return
     */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody Hospital hospital) {
        int result = hospitalService.update(hospital);
        return ResultUtils.getResult(result, result ==0?"添加失败":"添加成功");
    }

    /**
     * 删除医院
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public Map<String, Object> delete(Long id) {
        int result = hospitalService.delete(id);
        return ResultUtils.getResult(result, result ==0?"删除失败":"删除成功");
    }
    @InitBinder
    public void initBind(WebDataBinder binder){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    /**
     * 导出医院
     * @param hospital
     * @param response
     */
    @GetMapping("/export")
    public void export(Hospital hospital, HttpServletResponse response) {
        try {
            Workbook export = hospitalService.export(hospital);
            response.setHeader("content-Type", "application/vnd.ms-excel;charset=UTF-8");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename="+ URLEncoder.encode("医疗信息.xls", "utf-8"));
            export.write(response.getOutputStream());
            export.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 医院级别
     * @return
     */
    @PostMapping("/level")
    public Map<String, Object> level() {
        List<Map<String,Object>> result = hospitalService.level();
        return ResultUtils.getResult(1, "成功",result);
    }

    /**
     * 国内区域
     * @return
     */
    @GetMapping("/area")
    public Map<String, Object> area(Integer areaId) {
        List<Map<String,Object>> result;
        if(areaId==null){
            result = hospitalService.area();
        }else{
            result = hospitalService.areaRegion(areaId);
        }
        return ResultUtils.getResult(1, "成功",result);
    }

    /**
     * 行政区域级联
     * @param regionId
     * @return
     */
    @GetMapping("/region")
    public Map<String, Object> region(Long regionId) {
        List<Map<String,Object>> result = hospitalService.region(regionId);
        return ResultUtils.getResult(1, "成功",result);
    }

}
