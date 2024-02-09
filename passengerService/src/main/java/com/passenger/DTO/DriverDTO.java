package com.passenger.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverDTO {

    private String driverId;
    private String name;
    private String phone;
    private String vehicleType;
    private String city;
    private boolean status;
    private String availableFrom;

}
