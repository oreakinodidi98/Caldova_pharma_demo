package com.caldova.production.testsupport;

import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurerAdapter;
import org.springframework.web.context.WebApplicationContext;

/**
 * Restores the automatic Spring Security MockMvc wiring that Spring Boot's
 * webmvc-test module no longer configures out of the box: it makes sure every
 * request run through the auto-configured {@code MockMvc} picks up the
 * {@code TestSecurityContextHolder} (populated by annotations such as
 * {@code @WithMockUser}) the same way {@code .apply(springSecurity())} used to.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityMockMvcSupportConfiguration {

    @Bean
    public MockMvcBuilderCustomizer securityMockMvcBuilderCustomizer() {
        return builder -> builder.apply(new MockMvcConfigurerAdapter() {
            @Override
            public org.springframework.test.web.servlet.request.RequestPostProcessor beforeMockMvcCreated(
                    ConfigurableMockMvcBuilder<?> configurableBuilder, WebApplicationContext context) {
                return SecurityMockMvcRequestPostProcessors.testSecurityContext();
            }
        });
    }
}
