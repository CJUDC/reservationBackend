package com.sk8Dev.reservation.service;

import com.sk8Dev.reservation.entity.ReservationEntity;

/**
 * Service interface for managing reservations.
 */
public interface ReservationService {

    /**
     * Creates a new reservation if no active reservation exists at the same date and time.
     *
     * @param reservation the reservation entity to create
     * @return the created reservation entity
     * @throws com.sk8Dev.reservation.exception.ReservationException if a conflict is detected
     */
    ReservationEntity createReservation(ReservationEntity reservation);

    /**
     * Cancels an existing reservation by its ID.
     *
     * @param id the ID of the reservation to cancel
     * @throws com.sk8Dev.reservation.exception.ReservationException if the reservation is not found
     */
    void cancelReservation(Long id);
}
