package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * 订单服务接口
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/10/25 11:41
 */
public interface OrderService {

    /**
     * 创建商品订单，添加支付记录
     *
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @author Mr.M
     * @since 2022/10/4 11:02
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付交易记录
     *
     * @param payNo 交易记录号
     * @return {@link com.xuecheng.orders.model.po.XcPayRecord}
     * @author Wuxy
     * @since 2022/10/20 23:38
     */
    XcPayRecord getPayRecordByPayNo(String payNo);

    /**
     * 保存支付宝支付结果（更新【支付记录】以及【订单】状态）
     *
     * @param payStatusDto 支付结果信息
     * @author Wuxy
     * @since 2022/10/4 16:52
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

}
