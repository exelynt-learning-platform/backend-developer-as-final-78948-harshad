package com.booking.security;

import com.booking.model.Role;
import com.booking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L);
    }

    @Test
    void testGenerateAndValidateJwtToken() {
        User user = new User("jwtuser@booking.com", "pass", "JWT User", Role.ROLE_USER);
        user.setId(10L);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(auth);
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("jwtuser@booking.com", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void testValidateInvalidJwtToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid-token-string"));
    }
}
