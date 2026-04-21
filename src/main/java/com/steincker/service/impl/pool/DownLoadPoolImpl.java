package com.steincker.service.impl.pool;

import com.steincker.service.pool.DownLoadPool;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName DownLoadPoolImpl
 * @Author ST000056
 * @Date 2023-12-08 15:38
 * @Version 1.0
 * @Description 下载线程池实现
 **/
@Service
public class DownLoadPoolImpl implements DownLoadPool {
    @Override
    public ExecutorService downLoadLogZip() {

         return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);

    }

    @Override
    public ExecutorService startExportLog() {
        return null;

    }
}
