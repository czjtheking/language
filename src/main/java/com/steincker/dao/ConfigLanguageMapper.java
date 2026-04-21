package com.steincker.dao;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.steincker.entity.ConfigLanguage;
import com.steincker.entity.EnergyModelEvent;
import lombok.NonNull;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * [系统配置] 多语言 mapper
 * @author ST000050
 */
@DS("energy")
public interface ConfigLanguageMapper {
    // -------------------- find --------------------

    /** 使用 id 获取多语言 【默认包括逻辑删除】 */
    ConfigLanguage findById(Long id);

    // -------------------- findAll --------------------

    /** 获取所有多语言 【默认不包括逻辑删除】 */
    List<ConfigLanguage> findAll();

    /** 使用 id 列表获取所有多语言 【默认包括逻辑删除】 */
    List<ConfigLanguage> findAllByIdIn(Collection<Long> ids);

    int setGroupConcatMaxLen();


    /** 获取所有多语言 【默认不包括逻辑删除】 */
    List<ConfigLanguage> findAllGroupBy();

    // -------------------- service --------------------

    /** 使用 app + type + lang 获取语言列表 【默认不包括逻辑删除】 */
    List<ConfigLanguage> findAllByAppAndTypeAndLang(String app, String type, String lang);

    /** 使用 code + type + app + lang 获取语言 【默认包括逻辑删除】 */
    ConfigLanguage findByCodeAndTypeAndAppAndLang(@NonNull String code, @NonNull String type, @NonNull String app, @NonNull String lang);

    /** 使用 codes + type + app + lang 获取语言 【默认包括逻辑删除】 */
    List<ConfigLanguage> findByCodeInAndTypeAndAppAndLang(@NonNull Collection<String> codes, @NonNull String type, @NonNull String app, @NonNull String lang);

    /**
     * 使用 types + app + lang 获取语言 【默认包括逻辑删除】
     */
    List<ConfigLanguage> findByTypeInAndAppAndLang(@NonNull Collection<String> types, @NonNull String app, @NonNull String lang);

    /**
     * 根据国际化text 模糊查询满足条件的code 列表
     */
    List<ConfigLanguage> listI18CodeByText(@NonNull String type, @NonNull String app, @NonNull String lang, String text);

    /**
     * 批量插入国际化信息
     */
    int insertBatch(@Param("list") List<ConfigLanguage> activeList);

    /**
    * 批量删除国际化信息
    * */
    int deleteBatch(@Param("list") List<ConfigLanguage> activeList);

    /**
     * 获取告警模型
     */
    List<EnergyModelEvent> listAlarmModelEvent();

}
