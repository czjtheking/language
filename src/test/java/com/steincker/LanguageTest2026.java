package com.steincker;

import com.steincker.common.util.LinkedProperties;
import com.steincker.common.util.PropertiesUtils;
import com.steincker.dao.*;
import com.steincker.entity.ConfigLanguage;
import com.steincker.entity.CustomerConfigLanguage;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @ClassName languageTest
 * @Author ST000056
 * @Date 2024-10-19 14:37
 * @Version 1.0
 * @Description
 **/
@SpringBootTest
public class LanguageTest2026 {

    @Resource
    ConfigLanguageMapper configLanguageMapper;

    @Resource
    RegionLanguageMapper regionLanguageMapper;

    @Resource
    PermissionConfigLanguageMapper permissionConfigLanguageMapper;

    @Resource
    PushMessageDetailMapper pushMessageDetailMapper;

    @Resource
    CustomerConfigLanguageMapper customerConfigLanguageMapper;

    /**
     * 语言包文件路径，即存储language历史记录的文件路径，在此处统一修改
     */
    private static final String PREFIX = "D:/MW000242/Desktop";

    /**
     * energy项目的代码存储前缀地址，用于导出多语言时指定需要导出的不同服务的i18n文件
     */
    private static final String ENERGY_PATH = "D:/Code";

    /**
     * permission项目的代码存储前缀地址，用于导出多语言时指定需要导出的不同服务的i18n文件
     */
    private static final String PERMISSION_PATH = "D:/Code";
    /**
     * atmoce项目的代码存储前缀地址，用于导出多语言时指定需要导出的不同服务的i18n文件
     */
    private static final String ATMOCE_PATH = "D:/Code";

    public static final List<String> defaultLangList = List.of("key", "zh-CN", "en-US", "de-DE", "fr-FR",
            "th-TH", "es-ES", "pl-PL", "it-IT", "nl-NL", "zh-TW",
            "pt-PT", "ro-RO", "ja-JP", "sv-SE", "fi-FI");


    //------------------------------------------------导出------------------------------------------------------------//


    //=============================1.将各服务多语言错误码文件 复制重命名到指定文件夹中===================
    @Test
    void propertiesCopyToFileFold() {

        //正则表达式 截取 zh_CN这种
        Pattern pattern = Pattern.compile("messages_([a-z]{2}_[A-Z]{2})\\.properties");

        Map<String, String> servicePath = new HashMap<>();

        //开发
        servicePath.put("energy", ENERGY_PATH + "/steincker-energy/energy-manage-app/src/main/resources/i18n");
        servicePath.put("permission", PERMISSION_PATH + "/steincker-permission/permission-infrastructure/src/main/resources");
//        servicePath.put("customer", "D:/steincker_code/steincker-customer/steincker-customer/customer-content-app/src/main/resources/i18n");
//        servicePath.put("openapi", "D:/steincker_code/moorewatt-openAPI/moorewatt-openapi/openapi-app/src/main/resources/i18n");
//        servicePath.put("cloud", "D:/steincker_code/steincker-cloud/steincker-cloud/steincker-cloud-framework/steincker-cloud-starter-i18n/src/main/resources/i18n-common");
//        servicePath.put("push", "D:/steincker_code/steincker-push/steincker-push/push-message-app/src/main/resources");
//        servicePath.put("system", "D:/steincker_code/steincker-system/steincker-system/system-app/src/main/resources/i18n");
        servicePath.put("ai", ATMOCE_PATH + "/atmoce-ai/i18n/message");


        //线上
//                servicePath.put("energy", "D:/i18/steincker-energy/energy-manage-app/src/main/resources/i18n");
//                servicePath.put("permission", "D:/i18/steincker-permission/permission-infrastructure/src/main/resources");
//                servicePath.put("customer", "D:/i18/steincker-customer/customer-content-app/src/main/resources/i18n");
//                servicePath.put("openapi", "D:/i18/moorewatt-openapi/openapi-app/src/main/resources/i18n");
//                servicePath.put("cloud", "D:/i18/steincker-cloud/steincker-cloud-framework/steincker-cloud-starter-i18n/src/main/resources/i18n-common");
//                servicePath.put("push", "D:/i18/steincker-push/push-message-app/src/main/resources");
//                servicePath.put("system", "D:/i18/steincker-system/system-app/src/main/resources/i18n");
//                servicePath.put("ai", "D:/i18/atmoce-ai/i18n/message");




        // 目标文件夹路径
        String targetDirectory = PREFIX + "/language/原始错误码properties";
        //String targetDirectory = PREFIX + "/language/异常码backups";

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
        File folder = new File(PREFIX + "/language/原始错误码properties");

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
                properties.loadProperties(PREFIX + "/language/原始错误码properties/" + propName);

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
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }



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

                for (int i = 1; i < defaultLangList.size(); i++) {
                    String defaultLang = defaultLangList.get(i);
                    for (String lang : langList) {
                        String value = utilsMap.get(lang).getProperty(key);
                        if (Objects.equals(lang, defaultLang)) {
                            Cell cell2 = row.createCell(i);
                            cell2.setCellValue(value);
                        }
                    }
                }



            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream(PREFIX + "/language/原始错误码properties/error.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿



        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = PREFIX + "/language/原始错误码properties/error.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = PREFIX + "/language/0202/2026_0304_第二次导出_对比第一次/error.xlsx";

        //输出位置_新增
        String outputAdd = PREFIX + "/language/原始错误码properties/error_add.xlsx";
        //输出位置_修改
        String outputUpdate = PREFIX + "/language/原始错误码properties/error_update.xlsx";

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
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }

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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        String defaultLang = defaultLangList.get(i);
                        if (Objects.equals(lang, defaultLang)) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(value);
                        }
                    }

                }



            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream(PREFIX + "/language/原始数据库多语言/energy_config_language.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();



        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = PREFIX + "/language/原始数据库多语言/energy_config_language.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = PREFIX + "/language/0202/2026_0304_第二次导出_对比第一次/energy_config_language.xlsx";

        //输出位置
        String outputAdd = PREFIX + "/language/原始数据库多语言/energy_config_language_add.xlsx";
        String outputUpdate = PREFIX + "/language/原始数据库多语言/energy_config_language_update.xlsx";

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
            for (int i = 0; i <= defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                if (i == 0) {
                    headerCell.setCellValue(defaultLangList.get(i));
                    continue;
                }
                if (i == 1) {
                    headerCell.setCellValue("field");
                    continue;
                }
                headerCell.setCellValue(defaultLangList.get(i - 1));
            }

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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        String defaultLang = defaultLangList.get(i);
                        if (Objects.equals(lang, defaultLang)) {
                            Cell cell1 = row.createCell(i + 1);
                            cell1.setCellValue(value);
                        }
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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        String defaultLang = defaultLangList.get(i);
                        if (Objects.equals(lang, defaultLang)) {
                            Cell cell2 = row2.createCell(i + 1);
                            cell2.setCellValue(value);
                        }
                    }
                }


            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream(PREFIX + "/language/原始数据库多语言/permission_config_language.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();


        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = PREFIX + "/language/原始数据库多语言/permission_config_language.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = PREFIX + "/language/0202/2026_0304_第二次导出_对比第一次/permission_config_language.xlsx";

        //输出位置
        String outputAdd = PREFIX + "/language/原始数据库多语言/permission_config_language_add.xlsx";
        String outputUpdate = PREFIX + "/language/原始数据库多语言/permission_config_language_update.xlsx";

        comparePermissionAndPushExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        comparePermissionAndPushExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);


    }

    /** customer_config_language 国际化数据导出到Excel  新版*/
    @Test
    void customerConfigLanguageToExcelNew() throws IOException {

        customerConfigLanguageMapper.setGroupConcatMaxLen();

        //查询所有多语言
        List<CustomerConfigLanguage> configLanguages = customerConfigLanguageMapper.findAllGroupBy().stream().filter(
                configLanguage -> configLanguage.getText() != null && !configLanguage.getText().isEmpty()).collect(Collectors.toList());


        //按照 type 类型分类
        Map<String, List<CustomerConfigLanguage>> languageMaps = configLanguages.stream()
                .collect(Collectors.groupingBy(
                        CustomerConfigLanguage::getType,
                        Collectors.toList()
                ));

        Set<String> keySet = languageMaps.keySet();

        //创建 Excel 工作簿
        Workbook workbook = new XSSFWorkbook();

        //有多少大分类 就生成多少sheet页
        for (String configType : keySet) {

            //获取该分类的列表
            List<CustomerConfigLanguage> languageMapsByTypeList = languageMaps.get(configType);

            Map<String, CustomerConfigLanguage> languageMapsByTypeMap = languageMapsByTypeList.stream().
                    collect(Collectors.toMap(CustomerConfigLanguage::getCode , configLanguage -> configLanguage));


            Sheet sheet = workbook.createSheet(configType);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }

            int rowNum = 1;

            //遍历每个code
            for (String code : languageMapsByTypeMap.keySet()) {
                CustomerConfigLanguage configLanguage = languageMapsByTypeMap.get(code);

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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        String defaultLang = defaultLangList.get(i);
                        if (Objects.equals(lang, defaultLang)) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(value);
                        }
                    }

                }



            }

        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream(PREFIX + "/language/原始数据库多语言/customer_config_language.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();



        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = PREFIX + "/language/原始数据库多语言/customer_config_language.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = PREFIX + "/language/0202/2026_0304_第二次导出_对比第一次/customer_config_language.xlsx";

        //输出位置
        String outputAdd = PREFIX + "/language/原始数据库多语言/customer_config_language.xlsx";
        String outputUpdate = PREFIX + "/language/原始数据库多语言/customer_config_language.xlsx";

        compareExcelOutPutAdd(oldExcelFilePath, NewExcelFilePath, outputAdd);
        compareExcelOutPutUpdate(oldExcelFilePath, NewExcelFilePath, outputUpdate);


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
        for (int i = 0; i <= defaultLangList.size(); i++) {
            Cell headerCell = headerRow.createCell(i);
            if (i == 0) {
                headerCell.setCellValue(defaultLangList.get(i));
                continue;
            }
            if (i == 1) {
                headerCell.setCellValue("field");
                continue;
            }
            headerCell.setCellValue(defaultLangList.get(i - 1));
        }

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

                for (int i = 1; i < defaultLangList.size(); i++) {
                    String defaultLang = defaultLangList.get(i);
                    if (Objects.equals(lang, defaultLang)) {
                        Cell cell1 = row.createCell(i + 1);
                        cell1.setCellValue(value);
                    }
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

                for (int i = 1; i < defaultLangList.size(); i++) {
                    String defaultLang = defaultLangList.get(i);
                    if (Objects.equals(lang, defaultLang)) {
                        Cell cell2 = row2.createCell(i + 1);
                        cell2.setCellValue(value);
                    }
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

                for (int i = 1; i < defaultLangList.size(); i++) {
                    String defaultLang = defaultLangList.get(i);
                    if (Objects.equals(lang, defaultLang)) {
                        Cell cell2 = row3.createCell(i + 1);
                        cell2.setCellValue(value);
                    }
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

                for (int i = 1; i < defaultLangList.size(); i++) {
                    String defaultLang = defaultLangList.get(i);
                    if (Objects.equals(lang, defaultLang)) {
                        Cell cell2 = row4.createCell(i + 1);
                        cell2.setCellValue(value);
                    }
                }

            }


        }

        // 写入 Excel 文件
        try (OutputStream output = new FileOutputStream(PREFIX + "/language/原始数据库多语言/push_message_detail.xlsx")) {
            workbook.write(output);
        }

        // 关闭工作簿
        workbook.close();

        //导出文档成功后 继续执行对比旧文件，获取到新增的key和修改的key
        //新导出的 Excel文件
        String NewExcelFilePath = PREFIX + "/language/原始数据库多语言/push_message_detail.xlsx";

        //旧的全量导出的 Excel文件
        String oldExcelFilePath = PREFIX + "/language/0202/2026_0304_第二次导出_对比第一次/push_message_detail.xlsx";

        //输出位置
        String outputAdd = PREFIX + "/language/原始数据库多语言/push_message_detail_add.xlsx";
        String outputUpdate = PREFIX + "/language/原始数据库多语言/push_message_detail_update.xlsx";

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
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }

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

                    for (int i = 1; i < defaultLangList.size(); i++) {//输出新增的各key的翻译文本
                        Cell cell = row.createCell(i);
                        if (newRow.getCell(i) != null) {
                            cell.setCellValue(newRow.getCell(i).getStringCellValue());
                        }
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

    /** 比较 customer 数据库  以及错误码 新老全量  导出新增的key*/
    @Test
    void compareCustomerExcelOutPutAdd(String oldExcelFilePath, String NewExcelFilePath, String outPutPath) throws IOException {
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
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }

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

                    for (int i = 1; i < defaultLangList.size(); i++) {//输出新增的各key的翻译文本
                        Cell cell = row.createCell(i);
                        if (newRow.getCell(i) != null) {
                            cell.setCellValue(newRow.getCell(i).getStringCellValue());
                        }
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
            for (int i = 0; i <= defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                if (i == 0) {
                    headerCell.setCellValue(defaultLangList.get(i));
                    continue;
                }
                if (i == 1) {
                    headerCell.setCellValue("field");
                    continue;
                }
                headerCell.setCellValue(defaultLangList.get(i - 1));
            }


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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        Cell cell2 = row.createCell(i + 1);
                        if (newRow.getCell(i + 1) != null) {
                            cell2.setCellValue(newRow.getCell(i + 1).getStringCellValue());
                        }
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
            for (int i = 0; i < defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(defaultLangList.get(i));
            }


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

                    for (int i = 1; i < defaultLangList.size(); i++) {//输出新增的各key的翻译文本
                        Cell cell = row.createCell(i);
                        if (newRow.getCell(i) != null) {
                            cell.setCellValue(newRow.getCell(i).getStringCellValue());
                        }
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
            for (int i = 0; i <= defaultLangList.size(); i++) {
                Cell headerCell = headerRow.createCell(i);
                if (i == 0) {
                    headerCell.setCellValue(defaultLangList.get(i));
                    continue;
                }
                if (i == 1) {
                    headerCell.setCellValue("field");
                    continue;
                }
                headerCell.setCellValue(defaultLangList.get(i - 1));
            }


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

                    for (int i = 1; i < defaultLangList.size(); i++) {
                        Cell cell2 = row.createCell(i + 1);
                        if (newRow.getCell(i + 1) != null) {
                            cell2.setCellValue(newRow.getCell(i + 1).getStringCellValue());
                        }
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
        String excelFilePath = PREFIX + "/language/0202/2026-03-25后端异常码翻译内容.xlsx";


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
                    String propertiesFilePath = PREFIX + "/language/更新后的错误码properties/" + oldPropName;

                    //老文件路径
                    String oldPropertiesFilePath = PREFIX + "/language/原始错误码properties/" + oldPropName;
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

                        for (int x = 1; x < defaultLangList.size(); x++) {
                           String defaultLang = defaultLangList.get(x);
                            if (Objects.equals(lang, defaultLang)) {
                                valueCell = row.getCell(x);
                            }
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

        String excelFilePath = PREFIX + "/language/0202/2025-07-15后端数据库翻译内容.xlsx";

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

        String excelFilePath = PREFIX + "/language/0202/2026-03-25后端数据库翻译内容.xlsx";

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
        String filePath = PREFIX + "/language/sql/tesSql.sql";

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
            String sourceDirPath = PREFIX + "/language/更新后的错误码properties";
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
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fileName);
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

    private static final String CHINESE_COL_NAME = "zh-CN";
    private static final String SEQ_COL_NAME = "序号";

    /*
    输出的将翻译同步后的全量表的文件路径定义，可修改
     */
    private static final String OUTPUT_PATH = "D:/MW000242/Desktop/全量表_更新后.xlsx";


    @Test
    public void testSyncTranslation() throws IOException {
        // aPath路径为去重后的已经翻译的表，即翻译公司返回给我们的表
        String aPath = "src/test/resources/去重表_已翻译.xlsx";
        // bPath为上一次的全量表，未去重，部分多语言可能还未修改
        String bPath = "src/test/resources/全量表_旧.xlsx";
        String outputPath = "src/test/resources/全量表_更新后04211136.xlsx";
        int headerRowIndex = 1; // 第二行为标题行（0-based）

        // 执行同步（所有sheet）
        syncAllSheets(aPath, bPath, outputPath,headerRowIndex);

        // 简单验证输出文件存在
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(outputPath))) {
            assertTrue(wb.getNumberOfSheets() > 0);
        }
        System.out.println("所有sheet同步完成，输出文件：" + outputPath);
    }

    /**
     * 同步两个工作簿中所有同名的 sheet
     */




    /**
     * 同步单个 sheet 的内容（修改 sheetB）
     * @param aPath A表sheet
     * @param bPath B表sheet
     * @param headerRowIndex 标题行索引（0-based，例如第二行传1）
     */
    private static void syncAllSheets(String aPath, String bPath, String outputPath, int headerRowIndex) throws IOException {
        System.out.println("开始同步...");
        Workbook wbA = null;
        Workbook wbB = null;
        try {
            wbA = new XSSFWorkbook(new FileInputStream(aPath));
            wbB = new XSSFWorkbook(new FileInputStream(bPath));

            Set<String> sheetNames = new LinkedHashSet<>();
            for (int i = 0; i < wbA.getNumberOfSheets(); i++) sheetNames.add(wbA.getSheetName(i));
            for (int i = 0; i < wbB.getNumberOfSheets(); i++) sheetNames.add(wbB.getSheetName(i));

            for (String sheetName : sheetNames) {
                Sheet sheetA = wbA.getSheet(sheetName);
                Sheet sheetB = wbB.getSheet(sheetName);
                if (sheetA == null || sheetB == null) {
                    System.out.println("跳过 sheet: " + sheetName + " (其中一个表缺失)");
                    continue;
                }
                System.out.println("正在同步 sheet: " + sheetName);
                syncSingleSheet(sheetA, sheetB, headerRowIndex);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                wbB.write(fos);
            }
            System.out.println("同步完成，保存至: " + outputPath);
        } finally {
            if (wbA != null) wbA.close();
            if (wbB != null) wbB.close();
        }
    }

    private static void syncSingleSheet(Sheet sheetA, Sheet sheetB, int headerRowIndex) {
        Row headerA = sheetA.getRow(headerRowIndex);
        Row headerB = sheetB.getRow(headerRowIndex);
        if (headerA == null || headerB == null) {
            throw new IllegalArgumentException("标题行不存在，索引: " + headerRowIndex);
        }

        // 1. 获取A表和B表的所有列名
        Set<String> allCols = new LinkedHashSet<>();
        for (Cell cell : headerB) allCols.add(getCellStringValue(cell).trim());
        for (Cell cell : headerA) allCols.add(getCellStringValue(cell).trim());

        // 2. 将序号列放在第一列（可选，便于查看）
        List<String> orderedCols = new ArrayList<>(allCols);
        orderedCols.remove(SEQ_COL_NAME);
        orderedCols.add(0, SEQ_COL_NAME);

        // 3. 重建B表，使其列顺序与 orderedCols 一致（保留原有数据，新列留空）
        rebuildSheet(sheetB, orderedCols, headerRowIndex);

        // 4. 识别语言列（形如 zh-CN, en-US 等）
        Set<String> languageCols = new HashSet<>();
        Pattern langPattern = Pattern.compile("[a-z]{2}-[A-Z]{2}");
        for (String col : orderedCols) {
            if (langPattern.matcher(col).matches()) {
                languageCols.add(col);
            }
        }
        // 确保中文列也在语言列中（如果不符合正则）
        if (!languageCols.contains(CHINESE_COL_NAME)) {
            languageCols.add(CHINESE_COL_NAME);
        }
        System.out.println("语言列: " + languageCols);

        // 5. 读取A表数据：中文 -> Map<语言列名, 翻译值>
        Map<String, Map<String, String>> aData = readASheetByChinese(sheetA, headerRowIndex, orderedCols, languageCols);

        // 6. 获取B表中中文列索引（重建后列顺序已固定）
        int chineseColIdxB = orderedCols.indexOf(CHINESE_COL_NAME);
        if (chineseColIdxB == -1) {
            throw new IllegalArgumentException("B表缺少中文列: " + CHINESE_COL_NAME);
        }

        // 7. 遍历B表数据行（从 headerRowIndex+1 开始）
        for (int i = headerRowIndex + 1; i <= sheetB.getLastRowNum(); i++) {
            Row rowB = sheetB.getRow(i);
            if (rowB == null) continue;

            String chinese = getCellStringValue(rowB.getCell(chineseColIdxB)).trim();
            if (chinese.isEmpty()) continue;

            Map<String, String> aRow = aData.get(chinese);
            if (aRow == null) {
                System.out.println("警告: 中文 \"" + chinese + "\" 在A表中未找到，跳过该行");
                continue;
            }

            // 只更新语言列
            for (String langCol : languageCols) {
                int colIdx = orderedCols.indexOf(langCol);
                if (colIdx >= 0 && aRow.containsKey(langCol)) {
                    Cell cell = rowB.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellValue(aRow.get(langCol));
                }
            }
        }

        // 8. 按序号排序（可选）
        int seqColIdx = orderedCols.indexOf(SEQ_COL_NAME);
        if (seqColIdx >= 0) {
            sortSheetBySeq(sheetB, seqColIdx, headerRowIndex);
        }
    }

    /**
     * 读取 A 表，返回 中文 -> Map<语言列名, 翻译值>
     */
    private static Map<String, Map<String, String>> readASheetByChinese(Sheet sheetA, int headerRowIndex,
                                                                        List<String> orderedCols, Set<String> languageCols) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Row headerA = sheetA.getRow(headerRowIndex);
        if (headerA == null) return result;

        Map<String, Integer> colIndexA = new HashMap<>();
        for (Cell cell : headerA) {
            colIndexA.put(getCellStringValue(cell).trim(), cell.getColumnIndex());
        }

        Integer chineseColIdxA = colIndexA.get(CHINESE_COL_NAME);
        if (chineseColIdxA == null) {
            throw new IllegalArgumentException("A表缺少中文列: " + CHINESE_COL_NAME);
        }

        // 只关心 languageCols 中在A表实际存在的列
        Map<String, Integer> langColIdxA = new HashMap<>();
        for (String lang : languageCols) {
            if (colIndexA.containsKey(lang)) {
                langColIdxA.put(lang, colIndexA.get(lang));
            }
        }

        for (int i = headerRowIndex + 1; i <= sheetA.getLastRowNum(); i++) {
            Row row = sheetA.getRow(i);
            if (row == null) continue;
            String chinese = getCellStringValue(row.getCell(chineseColIdxA)).trim();
            if (chinese.isEmpty()) continue;

            Map<String, String> langValues = new HashMap<>();
            for (Map.Entry<String, Integer> entry : langColIdxA.entrySet()) {
                String lang = entry.getKey();
                int colIdx = entry.getValue();
                String value = getCellStringValue(row.getCell(colIdx));
                langValues.put(lang, value);
            }
            result.put(chinese, langValues);
        }
        return result;
    }

    // ---------------------- 辅助方法（与之前相同）------------------------
    private static void rebuildSheet(Sheet sheet, List<String> orderedCols, int headerRowIndex) {
        // 读取原表所有数据行（跳过标题行及之前行）
        Row oldHeader = sheet.getRow(headerRowIndex);
        Map<String, Integer> oldColIndex = new HashMap<>();
        for (Cell cell : oldHeader) {
            oldColIndex.put(getCellStringValue(cell).trim(), cell.getColumnIndex());
        }

        List<Map<String, String>> rowsData = new ArrayList<>();
        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Map<String, String> rowMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : oldColIndex.entrySet()) {
                String colName = entry.getKey();
                int idx = entry.getValue();
                String value = getCellStringValue(row.getCell(idx));
                rowMap.put(colName, value);
            }
            rowsData.add(rowMap);
        }

        // 清空原sheet中从标题行开始往后的所有行（保留标题行之前的行，如有）
        int lastRow = sheet.getLastRowNum();
        for (int i = lastRow; i >= headerRowIndex; i--) {
            Row r = sheet.getRow(i);
            if (r != null) sheet.removeRow(r);
        }

        // 重新创建标题行（确保在 headerRowIndex 位置）
        Row newHeader = sheet.getRow(headerRowIndex);
        if (newHeader == null) newHeader = sheet.createRow(headerRowIndex);
        for (Cell cell : newHeader) newHeader.removeCell(cell);
        for (int i = 0; i < orderedCols.size(); i++) {
            newHeader.createCell(i).setCellValue(orderedCols.get(i));
        }

        // 重新写入数据行
        int rowNum = headerRowIndex + 1;
        for (Map<String, String> rowMap : rowsData) {
            Row newRow = sheet.getRow(rowNum);
            if (newRow == null) newRow = sheet.createRow(rowNum);
            for (int colIdx = 0; colIdx < orderedCols.size(); colIdx++) {
                String colName = orderedCols.get(colIdx);
                String value = rowMap.getOrDefault(colName, "");
                Cell cell = newRow.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cell.setCellValue(value);
            }
            rowNum++;
        }
    }

    private Map<String, Map<String, String>> readASheetToMap(Sheet sheet, List<String> orderedCols, int headerRowIndex) {
        Map<String, Map<String, String>> data = new HashMap<>();

        // 1. 读取A表的标题行，建立 列名 -> 列索引 的映射
        Row headerA = sheet.getRow(headerRowIndex);
        if (headerA == null) {
            throw new IllegalArgumentException("A表缺少标题行，索引：" + headerRowIndex);
        }
        Map<String, Integer> colIndexMapA = new HashMap<>();
        for (Cell cell : headerA) {
            String colName = getCellStringValue(cell).trim();
            colIndexMapA.put(colName, cell.getColumnIndex());
        }

        // 2. 确保A表包含中文列
        if (!colIndexMapA.containsKey(CHINESE_COL_NAME)) {
            throw new IllegalArgumentException("A表缺少列：" + CHINESE_COL_NAME + "，实际列名：" + colIndexMapA.keySet());
        }

        // 3. 遍历A表数据行
        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // 获取中文内容（作为key）
            Integer chineseColIdxA = colIndexMapA.get(CHINESE_COL_NAME);
            String chinese = getCellStringValue(row.getCell(chineseColIdxA)).trim();
            if (chinese.isEmpty()) continue;

            // 构建行数据：只包含 orderedCols 中存在的列
            Map<String, String> rowData = new HashMap<>();
            for (String colName : orderedCols) {
                if (colIndexMapA.containsKey(colName)) {
                    int colIdx = colIndexMapA.get(colName);
                    String value = getCellStringValue(row.getCell(colIdx));
                    rowData.put(colName, value);
                } else {
                    // 如果A表没有该列，则留空（不放入map，后续更新时该列不覆盖）
                    // rowData.put(colName, "");  // 可选
                }
            }
            data.put(chinese, rowData);
        }
        return data;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private static void sortSheetBySeq(Sheet sheet, int seqColIndex, int headerRowIndex) {
        if (seqColIndex < 0) return;
        List<Row> rows = new ArrayList<>();
        for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r != null) rows.add(r);
        }
        rows.sort(Comparator.comparingInt(r -> {
            Integer seq = getSeqValue(r, seqColIndex);
            return seq != null ? seq : Integer.MAX_VALUE;
        }));
        int rowNum = headerRowIndex + 1;
        for (Row row : rows) {
            Row newRow = sheet.getRow(rowNum);
            if (newRow == null) newRow = sheet.createRow(rowNum);
            for (Cell cell : row) {
                Cell newCell = newRow.getCell(cell.getColumnIndex(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                newCell.setCellValue(getCellStringValue(cell));
            }
            rowNum++;
        }
        for (int i = rowNum; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r != null) sheet.removeRow(r);
        }
    }

    private static Integer getSeqValue(Row row, int colIndex) {
        if (colIndex < 0) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case NUMERIC: return (int) cell.getNumericCellValue();
                case STRING: return Integer.parseInt(cell.getStringCellValue().trim());
                default: return null;
            }
        } catch (Exception e) { return null; }
    }


}
