package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequest {

    @NotBlank(message = "Card number required")
    @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13..19 digits")
    private String cardNumber;

    @NotBlank(message = "Owner name required")
    private String ownerName;

    @NotBlank(message = "Expiry required")
    @Pattern(regexp = "\\d{4}-\\d{2}|\\d{2}/\\d{2}", message = "Expiry format YYYY-MM or MM/YY")
    private String expiry;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal initialBalance;
}
