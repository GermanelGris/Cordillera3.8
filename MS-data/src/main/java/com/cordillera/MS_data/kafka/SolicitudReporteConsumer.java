package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.dto.SolicitudReporteDto;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.service.EmailService;
import com.cordillera.MS_data.service.EstadisticaStockService;
import com.cordillera.MS_data.service.ReporteGeneradorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudReporteConsumer {

    private final ObjectMapper objectMapper;
    private final EstadisticaStockService estadisticaStockService;
    private final ReporteGeneradorService reporteGeneradorService;
    private final EmailService emailService;

    @KafkaListener(topics = "solicitud-reporte", groupId = "ms-data-group")
    public void consumir(String mensaje) {
        log.info("MS-DATA recibió solicitud de reporte: {}", mensaje);
        try {
            SolicitudReporteDto solicitud = objectMapper.readValue(mensaje, SolicitudReporteDto.class);

            if (solicitud.getDestinatario() == null || solicitud.getDestinatario().isBlank()) {
                log.warn("Solicitud de reporte sin destinatario, se descarta");
                return;
            }
            String formato = solicitud.getFormato() == null ? "PDF" : solicitud.getFormato().toUpperCase();

            // 1. Calcula estadísticas de stock y obtiene el detalle del inventario
            EstadisticasStock stats = estadisticaStockService.calcular();
            List<Inventario> inventario = estadisticaStockService.listarInventario();

            // 2. Genera el reporte en el formato solicitado (PDF o EXCEL)
            ArchivoReporte archivo = reporteGeneradorService.generar(
                    formato, solicitud.getPeriodo(), stats, inventario);

            // 3. Envía el correo con el reporte adjunto
            emailService.enviarConAdjunto(
                    solicitud.getDestinatario(), solicitud.getAsunto(), solicitud.getCuerpo(), archivo);

            log.info("Reporte {} enviado correctamente a {}", archivo.nombre(), solicitud.getDestinatario());
        } catch (Exception e) {
            log.error("Error procesando solicitud de reporte: {}", e.getMessage(), e);
        }
    }
}
