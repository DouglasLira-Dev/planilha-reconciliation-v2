package com.reconciliation.config;

import com.reconciliation.model.User;
import com.reconciliation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@reconciliation.com");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Usuário ADMIN criado: admin / admin123");
        }

        if (!userRepository.existsByUsername("operator")) {
            User operator = new User();
            operator.setUsername("operator");
            operator.setPassword(passwordEncoder.encode("operator123"));
            operator.setEmail("operator@reconciliation.com");
            operator.setRole(User.Role.OPERATOR);
            operator.setEnabled(true);
            userRepository.save(operator);
            log.info("Usuário OPERATOR criado: operator / operator123");
        }
    }
}