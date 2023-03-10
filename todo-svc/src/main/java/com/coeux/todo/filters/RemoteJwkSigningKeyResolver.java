package com.coeux.todo.filters;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.io.Decoders;

// Updated from: https://github.com/okta/okta-jwt-verifier-java/blob/master/impl/src/main/java/com/okta/jwt/impl/jjwt/RemoteJwkSigningKeyResolver.java
public final class RemoteJwkSigningKeyResolver implements SigningKeyResolver {

    private final URI jwkUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Object lock = new Object();
    private volatile Map<String, Key> keyMap = new HashMap<>();

    public RemoteJwkSigningKeyResolver(URI jwkUri, HttpClient httpClient) {
        this.jwkUri = jwkUri;
        this.httpClient = httpClient;
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, Claims claims) {
        return getKey(header.getKeyId());
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, String plaintext) {
        return getKey(header.getKeyId());
    }

    private Key getKey(String keyId) {

        // check non synchronized to avoid a lock
        Key result = keyMap.get(keyId);
        if (result != null) {
            return result;
        }

        synchronized (lock) {
            // once synchronized, check the map once again the a previously
            // synchronized thread could have already updated they keys
            result = keyMap.get(keyId);
            if (result != null) {
                return result;
            }

            // finally, fallback to updating the keys, an return a value (or null)
            updateKeys();
            return keyMap.get(keyId);
        }
    }

    public void updateKeys() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(jwkUri)
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
            Map<String, Key> newKeys = objectMapper.readValue(response.body(), JwkKeys.class).getKeys().stream()
                    .filter(jwkKey -> "sig".equals(jwkKey.getPublicKeyUse()))
                    .filter(jwkKey -> "RSA".equals(jwkKey.getKeyType()))
                    .collect(Collectors.toMap(JwkKey::getKeyId, jwkKey -> {
                        BigInteger modulus = base64ToBigInteger(jwkKey.getPublicKeyModulus());
                        BigInteger exponent = base64ToBigInteger(jwkKey.getPublicKeyExponent());
                        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
                        try {
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            return keyFactory.generatePublic(rsaPublicKeySpec);
                        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                            throw new IllegalStateException("Failed to parse public key");
                        }
                    }));

            keyMap = Collections.unmodifiableMap(newKeys);

        } catch (IOException | InterruptedException e) {
            throw new JwtException("Failed to fetch keys from URL: " + jwkUri, e);
        }
    }

    private BigInteger base64ToBigInteger(String value) {
        return new BigInteger(1, Decoders.BASE64URL.decode(value));
    }
}