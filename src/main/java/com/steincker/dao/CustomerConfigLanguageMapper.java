package com.steincker.dao;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.steincker.entity.CustomerConfigLanguage;

import java.util.List;

@DS("customer")
public interface CustomerConfigLanguageMapper {

    int setGroupConcatMaxLen();

    /**  获取所有实体信息 已按照code type等分组 【默认不包括逻辑删除】 */
    List<CustomerConfigLanguage> findAllGroupBy();

    /** 获取所有多语言 【默认不包括逻辑删除】 */
    List<CustomerConfigLanguageMapper> findAll();
}
