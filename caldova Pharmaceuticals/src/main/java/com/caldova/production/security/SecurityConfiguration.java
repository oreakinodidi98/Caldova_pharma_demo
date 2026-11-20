package com.caldova.production.security;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public UserDetailsService users(DataSource datasource) {
        JdbcDaoImpl users = new JdbcDaoImpl();
        users.setDataSource(datasource);
        users.setUsersByUsernameQuery(
                "SELECT username, password_hash, enabled FROM CALDOVA_OWNER.app_user WHERE username=?");
        users.setAuthoritiesByUsernameQuery(
                "SELECT username, 'ROLE_PRESENTER' FROM CALDOVA_OWNER.app_user WHERE username=?");
        return users;
    }

    @Bean
    public SecurityFilterChain security(HttpSecurity http, UserDetailsService users,
                                        PasswordEncoder encoder) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        http.authenticationProvider(provider)
                .authorizeRequests()
                .antMatchers("/login", "/css/**", "/error", "/api/health").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").defaultSuccessUrl("/", false)
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof AuthenticationServiceException) {
                        LOG.warn("Oracle sign-in service failed ({})", exception.getClass().getSimpleName());
                        response.sendError(503, "Sign-in is temporarily unavailable. Check the local Oracle database.");
                    } else {
                        LOG.warn("Presenter sign-in was rejected");
                        response.sendRedirect("/login?error");
                    }
                })
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/login?logout");
        http.headers().contentSecurityPolicy(
                "default-src 'none'; style-src 'self'; img-src 'self'; form-action 'self'; "
                        + "frame-ancestors 'none'; base-uri 'none'")
                .and().referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER);
        return http.build();
    }
}
