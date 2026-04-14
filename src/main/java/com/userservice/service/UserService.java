package com.userservice.service;

import com.userservice.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for user control
 * Provides creation, update, search for users
 */
public interface UserService {

    /**
     * Creates new user
     *
     * @param userDto user data
     * @return saved user data
     */
    UserDto createUser(UserDto userDto);

    /**
     * Provides user data by id
     *
     * @param id user id
     * @return users data
     */
    UserDto getUserById(Long id);

    /**
     * Return users page with filtration possibilities
     *
     * @param name     name for search
     * @param surname  surname for search
     * @param pageable pagination and sort params
     * @return page with results
     */
    Page<UserDto> getAllUsers(String name, String surname, Pageable pageable);

    /**
     * Updates user data
     *
     * @param id      users id
     * @param userDto new user data
     * @return updated user data
     */
    UserDto updateUser(Long id, UserDto userDto);

    /**
     * Changes activated status
     *
     * @param id     user id
     * @param active new active status
     */
    void toggleStatus(Long id, boolean active);

    /**
     *
     *
     * @param email user email
     * @return user data
     */
    UserDto getUserByEmail(String email);

}