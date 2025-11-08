package com.Rently.Persistence.Repository;

import com.Rently.Business.DTO.ListingCardDTO;
import com.Rently.Persistence.Entity.Alojamiento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AlojamientoRepository extends JpaRepository<Alojamiento, Long> {
    List<Alojamiento> findByCiudadAndEliminadoFalse(String ciudad);

    List<Alojamiento> findByPrecioPorNocheBetweenAndEliminadoFalse(Double min, Double max);

    List<Alojamiento> findByAnfitrionId(Long anfitrionId);

    Optional<Alojamiento> findByIdAndEliminadoFalse(Long id);

    List<Alojamiento> findAllByEliminadoFalse();

    List<Alojamiento> findByAnfitrionIdAndEliminadoFalse(Long anfitrionId);


    @Query("""
            SELECT new com.Rently.Business.DTO.ListingCardDTO(
                a.id,
                a.titulo,
                a.ciudad,
                a.precioPorNoche,
                COALESCE(MIN(i.url), '')
            )
            FROM Alojamiento a
            LEFT JOIN a.imagenes i
            WHERE (:precioMax IS NULL OR a.precioPorNoche <= :precioMax)
              AND (
                   :servicios IS NULL
                   OR EXISTS (
                       SELECT 1 FROM a.servicios s
                       WHERE s.nombre IN :servicios
                   )
              )
            GROUP BY a.id, a.titulo, a.ciudad, a.precioPorNoche
            ORDER BY function('RAND')
            """)
    List<ListingCardDTO> findFeaturedRandom(
            @Param("precioMax") Double precioMax,
            @Param("servicios") List<String> servicios,
            Pageable pageable
    );
}