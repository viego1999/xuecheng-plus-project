package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Collections;

/**
 * Token 配置类
 *
 * @author Administrator
 * @version 1.0
 **/
@Configuration
public class TokenConfig {

    /**
     * 生成 JWT 的 Signature 的服务端密钥
     */
    private static final String SIGNING_KEY = "mq123";
    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;

    @Bean
    public TokenStore inMemoryTokenStore() {
        // 使用内存存储令牌（普通令牌）
        return new InMemoryTokenStore();
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(SIGNING_KEY);
        return converter;
    }

    /**
     * JWT
     */
    @Bean
    @Primary
    public TokenStore jwtTokenStore() {
        // Jwt 令牌
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean(name = "authorizationServerTokenServicesCustom")
    public AuthorizationServerTokenServices tokenService() {
        DefaultTokenServices service = new DefaultTokenServices();
        service.setSupportRefreshToken(true);//支持刷新令牌
        service.setTokenStore(jwtTokenStore());//令牌存储策略

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Collections.singletonList(accessTokenConverter));

        service.setTokenEnhancer(tokenEnhancerChain);
        service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期 2 小时
        service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期 3 天
        return service;
    }

}
