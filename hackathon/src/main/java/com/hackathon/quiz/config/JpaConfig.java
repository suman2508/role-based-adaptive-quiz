package com.hackathon.quiz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA configuration:
 * - Enables auditing fields like @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 * - Provides a simple AuditorAware bean (replace with security context lookup when security is wired)
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Returns the current auditor (username/userId). Replace with SecurityContext-based implementation.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        // TODO: integrate with Spring Security Authentication once ready
        return () -> Optional.of("system");
    }
}
