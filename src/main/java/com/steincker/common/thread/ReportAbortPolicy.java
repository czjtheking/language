package com.steincker.common.thread;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 带状态信息的拒绝策略
 * @author ST000050
 */
@Getter
@AllArgsConstructor
public class ReportAbortPolicy extends ThreadPoolExecutor.AbortPolicy {
    /** 线程池名称 */
    private final String name;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Task reject! name: %s, size: %d (active: %d, core: %d, max: %d, largest: %d)," +
                        " task: %d (completed: %d), status: (isShutdown: %s, isTerminated: %s, isTerminating: %s)",
                name, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
        throw new RejectedExecutionException(msg);
    }
}
