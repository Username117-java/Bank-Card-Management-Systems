package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails principal) {
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Текущий пользователь не найден"));
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> listMyCards(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = getCurrentUser(principal);
        Page<CardDto> cards = cardService.listUserCards(user, search, status, page, size);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {

        User user = getCurrentUser(principal);
        CardDto dto = cardService.getUserCardBalance(id, user);
        return ResponseEntity.ok(Map.of(
                "card", dto.getMaskedNumber(),
                "balance", dto.getBalance()
        ));
    }

    @PostMapping
    public ResponseEntity<CardDto> createCard(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateCardRequest req) {

        User user = getCurrentUser(principal);
        var card = cardService.createCardForUser(
                user,
                req.getCardNumber(),
                req.getOwnerName(),
                req.getExpiry(),
                req.getInitialBalance()
        );
        return ResponseEntity.ok(cardService.getDto(card.getId()).orElseThrow());
    }

    @PostMapping("/{id}/request-block")
    public ResponseEntity<?> requestBlock(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {

        User user = getCurrentUser(principal);
        if (!cardService.doesCardBelongToUser(id, user)) {
            return ResponseEntity.status(403).body("Карта не принадлежит пользователю");
        }
        cardService.blockCard(id);
        return ResponseEntity.ok("Запрос на блокировку карты отправлен");
    }
}