package com.cordillera.MS_kpi.factory;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class KpiFactoryTest {

    private final KpiCalculatorRegistry registry = new KpiCalculatorRegistry();

    @Test
    void suma_calculaYExponeTipo() {
        KpiCalculatorCreator c = registry.obtenerCreador("SUMA");
        assertThat(c.calcular(List.of(new BigDecimal("100"), new BigDecimal("50")))).isEqualByComparingTo("150");
        assertThat(c.getTipo()).isEqualTo(TipoCalculo.SUMA);
    }

    @Test
    void promedio_calcula() {
        KpiCalculatorCreator c = registry.obtenerCreador("promedio"); // case-insensitive
        assertThat(c.calcular(List.of(new BigDecimal("100"), new BigDecimal("200")))).isEqualByComparingTo("150.0000");
    }

    @Test
    void maximo_calcula() {
        assertThat(registry.obtenerCreador("MAXIMO")
                .calcular(List.of(new BigDecimal("3"), new BigDecimal("9"), new BigDecimal("5"))))
                .isEqualByComparingTo("9");
    }

    @Test
    void minimo_calcula() {
        assertThat(registry.obtenerCreador("MINIMO")
                .calcular(List.of(new BigDecimal("3"), new BigDecimal("9"), new BigDecimal("5"))))
                .isEqualByComparingTo("3");
    }

    @Test
    void tipoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> registry.obtenerCreador("RAIZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void listaVacia_lanzaExcepcion() {
        assertThatThrownBy(() -> registry.obtenerCreador(TipoCalculo.SUMA).calcular(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede estar vacía");
    }

    @Test
    void getTipo_delegaAlProducto() {
        assertThat(registry.obtenerCreador(TipoCalculo.MAXIMO).getTipo()).isEqualTo(TipoCalculo.MAXIMO);
        assertThat(registry.obtenerCreador(TipoCalculo.MINIMO).getTipo()).isEqualTo(TipoCalculo.MINIMO);
        assertThat(registry.obtenerCreador(TipoCalculo.PROMEDIO).getTipo()).isEqualTo(TipoCalculo.PROMEDIO);
    }
}
