package com.xuecheng.checkcode.model;

import lombok.Data;

/**
 * 验证码生成结果类
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/29 15:48
 */
@Data
public class CheckCodeResultDto {

    /**
     * key用于验证
     */
    private String key;

    /**
     * 混淆后的内容
     * 举例：
     * <pre>
     * 1.图片验证码为:图片base64编码
     * 2.短信验证码为:null
     * 3.邮件验证码为: null
     * 4.邮件链接点击验证为：null
     * ...
     * </pre>
     */
    private String aliasing;
}
