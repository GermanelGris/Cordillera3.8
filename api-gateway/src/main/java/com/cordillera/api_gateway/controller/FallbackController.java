package com.cordillera.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Endpoints de fallback invocados por el Circuit Breaker del Gateway
 * cuando un microservicio no está disponible.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> fallbackLogin() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "MS-Login no disponible",
                "mensaje", "El servicio de autenticación está temporalmente fuera de servicio. Intente más tarde.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/datos")
    public ResponseEntity<Map<String, Object>> fallbackDatos() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "MS-Data no disponible",
                "mensaje", "El servicio de datos está temporalmente fuera de servicio. Intente más tarde.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> fallbackKpi() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "MS-KPI no disponible",
                "mensaje", "El servicio de KPIs está temporalmente fuera de servicio. Intente más tarde.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/reportes")
    public ResponseEntity<Map<String, Object>> fallbackReportes() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "MS-Reportes no disponible",
                "mensaje", "El servicio de reportes está temporalmente fuera de servicio. Intente más tarde.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
