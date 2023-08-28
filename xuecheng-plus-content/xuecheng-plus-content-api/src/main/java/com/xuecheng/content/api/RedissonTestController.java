package com.xuecheng.content.api;

import org.redisson.api.RLock;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

/**
 * Redisson 测试接口
 *
 * @author Wuxy
 * @version 1.0
 * @ClassName RedissonTestController
 * @since 2023/2/4 19:17
 */
@RestController
@RequestMapping("/redisson")
public class RedissonTestController {

    @Autowired
    private RedissonClient redissonClient;


    /**
     * 入队
     */
    @GetMapping("/joinqueue")
    public Queue<String> joinQueue(String queue) {
        RQueue<String> rq = redissonClient.getQueue("queue001");
        rq.add(queue);
        return rq;
    }

    /**
     * 出队
     */
    @GetMapping("/removequeue")
    public String removeQueue() {
        RQueue<String> rq = redissonClient.getQueue("queue001");
        return rq.poll();
    }

    /**
     * 获取分布式锁
     */
    @GetMapping("/getlock")
    public void getLock() {
        RLock lock = redissonClient.getLock("lock001");
        // 获取lock001锁
        lock.lock();
        try {
            System.out.println("拿到锁了");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            // 释放锁
            System.out.println("释放锁");
            lock.unlock();
        }
    }
}
