package com.example.danceplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/", "/index.html", "/css/**", "/js/**", "/images/**",
                                                                "/admin-login.html", "/ai-planner-view.html",
                                                                "/coach.html", "/dancer.html", "/timetable.html",
                                                                "/dancer-login.html")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/dancers/**", "/api/groups/**", "/api/coaches/**",
                                                                "/api/halls/**", "/api/ai/**",
                                                                "/api/coach/**", "/api/dancer/**", "/api/calendar/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/admin.html", "/admin-timetable.html", "/add-*.html",
                                                                "/delete-*.html")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/admin-login.html")
                                                .loginProcessingUrl("/admin/login")
                                                .defaultSuccessUrl("/admin.html", true)
                                                .failureUrl("/admin-login.html?error")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/index.html")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .httpBasic(Customizer.withDefaults());

                return http.build();
        }
}