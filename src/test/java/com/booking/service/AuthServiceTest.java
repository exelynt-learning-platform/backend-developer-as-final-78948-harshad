package com.booking.service;

import com.booking.dto.AuthResponse;
import com.booking.dto.LoginRequest;
import com.booking.dto.RegisterRequest;
import com.booking.exception.BadRequestException;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.UserRepository;
import com.booking.security.JwtUtils;
import com.booking.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@booking.com", "encodedPassword", "Test User", Role.ROLE_USER);
        user.setId(1L);
    }

    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("test@booking.com", "password123");
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("dummy-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("dummy-jwt-token", response.getToken());
        assertEquals("test@booking.com", response.getEmail());
        assertEquals("ROLE_USER", response.getRole());
    }

    @Test
    void testLogin_BadCredentials() {
        LoginRequest loginRequest = new LoginRequest("test@booking.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest("new@booking.com", "Password123", "New User", Role.ROLE_USER);

        when(userRepository.existsByEmail("new@booking.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        User newUser = new User("new@booking.com", "encodedSecret", "New User", Role.ROLE_USER);
        newUser.setId(2L);
        UserDetailsImpl userDetails = UserDetailsImpl.build(newUser);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("new-user-jwt");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("new-user-jwt", response.getToken());
        assertEquals("new@booking.com", response.getEmail());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("test@booking.com", "Password123", "Duplicate User", Role.ROLE_USER);
        when(userRepository.existsByEmail("test@booking.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
    }
}
