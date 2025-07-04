package com.oauth.authserver.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Optional;

@Entity
@Table(name = "clients")
@Data
public class Client {

    @Id
    @Column(name = "client_id") 
    private String clientId;

    @Column(name = "redirect_uri")
    private String redirectUri;
}