package com.steincker.dao;


import com.steincker.entity.RegionLanguage;

import java.util.Collection;
import java.util.List;

/**
 * [国家地区] 多语言 mapper
 * @author ST000050
 */
public interface RegionLanguageMapper {
    // -------------------- find --------------------

    /** 使用 id 获取多语言 【默认包括逻辑删除】 */
    RegionLanguage findById(Long id);

    // -------------------- findAll --------------------

    /** 获取所有多语言 【默认不包括逻辑删除】 */
    List<RegionLanguage> findAll();

    /** 使用 id 列表获取所有多语言 【默认包括逻辑删除】 */
    List<RegionLanguage> findAllByIdIn(Collection<Long> ids);
}
