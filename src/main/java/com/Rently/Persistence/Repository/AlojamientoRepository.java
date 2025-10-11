package com.Rently.Persistence.Repository;

import com.Rently.Persistence.Entity.Alojamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlojamientoRepository extends JpaRepository<Alojamiento, Long> {
    List<Alojamiento> findByCiudadAndEliminadoFalse(String ciudad);
    List<Alojamiento> findByPrecioPorNocheBetweenAndEliminadoFalse(Double min, Double max);
    List<Alojamiento> findByAnfitrionId(Long anfitrionId);

    Optional<Alojamiento> findByIdAndEliminadoFalse(Long id);
    List<Alojamiento> findAllByEliminadoFalse();
    List<Alojamiento> findByAnfitrionIdAndEliminadoFalse(Long anfitrionId);

}

