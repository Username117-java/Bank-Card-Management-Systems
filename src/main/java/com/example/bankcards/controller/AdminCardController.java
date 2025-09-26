package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardDto>> allCards(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        Page<CardDto> p = cardService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<CardDto> create(@RequestParam Long userId,
                                          @Valid @RequestBody CreateCardRequest req) {
        var card = cardService.createCardForAnyUser(userId, req);
        return ResponseEntity.ok(cardService.getDto(card.getId()).orElseThrow());
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<?> block(@PathVariable Long id) {
        cardService.blockCardByAdmin(id);
        return ResponseEntity.ok("Карта заблокирована администратором");
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok("Карта активирована");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        cardService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}