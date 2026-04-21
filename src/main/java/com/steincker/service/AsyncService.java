package com.steincker.service;

/**
 * @ClassName AsyncService
 * @Author ST000056
 * @Date 2024-04-09 14:45
 * @Version 1.0
 * @Description
 **/

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

    @Async("taskExecutor")
    public void testAsyncTask() {
        try {
            // 模拟一个长时间运行的任务
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("异步任务 testAsyncTask 完成");
    }
}
