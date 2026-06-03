package com.sk8Dev.reservation.service;

import com.sk8Dev.reservation.dto.request.CreateReservationRequest;
import com.sk8Dev.reservation.dto.response.ReservationResponse;

import java.util.List;

/**
 * Service interface for managing reservations.
 */
public interface ReservationService {

    /**
     * Returns all reservations.
     *
     * @return list of all reservation responses
     */
    List<ReservationResponse> findAll();

    /**
     * Creates a new reservation if no active reservation exists at the same date and time.
     *
     * @param request the reservation creation request
     * @return the created reservation as a response DTO
     * @throws com.sk8Dev.reservation.exception.ReservationException if a conflict is detected
     */
    ReservationResponse createReservation(CreateReservationRequest request);

    /**
     * Cancels an existing reservation by its ID.
     *
     * @param id the ID of the reservation to cancel
     * @throws com.sk8Dev.reservation.exception.ReservationException if the reservation is not found
     */
    void cancelReservation(Long id);
}
