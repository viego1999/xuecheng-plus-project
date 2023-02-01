package com.xuecheng.ucenter.feignclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务远程接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CheckCodeClient
 * @since 2023/2/1 18:48
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClient.CheckCodeClientFallbackFactory.class)
public interface CheckCodeClient {

    @PostMapping("/checkcode/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);

    @Slf4j
    class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {

        @Override
        public CheckCodeClient create(Throwable cause) {
            log.error("验证码服务远程过程调用失败，熔断信息：{}", cause.getMessage());
            return null;
        }
    }
}
