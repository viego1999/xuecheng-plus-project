package com.xuecheng;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Wuxy
 * @version 1.0
 * @ClassName TestBCryptEncoder
 * @since 2023/2/1 14:58
 */
public class TestBCryptEncoder {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123";
        for (int i = 0; i < 10; i++) {
            String hashPass = encoder.encode(password);
            System.out.println("hashPass: " + hashPass);

            System.out.println(encoder.matches(password, hashPass));
        }
    }

}
