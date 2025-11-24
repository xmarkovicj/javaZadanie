package com.markovic.javazadanie.fx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pre člena študijnej skupiny.
 * Zodpovedá tomu, čo backend vracia v membership / group detail API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String name;
    private String email;
    private String role; // napr. "STUDENT", "TEACHER" alebo "ADMIN"
}
