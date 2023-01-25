package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * 媒资文件处理业务方法
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName MediaFileProcessService
 * @since 2023/1/25 15:58
 */
public interface MediaFileProcessService {

    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取数量
     * @return {@link java.util.List}<{@link com.xuecheng.media.model.po.MediaProcess}>
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     * 将 url 存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
     *
     * @param status   处理结果，2:成功 3 失败
     * @param fileId   文件 id
     * @param url      文件访问 url
     * @param errorMsg 失败原因
     * @author Mr.M
     * @since 2022/9/14 14:45
     */
    void saveProcessFinishStatus(String status, String fileId, String url, String errorMsg);

}
