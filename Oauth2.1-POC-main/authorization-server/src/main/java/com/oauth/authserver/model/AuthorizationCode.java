package com.oauth.authserver.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "oauth2_authorization_code")
@Data
public class AuthorizationCode {
    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "code_value")
    private String codeValue;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "code_metadata")
    private String codeMetadata;
}
