package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒资管理服务远程接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName MediaServiceClient
 * @since 2023/1/31 11:12
 */
@FeignClient(value = "media-api",
        configuration = MultipartSupportConfig.class,
        fallbackFactory = MediaServiceClient.MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    /**
     * 上传文件
     */
    @RequestMapping(value = "/media/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    String uploadFile(@RequestPart("filedata") MultipartFile filedata,
                      @RequestParam(value = "folder", required = false) String folder,
                      @RequestParam(value = "objectName", required = false) String objectName);

    // 服务降级处理，可以获取异常信息
    @Slf4j
    class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
        // 降级方法
        @Override
        public MediaServiceClient create(Throwable cause) {
            log.error("远程调用媒资管理服务上传文件时发生熔断，熔断异常是：{}", cause.getMessage());
            return null;
        }
    }
}
