package com.steincker.controller;

import com.steincker.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName exportExcelTest
 * @Author ST000056
 * @Date 2023-12-28 20:18
 * @Version 1.0
 * @Description
 **/

@RestController
@RequestMapping(value = "/excel")
public class exportExcelTest {


    @Autowired
    private ExcelExportService excelExportService;

    /** 数据导出到Excel*/
    @GetMapping("/exportToExcel")
    public void exportToExcel(HttpServletResponse response) throws IOException {

        List<Map<Integer,String>> dataList = new ArrayList<>();

        Map<Integer,String> map1 = new HashMap<>();
        map1.put(1,"yh1");
        map1.put(2,"yh2");
        dataList.add(map1);
        Map<Integer,String> map2 = new HashMap<>();
        map2.put(1,"yhx1");
        map2.put(2,"yhx2");
        dataList.add(map2);
        // 调用导出服务进行导出
        excelExportService.exportToExcel(dataList, response);
    }

}
