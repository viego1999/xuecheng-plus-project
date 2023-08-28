package com.xuecheng.content.feignclient;

import com.xuecheng.content.feignclient.po.CourseIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 搜索服务远程接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName SearchServiceClient
 * @since 2023/1/31 18:11
 */
@FeignClient(value = "search",
        fallbackFactory = SearchServiceClient.SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    /**
     * 添加课程
     */
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);

    @Slf4j
    class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

        @Override
        public SearchServiceClient create(Throwable cause) {
            log.error("远程调用课程索引添加服务时发生熔断，熔断异常是：{}", cause.getMessage());
            return null;
        }
    }

}
