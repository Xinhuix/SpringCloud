package com.visionvera.util;

import com.visionvera.bean.datacore.MonitorVedioReport;
import com.visionvera.bean.datacore.VphoneReport;
import com.visionvera.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author Administrator
 * @date 2018年09月21日 12:47
 */
@Mapper
public interface ConversionMapper {
    ConversionMapper MAPPER = Mappers.getMapper(ConversionMapper.class);

    /**
     * 可视电话参数转换
     * @param vphone
     * @return
     */
    @Mappings({
            @Mapping(source = "srcMac", target = "srcAddr"),
            @Mapping(source = "dstMac", target = "dstAddr"),
            @Mapping(source = "time", target = "startTime")
    })
    VphoneReport vphoneToVphoneReport(Vphone vphone);

    /**
     * 发布直播参数转换
     * @param publish
     * @return
     */
    @Mappings({
            @Mapping(source = "devMac", target = "devAddr"),
            @Mapping(source = "id", target = "uniqId"),
            @Mapping(source = "time", target = "startTime")
    })
    MonitorVedioReport publishToMonitorVedioReport(Publish publish);
    /**
     * 发布直播参数转换
     * @param publish
     * @return
     */
    @Mappings({
//            @Mapping(source = "devMac", target = "devAddr"),
            @Mapping(source = "time", target = "startTime")
    })
    MonitorVedioReport vedioRecordToMonitorVedioReport(VedioRecord vedioRecord);

    /**
     * 查询点播参数转换
     * @param vedioPlay
     * @return
     */
    @Mappings({
            @Mapping(source = "devMac", target = "devAddr"),
            @Mapping(source = "time", target = "startTime")
    })
    MonitorVedioReport vedioPlayToMonitorVedioReport(VedioPlay vedioPlay);

    /**
     * 查询会议参数转换
     * @param meeting
     * @return
     */
    MonitorVedioReport meetingToMonitorVedioReport(Meeting meeting);
}
