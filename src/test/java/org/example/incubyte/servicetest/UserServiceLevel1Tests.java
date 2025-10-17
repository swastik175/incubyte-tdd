package org.example.incubyte.servicetest;

import org.example.incubyte.dto.UserDTO;
import org.example.incubyte.entity.User;
import org.example.incubyte.exception.DuplicateEmailException;
import org.example.incubyte.exception.UserNotFoundException;
import org.example.incubyte.repository.UserRepository;
import org.example.incubyte.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Level 1: Simple Unit Tests")
class UserServiceLevel1Tests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {

        when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
        User savedUser = User.builder()
                .id(1L)
                .name(testUserDTO.getName())
                .email(testUserDTO.getEmail())
                .phone(testUserDTO.getPhone())
                .active(true)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.createUser(testUserDTO);

        assertNotNull(result);
        assertEquals(testUserDTO.getName(), result.getName());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreateUserWithDuplicateEmail() {

        when(userRepository.findByEmail(testUserDTO.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(testUserDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void testGetUserByIdSuccess() {

        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .active(true)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user.getName(), result.getName());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserByIdNotFound() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }
}