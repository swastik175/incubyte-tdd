package org.example.incubyte.servicetest;

import org.example.incubyte.dto.UserDTO;
import org.example.incubyte.entity.User;
import org.example.incubyte.exception.DuplicateEmailException;
import org.example.incubyte.exception.UserNotFoundException;
import org.example.incubyte.repository.UserRepository;
import org.example.incubyte.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Complete TDD Test Suite")
class UserServiceLevel2Tests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDTO testUserDTO;
    private User testUser;
    private long currentTime;

    @BeforeEach
    void setUp() {
        currentTime = System.currentTimeMillis();
        
        testUserDTO = UserDTO.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .active(true)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    // ===== CREATE USER TESTS =====
    @Nested
    @DisplayName("CREATE - User Creation Tests")
    class CreateUserTests {

        @Nested
        @DisplayName("Level 1: Basic Functionality")
        class Level1Basic {

            @Test
            @DisplayName("Should create user with valid data")
            void shouldCreateUserWithValidData() {
                when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.createUser(testUserDTO);

                assertNotNull(result);
                assertEquals("John Doe", result.getName());
                assertEquals("john@example.com", result.getEmail());
                assertEquals("1234567890", result.getPhone());
                verify(userRepository, times(1)).save(any(User.class));
            }

            @Test
            @DisplayName("Should throw DuplicateEmailException when email exists")
            void shouldThrowDuplicateEmailException() {
                when(userRepository.findByEmail(testUserDTO.getEmail()))
                        .thenReturn(Optional.of(testUser));

                assertThrows(DuplicateEmailException.class,
                        () -> userService.createUser(testUserDTO));
                verify(userRepository, never()).save(any(User.class));
            }
        }

        @Nested
        @DisplayName("Level 2: Enhanced Validation")
        class Level2Enhanced {

            @Test
            @DisplayName("Should set active flag to true by default")
            void shouldSetActiveTrue() {
                when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.createUser(testUserDTO);

                assertTrue(result.getActive());
            }

            @Test
            @DisplayName("Should capture and verify user data in save call")
            void shouldCaptureUserDataInSave() {
                when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                when(userRepository.save(userCaptor.capture())).thenReturn(testUser);

                userService.createUser(testUserDTO);

                User capturedUser = userCaptor.getValue();
                assertEquals("John Doe", capturedUser.getName());
                assertEquals("john@example.com", capturedUser.getEmail());
                assertTrue(capturedUser.getActive());
            }

            @Test
            @DisplayName("Should verify email uniqueness check is performed")
            void shouldVerifyEmailUniquenessCheck() {
                when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                userService.createUser(testUserDTO);

                verify(userRepository, times(1)).findByEmail("john@example.com");
            }

            @Test
            @DisplayName("Should return DTO with all user fields")
            void shouldReturnCompleteDTO() {
                when(userRepository.findByEmail(testUserDTO.getEmail())).thenReturn(Optional.empty());
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.createUser(testUserDTO);

                assertEquals(testUser.getId(), result.getId());
                assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
                assertEquals(testUser.getUpdatedAt(), result.getUpdatedAt());
            }
        }

        @Nested
        @DisplayName("Level 3: Edge Cases & Error Handling")
        class Level3EdgeCases {

            @Test
            @DisplayName("Should handle duplicate email with proper error message")
            void shouldProvideMeaningfulErrorMessage() {
                when(userRepository.findByEmail(testUserDTO.getEmail()))
                        .thenReturn(Optional.of(testUser));

                DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                        () -> userService.createUser(testUserDTO));
                assertTrue(exception.getMessage().contains("john@example.com"));
            }

            @Test
            @DisplayName("Should not save user if email already exists")
            void shouldNotSaveOnDuplicateEmail() {
                when(userRepository.findByEmail(testUserDTO.getEmail()))
                        .thenReturn(Optional.of(testUser));

                assertThrows(DuplicateEmailException.class,
                        () -> userService.createUser(testUserDTO));
                verify(userRepository, never()).save(any(User.class));
            }

            @Test
            @DisplayName("Should handle special characters in email")
            void shouldHandleSpecialCharactersInEmail() {
                UserDTO specialDTO = UserDTO.builder()
                        .name("Test User")
                        .email("test+tag@example.co.uk")
                        .phone("1234567890")
                        .build();

                when(userRepository.findByEmail(specialDTO.getEmail())).thenReturn(Optional.empty());
                User savedUser = User.builder().id(2L).build();
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                assertDoesNotThrow(() -> userService.createUser(specialDTO));
            }
        }
    }

    // ===== READ USER TESTS =====
    @Nested
    @DisplayName("READ - User Retrieval Tests")
    class ReadUserTests {

        @Nested
        @DisplayName("Level 1: Basic Functionality")
        class Level1Basic {

            @Test
            @DisplayName("Should retrieve user by id")
            void shouldGetUserById() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

                UserDTO result = userService.getUserById(1L);

                assertNotNull(result);
                assertEquals(1L, result.getId());
                assertEquals("John Doe", result.getName());
            }

            @Test
            @DisplayName("Should throw UserNotFoundException when user not found")
            void shouldThrowUserNotFoundWhenIdDoesNotExist() {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class,
                        () -> userService.getUserById(999L));
            }

            @Test
            @DisplayName("Should retrieve all users")
            void shouldGetAllUsers() {
                User user2 = User.builder().id(2L).name("Jane Doe").email("jane@example.com")
                        .phone("9876543210").active(true).createdAt(currentTime).updatedAt(currentTime).build();
                when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

                List<UserDTO> result = userService.getAllUsers();

                assertEquals(2, result.size());
                assertEquals("John Doe", result.get(0).getName());
                assertEquals("Jane Doe", result.get(1).getName());
            }
        }

        @Nested
        @DisplayName("Level 2: Enhanced Retrieval")
        class Level2Enhanced {

            @Test
            @DisplayName("Should return empty list when no users exist")
            void shouldReturnEmptyListWhenNoUsers() {
                when(userRepository.findAll()).thenReturn(Arrays.asList());

                List<UserDTO> result = userService.getAllUsers();

                assertTrue(result.isEmpty());
            }

            @Test
            @DisplayName("Should retrieve only active users")
            void shouldRetrieveActiveUsers() {
                User inactiveUser = User.builder().id(2L).name("Inactive User")
                        .email("inactive@example.com").phone("1111111111")
                        .active(false).createdAt(currentTime).updatedAt(currentTime).build();
                when(userRepository.findByActive(true)).thenReturn(Arrays.asList(testUser));

                List<UserDTO> result = userService.getActiveUsers();

                assertEquals(1, result.size());
                assertTrue(result.get(0).getActive());
            }

            @Test
            @DisplayName("Should preserve all user fields in retrieved DTO")
            void shouldPreserveAllUserFields() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

                UserDTO result = userService.getUserById(1L);

                assertEquals(testUser.getId(), result.getId());
                assertEquals(testUser.getName(), result.getName());
                assertEquals(testUser.getEmail(), result.getEmail());
                assertEquals(testUser.getPhone(), result.getPhone());
                assertEquals(testUser.getActive(), result.getActive());
                assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
                assertEquals(testUser.getUpdatedAt(), result.getUpdatedAt());
            }
        }

        @Nested
        @DisplayName("Level 3: Error Handling & Edge Cases")
        class Level3EdgeCases {

            @Test
            @DisplayName("Should provide meaningful error when user not found")
            void shouldProvideMeaningfulErrorMessage() {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                        () -> userService.getUserById(999L));
                assertTrue(exception.getMessage().contains("999"));
            }

            @Test
            @DisplayName("Should handle zero id gracefully")
            void shouldHandleZeroId() {
                when(userRepository.findById(0L)).thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class,
                        () -> userService.getUserById(0L));
            }

            @Test
            @DisplayName("Should handle negative id gracefully")
            void shouldHandleNegativeId() {
                when(userRepository.findById(-1L)).thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class,
                        () -> userService.getUserById(-1L));
            }
        }
    }

    // ===== UPDATE USER TESTS =====
    @Nested
    @DisplayName("UPDATE - User Modification Tests")
    class UpdateUserTests {

        @Nested
        @DisplayName("Level 1: Basic Functionality")
        class Level1Basic {

            @Test
            @DisplayName("Should update user name successfully")
            void shouldUpdateUserName() {
                UserDTO updateDTO = UserDTO.builder().name("Jane Doe").build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                testUser.setName("Jane Doe");
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("Jane Doe", result.getName());
            }

            @Test
            @DisplayName("Should throw exception when updating non-existent user")
            void shouldThrowExceptionWhenUserNotFound() {
                UserDTO updateDTO = UserDTO.builder().name("Jane Doe").build();
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class,
                        () -> userService.updateUser(999L, updateDTO));
            }
        }

        @Nested
        @DisplayName("Level 2: Enhanced Update Operations")
        class Level2Enhanced {

            @Test
            @DisplayName("Should update phone number successfully")
            void shouldUpdatePhoneNumber() {
                UserDTO updateDTO = UserDTO.builder().phone("9999999999").build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                testUser.setPhone("9999999999");
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("9999999999", result.getPhone());
            }

            @Test
            @DisplayName("Should update active status successfully")
            void shouldUpdateActiveStatus() {
                UserDTO updateDTO = UserDTO.builder().active(false).build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                testUser.setActive(false);
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertFalse(result.getActive());
            }

            @Test
            @DisplayName("Should update email with uniqueness check")
            void shouldUpdateEmailWithUniquenessCheck() {
                UserDTO updateDTO = UserDTO.builder().email("newemail@example.com").build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
                testUser.setEmail("newemail@example.com");
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("newemail@example.com", result.getEmail());
                verify(userRepository).findByEmail("newemail@example.com");
            }

            @Test
            @DisplayName("Should allow same email update without throwing exception")
            void shouldAllowSameEmailUpdate() {
                UserDTO updateDTO = UserDTO.builder().email("john@example.com").build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("john@example.com", result.getEmail());
                verify(userRepository, never()).findByEmail("john@example.com");
            }
        }

        @Nested
        @DisplayName("Level 3: Complex Update Scenarios")
        class Level3ComplexScenarios {

            @Test
            @DisplayName("Should throw exception when updating to duplicate email")
            void shouldThrowExceptionOnDuplicateEmailUpdate() {
                User otherUser = User.builder().id(2L).email("existing@example.com").build();
                UserDTO updateDTO = UserDTO.builder().email("existing@example.com").build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(otherUser));

                assertThrows(DuplicateEmailException.class,
                        () -> userService.updateUser(1L, updateDTO));
            }

            @Test
            @DisplayName("Should update multiple fields simultaneously")
            void shouldUpdateMultipleFields() {
                UserDTO updateDTO = UserDTO.builder()
                        .name("Updated Name")
                        .phone("5555555555")
                        .active(false)
                        .build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                testUser.setName("Updated Name");
                testUser.setPhone("5555555555");
                testUser.setActive(false);
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("Updated Name", result.getName());
                assertEquals("5555555555", result.getPhone());
                assertFalse(result.getActive());
            }

            @Test
            @DisplayName("Should preserve fields not included in update")
            void shouldPreserveUnchangedFields() {
                UserDTO updateDTO = UserDTO.builder().name("New Name").build();
                User existingUser = User.builder()
                        .id(1L).name("Old Name").email("john@example.com")
                        .phone("1234567890").active(true)
                        .createdAt(currentTime).updatedAt(currentTime).build();
                when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
                existingUser.setName("New Name");
                when(userRepository.save(any(User.class))).thenReturn(existingUser);

                UserDTO result = userService.updateUser(1L, updateDTO);

                assertEquals("john@example.com", result.getEmail());
                assertEquals("1234567890", result.getPhone());
                assertTrue(result.getActive());
            }
        }
    }

    // ===== DELETE USER TESTS =====
    @Nested
    @DisplayName("DELETE - User Removal Tests")
    class DeleteUserTests {

        @Nested
        @DisplayName("Level 1: Basic Functionality")
        class Level1Basic {

            @Test
            @DisplayName("Should delete user successfully")
            void shouldDeleteUserSuccessfully() {
                when(userRepository.existsById(1L)).thenReturn(true);
                doNothing().when(userRepository).deleteById(1L);

                assertDoesNotThrow(() -> userService.deleteUser(1L));
                verify(userRepository, times(1)).deleteById(1L);
            }

            @Test
            @DisplayName("Should throw exception when deleting non-existent user")
            void shouldThrowExceptionWhenUserNotFound() {
                when(userRepository.existsById(999L)).thenReturn(false);

                assertThrows(UserNotFoundException.class,
                        () -> userService.deleteUser(999L));
            }
        }

        @Nested
        @DisplayName("Level 2: Enhanced Delete Operations")
        class Level2Enhanced {

            @Test
            @DisplayName("Should verify existence before deletion")
            void shouldVerifyExistenceBeforeDeletion() {
                when(userRepository.existsById(1L)).thenReturn(true);
                doNothing().when(userRepository).deleteById(1L);

                userService.deleteUser(1L);

                verify(userRepository, times(1)).existsById(1L);
            }

            @Test
            @DisplayName("Should provide meaningful error for non-existent user")
            void shouldProvideMeaningfulErrorMessage() {
                when(userRepository.existsById(999L)).thenReturn(false);

                UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                        () -> userService.deleteUser(999L));
                assertTrue(exception.getMessage().contains("999"));
            }

            @Test
            @DisplayName("Should handle multiple deletion attempts gracefully")
            void shouldHandleMultipleDeletionAttempts() {
                when(userRepository.existsById(1L)).thenReturn(true).thenReturn(false);
                doNothing().when(userRepository).deleteById(1L);

                userService.deleteUser(1L);
                assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
            }
        }
    }
}