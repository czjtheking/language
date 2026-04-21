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
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@RequestMapping(value = "/yh")
public class yhDemoController {

    @Autowired
    private  DownLoadPool loadPool ;

    /**
    * 将map中每个key对应的文件列表（这里的key对应的值可能不止一个文件 所以用字符串数组装起来 ）分别打包成一个zip 再将每一个zip合并成一个新的zip，通过浏览器下载下来
    * */
    @ResponseBody
    @RequestMapping("/downlogsfileBatch2")
    public void downPrintLodopFile(HttpServletResponse response, HttpServletRequest request) throws Exception{

        Map<String, List<String>> snLogMap = new HashMap<>();

        List<String> files = new ArrayList<>();

        String setup_fileName = "file:///D:/ST000056/Pictures/Camera Roll/test.png";
        String install32_fileName = "file:///D:/ST000056/Pictures/Camera Roll/test2.png";
        String zip = "http://172.18.120.92:9210/policy/Privacy_policy/steinckerAPP_policy.pdf";

        files.add(setup_fileName);
        files.add(install32_fileName);
        files.add(zip);

        List<String> files1 = new ArrayList<>();
        files1.add("http://172.18.120.92:9210/policy/Privacy_policy/steinckerAPP_term.pdf");

        snLogMap.put("sn-2", files1);
        snLogMap.put("sn-1", files);



        String workPath = System.getProperty("user.dir");

        //执行down
        //down(workPath, files, request, response);
       down2(workPath, snLogMap, request, response);
    }

    public void down(String path, List<String> files, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // path 压缩文件初始设置
        String base_name = "printFiles";
        String fileZip = base_name + ".zip"; // 拼接zip文件,之后下载下来的压缩文件的名字
        String filePath = path + fileZip;// 之后用来生成zip文件

        // 创建临时压缩文件

        ExecutorService executorService = loadPool.downLoadLogZip();

        executorService.execute(new DownloadThread2(files, filePath));

        executorService.shutdown();

        // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
        boolean allTaskCompleted = executorService.awaitTermination(2, TimeUnit.MINUTES);
        if (!allTaskCompleted) {
            System.out.println("失败");
        }


//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        executorService.execute(() -> {
//
//            try {
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
//            ZipOutputStream zos = new ZipOutputStream(bos);
//            ZipEntry ze = null;
//            for (int i = 0; i < files.length; i++) {// 将所有需要下载的文件都写入临时zip文件
//                BufferedInputStream bis = new BufferedInputStream(
//                        new FileInputStream(files[i]));
//                ze = new ZipEntry(
//                        files[i].substring(files[i].lastIndexOf("/")));
//                zos.putNextEntry(ze);
//                int s = -1;
//                while ((s = bis.read()) != -1) {
//                    zos.write(s);
//                }
//                bis.close();
//            }
//            zos.flush();
//            zos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        });
//
//        // 关闭线程池
//        executorService.shutdown();
//
//        // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
//        boolean allTaskCompleted = executorService.awaitTermination(30, TimeUnit.MINUTES);
//        if (!allTaskCompleted) {
//            System.out.println("失败");
//        }
        // 以上，临时压缩文件创建完成

        // 进行浏览器下载
        DownloadUtil.browserDownload(fileZip,filePath,response,request);

    }


    public void down2(String path, Map<String, List<String>> snLogMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // path 压缩文件初始设置
//        String base_name = "printFiles";
//        String fileZip = base_name + ".zip"; // 拼接zip文件,之后下载下来的压缩文件的名字
//        String filePath = path + fileZip;// 之后用来生成zip文件

        List<String> fileZipList = new ArrayList<>();
        List<String> filePathList = new ArrayList<>();

        Set<String> snList = snLogMap.keySet();
        ExecutorService executorService = loadPool.downLoadLogZip();
        snList.forEach(sn -> {
            List<String> files = snLogMap.get(sn);
            String filePath = path + "/" + sn + ".zip";
            fileZipList.add(sn + ".zip");
            filePathList.add(filePath);

            executorService.execute(new DownloadThread2(files, filePath));
        });


        executorService.shutdown();

        // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
        boolean allTaskCompleted = executorService.awaitTermination(2, TimeUnit.MINUTES);
        if (!allTaskCompleted) {
            System.out.println("下载失败");
        }


        //打包成一个zip 通过浏览器下载
        DownloadUtil.doGetZip(request,response,filePathList);




    }

    private static class DownloadThread2 implements Runnable {
        private List<String> files;

        private String filePath;




        public DownloadThread2(List<String> files, String filePath) {
            this.files = files;
            this.filePath = filePath;
        }

        @Override
        public void run() {

            try {

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                ZipOutputStream zos = new ZipOutputStream(bos);
                ZipEntry ze = null;

                for (int i = 0; i < files.size(); i++) {// 将所有需要下载的文件都写入临时zip文件
//                    BufferedInputStream bis = new BufferedInputStream(
//                            new FileInputStream(files.get(i)));
                    BufferedInputStream bis = new BufferedInputStream(
                            new URL(files.get(i)).openStream());
                    ze = new ZipEntry(
                            files.get(i).substring(files.get(i).lastIndexOf("/")));
                    zos.putNextEntry(ze);
                    int s = -1;
                    while ((s = bis.read()) != -1) {
                        zos.write(s);
                    }
                    bis.close();
                }
                zos.flush();
                zos.close();


            } catch (Exception e) {

                System.out.println(e);
            }


        }
    }

















}