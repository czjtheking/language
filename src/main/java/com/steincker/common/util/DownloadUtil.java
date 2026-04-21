package com.steincker.common.util;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @ClassName DownloadUtil
 * @Author ST000056
 * @Date 2023-12-07 16:11
 * @Version 1.0
 * @Description
 **/


public class DownloadUtil {


    /**
     * 通过浏览器下载
     * */
    public static void browserDownload(String fileZip, String filePath, HttpServletResponse response, HttpServletRequest request) {

        try {
            // 获得浏览器代理信息
            String agent = request.getHeader("User-Agent").toUpperCase();
            // 判断浏览器代理并分别设置响应给浏览器的编码格式
            String finalFileName = null;
            if ((agent.indexOf("MSIE") > 0)
                    || ((agent.indexOf("RV") != -1) && (agent.indexOf("FIREFOX") == -1)))
                finalFileName = URLEncoder.encode(fileZip, "UTF-8");
            else {
                finalFileName = new String(fileZip.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setContentType("application/x-download");// 告知浏览器下载文件，而不是直接打开，浏览器默认为打开
            response.setHeader("Content-Disposition", "attachment;filename=\""
                    + finalFileName + "\"");// 下载文件的名称
            //输出到本地
            ServletOutputStream servletOutputStream = response.getOutputStream();
            DataOutputStream temps = new DataOutputStream(servletOutputStream);

            DataInputStream in = new DataInputStream(new FileInputStream(filePath));// 浏览器下载临时文件的路径
            byte[] b = new byte[2048];
            File reportZip = new File(filePath);// 之后用来删除临时压缩文件

            while ((in.read(b)) != -1) {
                temps.write(b);
            }
            temps.flush();

            if (temps != null) {
                temps.close();
            }

            if (in != null) {
                in.close();
            }

            if (reportZip != null) {
                reportZip.delete();// 删除服务器本地产生的临时压缩文件
            }

            servletOutputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 浏览器下载单个文件
     * */
    public static void doGet(HttpServletRequest request, HttpServletResponse response,  String filePath)
            throws ServletException, IOException {
        // 指定要下载的文件路径列表
        //String filePath = "D:/ST000056/Pictures/Camera Roll/test.png";

        // 设置响应头
        response.setContentType("application/octet-stream");

        // 逐个处理文件并写入响应流
        File file = new File(filePath);

        // 设置响应头，告诉浏览器文件名
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

        // 读取文件并写入响应流
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
        }

        // 写完一个文件后，刷新响应流
        response.flushBuffer();

        if (file != null) {
            file.delete();// 删除服务器本地产生的临时文件
        }

    }


    /**
     * 浏览器下载多个文件 压缩
     * */
    public static void doGetZip(HttpServletRequest request, HttpServletResponse response, List<String> filePaths)
            throws ServletException, IOException {
        // 指定要下载的文件路径列表
//        String[] filePaths = {
//                "D:/ST000056/Pictures/Camera Roll/test.png",
//                "D:/ST000056/Pictures/Camera Roll/kaxiusi.png",
//                // 添加更多文件路径...
//        };

        // 设置响应头，表示为zip文件
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=downloadedFiles.zip");

        // 创建ZipOutputStream
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                // 创建ZipEntry并写入ZipOutputStream
                zipOut.putNextEntry(new ZipEntry(file.getName()));

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                }

                // 关闭当前ZipEntry
                zipOut.closeEntry();

                if (file != null) {
                    file.delete();// 删除服务器本地产生的临时文件
                }

            }

            // 完成压缩
            zipOut.finish();
        }
    }
}



