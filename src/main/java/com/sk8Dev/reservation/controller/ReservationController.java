package com.sk8Dev.reservation.controller;

import com.sk8Dev.reservation.dto.request.CreateReservationRequest;
import com.sk8Dev.reservation.dto.response.ReservationResponse;
import com.sk8Dev.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservations", description = "Reservation management endpoints")
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "List all reservations")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "List of reservations retrieved"))
    @GetMapping
    public List<ReservationResponse> findAll() {
        return reservationService.findAll();
    }

    @Operation(summary = "Create a new reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or scheduling conflict")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @Operation(summary = "Cancel a reservation by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled"),
            @ApiResponse(responseCode = "400", description = "Reservation not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        reservationService.cancelReservation(id);
    }
}
