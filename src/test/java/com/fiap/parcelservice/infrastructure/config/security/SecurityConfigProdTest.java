package com.fiap.parcelservice.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigProdTest {

    @InjectMocks
    private SecurityConfigProd securityConfigProd;

    @Mock
    private HttpSecurity http;

    @Mock
    private SecurityFilterChain filterChain;

    @Test
    @DisplayName("Deve construir SecurityFilterChain chamando authorizeHttpRequests e oauth2ResourceServer")
    void shouldReturnSecurityFilterChainAndConfigureHttpSecurity() throws Exception {
        // Arrange
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.oauth2ResourceServer(any())).thenReturn(http);
        doReturn(filterChain).when(http).build();

        // Act
        SecurityFilterChain result = securityConfigProd.securityFilterChain(http);

        // Assert
        assertSame(filterChain, result);

        // Verify
        verify(http).authorizeHttpRequests(any());
        verify(http).oauth2ResourceServer(any());
        verify(http).build();
        verifyNoMoreInteractions(http);
    }
}