package com.example.campus_hub;

import com.example.campus_hub.entity.User;
import com.example.campus_hub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (!repo.existsByEmail("admin@campushub.edu")) {

                User admin = new User();
                admin.setEmail("admin@campushub.edu");
                admin.setFullName("System Admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                admin.setStatus(User.Status.ACTIVE);

                repo.save(admin);

                System.out.println("✅ Admin created");
            }
        };
    }
}