package ru.pyatkinmv.pognaleey.service;

import java.time.Instant;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.model.UserRole;
import ru.pyatkinmv.pognaleey.security.JwtProvider;

@RequiredArgsConstructor
@Service
public class AuthService {
  private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

  private final UserService userService;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  private static void validateUsername(String username) {
    String errorMessage = null;
    if (username == null || username.isBlank()) {
      errorMessage = "Username is required.";
    } else if (!USERNAME_PATTERN.matcher(username).matches()) {
      errorMessage =
          "Username can only contain letters, digits, periods, hyphens, and underscores.";
    } else if (username.length() < 3 || username.length() > 20) {
      errorMessage = "Username must be between 3 and 20 characters long.";
    }
    if (errorMessage != null) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private static void validatePassword(String password) {
    String errorMessage = null;
    if (password == null || password.isBlank()) {
      errorMessage = "Password is required.";
    } else if (password.length() < 3 || password.length() > 20) {
      errorMessage = "Password must be between 3 and 20 characters long.";
    }
    if (errorMessage != null) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  public String authenticateUser(AuthRequestDto request) {
    var user = loadUserAndValidatePassword(request);
    return jwtProvider.generateToken(user.getId(), user.getUsername(), user.getAuthorities());
  }

  private User loadUserAndValidatePassword(AuthRequestDto authRequestDto) {
    var user = userService.loadUserByUsername(authRequestDto.username().toLowerCase());

    var isPasswordValid = passwordEncoder.matches(authRequestDto.password(), user.getPassword());

    if (isPasswordValid) {
      return user;
    } else {
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  public UserDto registerUser(AuthRequestDto registerRequest) {
    validateUsername(registerRequest.username());
    validatePassword(registerRequest.password());

    if (userService.existsByUsername(registerRequest.username().toLowerCase())) {
      throw new IllegalArgumentException(
          String.format("Username %s is already taken", registerRequest.username()));
    }

    return userService.create(
        User.builder()
            .username(registerRequest.username().toLowerCase())
            .password(passwordEncoder.encode(registerRequest.password()))
            .role(UserRole.USER)
            .createdAt(Instant.now())
            .build());
  }
}
