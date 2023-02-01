package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * 认证 service
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName AuthService
 * @since 2023/2/1 16:25
 */
public interface AuthService {

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     * @return {@link com.xuecheng.ucenter.model.dto.XcUserExt} 用户信息
     * @author Wuxy
     * @since 2022/9/29 12:11
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
