package com.cordillera.MS_data.repository;

import com.cordillera.MS_data.entity.DatoIngresado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatoRepository extends JpaRepository<DatoIngresado, Long> {

    List<DatoIngresado> findByPeriodo(String periodo);

    List<DatoIngresado> findByTipo(String tipo);

    List<DatoIngresado> findByFuente(String fuente);

    List<DatoIngresado> findByPeriodoAndTipo(String periodo, String tipo);

    List<DatoIngresado> findByProcesado(Boolean procesado);
}
