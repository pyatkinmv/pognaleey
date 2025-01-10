package ru.pyatkinmv.pognaleey.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.pyatkinmv.pognaleey.model.User;

import java.util.Optional;

@Slf4j
public final class AuthenticatedUserProvider {

    public static User getCurrentUserOrThrow() {
        return getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user found"));
    }

    public static Optional<User> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        var principal = authentication.getPrincipal();

        if (!(principal instanceof User)) {
            log.warn("Wrong user type {}", principal.getClass().getName());
            return Optional.empty();
        }

        return Optional.of((User) principal);
    }
}
