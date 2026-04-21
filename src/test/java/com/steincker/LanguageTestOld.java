package com.steincker;

import com.steincker.common.util.LinkedProperties;
import com.steincker.common.util.PropertiesUtils;
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

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName languageTest
 * @Author ST000056
 * @Date 2024-10-19 14:37
 * @Version 1.0
 * @Description
 **/
@SpringBootTest
public class LanguageTestOld {

    @Resource
    ConfigLanguageMapper configLanguageMapper;

    @Resource
    RegionLanguageMapper regionLanguageMapper;

    //=============================1.将各服务多语言错误码文件 复制重命名到指定文件夹中===================
    @Test
    void propertiesCopyToFileFold() {

        //正则表达式 截取 zh_CN这种
        Pattern pattern = Pattern.compile("messages_([a-z]{2}_[A-Z]{2})\\.properties");

        Map<String, String> servicePath = new HashMap<>();
        servicePath.put("energy", "D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n");
        servicePath.put("permission", "D:/steincker_code/steincker-permission/steincker-permission/permission-infrastructure/src/main/resources");
        servicePath.put("customer", "D:/steincker_code/steincker-customer/steincker-customer/customer-content-app/src/main/resources/i18n");
        servicePath.put("openapi", "D:/steincker_code/moorewatt-openAPI/moorewatt-openapi/openapi-app/src/main/resources/i18n");
        servicePath.put("cloud", "D:/steincker_code/steincker-cloud/steincker-cloud/steincker-cloud-framework/steincker-cloud-starter-i18n/src/main/resources/i18n-common");
        servicePath.put("push", "D:/steincker_code/steincker-push/steincker-push/push-message-app/src/main/resources");
        servicePath.put("system", "D:/steincker_code/steincker-system/steincker-system/system-app/src/main/resources/i18n");




        // 目标文件夹路径
        String targetDirectory = "D:/ST000056/Desktop/language/原始错误码properties";
        //String targetDirectory = "D:/ST000056/Desktop/language/异常码backups";

        // 确保目标文件夹存在
        new File(targetDirectory).mkdirs();

        // 遍历源文件路径列表
        for (String type : servicePath.keySet()) {
            String path = servicePath.get(type);

            // 创建源文件对象
            File sourceFile = new File(path);

            // 检查源文件是否存在
            if (sourceFile.exists()) {

                //过滤出错误码类型的文件
                File[] files = sourceFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".properties"));
                if (files != null) {

                    for (File file : files) {

                        //获取语言 默认zh-CN
                        String lang = "zh-CN";

                        Matcher matcher = pattern.matcher(file.getName());
                        if (matcher.find()) {
                            lang = matcher.group(1).replace("_", "-");
                        }


                        // 构造目标文件名（可以根据需要修改重命名逻辑）
                        String fileName = type + "[" + lang + "]";
                        String targetFilePath = targetDirectory + File.separator + fileName + ".properties";

                        // 创建目标文件对象
                        File targetFile = new File(targetFilePath);

                        //复制和重命名文件
                        try {
                            copyAndRenameFile(file, targetFile);
                            System.out.println("File " + file.getName() + " has been copied and renamed to " + targetFile.getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }


            }

        }

    }


    //========================== 2.将对应的 properties 全量导出到 excel =======================================
    /** properties导出 Excel 文档 测试代码 新版*/
    @Test
    void propertiesToExcelNew() throws IOException {
        File folder = new File("D:/ST000056/Desktop/language/原始错误码properties");

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".properties"));

        // 创建 Excel 工作簿和工作表
        Workbook workbook = new XSSFWorkbook();


        Map<String, Map<String, PropertiesUtils>> map = new HashMap<>();
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
            Cell headerCell6 = headerRow.createCell(5);
            headerCell6.setCellValue("th-TH");
            Cell headerCell7 = headerRow.createCell(6);
            headerCell7.setCellValue("es-ES");
            Cell headerCell8 = headerRow.createCell(7);
            headerCell8.setCellValue("pl-PL");
            Cell headerCell9 = headerRow.createCell(8);
            headerCell9.setCellValue("it-IT");
            Cell headerCell10 = headerRow.createCell(9);
            headerCell10.setCellValue("nl-NL");



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

                    if (Objects.equals(lang, "th-TH")) {

                        Cell cell2 = row.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "es-ES")) {

                        Cell cell2 = row.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "pl-PL")) {

                        Cell cell2 = row.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "it-IT")) {

                        Cell cell2 = row.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "nl-NL")) {

                        Cell cell2 = row.createCell(9);
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


    //=================================3.数据库 全量导出到 excel====================================

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

    /** energy_config_language 国际化数据导出到Excel  新版*/
    @Test
    void configLanguageToExcelNew() throws IOException {

        //查询所有多语言
        List<ConfigLanguage> configLanguages = configLanguageMapper.findAllGroupBy().stream().filter(
                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());


        //按照 type 类型分类
        Map<String, List<ConfigLanguage>> languageMaps = configLanguages.stream()
                .collect(Collectors.groupingBy(
                        ConfigLanguage::getType,
                        Collectors.toList()
                ));

        Set<String> keySet = languageMaps.keySet();

        //创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();

        //有多少大分类 就生成多少sheet页
        for (String configType : keySet) {

            //获取该分类的列表
            List<ConfigLanguage> languageMapsByTypeList = languageMaps.get(configType);

            Map<String, ConfigLanguage> languageMapsByTypeMap = languageMapsByTypeList.stream().
                    collect(Collectors.toMap(ConfigLanguage::getCode , configLanguage -> configLanguage));


            Sheet sheet = workbook.createSheet(configType);

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
            Cell headerCell6 = headerRow.createCell(5);
            headerCell6.setCellValue("th-TH");
            Cell headerCell7 = headerRow.createCell(6);
            headerCell7.setCellValue("es-ES");
            Cell headerCell8 = headerRow.createCell(7);
            headerCell8.setCellValue("pl-PL");
            Cell headerCell9 = headerRow.createCell(8);
            headerCell9.setCellValue("it-IT");
            Cell headerCell10 = headerRow.createCell(9);
            headerCell10.setCellValue("nl-NL");
            int rowNum = 1;

            //遍历每个code
            for (String code : languageMapsByTypeMap.keySet()) {
                ConfigLanguage configLanguage = languageMapsByTypeMap.get(code);

                //获取每个code的语言和对应的语言文本list
                List<String> languages = List.of(configLanguage.getLang().split(","));
                List<String> texts = List.of(configLanguage.getText().split("\\|"));



                if (languages.size() != texts.size()) {
                    //输出语种和对应翻译 不对等的多语言文本
                    System.out.println("the size is not =, the code is " + code);
                    continue;
                }

                //封装一下 map 语种对应的多语言
                Map<String, String> textMap = new HashMap<>();
                for (int i = 0; i < languages.size() ; i++) {
                    textMap.put(languages.get(i), texts.get(i));
                }

                //创建第一行 对应的就是多语言key
                Row row = sheet.createRow(rowNum++);
                Cell cell1 = row.createCell(0);
                cell1.setCellValue(code);

                //遍历每个语言
                for (String lang : textMap.keySet()) {
                    String value = textMap.get(lang);

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

                    if (Objects.equals(lang, "th-TH")) {

                        Cell cell2 = row.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "es-ES")) {

                        Cell cell2 = row.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "pl-PL")) {

                        Cell cell2 = row.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "it-IT")) {

                        Cell cell2 = row.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, "nl-NL")) {

                        Cell cell2 = row.createCell(9);
                        cell2.setCellValue(value);
                    }

                }



            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/database.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();


    }


    //==============================4.执行两次 比较新旧的错误码excel文档 新旧的数据库错误码excel文档 导出新增的key======================
    /** 比较新老全量 错误码 导出新增的key*/
    @Test
    void compareExcelOutPutAdd() throws IOException {

        //新导出的 Excel文件
         String NewExcelFilePath = "D:/ST000056/Desktop/language/原始错误码properties/error.xlsx";
//         String NewExcelFilePath = "D:/ST000056/Desktop/language/原始数据库多语言/database.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/2024/1231//error.xlsx";
//        String oldExcelFilePath = "D:/ST000056/Desktop/language/2024/1231/database.xlsx";

        //输出位置
//        OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/database_add.xlsx");

        OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始错误码properties/error_add.xlsx");



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
                Cell headerCell6 = headerRow.createCell(5);
                headerCell6.setCellValue("th-TH");
                Cell headerCell7 = headerRow.createCell(6);
                headerCell7.setCellValue("es-ES");
                Cell headerCell8 = headerRow.createCell(7);
                headerCell8.setCellValue("pl-PL");
                Cell headerCell9 = headerRow.createCell(8);
                headerCell9.setCellValue("it-IT");
                Cell headerCell10 = headerRow.createCell(9);
                headerCell10.setCellValue("nl-NL");

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
                            String oldKey = oldKeyCell == null ? "" : oldKeyCell.getStringCellValue();

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
                        if (newRow.getCell(1) != null) {
                            cell2.setCellValue(newRow.getCell(1).getStringCellValue());
                        }



                        Cell cell3 = row.createCell(2);
                        if (newRow.getCell(2) != null) {
                            cell3.setCellValue(newRow.getCell(2).getStringCellValue());
                        }



                        Cell cell4 = row.createCell(3);
                        if (newRow.getCell(3) != null) {
                            cell4.setCellValue(newRow.getCell(3).getStringCellValue());
                        }



                        Cell cell5 = row.createCell(4);
                        if (newRow.getCell(4) != null) {
                            cell5.setCellValue(newRow.getCell(4).getStringCellValue());
                        }



                        Cell cell6 = row.createCell(5);
                        if (newRow.getCell(5) != null) {
                            cell6.setCellValue(newRow.getCell(5).getStringCellValue());
                        }



                        Cell cell7 = row.createCell(6);
                        if (newRow.getCell(6) != null) {
                            cell7.setCellValue(newRow.getCell(6).getStringCellValue());
                        }



                        Cell cell8 = row.createCell(7);
                        if (newRow.getCell(7) != null) {
                            cell8.setCellValue(newRow.getCell(7).getStringCellValue());
                        }



                        Cell cell9 = row.createCell(8);
                        if (newRow.getCell(8) != null) {
                            cell9.setCellValue(newRow.getCell(8).getStringCellValue());
                        }

                        Cell cell10 = row.createCell(9);
                        if (newRow.getCell(9) != null) {
                            cell10.setCellValue(newRow.getCell(9).getStringCellValue());
                        }


                    }


                }


            }


            // 写入 Excel 文件
            try (output) {
                workbook.write(output);
            }

            // 关闭工作簿
            workbook.close();







    }


    //==============================5. excel 导入到 properties======================

    /** 读取 Excel 文档 写入信息到 properties 新版*/
    @Test
    void ExcelToPropertiesNew() throws IOException {

        //导入的 Excel文件
        String excelFilePath = "D:/ST000056/Desktop/language/1217/2024-12-19后端异常码翻译荷兰语内容.xlsx";


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

                            if (Objects.equals(lang, "th-TH")) {
                                valueCell = row.getCell(5);
                            }

                            if (Objects.equals(lang, "es-ES")) {
                                valueCell = row.getCell(6);
                            }

                            if (Objects.equals(lang, "pl-PL")) {
                                valueCell = row.getCell(7);
                            }

                            if (Objects.equals(lang, "it-IT")) {
                                valueCell = row.getCell(8);
                            }

                            if (Objects.equals(lang, "nl-NL")) {
                                valueCell = row.getCell(9);
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



    //================================6. excel 导入到数据库============================
    /** 读取 Excel 文档 写入信息到 energy_config_language表中 生成对应sql*/
    @Test
    void ExcelToConfigLanguage() throws IOException {

        String excelFilePath = "D:/ST000056/Desktop/language/0106/2025-01-06后端数据库翻译问题单修改德法内容.xlsx";

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
                insertSqlList.add(deleteSql.toString().replace("$", "\\$"));

                //替换最后一个逗号
                insertSql.setCharAt(insertSql.length() - 1, ';');
                insertSqlList.add(insertSql.toString().replace("$", "\\$"));



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



    //================================7. 更新后的错误码 转换文件名称 放置到对应文件夹中====================
    @Test
    void errorErrorExcelToSystem() throws IOException {
        // 创建 File 对象，表示指定路径
        File directory = new File("D:/steincker_code/demo/demo/demo/src/main/resources/i18");

        List<String> foldNames = new ArrayList<>();
        // 检查路径是否存在且是一个目录
        if (directory.exists() && directory.isDirectory()) {
           // 获取目录下的所有文件和文件夹
            File[] files = directory.listFiles();
            if (files != null) {
                // 遍历文件和文件夹
                for (File file : files) {
                    // 检查是否是目录
                    if (file.isDirectory()) {
                        // 输出目录名称
                        foldNames.add(file.getName());
                    }
                }
            }
        }

        //遍历各个文件夹 设置对应服务的错误码多语言
        for (String foldName : foldNames) {

            // 指定源文件夹路径
            String sourceDirPath = "D:/ST000056/Desktop/language/更新后的错误码properties/1226_新增的多语言翻译";
            // 指定目标文件夹路径
            String targetDirPath = "D:/steincker_code/demo/demo/demo/src/main/resources/i18/" + foldName;

            // 创建源文件夹和目标文件夹的 File 对象
            File sourceDir = new File(sourceDirPath);
            File targetDir = new File(targetDirPath);

            // 检查源文件夹是否存在且是一个目录
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                // 检查目标文件夹是否存在，如果不存在则创建
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                // 获取源文件夹下的所有文件
                File[] files = sourceDir.listFiles((dir, name) -> name.endsWith(".properties"));

                if (files != null) {
                    // 遍历所有 .properties 文件
                    for (File file : files) {
                        // 获取文件名
                        String fileName = file.getName();
                        // 解析文件名，例如 cloud[es-ES].properties
                        String newName = parseFileName(fileName, foldName);
                        if (newName != null) {
                            // 构建目标文件路径
                            Path targetFilePath = Paths.get(targetDirPath, newName);
                            // 复制文件
                            try {
                                Files.copy(file.toPath(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Copied: " + file.getAbsolutePath() + " to " + targetFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                System.out.println("指定的源文件夹不存在或不是一个目录");
            }
            }
        }

    // 解析文件名，例如 cloud[es-ES].properties 转换为 messages_es_ES.properties
    private static String parseFileName(String fileName, String foldName) {
        // 使用正则表达式匹配文件名
        String pattern = foldName + "\\[(\\w+-\\w+)\\].properties";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fileName);
        if (m.find()) {
            String locale = m.group(1);
            // 替换为新的文件名格式
            return "messages_" + locale.replace("-", "_") + ".properties";
        }
        return null;
    }



    //========================================单独的截取方法=====================================
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
            // inputString substring keyworedIndex
            return inputString.substring(0, keywordIndex);
        } else {
            return inputString;  // 如果找不到关键字，返回整个输入字符串
        }
    }

    /**
     * 复制和重命名文件的方法
     *
     * @param source 源文件对象
     * @param target 目标文件对象
     * @throws IOException 如果发生I/O错误
     */
    public static void copyAndRenameFile(File source, File target) throws IOException {
        // 确保源文件存在且是一个文件
        if (source.exists() && source.isFile()) {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(target)) {

                // 复制文件内容
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        } else {
            throw new IOException("Source file does not exist or is not a file: " + source.getAbsolutePath());
        }
    }


}
