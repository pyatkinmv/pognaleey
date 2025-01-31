package ru.pyatkinmv.pognaleey.security;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.pyatkinmv.pognaleey.dto.UserDto;

@Slf4j
public final class AuthenticatedUserProvider {

  public static UserDto getCurrentUserOrThrow() {
    return getCurrentUser()
        .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
  }

  public static Optional<UserDto> getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    var principal = authentication.getPrincipal();

    if (!(principal instanceof UserDto)) {
      return Optional.empty();
    }

    return Optional.of((UserDto) principal);
  }
}
