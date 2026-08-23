package com.wokioki.parcelflow.identity.auth;

import com.wokioki.parcelflow.identity.auth.dto.RegisterRequest;
import com.wokioki.parcelflow.identity.auth.dto.RegisterResponse;
import com.wokioki.parcelflow.identity.auth.exception.EmailAlreadyExistsException;
import com.wokioki.parcelflow.identity.user.Role;
import com.wokioki.parcelflow.identity.user.User;
import com.wokioki.parcelflow.identity.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email()
            .trim()
            .toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = new User(
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            request.firstName().trim(),
            request.lastName().trim(),
            Role.CUSTOMER
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole()
        );
    }
}
