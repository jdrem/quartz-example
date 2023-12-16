package net.remgant.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoSomethingService {

    public void deactivateDevice(String deviceId) {
        log.info("device {} deactivated", deviceId);
    }

    public void deactivateAccount(String accountId) {
        log.info("account {} deactivated", accountId);
    }
}
