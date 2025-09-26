package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardExpiryService {

    private final CardRepository cardRepository;

    // Запускать каждый день в 0:00 AM
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredCards() {
        List<Card> activeCards = cardRepository.findByStatus(CardStatus.ACTIVE);
        int updatedCount = 0;

        for (Card card : activeCards) {
            try {
                YearMonth expiry = YearMonth.parse(card.getExpiry());
                if (expiry.isBefore(YearMonth.now()) && card.getStatus() != CardStatus.EXPIRED) {
                    card.setStatus(CardStatus.EXPIRED);
                    cardRepository.save(card);
                    updatedCount++;
                    log.info("Карта {} помечена как просроченная", card.getMaskedNumber());
                }
            } catch (Exception e) {
                log.warn("Ошибка при проверке срока действия карты {}: {}", card.getId(), e.getMessage());
            }
        }

        if (updatedCount > 0) {
            log.info("Обновлено статусов карт: {}", updatedCount);
        }
    }
}