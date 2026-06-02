package com.sk8Dev.reservation.service.impl;

import com.sk8Dev.reservation.entity.ReservationEntity;
import com.sk8Dev.reservation.entity.ReservationStatus;
import com.sk8Dev.reservation.exception.ReservationException;
import com.sk8Dev.reservation.repository.ReservationRepository;
import com.sk8Dev.reservation.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ReservationService} with business rule enforcement.
 */
@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Creates a new reservation only if no active reservation exists at the same date and time.
     *
     * @param reservation the reservation entity to create
     * @return the created reservation entity
     * @throws ReservationException if an active reservation already exists at the given date and time
     */
    @Override
    @Transactional
    public ReservationEntity createReservation(ReservationEntity reservation) {
        var exists = reservationRepository.existsByDataAndTimeAndStatus(
                reservation.getData(), reservation.getTime(), ReservationStatus.ACTIVO);
        if (exists) {
            log.warn("Reservation conflict at {} {} for customer {}", reservation.getData(), reservation.getTime(), reservation.getCustomerName());
            throw new ReservationException("An active reservation already exists at the same date and time");
        }
        reservation.setStatus(ReservationStatus.ACTIVO);
        var saved = reservationRepository.save(reservation);
        log.info("Reservation created with id {} for customer {}", saved.getId(), saved.getCustomerName());
        return saved;
    }

    /**
     * Cancels an existing reservation by its ID.
     *
     * @param id the ID of the reservation to cancel
     * @throws ReservationException if no reservation is found with the given ID
     */
    @Override
    @Transactional
    public void cancelReservation(Long id) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Reservation not found with id {}", id);
                    return new ReservationException("Reservation not found with id: " + id);
                });
        reservation.setStatus(ReservationStatus.CANCELADA);
        reservationRepository.save(reservation);
        log.info("Reservation with id {} cancelled", id);
    }
}
