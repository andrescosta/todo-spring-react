package com.coeux.todo.filters;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

/* 
 * Inpired by https://github.com/hantsy/spring-webmvc-jwt-sample
*/

@Component
public class JwtTokenProvider {

    Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "roles";

    @Value("${jwt.clockSkew}")
    long clockSkew; 


    @Value("${jwt.jwksURI}")
    URI jwksURI;

    @PostConstruct
    public void init() {
    }

    public Authentication getAuthentication(String token) {
        var res = new RemoteJwkSigningKeyResolver(getJwksURI(), HttpClient.newHttpClient());
        Claims claims = Jwts.parserBuilder()
                .setSigningKeyResolver(res)
                .setClock(new MyClock())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);

        Collection<? extends GrantedAuthority> authorities = authoritiesClaim == null ? AuthorityUtils.NO_AUTHORITIES
                : AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            var res = new RemoteJwkSigningKeyResolver(getJwksURI(), HttpClient.newHttpClient());
            Jws<Claims> claims = Jwts
                    .parserBuilder()
                    .setSigningKeyResolver(res)
                    .setClock(new MyClock())
                    .build()
                    .parseClaimsJws(token);
            logger.info("expiration date: {}", claims.getBody().getExpiration());
            return true;
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    // For debugging
    public static class MyClock implements Clock {
        public Date now() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, -1);
            return cal.getTime();
        }
    }

    private long getClockSkew() {
        return clockSkew;
    }

    private URI getJwksURI() {
        return jwksURI;
    }

}