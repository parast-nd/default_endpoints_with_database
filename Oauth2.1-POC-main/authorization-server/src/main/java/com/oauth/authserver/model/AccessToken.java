package com.oauth.authserver.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "oauth2_access_token")
@Data
public class AccessToken {
    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "registered_client_id")
    private String registeredClientId;

    @Column(name = "principal_name")
    private String principalName;

    @Column(name = "token_value")
    private String tokenValue;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "token_metadata")
    private String tokenMetadata;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "token_scopes")
    private String tokenScopes;
}

/*
 * CREATE TABLE oauth2_access_token (
    id VARCHAR(100) PRIMARY KEY,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    token_value TEXT NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    token_metadata TEXT,
    token_type VARCHAR(100),
    token_scopes TEXT
);CREATE TABLE oauth2_refresh_token (
    id VARCHAR(100) PRIMARY KEY,
    token_value TEXT NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    token_metadata TEXT
);CREATE TABLE oauth2_authorization_code (
    id VARCHAR(100) PRIMARY KEY,
    code_value TEXT NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    code_metadata TEXT
);
 */
