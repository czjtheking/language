package com.steincker.dao;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.steincker.entity.PermissionConfigLanguage;

import java.util.List;

@DS("permission")
public interface PermissionConfigLanguageMapper {


    /**
     * 根据主键删除数据库的记录
     * @return 删除条数
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 新写入数据库记录
     * @param record 对象属性
     * @return 返回插入条数
     */
    int insert(PermissionConfigLanguage record);

    /**
     * 动态字段,写入数据库记录
     * @param record 对象属性
     * @return 返回插入条数
     */
    int insertSelective(PermissionConfigLanguage record);



    /**
     * 根据指定主键获取一条数据库记录
     * @return 返回数据库对象
     */
    PermissionConfigLanguage selectByPrimaryKey(Long id);



    /**
     * 根据主键来更新符合条件的数据库记录,动态字段属性不为空才会更新
     * @param record 对象属性
     * @return 更新影响条数
     */
    int updateByPrimaryKeySelective(PermissionConfigLanguage record);

    /**
     * 根据主键来更新符合条件的数据库记录，为空更新为空
     * @param record 对象属性，包含主键
     * @return 更新影响条数
     */
    int updateByPrimaryKey(PermissionConfigLanguage record);

    int setGroupConcatMaxLen();

    /**  获取所有实体信息 已按照code type等分组 【默认不包括逻辑删除】 */
    List<PermissionConfigLanguage> findAllGroupBy();
}