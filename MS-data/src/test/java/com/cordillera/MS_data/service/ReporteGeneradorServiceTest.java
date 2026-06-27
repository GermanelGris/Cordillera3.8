package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.entity.Inventario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReporteGeneradorServiceTest {

    private final ReporteGeneradorService service = new ReporteGeneradorService();

    private EstadisticasStock stats() {
        return EstadisticasStock.builder()
                .cantidadProductos(2).sumaStock(30).promedioStock(15.0)
                .medianaStock(15.0).stockMaximo(20).stockMinimo(10).build();
    }

    private List<Inventario> inventario() {
        return List.of(
                Inventario.builder().id(1L).productoId(100).nombre("Producto A").stock(10).build(),
                Inventario.builder().id(2L).productoId(200).nombre("Producto B").stock(20).build()
        );
    }

    @Test
    void generar_pdf_devuelveArchivoPdf() {
        ArchivoReporte archivo = service.generar("PDF", "2026-05", stats(), inventario());

        assertThat(archivo.nombre()).isEqualTo("reporte-stock.pdf");
        assertThat(archivo.contentType()).isEqualTo("application/pdf");
        assertThat(archivo.contenido()).isNotEmpty();
    }

    @Test
    void generar_excel_devuelveArchivoXlsx() {
        ArchivoReporte archivo = service.generar("EXCEL", "2026-05", stats(), inventario());

        assertThat(archivo.nombre()).isEqualTo("reporte-stock.xlsx");
        assertThat(archivo.contentType()).contains("spreadsheetml");
        assertThat(archivo.contenido()).isNotEmpty();
    }

    @Test
    void generar_formatoNuloOPeriodoVacio_generaPdfPorDefecto() {
        ArchivoReporte archivo = service.generar(null, "", stats(), inventario());

        assertThat(archivo.nombre()).endsWith(".pdf");
        assertThat(archivo.contenido()).isNotEmpty();
    }
}
