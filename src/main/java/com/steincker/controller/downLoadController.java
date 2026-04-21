package com.steincker.controller;

import com.steincker.common.util.DownloadUtil;
import com.steincker.service.pool.DownLoadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping(value = "/yhx")
public class downLoadController {

    @Autowired
    private  DownLoadPool loadPool ;




    /**
    * 将map中每个key对应的文件（这里的key对应值默认为一个zip文件 即一个字符串） 分别下载出来 最后合并成一个zip通过浏览器下载出
    * */
    @ResponseBody
    @RequestMapping("/downlogsfileBatch")
    public void downPrintLodopFile(HttpServletResponse response, HttpServletRequest request) throws Exception{

        Map<String, String> snLogMap = new HashMap<>();


        String zip1 = "http://172.18.120.92:9210/policy/Privacy_policy/steinckerAPP_policy.pdf";

        String zip2 = "http://172.18.120.92:9210/policy/Privacy_policy/steinckerAPP_term.pdf";


        snLogMap.put("sn-1", zip1);
        snLogMap.put("sn-2", zip2);


        String workPath = System.getProperty("user.dir");

        //执行down
       down(workPath, snLogMap, request, response);
    }


    public void down(String path, Map<String, String> snLogMap, HttpServletRequest request, HttpServletResponse response)  {

        try {

            List<String> filePathList = new ArrayList<>();

            Set<String> snList = snLogMap.keySet();
            ExecutorService executorService = loadPool.downLoadLogZip();
            snList.forEach(sn -> {

                String files = snLogMap.get(sn);

                URL url = null;
                try {
                    url = new URL(files);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }

                //输出文件的名称
                String fileName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);

                //输出文件的路径
                String filePath = path + "/" + fileName;

                filePathList.add(filePath);

                executorService.execute(new DownloadThread(url, fileName));
            });


            executorService.shutdown();

            // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
            boolean allTaskCompleted = executorService.awaitTermination(2, TimeUnit.MINUTES);
            if (!allTaskCompleted) {
                System.out.println("下载失败");
            }


            //打包成一个zip 通过浏览器下载
            DownloadUtil.doGetZip(request,response,filePathList);

        } catch (Exception e) {

        }






    }

    private static class DownloadThread implements Runnable {
        private URL url;

        private String fileName;


        public DownloadThread(URL url,String fileName) {
            this.url = url;
            this.fileName = fileName;
        }

        @Override
        public void run() {

            try {

                BufferedInputStream in = new BufferedInputStream(url.openStream());

                //默认输出到当前工作目录
                FileOutputStream out = new FileOutputStream(fileName);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                in.close();
                out.close();


            } catch (Exception e) {

                System.out.println(e);
            }


        }
    }

















}