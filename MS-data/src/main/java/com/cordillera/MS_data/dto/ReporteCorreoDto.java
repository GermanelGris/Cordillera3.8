package com.cordillera.MS_data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Mensaje recibido por Kafka para generar y enviar por correo un reporte/KPI
 * en PDF + CSV + XLSX. Lo publican MS-reportes y MS-kpi.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteCorreoDto {

    private String destinatario;
    private String asunto;
    private String cuerpo;
    private String titulo;
    private List<String> columnas;
    private List<List<String>> filas;
}
