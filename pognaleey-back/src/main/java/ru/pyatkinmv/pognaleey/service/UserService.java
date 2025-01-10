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

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

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

    public UserDto registerUser(AuthRequestDto registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException(
                    String.format("Username %s is already taken", registerRequest.username())
            );
        }

        return TravelMapper.toUserDto(userRepository.save(
                        User.builder()
                                .username(registerRequest.username())
                                .password(passwordEncoder.encode(registerRequest.password()))
                                .createdAt(Instant.now())
                                .build()
                )
        );
    }

    public List<User> findUsersByIds(Collection<Long> usersIds) {
        return userRepository.findAllByIdIn(usersIds);
    }
}

