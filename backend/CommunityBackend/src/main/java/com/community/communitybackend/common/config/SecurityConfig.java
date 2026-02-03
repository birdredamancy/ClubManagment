package com.community.communitybackend.common.config;

import com.community.communitybackend.common.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * - 关闭CSRF（前后端分离项目不需要）
 * - 配置无状态Session（使用JWT，不需要Session）
 * - 放行登录注册接口，其他接口需要认证
 * - 注入JWT过滤器
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 无状态Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 放行登录注册接口
                        .requestMatchers("/api/auth/**").permitAll()
                        // 放行错误页面
                        .requestMatchers("/error").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
