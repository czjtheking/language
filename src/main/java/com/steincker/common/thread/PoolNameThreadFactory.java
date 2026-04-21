package com.steincker.common.thread;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;

/**
 * 池线程名称线程池工厂（在线程空闲停止后重新获取可用编号）
 * @author ST000050
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class PoolNameThreadFactory implements ThreadFactory {
    // ---------- member ----------

    /** 线程池名称 */
    private final String name;

    /** 线程池个数 */
    private final int size;

    // ---------- status ----------

    /** 线程池记录 【暂未主动清理】 */
    private final ConcurrentMap<Integer, Thread> threads = new ConcurrentHashMap<>();
    /** 线程号记录 */
    private volatile int index;

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        synchronized (this) {
            // 获取本次线程序号和名称
            int currentIndex = acquirePoolIndex();
            String currentName = name + "-" + currentIndex;
            Thread thread = new Thread(runnable, currentName);
            threads.put(currentIndex, thread);
            log.debug("{} thread {} added", name, currentIndex);
            return thread;
        }
    }

    /** 获取可用序号 【！在线程运行报错时会存在问题，此时当前线程还未结束，但已经在尝试申请新线程，导致申请失败，但是似乎不影响正常任务申请执行！】 */
    private int acquirePoolIndex() {
        clearPool();

        for (int i = index; i <= size; i++) {
            if (acquirePoolIndex(i)) {
                index = i < size ? i + 1 : 1;
                return i;
            }
        }

        for (int i = 1; i < index; i++) {
            if (acquirePoolIndex(i)) {
                index = i + 1;
                return i;
            }
        }

        // 线程池不讲道理，会超出池数申请，暂往后顺延
        int overSize = size + 1;
        while (!acquirePoolIndex(overSize)) {
            overSize++;
        }
        log.warn("{} thread pool run out: {} -> {}", name, size, overSize);
        return overSize;
    }

    /** 判断序号是否可用 */
    private boolean acquirePoolIndex(int i) {
        return !threads.containsKey(i);
    }

    /** 清理线程序号 */
    private void clearPool() {
        threads.forEach((i, thread) -> {
            if (!thread.isAlive()) {
                threads.remove(i);
                log.debug("{} thread {} removed", name, i);
            }
        });
    }
}
