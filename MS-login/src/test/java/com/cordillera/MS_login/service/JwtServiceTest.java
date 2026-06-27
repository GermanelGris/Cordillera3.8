package com.cordillera.MS_login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "cordillera-grupo-jwt-secret-key-2026-pruebas");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
    }

    @Test
    void generarToken_yExtraerUsername() {
        String token = jwtService.generarToken("admin@cordillera.cl", Map.of("rol", "ADMIN"));

        assertThat(token).isNotBlank();
        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin@cordillera.cl");
    }

    @Test
    void esTokenValido_conUsernameCorrecto_retornaTrue() {
        String token = jwtService.generarToken("user@cordillera.cl", Map.of());

        assertThat(jwtService.esTokenValido(token, "user@cordillera.cl")).isTrue();
    }

    @Test
    void esTokenValido_conUsernameDistinto_retornaFalse() {
        String token = jwtService.generarToken("user@cordillera.cl", Map.of());

        assertThat(jwtService.esTokenValido(token, "otro@cordillera.cl")).isFalse();
    }
}
