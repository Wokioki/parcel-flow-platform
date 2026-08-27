package com.wokioki.parcelflow.identity.user;

import com.wokioki.parcelflow.identity.user.dto.CurrentUserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
        Authentication authentication
    ) {
        return userQueryService.getCurrentUser(
            authentication.getName()
        );
    }
}
