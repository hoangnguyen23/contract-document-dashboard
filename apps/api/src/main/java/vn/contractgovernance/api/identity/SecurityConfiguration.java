package vn.contractgovernance.api.identity;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http, SessionFilter sessions) {
        // Opaque database sessions and their CSRF token are enforced by SessionFilter for every API operation.
        http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
                .httpBasic(basic -> basic.disable()).formLogin(form -> form.disable()).logout(logout -> logout.disable())
                .requestCache(cache -> cache.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(sessions, AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    FilterRegistrationBean<SessionFilter> disableContainerSessionFilter(SessionFilter filter) {
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
