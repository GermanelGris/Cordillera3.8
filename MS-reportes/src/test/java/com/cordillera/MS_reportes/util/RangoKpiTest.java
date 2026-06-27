package com.cordillera.MS_reportes.util;

import com.cordillera.MS_reportes.dto.KpiDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class RangoKpiTest {

    private KpiDto kpi(String nombre, String tipoCalc, String valor) {
        KpiDto k = new KpiDto();
        k.setNombre(nombre);
        k.setTipoCalculo(tipoCalc);
        if (valor != null) k.setValor(new BigDecimal(valor));
        return k;
    }

    @Test
    void ventas_sumaAlta_esOptimo() {
        assertThat(RangoKpi.etiquetaPara(kpi("Ventas", "SUMA", "1600000"))).contains("ÓPTIMO");
    }

    @Test
    void ventas_regular() {
        assertThat(RangoKpi.etiquetaPara(kpi("Ventas", "SUMA", "1200000"))).contains("REGULAR");
    }

    @Test
    void ventas_malo() {
        assertThat(RangoKpi.etiquetaPara(kpi("Ventas", "SUMA", "500000"))).contains("MALO");
    }

    @Test
    void costos_menosEsMejor() {
        assertThat(RangoKpi.evaluar("COSTOS", new BigDecimal("700000"))).isEqualTo(RangoKpi.OPTIMO);
        assertThat(RangoKpi.evaluar("COSTOS", new BigDecimal("900000"))).isEqualTo(RangoKpi.REGULAR);
        assertThat(RangoKpi.evaluar("COSTOS", new BigDecimal("2000000"))).isEqualTo(RangoKpi.MALO);
    }

    @Test
    void inventario_masEsMejor() {
        assertThat(RangoKpi.evaluar("INVENTARIO", new BigDecimal("2500000"))).isEqualTo(RangoKpi.OPTIMO);
        assertThat(RangoKpi.evaluar("PRODUCCION", new BigDecimal("5000"))).isEqualTo(RangoKpi.MALO);
    }

    @Test
    void tipoCalculoNoAplicable_retornaVacio() {
        assertThat(RangoKpi.etiquetaPara(kpi("Ventas", "MAXIMO", "1600000"))).isEmpty();
    }

    @Test
    void kpiNuloOTipoNulo_retornaVacio() {
        assertThat(RangoKpi.etiquetaPara(null)).isEmpty();
        assertThat(RangoKpi.etiquetaPara(kpi("Ventas", null, "100"))).isEmpty();
    }

    @Test
    void evaluar_conNulosODesconocido_retornaNull() {
        assertThat(RangoKpi.evaluar(null, new BigDecimal("1"))).isNull();
        assertThat(RangoKpi.evaluar("VENTAS", null)).isNull();
        assertThat(RangoKpi.evaluar("DESCONOCIDO", new BigDecimal("1"))).isNull();
    }

    @Test
    void inferirTipoDato_variasCategorias() {
        assertThat(RangoKpi.inferirTipoDato(kpi("Total de ventas", "SUMA", "1"))).isEqualTo("VENTAS");
        assertThat(RangoKpi.inferirTipoDato(kpi("Costo mensual", "SUMA", "1"))).isEqualTo("COSTOS");
        assertThat(RangoKpi.inferirTipoDato(kpi("Produccion total", "SUMA", "1"))).isEqualTo("PRODUCCION");
        assertThat(RangoKpi.inferirTipoDato(kpi("Otra cosa", "SUMA", "1"))).isNull();
    }

    @Test
    void getters() {
        assertThat(RangoKpi.OPTIMO.getEmoji()).isNotBlank();
        assertThat(RangoKpi.MALO.getEtiqueta()).isEqualTo("MALO");
    }
}
