package com.company.aneon.f01.integration;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * STUB CO NHAN - chi profile local/test: coi moi vi tri deu kha anh xa.
 * Khi tich hop F18 that, thay boi adapter goi dich vu F18.
 */
@Component
@Profile({"local", "test"})
public class LocalDigitalMapStubAdapter implements DigitalMapPort {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isLocationMappable(UUID locationId) {
        return true;
    }
}
