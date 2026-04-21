package com.steincker;

/**
 * @ClassName AsyncServiceTest
 * @Author ST000056
 * @Date 2024-04-09 14:48
 * @Version 1.0
 * @Description
 **/

import com.steincker.service.AsyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AsyncServiceTest {

    @Autowired
    private AsyncService asyncService;

    @Test
    public void testTestAsyncTask() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        System.out.println("开始执行异步任务...");
        asyncService.testAsyncTask();
        // 主线程不等待异步任务完成，立即继续执行
        System.out.println("主线程继续执行其他任务...");
        Thread.sleep(6000); // 等待一会儿以便观察异步任务是否完成
        long endTime = System.currentTimeMillis();
        System.out.println("测试结束，耗时：" + (endTime - startTime) + "ms");
    }
}