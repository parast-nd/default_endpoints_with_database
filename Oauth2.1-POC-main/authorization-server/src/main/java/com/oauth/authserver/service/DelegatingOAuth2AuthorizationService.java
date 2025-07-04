package com.oauth.authserver.service;

import com.oauth.authserver.model.AccessToken;
import com.oauth.authserver.model.RefreshToken;
import com.oauth.authserver.model.AuthorizationCode;
import com.oauth.authserver.repository.AccessTokenRepository;
import com.oauth.authserver.repository.RefreshTokenRepository;
import com.oauth.authserver.repository.AuthorizationCodeRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

public class DelegatingOAuth2AuthorizationService implements OAuth2AuthorizationService {
    private final OAuth2AuthorizationService delegate;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    public DelegatingOAuth2AuthorizationService(
            OAuth2AuthorizationService delegate,
            AccessTokenRepository accessTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthorizationCodeRepository authorizationCodeRepository) {
        this.delegate = delegate;
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        delegate.save(authorization);
        // Copy access token
        if (authorization.getAccessToken() != null) {
            var token = authorization.getAccessToken().getToken();
            AccessToken entity = new AccessToken();
            entity.setId(authorization.getId());
            entity.setRegisteredClientId(authorization.getRegisteredClientId());
            entity.setPrincipalName(authorization.getPrincipalName());
            entity.setTokenValue(token.getTokenValue());
            entity.setIssuedAt(token.getIssuedAt());
            entity.setExpiresAt(token.getExpiresAt());
            entity.setTokenType(token.getTokenType().getValue());
            entity.setTokenScopes(String.join(",", token.getScopes()));
            accessTokenRepository.save(entity);
        }
        // Copy refresh token
        if (authorization.getRefreshToken() != null) {
            var token = authorization.getRefreshToken().getToken();
            RefreshToken entity = new RefreshToken();
            entity.setId(authorization.getId());
            entity.setTokenValue(token.getTokenValue());
            entity.setIssuedAt(token.getIssuedAt());
            entity.setExpiresAt(token.getExpiresAt());
            entity.setTokenMetadata("");
            refreshTokenRepository.save(entity);
        }
        // Copy authorization code
        var codeToken = authorization.getToken(org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode.class);
        if (codeToken != null && codeToken.getToken() != null) {
            var token = codeToken.getToken();
            AuthorizationCode entity = new AuthorizationCode();
            entity.setId(authorization.getId());
            entity.setCodeValue(token.getTokenValue());
            entity.setIssuedAt(token.getIssuedAt());
            entity.setExpiresAt(token.getExpiresAt());
            entity.setCodeMetadata("");
            authorizationCodeRepository.save(entity);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
        // Optionally, remove from your tables as well
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, org.springframework.security.oauth2.server.authorization.OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }
}
