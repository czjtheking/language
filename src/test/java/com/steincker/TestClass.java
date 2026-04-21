package com.steincker;

import com.steincker.common.util.*;
import com.steincker.dao.ConfigLanguageMapper;
import com.steincker.dao.RegionLanguageMapper;
import com.steincker.entity.ConfigLanguage;
import com.steincker.entity.EnergyModelEvent;
import com.steincker.entity.RegionLanguage;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName Test
 * @Author ST000056
 * @Date 2023-10-19 15:24
 * @Version 1.0
 * @Description 单元测试
 **/
@SpringBootTest
public class TestClass {

    @Resource
    ConfigLanguageMapper configLanguageMapper;

    @Resource
    RegionLanguageMapper regionLanguageMapper;


    @Test
     void get15minTime() {

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(1736910899000L);

            //将分钟数取整到最近的15分钟
            int min = calendar.get(Calendar.MINUTE);
            int remainder = min % 15;
            //当前分钟减去对应时间
            calendar.add(Calendar.MINUTE, -remainder);

            //将秒、毫秒置0
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            //获取最近的15分钟点
            Date near15Min = calendar.getTime();
            //log.info("the paramDate is:{} , the near15minDate is: {}", paramDate, near15Min);
            long yhTime = near15Min.getTime();

        } catch (Exception e) {



        }
    }

    @Test
    void threadPoolTest() {

        for (int j = 0; j < 3; j++) {

            //使用线程池工具处理耗时操作
            DownLoadThreadPoolUtil.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    //在此执行耗时操作
                    //例如：文件下载、数据库存取、音频格式转换等
                    for (int i = 0; i < 2; i++) {
                        System.out.println("线程名:" + Thread.currentThread().getName() + " i是:" + i);
                    }

                }
            });

        }

    }

    /** energy_config_language 国际化数据导出到Excel*/
    @Test
    void configLanguageToExcel() throws IOException {

        //查询所有多语言
        List<ConfigLanguage> configLanguages = configLanguageMapper.findAll().stream().filter(
                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());

//        List<ConfigLanguage> configLanguages = configLanguageMapper.findAllByAppAndTypeAndLang("energy","model.event.suggestion","lang").stream().filter(
//                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());


        //按照 type 类型分类
        Map<String, List<ConfigLanguage>> languageMaps = configLanguages.stream()
                .collect(Collectors.groupingBy(
                        ConfigLanguage::getType,
                        Collectors.toList()
                ));

        Set<String> keySet = languageMaps.keySet();

        //有多少大分类 就生成多少文档
        for (String configType : keySet) {

            //创建 Excel 工作簿
            Workbook workbook = new XSSFWorkbook();

            //获取该分类的列表
            List<ConfigLanguage> languageMapsByTypeList = languageMaps.get(configType);

            //按照语言分类
            Map<String, List<ConfigLanguage>> languageMapsByLang = languageMapsByTypeList.stream()
                    .collect(Collectors.groupingBy(
                            ConfigLanguage::getLang,
                            Collectors.toList()
                    ));

            //获取所有语言
            Set<String> langSet = languageMapsByLang.keySet();

            //有多少语言 就生成多少sheet页
            for (String lang : langSet) {

                // 创建 Excel 工作表
                Sheet sheet = workbook.createSheet(lang);
                // 创建表头行
                Row headerRow = sheet.createRow(0);
                Cell headerCell1 = headerRow.createCell(0);
                headerCell1.setCellValue("type");
                Cell headerCell2 = headerRow.createCell(1);
                headerCell2.setCellValue("Key");
                Cell headerCell3 = headerRow.createCell(2);
                headerCell3.setCellValue(lang);

                // 遍历 properties 并写入 Excel
                int rowNum = 1;

                List<ConfigLanguage> filteredList = languageMapsByTypeList.stream()
                        .filter(configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty() &&
                                Objects.equals(configLanguage.getLang(), lang))
                        .collect(Collectors.toList());

                Map<String,String> languageTextMap = filteredList.stream()
                        .collect(Collectors.toMap(ConfigLanguage::getCode, ConfigLanguage :: getText));



                Set<String> codeSet = languageTextMap.keySet();

                for (String code : codeSet) {
                    Row row = sheet.createRow(rowNum++);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(configType);
                    Cell cell2 = row.createCell(1);
                    cell2.setCellValue(code);
                    Cell cell3 = row.createCell(2);
                    cell3.setCellValue(languageTextMap.get(code));
                }


                String fileName = configType.replace('.','_');
                // 写入 Excel 文件
                try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/"+ fileName +".xlsx")) {
                    workbook.write(output);
                }

            }

            // 关闭工作簿
            workbook.close();

        }


    }

    /** 读取 Excel 文档 写入信息到 energy_config_language表中 生成对应sql*/
    @Test
    void ExcelToConfigLanguage() throws IOException {

        String excelFilePath = "D:/ST000056/Desktop/language/0903/2024-09-30后端数据库翻译内容.xlsx";

        String regex = "\\b[a-z]{2}-[A-Z]{2}\\b"; // 正则表达式匹配 "xx-XX" 格式的字符串

        List<ConfigLanguage> configLanguages = new ArrayList<>();

        List<String> insertSqlList = new ArrayList<>();

        try {
            FileInputStream excelFile = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(excelFile);
            int sheetCount = workbook.getNumberOfSheets();

            // 遍历每个工作表并获取名称
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                StringBuffer insertSql = new StringBuffer(" INSERT INTO energy_config_language (app, `type`, code, lang, `text`) VALUES\n ");

                StringBuffer deleteSql = new StringBuffer(" delete from energy_config_language where  ");

                boolean isValid = sheetName.matches(regex);

                //不满足 xx-XX格式 直接跳过
                if (!isValid) {
                    continue;
                }

                //每个sheet页数据处理
                for (Row row : sheet) {

                    ConfigLanguage config = new ConfigLanguage();

                    if (row.getRowNum() == 0) {
                        continue;
                    }

                    Cell typeCell = row.getCell(0);
                    Cell keyCell = row.getCell(1);
                    Cell textCell = row.getCell(2);

                    if (typeCell != null && keyCell != null) {
                        String type = typeCell.getStringCellValue();
                        String code = keyCell.getStringCellValue();
                        String text = textCell.getStringCellValue();
                        String lang = sheetName;

                        config.setApp("energy");
                        config.setType(type);
                        config.setCode(code);
                        config.setText(text);
                        config.setLang(lang);
                        config.setDeleted(0L);
                        configLanguages.add(config);

                        //拼接插入sql语句
                        insertSql.append(" ( 'energy',").
                                append("\"").append(type).append("\",").
                                append("\"").append(code).append("\",").
                                append("\"").append(lang).append("\",").
                                append("\"").append(text).append("\")\n,");

                        //拼接删除sql语句
                        deleteSql.append("( app = 'energy' and ").append(" `type` = '" ).append(type).
                                append("' and").append(" code = '").append(code).append("' and lang = '").append(lang).append("' )\n or");
                    }

                }

                //删除条件删除最后一个or 替换为;
                deleteSql.deleteCharAt(deleteSql.length() - 1);
                deleteSql.setCharAt(deleteSql.length() - 1, ';');
                insertSqlList.add(deleteSql.toString());

                //替换最后一个逗号
                insertSql.setCharAt(insertSql.length() - 1, ';');
                insertSqlList.add(insertSql.toString());


            }

            System.out.println(insertSqlList);

            //输出sql
            sqlToTxt(insertSqlList);


//            //删除旧国际化
//            configLanguageMapper.deleteBatch(configLanguages);
//
//            //新增新国际化
//            configLanguageMapper.insertBatch(configLanguages);




        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }

    }

    /** 单独处理 告警 国际化数据导出到Excel*/
    @Test
    void eventToExcel() throws IOException {

        //获取所有告警模型
        List<EnergyModelEvent> modelEventList = configLanguageMapper.listAlarmModelEvent();

        //查询所有多语言
//        List<ConfigLanguage> configLanguages = configLanguageMapper.findAll().stream().filter(
//                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());

        List<ConfigLanguage> configLanguages = configLanguageMapper.findAllByAppAndTypeAndLang("energy","model.event","lang").stream().filter(
                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());


        //按照 type 类型分类
        Map<String, List<ConfigLanguage>> languageMaps = configLanguages.stream()
                .collect(Collectors.groupingBy(
                        ConfigLanguage::getType,
                        Collectors.toList()
                ));

        Set<String> keySet = languageMaps.keySet();

        //有多少大分类 就生成多少文档
        for (String configType : keySet) {

            //创建 Excel 工作簿
            Workbook workbook = new XSSFWorkbook();

            //获取该分类的列表
            List<ConfigLanguage> languageMapsByTypeList = languageMaps.get(configType);

            //按照语言分类
            Map<String, List<ConfigLanguage>> languageMapsByLang = languageMapsByTypeList.stream()
                    .collect(Collectors.groupingBy(
                            ConfigLanguage::getLang,
                            Collectors.toList()
                    ));

            //获取所有语言
            Set<String> langSet = languageMapsByLang.keySet();

            //有多少语言 就生成多少sheet页
            for (String lang : langSet) {

                // 创建 Excel 工作表
                Sheet sheet = workbook.createSheet(lang);
                // 创建表头行
                Row headerRow = sheet.createRow(0);
                Cell headerCell1 = headerRow.createCell(0);
                headerCell1.setCellValue("type");
                Cell headerCell2 = headerRow.createCell(1);
                headerCell2.setCellValue("Key");
                Cell headerCell3 = headerRow.createCell(2);
                headerCell3.setCellValue(lang);

                // 遍历 properties 并写入 Excel
                int rowNum = 1;

                List<ConfigLanguage> filteredList = languageMapsByTypeList.stream()
                        .filter(configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty() &&
                                Objects.equals(configLanguage.getLang(), lang))
                        .collect(Collectors.toList());

                Map<String,String> languageTextMap = filteredList.stream()
                        .collect(Collectors.toMap(ConfigLanguage::getCode, ConfigLanguage :: getText));



                Set<String> codeSet = languageTextMap.keySet();

                for (String code : codeSet) {
                    Row row = sheet.createRow(rowNum++);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(configType);
                    Cell cell2 = row.createCell(1);
                    cell2.setCellValue(code);
                    Cell cell3 = row.createCell(2);
                    cell3.setCellValue(languageTextMap.get(code));
                }


                String fileName = configType.replace('.','_');
                // 写入 Excel 文件
                try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/test/alarm/"+ fileName +".xlsx")) {
                    workbook.write(output);
                }

            }

            // 关闭工作簿
            workbook.close();

        }





    }

    /** energy_region_language 国际化数据导出到Excel*/
    @Test
    void reginLanguageToExcel() throws IOException {

        List<RegionLanguage> regionLanguages = regionLanguageMapper.findAll().stream().filter(
                regionLanguage -> regionLanguage.getName() != null && !regionLanguage.getName().isEmpty()).collect(Collectors.toList());


        //按照语言分类
        Map<String, List<RegionLanguage>> languageMapsByLang = regionLanguages.stream()
                .collect(Collectors.groupingBy(
                        RegionLanguage::getLang,
                        Collectors.toList()
                ));


        //获取所有语言
        Set<String> langSet = languageMapsByLang.keySet();

        // 创建 Excel 工作簿和工作表
        Workbook workbook = new XSSFWorkbook();

        //有多少语言 就生成多少sheet页
        for (String lang : langSet) {

            List<RegionLanguage> languageMapsByLangList = languageMapsByLang.get(lang);

            Map<String, RegionLanguage> textLanguageMaps = languageMapsByLangList.stream().collect(Collectors.toMap(RegionLanguage :: getCode , r -> r));

            Set<String> codeSet = textLanguageMaps.keySet();

            Sheet sheet = workbook.createSheet(lang);
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            Cell headerCell1 = headerRow.createCell(0);
            headerCell1.setCellValue("type");
            Cell headerCell2 = headerRow.createCell(1);
            headerCell2.setCellValue("Key");
            Cell headerCell3 = headerRow.createCell(2);
            headerCell3.setCellValue(lang);

            // 遍历 properties 并写入 Excel
            int rowNum = 1;


            for (String code : codeSet) {
                Row row = sheet.createRow(rowNum++);
                Cell cell1 = row.createCell(0);
                cell1.setCellValue(textLanguageMaps.get(code).getType());
                Cell cell2 = row.createCell(1);
                cell2.setCellValue(code);
                Cell cell3 = row.createCell(2);
                cell3.setCellValue(textLanguageMaps.get(code).getName());
            }




            // 写入 Excel 文件
            try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/test/regionConfigMessages.xlsx")) {
                workbook.write(output);
            }

        }




        // 关闭工作簿
        workbook.close();

    }

    /** properties导出 Excel 文档 测试代码*/
    @Test
    void propertiesToExcel() throws IOException {


        // 读取 properties zh-CN 文件
//        Properties properties = new Properties();
//        try (InputStream input = new FileInputStream
//                ("D:/steincker_code/steincker-permission/steincker-permission/permission-infrastructure/src/main/resources/messages.properties")) {
//            properties.load(input);
//        }

        PropertiesUtils properties = new PropertiesUtils();
        properties.loadProperties("D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n/messages_zh_CN_cloud.properties");




        // 读取 properties en_US 文件
//        Properties properties_en_US = new Properties();
//        try (InputStream input = new FileInputStream
//                ("D:/steincker_code/steincker-permission/steincker-permission/permission-infrastructure/src/main/resources/messages_en_US.properties")) {
//            properties_en_US.load(input);
//        }

        PropertiesUtils properties_en_US = new PropertiesUtils();
        properties_en_US.loadProperties("D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n/messages_en_US_cloud.properties");

        PropertiesUtils properties_de = new PropertiesUtils();
        properties_de.loadProperties("D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n/messages_de_DE_cloud.properties");

        PropertiesUtils properties_fr = new PropertiesUtils();
        properties_fr.loadProperties("D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n/messages_fr_FR_cloud.properties");

        // 创建 Excel 工作簿和工作表
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Messages");

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Key");
        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("zh-CN");
        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("en-US");
        Cell headerCell4 = headerRow.createCell(3);
        headerCell4.setCellValue("de-DE");
        Cell headerCell5 = headerRow.createCell(4);
        headerCell5.setCellValue("fr-FR");

        List<String> keyNames = properties.getKeyList().stream().map(String ::valueOf).collect(Collectors.toList());

        Set<String> keyNames_en_US = properties_en_US.stringPropertyNames();

        // 遍历 properties 并写入 Excel
        int rowNum = 1;
        for (String key : keyNames) {
            String value = properties.getProperty(key);

            String value_en_US = properties_en_US.getProperty(key);

            String value_de = properties_de.getProperty(key);
            String value_fr = properties_fr.getProperty(key);

            Row row = sheet.createRow(rowNum++);
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(key);
            Cell cell2 = row.createCell(1);
            cell2.setCellValue(value);
            Cell cell3 = row.createCell(2);
            cell3.setCellValue(value_en_US);

            Cell cell4 = row.createCell(3);
            cell4.setCellValue(value_de);

            Cell cell5 = row.createCell(4);
            cell5.setCellValue(value_fr);
        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/0619/cloud_messages.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();
    }

    /** 读取 Excel 文档 写入信息到 properties*/
    @Test
    void ExcelToProperties() throws IOException {

        //导入的 Excel文件
        //String excelFilePath = "D:/ST000056/Desktop/language/messages_permission.xlsx";
        String excelFilePath = "D:/ST000056/Desktop/language/0607/2024-06-07后端异常码翻译内容.xlsx";

        //新版生成的 properties文件
        String propertiesFilePath = "D:/steincker_code/demo/demo/demo/src/main/resources/i18/energy/messages_fr_FR.properties";

        //老版 properties文件
        PropertiesUtils oldProperties = new PropertiesUtils();
        oldProperties.loadProperties
                ("D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n/messages_fr_FR.properties");

        //老版的 key名称全集
        List<String> oldKeyNames = oldProperties.getKeyList().stream().map(String ::valueOf).collect(Collectors.toList());

        try {
            FileInputStream excelFile = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(excelFile);
            Sheet sheet = workbook.getSheet("energy");
            LinkedProperties properties = new LinkedProperties();


            for (String oldKey : oldKeyNames) {

                properties.setProperty(oldKey, oldProperties.getProperty(oldKey));

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue;
                    }
                    Cell keyCell = row.getCell(0);

                    //中文 zh_CN
                    Cell valueCell = row.getCell(4);

                    //第三列 为其他语言 en_US/de_DE
                    //Cell valueCell = row.getCell(2);
                    if (keyCell != null && valueCell != null) {
                        String key = keyCell.getStringCellValue();
                        String value = valueCell.getStringCellValue();

                        //如果有匹配的key 那么使用Excel文件中的value值 否则还是使用原来的值
                        if (Objects.equals(oldKey, key)) {
                            System.out.println("文本有变更，key ：" + key + " newValue : " + value + " oldValue :" + oldProperties.getProperty(oldKey));
                            properties.setProperty(oldKey, value);
                        }
                    }


                }


            }

            FileOutputStream output = new FileOutputStream(propertiesFilePath);
            properties.store(output, "Excel to Properties");
            output.close();

            System.out.println("Properties file generated successfully.");

        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }
    }

    /** properties导出 Excel 文档 测试代码 新版*/
    @Test
    void propertiesToExcelNew() throws IOException {
        File folder = new File("D:/ST000056/Desktop/language/原始错误码properties");

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".properties") || name.toLowerCase().endsWith(".properties"));

        // 创建 Excel 工作簿和工作表
        Workbook workbook = new XSSFWorkbook();


        Map<String,Map<String,PropertiesUtils>> map = new HashMap<>();
        if (files != null) {
            for (File file : files) {

                //文件命名格式 如energy的错误码文件  ： energy[zh-CN].properties
                String propName = file.getName();

                //获取语言
                String language = getKeywords(propName, "\\[(.*?)\\]");

                //获取是哪个服务的错误码
                String type = getStringBeforeKeyword(propName, "[");

                PropertiesUtils properties = new PropertiesUtils();
                properties.loadProperties("D:/ST000056/Desktop/language/原始错误码properties/" + propName);

                Map<String, PropertiesUtils> langMap = map.get(type);

                if (langMap != null) {
                    langMap.put(language, properties);
                } else {
                    langMap = new HashMap<>();
                    langMap.put(language, properties);
                }

                map.put(type, langMap);

            }
        }

        //服务名称 如 energy、permission
        Set<String> typeList = map.keySet();

        for (String type : typeList) {

            Sheet sheet = workbook.createSheet(type);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            Cell headerCell1 = headerRow.createCell(0);
            headerCell1.setCellValue("Key");
            Cell headerCell2 = headerRow.createCell(1);
            headerCell2.setCellValue("zh-CN");
            Cell headerCell3 = headerRow.createCell(2);
            headerCell3.setCellValue("en-US");
            Cell headerCell4 = headerRow.createCell(3);
            headerCell4.setCellValue("de-DE");
            Cell headerCell5 = headerRow.createCell(4);
            headerCell5.setCellValue("fr-FR");


            Map<String , PropertiesUtils> utilsMap = map.get(type);

            //获取每个服务的多语言错误码对应 语言
            List<String> langList = new ArrayList<>(utilsMap.keySet());

            PropertiesUtils prop =  utilsMap.get(langList.get(0));

            List<String> keyNames = prop.getKeyList().stream().map(String ::valueOf).collect(Collectors.toList());

            int rowNum = 1;
            for(String key : keyNames) {

                Row row = sheet.createRow(rowNum++);
                Cell cell1 = row.createCell(0);
                cell1.setCellValue(key);

                for (String lang : langList) {
                    String value = utilsMap.get(lang).getProperty(key);

                    if (Objects.equals(lang, "zh-CN")) {

                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "en-US")) {

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "de-DE")) {

                        Cell cell2 = row.createCell(3);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "fr-FR")) {

                        Cell cell2 = row.createCell(4);
                        cell2.setCellValue(value);
                    }

                }


            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始错误码properties/error.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();

    }


    /** 读取 Excel 文档 写入信息到 properties 新版*/
    @Test
    void ExcelToPropertiesNew() throws IOException {

        //导入的 Excel文件
        String excelFilePath = "D:/ST000056/Desktop/language/0903/2024-09-30后端异常码翻译内容.xlsx";


        try {
            FileInputStream excelFile = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(excelFile);

            //获取该文档有多少sheet页
            int sheetCount = workbook.getNumberOfSheets();

            // 遍历每个工作表并获取名称
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);

                //类型名称 energy、permission
                String type = sheet.getSheetName();

                //获取语言list （获取第一行数据）
                Row firstRow = sheet.getRow(0); // 获取第一行

                List<String> langList = new ArrayList<>();

                for (int j = 1 ; j < firstRow.getLastCellNum() ; j++ ) {
                    Cell cell = firstRow.getCell(j);
                    langList.add(cell.getStringCellValue());
                }

                //遍历语言，根据老版本的prop文件和当前Excel中的key对比 生成新版本的prop
                for (String lang : langList) {

                    LinkedProperties properties = new LinkedProperties();

                    //拼接老版本文件名称
                    String oldPropName = type + "[" + lang + "].properties";

                    //新版生成的 properties文件
                    String propertiesFilePath = "D:/ST000056/Desktop/language/更新后的错误码properties/" + oldPropName;

                    //老版 properties文件
                    PropertiesUtils oldProperties = new PropertiesUtils();
                    oldProperties.loadProperties
                            ("D:/ST000056/Desktop/language/原始错误码properties/" + oldPropName);

                    //老版的 key名称全集
                    List<String> oldKeyNames = oldProperties.getKeyList().stream().map(String ::valueOf).collect(Collectors.toList());

                    //匹配key
                    for (String oldKey : oldKeyNames) {

                        properties.setProperty(oldKey, oldProperties.getProperty(oldKey));

                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) {
                                continue;
                            }

                            //第一列为 key
                            Cell keyCell = row.getCell(0);
                            Cell valueCell = null;

                            if (Objects.equals(lang, "zh-CN")) {
                                valueCell = row.getCell(1);
                            }

                            if (Objects.equals(lang, "en-US")) {
                                valueCell = row.getCell(2);
                            }

                            if (Objects.equals(lang, "de-DE")) {
                                valueCell = row.getCell(3);
                            }

                            if (Objects.equals(lang, "fr-FR")) {
                                valueCell = row.getCell(4);
                            }

                            if (keyCell != null && valueCell != null) {
                                String key = keyCell.getStringCellValue();
                                String value = valueCell.getStringCellValue();

                                //如果有匹配的key 那么使用Excel文件中的value值 否则还是使用原来的值
                                if (Objects.equals(oldKey, key)) {
                                    System.out.println("文本有变更，key ：" + key + " newValue : " + value + " oldValue :" + oldProperties.getProperty(oldKey));
                                    properties.setProperty(oldKey, value);
                                }
                            }

                        }

                    }

                    FileOutputStream output = new FileOutputStream(propertiesFilePath);
                    properties.store(output, "Excel to Properties :" + lang);
                    output.close();

                }

            }

            System.out.println("Properties file generated successfully.");

        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }
    }





    /**
     * 线程池工具类测试
     */
    @Test
    @Async
    void ThreadPoolDemo() throws InterruptedException {


        // 获取线程池实例
        ThreadPoolUtil threadPool = ThreadPoolUtil.getInstance("yh");

        Long startTime = System.currentTimeMillis();
        // 提交任务到线程池执行
        for (int i = 0; i < 1; i++) {
            final int taskId = i;
            threadPool.execute(() -> {
                // 模拟任务执行
                try {
                    configLanguageToExcel();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Task " + taskId + " completed by thread: " + Thread.currentThread().getName());
            });
        }

        // 提交任务到线程池执行
        for (int i = 0; i < 1; i++) {
            final int taskId = i;
            threadPool.execute(() -> {
                // 模拟任务执行
                try {
                    propertiesToExcel();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Task_tow " + taskId + " completed by thread: " + Thread.currentThread().getName());
            });
        }



        // 关闭线程池
        // 注意：在实际应用中，应该在程序退出时调用线程池的 shutdown() 方法来关闭线程池，以确保资源的释放。
//        threadPool.shutdown();
//
//
//        // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
//        boolean allTaskCompleted = threadPool.awaitTermination(2, TimeUnit.MINUTES);

        Long endTime = System.currentTimeMillis();
        System.out.println("spend time: " + (endTime - startTime));


    }


    @Test
    void ThreadPoolDemo2() throws InterruptedException {

        Long startTime = System.currentTimeMillis();

        ThreadPoolExecutor threadPool =  ThreadHelper.executorBySize("device-" + "yh" + "-" + ((ThreadHelper.CORE_SIZE * 4L)),
                1, 100 * 10000, 30L);

        for (int i = 0; i < 2; i++) {

            int finalI = i;

            threadPool.execute(() -> {
                try {

                    if (finalI == 0) {
                        propertiesToExcel();
                    } else  {
                        configLanguageToExcel();
                    }

                } catch (Exception e) {

                }
            });

        }


        // 注意：在实际应用中，应该在程序退出时调用线程池的 shutdown() 方法来关闭线程池，以确保资源的释放。
        threadPool.shutdown();


        // 等待线程池中所有任务执行完成(指定时间内没有执行完则返回false)
        boolean allTaskCompleted = threadPool.awaitTermination(2, TimeUnit.MINUTES);


        Long endTime = System.currentTimeMillis();
        System.out.println("spend time: " + (endTime - startTime));


    }

    @Test
    void test() {

        List<Object> yhList = List.of(2L,11L);
        List<Long> singleItemList = yhList.stream().map(onj -> (Long)onj).collect(Collectors.toList());
        System.out.println(singleItemList); // 输出: [Hello]



    }


    /** 比较新老全量 错误码 导出新增的key*/
    @Test
    void compareExcelOutPutAdd() {

        //新导出的 Excel文件
        String NewExcelFilePath = "D:/ST000056/Desktop/language/原始错误码properties/error.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/1017/error_old.xlsx";

        try {

            FileInputStream newExcelFile = new FileInputStream(NewExcelFilePath);
            Workbook newWorkbook = WorkbookFactory.create(newExcelFile);

            FileInputStream oldExcelFile = new FileInputStream(oldExcelFilePath);
            Workbook oldWorkbook = WorkbookFactory.create(oldExcelFile);


            //获取该新文档有多少sheet页
            int newSheetCount = newWorkbook.getNumberOfSheets();
            int oldSheetCount = oldWorkbook.getNumberOfSheets();

            //key type(energy/permission 等)
            Map<String, Sheet> newSheetMap = new HashMap<>();
            Map<String, Sheet> oldSheetMap = new HashMap<>();

            // 遍历每个工作表并获取名称
            for (int i = 0; i < newSheetCount; i++) {
                Sheet newSsheet = newWorkbook.getSheetAt(i);

                //类型名称 energy、permission
                String type = newSsheet.getSheetName();
                newSheetMap.put(type, newSsheet);
            }

            for (int i = 0; i < oldSheetCount; i++) {
                Sheet oldSheet = oldWorkbook.getSheetAt(i);

                //类型名称 energy、permission
                String type = oldSheet.getSheetName();
                oldSheetMap.put(type, oldSheet);
            }

            // 创建 Excel 工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            for (String type : newSheetMap.keySet()) {

                Sheet sheet = workbook.createSheet(type);

                // 创建表头行
                Row headerRow = sheet.createRow(0);
                Cell headerCell1 = headerRow.createCell(0);
                headerCell1.setCellValue("Key");
                Cell headerCell2 = headerRow.createCell(1);
                headerCell2.setCellValue("zh-CN");
                Cell headerCell3 = headerRow.createCell(2);
                headerCell3.setCellValue("en-US");
                Cell headerCell4 = headerRow.createCell(3);
                headerCell4.setCellValue("de-DE");
                Cell headerCell5 = headerRow.createCell(4);
                headerCell5.setCellValue("fr-FR");

                Sheet newSheet = newSheetMap.get(type);

                Sheet oldSheet = oldSheetMap.get(type);

                //遍历新版文档，和旧版文档对比 没有对应上的就是新增的key
                int rowNum = 1;
                for (Row newRow : newSheet) {
                    if (newRow.getRowNum() == 0) {
                        continue;
                    }

                    //第一列为 key
                    Cell newKeyCell = newRow.getCell(0);
                    String newKey = newKeyCell.getStringCellValue();

                    //加标记 false就是已有的  true就是新增的
                    boolean isAdd = true;

                    if (oldSheet != null) {

                        //和旧文档比较
                        for (Row oldRow : oldSheet) {
                            if (oldRow.getRowNum() == 0) {
                                continue;
                            }
                            //第一列为 key
                            Cell oldKeyCell = oldRow.getCell(0);
                            String oldKey = oldKeyCell.getStringCellValue();

                            if (Objects.equals(newKey, oldKey)) {
                                isAdd = false;
                            }

                        }
                    }


                    if (isAdd) {

                        Row row = sheet.createRow(rowNum++);
                        Cell cell1 = row.createCell(0);
                        cell1.setCellValue(newKey);

                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue(newRow.getCell(1).getStringCellValue());

                        Cell cell3 = row.createCell(2);
                        cell3.setCellValue(newRow.getCell(2).getStringCellValue());

                        Cell cell4 = row.createCell(3);
                        cell4.setCellValue(newRow.getCell(3).getStringCellValue());

                        Cell cell5 = row.createCell(4);
                        cell5.setCellValue(newRow.getCell(4).getStringCellValue());

                    }


                }





            }


            // 写入 Excel 文件
            try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始错误码properties/error_add.xlsx")) {
                workbook.write(output);
            }

            // 关闭工作簿
            workbook.close();


        } catch (Exception e) {

        }





    }


    /** 输出sql文件*/
    void sqlToTxt(List<String> sqlList) throws IOException {

        // 文件路径
        String filePath = "D:/ST000056/Desktop/language/sql/tesSql.sql";

       FileWriter fileWriter = new FileWriter(filePath);
       BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            sqlList.forEach(s -> {

                // 如果需要，可以添加换行符以便在文件中分隔多个SQL语句
                try {
                    // 写入SQL语句
                    bufferedWriter.write(s);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // 刷新缓冲区并关闭流
            bufferedWriter.flush();

        System.out.println("SQL语句已成功写入文件：" + filePath);
    }







    //获取key符合号regex的关键字
    String getKeywords(String key, String regex) {

        if (key == null || regex == null) {
            return "";
        }

        String keyword = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(key);

        // 查找匹配的子字符串
        if (matcher.find()) {
            keyword = matcher.group(1);
        }

        return keyword;
    }

    /**
     * 获取输入字符串中关键字之前的部分
     * @param inputString 输入字符串
     * @param keyword 关键字
     * @return 关键字之前的部分，如果关键字不在输入字符串中，则返回整个输入字符串
     */
    public static String getStringBeforeKeyword(String inputString, String keyword) {
        int keywordIndex = inputString.indexOf(keyword);
        if (keywordIndex != -1) {
            return inputString.substring(0, keywordIndex);
        } else {
            return inputString;  // 如果找不到关键字，返回整个输入字符串
        }
    }
}

