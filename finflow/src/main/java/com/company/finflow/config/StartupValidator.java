package com.company.finflow.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupValidator {

    @Value("${spring.profiles.active}")
    private String profile;

    @PostConstruct
    public void validate() {
        if ("dev".equals(profile)) {
            System.out.println("WARNING: Running in DEV profile");
        } else {
            System.out.println("INFO: Running in PROD profile");
        }
    }
}
