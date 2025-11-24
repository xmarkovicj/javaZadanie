package com.markovic.javazadanie.fx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupDto {
    private Long id;          // alebo groupId – uprav podľa backendu
    private String name;
    private String description;
    private String createdAt; // voliteľné
}
