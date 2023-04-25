package com.coeux.todo.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.coeux.todo.jwt.JwtTokenAuthenticationProvider;

@Configuration
public class SecurityConfig {

        Logger log = LoggerFactory.getLogger(SecurityConfig.class);
        @Bean
        SecurityFilterChain springWebFilterChain(HttpSecurity http,
                        JwtTokenAuthenticationProvider tokenProvider) throws Exception {
                return http
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(c -> c.authenticationEntryPoint(
                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                                .requestMatchers("/actuator/**").permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(new JwtTokenAuthenticationFilter(tokenProvider),
                                                UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

}
