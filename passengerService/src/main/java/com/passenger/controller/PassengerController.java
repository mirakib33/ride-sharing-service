package com.passenger.controller;

import com.passenger.DTO.PassengerDTO;
import com.passenger.constants.PassengerConstants;
import com.passenger.entity.Passenger;
import com.passenger.service.PassengerService;
import com.passenger.utils.PassengerUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping(path = "/api/passenger")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/register")
    public ResponseEntity<String> registerPassenger(@Valid @RequestBody PassengerDTO passengerDTO) {
        try {
            ResponseEntity<String> response = passengerService.registerPassenger(passengerDTO);
            return new ResponseEntity<>(response.getBody(), response.getStatusCode());
        } catch (Exception e) {
            log.error("Error occurred while registering passenger", e);
        }
        return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/availableDrivers")
    public ResponseEntity<?> getAvailableDriver(@RequestParam("availableFrom") String availableFrom) {

    }

}
