package com.xuecheng.ucenter.model.dto;

import com.xuecheng.ucenter.model.po.XcUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户扩展信息
 *
 * @author Wuxy
 * @version 1.0
 * @since 2022/9/30 13:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class XcUserExt extends XcUser {
    /**
     * 用户权限
     */
    List<String> permissions = new ArrayList<>();

}
