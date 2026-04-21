package com.steincker;

import com.steincker.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;

    @Test
    public void testRegisterDevice() {
        // 发布事件
        deviceService.registerDevice(100L);

        // 如果事件监听器方法被执行，应该会输出日志或进行其他操作
        // 你可以通过日志或断点来验证
    }
}