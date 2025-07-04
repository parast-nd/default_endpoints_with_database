package com.oauth.oauthclient.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OAuthService {

    private final WebClient webClient;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    public OAuthService() {
        this.webClient = WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build();
    }

    public record PKCEData(String codeVerifier, String codeChallenge, String state) {}

    public PKCEData generatePKCE() {
        // Generate code verifier
        byte[] verifierBytes = new byte[32];
        secureRandom.nextBytes(verifierBytes);
        String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);

        // Generate code challenge
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // Generate state
            byte[] stateBytes = new byte[16];
            secureRandom.nextBytes(stateBytes);
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);

            // Store session
            sessions.put(state, codeVerifier);

            return new PKCEData(codeVerifier, codeChallenge, state);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PKCE", e);
        }
    }

    public String getCodeVerifier(String state) {
        return sessions.get(state);
    }

    public void removeSession(String state) {
        sessions.remove(state);
    }

    public Map<String, Object> exchangeCodeForTokens(String code, String codeVerifier) {
        logger.info("Step 7: Exchanging code for tokens");
        
        // After receiving tokens
        Map<String, Object> response = webClient.post()
            .uri("/oauth2/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=authorization_code" +
                     "&code=" + code +
                     "&client_id=client123" +
                     "&code_verifier=" + codeVerifier +
                     "&redirect_uri=http://localhost:8081/callback")
            .retrieve()
            .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
            .block();
        
        logger.info("Step 9: Token exchange successful");
        return response;
    }
}