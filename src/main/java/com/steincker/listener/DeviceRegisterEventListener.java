package com.steincker.listener;

import com.steincker.entity.DeviceRegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


/**
 * @ClassName DeviceRegisterEventListener
 * @Author ST000056
 * @Date 2025-03-12 15:33
 * @Version 1.0
 * @Description
 **/
@Component
public class DeviceRegisterEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegisterEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true, classes = DeviceRegisterEvent.class)
    public void handleDeviceRegisterEvent(DeviceRegisterEvent event) {
        logger.info("yyyyyyhhhhhhhhhh Handling device register event: {}", event.getDeviceId());
        // 其他处理逻辑
    }
}
