package com.cordillera.MS_reportes.factory;

import com.cordillera.MS_reportes.dto.DatoDto;
import com.cordillera.MS_reportes.dto.KpiDto;
import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.entity.Reporte;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReporteFactoryTest {

    private final ReporteCreadorRegistry registry = new ReporteCreadorRegistry();

    private ReporteDto dto(String tipo) {
        ReporteDto d = new ReporteDto();
        d.setTipo(tipo);
        d.setTitulo("Reporte " + tipo);
        d.setPeriodo("2026-05");
        d.setGeneradoPor("tester");
        d.setDescripcionAdicional("observaciones");
        return d;
    }

    private KpiDto kpi(String nombre, String tipoCalc, String valor) {
        KpiDto k = new KpiDto();
        k.setNombre(nombre);
        k.setTipoCalculo(tipoCalc);
        k.setValor(new BigDecimal(valor));
        k.setUnidad("CLP");
        k.setPeriodo("2026-05");
        return k;
    }

    private DatoDto dato(String tipo, String valor) {
        DatoDto d = new DatoDto();
        d.setTipo(tipo);
        d.setValor(new BigDecimal(valor));
        d.setPeriodo("2026-05");
        return d;
    }

    @Test
    void creadorKpi_construyeReporteConKpis() {
        Reporte r = registry.obtenerCreador("KPI")
                .construirReporte(dto("KPI"), List.of(kpi("Ventas", "SUMA", "1600000")), List.of());

        assertThat(r.getTipo()).isEqualTo("KPI");
        assertThat(r.getContenido()).contains("REPORTE DE KPIs").contains("Ventas");
        assertThat(r.getEstado()).isEqualTo("GENERADO");
        assertThat(r.getGeneradoPor()).isEqualTo("tester");
    }

    @Test
    void creadorKpi_sinKpis_indicaVacio() {
        Reporte r = registry.obtenerCreador("KPI").construirReporte(dto("KPI"), List.of(), List.of());

        assertThat(r.getContenido()).contains("No se encontraron KPIs");
    }

    @Test
    void creadorMensual_agrupaDatosPorTipo() {
        Reporte r = registry.obtenerCreador("MENSUAL").construirReporte(
                dto("MENSUAL"),
                List.of(kpi("Ventas", "SUMA", "1000000")),
                List.of(dato("VENTA", "100"), dato("VENTA", "200"), dato("GASTO", "50")));

        assertThat(r.getTipo()).isEqualTo("MENSUAL");
        assertThat(r.getContenido()).contains("REPORTE MENSUAL").contains("VENTA");
    }

    @Test
    void creadorResumen_muestraTotales() {
        Reporte r = registry.obtenerCreador("resumen").construirReporte(
                dto("RESUMEN"),
                List.of(kpi("Ventas", "SUMA", "1600000")),
                List.of(dato("VENTA", "100")));

        assertThat(r.getTipo()).isEqualTo("RESUMEN");
        assertThat(r.getContenido()).contains("RESUMEN EJECUTIVO").contains("Total de datos");
    }

    @Test
    void tipoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> registry.obtenerCreador("XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }
}
