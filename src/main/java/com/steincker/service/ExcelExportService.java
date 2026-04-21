package com.steincker.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @InterfaceName ExcelExportService
 * @Author ST000056
 * @Date 2023-12-28 20:28
 * @Version 1.0
 * @Description
 **/
public interface ExcelExportService {

    public void exportToExcel(List<Map<Integer,String>> dataList, HttpServletResponse response) throws IOException;
}
