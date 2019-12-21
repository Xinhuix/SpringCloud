package com.visionvera.feign;

import org.springframework.cloud.netflix.feign.FeignClient;

import com.visionvera.common.api.operation.OperationUserAPI;
import com.visionvera.constrant.CommonConstrant;

@FeignClient(name = CommonConstrant.OPERATION_SERVICE_NAME)//运维中心服务的平台管理
public interface OperationUserService extends OperationUserAPI {

}
