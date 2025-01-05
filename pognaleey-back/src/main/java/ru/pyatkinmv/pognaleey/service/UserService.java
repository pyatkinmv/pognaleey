package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.UserRepository;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }

    public boolean isValidUser(AuthRequestDto authRequestDto) {
        var user = loadUserByUsername(authRequestDto.username());

        return passwordEncoder.matches(authRequestDto.password(), user.getPassword());
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
    }

    public UserDto registerUser(AuthRequestDto registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException(
                    String.format("Username %s is already taken", registerRequest.username())
            );
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));

        return toUserDto(userRepository.save(
                        User.builder()
                                .username(registerRequest.username())
                                .password(passwordEncoder.encode(registerRequest.password()))
                                .createdAt(Instant.now())
                                .build()
                )
        );
    }
}

