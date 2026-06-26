package com.reconciliation.controller;

import com.reconciliation.dto.AuthRequest;
import com.reconciliation.dto.AuthResponse;
import com.reconciliation.dto.RefreshTokenRequest;
import com.reconciliation.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

            log.info("Login bem-sucedido: {}", request.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    request.getUsername(),
                    authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
            ));

        } catch (Exception e) {
            log.warn("Falha no login para usuário: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Credenciais inválidas\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (tokenProvider.validateToken(refreshToken) && !tokenProvider.isTokenExpired(refreshToken)) {
                String username = tokenProvider.getUsernameFromToken(refreshToken);
                String newAccessToken = tokenProvider.generateAccessToken(
                        new UsernamePasswordAuthenticationToken(username, null, null)
                );
                return ResponseEntity.ok(new AuthResponse(
                        newAccessToken,
                        refreshToken,
                        "Bearer",
                        username,
                        null
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Refresh token inválido ou expirado\"}");
        } catch (Exception e) {
            log.error("Erro ao renovar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Refresh token inválido\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // O logout é stateless com JWT, mas podemos invalidar o refresh token no cliente
        // Em uma implementação mais avançada, poderíamos adicionar à blacklist
        log.info("Logout realizado");
        return ResponseEntity.ok("{\"message\": \"Logout realizado com sucesso\"}");
    }
}