package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);

    Page<Card> findByUserAndOwnerNameContainingIgnoreCase(User user, String ownerName, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long cardId, Long userId);

    List<Card> findByStatus(CardStatus status);

    boolean existsByEncryptedNumber(String encryptedNumber);
}
