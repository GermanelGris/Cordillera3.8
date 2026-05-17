package com.cordillera.MS_data.repository;

import com.cordillera.MS_data.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Integer productoId);

    @Modifying
    @Query("UPDATE Inventario i SET i.stock = i.stock - :cantidad WHERE i.productoId = :productoId AND i.stock >= :cantidad")
    int descontarStock(@Param("productoId") Integer productoId, @Param("cantidad") Integer cantidad);
}
