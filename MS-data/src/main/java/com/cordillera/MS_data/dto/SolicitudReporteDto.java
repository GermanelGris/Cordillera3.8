package com.cordillera.MS_data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Solicitud de reporte recibida por Kafka desde MS-Reporte-Mail.
 * Contiene la información del correo y el formato deseado del adjunto.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolicitudReporteDto {

    private String destinatario;
    private String asunto;
    private String cuerpo;
    /** PDF o EXCEL */
    private String formato;
    /** Etiqueta opcional del periodo a mostrar en el reporte. */
    private String periodo;
}
