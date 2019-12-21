package com.visionvera.service.impl;

import com.visionvera.bean.base.BaseReturn;
import com.visionvera.bean.base.ReturnData;
import com.visionvera.bean.datacore.TRegionb;
import com.visionvera.constrant.WsConstants;
import com.visionvera.dao.authentication.RegionDao;
import com.visionvera.service.RegionService;
import com.visionvera.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(transactionManager = "transactionManager_authentication", rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public class RegionServiceImpl implements RegionService {
    @Autowired
    private RegionDao regionDao;

    /**
     * 获取行政区域信息，提供给P-Server(掌上通)业务使用
     * @param region {"id":""} 多个id使用英文逗号","隔开
     * @return
     */
    @Override
    public ReturnData getRegionForPServer(TRegionb region) {
        BaseReturn dataReturn = new BaseReturn();
        String[] idArray = null;
        //保存行政区域ID的StringBuilder
        StringBuilder idsStringBuilder;
        //保存行政区域名称的StringBuilder
        StringBuilder namesStringBuilder;

        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();//结果List

        if (StringUtil.isNotNull(region.getId())) {
            idArray = region.getId().split(",");
        }

        List<TRegionb> regionList = this.regionDao.selectRegionByIds(idArray);//通过ID批量查询行政区域信息
        if (regionList != null && regionList.size() > 0) {
            for (TRegionb regionInfo : regionList) {
                idsStringBuilder = new StringBuilder();
                namesStringBuilder = new StringBuilder();
                //将第一次查询到的ID和name保存起来 Start
                idsStringBuilder.insert(0, regionInfo.getId() + "|");
                namesStringBuilder.insert(0, regionInfo.getName() + "|");
                //将第一次查询到的ID和name保存起来 End
                //递归调用
                Map<String, String> resultMap = this.getParentRegionRecursion(regionInfo, idsStringBuilder, namesStringBuilder);
                resultList.add(resultMap);
            }
        }
        return dataReturn.returnResult(WsConstants.OK, "获取成功", null, resultList);
    }

    /**
     * 递归查询(向上查询)：通过父ID查询行政区域信息，当gradeid为1表示一级的行政区域
     * @param region {"pid":""}
     * @param
     * @return {"ids":"110000000000,110100000000","names":"北京市,直辖市"}
     */
    public Map<String, String> getParentRegionRecursion(TRegionb region, StringBuilder idsStringBuilder, StringBuilder namesStringBuilder) {
        if (region.getGradeid().equals("1")) {
            //每个数据只有一个,所以无需创建更大的Map
            Map<String, String> resultMap = new HashMap<String, String>(1, 1);
            resultMap.put("ids", idsStringBuilder.toString());
            resultMap.put("names", namesStringBuilder.toString());
            return resultMap;
        } else {
            TRegionb regionInfo = this.regionDao.selectRegionById(region.getPid());
            idsStringBuilder.insert(0, regionInfo.getId() + "|");
            namesStringBuilder.insert(0, regionInfo.getName() + "|");
            //递归调用
            return this.getParentRegionRecursion(regionInfo, idsStringBuilder, namesStringBuilder);
        }
    }
}
