package com.shutu.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 * 你可以使用 Redisson, Jedis,或者 Spring Integration 的 RedisLockRegistry 来实现它
 */
public interface DistributedLock {

    /**
     * 尝试获取锁
     * @param key 锁的key
     * @param timeout 获取锁的等待时间
     * @param unit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(String key, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁
     * @param key 锁的key
     */
    void unlock(String key);
}