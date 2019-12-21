package com.visionvera.feign;

import org.springframework.cloud.netflix.feign.FeignClient;

import com.visionvera.common.api.alarm.AlarmAPI;
import com.visionvera.constrant.CommonConstrant;

@FeignClient(name = CommonConstrant.ALARM_SERVICE_NAME)//报警服务的平台管理
public interface AlarmService extends AlarmAPI {

}
