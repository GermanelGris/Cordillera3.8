package com.cordillera.MS_reportes.kafka;

import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteEventConsumer {

    private final ReporteService reporteService;

    @KafkaListener(topics = "kpi-calculado", groupId = "reportes-group")
    public void consumirKpiCalculado(String mensaje) {
        log.info("Reporte Consumer recibió evento kpi-calculado: {}", mensaje);
        try {
            String periodo = extraerCampo(mensaje, "periodo");
            String nombre  = extraerCampo(mensaje, "nombre");

            if (periodo.isBlank()) return;

            ReporteDto dto = new ReporteDto();
            dto.setTipo("KPI");
            dto.setTitulo("Reporte KPI Auto - " + nombre + " - " + periodo);
            dto.setPeriodo(periodo);
            dto.setGeneradoPor("kafka-consumer");
            dto.setDescripcionAdicional("Generado automáticamente al recibir KPI calculado: " + nombre);

            reporteService.generar(dto);
            log.info("Reporte KPI automático generado para periodo: {}", periodo);
        } catch (Exception e) {
            log.error("Error procesando evento kpi-calculado: {}", e.getMessage());
        }
    }

    private String extraerCampo(String json, String campo) {
        try {
            String key = "\"" + campo + "\":\"";
            int idx = json.indexOf(key);
            if (idx < 0) return "";
            String sub = json.substring(idx + key.length());
            return sub.substring(0, sub.indexOf('"'));
        } catch (Exception e) {
            return "";
        }
    }
}
