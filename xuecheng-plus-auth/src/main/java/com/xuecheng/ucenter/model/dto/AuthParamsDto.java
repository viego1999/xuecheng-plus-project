package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证用户请求参数
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/29 10:56
 */
@Data
public class AuthParamsDto {
    /**
     * 用户名
     */
    private String username;

    /**
     * 域  用于扩展
     */
    private String password;

    /**
     * 手机号
     */
    private String cellphone;

    /**
     * 验证码
     */
    private String checkcode;

    /**
     * 验证码 key
     */
    private String checkcodekey;

    /**
     * 认证的类型   password:用户名密码模式类型    sms:短信模式类型
     */
    private String authType;

    /**
     * 附加数据，作为扩展，不同认证类型可拥有不同的附加数据。<p>
     * 如认证类型为短信时包含smsKey : sms:3d21042d054548b08477142bbca95cfa; 所有情况下都包含clientId
     */
    private Map<String, Object> payload = new HashMap<>();

}
