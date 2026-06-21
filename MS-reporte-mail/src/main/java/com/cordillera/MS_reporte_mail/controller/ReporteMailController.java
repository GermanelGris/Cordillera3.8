package com.cordillera.MS_reporte_mail.controller;

import com.cordillera.MS_reporte_mail.dto.SolicitudReporteDto;
import com.cordillera.MS_reporte_mail.kafka.SolicitudReportePublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reportes-mail")
@RequiredArgsConstructor
public class ReporteMailController {

    private final SolicitudReportePublisher publisher;

    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(@Valid @RequestBody SolicitudReporteDto dto) {
        publisher.publicarSolicitud(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "estado", "ACEPTADO",
                "mensaje", "Solicitud enviada a MS-DATA. El reporte se generará y enviará a " + dto.getDestinatario()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MS-Reporte-Mail OK");
    }
}
