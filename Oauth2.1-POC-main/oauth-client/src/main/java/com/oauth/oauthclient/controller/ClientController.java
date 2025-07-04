package com.oauth.oauthclient.controller;

import com.oauth.oauthclient.service.OAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Controller
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final OAuthService oauthService;

    public ClientController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
            <h1>OAuth 2.1 PKCE Demo</h1>
            <p>Click the button below to start the OAuth flow:</p>
            <a href="/login" style="display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px;">Login with OAuth</a>
            
            <h2>How it works:</h2>
            <ol>
                <li>Click login → generates PKCE code verifier/challenge</li>
                <li>Redirects to authorization server with challenge</li>
                <li>User logs in (username: user1, password: pass123)</li>
                <li>Auth server returns authorization code</li>
                <li>Client exchanges code + verifier for tokens</li>
                <li>Client fetches user data with access token</li>
            </ol>
            """;
    }

    @GetMapping("/login")
    public String login() {
        logger.info("Step 1: User clicked login button, starting PKCE flow.");
        var pkceData = oauthService.generatePKCE();
        
        logger.info("Step 2: Generated PKCE data. State: {}, Code Verifier (start): {}...", pkceData.state(), pkceData.codeVerifier().substring(0, 10));
        
        String authorizationUrl = "http://localhost:8080/oauth2/authorize" +
            "?response_type=code" +
            "&client_id=client123" +
            "&redirect_uri=http://localhost:8081/callback" +
            "&scope=profile" +
            "&state=" + pkceData.state() +
            "&code_challenge=" + pkceData.codeChallenge() +
            "&code_challenge_method=S256";
            
        logger.info("Step 3: Redirecting user to authorization server at: {}", authorizationUrl);
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code, @RequestParam String state, HttpSession session, Model model) {
        logger.info("Step 6: Received callback from authorization server with code and state.");
        logger.debug("Received authorization code: {}", code);
        logger.debug("Received state: {}", state);

        String codeVerifier = oauthService.getCodeVerifier(state);
        if (codeVerifier == null) {
            logger.error("No code_verifier found for state: {}. Possible session timeout or CSRF attack.", state);
            model.addAttribute("error", "Your session has expired or the state is invalid. Please try logging in again.");
            return "error";
        }
        logger.info("Step 7: Found matching code_verifier for the state. Preparing to exchange code for tokens.");

        try {
            Map<String, Object> tokens = oauthService.exchangeCodeForTokens(code, codeVerifier);
            logger.info("Step 8: Successfully exchanged code for tokens.");
            logger.debug("Received tokens: {}", tokens);

            session.setAttribute("access_token", tokens.get("access_token"));
            session.setAttribute("refresh_token", tokens.get("refresh_token"));
            oauthService.removeSession(state); // Clean up the used state and verifier

            // Remove token validation using /oauth2/introspect
            // After receiving the access token, directly show the callback/success page
            return "login-success";

        } catch (Exception e) {
            logger.error("Error during token exchange: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to exchange authorization code for tokens. " + e.getMessage());
            return "error";
        }
    }
}