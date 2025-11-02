package com.amit.crud.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    // Needed to build MvcRequestMatcher
//    @Bean
//    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
//        return new MvcRequestMatcher.Builder(introspector);
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // H2 console is NOT a Spring MVC endpoint → use AntPathRequestMatcher
//                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
//                        // API endpoints ARE Spring MVC → use MvcRequestMatcher
//                        .requestMatchers(mvc.pattern("/auth/**")).permitAll()
//                        .requestMatchers(mvc.pattern("/api/admin/**")).hasRole("ADMIN")
//                        .requestMatchers(mvc.pattern("/api/customer/**")).hasRole("CUSTOMER")
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"),new AntPathRequestMatcher("/auth/**")))
//                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
//                .formLogin(form -> form.permitAll())
//                .httpBasic(withDefaults());
//
//        return http.build();
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Allow all requests without authentication
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // Needed for H2 console

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}