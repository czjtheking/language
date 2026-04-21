package com.steincker.service;

import com.steincker.entity.SysConfig;

import java.util.List;

/**
 * @ClassName SysConfigService
 * @Author ST000056
 * @Date 2023-10-19 20:42
 * @Version 1.0
 * @Description TODO
 **/


public interface  SysConfigService {


    /*
    * 根据variable字段查询SysConfig
    * */
    public SysConfig getByPrimaryKey(String id);

    /*
    * 直接查询SysConfig
    * */
    public List<SysConfig> getSysConfigAll();
}
