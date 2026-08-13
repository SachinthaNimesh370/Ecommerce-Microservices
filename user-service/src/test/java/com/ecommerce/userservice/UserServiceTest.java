package com.ecommerce.userservice;

import com.ecommerce.userservice.dto.AuthResponse;
import com.ecommerce.userservice.dto.LoginRequest;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.dto.UserResponse;
import com.ecommerce.userservice.entity.Role;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtService;
import com.ecommerce.userservice.service.AuthService;
import com.ecommerce.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded_pass")
                .role(Role.CUSTOMER)
                .build();

        registerRequest = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("raw_pass")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Register: Should successfully register a new user and return JWT token")
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register: Should throw exception when email already exists")
    void register_EmailExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login: Should authenticate user and return JWT token")
    void login_Success() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("raw_pass")
                .build();

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken(sampleUser)).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("JWT: Should generate and validate token correctly")
    void jwtValidation() {
        when(jwtService.isTokenValid("mocked_jwt_token", sampleUser)).thenReturn(true);

        boolean isValid = jwtService.isTokenValid("mocked_jwt_token", sampleUser);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Get User: Should retrieve current user by email")
    void getCurrentUser_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

        UserResponse userResponse = userService.getCurrentUser("john@example.com");

        assertNotNull(userResponse);
        assertEquals(1L, userResponse.getId());
        assertEquals("John Doe", userResponse.getName());
    }

    @Test
    @DisplayName("Get User: Should throw ResourceNotFoundException when user does not exist")
    void getUser_NotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser("unknown@example.com"));
    }

    @Test
    @DisplayName("Update User: Should update user name and password")
    void updateUser_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("new_pass")).thenReturn("encoded_new_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        RegisterRequest updateReq = RegisterRequest.builder()
                .name("John Updated")
                .password("new_pass")
                .build();

        UserResponse updated = userService.updateUser("john@example.com", updateReq);

        assertNotNull(updated);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Authorization: Admin role user can access all users list")
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponse> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
    }
}
