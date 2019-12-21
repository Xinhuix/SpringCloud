package com.visionvera.feign;

import org.springframework.cloud.netflix.feign.FeignClient;

import com.visionvera.common.api.resource.PlatformResAPI;
import com.visionvera.constrant.CommonConstrant;

@FeignClient(name = CommonConstrant.RESOURCE_SERVICE_NAME)//静态资源服务的平台管理
public interface PlatformResourceService extends PlatformResAPI {

}
