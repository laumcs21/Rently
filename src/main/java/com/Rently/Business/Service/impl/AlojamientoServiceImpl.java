package com.Rently.Business.Service.impl;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.Service.AlojamientoService;
import com.Rently.Persistence.DAO.AlojamientoDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlojamientoServiceImpl implements AlojamientoService {

    private final AlojamientoDAO alojamientoDAO;

    public AlojamientoServiceImpl(AlojamientoDAO alojamientoDAO) {
        this.alojamientoDAO = alojamientoDAO;
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
}

