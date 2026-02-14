package com.company.finflow.security;

import com.company.finflow.security.auth.FinflowUserDetailsService;
import com.company.finflow.security.handler.RestAccessDeniedHandler;
import com.company.finflow.security.handler.RestAuthenticationEntryPoint;
import com.company.finflow.security.jwt.JwtAuthenticationFilter;
import com.company.finflow.tenant.context.TenantFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TenantFilter tenantFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FinflowUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(tenantFilter, JwtAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/error",
                    "/api/health",
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                .requestMatchers("/api/tenants/**", "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/budgets/**").hasAnyRole("FINANCE_MANAGER", "ADMIN", "AUDITOR")
                .requestMatchers(HttpMethod.POST, "/api/budgets/**").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/budgets/**").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/budgets/**").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/audit-logs/**").hasAnyRole("AUDITOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/audit-logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/audit-logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/audit-logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/workflow-steps/*/approve").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/workflow-steps/*/reject").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/workflow-steps").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/workflow-steps/**").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/workflow-steps/**").hasAnyRole("FINANCE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/workflow-steps/**").hasAnyRole(
                    "FINANCE_MANAGER",
                    "ADMIN",
                    "AUDITOR"
                )
                .requestMatchers(HttpMethod.POST, "/api/expenses").hasAnyRole("EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/expenses/*/submit").hasAnyRole("EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/expenses/**").hasAnyRole("EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/expenses/**").hasAnyRole("EMPLOYEE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/expenses/**").hasAnyRole(
                    "EMPLOYEE",
                    "FINANCE_MANAGER",
                    "ADMIN",
                    "AUDITOR"
                )
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
