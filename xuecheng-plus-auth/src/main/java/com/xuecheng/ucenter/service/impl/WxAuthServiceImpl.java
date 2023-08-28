package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 微信扫码认证
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName WxAuthServiceImpl
 * @since 2023/2/1 19:21
 */
@Slf4j
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService {
    @Resource
    private XcUserMapper userMapper;

    @Resource
    private XcUserRoleMapper userRoleMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WxAuthServiceImpl currentProxy;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;


    /**
     * 微信扫码认证，不校验验证码，不校验密码
     *
     * @param authParamsDto 认证参数
     * @return {@link XcUserExt}
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 账号
        String username = authParamsDto.getUsername();
        XcUser user = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }

    public XcUser wxAuth(String code) {
        // 根据授权码获取 access_token
        Map<String, String> accessTokenMap = getAccessToken(code);
        if (accessTokenMap == null) {
            return null;
        }
        // 根据令牌获取用户信息
        String openid = accessTokenMap.get("openid");
        String accessToken = accessTokenMap.get("access_token");
        // 拿 access_token 查询用户信息
        Map<String, String> userInfo = getUserInfo(accessToken, openid);
        if (userInfo == null) {
            return null;
        }
        // 添加用户到数据库并返回
        return currentProxy.addWxUser(userInfo);
    }

    /**
     * 申请访问令牌,响应示例
     * <pre>
     * {
     *  "access_token":"ACCESS_TOKEN",
     *  "expires_in":7200,
     *  "refresh_token":"REFRESH_TOKEN",
     *  "openid":"OPENID",
     *  "scope":"SCOPE",
     *  "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getAccessToken(String code) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 请求微信地址
        String wxUrl = String.format(wxUrl_template, appid, secret, code);
        log.info("调用微信接口申请 access_token, url:{}", wxUrl);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        log.info("调用微信接口申请 access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    /**
     * 获取用户信息，示例如下：
     * <pre>
     * {
     *    "openid": "OPENID",
     *    "nickname": "NICKNAME",
     *    "sex": 1,
     *    "province": "PROVINCE",
     *    "city": "CITY",
     *    "country": "COUNTRY",
     *    "headimgurl": "<a href="https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0">headimgurl</a>",
     *    "privilege": [
     *        "PRIVILEGE1",
     *        "PRIVILEGE2"
     *    ],
     *    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * </pre>
     */
    private Map<String, String> getUserInfo(String accessToken, String openid) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        // 请求微信地址
        String wxUrl = String.format(wxUrl_template, accessToken, openid);
        log.info("调用微信接口申请 access_token, url:{}", wxUrl);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        log.info("调用微信接口申请 access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    @Transactional
    public XcUser addWxUser(Map<String, String> userInfo) {
        String unionid = userInfo.get("unionid");
        // 根据 unionid 查询数据库
        XcUser user = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (user != null) {
            return user;
        }
        String userId = UUID.randomUUID().toString();
        user = new XcUser();
        user.setId(userId);
        user.setWxUnionid(unionid);
        // 记录从微信得到的昵称
        user.setNickname(userInfo.get("nickname"));
        user.setUserpic(userInfo.get("headimgurl"));
        user.setName(userInfo.get("nickname"));
        user.setUsername(unionid);
        user.setPassword(unionid);
        user.setUtype("101001"); // 学生类型
        user.setStatus("1"); // 用户状态
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);

        XcUserRole userRole = new XcUserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        userRole.setRoleId("17"); // 学生角色

        userRoleMapper.insert(userRole);

        return user;
    }
}
