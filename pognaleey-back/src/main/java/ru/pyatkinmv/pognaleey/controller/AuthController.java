package ru.pyatkinmv.pognaleey.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pyatkinmv.pognaleey.dto.AuthRequestDto;
import ru.pyatkinmv.pognaleey.dto.UserDto;
import ru.pyatkinmv.pognaleey.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody AuthRequestDto request) {
    return ResponseEntity.ok(authService.authenticateUser(request));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(@RequestBody AuthRequestDto request) {
    var user = authService.registerUser(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }
}
