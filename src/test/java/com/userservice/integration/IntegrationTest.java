package com.userservice.integration;

import com.userservice.dto.CardDto;
import com.userservice.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IntegrationTest extends BaseIntegrationTest {

    @Test
    void fullFlowTest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("Test");
        userDto.setSurname("User");
        userDto.setEmail("test@example.com");

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        for (int i = 0; i < 5; i++) {
            CardDto card = new CardDto();
            card.setNumber("111111111111111" + i);
            card.setHolder("TEST HOLDER");
            card.setExpirationDate("12/29");
            card.setActive(true);

            mockMvc.perform(post("/api/v1/cards/user/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isCreated());
        }

        CardDto extraCard = new CardDto();
        extraCard.setNumber("0000000000000000");
        extraCard.setActive(true);

        mockMvc.perform(post("/api/v1/cards/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extraCard)))
                .andExpect(status().isBadRequest()); // Ожидаем ошибку лимита
    }
}