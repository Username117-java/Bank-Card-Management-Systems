package com.example.bankcards.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private String expiry;
    private String status;
    private BigDecimal balance;
}
