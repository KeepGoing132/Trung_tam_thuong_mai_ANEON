package com.company.aneon.shared.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class AdminBootstrap implements ApplicationRunner {
    private final AuthService auth;
    private final String username;
    private final String password;

    AdminBootstrap(AuthService auth, @Value("${ANEON_BOOTSTRAP_ADMIN_USERNAME:}") String username,
                   @Value("${ANEON_BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.auth = auth;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!username.isBlank() || !password.isBlank()) auth.bootstrapAdmin(username, password);
    }
}
