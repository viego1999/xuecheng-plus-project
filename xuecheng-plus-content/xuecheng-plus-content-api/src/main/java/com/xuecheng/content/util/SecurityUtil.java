package com.xuecheng.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 获取当前用户身份工具类
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName SecurityUtil
 * @since 2023/2/1 15:56
 */
@Slf4j
public class SecurityUtil {

    public static XcUser getUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String) {
                // 取出用户身份信息
                String s = principal.toString();
                // 转换为对象
                return JSON.parseObject(s, XcUser.class);
            }
        } catch (Exception e) {
            log.error("获取当前登录用户身份出错：{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用户实体类
     */
    @Data
    public static class XcUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;

        private String nickname;

        private String wxUnionid;

        private Long companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

    }
}
