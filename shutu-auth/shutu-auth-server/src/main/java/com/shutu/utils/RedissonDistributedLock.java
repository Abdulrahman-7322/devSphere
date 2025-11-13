package com.shutu.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式锁实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedissonDistributedLock implements DistributedLock {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取锁，并设置锁的自动释放时间（防止死锁）
     *
     * @param key       锁的key
     * @param waitTime  获取锁的最大等待时间
     * @param unit      时间单位
     * @return 是否成功获取锁
     */
    @Override
    public boolean tryLock(String key, long waitTime, TimeUnit unit) throws InterruptedException {
        // 从 Redisson 客户端获取一个锁对象
        RLock lock = redissonClient.getLock(key);
        
        // Redisson 的 tryLock 方法提供了 leaseTime 参数，这是防止死锁的关键。
        // leaseTime 指的是锁的有效时间，超过这个时间锁会自动释放。
        // 这样即使服务在持有锁期间宕机，也不会导致锁永远无法被其他线程获取。
        // 这里我们将 leaseTime 设置为比业务执行时间稍长，例如 30 秒。
        long leaseTime = 30; 
        
        log.debug("尝试获取分布式锁, key: {}, 等待时间: {}s, 锁自动释放时间: {}s", key, waitTime, leaseTime);
        
        // 尝试加锁，最多等待 waitTime，上锁以后 leaseTime 秒自动解锁
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    /**
     * 释放锁
     *
     * @param key 锁的key
     */
    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        
        // 检查锁是否存在并且是由当前线程持有，这是安全的释放锁操作
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("成功释放分布式锁, key: {}", key);
        } else {
            // 如果锁不存在或不被当前线程持有，打印警告，避免释放别人的锁
            log.warn("尝试释放一个不被当前线程持有的锁, key: {}", key);
        }
    }
}