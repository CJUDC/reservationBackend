package com.sk8Dev.reservation.repository;

import com.sk8Dev.reservation.entity.ReservationEntity;
import com.sk8Dev.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    boolean existsByDataAndTimeAndStatus(LocalDate data, LocalTime time, ReservationStatus status);

    boolean existsByDataAndTime(LocalDate data, LocalTime time);
}
