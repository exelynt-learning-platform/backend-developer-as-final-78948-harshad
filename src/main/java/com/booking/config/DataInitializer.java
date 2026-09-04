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
        // Seed Seed Admin User if not present
        if (!userRepository.existsByEmail("admin@booking.com")) {
            User admin = new User(
                    "admin@booking.com",
                    passwordEncoder.encode("Admin@123"),
                    "System Administrator",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
        }

        // Seed Standard Test User if not present
        User standardUser = null;
        if (!userRepository.existsByEmail("user@booking.com")) {
            standardUser = new User(
                    "user@booking.com",
                    passwordEncoder.encode("User@123"),
                    "Jane Doe",
                    Role.ROLE_USER
            );
            standardUser = userRepository.save(standardUser);
        } else {
            standardUser = userRepository.findByEmail("user@booking.com").orElse(null);
        }

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
}
