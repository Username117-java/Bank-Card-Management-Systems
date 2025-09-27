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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        YearMonth now = YearMonth.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
        List<Card> cards = cardRepository.findAll();
        for (Card card : cards) {
            try {
                YearMonth expiry = YearMonth.parse(card.getExpiry(), fmt);
                if (expiry.isBefore(now)) {
                    card.setStatus(CardStatus.EXPIRED);
                    cardRepository.save(card);
                }
            } catch (DateTimeParseException e) {
                // Если формат неправильный — считаем карту некорректной
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        }
    }
}