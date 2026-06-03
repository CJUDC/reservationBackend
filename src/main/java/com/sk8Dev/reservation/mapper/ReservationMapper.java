package com.sk8Dev.reservation.mapper;

import com.sk8Dev.reservation.dto.request.CreateReservationRequest;
import com.sk8Dev.reservation.dto.response.ReservationResponse;
import com.sk8Dev.reservation.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationEntity toEntity(CreateReservationRequest request) {
        var entity = new ReservationEntity();
        entity.setCustomerName(request.customerName());
        entity.setData(request.date());
        entity.setTime(request.time());
        entity.setService(request.service());
        return entity;
    }

    public ReservationResponse toResponse(ReservationEntity entity) {
        return new ReservationResponse(
                entity.getId(),
                entity.getCustomerName(),
                entity.getData(),
                entity.getTime(),
                entity.getService(),
                entity.getStatus()
        );
    }
}
