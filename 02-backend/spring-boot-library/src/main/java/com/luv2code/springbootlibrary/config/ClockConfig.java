package com.luv2code.springbootlibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /**
     * Single source of "now" for the application. Tests can replace it with {@link Clock#fixed}.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
