package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableSwagger2Doc
@SpringBootApplication
public class ContentApiApplication { // 注意主类的存放位置（因为要扫描其他模块的组件）

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }

}
