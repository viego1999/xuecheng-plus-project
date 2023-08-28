package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 视频处理服务实现类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName MediaFileProcessServiceImpl
 * @since 2023/1/25 16:09
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Resource
    private MediaFilesMapper mediaFilesMapper;
    @Resource
    private MediaProcessMapper mediaProcessMapper;
    @Resource
    private MediaProcessHistoryMapper historyMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectOne(new LambdaQueryWrapper<MediaProcess>()
                .eq(MediaProcess::getFileId, fileId));
        if (mediaProcess == null) {
            log.warn("更新任务状态时此任务不存在 {}", fileId);
            return;
        }
        // 处理失败
        if (Objects.equals(status, "3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }

        // status = 2，处理成功，更新 url 和状态
        mediaProcess.setStatus("2");
        mediaProcess.setUrl(url);
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        // 更新文件表中的 url 字段
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        // 添加到历史记录
        MediaProcessHistory history = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, history);
        historyMapper.insert(history);

        // 删除 mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }

}
