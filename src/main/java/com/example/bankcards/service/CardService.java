package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.AesEncryptor;
import com.example.bankcards.util.CardMaskingUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AesEncryptor aesEncryptor;

    public Card createCardForUser(User user, String plainNumber, String ownerName, String expiry, BigDecimal initialBalance) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }

        String encrypted = aesEncryptor.encrypt(plainNumber);

        // Проверка уникальности номера карты
        if (cardRepository.existsByEncryptedNumber(encrypted)) {
            throw new IllegalArgumentException("Карта с таким номером уже существует");
        }
        String masked = CardMaskingUtil.mask(plainNumber);
        Card c = Card.builder()
                .encryptedNumber(encrypted)
                .maskedNumber(masked)
                .ownerName(ownerName)
                .expiry(expiry)
                .status(CardStatus.ACTIVE)
                .balance(initialBalance)
                .user(user)
                .build();
        return cardRepository.save(c);
    }

    // Админ: создать карту для любого пользователя (по userId)
    public Card createCardForAnyUser(Long userId, CreateCardRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));
        return createCardForUser(user, req.getCardNumber(), req.getOwnerName(), req.getExpiry(), req.getInitialBalance());
    }

    public Optional<CardDto> getDto(Long id) {
        return cardRepository.findById(id).map(this::toDto);
    }

    private CardDto toDto(Card c) {
        return CardDto.builder()
                .id(c.getId())
                .maskedNumber(c.getMaskedNumber())
                .ownerName(c.getOwnerName())
                .expiry(c.getExpiry())
                .status(c.getStatus().name())
                .balance(c.getBalance())
                .build();
    }

    public Page<CardDto> listUserCards(User user, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Card> p;
        if (search != null && !search.isBlank()) {
            p = cardRepository.findByUserAndOwnerNameContainingIgnoreCase(user, search, pageable);
        } else {
            p = cardRepository.findByUser(user, pageable);
        }
        return p.map(this::toDto);
    }

    // Новый метод: проверяет, принадлежит ли карта пользователю
    public boolean doesCardBelongToUser(Long cardId, User user) {
        return cardRepository.findByIdAndUserId(cardId, user.getId()).isPresent();
    }

    // Возвращает Optional<Card> только если карта принадлежит пользователю
    public Optional<Card> findUserCardById(Long cardId, User user) {
        return cardRepository.findByIdAndUserId(cardId, user.getId());
    }

    public Page<CardDto> findAll(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::toDto);
    }

    public void blockCard(Long cardId) {
        Card c = cardRepository.findById(cardId).orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));
        c.setStatus(CardStatus.BLOCKED);
        cardRepository.save(c);
    }

    // Admin блокировка принудительно
    public void blockCardByAdmin(Long cardId) {
        blockCard(cardId);
    }

    public void activateCard(Long cardId) {
        Card c = cardRepository.findById(cardId).orElseThrow(() -> new EntityNotFoundException("Карта не найдена: " + cardId));
        c.setStatus(CardStatus.ACTIVE);
        cardRepository.save(c);
    }

    public void deleteByAdmin(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Карта не найдена: " + cardId);
        }
        cardRepository.deleteById(cardId);
    }

    public CardDto getUserCardBalance(Long cardId, User user) {
        Card card = findUserCardById(cardId, user)
                .orElseThrow(() -> new IllegalArgumentException("Карта не найдена или не принадлежит пользователю"));
        return toDto(card);
    }

    @Transactional
    public void transferBetweenOwnCards(User user, Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }


        Card from = cardRepository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("Карта отправителя не найдена"));
        Card to = cardRepository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("Карта получателя не найдена"));
        // Проверка принадлежности карт пользователю
        if (!from.getUser().getId().equals(user.getId()) || !to.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Обе карты должны принадлежать пользователю");
        }

        // Проверка статуса карт
        if (from.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта отправителя не активна");
        }
        if (to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Карта получателя не активна");
        }

        // Проверка достаточности средств
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств на карте отправителя");
        }

        // Проверка на одинаковые карты
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Нельзя переводить на ту же карту");
        }

        // Проверка не истек ли срок действия карт
        if (isCardExpired(from)) {
            throw new IllegalArgumentException("Срок действия карты отправителя истек");
        }
        if (isCardExpired(to)) {
            throw new IllegalArgumentException("Срок действия карты получателя истек");
        }

        // Выполнение перевода
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
    }

    private boolean isCardExpired(Card card) {
        try {
            YearMonth expiry = YearMonth.parse(card.getExpiry());
            return expiry.isBefore(YearMonth.now());
        } catch (Exception e) {
            // В случае ошибки парсинга считаем карту проблемной (можно обработать иначе)
            return true;
        }
    }
}
