package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableSwagger2Doc
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@SpringBootApplication
public class ContentApplication { // 注意主类的存放位置（因为要扫描其他模块的组件）

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
