package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DocumentoTabularServiceTest {

    private final DocumentoTabularService service = new DocumentoTabularService();

    @Test
    void generarTodos_devuelvePdfCsvYXlsx() {
        List<String> columnas = List.of("Campo", "Valor");
        List<List<String>> filas = List.of(
                List.of("Nombre", "KPI Ventas"),
                List.of("Valor", "150000"),
                List.of("Texto, con coma", "Con \"comillas\"") // fuerza el escape CSV
        );

        List<ArchivoReporte> archivos = service.generarTodos("Reporte de Prueba", columnas, filas);

        assertThat(archivos).hasSize(3);
        assertThat(archivos.get(0).nombre()).endsWith(".pdf");
        assertThat(archivos.get(0).contenido()).isNotEmpty();
        assertThat(archivos.get(1).nombre()).endsWith(".csv");
        assertThat(archivos.get(1).contenido()).isNotEmpty();
        assertThat(archivos.get(2).nombre()).endsWith(".xlsx");
        assertThat(archivos.get(2).contenido()).isNotEmpty();
        assertThat(archivos.get(2).contentType()).contains("spreadsheetml");
    }

    @Test
    void generarTodos_conTituloNuloYSinColumnas_usaNombreBasePorDefecto() {
        List<ArchivoReporte> archivos = service.generarTodos(null, null, null);

        assertThat(archivos).hasSize(3);
        assertThat(archivos.get(0).nombre()).isEqualTo("reporte.pdf");
        assertThat(archivos.get(1).nombre()).isEqualTo("reporte.csv");
        assertThat(archivos.get(2).nombre()).isEqualTo("reporte.xlsx");
    }
}
