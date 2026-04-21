package com.steincker.service;

/**
 * @ClassName DeviceService
 * @Author ST000056
 * @Date 2025-03-12 15:43
 * @Version 1.0
 * @Description
 **/

import com.steincker.entity.DeviceRegisterEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DeviceService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void registerDevice(Long deviceId) {
        // 模拟设备注册逻辑
        System.out.println("Registering device: " + deviceId);

        // 发布事件
        eventPublisher.publishEvent(new DeviceRegisterEvent(deviceId));
    }
}
