package com.oauth.authserver.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "oauth2_refresh_token")
@Data
public class RefreshToken {
    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "token_value")
    private String tokenValue;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "token_metadata")
    private String tokenMetadata;
}
