package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 UserDetailsService 用来对接 Spring Security
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName UserServiceImpl
 * @since 2023/2/1 14:35
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private XcMenuMapper menuMapper;


    /**
     * 查询用户信息组成用户身份信息
     *
     * @param authParamsDtoJson 类型的 json 数据
     * @return {@link org.springframework.security.core.userdetails.UserDetails}
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String authParamsDtoJson) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto;
        try {
            // 将认证参数转为 AuthParamsDto 类型
            authParamsDto = JSON.parseObject(authParamsDtoJson, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", authParamsDtoJson);
            throw new RuntimeException("认证请求数据格式不对");
        }
        // 认证方式
        String authType = authParamsDto.getAuthType();
        // 从 spring 容器中拿到具体的认真实例
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        // 开始认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserDetails(xcUserExt);
    }

    /**
     * 根据 XcUserExt 对象构造一个 UserDetails 对象
     *
     * @param userExt userExt 对象-用户信息
     * @return {@link UserDetails}
     */
    public UserDetails getUserDetails(XcUserExt userExt) {
        // 查询用户权限
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId(userExt.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus == null || xcMenus.size() == 0) {
            permissions.add("p1");
        } else {
            xcMenus.forEach(menu -> permissions.add(menu.getCode()));
        }
        // 将用户权限放在 XcUserExt 中
        userExt.setPermissions(permissions);
        String[] authorities = permissions.toArray(new String[0]);

        String password = userExt.getPassword();
        // 为了安全在令牌中不存放密码
        userExt.setPassword(null);

        String userJson = JSON.toJSONString(userExt);
        return User.withUsername(userJson).password(password).authorities(authorities).build();
    }
}
