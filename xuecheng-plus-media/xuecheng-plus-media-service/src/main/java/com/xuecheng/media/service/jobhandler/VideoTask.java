package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理任务
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName VideoTask
 * @since 2023/1/25 17:04
 */
@Slf4j
@Component
public class VideoTask {
    @Autowired
    private MediaFileService mediaFileService;
    @Autowired
    private MediaFileProcessService mediaFileProcessService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /**
     * 视频处理任务
     *
     * @throws Exception 异常
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();

        // 一次取出 2 条记录，可以调整此数据，一次处理的最大个数不要超过 cpu 核心数
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);
        if (mediaProcessList == null || mediaProcessList.isEmpty()) {
            log.debug("查询到待处理视频任务为0");
            return;
        }
        // 要处理的任务数
        int size = mediaProcessList.size();
        // 启动多线程去处理
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 遍历 mediaProcessList，将任务放入线程池
        for (MediaProcess mediaProcess : mediaProcessList) {
            threadPool.execute(() -> {
                // 创建文件
                File origin = null;
                // 处理结束的视频文件
                File mp4 = null;
                try {
                    // 任务的执行逻辑
                    String status = mediaProcess.getStatus();
                    // 保证幂等性
                    if ("2".equals(status)) {
                        log.debug("视频已经处理完成，视频信息：{}。", mediaProcess);
                        return;
                    }
                    // 桶
                    String bucket = mediaProcess.getBucket();
                    // 存储路径
                    String filepath = mediaProcess.getFilePath();
                    // 原始视频的 md5 值
                    String fileId = mediaProcess.getFileId();
                    // 原始文件名称
                    String filename = mediaProcess.getFilename();

                    try {
                        origin = File.createTempFile("origin", null);
                        mp4 = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        log.error("处理视频前创建临时文件失败", e);
                        return;
                    }
                    try {
                        // 将原始视频下载到本地
                        origin = mediaFileService.downloadFileFromMinio(origin, bucket, filepath);
                    } catch (Exception e) {
                        log.error("下载原始文件过程中出错：{}，文件信息：{}", e.getMessage(), mediaProcess);
                        return;
                    }
                    // 调用工具类将 avi 转换为 mp4
                    // 转换后的mp4文件的名称
                    String mp4Name = fileId + ".mp4";
                    // 转换后的mp4文件的路径
                    String mp4Path = mp4.getAbsolutePath();
                    // 创建工具类对象的路径
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, origin.getAbsolutePath(), mp4Name, mp4Path);
                    // 开始视频转换，成功将返回 success
                    String result = videoUtil.generateMp4();
                    // 处理状态
                    String newStatus = "3";
                    // 访问 url
                    String url = "";
                    // 出错信息
                    String errormsg = null;
                    // 转换成功
                    if ("success".equals(result)) {
                        // 上传到 minio 的路径
                        String objectName = getFilePath(fileId, ".mp4");
                        try {
                            // 上传到 minio
                            mediaFileService.addMediaFilesToMinio(mp4Path, bucket, objectName);
                        } catch (Exception e) {
                            log.error("上传文件出错", e);
                            return;
                        }
                        newStatus = "2"; // 处理成功
                        url = "/" + bucket + "/" + objectName; // 访问 url
                    } else {
                        log.error("generateMp4 error ,video_path is {},error msg is {}", bucket + filepath, result);
                    }

                    try {
                        // 记录任务处理结果
                        mediaFileProcessService.saveProcessFinishStatus(newStatus, fileId, url, result);
                    } catch (Exception e) {
                        log.error("保存任务处理结果出错", e);
                    }

                } finally {
                    // 清理文件
                    log.debug("清理临时文件");
                    try {
                        if (origin != null) {
                            origin.delete();
                        }
                        if (mp4 != null) {
                            mp4.delete();
                        }
                    } catch (Exception ignored) {
                    }
                    // 计数器减一
                    countDownLatch.countDown();
                }
            });
        }
        // 等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /**
     * 根据文件md5值获取文件绝对路径
     *
     * @param fileId  文件md5值
     * @param fileExt 文件扩展名
     * @return 文件绝对路径
     */
    private String getFilePath(String fileId, String fileExt) {
        return fileId.charAt(0) + "/" + fileId.charAt(1) + "/" + fileId + "/" + fileId + fileExt;
    }

}
