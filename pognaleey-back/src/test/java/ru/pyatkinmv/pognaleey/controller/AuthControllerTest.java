package ru.pyatkinmv.pognaleey.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.pyatkinmv.pognaleey.DatabaseCleaningTest;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest extends DatabaseCleaningTest {
    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void registerUser() {
        var request = new AuthRequestDto("test_user", "password123");
        var response = authController.registerUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var storedUser = userRepository.findByUsername("test_user");
        assertThat(storedUser).isPresent();

        var user = response.getBody();
        assertThat(user).isEqualTo(new UserDto(storedUser.get().getId(), "test_user"));

        assertThrows(
                IllegalArgumentException.class,
                () -> authController.registerUser(request),
                "Username test_user is already taken"
        );
    }

    @Test
    public void login() {
        var request = new AuthRequestDto("test_user", "password123");
        assertThrows(
                UsernameNotFoundException.class,
                () -> authController.login(request),
                "User test_user not found"
        );

        authController.registerUser(request);

        var response = authController.login(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var wrongPasswordRequest = new AuthRequestDto(request.username(), "wrong_password123");
        assertThrows(
                BadCredentialsException.class,
                () -> authController.login(wrongPasswordRequest),
                "Invalid credentials"
        );
    }
}
