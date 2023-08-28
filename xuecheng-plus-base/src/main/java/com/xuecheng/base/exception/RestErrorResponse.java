package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 错误响应参数包装
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName RestErrorResponse
 * @since 2023/1/19 19:06
 */
@Data
@AllArgsConstructor
public class RestErrorResponse {

    /**
     * 异常信息
     */
    private String errMessage;

}
