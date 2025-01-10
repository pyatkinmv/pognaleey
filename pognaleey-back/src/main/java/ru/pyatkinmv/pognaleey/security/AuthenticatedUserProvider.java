package ru.pyatkinmv.pognaleey.security;

import org.springframework.security.core.context.SecurityContextHolder;
import ru.pyatkinmv.pognaleey.model.User;

import java.util.Optional;

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
            return Optional.empty();
        }

        return Optional.of((User) principal);
    }
}
