package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

/**
 * 微信登录
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName WxLoginController
 * @since 2023/2/1 19:19
 */
@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    private WxAuthServiceImpl wxAuthService;

    @PostMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调，code：{}，state：{}", code, state);
        XcUser user = wxAuthService.wxAuth(code);
        if (user == null) {
            return "redirect:http://www.xuecheng-plus.com/error.html";
        }
        String username = user.getUsername();
        return "redirect:http://www.xuecheng-plus.com/sign.html?username=" + username + "&authType=wx";
    }
}
