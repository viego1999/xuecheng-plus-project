package com.xuecheng.checkcode.model;

import lombok.Data;

/**
 * 验证码生成参数类
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/29 15:48
 */
@Data
public class CheckCodeParamsDto {

    /**
     * 验证码类型:pic、sms、email等
     */
    private String checkCodeType;

    /**
     * 业务携带参数
     */
    private String param1;
    private String param2;
    private String param3;
}
