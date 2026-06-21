package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticaStockService {

    private final InventarioRepository inventarioRepository;

    /** Calcula las estadísticas (suma, promedio, mediana, máximo, mínimo) sobre el stock del inventario. */
    public EstadisticasStock calcular() {
        List<Inventario> inventarios = inventarioRepository.findAll();

        if (inventarios.isEmpty()) {
            return EstadisticasStock.builder()
                    .cantidadProductos(0)
                    .sumaStock(0)
                    .promedioStock(0)
                    .medianaStock(0)
                    .stockMaximo(0)
                    .stockMinimo(0)
                    .build();
        }

        int[] stocks = inventarios.stream()
                .mapToInt(Inventario::getStock)
                .sorted()
                .toArray();

        long suma = 0;
        for (int s : stocks) suma += s;

        double promedio = (double) suma / stocks.length;
        double mediana = calcularMediana(stocks);

        return EstadisticasStock.builder()
                .cantidadProductos(stocks.length)
                .sumaStock(suma)
                .promedioStock(promedio)
                .medianaStock(mediana)
                .stockMaximo(stocks[stocks.length - 1])
                .stockMinimo(stocks[0])
                .build();
    }

    /** Recupera el inventario ordenado por nombre para el detalle del reporte. */
    public List<Inventario> listarInventario() {
        return inventarioRepository.findAll();
    }

    private double calcularMediana(int[] ordenados) {
        int n = ordenados.length;
        if (n % 2 == 1) {
            return ordenados[n / 2];
        }
        return (ordenados[n / 2 - 1] + ordenados[n / 2]) / 2.0;
    }
}
