package com.markovic.javazadanie.fx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String createdDate; // alebo createdAt podľa backendu
}
