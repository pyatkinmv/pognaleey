package ru.pyatkinmv.pognaleey.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.mapper.TravelMapper;
import ru.pyatkinmv.pognaleey.model.User;
import ru.pyatkinmv.pognaleey.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
    }

    public List<UserDto> findUsersByIds(Collection<Long> usersIds) {
        return userRepository.findAllByIdIn(usersIds)
                .stream()
                .map(TravelMapper::toUserDto)
                .toList();
    }

    public Optional<UserDto> findUserById(Long id) {
        return userRepository.findById(id).map(TravelMapper::toUserDto);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserDto create(User user) {
        return TravelMapper.toUserDto(userRepository.save(user));
    }
}

