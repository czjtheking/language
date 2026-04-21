package com.steincker.common.util;


import lombok.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName ThreadPoolUtil
 * @Author ST000056
 * @Date 2023-12-04 20:24
 * @Version 1.0
 * @Description
 **/

/**
 * 线程池的工具类
 * 用于进行线程的管理，防止重复创建、杀死线程。
 * <p>
 * 多线程运行期间，如果系统不断的创建、杀死新线程，
 * 会产生过度消耗系统资源，以及过度切换线程的问题，甚至可能导致系统资源的崩溃。
 * 因此需要线程池，对线程进行管理。
 */
public class ThreadPoolUtil {

    private String TAG = getClass().getName();
    private static volatile ThreadPoolUtil mInstance;
    //核心线程池的数量，同时能够执行的线程数量
    private int corePoolSize;
    //最大线程池数量，表示当缓冲队列满的时候能继续容纳的等待任务的数量
    private int maxPoolSize;
    //存活时间
    private long keepAliveTime = 1;
    private TimeUnit unit = TimeUnit.HOURS;
    private ThreadPoolExecutor executor;

    private String threadName;

    private ThreadPoolUtil(String name) {
        //给corePoolSize赋值：当前设备可用处理器核心数*2 + 1,能够让cpu的效率得到最大程度执行（有研究论证的）
        corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        threadName = ObjectUtils.isEmpty(name) ? "thread-pool-" : name;
        maxPoolSize = corePoolSize;
        executor = new ThreadPoolExecutor(
                //当某个核心任务执行完毕，会依次从缓冲队列中取出等待任务
                corePoolSize,
                // 然后new LinkedBlockingQueue<Runnable>(),然后maximumPoolSize,但是它的数量是包含了corePoolSize的
                maxPoolSize,
                //表示的是maximumPoolSize当中等待任务的存活时间
                keepAliveTime,
                unit,
                //缓冲队列，用于存放等待任务，Linked的先进先出
                new LinkedBlockingQueue<Runnable>(),
                new DefaultThreadFactory(Thread.NORM_PRIORITY, threadName),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public static ThreadPoolUtil getInstance(String name) {
        if (mInstance == null) {
            synchronized (ThreadPoolUtil.class) {
                if (mInstance == null) {
                    mInstance = new ThreadPoolUtil(name);
                }
            }
        }
        return mInstance;
    }

    /**
     * 执行任务
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        if (executor == null) {
            executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new DefaultThreadFactory(Thread.NORM_PRIORITY, threadName),
                    new ThreadPoolExecutor.AbortPolicy());
        }
        if (runnable != null) {
            executor.execute(runnable);
        }
    }

    /**
     * 执行任务
     *
     * @param runnable
     */
    public Future<?> submit(Runnable runnable) {
        if (executor == null) {
            executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new DefaultThreadFactory(Thread.NORM_PRIORITY, threadName),
                    new ThreadPoolExecutor.AbortPolicy());
        }
        if (runnable != null) {
            return  executor.submit(runnable);
        }

        return null;
    }

    /**
     * 移除任务
     *
     * @param runnable
     */
    public void remove(Runnable runnable) {
        if (runnable != null) {
            executor.remove(runnable);
        }
    }

    /**
     * 关闭线程池
     *
     */
    public void shutdown() {

        executor.shutdown();

    }

    /**
     * 等待线程池中所有任务执行完成
     *
     * @param timeout 等待的最大时间长度
     * @param unit    时间单位
     * @return 如果在指定时间内所有任务都执行完成，则返回 true；否则返回 false
     * @throws InterruptedException 如果等待过程中发生中断
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (executor != null) {
            return executor.awaitTermination(timeout, unit);
        }
        return true; // 如果线程池为null，返回true，表示任务已经完成
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        //线程池的计数
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        //线程的计数
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;
        private final int threadPriority;

        DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
            this.threadPriority = threadPriority;
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            // 返回True该线程就是守护线程
            // 守护线程应该永远不去访问固有资源，如：数据库、文件等。因为它会在任何时候甚至在一个操作的中间发生中断。
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            thread.setPriority(threadPriority);
            return thread;
        }
    }
}