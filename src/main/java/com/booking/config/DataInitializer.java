package com.booking.config;

import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin accounts
        seedUserIfNotExists("admin@booking.com", "Admin@123", "System Administrator", Role.ROLE_ADMIN);
        seedUserIfNotExists("admin@example.com", "admin123", "Admin Example", Role.ROLE_ADMIN);
        seedUserIfNotExists("admin", "admin123", "Admin User", Role.ROLE_ADMIN);

        // Seed Standard User accounts
        User standardUser = seedUserIfNotExists("user@booking.com", "User@123", "Jane Doe", Role.ROLE_USER);
        seedUserIfNotExists("user@example.com", "user123", "User Example", Role.ROLE_USER);
        seedUserIfNotExists("user", "user123", "Standard User", Role.ROLE_USER);

        // Seed Sample Resources if empty
        if (resourceRepository.count() == 0) {
            Resource r1 = resourceRepository.save(new Resource(
                    "Executive Conference Room A",
                    "Spacious conference room equipped with 4K projector and AV system",
                    "ROOM",
                    "Building 1, Floor 3",
                    20,
                    true
            ));

            Resource r2 = resourceRepository.save(new Resource(
                    "Company Electric Sedan",
                    "Tesla Model 3 available for corporate travel",
                    "VEHICLE",
                    "Garage Slot B-12",
                    5,
                    true
            ));

            Resource r3 = resourceRepository.save(new Resource(
                    "4K Cinema Camera Kit",
                    "Sony FX6 Camera setup with tripod and wireless microphone kit",
                    "EQUIPMENT",
                    "Media Locker #4",
                    1,
                    true
            ));

            // Seed sample reservation for testing filter/pagination
            if (reservationRepository.count() == 0 && standardUser != null) {
                Reservation res1 = new Reservation(
                        r1,
                        standardUser,
                        LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                        ReservationStatus.CONFIRMED,
                        new BigDecimal("150.00")
                );

                Reservation res2 = new Reservation(
                        r2,
                        standardUser,
                        LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(2).withHour(18).withMinute(0),
                        ReservationStatus.PENDING,
                        new BigDecimal("250.50")
                );

                reservationRepository.save(res1);
                reservationRepository.save(res2);
            }
        }
    }

    private User seedUserIfNotExists(String email, String rawPassword, String fullName, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(
                    email,
                    passwordEncoder.encode(rawPassword),
                    fullName,
                    role
            );
            return userRepository.save(user);
        });
    }
}
