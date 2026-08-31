package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.LoginResponse;
import com.wokioki.parcelflow.identity.auth.dto.RegisterResponse;
import com.wokioki.parcelflow.identity.auth.exception.EmailAlreadyExistsException;
import com.wokioki.parcelflow.identity.auth.refreshtoken.InvalidRefreshTokenException;
import com.wokioki.parcelflow.identity.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private RefreshService refreshService;

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterResponse response = new RegisterResponse(UUID.randomUUID(), "john@example.com", "John", "Doe", Role.CUSTOMER);

        when(registrationService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "john@example.com",
              "password": "password123",
              "firstName": "John",
              "lastName": "Doe"
            }
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("john@example.com")).andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe")).andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "invalid-email",
              "password": "123",
              "firstName": "",
              "lastName": ""
            }
            """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        doThrow(new EmailAlreadyExistsException("john@example.com")).when(registrationService).register(any());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "john@example.com",
              "password": "password123",
              "firstName": "John",
              "lastName": "Doe"
            }
            """)).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409)).andExpect(jsonPath("$.message").value("User with email 'john@example.com' already exists"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        when(loginService.login(any())).thenReturn(new LoginResponse("test-access-token", "test-refresh-token", "Bearer"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "john@example.com",
              "password": "password123"
            }
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("test-access-token")).andExpect(jsonPath("$.refreshToken").value("test-refresh-token")).andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "invalid-email",
              "password": ""
            }
            """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(loginService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "email": "john@example.com",
              "password": "wrong-password"
            }
            """)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void shouldRefreshTokens() throws Exception {
        when(refreshService.refresh(any())).thenReturn(new LoginResponse("new-access-token", "new-refresh-token", "Bearer"));

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
            {
              "refreshToken": "old-refresh-token"
            }
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("new-access-token")).andExpect(jsonPath("$.refreshToken").value("new-refresh-token")).andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestForEmptyRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": ""
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Request validation failed"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidRefreshToken() throws Exception {
        when(refreshService.refresh(any()))
            .thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "refreshToken": "invalid-refresh-token"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message")
                .value("Invalid or expired refresh token"));
    }
}
