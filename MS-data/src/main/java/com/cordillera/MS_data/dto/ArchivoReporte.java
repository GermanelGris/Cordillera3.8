package com.cordillera.MS_data.dto;

/**
 * Archivo de reporte generado (en memoria) listo para adjuntarse a un correo.
 */
public record ArchivoReporte(String nombre, String contentType, byte[] contenido) {
}
