package com.xuecheng.checkcode.service;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;

/**
 * 验证码接口
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/29 15:59
 */
public interface CheckCodeService {


    /**
     * 生成验证码
     *
     * @param checkCodeParamsDto 生成验证码参数
     * @return {@link com.xuecheng.checkcode.model.CheckCodeResultDto} 验证码结果
     * @author Mr.M
     * @since 2022/9/29 18:21
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * 校验验证码
     *
     * @param key  密钥
     * @param code 验证码
     * @return boolean
     * @author Wuxy
     * @since 2022/9/29 18:46
     */
    boolean verify(String key, String code);


    /**
     * 验证码生成器
     *
     * @author Wuxy
     * @since 2022/9/29 16:34
     */
    interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);


    }

    /**
     * key生成器
     *
     * @author Wuxy
     * @since 2022/9/29 16:34
     */
    interface KeyGenerator {

        /**
         * key生成
         *
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * 验证码存储
     *
     * @author Wuxy
     * @since 2022/9/29 16:34
     */
    interface CheckCodeStore {

        /**
         * 向缓存设置key
         *
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         * @author Mr.W
         * @since 2022/9/29 17:15
         */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
