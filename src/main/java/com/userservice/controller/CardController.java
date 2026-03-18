package com.userservice.controller;

import com.userservice.dto.CardDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.userservice.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<CardDto> addCardToUser(
            @PathVariable Long userId,
            @Valid @RequestBody CardDto cardDto) {
        return new ResponseEntity<>(cardService.addCardToUser(userId, cardDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardDto>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @Valid @RequestBody CardDto cardDto) {
        return ResponseEntity.ok(cardService.updateCard(id, cardDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleCardStatus(@PathVariable Long id, @RequestParam boolean active) {
        cardService.toggleCardStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}