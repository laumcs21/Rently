package com.Rently.Business.Service;

import com.Rently.Business.DTO.ReservaDTO;
import com.Rently.Persistence.Entity.EstadoReserva;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de reservas.
 */
public interface ReservaService {
    /**
     * Crea una nueva reserva.
     *
     * @param reservaDTO la reserva a crear
     * @return la reserva creada
     */
    ReservaDTO create(ReservaDTO reservaDTO);

    /**
     * Busca una reserva por su ID.
     *
     * @param id el ID de la reserva
     * @return un optional que contiene la reserva si se encuentra, o vacío en caso contrario
     */
    Optional<ReservaDTO> findById(Long id);

    /**
     * Devuelve una lista de todas las reservas.
     *
     * @return una lista de todas las reservas
     */
    List<ReservaDTO> findAll();

    /**
     * Busca reservas por el ID del usuario.
     *
     * @param userId el ID del usuario
     * @return una lista de reservas del usuario especificado
     */
    List<ReservaDTO> findByUserId(Long userId);

    /**
     * Busca reservas por el ID del alojamiento.
     *
     * @param alojamientoId el ID del alojamiento
     * @return una lista de reservas del alojamiento especificado
     */
    List<ReservaDTO> findByAlojamientoId(Long alojamientoId);

    /**
     * Actualiza una reserva existente.
     *
     * @param id         el ID de la reserva
     * @param reservaDTO la reserva con la información actualizada
     * @return un optional que contiene la reserva actualizada si se encuentra, o vacío en caso contrario
     */
    Optional<ReservaDTO> update(Long id, ReservaDTO reservaDTO);

    /**
     * Cancela una reserva por parte del usuario propietario aplicando las reglas de negocio.
     *
     * @param id el ID de la reserva a cancelar
     * @return la reserva cancelada
     */
    ReservaDTO cancelByUser(Long id);

    /**
     * Actualiza el estado de una reserva.
     *
     * @param id         el ID de la reserva
     * @param newState el nuevo estado de la reserva
     * @return true si el estado fue actualizado, false en caso contrario
     */
    boolean updateState(Long id, EstadoReserva newState);

    /**
     * Elimina una reserva por su ID.
     *
     * @param id el ID de la reserva
     * @return true si la reserva fue eliminada, false en caso contrario
     */
    boolean delete(Long id);
}
