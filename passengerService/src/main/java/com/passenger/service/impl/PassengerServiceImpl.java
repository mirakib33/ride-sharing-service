package com.passenger.service.impl;

import com.passenger.DTO.PassengerDTO;
import com.passenger.DTO.RideDTO;
import com.passenger.clients.DriverClient;
import com.passenger.clients.RideClient;
import com.passenger.constants.PassengerConstants;
import com.passenger.DTO.DriverDTO;
import com.passenger.entity.Passenger;
import com.passenger.repository.PassengerRepository;
import com.passenger.service.PassengerService;
import com.passenger.utils.PassengerUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RestTemplate restTemplate;

    private final DriverClient driverClient;

    private final RideClient rideClient;

    @Autowired
    public PassengerServiceImpl(DriverClient driverClient, RideClient rideClient) {

        this.driverClient = driverClient;
        this.rideClient = rideClient;

    }


    private String startFrom = "";
    private String vehicleType = "";
    private String endPlace = "";
    private Double totalFare = 0.00;


    @Override
    public ResponseEntity<String> registerPassenger(PassengerDTO passengerDTO) {

        try {
            Passenger passenger = dtoToPassenger(passengerDTO);

            String passengerId = UUID.randomUUID().toString();
            passenger.setPassengerId(passengerId);

            passengerRepository.saveAndFlush(passenger);
            log.info("Passenger registered successfully");
            return PassengerUtils.getResponseEntity("Passenger registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while registering passenger", e);
        }
        return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getPassengerById(String passengerId) {
        try {
            log.info("Getting passenger by passenger id");
            Optional<Passenger> passenger = passengerRepository.findById(passengerId);

            if (passenger.isPresent()) {
                PassengerDTO passengerDTO = passengerToDto(passenger.get());
                return new ResponseEntity<>(passengerDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Passenger does not exist with this id", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error occurred while getting passenger by passenger id", e);
            return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAvailableDrivers(String availableFrom, String type) {
        try {
            startFrom = availableFrom;
            vehicleType = type;
            ResponseEntity<?> response = driverClient.getAvailableDriver(availableFrom, type);
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while getting available drivers", e);
        }
        return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<?> getFareByDistanceAndType(String destination) {
        try{
            endPlace = destination;
            Double distanceInMeters = getDistance(destination);
            Double fare = calculateFare(distanceInMeters);
            totalFare = fare;
            log.info("Fare calculated successfully");
            return new ResponseEntity<>(fare, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while getting fare", e);
        }
        return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> rideRequest(String passengerId, String driverId) {
        try{
            RideDTO rideDTO = new RideDTO();
            if(passengerId != null && !passengerId.equals("")) {
                rideDTO.setPassengerId(passengerId);
            } else {
                throw new IllegalStateException("passengerId is required for ride request");
            }

            if(driverId != null && !driverId.equals("")) {
                rideDTO.setDriverId(driverId);
            } else {
                throw new IllegalStateException("driverId is required for ride request");
            }

            if(startFrom != null && !startFrom.equals("")) {
                rideDTO.setStartFrom(startFrom);
            } else {
                throw new IllegalStateException("startFrom is required for ride request");
            }

            if(endPlace != null && !endPlace.equals("")) {
                rideDTO.setDestination(endPlace);
            } else {
                throw new IllegalStateException("endPlace is required for ride request");
            }

            if(totalFare != null && !totalFare.equals("")) {
                rideDTO.setFare(totalFare);
            } else {
                throw new IllegalStateException("Passenger ID is required for ride request");
            }

            ResponseEntity<String> response = rideClient.rideRequest(rideDTO);
            log.info("Ride requested successfully");
            return PassengerUtils.getResponseEntity(response.getBody(), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while registering passenger", e);
        }
        return PassengerUtils.getResponseEntity(PassengerConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public Passenger dtoToPassenger(PassengerDTO passengerDTO) {
        return this.modelMapper.map(passengerDTO, Passenger.class);
    }

    public PassengerDTO passengerToDto(Passenger passenger) {
        return this.modelMapper.map(passenger, PassengerDTO.class);
    }

    private Double calculateFare(Double distanceInMeters) {

            Double distanceInKm = distanceInMeters / 1000.0;
            Double distanceFare = distanceInKm * PassengerConstants.RATE_PER_KM;
            Double actualFare = 0.00;

            if ("Small".equalsIgnoreCase(vehicleType)) {
                actualFare = distanceFare + PassengerConstants.SMALL_TYPE_RATE;
            } else if ("Medium".equalsIgnoreCase(vehicleType)) {
                actualFare = distanceFare + PassengerConstants.MEDIUM_TYPE_RATE;
            } else if ("Large".equalsIgnoreCase(vehicleType)) {
                actualFare = distanceFare + PassengerConstants.LARGE_TYPE_RATE;
            }

        Random random = new Random();
        Integer randomFare = random.nextInt(90) + 10;

            log.info("Fare calculated successfully");
            return actualFare + randomFare;
    }

    private Double getDistance(String destination) {

//            MapboxDirections mapboxDirections = MapboxDirections.builder()
//                    .accessToken(PassengerConstants.MAPBOX_ACCESS_TOKEN)
//                    .origin(startFrom)
//                    .destination(destination)
//                    .profile(DirectionsCriteria.PROFILE_DRIVING) // Specify travel mode
//                    .build();
//
//            Response<DirectionsResponse> directionsResponse = mapboxDirections.executeCall();
//
//            DirectionsRoute route = directionsResponse.body().routes().get(0);
//            Double distance = route.distance(); // Get distance in meters
//
//            return distance;

            Random random = new Random();
            Double distance = Math.round(random.nextDouble() * 100000.0) / 100000.0 + (random.nextInt(900) + 100);
            return distance;
    }


}
