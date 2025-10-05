package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.user.AuthResponseDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserLoginDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserRegistrationDto;
import hu.bme.aut.crypto_casino_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
		UserDto registeredUser = authService.registerUser(registrationDto);
		return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
		AuthResponseDto authResponse = authService.login(loginDto);
		return ResponseEntity.ok(authResponse);
	}

}
