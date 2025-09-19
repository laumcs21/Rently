package com.Rently.Persistence.Repository;

import com.Rently.Persistence.Entity.AlojamientoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlojamientoImagenRepository extends JpaRepository<AlojamientoImagen, Long> {
    List<AlojamientoImagen> findByAlojamientoIdOrderByOrdenAsc(Long alojamientoId);
}

