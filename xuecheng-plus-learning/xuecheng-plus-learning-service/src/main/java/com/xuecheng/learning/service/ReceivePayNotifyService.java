package com.xuecheng.learning.service;

import com.xuecheng.messagesdk.model.po.MqMessage;

/**
 * 接收支付结果消息处理接口类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName ReceivePayNotifyService
 * @since 2023/2/2 15:16
 */
public interface ReceivePayNotifyService {

    /**
     * 回复消息 <p>
     * 当学习中心服务处理完成后，向订单服务进行回复
     *
     * @param mqMessage 消息
     */
    void send(MqMessage mqMessage);

}
