package com.oauth.authserver.repository;

import com.oauth.authserver.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    Optional<AccessToken> findByTokenValue(String tokenValue);
}