package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.MailConfigDto;
import com.cordillera.MS_data.dto.MailConfigResponse;
import com.cordillera.MS_data.service.EmailService;
import com.cordillera.MS_data.service.MailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de administración de la configuración SMTP.
 * La autorización por rol ADMIN se aplica en el frontend / api-gateway (JWT).
 */
@Slf4j
@RestController
@RequestMapping("/mail-config")
@RequiredArgsConstructor
public class MailConfigController {

    private final MailConfigService mailConfigService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<MailConfigResponse> obtener() {
        return ResponseEntity.ok(mailConfigService.obtenerResponse());
    }

    @PutMapping
    public ResponseEntity<MailConfigResponse> actualizar(@Valid @RequestBody MailConfigDto dto) {
        return ResponseEntity.ok(mailConfigService.guardar(dto));
    }

    @PostMapping("/probar")
    public ResponseEntity<Map<String, String>> probar(@RequestParam String destinatario) {
        // El parámetro puede llegar duplicado ("a@b.com,a@b.com") si algún salto del
        // proxy repite la query; nos quedamos con la primera dirección.
        String destino = primeraDireccion(destinatario);
        try {
            emailService.enviarPrueba(destino);
            return ResponseEntity.ok(Map.of(
                    "estado", "OK",
                    "mensaje", "Correo de prueba enviado a " + destino));
        } catch (Exception e) {
            // Se responde 200 con estado ERROR para que el frontend muestre el motivo
            // sin tratarlo como un fallo de red/gateway.
            log.error("Fallo enviando correo de prueba a {}: {}", destino, e.getMessage(), e);
            String causa = e.getMessage();
            if (causa == null && e.getCause() != null) causa = e.getCause().getMessage();
            return ResponseEntity.ok(Map.of(
                    "estado", "ERROR",
                    "mensaje", causa == null ? "Error enviando el correo de prueba" : causa));
        }
    }

    private String primeraDireccion(String valor) {
        if (valor == null) return "";
        return valor.split(",")[0].trim();
    }
}
