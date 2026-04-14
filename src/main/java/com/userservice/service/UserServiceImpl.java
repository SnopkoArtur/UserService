package com.userservice.service;

import com.userservice.dao.UserRepository;
import com.userservice.dto.UserDto;
import com.userservice.entity.User;
import com.userservice.exception.CardCountException;
import com.userservice.exception.UserNotFoundException;
import com.userservice.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import com.userservice.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.userservice.specification.UserSpecifications;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private static final long ACTIVE_CARD_LIMIT = 5;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto.getCards() != null && userDto.getCards().size() > ACTIVE_CARD_LIMIT) {
            throw new CardCountException("User cannot have more than "+ ACTIVE_CARD_LIMIT +" cards");
        }
        User user = userMapper.toEntity(userDto);

        if (user.getCards() != null) {
            user.getCards().forEach(card -> card.setUser(user));
        }
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public Page<UserDto> getAllUsers(String name, String surname, Pageable pageable) {
        return userRepository.findAll(UserSpecifications.filterUsers(name, surname), pageable)
                .map(userMapper::toDto);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + id));

        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        return userMapper.toDto(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void toggleStatus(Long id, boolean active) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("User not found, id: " + id);
        userRepository.updateStatus(id, active);
    }

    @Override
    public UserDto getUserByEmail (String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found, id: " + email));
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.hasRole("ADMIN") && !user.getId().equals(currentUserId)) {
            throw new AccessDeniedException("You don't own this card");
        }
        return userMapper.toDto(user);
    }
}