package com.steincker.service.impl;


import com.steincker.service.ExcelExportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportServiceImpl  implements ExcelExportService {

    @Override
    public void exportToExcel(List<Map<Integer,String>> dataList, HttpServletResponse response) throws IOException {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();

        // 创建工作表
        Sheet sheet = workbook.createSheet("MyData");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("列1");
        headerRow.createCell(1).setCellValue("列2");
        // 添加更多列...

        // 创建数据行
        int rowNum = 1;
        for (Map<Integer,String> data : dataList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.get(1));
            row.createCell(1).setCellValue(data.get(2));
            // 添加更多列...

        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=myData.xlsx");

        // 将工作簿写入响应流
        workbook.write(response.getOutputStream());

        // 关闭工作簿
        workbook.close();
    }
}

