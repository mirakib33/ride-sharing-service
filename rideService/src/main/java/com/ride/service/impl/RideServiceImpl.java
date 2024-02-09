package com.ride.service.impl;

import com.ride.DTO.RideDTO;
import com.ride.constants.RideConstants;
import com.ride.entity.Ride;
import com.ride.repository.RideRepository;
import com.ride.service.RideService;
import com.ride.utils.ResourceNotFoundException;
import com.ride.utils.RideUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ALL")
@Slf4j
@Service
public class RideServiceImpl implements RideService {

    @Autowired
    RideRepository rideRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ResponseEntity<String> rideRequest(RideDTO rideDTO) {
        try {
            Ride ride = dtoToRide(rideDTO);

            String rideId = UUID.randomUUID().toString();
            ride.setRideId(rideId);

            Date requestedOn = new Date();
            ride.setRequestedOn(requestedOn);

            rideRepository.saveAndFlush(ride);
            log.info("Ride requested successfully");
            return RideUtils.getResponseEntity("Ride requested successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error occurred while requesting for ride", e);
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getRideByPassengerId(String passengerId) {
        try {
            log.info("Getting ride by passenger id");
            Optional<Ride> ride = rideRepository.findByPassengerId(passengerId);
            if(!ride.isPresent()) {
                return new ResponseEntity<>("Ride does not exists with this id", HttpStatus.NOT_FOUND);
            }
            RideDTO rideDTO = rideToDto(ride.get());
            return new ResponseEntity<>(rideDTO, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while getting ride by passenger id", e);
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getRideByDriverId(String driverId) {
        try {
            log.info("Getting ride by driver id");
            Optional<Ride> ride = rideRepository.findByDriverId(driverId);
            if(!ride.isPresent()) {
                return new ResponseEntity<>("Ride does not exists with this id", HttpStatus.NOT_FOUND);
            }
            RideDTO rideDTO = rideToDto(ride.get());
            return new ResponseEntity<>(rideDTO, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while getting ride by driver id", e);
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateRideStatusByDriver(String rideId, RideDTO rideDTO) {
        try{
            Ride ride = rideRepository.findById(rideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));

            Date currentDateTime = new Date();
            String message = "";

            if(rideDTO.getStatus() != null & "Accepted".equals(rideDTO.getStatus())) {
                ride.setStatus(rideDTO.getStatus());
                ride.setAcceptedOn(currentDateTime);
                rideRepository.save(ride);
                message = "Ride accepted successfully";
            } else if("Start".equals(ride.getStatus())) {
                ride.setStatus(rideDTO.getStatus());
                ride.setStartTime(currentDateTime);
                rideRepository.save(ride);
                message = "Ride started";
            } else if("End".equals(ride.getStatus())) {
                ride.setStatus(rideDTO.getStatus());
                ride.setEndTime(currentDateTime);
                rideRepository.save(ride);
                message = "Ride ended";
            } else {
                ride.setStatus("Cancelled");
                ride.setStatus(rideDTO.getStatus());
                rideRepository.save(ride);
                message = "Ride cancelled successfully";
            }
            log.info("Ride status updated successfully by driver");
            return RideUtils.getResponseEntity(message, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while updating ride status by driver", e);
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateRideStatusByPassenger(String rideId, RideDTO rideDTO) {
        try{
            Ride ride = rideRepository.findById(rideId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));

            if(rideDTO.getStatus() != null) {
                ride.setStatus(rideDTO.getStatus());
            }
            rideRepository.saveAndFlush(ride);
            log.info("Ride status updated successfully by passenger");
            return RideUtils.getResponseEntity("Ride cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while updating ride status by passenger", e);
        }
        return RideUtils.getResponseEntity(RideConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public Ride dtoToRide(RideDTO rideDTO) {
        return this.modelMapper.map(rideDTO, Ride.class);
    }

    public RideDTO rideToDto(Ride ride) {
        return this.modelMapper.map(ride, RideDTO.class);
    }

}
