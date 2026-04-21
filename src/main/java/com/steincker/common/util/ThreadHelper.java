package com.steincker.common.util;


import com.steincker.common.thread.PoolNameThreadFactory;
import com.steincker.common.thread.ReportAbortPolicy;
import com.steincker.common.thread.SingleNameThreadFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 线程池辅助
 * @author ST000050
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadHelper {
    // ---------- const ----------

    /** 默认核数 */
    public static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    /** 默认 IO rate 【暂直接 * 2，具体自定义 IO 线程池时根据业务阻塞系数确定：CPU核数 / （1 - 阻塞系数）】 */
    public static final int CPU_RATE_IO = 2;

    /** 默认队列数：10w 【需要根据业务实际情况调整！确保高峰期任务提交不丢失，实在处理不过来内存不溢出】 */
    public static final int QUEUE_SIZE = 10 * 100 * 100;

    /** 默认空闲时间：30 秒 */
    public static final long KEEP_ALIVE_SECONDS = 30L;

    /** 通用任务 */
    public static final String THREAD_NAME = "default";

    /** 通用 IO 任务 */
    public static final String THREAD_NAME_IO = "default-io";

    /** 通用定时任务 */
    public static final String SCHEDULE_NAME = "schedule";

    // ---------- instance ----------
    /** 任务线程池 */
    private static final ConcurrentMap<String, ThreadPoolExecutor> EXECUTORS = new ConcurrentHashMap<>();
    /** 定时线程池 */
    private static final ConcurrentMap<String, ScheduledThreadPoolExecutor> SCHEDULERS = new ConcurrentHashMap<>();

    /** 执行通用任务【*仅 CPU 运算，确保不包含 IO！*】 */
    public static void execute(Runnable runnable) {
        ExecutorHolder.INSTANCE.execute(runnable);
    }

    // ---------- pool ----------

    /** 执行通用 IO 任务 */
    public static void executeIo(Runnable runnable) {
        IoExecutorHolder.INSTANCE.execute(runnable);
    }

    /** 执行通用延时任务【*确保不包含具体耗时执行！*】 */
    @SuppressWarnings("unchecked")
    public static <T> ScheduledFuture<T> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) SchedulerHolder.INSTANCE.schedule(command, delay, unit);
    }

    // ---------- default execute ----------

    /** 执行通用延时任务【*确保不包含具体耗时执行！*】 */
    public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        return SchedulerHolder.INSTANCE.schedule(callable, delay, unit);
    }

    /** 执行通用定时任务【*确保不包含具体耗时执行！*】 */
    @SuppressWarnings("unchecked")
    public static <T> ScheduledFuture<T> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) SchedulerHolder.INSTANCE.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    // ---------- default schedule ----------

    /** 执行通用定时任务【*确保不包含具体耗时执行！*】 */
    @SuppressWarnings("unchecked")
    public static <T> ScheduledFuture<T> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ScheduledFuture<T>) SchedulerHolder.INSTANCE.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /** 获取通用任务线程池 */
    public static ThreadPoolExecutor executor() {
        return ExecutorHolder.INSTANCE;
    }

    /** 获取通用 IO 任务线程池 */
    public static ThreadPoolExecutor ioExecutor() {
        return IoExecutorHolder.INSTANCE;
    }

    /** 获取通用定时任务线程池 */
    public static ScheduledThreadPoolExecutor scheduler() {
        return SchedulerHolder.INSTANCE;
    }

    // ---------- default executor / scheduler ----------

    /** 创建 ThreadPoolExecutor 【核数线程】 */
    public static ThreadPoolExecutor executor(String name) {
        return executorBySize(name, CORE_SIZE);
    }

    /** 创建 ThreadPoolExecutor 【核数线程 * 倍率】 */
    public static ThreadPoolExecutor executorByRate(String name, int cpuRate) {
        return executorBySize(name, CORE_SIZE * cpuRate);
    }

    /** 创建 ThreadPoolExecutor 【自定义线程】 */
    public static ThreadPoolExecutor executorBySize(String name, int coreSize) {
        return executorBySize(name, coreSize, QUEUE_SIZE, KEEP_ALIVE_SECONDS);
    }

    // ---------- executor ----------

    /** 创建 ThreadPoolExecutor 【核数线程 * 倍率】 */
    public static ThreadPoolExecutor executorByRate(String name, int cpuRate, int queueSize, long keepAliveSeconds) {
        return executorBySize(name, CORE_SIZE * cpuRate, queueSize, keepAliveSeconds);
    }

    /** 创建 ThreadPoolExecutor 【自定义线程】 */
    public static ThreadPoolExecutor executorBySize(String name, int coreSize, int queueSize, long keepAliveSeconds) {
        return MapUtil.computeIfAbsent(EXECUTORS, name, () -> buildExecutor(name, coreSize, queueSize, keepAliveSeconds));
    }

    /** 创建 ThreadPoolExecutor 【单线程】 */
    public static ThreadPoolExecutor singleExecutor(String name) {
        return executorBySize(name, 1);
    }

    /** 创建 ScheduledThreadPoolExecutor 【核数线程】 */
    public static ScheduledThreadPoolExecutor scheduler(String name) {
        return schedulerBySize(name, CORE_SIZE);
    }

    /** 创建 ScheduledThreadPoolExecutor 【核数线程 * 倍率】 */
    public static ScheduledThreadPoolExecutor schedulerByRate(String name, int cpuRate) {
        return schedulerBySize(name, CORE_SIZE * cpuRate);
    }

    /** 创建 ScheduledThreadPoolExecutor 【自定义线程】 */
    public static ScheduledThreadPoolExecutor schedulerBySize(String name, int coreSize) {
        return MapUtil.computeIfAbsent(SCHEDULERS, name, () -> buildScheduler(name, coreSize));
    }

    // ---------- scheduler ----------

    /** 创建 ScheduledThreadPoolExecutor 【单线程】 */
    public static ScheduledThreadPoolExecutor singleScheduler(String name) {
        return schedulerBySize(name, 1);
    }

    /** 创建 ThreadPoolExecutor */
    private static ThreadPoolExecutor buildExecutor(String name, int coreSize, int queueSize, long keepAliveSeconds) {
        // 初始化线程池 【默认 core 相等，允许核心线程 timeout】
        log.info("[thread] init pool: {}, size: {}, timeout: {} s", name, coreSize, keepAliveSeconds);
        ThreadPoolExecutor exe = new ThreadPoolExecutor(coreSize, coreSize,
                keepAliveSeconds > 0 ? keepAliveSeconds : 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                coreSize > 1 ? new PoolNameThreadFactory(name, coreSize) : new SingleNameThreadFactory(name),
                new ReportAbortPolicy(name));

        // 核心线程空闲超时回收 【数据通管机频繁创建回收系统是否会出现问题？】
        if (keepAliveSeconds > 0) {
            exe.allowCoreThreadTimeOut(true);
        }
        return exe;
    }

    /** 创建 ScheduledThreadPoolExecutor */
    private static ScheduledThreadPoolExecutor buildScheduler(String name, int coreSize) {
        // 创建 ScheduledThreadPoolExecutor 【是否会线程或队列内存溢出？】
        return new ScheduledThreadPoolExecutor(coreSize,
                coreSize > 1 ? new PoolNameThreadFactory(name, coreSize) : new SingleNameThreadFactory(name),
                new ReportAbortPolicy(name));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ExecutorHolder {
        /** 通用任务线程池 【核数线程 + 1】 */
        private static final ThreadPoolExecutor INSTANCE = buildExecutor(THREAD_NAME, CORE_SIZE + 1, QUEUE_SIZE, KEEP_ALIVE_SECONDS);
    }

    // ---------- build ----------

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class IoExecutorHolder {
        /** 通用 IO 任务线程池 【核数线程 * 倍率】 */
        private static final ThreadPoolExecutor INSTANCE = buildExecutor(THREAD_NAME_IO, CORE_SIZE * CPU_RATE_IO, QUEUE_SIZE, KEEP_ALIVE_SECONDS);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SchedulerHolder {
        /** 通用定时任务线程池 【核数线程】 */
        private static final ScheduledThreadPoolExecutor INSTANCE = buildScheduler(SCHEDULE_NAME, CORE_SIZE);
    }
}
