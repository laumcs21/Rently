package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.ListingCardDTO;
import com.Rently.Persistence.Entity.Alojamiento;
import com.Rently.Persistence.Entity.AlojamientoImagen;
import com.Rently.Persistence.Mapper.AlojamientoMapper;
import com.Rently.Persistence.Repository.AlojamientoImagenRepository;
import com.Rently.Persistence.Repository.AlojamientoRepository;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Business.Service.ImageService;
import com.Rently.Persistence.DAO.AlojamientoDAO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlojamientoServiceImpl implements AlojamientoService {

    private final AlojamientoDAO alojamientoDAO;
    private final AlojamientoRepository repo;
    private final AlojamientoImagenRepository imagenRepo;
    private final AlojamientoMapper mapper;


    public AlojamientoServiceImpl(AlojamientoDAO alojamientoDAO, AlojamientoRepository repo, ImageService imageService, AlojamientoImagenRepository imagenRepo, AlojamientoMapper mapper) {
        this.alojamientoDAO = alojamientoDAO;
        this.repo = repo;
        this.imagenRepo = imagenRepo;
        this.mapper = mapper;
    }

    @Override
    public AlojamientoDTO create(AlojamientoDTO alojamientoDTO) {
        validateCreateData(alojamientoDTO);
        return alojamientoDAO.crearAlojamiento(alojamientoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlojamientoDTO> findById(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido");
        return alojamientoDAO.buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> findAll() {
        return alojamientoDAO.listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> findActive() {
        return alojamientoDAO.listarActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> findByCity(String city) {
        if (city == null || city.trim().isEmpty()) throw new IllegalArgumentException("Ciudad inválida");
        return alojamientoDAO.buscarPorCiudad(city.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> findByPrice(Double min, Double max) {
        if (min == null || max == null) throw new IllegalArgumentException("Rango de precios inválido");
        if (min < 0 || max < 0) throw new IllegalArgumentException("Los precios no pueden ser negativos");
        if (min > max) throw new IllegalArgumentException("El precio mínimo no puede ser mayor al máximo");
        return alojamientoDAO.buscarPorPrecio(min, max);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlojamientoDTO> findByHost(Long hostId) {
        if (hostId == null || hostId <= 0) throw new IllegalArgumentException("ID anfitrión inválido");
        return alojamientoDAO.buscarPorAnfitrion(hostId);
    }

    @Override
    public Optional<AlojamientoDTO> update(Long id, AlojamientoDTO alojamientoDTO) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido");
        validateUpdateData(alojamientoDTO);

        Optional<AlojamientoDTO> existing = alojamientoDAO.buscarPorId(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Alojamiento con ID " + id + " no encontrado");
        }

        return alojamientoDAO.actualizar(id, alojamientoDTO);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido");

        Optional<AlojamientoDTO> existing = alojamientoDAO.buscarPorId(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Alojamiento con ID " + id + " no encontrado");
        }

        return alojamientoDAO.eliminar(id);
    }


    // ---------------- Validaciones privadas ----------------

    private void validateCreateData(AlojamientoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Alojamiento es obligatorio");
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty())
            throw new IllegalArgumentException("titulo es obligatorio");
        if (dto.getPrecioPorNoche() == null || dto.getPrecioPorNoche() <= 0)
            throw new IllegalArgumentException("precioPorNoche debe ser mayor a 0");
        if (dto.getCapacidadMaxima() == null || dto.getCapacidadMaxima() <= 0)
            throw new IllegalArgumentException("capacidadMaxima debe ser mayor o igual a 1");
        if (dto.getAnfitrionId() == null || dto.getAnfitrionId() <= 0)
            throw new IllegalArgumentException("anfitrionId es obligatorio");
        // opcional: validar longitud/latitud si se proveen
        if (dto.getLatitud() != null && (dto.getLatitud() < -90 || dto.getLatitud() > 90))
            throw new IllegalArgumentException("latitud inválida");
        if (dto.getLongitud() != null && (dto.getLongitud() < -180 || dto.getLongitud() > 180))
            throw new IllegalArgumentException("longitud inválida");
    }

    private void validateUpdateData(AlojamientoDTO dto) {
        if (dto == null) return;
        if (dto.getTitulo() != null && dto.getTitulo().trim().isEmpty())
            throw new IllegalArgumentException("titulo no puede estar vacío");
        if (dto.getPrecioPorNoche() != null && dto.getPrecioPorNoche() <= 0)
            throw new IllegalArgumentException("precioPorNoche debe ser mayor a 0");
        if (dto.getCapacidadMaxima() != null && dto.getCapacidadMaxima() <= 0)
            throw new IllegalArgumentException("capacidadMaxima debe ser mayor o igual a 1");
    }

    /**
     * Obtiene alojamientos destacados aleatorios (con filtros opcionales).
     * @param precioMax   tope de precio (nullable)
     * @param servicios   nombres exactos de servicios (nullable o vacía)
     * @param limit       cantidad (1..24), por defecto 12
     */
    @Override
    public List<ListingCardDTO> getFeaturedRandom(Double precioMax, List<String> servicios, Integer limit) {
        int size = (limit == null || limit <= 0 || limit > 24) ? 12 : limit;
        Pageable page = PageRequest.of(0, size);
        List<String> filtrosServicios = (servicios == null || servicios.isEmpty()) ? null : servicios;

        // 1. el repo YA me devuelve ListingCardDTO (porque el @Query arma el DTO)
        List<ListingCardDTO> base = repo.findFeaturedRandom(precioMax, filtrosServicios, page);

        // 2. aquí los “enriquecemos” con la portada real (por si en BD hay más de una imagen)
        base.forEach(dto -> {
            String portada = imagenRepo
                    .findFirstByAlojamientoIdOrderByOrdenAsc(dto.getId())
                    .map(AlojamientoImagen::getUrl)
                    .orElse(dto.getPortadaUrl());   // por si ya venía del query

            dto.setPortadaUrl(portada);
        });
        return base;
    }

    @Override
    public void agregarImagen(Long alojamientoId, String urlImagen) {
        // Buscar el alojamiento
        Alojamiento alojamiento = repo.findById(alojamientoId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado con ID: " + alojamientoId));

        // Inicializar lista si está vacía o nula
        if (alojamiento.getImagenes() == null) {
            alojamiento.setImagenes(new ArrayList<>());
        }

        // ⬅️ evitar duplicados (mismo URL)
        boolean yaExiste = alojamiento.getImagenes().stream()
                .anyMatch(img -> urlImagen.equals(img.getUrl()));
        if (yaExiste) {
            return; // no guardamos otra vez
        }

        // calcular orden sencillito
        int orden = alojamiento.getImagenes().size() + 1;

        AlojamientoImagen imagen = new AlojamientoImagen(urlImagen, orden, alojamiento);
        alojamiento.getImagenes().add(imagen);

        // Guardar cambios
        repo.save(alojamiento);
    }

    @Override
    public void eliminarImagen(Long alojamientoId, String url) {
        Alojamiento alo = repo.findById(alojamientoId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        if (alo.getImagenes() == null) return;

        alo.getImagenes().removeIf(img -> url.equals(img.getUrl()));

        repo.save(alo);
    }



}

