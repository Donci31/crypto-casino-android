package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.user.AuthResponseDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserLoginDto;
import hu.bme.aut.crypto_casino_backend.dto.user.UserRegistrationDto;
import hu.bme.aut.crypto_casino_backend.exception.ResourceAlreadyExistsException;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.mapper.UserMapper;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import hu.bme.aut.crypto_casino_backend.security.JwtService;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	private final AuthenticationManager authenticationManager;

	@Transactional
	public UserDto registerUser(UserRegistrationDto registrationDto) {
		if (userRepository.existsByUsername(registrationDto.getUsername())) {
			throw new ResourceAlreadyExistsException("Username already exists");
		}

		if (userRepository.existsByEmail(registrationDto.getEmail())) {
			throw new ResourceAlreadyExistsException("Email already exists");
		}

		User user = userMapper.fromRegistrationDto(registrationDto);
		user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));

		User savedUser = userRepository.save(user);

		return userMapper.toDto(savedUser);
	}

	public AuthResponseDto login(UserLoginDto loginDto) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));

		User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		UserDetails userDetails = UserPrincipal.create(user);

		String accessToken = jwtService.generateToken(userDetails);
		String refreshToken = jwtService.generateRefreshToken(userDetails);

		return AuthResponseDto.builder()
			.token(accessToken)
			.refreshToken(refreshToken)
			.tokenType("Bearer")
			.user(userMapper.toDto(user))
			.build();
	}

	public AuthResponseDto refreshToken(String refreshToken) {
		final String username = jwtService.extractUsername(refreshToken);
		User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		UserDetails userDetails = UserPrincipal.create(user);

		if (!jwtService.isTokenValid(refreshToken, userDetails)) {
			throw new IllegalArgumentException("Invalid refresh token");
		}

		String newAccessToken = jwtService.generateToken(userDetails);
		String newRefreshToken = jwtService.generateRefreshToken(userDetails);

		return AuthResponseDto.builder()
			.token(newAccessToken)
			.refreshToken(newRefreshToken)
			.tokenType("Bearer")
			.user(userMapper.toDto(user))
			.build();
	}

}
