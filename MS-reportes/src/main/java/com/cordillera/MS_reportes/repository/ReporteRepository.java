package com.cordillera.MS_reportes.repository;

import com.cordillera.MS_reportes.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByPeriodo(String periodo);

    List<Reporte> findByTipo(String tipo);

    List<Reporte> findByEstado(String estado);

    List<Reporte> findByPeriodoAndTipo(String periodo, String tipo);
}
