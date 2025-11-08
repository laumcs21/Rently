package com.Rently.Business.Service;

import com.Rently.Business.DTO.AlojamientoDTO;
import com.Rently.Business.DTO.ListingCardDTO;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de alojamientos.
 */
public interface AlojamientoService {
    /**
     * Crea un nuevo alojamiento.
     *
     * @param alojamientoDTO el alojamiento a crear
     * @return el alojamiento creado
     */
    AlojamientoDTO create(AlojamientoDTO alojamientoDTO);

    /**
     * Busca un alojamiento por su ID.
     *
     * @param id el ID del alojamiento
     * @return un optional que contiene el alojamiento si se encuentra, o vacío en caso contrario
     */
    Optional<AlojamientoDTO> findById(Long id);

    /**
     * Devuelve una lista de todos los alojamientos.
     *
     * @return una lista de todos los alojamientos
     */
    List<AlojamientoDTO> findAll();

    /**
     * Devuelve una lista de todos los alojamientos activos.
     *
     * @return una lista de todos los alojamientos activos
     */
    List<AlojamientoDTO> findActive();

    /**
     * Busca alojamientos por ciudad.
     *
     * @param city la ciudad por la que buscar
     * @return una lista de alojamientos en la ciudad especificada
     */
    List<AlojamientoDTO> findByCity(String city);

    /**
     * Busca alojamientos por un rango de precios.
     *
     * @param min el precio mínimo
     * @param max el precio máximo
     * @return una lista de alojamientos dentro del rango de precios especificado
     */
    List<AlojamientoDTO> findByPrice(Double min, Double max);

    /**
     * Busca alojamientos por el ID del anfitrión.
     *
     * @param hostId el ID del anfitrión
     * @return una lista de alojamientos del anfitrión especificado
     */
    List<AlojamientoDTO> findByHost(Long hostId);

    /**
     * Actualiza un alojamiento existente.
     *
     * @param id             el ID del alojamiento
     * @param alojamientoDTO el alojamiento con la información actualizada
     * @return un optional que contiene el alojamiento actualizado si se encuentra, o vacío en caso contrario
     */
    Optional<AlojamientoDTO> update(Long id, AlojamientoDTO alojamientoDTO);

    /**
     * Elimina un alojamiento por su ID.
     *
     * @param id el ID del alojamiento
     * @return true si el alojamiento fue eliminado, false en caso contrario
     */
    boolean delete(Long id);

    List<ListingCardDTO> getFeaturedRandom(Double precioMax, List<String> servicios, Integer limit);

    void agregarImagen(Long alojamientoId, String urlImagen);

    void eliminarImagen(Long alojamientoId, String url);

    }
