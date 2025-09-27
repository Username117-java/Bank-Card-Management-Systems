package com.example.bankcards.service;

import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.AesEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;   // 👈 добавляем
    private AesEncryptor aesEncryptor;
    private CardService cardService;

    @BeforeEach
    void setUp() {
        aesEncryptor = new AesEncryptor("testSecretKey1234");
        cardService = new CardService(cardRepository, userRepository, aesEncryptor);
    }

    @Test
    void transferBetweenOwnCards_success() {
        User u = User.builder().id(1L).username("user").build();
        Card from = Card.builder()
                .id(10L)
                .user(u)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .expiry("12/30") // Добавьте срок действия
                .build();
        Card to = Card.builder()
                .id(20L)
                .user(u)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .expiry("12/30") // Добавьте срок действия
                .build();

        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(to));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        cardService.transferBetweenOwnCards(u, 10L, 20L, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("80.00"), to.getBalance());
        verify(cardRepository, times(2)).save(any());
    }

    @Test
    void transfer_insufficientFunds() {
        User u = User.builder().id(1L).username("user").build();
        Card from = Card.builder()
                .id(10L)
                .user(u)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("10.00"))
                .expiry("12/30")
                .build();
        Card to = Card.builder()
                .id(20L)
                .user(u)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("5.00"))
                .expiry("12/30")
                .build();
        when(cardRepository.findById(10L)).thenReturn(Optional.of(from));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(to));
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                cardService.transferBetweenOwnCards(u, 10L, 20L, new BigDecimal("30.00")));
        assertTrue(ex.getMessage().contains("Недостаточно средств"));
    }
}
