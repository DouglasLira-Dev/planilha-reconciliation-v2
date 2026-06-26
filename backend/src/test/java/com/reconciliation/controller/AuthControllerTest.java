package com.reconciliation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reconciliation.dto.AuthRequest;
import com.reconciliation.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateAccessToken(any())).thenReturn("mocked-access-token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("mocked-refresh-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked-access-token"))
                .andExpect(jsonPath("$.username").value("admin"));
    }
}