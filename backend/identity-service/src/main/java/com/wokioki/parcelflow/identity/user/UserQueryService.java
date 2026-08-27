package com.wokioki.parcelflow.identity.user;

import com.wokioki.parcelflow.identity.user.dto.CurrentUserResponse;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryService {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found")
            );

        return new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
    }
}
