package com.xuecheng.media;

import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 测试 Minio 上传、删除、查询文件
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName MinioTest
 * @since 2023/1/21 17:25
 */
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    void testUpload() throws Exception {
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .object("F22_2.mp4") // 同一个组内对象名不能同名
                .filename("D:\\EVVideos\\F22_2.mp4")
                .build();
        // 上传
        minioClient.uploadObject(uploadObjectArgs);
        System.out.println("上传完成。");
    }

    // 指定桶内的子目录
    @Test
    void testUpload2() throws Exception {
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .object("F22/F22_3.mp4") // 同一个组内对象名不能同名
                .filename("D:\\EVVideos\\F22_3.mp4")
                .build();
        // 上传
        minioClient.uploadObject(uploadObjectArgs);
        System.out.println("上传完成。");
    }

    // 测试删除文件
    @Test
    void testDelete() throws Exception {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("F22_1.mp4")
                .build();
        minioClient.removeObject(removeObjectArgs);
        System.out.println("删除成功。");
    }

    // 测试查询文件
    @Test
    void testGetFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("F22_2.mp4")
                .build();
        try (FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
             FileOutputStream outputStream = new FileOutputStream("F22.mp4")) {
            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
