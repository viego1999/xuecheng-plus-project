package com.xuecheng.messagesdk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.messagesdk.model.po.MqMessage;

import java.util.List;

/**
 * <p>
 * 消息服务类
 * </p>
 *
 * @author Wuxy
 * @since 2022-09-21
 */
public interface MqMessageService extends IService<MqMessage> {

    /**
     * 扫描消息表记录，采用与扫描视频处理表相同的思路
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      扫描记录数
     * @return {@link java.util.List}<{@link MqMessage}> 消息记录
     * @author Wuxy
     * @since 2022/9/21 18:55
     */
    List<MqMessage> getMessageList(int shardIndex, int shardTotal, String messageType, int count);

    /**
     * 添加消息
     *
     * @param messageType  消息类型（例如：支付结果、课程发布等）
     * @param businessKey1 业务id
     * @param businessKey2 业务id
     * @param businessKey3 业务id
     * @return {@link com.xuecheng.messagesdk.model.po.MqMessage} 消息内容
     * @author Wuxy
     * @since 2022/9/23 13:45
     */
    MqMessage addMessage(String messageType, String businessKey1, String businessKey2, String businessKey3);

    /**
     * 完成任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     * @author Wuxy
     * @since 2022/9/21 20:49
     */
    int completed(long id);

    /**
     * 完成阶段一任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     * @author Wuxy
     * @since 2022/9/21 20:49
     */
    int completedStageOne(long id);

    /**
     * 完成阶段二任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     * @author Wuxy
     * @since 2022/9/21 20:49
     */
    int completedStageTwo(long id);

    /**
     * 完成阶段三任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     * @author Wuxy
     * @since 2022/9/21 20:49
     */
    int completedStageThree(long id);

    /**
     * 完成阶段四任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     * @author Wuxy
     * @since 2022/9/21 20:49
     */
    int completedStageFour(long id);

    /**
     * 查询阶段一状态
     *
     * @param id 消息id
     * @return int
     * @author Wuxy
     * @since 2022/9/21 20:54
     */
    public int getStageOne(long id);

    /**
     * 查询阶段二状态
     *
     * @param id 消息id
     * @return int
     * @author Wuxy
     * @since 2022/9/21 20:54
     */
    int getStageTwo(long id);

    /**
     * 查询阶段三状态
     *
     * @param id 消息id
     * @return int
     * @author Wuxy
     * @since 2022/9/21 20:54
     */
    int getStageThree(long id);

    /**
     * 查询阶段四状态
     *
     * @param id 消息id
     * @return int
     * @author Wuxy
     * @since 2022/9/21 20:54
     */
    int getStageFour(long id);

}
