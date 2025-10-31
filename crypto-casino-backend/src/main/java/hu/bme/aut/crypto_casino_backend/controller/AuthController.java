package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.user.AuthResponseDto;
import hu.bme.aut.crypto_casino_backend.dto.user.RefreshTokenRequest;
import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserLoginDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserRegistrationDto;
import hu.bme.aut.crypto_casino_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
		log.info("Registration request for username: {}", registrationDto.getUsername());
		AuthResponseDto authResponse = authService.registerUser(registrationDto);
		log.info("User registered successfully: {}", authResponse.getUser().getUsername());
		return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
		log.info("Login request for: {}", loginDto.getUsernameOrEmail());
		AuthResponseDto authResponse = authService.login(loginDto);
		log.info("Login successful for: {}", loginDto.getUsernameOrEmail());
		return ResponseEntity.ok(authResponse);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		log.info("Token refresh request");
		AuthResponseDto authResponse = authService.refreshToken(request.getRefreshToken());
		log.info("Token refresh successful");
		return ResponseEntity.ok(authResponse);
	}

}
