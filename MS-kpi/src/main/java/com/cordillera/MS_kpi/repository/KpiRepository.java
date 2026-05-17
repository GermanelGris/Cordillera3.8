package com.cordillera.MS_kpi.repository;

import com.cordillera.MS_kpi.entity.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {

    List<Kpi> findByPeriodo(String periodo);

    List<Kpi> findByNombreContainingIgnoreCase(String nombre);

    List<Kpi> findByTipoCalculo(String tipoCalculo);

    List<Kpi> findByPeriodoAndTipoCalculo(String periodo, String tipoCalculo);
}
