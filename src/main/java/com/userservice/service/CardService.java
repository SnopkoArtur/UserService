package com.userservice.service;

import com.userservice.dto.CardDto;

import java.util.List;

/**
 * Service for user cards
 * Handles creating, update, limit check
 */
public interface CardService {

    /**
     * Add a card to a user
     * Ensures that everything is within a limit
     *
     * @param userId  holder id
     * @param cardDto data for a new card
     * @return created card data
     * @throws com.userservice.exception.CardCountException if limit is reached
     */
    CardDto addCardToUser(Long userId, CardDto cardDto);

    /**
     * Gets info about a card from id
     *
     * @param id cards id
     * @return card data
     */
    CardDto getCardById(Long id);

    /**
     * Provides all cards info for a certain user
     *
     * @param userId holder id
     * @return list of cards
     */
    List<CardDto> getCardsByUserId(Long userId);

    /**
     * Changes the status
     * Ensures that everything is within a limit
     *
     * @param id cards id
     * @param active new status
     */
    void toggleCardStatus(Long id, boolean active);

    /**
     * Updates card info
     *
     * @param id  cards id
     * @param dto new data for a card
     * @return updated card data
     */
    CardDto updateCard(Long id, CardDto dto);
}