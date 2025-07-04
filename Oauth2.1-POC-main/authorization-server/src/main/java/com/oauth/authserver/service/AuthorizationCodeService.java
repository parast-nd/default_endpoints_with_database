package com.oauth.authserver.service;

import com.oauth.authserver.model.AuthorizationCode;
import com.oauth.authserver.repository.AuthorizationCodeRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthorizationCodeService {
    private final AuthorizationCodeRepository authorizationCodeRepository;

    public AuthorizationCodeService(AuthorizationCodeRepository authorizationCodeRepository) {
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    public void save(AuthorizationCode code) {
        authorizationCodeRepository.save(code);
    }

    public Optional<AuthorizationCode> findByCode(String code) {
        return authorizationCodeRepository.findByCodeValue(code);
    }

    public void delete(String code) {
        authorizationCodeRepository.deleteById(code);
    }
}
