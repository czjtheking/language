package com.steincker.dao;

import com.steincker.entity.SysConfig;

import java.util.List;

public interface SysConfigMapper {
    int deleteByPrimaryKey(String variable);

    int insert(SysConfig record);

    int insertSelective(SysConfig record);

    SysConfig selectByPrimaryKey(String variable);

    int updateByPrimaryKeySelective(SysConfig record);

    int updateByPrimaryKey(SysConfig record);

    List<SysConfig> selectSysConfigAll();
}