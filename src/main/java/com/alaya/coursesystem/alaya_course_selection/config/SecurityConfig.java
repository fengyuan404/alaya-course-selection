// 包路径：com.alaya.coursesystem.alaya_course_selection.config
package com.alaya.coursesystem.alaya_course_selection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; // 这是Spring MVC的类，正确！

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 注入跨域配置源（关联之前的CorsConfig）
    private final CorsConfigurationSource corsConfigurationSource;

    // 构造器注入CorsConfigurationSource（确保跨域配置生效）
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http




                // 1. 配置跨域（使用注入的corsConfigurationSource，与CorsConfig保持一致）
                .cors(cors -> cors.configurationSource( corsConfigurationSource))




                // 1. CSRF防护（测试环境临时关闭，正式环境启用）
                .csrf(csrf -> csrf.disable()// 测试环境关闭；正式环境替换为下面这行
                       // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                // 2. 会话安全配置
                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                        .invalidSessionUrl("/api/auth/login")
//                        .maximumSessions(1)
//                        .expiredUrl("/api/auth/login?expired")
                                .maximumSessions(1) // 同一用户最多1个Session
                )

                // 3. 安全头配置
                .headers(headers -> headers
                        .addHeaderWriter(new ContentSecurityPolicyHeaderWriter(
                                "default-src 'self'; script-src 'self'"
                        ))
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        .frameOptions(frame -> frame
                                .sameOrigin()
                        )
                )
                // 4. 接口权限控制（合并重复配置，只保留一次）
                .authorizeHttpRequests(auth -> auth
                        // 放行OPTIONS预检请求（跨域必须）
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout", "/h2-console/**").permitAll()
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/student/courses/**").permitAll() // 临时放行
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // 5. HTTP Basic（测试环境用）
              //  .httpBasic(Customizer.withDefaults())
                // 6. 退出登录配置
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/api/auth/login?logout")
                        .permitAll()
                );


        return http.build();

    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}