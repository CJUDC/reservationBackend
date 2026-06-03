package com.sk8Dev.reservation.service.impl;

import com.sk8Dev.reservation.dto.request.CreateReservationRequest;
import com.sk8Dev.reservation.dto.response.ReservationResponse;
import com.sk8Dev.reservation.entity.ReservationEntity;
import com.sk8Dev.reservation.entity.ReservationStatus;
import com.sk8Dev.reservation.exception.ReservationException;
import com.sk8Dev.reservation.mapper.ReservationMapper;
import com.sk8Dev.reservation.repository.ReservationRepository;
import com.sk8Dev.reservation.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ReservationService} with business rule enforcement.
 */
@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(ReservationRepository reservationRepository, ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    /**
     * Returns all reservations mapped to response DTOs.
     *
     * @return list of all reservation responses
     */
    @Override
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new reservation only if no active reservation exists at the same date and time.
     *
     * @param request the reservation creation request
     * @return the created reservation as a response DTO
     * @throws ReservationException if an active reservation already exists at the given date and time
     */
    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        var entity = reservationMapper.toEntity(request);
        var exists = reservationRepository.existsByDataAndTimeAndStatus(
                entity.getData(), entity.getTime(), ReservationStatus.ACTIVE);
        if (exists) {
            log.warn("Reservation conflict at {} {} for customer {}", entity.getData(), entity.getTime(), entity.getCustomerName());
            throw new ReservationException("An active reservation already exists at the same date and time");
        }
        entity.setStatus(ReservationStatus.ACTIVE);
        var saved = reservationRepository.save(entity);
        log.info("Reservation created with id {} for customer {}", saved.getId(), saved.getCustomerName());
        return reservationMapper.toResponse(saved);
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
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Reservation with id {} cancelled", id);
    }
}
