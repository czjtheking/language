package com.steincker.controller;

import com.steincker.entity.SysConfig;
import com.steincker.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName SysConfigController
 * @Author ST000056
 * @Date 2023-10-19 20:27
 * @Version 1.0
 * @Description TODO
 **/
@RestController
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;
    @RequestMapping("/getSysConfig")
    public SysConfig getSysConfig( ){
        SysConfig sysConfig = sysConfigService.getByPrimaryKey("diagnostics.allow_i_s_tables");

        return sysConfig;
    }

    @RequestMapping("/showSysConfigAll")
    public List<SysConfig> showSysConfigAll (){
        List<SysConfig> sysConfigList = sysConfigService.getSysConfigAll();

        return  sysConfigList;
    }

}
