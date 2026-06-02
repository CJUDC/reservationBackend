package com.sk8Dev.reservation.exception;

/**
 * Custom exception thrown when reservation business rules are violated.
 */
public class ReservationException extends RuntimeException {

    public ReservationException(String message) {
        super(message);
    }
}
