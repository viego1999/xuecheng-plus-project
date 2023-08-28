package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.CheckCodeService;

import java.util.Random;

/**
 * 数字生成器
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName NumberCheckCodeGenerator
 * @since 2023/2/1 21:53
 */
public class NumberCheckCodeGenerator implements CheckCodeService.CheckCodeGenerator {
    static final Random random = new Random();
    static final String str = "0123456789";

    @Override
    public String generate(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(random.nextInt(10)));
        }
        return sb.toString();
    }
}
