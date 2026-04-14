package com.userservice.service;

import com.userservice.dao.UserRepository;
import com.userservice.dto.CardDto;
import com.userservice.dto.UserDto;
import com.userservice.entity.User;
import com.userservice.exception.CardCountException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("TestName");
        user.setSurname("TestSurname");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("TestName");
        userDto.setSurname("TestSurname");
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void createUser_ThrowsException() {
        UserDto dto = new UserDto();

        List<CardDto> sixActiveCards = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            CardDto card = new CardDto();
            card.setActive(true);
            sixActiveCards.add(card);
        }
        dto.setCards(sixActiveCards);

        assertThrows(CardCountException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUser_Success() {
        when(userMapper.toEntity(any(UserDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals("TestName", result.getName());
        verify(userRepository).save(any());
    }

    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        Page<UserDto> result = userService.getAllUsers("Test", "TestSurname", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void toggleStatus_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.toggleStatus(1L, true);

        verify(userRepository).updateStatus(1L, true);
    }

    @Test
    void updateUser_Success() {
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(1L);
        updatedUserDto.setName("Updated");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(any())).thenReturn(updatedUserDto);

        UserDto result = userService.updateUser(1L, updatedUserDto);

        assertEquals("Updated", result.getName());
        assertNotNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void Delete_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void Delete_Fail() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->  userService.deleteUser(1L));
    }
}