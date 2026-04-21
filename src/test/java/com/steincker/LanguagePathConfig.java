package com.steincker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言包处理路径配置类
 * 从 application.properties 中读取路径配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "language")
public class LanguagePathConfig {

    // 基础目录
    private String baseDir;
    private String rawPropertiesDir;
    private String rawDbDir;
    private String updatePropertiesDir;
    private String compareOldDir;
    private String sqlOutputDir;
    private String importExcelPath;
    private String importDbExcelPath;

    // demo 项目 resources 目录
    private String demoResourcesDir;

    // 各服务 i18n 目录映射（由 @ConfigurationProperties 自动绑定）
    private Map<String, String> servicePath = new HashMap<>();

    // 注意：service.energy.i18n 等属性会被自动注入到 servicePath Map 中，
    // 需要确保 Map 的 key 与配置文件中的后缀一致（如 energy、permission 等）
    // 如果自动绑定失败，可手动添加 getter 或使用 @Value 注入单个属性。
    // 为简化，这里提供手动获取服务路径的方法。
    public String getServicePath(String serviceName) {
        return servicePath.get(serviceName);
    }
}