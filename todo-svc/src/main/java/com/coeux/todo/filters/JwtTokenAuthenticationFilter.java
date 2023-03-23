package com.coeux.todo.filters;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.coeux.todo.jwt.JwtTokenAuthenticationProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/* 
 * With the help of https://github.com/hantsy/spring-webmvc-jwt-sample
*/
public class JwtTokenAuthenticationFilter extends GenericFilterBean {

    public static final String HEADER_PREFIX = "Bearer ";

    private JwtTokenAuthenticationProvider jwtTokenProvider = null;

    public JwtTokenAuthenticationFilter(JwtTokenAuthenticationProvider tokenProvider) {
        this.jwtTokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        if (isAuthTokenPresent((HttpServletRequest) req)) {
            String token = getAuthToken((HttpServletRequest) req);
            if (jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(req, res);
    }

    private String getAuthToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        return bearerToken.substring(7);
    }

    private boolean isAuthTokenPresent(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        return StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX);
    }

}