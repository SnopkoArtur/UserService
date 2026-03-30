package com.userservice.controller;

import com.userservice.dto.CardDto;
import com.userservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * REST-controller for card handling
 * Provides creation, update, status change, search for cards
 */
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
     * Adds card to a user
     *
     * @param userId  users id
     * @param cardDto card data
     * @return created new card
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardDto> addCardToUser(
            @PathVariable Long userId,
            @Valid @RequestBody CardDto cardDto) {
        return new ResponseEntity<>(cardService.addCardToUser(userId, cardDto), HttpStatus.CREATED);
    }

    /**
     * Provides info about card by id
     *
     * @param id card id
     * @return card data
     */
    @GetMapping("/cards/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    /**
     * Provides all cards for certain user
     *
     * @param userId owner id
     * @return list of cards
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    @GetMapping("/user/{userId}/cards")
    public ResponseEntity<List<CardDto>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    /**
     * Update data for a card
     *
     * @param id      card id
     * @param cardDto new card data
     * @return updated card
     */
    @PutMapping("/cards/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @Valid @RequestBody CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(id, cardDto));
    }

    /**
     * Changes active status for a card
     *
     * @param id     card id
     * @param active new active status
     * @return 204
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/cards/{id}/status")
    public ResponseEntity<Void> toggleCardStatus(@PathVariable Long id, @RequestParam boolean active) {
        cardService.toggleCardStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}