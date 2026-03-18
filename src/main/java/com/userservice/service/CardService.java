package com.userservice.service;

import com.userservice.dao.CardRepository;
import com.userservice.dao.UserRepository;
import com.userservice.dto.CardDto;
import com.userservice.entity.PaymentCard;
import com.userservice.entity.User;
import com.userservice.exception.CardCountException;
import com.userservice.exception.CardNotFoundException;
import com.userservice.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import com.userservice.mapper.CardMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardDto addCardToUser(Long userId, CardDto cardDto) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + id));

        long activeCount = cardRepository.countByUserIdAndActiveTrue(userId);

        if (cardDto.isActive() && activeCount >= 5) {
            throw new CardCountException("Card limit (5) exceeded for this user");
        }

        PaymentCard card = cardMapper.toEntity(cardDto);
        card.setUser(user);
        return cardMapper.toDto(cardRepository.save(card));
    }

    public CardDto getCardById(Long id) {
        return cardRepository.findById(id)
                .map(cardMapper::toDto)
                .orElseThrow(() -> new CardNotFoundException("Card not found, id: " + id));
    }

    public List<CardDto> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Transactional
    public void toggleCardStatus(Long id, boolean active) {
        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + id));

        Long userId = card.getUser().getId();

        if (active && !card.isActive()) {
            userRepository.findByIdWithLock(userId);

            long activeCount = cardRepository.countByUserIdAndActiveTrue(userId);
            if (activeCount >= 5) {
                throw new CardCountException("User already have 5 active cards");
            }
        }

        cardRepository.updateStatus(id, active);
    }

    @Transactional
    public CardDto updateCard(Long id, CardDto dto) {
        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found, id: " + id));
        card.setNumber(dto.getNumber());
        card.setHolder(dto.getHolder());
        card.setExpirationDate(dto.getExpirationDate());
        return cardMapper.toDto(card);
    }
}