package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.security.JwtProvider;
import ru.pyatkinmv.pognaleey.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequestDto request) {
        if (userService.isValidUser(request)) {
            return ResponseEntity.ok(jwtProvider.generateToken(request.username()));
        }

        throw new BadCredentialsException("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody AuthRequestDto request) {
        var user = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
