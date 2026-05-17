package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.dto.KpiDto;
import com.cordillera.MS_kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiEventConsumer {

    private final KpiService kpiService;

    @KafkaListener(topics = "datos-ingresados", groupId = "kpi-group")
    public void consumirDatoIngresado(String mensaje) {
        log.info("KPI Consumer recibió evento datos-ingresados: {}", mensaje);
        try {
            // Extrae el valor del mensaje JSON de forma simple
            BigDecimal valor = extraerValor(mensaje);
            String periodo = extraerCampo(mensaje, "periodo");
            String tipo = extraerCampo(mensaje, "tipo");

            if (valor == null || periodo.isBlank()) return;

            KpiDto dto = new KpiDto();
            dto.setNombre("KPI Auto - " + tipo + " " + periodo);
            dto.setTipoCalculo("PROMEDIO");
            dto.setValores(List.of(valor));
            dto.setPeriodo(periodo);
            dto.setUnidad("CLP");
            dto.setDescripcion("Calculado automáticamente desde evento Kafka");

            kpiService.calcular(dto);
            log.info("KPI automático generado para tipo: {}, periodo: {}", tipo, periodo);
        } catch (Exception e) {
            log.error("Error procesando evento datos-ingresados: {}", e.getMessage());
        }
    }

    private BigDecimal extraerValor(String json) {
        try {
            int idx = json.indexOf("\"valor\":");
            if (idx < 0) return null;
            String sub = json.substring(idx + 8).trim();
            int end = sub.indexOf(',');
            if (end < 0) end = sub.indexOf('}');
            return new BigDecimal(sub.substring(0, end).trim());
        } catch (Exception e) {
            return null;
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
