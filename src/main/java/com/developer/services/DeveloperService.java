package com.developer.services;

import com.developer.enums.DevStatus;
import com.developer.enums.Role;
import com.developer.models.Developer;
import com.developer.repositories.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeveloperService {
    private final DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.developerRepository = developerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    public Developer getDeveloperById(Long id) {
        return developerRepository.findById(id).orElse(null);
    }

//    public Long saveDeveloper(Developer developer) {
//        developer.setPassword(passwordEncoder.encode(developer.getPassword()));
//        developer.setRole(Role.ROLE_USER);
//        developer.setStatus(DevStatus.ACTIVE);
//        return developerRepository.save(developer).getId();
//    }

    public Long deleteDeveloperById(Long id) {
        Developer developer = developerRepository.findById(id).orElse(null);
        if (developer == null)
            return null;

        developer.setStatus(DevStatus.DELETED);
        return id;
    }

    public Long updateDeveloper(Long id, Developer developer) {
        Developer existingDeveloper = developerRepository.findById(id).orElse(null);
        if (existingDeveloper == null)
            return null;

        existingDeveloper.setEmail(developer.getEmail());
        existingDeveloper.setPassword(developer.getPassword());
        existingDeveloper.setRole(developer.getRole());
        existingDeveloper.setFullName(developer.getFullName());
        existingDeveloper.setPosition(developer.getPosition());
        return developerRepository.save(existingDeveloper).getId();
    }

    public Optional<Developer> findByEmail(String email) {
        return developerRepository.findByEmail(email);
    }

    public List<Developer> findByTeamId(Long id) {
        return developerRepository.findByTeamIdAndStatus(id, DevStatus.ACTIVE);
    }

    public List<String> activateAccount(String email, String token) {
        Optional<Developer> developer = developerRepository.findByEmail(email);

        if (developer.isEmpty()) {
            return Collections.singletonList("Пользователь с таким email не найден");
        }

        if (!developer.get().getActivationToken().equals(token)) {
            return Collections.singletonList("Неправильный токен активации");
        }

        developer.get().setStatus(DevStatus.ACTIVE);
        developerRepository.save(developer.get());

        return Collections.singletonList("Аккаунт успешно активирован!");
    }

    public List<String> registerUser(Developer developer) throws MessagingException {
        if (developerRepository.findByEmail(developer.getEmail()).isPresent()) {
            return Collections.singletonList("Пользователь с таким email уже зарегистрирован");
        }

        developer.setActivationToken(UUID.randomUUID().toString());
        developer.setPassword(passwordEncoder.encode(developer.getPassword()));
        developer.setStatus(DevStatus.PENDING);
        developer.setRole(Role.ROLE_USER);
        developerRepository.save(developer);

        String activationLink = "http://localhost:8080/activate?email=" + developer.getEmail() +
                "&token=" + developer.getActivationToken();
        emailService.sendActivationEmail(developer.getEmail(), activationLink);
//        emailService.sendSimpleEmail(developer.getEmail(), "Активация аккаунта", activationLink);

        return Collections.singletonList("Регистрация прошла успешно, проверьте свою электронную почту для активации аккаунта");
    }

//    public void activateUser(String email) {
//        Optional<Developer> developer = developerRepository.findByEmail(email);
//        if (developer.isPresent()) {
//            developer.get().setActivated(true);
//            developerRepository.save(developer.get());
//        } else {
//            throw new RuntimeException("Пользователь не найден!");
//        }
//    }
//
//    public Developer createUser(Developer developer) {
//        Optional<Developer> existingUser = developerRepository.findByEmail(developer.getEmail());
//        if (existingUser.isPresent()) {
//            throw new RuntimeException("Email уже зарегистрирован!");
//        }
//        developer.setPassword(passwordEncoder.encode(developer.getPassword()));
//        developer.setRole(Role.ROLE_USER);
//        developer.setStatus(DevStatus.ACTIVE);
//        developer.setActivated(false);
//        developerRepository.save(developer);
//
//        String token = UUID.randomUUID().toString();
//        createEmailActivationToken(developer, token);
//
//        return developer;
//    }
//
//    private void createEmailActivationToken(Developer developer, String token) {
//        EmailActivationToken emailActivationToken = new EmailActivationToken();
//        emailActivationToken.setDeveloper(developer);
//        emailActivationToken.setToken(token);
//        emailActivationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
//        emailActivationTokenRepository.save(emailActivationToken);
//
//        // Отправить email с токеном пользователю
//        // Можно использовать Spring Boot Mail для отправки email
//    }
}
