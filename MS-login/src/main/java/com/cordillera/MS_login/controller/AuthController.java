package com.cordillera.MS_login.controller;

import com.cordillera.MS_login.dto.LoginRequest;
import com.cordillera.MS_login.dto.LoginResponse;
import com.cordillera.MS_login.dto.RegistroRequest;
import com.cordillera.MS_login.dto.UsuarioResponse;
import com.cordillera.MS_login.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Endpoints públicos (/auth/**)
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(request));
    }

    // ── Endpoints de gestión de usuarios (/usuarios/**)
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(authService.listarUsuarios());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MS-Login OK");
    }
}
