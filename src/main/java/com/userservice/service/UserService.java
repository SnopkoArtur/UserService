package com.userservice.service;

import com.userservice.dao.UserRepository;
import com.userservice.dto.UserDto;
import com.userservice.entity.User;
import com.userservice.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import com.userservice.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.userservice.specification.UserSpecifications;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getCards() != null && userDto.getCards().size() > 5) {
            throw new RuntimeException("User cannot have more than 5 cards");
        }
        User user = userMapper.toEntity(userDto);

        if (user.getCards() != null) {
            user.getCards().forEach(card -> card.setUser(user));
        }
        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + id));
        return userMapper.toDto(user);
    }

    public Page<UserDto> getAllUsers(String name, String surname, Pageable pageable) {
        return userRepository.findAll(UserSpecifications.filterUsers(name, surname), pageable)
                .map(userMapper::toDto);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + id));

        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        return userMapper.toDto(user);
    }

    @Transactional
    public void toggleStatus(Long id, boolean active) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("User not found, id: " + id);
        userRepository.updateStatus(id, active);
    }
}