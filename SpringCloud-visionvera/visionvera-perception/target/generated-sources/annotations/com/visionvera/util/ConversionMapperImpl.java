package com.visionvera.util;

import com.visionvera.bean.datacore.MonitorVedioReport;
import com.visionvera.bean.datacore.VphoneReport;
import com.visionvera.vo.Meeting;
import com.visionvera.vo.Publish;
import com.visionvera.vo.VedioPlay;
import com.visionvera.vo.VedioRecord;
import com.visionvera.vo.Vphone;
import java.text.SimpleDateFormat;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2019-10-31T17:53:41+0800",
    comments = "version: 1.2.0.Final, compiler: javac, environment: Java 1.8.0_111 (Oracle Corporation)"
)
public class ConversionMapperImpl implements ConversionMapper {

    @Override
    public VphoneReport vphoneToVphoneReport(Vphone vphone) {
        if ( vphone == null ) {
            return null;
        }

        VphoneReport vphoneReport = new VphoneReport();

        vphoneReport.setDstAddr( vphone.getDstMac() );
        vphoneReport.setSrcAddr( vphone.getSrcMac() );
        vphoneReport.setStartTime( vphone.getTime() );
        vphoneReport.setSrcNo( vphone.getSrcNo() );
        vphoneReport.setSrcMac( vphone.getSrcMac() );
        vphoneReport.setDstNo( vphone.getDstNo() );
        vphoneReport.setDstMac( vphone.getDstMac() );
        vphoneReport.setUserName( vphone.getUserName() );
        vphoneReport.setBusinessMode( vphone.getBusinessMode() );
        vphoneReport.setBusinessType( vphone.getBusinessType() );
        vphoneReport.setBusinessState( vphone.getBusinessState() );
        if ( vphone.getTime() != null ) {
            vphoneReport.setTime( new SimpleDateFormat().format( vphone.getTime() ) );
        }

        return vphoneReport;
    }

    @Override
    public MonitorVedioReport publishToMonitorVedioReport(Publish publish) {
        if ( publish == null ) {
            return null;
        }

        MonitorVedioReport monitorVedioReport = new MonitorVedioReport();

        monitorVedioReport.setDevAddr( publish.getDevMac() );
        monitorVedioReport.setUniqId( publish.getId() );
        monitorVedioReport.setStartTime( publish.getTime() );
        monitorVedioReport.setMsgNo( publish.getMsgNo() );
        monitorVedioReport.setDevNo( publish.getDevNo() );
        monitorVedioReport.setDevName( publish.getDevName() );
        monitorVedioReport.setDevMac( publish.getDevMac() );
        monitorVedioReport.setSvrName( publish.getSvrName() );
        monitorVedioReport.setSubscribeCount( publish.getSubscribeCount() );
        monitorVedioReport.setIp( publish.getIp() );
        monitorVedioReport.setPort( publish.getPort() );
        monitorVedioReport.setMonitorId( publish.getMonitorId() );
        monitorVedioReport.setUserName( publish.getUserName() );
        monitorVedioReport.setBusinessMode( publish.getBusinessMode() );
        monitorVedioReport.setBusinessType( publish.getBusinessType() );
        monitorVedioReport.setBusinessState( publish.getBusinessState() );
        monitorVedioReport.setPlatformType( publish.getPlatformType() );
        monitorVedioReport.setPlatformId( publish.getPlatformId() );
        if ( publish.getTime() != null ) {
            monitorVedioReport.setTime( new SimpleDateFormat().format( publish.getTime() ) );
        }
        if ( publish.getId() != null ) {
            monitorVedioReport.setId( Integer.parseInt( publish.getId() ) );
        }

        return monitorVedioReport;
    }

    @Override
    public MonitorVedioReport vedioRecordToMonitorVedioReport(VedioRecord vedioRecord) {
        if ( vedioRecord == null ) {
            return null;
        }

        MonitorVedioReport monitorVedioReport = new MonitorVedioReport();

        monitorVedioReport.setStartTime( vedioRecord.getTime() );
        monitorVedioReport.setUniqId( vedioRecord.getUniqId() );
        monitorVedioReport.setVirtualNo( vedioRecord.getVirtualNo() );
        monitorVedioReport.setVedioName( vedioRecord.getVedioName() );
        monitorVedioReport.setSvrName( vedioRecord.getSvrName() );
        monitorVedioReport.setSvrAddr( vedioRecord.getSvrAddr() );
        monitorVedioReport.setRecordType( vedioRecord.getRecordType() );
        monitorVedioReport.setRecordContent( vedioRecord.getRecordContent() );
        monitorVedioReport.setUserName( vedioRecord.getUserName() );
        monitorVedioReport.setBusinessMode( vedioRecord.getBusinessMode() );
        monitorVedioReport.setBusinessType( vedioRecord.getBusinessType() );
        monitorVedioReport.setBusinessState( vedioRecord.getBusinessState() );
        monitorVedioReport.setPlatformType( vedioRecord.getPlatformType() );
        monitorVedioReport.setPlatformId( vedioRecord.getPlatformId() );
        if ( vedioRecord.getTime() != null ) {
            monitorVedioReport.setTime( new SimpleDateFormat().format( vedioRecord.getTime() ) );
        }
        if ( vedioRecord.getId() != null ) {
            monitorVedioReport.setId( Integer.parseInt( vedioRecord.getId() ) );
        }

        return monitorVedioReport;
    }

    @Override
    public MonitorVedioReport vedioPlayToMonitorVedioReport(VedioPlay vedioPlay) {
        if ( vedioPlay == null ) {
            return null;
        }

        MonitorVedioReport monitorVedioReport = new MonitorVedioReport();

        monitorVedioReport.setDevAddr( vedioPlay.getDevMac() );
        monitorVedioReport.setStartTime( vedioPlay.getTime() );
        monitorVedioReport.setUniqId( vedioPlay.getUniqId() );
        monitorVedioReport.setVedioName( vedioPlay.getVedioName() );
        monitorVedioReport.setDevNo( vedioPlay.getDevNo() );
        monitorVedioReport.setDevMac( vedioPlay.getDevMac() );
        monitorVedioReport.setUserName( vedioPlay.getUserName() );
        monitorVedioReport.setBusinessMode( vedioPlay.getBusinessMode() );
        monitorVedioReport.setBusinessType( vedioPlay.getBusinessType() );
        monitorVedioReport.setBusinessState( vedioPlay.getBusinessState() );
        monitorVedioReport.setPlatformType( vedioPlay.getPlatformType() );
        monitorVedioReport.setPlatformId( vedioPlay.getPlatformId() );
        if ( vedioPlay.getTime() != null ) {
            monitorVedioReport.setTime( new SimpleDateFormat().format( vedioPlay.getTime() ) );
        }

        return monitorVedioReport;
    }

    @Override
    public MonitorVedioReport meetingToMonitorVedioReport(Meeting meeting) {
        if ( meeting == null ) {
            return null;
        }

        MonitorVedioReport monitorVedioReport = new MonitorVedioReport();

        monitorVedioReport.setDevNo( meeting.getDevNo() );
        monitorVedioReport.setDevMac( meeting.getDevMac() );
        monitorVedioReport.setUserName( meeting.getUserName() );
        monitorVedioReport.setBusinessMode( meeting.getBusinessMode() );
        monitorVedioReport.setBusinessType( meeting.getBusinessType() );
        monitorVedioReport.setBusinessState( meeting.getBusinessState() );
        if ( meeting.getTime() != null ) {
            monitorVedioReport.setTime( new SimpleDateFormat().format( meeting.getTime() ) );
        }
        monitorVedioReport.setMeetName( meeting.getMeetName() );
        monitorVedioReport.setDevs( meeting.getDevs() );

        return monitorVedioReport;
    }
}
