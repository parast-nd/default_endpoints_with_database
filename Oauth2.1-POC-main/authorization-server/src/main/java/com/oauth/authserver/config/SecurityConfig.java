package com.oauth.authserver.config;

import com.oauth.authserver.repository.ClientRepository;
import com.oauth.authserver.repository.UserRepository;
import com.oauth.authserver.service.DelegatingOAuth2AuthorizationService;
import com.oauth.authserver.repository.AccessTokenRepository;
import com.oauth.authserver.repository.RefreshTokenRepository;
import com.oauth.authserver.repository.AuthorizationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import javax.sql.DataSource;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClientRepository clientRepository;

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/oauth2/**", "/.well-known/**");
        org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.csrf(csrf -> csrf.disable());
        http.exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"))
        );
        return http.build();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/error", "/resources/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepository() {
            @Override
            public void save(RegisteredClient registeredClient) {
                // Implement if you want to allow dynamic client registration
                throw new UnsupportedOperationException();
            }

            @Override
            public RegisteredClient findById(String id) {
                Optional<com.oauth.authserver.model.Client> clientOpt = clientRepository.findByClientId(id);
                if (clientOpt.isPresent()) {
                    com.oauth.authserver.model.Client client = clientOpt.get();
                    return RegisteredClient.withId(client.getClientId())
                            .clientId(client.getClientId())
                            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                            .redirectUri(client.getRedirectUri())
                            .scope("profile")
                            .tokenSettings(TokenSettings.builder()
                                    .accessTokenFormat(org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat.REFERENCE)
                                    .accessTokenTimeToLive(Duration.ofMinutes(30))
                                    .build())
                            .build();
                }
                return null;
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                return findById(clientId);
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<com.oauth.authserver.model.User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }
            com.oauth.authserver.model.User user = userOpt.get();
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository,
            AccessTokenRepository accessTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthorizationCodeRepository authorizationCodeRepository) {
        OAuth2AuthorizationService delegate = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        return new DelegatingOAuth2AuthorizationService(
            delegate,
            accessTokenRepository,
            refreshTokenRepository,
            authorizationCodeRepository
        );
    }
}