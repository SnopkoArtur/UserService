package com.userservice.service;

import com.userservice.dao.CardRepository;
import com.userservice.dao.UserRepository;
import com.userservice.dto.CardDto;
import com.userservice.entity.PaymentCard;
import com.userservice.entity.User;
import com.userservice.exception.CardCountException;
import com.userservice.exception.CardNotFoundException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.userservice.mapper.CardMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {
    private static final long ACTIVE_CARD_LIMIT = 5;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardDto addCardToUser(Long userId, CardDto cardDto) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + userId));

        long activeCount = cardRepository.countByUserIdAndActiveTrue(userId);

        if (cardDto.isActive() && activeCount >= ACTIVE_CARD_LIMIT) {
            throw new CardCountException("Card limit ("+ ACTIVE_CARD_LIMIT + ") exceeded for this user");
        }

        PaymentCard card = cardMapper.toEntity(cardDto);
        card.setUser(user);
        return cardMapper.toDto(cardRepository.save(card));
    }

    public CardDto getCardById(Long id) {
        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + id));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (!card.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't own this card");
            }
        }
        return cardMapper.toDto(card);

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
            if (activeCount >= ACTIVE_CARD_LIMIT) {
                throw new CardCountException("User already have " + ACTIVE_CARD_LIMIT + " active cards");
            }
        }

        cardRepository.updateStatus(id, active);
    }

    @Transactional
    public CardDto updateCard(Long id, CardDto dto) {
        PaymentCard card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found, id: " + id));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.hasRole("ADMIN")) {
            if (!card.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You don't own this card");
            }
        }

        card.setNumber(dto.getNumber());
        card.setHolder(dto.getHolder());
        card.setExpirationDate(dto.getExpirationDate());
        return cardMapper.toDto(card);
    }
}