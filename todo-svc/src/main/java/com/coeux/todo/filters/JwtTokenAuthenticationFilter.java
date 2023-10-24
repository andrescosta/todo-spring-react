package com.coeux.todo.filters;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private JwtTokenAuthenticationProvider jwtTokenProvider = null;

    public JwtTokenAuthenticationFilter(JwtTokenAuthenticationProvider tokenProvider) {
        this.jwtTokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        if (jwtTokenProvider.isAuthTokenPresent((HttpServletRequest) req)) {
            String token = jwtTokenProvider.getAuthToken((HttpServletRequest) req);
            if (jwtTokenProvider.validateRequestToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(req, res);
    }
}