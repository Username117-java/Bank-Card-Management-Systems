package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final CardService cardService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Текущий пользователь не найден"));
    }

    @PostMapping
    public ResponseEntity<?> transfer(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody TransferRequest request) {

        User user = getCurrentUser(principal);
        cardService.transferBetweenOwnCards(user, request.getFromCardId(), request.getToCardId(), request.getAmount());
        return ResponseEntity.ok(Map.of("status", "Перевод выполнен успешно"));
    }
}