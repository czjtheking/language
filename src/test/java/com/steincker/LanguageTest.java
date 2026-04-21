package com.steincker;

import com.steincker.common.util.LinkedProperties;
import com.steincker.common.util.PropertiesUtils;
import com.steincker.dao.ConfigLanguageMapper;
import com.steincker.dao.PermissionConfigLanguageMapper;
import com.steincker.dao.PushMessageDetailMapper;
import com.steincker.dao.RegionLanguageMapper;
import com.steincker.entity.ConfigLanguage;
import com.steincker.entity.PermissionConfigLanguage;
import com.steincker.entity.PushMessageDetail;
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
public class LanguageTest {

    @Resource
    ConfigLanguageMapper configLanguageMapper;

    @Resource
    RegionLanguageMapper regionLanguageMapper;

    @Resource
    PermissionConfigLanguageMapper permissionConfigLanguageMapper;

    @Resource
    PushMessageDetailMapper pushMessageDetailMapper;

    public static final List<String> langList = List.of("key", "zh-CN", "en-US", "de-DE", "fr-FR",
            "th-TH", "es-ES", "pl-PL", "it-IT", "nl-NL", "zh-TW",
            "pt-PT", "ro-RO", "ja-JP");


    //------------------------------------------------导出------------------------------------------------------------//


    //=============================1.将各服务多语言错误码文件 复制重命名到指定文件夹中===================
    @Test
    void propertiesCopyToFileFold() {

        //正则表达式 截取 zh_CN这种
        Pattern pattern = Pattern.compile("messages_([a-z]{2}_[A-Z]{2})\\.properties");

        Map<String, String> servicePath = new HashMap<>();

        //开发
        servicePath.put("energy", "D:/steincker_code/steincker-energy/steincker-energy/energy-manage-app/src/main/resources/i18n");
        servicePath.put("permission", "D:/steincker_code/steincker-permission/steincker-permission/permission-infrastructure/src/main/resources");
        servicePath.put("customer", "D:/steincker_code/steincker-customer/steincker-customer/customer-content-app/src/main/resources/i18n");
        servicePath.put("openapi", "D:/steincker_code/moorewatt-openAPI/moorewatt-openapi/openapi-app/src/main/resources/i18n");
        servicePath.put("cloud", "D:/steincker_code/steincker-cloud/steincker-cloud/steincker-cloud-framework/steincker-cloud-starter-i18n/src/main/resources/i18n-common");
        servicePath.put("push", "D:/steincker_code/steincker-push/steincker-push/push-message-app/src/main/resources");
        servicePath.put("system", "D:/steincker_code/steincker-system/steincker-system/system-app/src/main/resources/i18n");
        servicePath.put("ai", "D:/steincker_code/atmoce-ai/i18n/message");


        //线上
        //        servicePath.put("energy", "D:/i18/steincker-energy/energy-manage-app/src/main/resources/i18n");
        //        servicePath.put("permission", "D:/i18/steincker-permission/permission-infrastructure/src/main/resources");
        //        servicePath.put("customer", "D:/i18/steincker-customer/customer-content-app/src/main/resources/i18n");
        //        servicePath.put("openapi", "D:/i18/moorewatt-openapi/openapi-app/src/main/resources/i18n");
        //        servicePath.put("cloud", "D:/i18/steincker-cloud/steincker-cloud-framework/steincker-cloud-starter-i18n/src/main/resources/i18n-common");
        //        servicePath.put("push", "D:/i18/steincker-push/push-message-app/src/main/resources");
        //        servicePath.put("system", "D:/i18/steincker-system/system-app/src/main/resources/i18n");
        //        servicePath.put("ai", "D:/i18/atmoce-ai/i18n/message");




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
            headerCell1.setCellValue(langList.get(0));
            Cell headerCell2 = headerRow.createCell(1); // zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(2); // en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(3); // de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(4); // fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(5); // th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(6); // es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(7); // pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(8); // it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(9); // nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(10); // zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(11); // pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(12); // ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(13);// ja-JP
            headerCell14.setCellValue(langList.get(13));



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

                    if (Objects.equals(lang, langList.get(1))) { // zh-CN

                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(2))) { // en-US

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(3))) { // de-DE

                        Cell cell2 = row.createCell(3);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(4))) { // fr-FR

                        Cell cell2 = row.createCell(4);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(5))) { // th-TH

                        Cell cell2 = row.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(6))) { // es-ES

                        Cell cell2 = row.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(7))) { // pl-PL

                        Cell cell2 = row.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(8))) { // it-IT

                        Cell cell2 = row.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(9))) { // nl-NL

                        Cell cell2 = row.createCell(9);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(10))) { // zh-TW

                        Cell cell2 = row.createCell(10);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(11))) { // pt-PT

                        Cell cell2 = row.createCell(11);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(12))) { // ro-RO

                        Cell cell2 = row.createCell(12);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(13))) { // ja-JP

                        Cell cell2 = row.createCell(13);
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



        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = "D:/ST000056/Desktop/language/原始错误码properties/error.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/1222/2026_0107_第四次导出_与第三次比较/error.xlsx";

        //输出位置_新增
        String outputAdd = "D:/ST000056/Desktop/language/原始错误码properties/error_add.xlsx";
        //输出位置_修改
        String outputUpdate = "D:/ST000056/Desktop/language/原始错误码properties/error_update.xlsx";

        compareExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        compareExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);

    }


    //=================================3.数据库 全量导出到 excel====================================

    /** energy_config_language 国际化数据导出到Excel  新版*/
    @Test
    void energyConfigLanguageToExcelNew() throws IOException {

        configLanguageMapper.setGroupConcatMaxLen();

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
            headerCell1.setCellValue(langList.get(0));
            Cell headerCell2 = headerRow.createCell(1);// zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(2);// en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(3);// de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(4);// fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(5);// th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(6);// es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(7);// pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(8);// it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(9);// nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(10);// zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(11);// pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(12);// ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(13);// ja-JP
            headerCell14.setCellValue(langList.get(13));
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

                    if (Objects.equals(lang, langList.get(1))) {// zh-CN

                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(2))) {// en-US

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(3))) {// de-DE

                        Cell cell2 = row.createCell(3);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(4))) {// fr-FR

                        Cell cell2 = row.createCell(4);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(5))) {// th-TH

                        Cell cell2 = row.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(6))) {// es-ES

                        Cell cell2 = row.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(7))) {// pl-PL

                        Cell cell2 = row.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(8))) {// it-IT

                        Cell cell2 = row.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(9))) {// nl-NL

                        Cell cell2 = row.createCell(9);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(10))) {// zh-TW

                        Cell cell2 = row.createCell(10);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(11))) {// pt-PT

                        Cell cell2 = row.createCell(11);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(12))) {// ro-RO

                        Cell cell2 = row.createCell(12);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(13))) {// ja-JP

                        Cell cell2 = row.createCell(13);
                        cell2.setCellValue(value);
                    }

                }



            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/energy_config_language.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();



        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = "D:/ST000056/Desktop/language/原始数据库多语言/energy_config_language.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/1222/2026_0107_第四次导出_与第三次比较/energy_config_language.xlsx";

        //输出位置
        String outputAdd = "D:/ST000056/Desktop/language/原始数据库多语言/energy_config_language_add.xlsx";
        String outputUpdate = "D:/ST000056/Desktop/language/原始数据库多语言/energy_config_language_update.xlsx";

        compareExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        compareExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);


    }

    /** permission_config_language 国际化数据导出到Excel  新版*/
    @Test
    void permissionConfigLanguageToExcelNew() throws IOException {

        permissionConfigLanguageMapper.setGroupConcatMaxLen();

        //查询所有多语言
        List<PermissionConfigLanguage> configLanguages = permissionConfigLanguageMapper.findAllGroupBy().stream().filter(
                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());


        //按照 type 类型分类
        Map<String, List<PermissionConfigLanguage>> languageMaps = configLanguages.stream()
                .collect(Collectors.groupingBy(
                        PermissionConfigLanguage::getType,
                        Collectors.toList()
                ));

        Set<String> keySet = languageMaps.keySet();

        //创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();

        //有多少大分类 就生成多少sheet页
        for (String configType : keySet) {

            //获取该分类的列表
            List<PermissionConfigLanguage> languageMapsByTypeList = languageMaps.get(configType);

            Map<String, PermissionConfigLanguage> languageMapsByTypeMap = languageMapsByTypeList.stream().
                    collect(Collectors.toMap(PermissionConfigLanguage::getCode , configLanguage -> configLanguage));


            Sheet sheet = workbook.createSheet(configType);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            Cell headerCell1 = headerRow.createCell(0);
            headerCell1.setCellValue(langList.get(0));
            Cell headerCellType = headerRow.createCell(1);
            headerCellType.setCellValue("field");
            Cell headerCell2 = headerRow.createCell(2);// zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(3);// en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(4);// de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(5);// fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(6);// th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(7);// es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(8);// pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(9);// it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(10);// nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(11);// zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(12);// pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(13);// ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(14);// ja-JP
            headerCell14.setCellValue(langList.get(13));
            int rowNum = 1;

            //遍历每个code
            for (String code : languageMapsByTypeMap.keySet()) {
                PermissionConfigLanguage configLanguage = languageMapsByTypeMap.get(code);

                //获取每个code的语言和对应的语言文本list
                List<String> languages = List.of(configLanguage.getLang().split(","));
                List<String> texts = List.of(configLanguage.getText().split("\\|"));
                List<String> descriptions = List.of(configLanguage.getDescription().split("\\|"));



                if (languages.size() != texts.size() || languages.size() != descriptions.size()) {
                    //输出语种和对应翻译 不对等的多语言文本
                    System.out.println("the size is not =, the code is " + code);
                    continue;
                }

                //封装一下 map 语种对应的多语言
                Map<String, String> textMap = new HashMap<>();
                Map<String, String> descriptionMap = new HashMap<>();
                for (int i = 0; i < languages.size() ; i++) {
                    textMap.put(languages.get(i), texts.get(i));
                    descriptionMap.put(languages.get(i), descriptions.get(i));
                    //                    异常处理
                    //                    try {
                    //                        textMap.put(languages.get(i), texts.get(i));
                    //                        descriptionMap.put(languages.get(i), descriptions.get(i));
                    //                    } catch (Exception e) {
                    //                        System.out.println("the lang is error , the code is " + code + " and the lang is " + languages.get(i));
                    //                        textMap.put(languages.get(i), "");
                    //                        descriptionMap.put(languages.get(i), "");
                    //                    }

                }

                //创建第一行 对应的就是多语言key
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(code);

                Cell cellText = row.createCell(1);
                cellText.setCellValue("text");

                //遍历每个语言 输出text文本
                for (String lang : textMap.keySet()) {
                    String value = textMap.get(lang);

                    if (Objects.equals(lang, langList.get(1))) {// zh-CN

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(2))) {// en-US

                        Cell cell2 = row.createCell(3);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(3))) {// de-DE

                        Cell cell2 = row.createCell(4);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(4))) {// fr-FR

                        Cell cell2 = row.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(5))) {// th-TH

                        Cell cell2 = row.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(6))) {// es-ES

                        Cell cell2 = row.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(7))) {// pl-PL

                        Cell cell2 = row.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(8))) {// it-IT

                        Cell cell2 = row.createCell(9);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(9))) {// nl-NL

                        Cell cell2 = row.createCell(10);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(10))) {// zh-TW

                        Cell cell2 = row.createCell(11);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(11))) {// pt-PT

                        Cell cell2 = row.createCell(12);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(12))) {// ro-RO
                        Cell cell2 = row.createCell(13);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(13))) {// ja-JP

                        Cell cell2 = row.createCell(14);
                        cell2.setCellValue(value);
                    }

                }

                Row row2 = sheet.createRow(rowNum++);
                //遍历每个语言 输出description文本
                Cell cell1 = row2.createCell(0);
                cell1.setCellValue(code);

                Cell cellDescription = row2.createCell(1);
                cellDescription.setCellValue("description");



                for (String lang : descriptionMap.keySet()) {
                    String value = descriptionMap.get(lang);

                    if (Objects.equals(lang, langList.get(1))) {// zh-CN

                        Cell cell2 = row2.createCell(2);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(2))) {// en-US

                        Cell cell2 = row2.createCell(3);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(3))) {// de-DE

                        Cell cell2 = row2.createCell(4);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(4))) {// fr-FR

                        Cell cell2 = row2.createCell(5);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(5))) {// th-TH

                        Cell cell2 = row2.createCell(6);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(6))) {// es-ES

                        Cell cell2 = row2.createCell(7);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(7))) {// pl-PL

                        Cell cell2 = row2.createCell(8);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(8))) {// it-IT

                        Cell cell2 = row2.createCell(9);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(9))) {// nl-NL

                        Cell cell2 = row2.createCell(10);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(10))) {// zh-TW

                        Cell cell2 = row2.createCell(11);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(11))) {// pt-PT

                        Cell cell2 = row2.createCell(12);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(12))) {// ro-RO

                        Cell cell2 = row2.createCell(13);
                        cell2.setCellValue(value);
                    }

                    if (Objects.equals(lang, langList.get(13))) {// ja-JP

                        Cell cell2 = row2.createCell(14);
                        cell2.setCellValue(value);
                    }

                }


            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/permission_config_language.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();


        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = "D:/ST000056/Desktop/language/原始数据库多语言/permission_config_language.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/1222/2026_0107_第四次导出_与第三次比较/permission_config_language.xlsx";

        //输出位置
        String outputAdd = "D:/ST000056/Desktop/language/原始数据库多语言/permission_config_language_add.xlsx";
        String outputUpdate = "D:/ST000056/Desktop/language/原始数据库多语言/permission_config_language_update.xlsx";

        comparePermissionAndPushExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        comparePermissionAndPushExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);


    }

    /** push_message_detail 国际化数据导出到Excel  新版*/
    @Test
    void pushMessageDetailToExcelNew() throws IOException {

        pushMessageDetailMapper.setGroupConcatMaxLen();
        //查询所有多语言
        List<PushMessageDetail> configLanguages = pushMessageDetailMapper.findAllGroupBy().stream().filter(
                configLanguage -> configLanguage.getTitle() != null && !configLanguage.getTitle().isEmpty()).collect(Collectors.toList());


        //创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();
        Map<String, PushMessageDetail> languageMapsByTypeMap = configLanguages.stream().
                collect(Collectors.toMap(configLanguage -> String.valueOf(configLanguage.getParentId()), configLanguage -> configLanguage));


        Sheet sheet = workbook.createSheet("pushDetail");

        // 创建表头行
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue(langList.get(0));
        Cell headerCellType = headerRow.createCell(1);
        headerCellType.setCellValue("field");
        Cell headerCell2 = headerRow.createCell(2);// zh-CN
        headerCell2.setCellValue(langList.get(1));
        Cell headerCell3 = headerRow.createCell(3);// en-US
        headerCell3.setCellValue(langList.get(2));
        Cell headerCell4 = headerRow.createCell(4);// de-DE
        headerCell4.setCellValue(langList.get(3));
        Cell headerCell5 = headerRow.createCell(5);// fr-FR
        headerCell5.setCellValue(langList.get(4));
        Cell headerCell6 = headerRow.createCell(6);// th-TH
        headerCell6.setCellValue(langList.get(5));
        Cell headerCell7 = headerRow.createCell(7);// es-ES
        headerCell7.setCellValue(langList.get(6));
        Cell headerCell8 = headerRow.createCell(8);// pl-PL
        headerCell8.setCellValue(langList.get(7));
        Cell headerCell9 = headerRow.createCell(9);// it-IT
        headerCell9.setCellValue(langList.get(8));
        Cell headerCell10 = headerRow.createCell(10);// nl-NL
        headerCell10.setCellValue(langList.get(9));
        Cell headerCell11 = headerRow.createCell(11);// zh-TW
        headerCell11.setCellValue(langList.get(10));
        Cell headerCell12 = headerRow.createCell(12);// pt-PT
        headerCell12.setCellValue(langList.get(11));
        Cell headerCell13 = headerRow.createCell(13);// ro-RO
        headerCell13.setCellValue(langList.get(12));
        Cell headerCell14 = headerRow.createCell(14);// ja-JP
        headerCell14.setCellValue(langList.get(13));
        int rowNum = 1;

        //遍历每个code
        for (String code : languageMapsByTypeMap.keySet()) {
            PushMessageDetail configLanguage = languageMapsByTypeMap.get(code);

            //获取每个code的语言和对应的语言文本list
            List<String> languages = List.of(configLanguage.getLanguage().split(","));
            List<String> titles = List.of(configLanguage.getTitle().split("\\|"));
            List<String> messages = List.of(configLanguage.getMessage().split("\\|"));
            List<String> appTitles = List.of(configLanguage.getAppTitle().split("\\|"));
            List<String> appMessages = List.of(configLanguage.getAppMessage().split("\\|"));


            if (languages.size() != titles.size() || languages.size() != messages.size()
                    || languages.size() != appTitles.size() || languages.size() != appMessages.size()) {
                //输出语种和对应翻译 不对等的多语言文本
                System.out.println("the size is not =, the code is " + code);
                continue;
            }

            //封装一下 map 语种对应的多语言
            Map<String, String> titleMap = new HashMap<>();
            Map<String, String> messageMap = new HashMap<>();
            Map<String, String> appTitleMap = new HashMap<>();
            Map<String, String> appMessageMap = new HashMap<>();
            for (int i = 0; i < languages.size(); i++) {
                titleMap.put(languages.get(i), titles.get(i));
                messageMap.put(languages.get(i), messages.get(i));
                appTitleMap.put(languages.get(i), appTitles.get(i));
                appMessageMap.put(languages.get(i), appMessages.get(i));
            }

            //创建第一行 对应的就是多语言key
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(code);

            Cell cellTitle = row.createCell(1);
            cellTitle.setCellValue("title");

            //遍历每个语言 输出title文本
            for (String lang : titleMap.keySet()) {
                String value = titleMap.get(lang);

                if (Objects.equals(lang, langList.get(1))) {// zh-CN

                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(2))) {// en-US

                    Cell cell2 = row.createCell(3);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(3))) {// de-DE

                    Cell cell2 = row.createCell(4);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(4))) {// fr-FR

                    Cell cell2 = row.createCell(5);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(5))) {// th-TH

                    Cell cell2 = row.createCell(6);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(6))) {// es-ES

                    Cell cell2 = row.createCell(7);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(7))) {// pl-PL

                    Cell cell2 = row.createCell(8);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(8))) {// it-IT

                    Cell cell2 = row.createCell(9);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(9))) {// nl-NL

                    Cell cell2 = row.createCell(10);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(10))) {// zh-TW

                    Cell cell2 = row.createCell(11);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(11))) {// pt-PT

                    Cell cell2 = row.createCell(12);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(12))) {// ro-RO

                    Cell cell2 = row.createCell(13);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(13))) {// ja-JP

                    Cell cell2 = row.createCell(14);
                    cell2.setCellValue(value);
                }

            }

            Row row2 = sheet.createRow(rowNum++);
            //遍历每个语言 输出message文本
            Cell cell1 = row2.createCell(0);
            cell1.setCellValue(code);

            Cell cellMessage = row2.createCell(1);
            cellMessage.setCellValue("message");

            for (String lang : messageMap.keySet()) {
                String value = messageMap.get(lang);

                if (Objects.equals(lang, langList.get(1))) {// zh-CN

                    Cell cell2 = row2.createCell(2);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(2))) {// en-US

                    Cell cell2 = row2.createCell(3);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(3))) {// de-DE

                    Cell cell2 = row2.createCell(4);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(4))) {// fr-FR

                    Cell cell2 = row2.createCell(5);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(5))) {// th-TH

                    Cell cell2 = row2.createCell(6);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(6))) {// es-ES

                    Cell cell2 = row2.createCell(7);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(7))) {// pl-PL

                    Cell cell2 = row2.createCell(8);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(8))) {// it-IT

                    Cell cell2 = row2.createCell(9);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(9))) {// nl-NL

                    Cell cell2 = row2.createCell(10);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(10))) {// zh-TW

                    Cell cell2 = row2.createCell(11);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(11))) {// pt-PT

                    Cell cell2 = row2.createCell(12);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(12))) {// ro-RO

                    Cell cell2 = row2.createCell(13);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(13))) {// ja-JP

                    Cell cell2 = row2.createCell(14);
                    cell2.setCellValue(value);
                }

            }

            //遍历每个语言 输出apptitle文本
            Row row3 = sheet.createRow(rowNum++);
            Cell cell3 = row3.createCell(0);
            cell3.setCellValue(code);
            Cell cellAppTitle = row3.createCell(1);
            cellAppTitle.setCellValue("app_title");

            for (String lang : appTitleMap.keySet()) {
                String value = appTitleMap.get(lang);

                if (Objects.equals(lang, langList.get(1))) {// zh-CN

                    Cell cell2 = row3.createCell(2);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(2))) {// en-US

                    Cell cell2 = row3.createCell(3);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(3))) {// de-DE

                    Cell cell2 = row3.createCell(4);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(4))) {// fr-FR

                    Cell cell2 = row3.createCell(5);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(5))) {// th-TH

                    Cell cell2 = row3.createCell(6);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(6))) {// es-ES

                    Cell cell2 = row3.createCell(7);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(7))) {// pl-PL

                    Cell cell2 = row3.createCell(8);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(8))) {// it-IT

                    Cell cell2 = row3.createCell(9);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(9))) {// nl-NL

                    Cell cell2 = row3.createCell(10);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(10))) {// zh-TW

                    Cell cell2 = row3.createCell(11);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(11))) {// pt-PT

                    Cell cell2 = row3.createCell(12);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(12))) {// ro-RO

                    Cell cell2 = row3.createCell(13);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(13))) {// ja-JP

                    Cell cell2 = row3.createCell(14);
                    cell2.setCellValue(value);
                }

            }


            //遍历每个语言 输出appMessage文本
            Row row4 = sheet.createRow(rowNum++);
            Cell cell4 = row4.createCell(0);
            cell4.setCellValue(code);
            Cell cellAppMessage = row4.createCell(1);
            cellAppMessage.setCellValue("app_message");

            for (String lang : appMessageMap.keySet()) {
                String value = appMessageMap.get(lang);

                if (Objects.equals(lang, langList.get(1))) {// zh-CN

                    Cell cell2 = row4.createCell(2);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(2))) {// en-US

                    Cell cell2 = row4.createCell(3);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(3))) {// de-DE

                    Cell cell2 = row4.createCell(4);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(4))) {// fr-FR

                    Cell cell2 = row4.createCell(5);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(5))) {// th-TH

                    Cell cell2 = row4.createCell(6);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(6))) {// es-ES

                    Cell cell2 = row4.createCell(7);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(7))) {// pl-PL

                    Cell cell2 = row4.createCell(8);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(8))) {// it-IT

                    Cell cell2 = row4.createCell(9);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(9))) {// nl-NL

                    Cell cell2 = row4.createCell(10);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(10))) {// zh-TW

                    Cell cell2 = row4.createCell(11);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(11))) {// pt-PT

                    Cell cell2 = row4.createCell(12);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(12))) {// ro-RO

                    Cell cell2 = row4.createCell(13);
                    cell2.setCellValue(value);
                }

                if (Objects.equals(lang, langList.get(13))) {// ja-JP

                    Cell cell2 = row4.createCell(14);
                    cell2.setCellValue(value);
                }

            }


        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream("D:/ST000056/Desktop/language/原始数据库多语言/push_message_detail.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();

        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = "D:/ST000056/Desktop/language/原始数据库多语言/push_message_detail.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = "D:/ST000056/Desktop/language/1222/2026_0107_第四次导出_与第三次比较/push_message_detail.xlsx";

        //输出位置
        String outputAdd = "D:/ST000056/Desktop/language/原始数据库多语言/push_message_detail_add.xlsx";
        String outputUpdate = "D:/ST000056/Desktop/language/原始数据库多语言/push_message_detail_update.xlsx";

        comparePermissionAndPushExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        comparePermissionAndPushExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);
    }


    //==============================4.执行多次 比较新旧的错误码excel文档 新旧的数据库错误码excel文档 导出新增的key======================
    /** 比较 energy 数据库  以及错误码 新老全量  导出新增的key*/
    @Test
    void compareExcelOutPutAdd(String oldExcelFilePath, String NewExcelFilePath, String outPutPath) throws IOException {

        OutputStream output = new FileOutputStream(outPutPath);



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
            headerCell1.setCellValue(langList.get(0));
            Cell headerCell2 = headerRow.createCell(1);// zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(2);// en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(3);// de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(4);// fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(5);// th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(6);// es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(7);// pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(8);// it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(9);// nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(10);// zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(11);// pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(12);// ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(13);// ja-JP
            headerCell14.setCellValue(langList.get(13));

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
                    if (newRow.getCell(1) != null) {// zh-CN
                        cell2.setCellValue(newRow.getCell(1).getStringCellValue());
                    }



                    Cell cell3 = row.createCell(2);
                    if (newRow.getCell(2) != null) {// en-US
                        cell3.setCellValue(newRow.getCell(2).getStringCellValue());
                    }



                    Cell cell4 = row.createCell(3);
                    if (newRow.getCell(3) != null) {// de-DE
                        cell4.setCellValue(newRow.getCell(3).getStringCellValue());
                    }



                    Cell cell5 = row.createCell(4);
                    if (newRow.getCell(4) != null) {// fr-FR
                        cell5.setCellValue(newRow.getCell(4).getStringCellValue());
                    }



                    Cell cell6 = row.createCell(5);
                    if (newRow.getCell(5) != null) {// th-TH
                        cell6.setCellValue(newRow.getCell(5).getStringCellValue());
                    }



                    Cell cell7 = row.createCell(6);
                    if (newRow.getCell(6) != null) {// es-ES
                        cell7.setCellValue(newRow.getCell(6).getStringCellValue());
                    }



                    Cell cell8 = row.createCell(7);
                    if (newRow.getCell(7) != null) {// pl-PL
                        cell8.setCellValue(newRow.getCell(7).getStringCellValue());
                    }



                    Cell cell9 = row.createCell(8);
                    if (newRow.getCell(8) != null) {// it-IT
                        cell9.setCellValue(newRow.getCell(8).getStringCellValue());
                    }

                    Cell cell10 = row.createCell(9);
                    if (newRow.getCell(9) != null) {// nl-NL
                        cell10.setCellValue(newRow.getCell(9).getStringCellValue());
                    }

                    Cell cell11 = row.createCell(10);
                    if (newRow.getCell(10) != null) {// zh-TW
                        cell11.setCellValue(newRow.getCell(10).getStringCellValue());
                    }

                    Cell cell12 = row.createCell(11);
                    if (newRow.getCell(11) != null) {// pt-PT
                        cell12.setCellValue(newRow.getCell(11).getStringCellValue());
                    }

                    Cell cell13 = row.createCell(12);
                    if (newRow.getCell(12) != null) {// ro-RO
                        cell13.setCellValue(newRow.getCell(12).getStringCellValue());
                    }

                    Cell cell14 = row.createCell(13);
                    if (newRow.getCell(13) != null) {// ja-JP
                        cell14.setCellValue(newRow.getCell(13).getStringCellValue());
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


    /** 比较 permission、push 数据库 新老全量  导出新增的key*/
    @Test
    void comparePermissionAndPushExcelOutPutAdd(String oldExcelFilePath, String NewExcelFilePath, String outPutPath) throws IOException {

        OutputStream output = new FileOutputStream(outPutPath);



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
            headerCell1.setCellValue(langList.get(0));
            Cell headerCellField = headerRow.createCell(1);
            headerCellField.setCellValue("field");
            Cell headerCell2 = headerRow.createCell(2);// zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(3);// en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(4);// de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(5);// fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(6);// th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(7);// es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(8);// pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(9);// it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(10);// nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(11);// zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(12);// pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(13);// ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(14);// ja-JP
            headerCell14.setCellValue(langList.get(13));

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

                //第二列为 字段类型
                Cell newFieldKeyCell = newRow.getCell(1);
                String newField = newFieldKeyCell.getStringCellValue();

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

                        //第一列为 字段类型
                        Cell oldFieldCell = oldRow.getCell(1);
                        String oldField = oldFieldCell == null ? "" : oldFieldCell.getStringCellValue();

                        if (Objects.equals(newKey, oldKey) && Objects.equals(oldField, newField)) {
                            isAdd = false;
                        }

                    }
                }


                if (isAdd) {

                    Row row = sheet.createRow(rowNum++);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(newKey);

                    Cell cellField = row.createCell(1);
                    cellField.setCellValue(newField);

                    Cell cell2 = row.createCell(2);// zh-CN
                    if (newRow.getCell(2) != null) {
                        cell2.setCellValue(newRow.getCell(2).getStringCellValue());
                    }



                    Cell cell3 = row.createCell(3);// en-US
                    if (newRow.getCell(3) != null) {
                        cell3.setCellValue(newRow.getCell(3).getStringCellValue());
                    }



                    Cell cell4 = row.createCell(4);// de-DE
                    if (newRow.getCell(4) != null) {
                        cell4.setCellValue(newRow.getCell(4).getStringCellValue());
                    }



                    Cell cell5 = row.createCell(5);// fr-FR
                    if (newRow.getCell(5) != null) {
                        cell5.setCellValue(newRow.getCell(5).getStringCellValue());
                    }



                    Cell cell6 = row.createCell(6);// th-TH
                    if (newRow.getCell(6) != null) {
                        cell6.setCellValue(newRow.getCell(6).getStringCellValue());
                    }



                    Cell cell7 = row.createCell(7);// es-ES
                    if (newRow.getCell(7) != null) {
                        cell7.setCellValue(newRow.getCell(7).getStringCellValue());
                    }



                    Cell cell8 = row.createCell(8);// pl-PL
                    if (newRow.getCell(8) != null) {
                        cell8.setCellValue(newRow.getCell(8).getStringCellValue());
                    }



                    Cell cell9 = row.createCell(9);// it-IT
                    if (newRow.getCell(9) != null) {
                        cell9.setCellValue(newRow.getCell(9).getStringCellValue());
                    }

                    Cell cell10 = row.createCell(10);// nl-NL
                    if (newRow.getCell(10) != null) {
                        cell10.setCellValue(newRow.getCell(10).getStringCellValue());
                    }

                    Cell cell11 = row.createCell(11);// zh-TW
                    if (newRow.getCell(11) != null) {
                        cell11.setCellValue(newRow.getCell(11).getStringCellValue());
                    }

                    Cell cell12 = row.createCell(12);// pt-PT
                    if (newRow.getCell(12) != null) {
                        cell12.setCellValue(newRow.getCell(12).getStringCellValue());
                    }

                    Cell cell13 = row.createCell(13);// ro-RO
                    if (newRow.getCell(13) != null) {
                        cell13.setCellValue(newRow.getCell(13).getStringCellValue());
                    }

                    Cell cell14 = row.createCell(14);// ja-JP
                    if (newRow.getCell(14) != null) {
                        cell14.setCellValue(newRow.getCell(14).getStringCellValue());
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



    //==============================4.5 执行多次 比较新旧的错误码excel文档 新旧的数据库错误码excel文档 导出修改的key======================
    /** 比较 energy 数据库  以及错误码 新老全量  导出修改的key*/
    @Test
    void compareExcelOutPutUpdate(String oldExcelFilePath, String NewExcelFilePath, String outPutPath) throws IOException {


        OutputStream output = new FileOutputStream(outPutPath);



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
            headerCell1.setCellValue(langList.get(0));
            Cell headerCell2 = headerRow.createCell(1);// zh-CN
            headerCell2.setCellValue(langList.get(1));
            Cell headerCell3 = headerRow.createCell(2);// en-US
            headerCell3.setCellValue(langList.get(2));
            Cell headerCell4 = headerRow.createCell(3);// de-DE
            headerCell4.setCellValue(langList.get(3));
            Cell headerCell5 = headerRow.createCell(4);// fr-FR
            headerCell5.setCellValue(langList.get(4));
            Cell headerCell6 = headerRow.createCell(5);// th-TH
            headerCell6.setCellValue(langList.get(5));
            Cell headerCell7 = headerRow.createCell(6);// es-ES
            headerCell7.setCellValue(langList.get(6));
            Cell headerCell8 = headerRow.createCell(7);// pl-PL
            headerCell8.setCellValue(langList.get(7));
            Cell headerCell9 = headerRow.createCell(8);// it-IT
            headerCell9.setCellValue(langList.get(8));
            Cell headerCell10 = headerRow.createCell(9);// nl-NL
            headerCell10.setCellValue(langList.get(9));
            Cell headerCell11 = headerRow.createCell(10);// zh-TW
            headerCell11.setCellValue(langList.get(10));
            Cell headerCell12 = headerRow.createCell(11);// pt-PT
            headerCell12.setCellValue(langList.get(11));
            Cell headerCell13 = headerRow.createCell(12);// ro-RO
            headerCell13.setCellValue(langList.get(12));
            Cell headerCell14 = headerRow.createCell(13);// ja-JP
            headerCell14.setCellValue(langList.get(13));

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

                //第二列为中文文本
                Cell newValueCell = newRow.getCell(1);
                String newValue = newValueCell.getStringCellValue();

                //加标记 false就是没有修改的  true就是有修改的
                boolean isUpdate = false;

                if (oldSheet != null) {

                    //和旧文档比较
                    for (Row oldRow : oldSheet) {
                        if (oldRow.getRowNum() == 0) {
                            continue;
                        }
                        //第一列为 key
                        Cell oldKeyCell = oldRow.getCell(0);
                        String oldKey = oldKeyCell == null ? "" : oldKeyCell.getStringCellValue();

                        //第二列为中文文本
                        Cell oldValueCell = oldRow.getCell(1);
                        String oldValue = oldValueCell.getStringCellValue();

                        //比较第二列的中文 如果不一致则输出
                        if (Objects.equals(newKey, oldKey) && !Objects.equals(newValue, oldValue)) {
                            isUpdate = true;
                        }

                    }
                }


                if (isUpdate) {

                    Row row = sheet.createRow(rowNum++);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(newKey);

                    Cell cell2 = row.createCell(1);
                    if (newRow.getCell(1) != null) {// zh-CN
                        cell2.setCellValue(newRow.getCell(1).getStringCellValue());
                    }



                    Cell cell3 = row.createCell(2);
                    if (newRow.getCell(2) != null) {// en-US
                        cell3.setCellValue(newRow.getCell(2).getStringCellValue());
                    }



                    Cell cell4 = row.createCell(3);
                    if (newRow.getCell(3) != null) {// de-DE
                        cell4.setCellValue(newRow.getCell(3).getStringCellValue());
                    }



                    Cell cell5 = row.createCell(4);
                    if (newRow.getCell(4) != null) {// fr-FR
                        cell5.setCellValue(newRow.getCell(4).getStringCellValue());
                    }



                    Cell cell6 = row.createCell(5);
                    if (newRow.getCell(5) != null) {// th-TH
                        cell6.setCellValue(newRow.getCell(5).getStringCellValue());
                    }



                    Cell cell7 = row.createCell(6);
                    if (newRow.getCell(6) != null) {// es-ES
                        cell7.setCellValue(newRow.getCell(6).getStringCellValue());
                    }



                    Cell cell8 = row.createCell(7);
                    if (newRow.getCell(7) != null) {// pl-PL
                        cell8.setCellValue(newRow.getCell(7).getStringCellValue());
                    }



                    Cell cell9 = row.createCell(8);
                    if (newRow.getCell(8) != null) {// it-IT
                        cell9.setCellValue(newRow.getCell(8).getStringCellValue());
                    }

                    Cell cell10 = row.createCell(9);
                    if (newRow.getCell(9) != null) {// nl-NL
                        cell10.setCellValue(newRow.getCell(9).getStringCellValue());
                    }

                    Cell cell11 = row.createCell(10);
                    if (newRow.getCell(10) != null) {// zh-TW
                        cell11.setCellValue(newRow.getCell(10).getStringCellValue());
                    }

                    Cell cell12 = row.createCell(11);
                    if (newRow.getCell(11) != null) {// pt-PT
                        cell12.setCellValue(newRow.getCell(11).getStringCellValue());
                    }

                    Cell cell13 = row.createCell(12);
                    if (newRow.getCell(12) != null) {// ro-RO
                        cell13.setCellValue(newRow.getCell(12).getStringCellValue());
                    }

                    Cell cell14 = row.createCell(13);
                    if (newRow.getCell(13) != null) {// ja-JP
                        cell14.setCellValue(newRow.getCell(13).getStringCellValue());
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


    /** 比较 permission、push 数据库 新老全量  导出修改的key*/
    @Test
    void comparePermissionAndPushExcelOutPutUpdate(String oldExcelFilePath, String NewExcelFilePath, String outPutPath) throws IOException {


        OutputStream output = new FileOutputStream(outPutPath);



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
            Cell headerCellField = headerRow.createCell(1);
            headerCellField.setCellValue("field");
            Cell headerCell2 = headerRow.createCell(2);
            headerCell2.setCellValue("zh-CN");
            Cell headerCell3 = headerRow.createCell(3);
            headerCell3.setCellValue("en-US");
            Cell headerCell4 = headerRow.createCell(4);
            headerCell4.setCellValue("de-DE");
            Cell headerCell5 = headerRow.createCell(5);
            headerCell5.setCellValue("fr-FR");
            Cell headerCell6 = headerRow.createCell(6);
            headerCell6.setCellValue("th-TH");
            Cell headerCell7 = headerRow.createCell(7);
            headerCell7.setCellValue("es-ES");
            Cell headerCell8 = headerRow.createCell(8);
            headerCell8.setCellValue("pl-PL");
            Cell headerCell9 = headerRow.createCell(9);
            headerCell9.setCellValue("it-IT");
            Cell headerCell10 = headerRow.createCell(10);
            headerCell10.setCellValue("nl-NL");
            Cell headerCell11 = headerRow.createCell(11);
            headerCell11.setCellValue("zh-TW");
            Cell headerCell12 = headerRow.createCell(12);
            headerCell12.setCellValue("pt-PT");
            Cell headerCell13 = headerRow.createCell(13);
            headerCell13.setCellValue("ro-RO");
            Cell headerCell14 = headerRow.createCell(14);
            headerCell14.setCellValue("ja-JP");

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

                //第二列为 字段类型
                Cell newFieldKeyCell = newRow.getCell(1);
                String newField = newFieldKeyCell.getStringCellValue();

                //第三列为中文文本
                Cell newValueCell = newRow.getCell(2);
                String newValue = newValueCell.getStringCellValue();

                //加标记 false就是没有修改的  true就是有修改的
                boolean isUpdate = false;

                if (oldSheet != null) {

                    //和旧文档比较
                    for (Row oldRow : oldSheet) {
                        if (oldRow.getRowNum() == 0) {
                            continue;
                        }
                        //第一列为 key
                        Cell oldKeyCell = oldRow.getCell(0);
                        String oldKey = oldKeyCell == null ? "" : oldKeyCell.getStringCellValue();

                        //第二列为 字段类型
                        Cell oldFieldCell = oldRow.getCell(1);
                        String oldField = oldFieldCell == null ? "" : oldFieldCell.getStringCellValue();

                        //第三列为中文文本
                        Cell oldValueCell = oldRow.getCell(2);
                        String oldValue = oldValueCell.getStringCellValue();

                        //比较第三列的中文 如果不一致则输出
                        if (Objects.equals(newKey, oldKey) && Objects.equals(newField, oldField)   && !Objects.equals(newValue, oldValue)) {
                            isUpdate = true;
                        }

                    }
                }


                if (isUpdate) {

                    Row row = sheet.createRow(rowNum++);
                    Cell cell1 = row.createCell(0);
                    cell1.setCellValue(newKey);

                    Cell cellField = row.createCell(1);
                    cellField.setCellValue(newField);

                    Cell cell2 = row.createCell(2);
                    if (newRow.getCell(2) != null) {
                        cell2.setCellValue(newRow.getCell(2).getStringCellValue());
                    }



                    Cell cell3 = row.createCell(3);
                    if (newRow.getCell(3) != null) {
                        cell3.setCellValue(newRow.getCell(3).getStringCellValue());
                    }



                    Cell cell4 = row.createCell(4);
                    if (newRow.getCell(4) != null) {
                        cell4.setCellValue(newRow.getCell(4).getStringCellValue());
                    }



                    Cell cell5 = row.createCell(5);
                    if (newRow.getCell(5) != null) {
                        cell5.setCellValue(newRow.getCell(5).getStringCellValue());
                    }



                    Cell cell6 = row.createCell(6);
                    if (newRow.getCell(6) != null) {
                        cell6.setCellValue(newRow.getCell(6).getStringCellValue());
                    }



                    Cell cell7 = row.createCell(7);
                    if (newRow.getCell(7) != null) {
                        cell7.setCellValue(newRow.getCell(7).getStringCellValue());
                    }



                    Cell cell8 = row.createCell(8);
                    if (newRow.getCell(8) != null) {
                        cell8.setCellValue(newRow.getCell(8).getStringCellValue());
                    }



                    Cell cell9 = row.createCell(9);
                    if (newRow.getCell(9) != null) {
                        cell9.setCellValue(newRow.getCell(9).getStringCellValue());
                    }

                    Cell cell10 = row.createCell(10);
                    if (newRow.getCell(10) != null) {
                        cell10.setCellValue(newRow.getCell(10).getStringCellValue());
                    }

                    Cell cell11 = row.createCell(11);
                    if (newRow.getCell(11) != null) {
                        cell11.setCellValue(newRow.getCell(11).getStringCellValue());
                    }

                    Cell cell12 = row.createCell(12);
                    if (newRow.getCell(12) != null) {
                        cell12.setCellValue(newRow.getCell(12).getStringCellValue());
                    }

                    Cell cell13 = row.createCell(13);
                    if (newRow.getCell(13) != null) {
                        cell13.setCellValue(newRow.getCell(13).getStringCellValue());
                    }

                    Cell cell14 = row.createCell(14);
                    if (newRow.getCell(14) != null) {
                        cell14.setCellValue(newRow.getCell(14).getStringCellValue());
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


    //------------------------------------------------导入------------------------------------------------------------//

    //==============================5. excel 导入到 properties======================

    /** 读取 Excel 文档 写入信息到 properties 新版*/
    @Test
    void ExcelToPropertiesNew() throws IOException {

        //导入的 Excel文件
        String excelFilePath = "D:/ST000056/Desktop/language/1222/2026-01-14后端异常码翻译内容.xlsx";


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

                    //老文件路径
                    String oldPropertiesFilePath = "D:/ST000056/Desktop/language/原始错误码properties/" + oldPropName;
                    //不存在名称就跳过
                    if(!checkFileExists(oldPropertiesFilePath)) {
                        continue;
                    }

                    //老版 properties文件
                    PropertiesUtils oldProperties = new PropertiesUtils();
                    oldProperties.loadProperties
                            (oldPropertiesFilePath);

                    //老版的 key名称全集
                    List<String> oldKeyNames = oldProperties.getKeyList().stream().map(String ::valueOf).collect(Collectors.toList());


                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        //新的文档 key 不匹配旧版的所有key 说明是新增的  加入
                        boolean isAllNotMatch = true;

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

                        if (Objects.equals(lang, "zh-TW")) {
                            valueCell = row.getCell(10);
                        }

                        if (Objects.equals(lang, "pt-PT")) {
                            valueCell = row.getCell(11);
                        }

                        if (Objects.equals(lang, "ro-RO")) {
                            valueCell = row.getCell(12);
                        }

                        if (Objects.equals(lang, "ja-JP")) {
                            valueCell = row.getCell(13);
                        }

                        //遍历旧文档key 比对合入
                        for (String oldKey : oldKeyNames) {

                            properties.setProperty(oldKey, oldProperties.getProperty(oldKey));

                            if (keyCell != null && valueCell != null) {
                                String key = keyCell.getStringCellValue();
                                String value = valueCell.getStringCellValue();

                                //如果有匹配的key 那么使用Excel文件中的value值 否则还是使用原来的值
                                if (Objects.equals(oldKey, key)) {
                                    System.out.println("文本有变更，key ：" + key + " newValue : " + value + " oldValue :" + oldProperties.getProperty(oldKey));
                                    properties.setProperty(oldKey, value);

                                    //旧数据赋值 防止重复覆盖
                                    oldProperties.setProperty(oldKey, value);
                                    isAllNotMatch = false;
                                }
                            }

                        }

                        //新的文档 key 不匹配旧版的所有key 说明是新增的  加入
                        if (isAllNotMatch) {
                            if (keyCell != null && valueCell != null) {
                                String key = keyCell.getStringCellValue();
                                String value = valueCell.getStringCellValue();
                                System.out.println("旧文档不存在的key 新文档存在的key，key ：" + key + " newValue : " + value );
                                properties.setProperty(key, value);
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

        String excelFilePath = "D:/ST000056/Desktop/language/0704/2025-07-15后端数据库翻译内容.xlsx";

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


        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }

    }

    /** 读取 Excel 文档 写入信息到 多个数据表中 生成对应表的sql*/
    @Test
    void ExcelToMultipleConfigLanguage() throws IOException {

        String excelFilePath = "D:/ST000056/Desktop/language/1222/2026-01-14后端数据库翻译内容.xlsx";

        String regex = "\\b[a-z]{2}-[A-Z]{2}\\b"; // 正则表达式匹配 "xx-XX" 格式的字符串


        List<String> insertSqlList = new ArrayList<>();

        try {
            FileInputStream excelFile = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(excelFile);
            int sheetCount = workbook.getNumberOfSheets();

            // 遍历每个工作表并获取名称 每个工作表的名称表示数据库里面的一张表
            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                //截取app 名称字段
                String app = getFirstWord(sheetName);

                StringBuffer insertSql = new StringBuffer();
                StringBuffer deleteSql = new StringBuffer();
                StringBuffer updateSql = new StringBuffer();

                //energy 数据库
                if (Objects.equals(sheetName, "energy_config_language")) {
                    insertSql.append(" INSERT INTO "+ sheetName +" (app, `type`, code, lang, `text`) VALUES\n ");
                    deleteSql.append(" delete from "+ sheetName + " where  ");

                    Map<Integer, String> langMap = new HashMap<>();

                    //每个sheet页数据处理
                    for (Row row : sheet) {

                        //设置各语言
                        if (row.getRowNum() == 0) {
                            for (int m = 2 ; m < row.getLastCellNum() ; m++) {
                                Cell langCell = row.getCell(m);
                                String lang = langCell.getStringCellValue();

                                boolean isValid = lang.matches(regex);
                                //不满足 xx-XX格式 直接跳过
                                if (isValid) {
                                    langMap.put(m, lang);
                                }
                            }
                            continue;
                        }

                        //第一列是type
                        Cell typeCell = row.getCell(0);

                        //第二列是code
                        Cell keyCell = row.getCell(1);

                        //第三列及以后都是对应的多语言翻译文本
                        for (int j = 2 ; j < row.getLastCellNum() ; j++) {

                            //多语言code
                            Cell textCell = row.getCell(j);

                            if (typeCell != null && keyCell != null) {
                                String type = typeCell.getStringCellValue();
                                DataFormatter formatter = new DataFormatter(); // 保留单元格原始格式
                                String code = formatter.formatCellValue(keyCell); // 无论数字、日期，均转为字符串
                                //String code = keyCell.getStringCellValue();
                                String text = "";
                                if (textCell != null) {
                                    text = textCell.getStringCellValue();
                                }

                                if (Objects.equals(text, "")) {
                                    continue;
                                }

                                String lang = langMap.get(j);

                                //拼接插入sql语句
                                insertSql.append(" ( '").append(app).append("',").
                                        append("\"").append(type).append("\",").
                                        append("\"").append(code).append("\",").
                                        append("\"").append(lang).append("\",").
                                        append("\"").append(text).append("\")\n,");

                                //拼接删除sql语句
                                deleteSql.append("( code = '").append(code).append("' and ").append(" `type` = '" ).append(type).
                                        append("' and").append(" app = '").append(app).append("' and lang = '").append(lang).append("' )\n or");
                            }

                        }


                    }

                }

                //permission 数据库
                if (Objects.equals(sheetName, "permission_config_language")) {
                    insertSql.append(" INSERT INTO "+ sheetName +" (app, `type`, code, lang, `text`) VALUES\n ");
                    deleteSql.append(" delete from "+ sheetName + " where  ");

                    Map<Integer, String> langMap = new HashMap<>();

                    //每个sheet页数据处理
                    for (Row row : sheet) {

                        //设置各语言
                        if (row.getRowNum() == 0) {
                            for (int m = 3 ; m < row.getLastCellNum() ; m++) {
                                Cell langCell = row.getCell(m);
                                String lang = langCell.getStringCellValue();

                                boolean isValid = lang.matches(regex);
                                //不满足 xx-XX格式 直接跳过
                                if (isValid) {
                                    langMap.put(m, lang);
                                }
                            }
                            continue;
                        }

                        //第一列是字段
                        Cell fieldCell = row.getCell(0);

                        //第二列是type
                        Cell typeCell = row.getCell(1);

                        //第三列是code
                        Cell keyCell = row.getCell(2);

                        //第四列及以后都是对应的多语言翻译文本
                        for (int j = 3 ; j < row.getLastCellNum() ; j++) {

                            //多语言code
                            Cell textCell = row.getCell(j);

                            if (typeCell != null && keyCell != null) {
                                String type = typeCell.getStringCellValue();
                                DataFormatter formatter = new DataFormatter(); // 保留单元格原始格式
                                String code = formatter.formatCellValue(keyCell); // 无论数字、日期，均转为字符串
                                String field = fieldCell.getStringCellValue();
                                String text = "";
                                if (textCell != null) {
                                    text = textCell.getStringCellValue();
                                }

                                if (Objects.equals(text, "")) {
                                    continue;
                                }

                                String lang = langMap.get(j);

                                if (Objects.equals(field, "description")) {//description字段作为修改加入
                                    //拼接修改sql语句
                                    updateSql.append(" update permission_config_language set ");
                                    updateSql.append(" ").append(field).append(" = '").append(text).append("'").
                                            append(" where code = '").append(code)
                                            .append("' and type = '").append(type)
                                            .append("' and app = '").append(app)
                                            .append("' and lang = '").append(lang).append("';\n");
                                    continue;

                                }

                                //拼接插入sql语句
                                insertSql.append(" ( '").append(app).append("',").
                                        append("\"").append(type).append("\",").
                                        append("\"").append(code).append("\",").
                                        append("\"").append(lang).append("\",").
                                        append("\"").append(text).append("\")\n,");

                                //拼接删除sql语句
                                deleteSql.append("( code = '").append(code).append("' and ").append(" `type` = '" ).append(type).
                                        append("' and").append(" app = '").append(app).append("' and lang = '").append(lang).append("' )\n or");


                            }

                        }


                    }
                }


                //push 数据库
                if (Objects.equals(sheetName, "push_message_detail")) {

                    insertSql.append(" INSERT INTO "+ sheetName +" (parent_id, `language`, title) VALUES\n ");
                    deleteSql.append(" delete from "+ sheetName + " where  ");

                    Map<Integer, String> langMap = new HashMap<>();

                    //每个sheet页数据处理
                    for (Row row : sheet) {

                        //设置各语言
                        if (row.getRowNum() == 0) {
                            for (int m = 2 ; m < row.getLastCellNum() ; m++) {
                                Cell langCell = row.getCell(m);
                                String lang = langCell.getStringCellValue();

                                boolean isValid = lang.matches(regex);
                                //不满足 xx-XX格式 直接跳过
                                if (isValid) {
                                    langMap.put(m, lang);
                                }
                            }
                            continue;
                        }

                        //第一列是字段
                        Cell fieldCell = row.getCell(0);

                        //第二列是code
                        Cell keyCell = row.getCell(1);

                        //第三列及以后都是对应的多语言翻译文本
                        for (int j = 2 ; j < row.getLastCellNum() ; j++) {

                            //多语言code
                            Cell textCell = row.getCell(j);

                            if ( keyCell != null) {
                                DataFormatter formatter = new DataFormatter(); // 保留单元格原始格式
                                String code = formatter.formatCellValue(keyCell); // 无论数字、日期，均转为字符串
                                String field = fieldCell.getStringCellValue();
                                String text = "";
                                if (textCell != null) {
                                    text = textCell.getStringCellValue();
                                }

                                if (Objects.equals(text, "")) {
                                    continue;
                                }
                                String lang = langMap.get(j);

                                if (!Objects.equals(field, "title")) {//description字段作为修改加入
                                    //拼接修改sql语句
                                    updateSql.append(" update push_message_detail set ");
                                    updateSql.append(" ").append(field).append(" = '").append(text).append("'").
                                            append(" where parent_id = '").append(code).
                                            append("' and `language` = '").append(lang).append("';\n");
                                    continue;
                                }

                                //拼接插入sql语句
                                insertSql.append(" ( '").append(code).append("',").
                                        append("\"").append(lang).append("\",").
                                        append("\"").append(text).append("\")\n,");

                                //拼接删除sql语句
                                deleteSql.append("( parent_id = '").append(code).append("' and `language` = '").append(lang).append("' )\n or");


                            }

                        }


                    }

                }







                //删除条件删除最后一个or 替换为;
                deleteSql.deleteCharAt(deleteSql.length() - 1);
                deleteSql.setCharAt(deleteSql.length() - 1, ';');
                insertSqlList.add(deleteSql.toString().replace("$", "\\$"));

                //替换最后一个逗号
                insertSql.setCharAt(insertSql.length() - 1, ';');
                insertSqlList.add(insertSql.toString().replace("$", "\\$"));


                //加入修改sql
                insertSqlList.add(updateSql.toString());

            }

            System.out.println(insertSqlList);

            //输出sql
            sqlToTxt(insertSqlList);


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
            String sourceDirPath = "D:/ST000056/Desktop/language/更新后的错误码properties";
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
                        // 解析文件名，例如 cloud[es-ES].properties 转换为 messages_es_ES.properties
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
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(fileName);
        if (m.find()) {
            String locale = m.group(1);
            // 替换为新的文件名格式
            return "messages_" + locale.replace("-", "_") + ".properties";
        }
        return null;
    }



    //========================================ps:单独的截取方法=====================================
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

    /**
     * 校验指定路径是否存在文件
     *
     * @param propertiesFilePath 文件路径
     * @return 如果文件存在返回 true，否则返回 false
     */
    public static boolean checkFileExists(String propertiesFilePath) {
        if (propertiesFilePath == null || propertiesFilePath.isEmpty()) {
            System.err.println("文件路径不能为空");
            return false;
        }

        File file = new File(propertiesFilePath);
        if (!file.exists()) {
            System.err.println("文件不存在: " + propertiesFilePath);
            return false;
        }

        if (!file.isFile()) {
            System.err.println(propertiesFilePath + " 不是一个文件");
            return false;
        }

        System.out.println("文件存在: " + propertiesFilePath);
        return true;
    }

    /**
     * 从下划线分隔的字符串中提取第一个单词
     * @param input 输入字符串（如 "permission_config_language"）
     * @return 第一个单词（如 "permission"），如果输入为null或空则返回空字符串
     */
    public static String getFirstWord(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 找到第一个下划线的位置
        int underscoreIndex = input.indexOf('_');

        // 如果没有下划线，返回整个字符串
        if (underscoreIndex == -1) {
            return input;
        }

        // 截取第一个下划线之前的部分
        return input.substring(0, underscoreIndex);
    }


}
