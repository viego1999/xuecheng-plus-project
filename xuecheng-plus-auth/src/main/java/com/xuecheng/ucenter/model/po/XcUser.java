package com.xuecheng.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("xc_user")
public class XcUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 用户名
     */
    private String username;

    private String password;

    private String salt;

    private String name;

    /**
     * 昵称
     */
    private String nickname;

    private String wxUnionid;

    private String companyId;
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
