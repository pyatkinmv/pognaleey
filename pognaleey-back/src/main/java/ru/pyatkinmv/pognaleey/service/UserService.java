package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.UserRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public boolean isValidUser(AuthRequestDto authRequestDto) {
        var user = loadUserByUsername(authRequestDto.username());

        return passwordEncoder.matches(authRequestDto.password(), user.getPassword());
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
    }

    // TODO: implement tests
    private static void validateUsername(String username) {
        String errorMessage = null;

        if (username == null || username.isBlank()) {
            errorMessage = "Имя пользователя обязательно.";
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            errorMessage = "Имя пользователя может содержать только буквы, цифры, точки, дефисы и подчёркивания.";
        } else if (username.length() < 3 || username.length() > 20) {
            errorMessage = "Имя пользователя должно быть от 3 до 20 символов.";
        }

        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static void validatePassword(String password) {
        String errorMessage = null;

        if (password == null || password.isBlank()) {
            errorMessage = "Пароль обязателен.";
        } else if (password.length() < 3 || password.length() > 20) {
            errorMessage = "Пароль должен быть от 3 до 20 символов.";
        }

        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public UserDto registerUser(AuthRequestDto registerRequest) {
        validateUsername(registerRequest.username());
        validatePassword(registerRequest.password());

        if (userRepository.existsByUsername(registerRequest.username().toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("Username %s is already taken", registerRequest.username())
            );
        }

        return TravelMapper.toUserDto(userRepository.save(
                        User.builder()
                                .username(registerRequest.username().toLowerCase())
                                .password(passwordEncoder.encode(registerRequest.password()))
                                .createdAt(Instant.now())
                                .build()
                )
        );
    }

    public List<User> findUsersByIds(Collection<Long> usersIds) {
        return userRepository.findAllByIdIn(usersIds);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
}

