package com.steincker.controller;


import com.steincker.common.util.DownLoadThreadPoolUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(value = "/demo")
public class DemoController {

    private static DownLoadThreadPoolUtil downLoadPool = DownLoadThreadPoolUtil.getInstance();

    @GetMapping("/test")
    @ResponseBody
    public String loop() {
       try {
           Date.parse("111");
       }catch (Exception e){
       }
       Random random = new Random();        // Noncompliant - new instance created with each invocation
       int rValue = random.nextInt();
      return "你好啊，这个是个演示的web项目！";
    }


    @ResponseBody
    @RequestMapping("downlogsfile")
    public void downlogsfile(HttpServletResponse response, String filename) throws IOException {
        //String yh = "D:/app.log.2023-11-27.0.zip";
        String yh = "D:/ST000056/Pictures/Camera Roll/test.png";

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=GBK");
        File file = new File(yh);

        if (file.exists()) {
            downloadtxt(response, file);
        }
    }

    public static void downloadtxt(HttpServletResponse res, File file) throws IOException {
        long length = file.length();
        res.addHeader("Content-Length", String.valueOf(length));
        res.addHeader("Content-Type", "text/plain; charset=utf-8");
        res.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
        OutputStream outputStream = res.getOutputStream();
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        FileInputStream fileInputStream = new FileInputStream(file);
        bis = new BufferedInputStream(fileInputStream);
        int i = bis.read(buff);
        while (i != -1) {
            outputStream.write(buff, 0, buff.length);
            outputStream.flush();
            i = bis.read(buff);
        }
        bis.close();
        fileInputStream.close();
        outputStream.close();
    }



    /**
    * web批量下载文件
    * */
    private static final int BUFFER_SIZE = 4096;
    private static final int NUM_THREADS = 4;

    @ResponseBody
    @RequestMapping("downlogsfileBatch")
    public void downlogsfileBatch(HttpServletResponse response, String filename) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=GBK");

        List<String> urls = new ArrayList<>();
        urls.add("file:///D:/app.log.2023-11-27.0.zip");
        urls.add("file:///D:/ST000056/Pictures/Camera Roll/test.png");

        int numFiles = urls.size();
        int numThreads = Math.min(numFiles, NUM_THREADS);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            int start = i * numFiles / numThreads;
            int end = (i + 1) * numFiles / numThreads;
            List<String> subUrls = urls.subList(start, end);
            Thread thread = new DownloadThread(subUrls);
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }




    }

    private static class DownloadThread extends Thread {
        private List<String> urls;


        public DownloadThread(List<String> urls) {
            this.urls = urls;
        }

        @Override
        public void run() {
            for (String url : urls) {
                try {
                    downloadFile(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        String fileName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);

        BufferedInputStream in = new BufferedInputStream(url.openStream());

        FileOutputStream out = new FileOutputStream(fileName);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        in.close();
        out.close();
    }



    @ResponseBody
    @RequestMapping("downlogsfileBatch2")
    public void downlogsfileBatch2(HttpServletResponse response, String filename) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=GBK");

        List<String> urls = new ArrayList<>();
        urls.add("file:///D:/app.log.2023-11-27.0.zip");
        urls.add("file:///D:/ST000056/Pictures/Camera Roll/test.png");


        DownloadThread2 yh = new DownloadThread2(urls);
        downLoadPool.execute(yh);

        //获取当前工作目录
        String workPath = System.getProperty("user.dir");



        }



    private static class DownloadThread2 implements Runnable {
        private List<String> urls;

        public DownloadThread2(List<String> urls) {
            this.urls = urls;
        }

        @Override
        public void run() {
            for (String url : urls) {
                try {
                    downloadFile(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }





}




