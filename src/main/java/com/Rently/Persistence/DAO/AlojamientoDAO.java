package com.Rently.Persistence.DAO;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Persistence.Entity.Alojamiento;
import com.Rently.Persistence.Entity.Anfitrion;
import com.Rently.Persistence.Entity.Servicio;
import com.Rently.Persistence.Mapper.AlojamientoMapper;
import com.Rently.Persistence.Repository.AlojamientoRepository;
import com.Rently.Persistence.Repository.AnfitrionRepository;
import com.Rently.Persistence.Repository.ServicioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class AlojamientoDAO {

    private final AlojamientoRepository alojamientoRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final ServicioRepository servicioRepository;
    private final AlojamientoMapper alojamientoMapper;

    public AlojamientoDAO(AlojamientoRepository alojamientoRepository,
                          AlojamientoMapper alojamientoMapper,
                          AnfitrionRepository anfitrionRepository,
                          ServicioRepository servicioRepository) {
        this.alojamientoRepository = alojamientoRepository;
        this.alojamientoMapper = alojamientoMapper;
        this.anfitrionRepository = anfitrionRepository;
        this.servicioRepository = servicioRepository;
    }

    @Transactional
    public AlojamientoDTO crearAlojamiento(AlojamientoDTO dto) {
        Alojamiento alojamiento = alojamientoMapper.toEntity(dto);

        // Setear anfitrión
        Anfitrion anfitrion = anfitrionRepository.findById(dto.getAnfitrionId())
                .orElseThrow(() -> new RuntimeException("El anfitrión no existe"));
        alojamiento.setAnfitrion(anfitrion);

        // Setear servicios
        if (dto.getServiciosId() != null && !dto.getServiciosId().isEmpty()) {
            List<Servicio> servicios = servicioRepository.findAllById(dto.getServiciosId());
            alojamiento.setServicios(servicios);
        }

        // Guardar alojamiento
        Alojamiento saved = alojamientoRepository.save(alojamiento);

        // Recargar para asegurar relaciones
        Alojamiento fullyLoaded = alojamientoRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        return alojamientoMapper.toDTO(fullyLoaded);
    }

    public Optional<AlojamientoDTO> actualizar(Long id, AlojamientoDTO dto) {
        // 🔒 No permitir actualizar registros soft-deleted
        return alojamientoRepository.findByIdAndEliminadoFalse(id).map(alojamiento -> {
            alojamientoMapper.updateFromDTO(alojamiento, dto);

            // Actualizar servicios si vienen IDs (null = no tocar)
            if (dto.getServiciosId() != null) {
                List<Servicio> servicios = servicioRepository.findAllById(dto.getServiciosId());
                alojamiento.setServicios(servicios);
            }

            Alojamiento actualizado = alojamientoRepository.save(alojamiento);
            return alojamientoMapper.toDTO(actualizado);
        });
    }

    // ======= LECTURAS (todas filtran eliminado=false) =======

    public Optional<AlojamientoDTO> buscarPorId(Long id) {
        return alojamientoRepository.findByIdAndEliminadoFalse(id)
                .map(alojamientoMapper::toDTO);
    }

    /** Suele ser lo que tu capa de servicio expone como "findAll" en los tests */
    public List<AlojamientoDTO> listarTodos() {
        return alojamientoMapper.toDTOList(alojamientoRepository.findAllByEliminadoFalse());
    }

    /** Si quieres mantener un "listarActivos" separado, no uses stream; consulta filtrada en BD */
    public List<AlojamientoDTO> listarActivos() {
        return alojamientoMapper.toDTOList(alojamientoRepository.findAllByEliminadoFalse());
    }

    public List<AlojamientoDTO> buscarPorCiudad(String ciudad) {
        return alojamientoMapper.toDTOList(alojamientoRepository.findByCiudadAndEliminadoFalse(ciudad));
    }

    public List<AlojamientoDTO> buscarPorPrecio(Double min, Double max) {
        return alojamientoMapper.toDTOList(
                alojamientoRepository.findByPrecioPorNocheBetweenAndEliminadoFalse(min, max)
        );
    }

    public List<AlojamientoDTO> buscarPorAnfitrion(Long anfitrionId) {
        return alojamientoMapper.toDTOList(
                alojamientoRepository.findByAnfitrionIdAndEliminadoFalse(anfitrionId)
        );
    }

    // ======= SOFT DELETE =======

    public boolean eliminar(Long id) {
        return alojamientoRepository.findById(id).map(alojamiento -> {
            if (!alojamiento.isEliminado()) {
                alojamiento.setEliminado(true);
                alojamientoRepository.save(alojamiento);
            }
            return true;
        }).orElse(false);
    }
}

