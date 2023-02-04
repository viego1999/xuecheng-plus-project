package com.xuecheng.orders.model.dto;

import lombok.Data;

/**
 * 支付结果数据, 用于接收支付结果通知处理逻辑
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/10/4 16:49
 */
@Data
public class PayStatusDto {

    /**
     * 商户订单号
     */
    String out_trade_no;

    /**
     * 支付宝交易号
     */
    String trade_no;

    /**
     * 交易状态
     */
    String trade_status;

    /**
     * 支付宝的 appid
     */
    String app_id;

    /**
     * 总金额
     */
    String total_amount;

}
