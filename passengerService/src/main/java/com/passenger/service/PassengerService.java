package com.passenger.service;


import com.passenger.entity.Passenger;
import org.springframework.http.ResponseEntity;

public interface PassengerService {
    public ResponseEntity<String> registerPassenger(Passenger passenger);
}
