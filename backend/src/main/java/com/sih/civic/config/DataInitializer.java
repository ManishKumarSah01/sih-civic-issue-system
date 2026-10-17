package com.sih.civic.config;

import com.sih.civic.model.User;
import com.sih.civic.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedData(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmailIgnoreCase("admin@civic.demo")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@civic.demo");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ADMIN");
                users.save(admin);
                log.info("✅ Admin seeded: admin@civic.demo / admin123");
            }
            if (!users.existsByEmailIgnoreCase("manish@civic.demo")) {
                User citizen = new User();
                citizen.setName("Manish Kumar");
                citizen.setEmail("manish@civic.demo");
                citizen.setPassword(encoder.encode("manish123"));
                citizen.setRole("CITIZEN");
                users.save(citizen);
                log.info("✅ Citizen seeded: manish@civic.demo / manish123");
            }
        };
    }
}
