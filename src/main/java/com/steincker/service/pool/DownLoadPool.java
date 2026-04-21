package com.steincker.service.pool;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName DownLoadPool
 * @Author ST000056
 * @Date 2023-12-08 15:36
 * @Version 1.0
 * @Description 下载线程池
 **/
@Service
public interface DownLoadPool {


    /**
    * 下载文件后压缩
    * */
    ExecutorService downLoadLogZip ();

    /**
    * 导出任务
    * */
    ExecutorService startExportLog();
}
