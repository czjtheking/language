package com.steincker.service.impl;

import com.steincker.dao.SysConfigMapper;
import com.steincker.entity.SysConfig;
import com.steincker.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysConfigServiceImpl implements SysConfigService {
    @Autowired
    private SysConfigMapper sysConfigMapper;
    public SysConfig getByPrimaryKey(String id){
        return sysConfigMapper.selectByPrimaryKey(id);
    }

    public List<SysConfig> getSysConfigAll(){
        return sysConfigMapper.selectSysConfigAll();
    }


}
