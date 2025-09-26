package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_number", nullable = false, columnDefinition = "text")
    private String encryptedNumber;

    @Column(name = "masked_number", nullable = false)
    private String maskedNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    // Сохраняем как YearMonth.toString() (YYYY-MM)
    @Column(name = "expiry", nullable = false)
    private String expiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
