package com.steincker.common.thread;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ThreadFactory;

/**
 * 单线程名称线程池工厂（在线程空闲停止后重新使用相同编号）
 * @author ST000050
 */
@Getter
@AllArgsConstructor
public class SingleNameThreadFactory implements ThreadFactory {
    /** 线程池名称 */
    private final String name;

    @Override
    public Thread newThread(Runnable runnable) {
        // 始终使用相同的名字创建线程
        return new Thread(runnable, name + "-s");
    }
}
