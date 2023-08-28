package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 测试使用 feign 远程上传文件
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName FeignUploadTest
 * @since 2023/1/31 11:25
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    private MediaServiceClient mediaServiceClient;

    // 远程调用，上传文件
    @Test
    void test() {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:/lessons/Xuecheng/develop/test.html"));
        mediaServiceClient.uploadFile(multipartFile, "course", "test.html");
    }
}
