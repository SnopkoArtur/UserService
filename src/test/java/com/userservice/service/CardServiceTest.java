package com.userservice.service;

import com.userservice.dao.CardRepository;
import com.userservice.dao.UserRepository;
import com.userservice.dto.CardDto;
import com.userservice.entity.PaymentCard;
import com.userservice.entity.User;
import com.userservice.exception.CardCountException;
import com.userservice.mapper.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User user;
    private PaymentCard card;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        card = new PaymentCard();
        card.setId(100L);
        card.setUser(user);
        card.setActive(true);

        cardDto = new CardDto();
        cardDto.setId(100L);
        cardDto.setActive(true);
    }

    @Test
    void addCardToUser_Success() {
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserIdAndActiveTrue(1L)).thenReturn(2L);
        when(cardMapper.toEntity(any(CardDto.class))).thenReturn(card);
        when(cardRepository.save(any())).thenReturn(card);
        when(cardMapper.toDto(any())).thenReturn(cardDto);

        CardDto result = cardService.addCardToUser(1L, cardDto);

        assertNotNull(result);
        verify(cardRepository).save(any());
    }

    @Test
    void addCardToUser_ThrowsException() {
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserIdAndActiveTrue(1L)).thenReturn(5L);

        assertThrows(CardCountException.class, () -> cardService.addCardToUser(1L, cardDto));
    }

    @Test
    void getCardById_Success() {
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(100L);

        assertEquals(100L, result.getId());
    }

    @Test
    void getCardsByUserId_Success() {
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(card));
        when(cardMapper.toDto(any())).thenReturn(cardDto);

        List<CardDto> result = cardService.getCardsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void toggleCardStatus_Success() {
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));

        cardService.toggleCardStatus(100L, false);

        verify(cardRepository).updateStatus(100L, false);
    }

    @Test
    void toggleCardStatus_ThrowsException() {
        card.setActive(false);
        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));

        when(cardRepository.countByUserIdAndActiveTrue(1L)).thenReturn(5L);

        assertThrows(CardCountException.class, () -> cardService.toggleCardStatus(100L, true));
    }

    @Test
    void updateCard_Success() {
        CardDto cardDtoU = new CardDto();
        cardDtoU.setId(100L);
        cardDtoU.setNumber("1111222233334444");

        when(cardRepository.findById(100L)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(any())).thenReturn(cardDtoU);

        CardDto result = cardService.updateCard(100L, cardDtoU);

        assertEquals("1111222233334444", result.getNumber());
        assertNotNull(result);
        verify(cardRepository).findById(100L);

    }
}