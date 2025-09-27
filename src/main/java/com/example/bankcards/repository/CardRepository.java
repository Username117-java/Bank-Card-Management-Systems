package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);

    Page<Card> findByUserAndOwnerNameContainingIgnoreCase(User user, String ownerName, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long cardId, Long userId);

    List<Card> findByStatus(CardStatus status);

    boolean existsByEncryptedNumber(String encryptedNumber);

    // Поиск по владельцу ИЛИ по последним 4 цифрам номера карты
    @Query("SELECT c FROM Card c WHERE c.user = :user AND " +
            "(LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.maskedNumber LIKE CONCAT('%', :search, '%'))")
    Page<Card> findByUserAndOwnerNameContainingOrMaskedNumberContaining(
            @Param("user") User user,
            @Param("search") String search,
            Pageable pageable);

    // Поиск с фильтрацией по статусу
    Page<Card> findByUserAndStatus(User user, CardStatus status, Pageable pageable);

    // Комбинированный поиск по всем параметрам
    @Query("SELECT c FROM Card c WHERE c.user = :user AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:search IS NULL OR LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.maskedNumber LIKE CONCAT('%', :search, '%'))")
    Page<Card> findByUserWithFilters(
            @Param("user") User user,
            @Param("status") CardStatus status,
            @Param("search") String search,
            Pageable pageable);
}
