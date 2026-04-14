package com.userservice.integration;

import com.userservice.dto.CardDto;
import com.userservice.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IntegrationTest extends BaseIntegrationTest {

    protected UsernamePasswordAuthenticationToken getAuth(Long userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    @Test
    void fullFlowTest() throws Exception {
        var adminAuth = getAuth(999L, "ADMIN");
        UserDto userDto = new UserDto();
        userDto.setName("Test");
        userDto.setSurname("User");
        userDto.setEmail("test@example.com");

        String response = mockMvc.perform(post("/api/v1/users")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();

        for (int i = 0; i < 5; i++) {
            CardDto card = new CardDto();
            card.setNumber("111111111111111" + i);
            card.setHolder("TEST HOLDER");
            card.setExpirationDate("12/29");
            card.setActive(true);

            mockMvc.perform(post("/api/v1/users/" + userId + "/cards")
                            .with(authentication(adminAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isCreated());
        }

        CardDto extraCard = new CardDto();
        extraCard.setNumber("0000000000000000");
        extraCard.setHolder("someholder");
        extraCard.setActive(true);

        mockMvc.perform(post("/api/v1/users/" + userId + "/cards")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extraCard)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/users/99999").with(authentication(adminAuth)))
                .andExpect(status().isNotFound());


        mockMvc.perform(get("/api/v1/cards/99999").with(authentication(adminAuth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void BadRequestTest() throws Exception {
        var adminAuth = getAuth(999L, "ADMIN");
        UserDto userDto = new UserDto();
        userDto.setName("Validation");
        userDto.setSurname("Test");
        userDto.setEmail("val@test.com");

        String userResponse = mockMvc.perform(post("/api/v1/users").with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(userResponse).get("id").asLong();

        CardDto invalidCard = new CardDto();
        invalidCard.setNumber("");
        invalidCard.setHolder("Test Holder");
        invalidCard.setExpirationDate("12/25");
        invalidCard.setActive(true);

        mockMvc.perform(post("/api/v1/users/" + userId + "/cards").with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCard)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.number").exists());
    }

    @Test
    void toggleUserStatus() throws Exception {
        var adminAuth = getAuth(999L, "ADMIN");
        UserDto userDto = new UserDto();
        userDto.setName("Status");
        userDto.setSurname("Test");
        userDto.setEmail("status@test.com");
        userDto.setActive(true);
        String response = mockMvc.perform(post("/api/v1/users").with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/v1/users/" + userId + "/status").with(authentication(adminAuth))
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/" + userId).with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void toggleCardStatus() throws Exception {
        var adminAuth = getAuth(999L, "ADMIN");
        UserDto userDto = new UserDto();
        userDto.setName("Card");
        userDto.setSurname("Tester");
        userDto.setEmail("card-test@example.com");

        String userResponse = mockMvc.perform(post("/api/v1/users").with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long userId = objectMapper.readTree(userResponse).get("id").asLong();

        CardDto cardDto = new CardDto();
        cardDto.setNumber("9999888877776666");
        cardDto.setHolder("CARD TESTER");
        cardDto.setExpirationDate("11/30");
        cardDto.setActive(true);

        String cardResponse = mockMvc.perform(post("/api/v1/users/" + userId + "/cards").with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long cardId = objectMapper.readTree(cardResponse).get("id").asLong();

        mockMvc.perform(patch("/api/v1/cards/" + cardId + "/status").with(authentication(adminAuth))
                        .param("active", "false"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cards/" + cardId).with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}