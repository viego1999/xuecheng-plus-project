package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 课程发布任务
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName CoursePublishTask
 * @since 2023/1/30 22:47
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    /**
     * 课程发布消息类型
     */
    public static final String MESSAGE_TYPE = "course_publish";

    @Autowired
    private CoursePublishService coursePublishService;


    /**
     * 任务调度入口
     */
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex = {}, shardTotal = {}.", shardIndex, shardTotal);
        // 参数：分片序号，分片总数，消息类型，一次最多取到的任务数，一次任务调度执行的超时时间
        process(shardIndex, shardTotal, MESSAGE_TYPE, 5, 60);
    }

    // 课程发布的执行逻辑
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 获取相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.parseLong(businessKey1);
        // 课程静态化，将静态页面上传到minio
        generateCourseHtml(mqMessage, courseId);

        // 将课程信息缓存到索引库 es
        saveCourseIndex(mqMessage, courseId);

        // 课程信息缓存到 redis
        saveCourseCache(mqMessage, courseId);

        return true;
    }

    /**
     * 生成课程静态化页面并上传至文件系统
     *
     * @param mqMessage 消息内容
     * @param courseId  课程id
     */
    public void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始进行课程静态化，课程id：{}", courseId);
        // 消息id
        Long id = mqMessage.getId();
        // 消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne == 1) {
            log.debug("课程静态化已经处理，课程id：{}，任务信息：{}", courseId, mqMessage);
            return;
        }

        // 生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("课程静态化异常");
        }
        // 上传静态化页面
        coursePublishService.uploadCourseHtml(courseId, file);
        // 保存第一阶段的状态
        mqMessageService.completedStageOne(id);
    }

    /**
     * 保存课程索引信息 es
     *
     * @param mqMessage 消息内容
     * @param courseId  课程id
     */
    public void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        log.debug("将课程索引信息存入 es，课程id：{}", courseId);
        // 消息id
        Long id = mqMessage.getId();
        // 消息处理的 service
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo == 2) {
            log.warn("课程索引已处理，直接返回，课程id：{}", courseId);
            return;
        }
        // 保存课程索引
        Boolean result = coursePublishService.saveCourseIndex(courseId);
        if (result) {
            // 保存第二阶段状态
            mqMessageService.completedStageTwo(id);
        }
    }

    /**
     * 将课程信息缓存至Redis
     *
     * @param mqMessage 消息内容
     * @param courseId  课程id
     */
    public void saveCourseCache(MqMessage mqMessage, Long courseId) {
        log.debug("将课程信息缓存至 redis，课程id：{}", courseId);
        try {
            MqMessageService mqMessageService = this.getMqMessageService();
            int stageThree = mqMessageService.getStageThree(courseId);
            if (stageThree == 3) {
                log.warn("课程信息已经存入Redis缓存，直接返回，课程id：{}", courseId);
                return;
            }
            TimeUnit.SECONDS.sleep(2);
            int count = mqMessageService.completedStageThree(courseId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
